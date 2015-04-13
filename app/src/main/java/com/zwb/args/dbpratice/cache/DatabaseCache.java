package com.zwb.args.dbpratice.cache;

import com.zwb.args.dbpratice.event.BaseEvent;
import com.zwb.args.dbpratice.event.EventStream;
import com.zwb.args.dbpratice.event.QueryEvent;
import com.zwb.args.dbpratice.exception.NoRecordException;
import com.zwb.args.dbpratice.exception.NoTagException;
import com.zwb.args.dbpratice.util.LogUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据缓存，用于提取事件中的数据
 * Created by pc on 2015/4/8.
 */
public class DatabaseCache {
    private static DatabaseCache cache;
    private Class<?> tableClazz;
    private String updateTag;
    private String insertTag;
    private Object queryValue;
    private String queryColumn;
    private String queryTag;
    private Set<String> insertTagSet;
    private Set<String> updateTagSet;
    private Map<String, BaseEvent> insertEventMap;
    private Map<String, BaseEvent> updateEventMap;

    private DatabaseCache() {
        insertEventMap = EventStream.getInstance().getInsertEventMap();
        insertTagSet = insertEventMap.keySet();
        updateEventMap = EventStream.getInstance().getUpdateEventMap();
        updateTagSet = updateEventMap.keySet();
    }

    /**
     * DatabaseCache的单例方法
     *
     * @return DatabaseCache的单例
     */
    public static DatabaseCache getInstance() {
        if (cache == null) {
            cache = new DatabaseCache();
        }

        return cache;
    }

    /**
     * 要查询的表
     *
     * @param clazz 表对象的class对象
     * @param <T>   表对象的类型
     * @return DatabaseCache的单例
     */
    public <T> DatabaseCache from(Class<T> clazz) {
        this.tableClazz = clazz;
        return this;
    }

//    public <T> List<T> findAll() throws NoTableException {
//        if (tableClazz == null) {
//            throw new NoTableException("There is no table");
//        }
//
//        List<T> dataList = new ArrayList<T>();
//        for (String tag : tagSet) {
//            if (tag.contains(tableClazz.getSimpleName().toLowerCase()) && tag.contains("update")) {
//                Map<Class<?>, Map<Integer, Object>> tableMap = ((InsertEvent) (eventMap.get(tag))).getTableData();
//                Map<Integer, Object> dataMap = tableMap.get(tableClazz);
//                Set<Integer> indexSet = dataMap.keySet();
//                for (Integer index : indexSet) {
//                    dataList.add((T) dataMap.get(index));
//                }
//                break;
//            }
//        }

//        return dataList;
//    }

    /**
     * where条件的构建
     *
     * @param column 列名
     * @param value  值
     * @return DatabaseCache的单例
     */
    public DatabaseCache where(String column, Object value) {
        updateTag = tableClazz.getSimpleName().toLowerCase() + "_query_update_";
        insertTag = tableClazz.getSimpleName().toLowerCase() + "_query_insert_";
        queryTag = tableClazz.getSimpleName().toLowerCase() + "_query";
        queryValue = value;
        queryColumn = column;
        return this;
    }

    /**
     * 查询所有的数据
     *
     * @param <T> 要返回的类型
     * @return 数据的List
     * @throws NoTagException
     * @throws NoRecordException
     */
    public <T> List<T> find() throws NoTagException, NoRecordException {
        boolean isInsert = false;
        List<T> records = new ArrayList<T>();
        Map<Integer, T> recordMap = new HashMap<Integer, T>();
        List<T> dataList = new ArrayList<T>();

        for (String tag : insertTagSet) {
            int index = getIndex(tag);
            QueryEvent queryEvent = (QueryEvent) insertEventMap.get(tag);
            T object = (T) queryEvent.getRecord();
            records.add(object);
            isInsert = true;
            recordMap.put(index, object);
        }

        for (String tag : updateTagSet) {
            int index = getIndex(tag);
            QueryEvent queryEvent = (QueryEvent) updateEventMap.get(tag);
            String column = getColumn(tag);
            Object data = queryEvent.getRecord();
            Object originData = recordMap.get(index);
            Field[] fields = data.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (recordMap.containsKey(index) && field.getName().equals(column)) {
                    T newData = (T) setValueFromOther(originData, data, field.getName());
                    recordMap.put(index, newData);
                }
            }
        }

        if (!isInsert) {
            throw new NoTagException("There is no event match the tag");
        }

        if (recordMap.size() <= 0) {
            throw new NoRecordException("There is no record");
        }

        Set<Integer> indexSet = recordMap.keySet();
        for (int index : indexSet) {
            T data = (T) recordMap.get(index);
            Field[] fields = data.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(queryColumn)) {
                    field.setAccessible(true);
                    String type = field.getType().getSimpleName();
                    try {
                        if (type.equals("String") || type.contains("String")) {
                            String columnData = (String) field.get(data);
                            if (columnData.equals((String) queryValue)) {
                                dataList.add((T) recordMap.get(index));
                            }
                        } else if (type.equals("Integer") || type.equals("int")) {
                            int columnData = field.getInt(data);
                            if (columnData == (int) queryValue) {
                                dataList.add((T) recordMap.get(index));
                            }
                        } else if (type.equals("Long") || type.equals("long")) {
                            long columnData = field.getLong(data);
                            if (columnData == (long) queryValue) {
                                dataList.add((T) recordMap.get(index));
                            }
                        } else if (type.equals("Double") || type.equals("double")) {
                            double columnData = field.getDouble(data);
                            if (columnData == (double) queryValue) {
                                dataList.add((T) recordMap.get(index));
                            }
                        } else if (type.equals("Float") || type.equals("float")) {
                            int columnData = field.getInt(data);
                            if (columnData == (int) queryValue) {
                                dataList.add((T) recordMap.get(index));
                            }
                        } else if (type.equals("Short") || type.equals("short")) {
                            int columnData = field.getInt(data);
                            if (columnData == (int) queryValue) {
                                dataList.add((T) recordMap.get(index));
                            }
                        }
                    } catch (IllegalAccessException e) {
                        LogUtil.e(e.toString());
                    }
                }
            }
        }
        return dataList;
    }

    /**
     * 查询符合某个条件的数据
     *
     * @param column 列名
     * @param clazz  表对象的class对象
     * @param <T>    表对象的类型
     * @return 符合条件的数据
     * @throws NoTagException
     * @throws NoRecordException
     */
    public <T> T find(String column, Class<T> clazz) throws NoTagException, NoRecordException {
        updateTag = tableClazz.getSimpleName().toLowerCase() + "_query_update_" + column;
        boolean isInsert = false;
        List<Object> records = new ArrayList<Object>();
        Map<Integer, Object> recordMap = new HashMap<Integer, Object>();
        for (String tag : insertTagSet) {
            int index = getIndex(tag);
            QueryEvent queryEvent = (QueryEvent) insertEventMap.get(tag);
            T object = (T) queryEvent.getRecord();
            records.add(object);
            isInsert = true;
            recordMap.put(index, object);
        }

        for (String tag : updateTagSet) {
            int index = getIndex(tag);
            QueryEvent queryEvent = (QueryEvent) updateEventMap.get(tag);
            Object data = queryEvent.getRecord();
            Object originData = recordMap.get(index);
            Field[] fields = data.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (recordMap.containsKey(index)) {
                    Object newData = setValueFromOther(originData, data, field.getName());
                    recordMap.put(index, newData);
                }
            }
        }


        if (!isInsert) {
            throw new NoTagException("There is no event match the tag");
        }

        if (records.size() <= 0) {
            throw new NoRecordException("There is no record");
        }

        return (T) getColumnValue(records.get(0), column);
    }

    /**
     * 更新数据
     *
     * @param originData 原先的数据
     * @param updateData 更新的数据
     * @param column     列名
     * @param <T>        数据的类型
     * @return 更新完数据的原先数据
     */
    private <T> T setValueFromOther(T originData, T updateData, String column) {
        Field[] originFields = originData.getClass().getDeclaredFields();
        Field[] updateFields = updateData.getClass().getDeclaredFields();
        int length = originFields.length;
        for (int i = 0; i < length; i++) {
            Field originField = originFields[i];
            Field updateField = updateFields[i];
            originField.setAccessible(true);
            updateField.setAccessible(true);
            if ((originField.getName()).equals(column)) {
                String type = originField.getType().getSimpleName();
                try {
                    originField.set(originData, getFieldValue(updateData, updateField, type));
                } catch (IllegalAccessException e) {
                    LogUtil.e(e.toString());
                }
            }
        }
        return originData;
    }

    /**
     * 获取事件对应的数据的位置
     *
     * @param tag 事件的tag
     * @return 对应数据的位置
     */
    private int getIndex(String tag) {
        String[] strArr = tag.split("_");
        int index = Integer.valueOf(strArr[strArr.length - 1]);
        return index;
    }

    /**
     * 获取列名
     *
     * @param tag 事件的tag
     * @return 列名
     */
    private String getColumn(String tag) {
        String[] strArr = tag.split("_");
        String column = strArr[strArr.length - 2];
        return column;
    }

    /**
     * 获取列值
     *
     * @param data   数据
     * @param column 列名
     * @return 列值
     */
    private Object getColumnValue(Object data, String column) {
        String methodName = getGetMethodName(column);
        Object value = null;
        try {
            Method method = data.getClass().getMethod(methodName);
            value = method.invoke(data);
        } catch (NoSuchMethodException e) {
            LogUtil.e(e.toString());
        } catch (InvocationTargetException e) {
            LogUtil.e(e.toString());
        } catch (IllegalAccessException e) {
            LogUtil.e(e.toString());
        }
        return value;
    }

    /**
     * 获取get方法
     *
     * @param column 列名
     * @return get方法的方法名
     */
    private String getGetMethodName(String column) {
        String firstChar = column.substring(0, 1);
        String methodName = "get" + firstChar.toUpperCase() + column.substring(1, column.length());
        return methodName;
    }

    /**
     * 获取字段的值
     *
     * @param obj   数据
     * @param field 字段
     * @param type  类型
     * @param <T>   数据的类型
     * @return 字段的值
     */
    private <T> Object getFieldValue(T obj, Field field, String type) {
        Object data = null;
        try {
            if (type.equals("String") || type.contains("String")) {
                data = field.get(obj);
            } else if (type.equals("Integer") || type.equals("int")) {
                data = field.getInt(obj);
            } else if (type.equals("Long") || type.equals("long")) {
                data = field.getLong(obj);
            } else if (type.equals("Double") || type.equals("double")) {
                data = field.getDouble(obj);
            } else if (type.equals("Float") || type.equals("float")) {
                data = field.getFloat(obj);
            } else if (type.equals("Short") || type.equals("short")) {
                data = field.getShort(obj);
            }
        } catch (IllegalAccessException e) {
            LogUtil.e(e.toString());
        }
        return data;
    }
}
