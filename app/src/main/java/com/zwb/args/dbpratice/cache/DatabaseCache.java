package com.zwb.args.dbpratice.cache;

import com.zwb.args.dbpratice.event.BaseEvent;
import com.zwb.args.dbpratice.event.EventStream;
import com.zwb.args.dbpratice.event.QueryEvent;
import com.zwb.args.dbpratice.exception.NoColumnChangeException;
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
 * Created by pc on 2015/4/8.
 */
public class DatabaseCache {
    private static DatabaseCache cache;
    private Class<?> tableClazz;
    private String updateTag;
    private String insertTag;
    private Object queryValue;
    private Map<String, BaseEvent> eventMap;
    private Set<String> tagSet;
    private String queryColumn;
    private String queryTag;

    private DatabaseCache() {
        eventMap = EventStream.getInstance().getEventMap();
        tagSet = eventMap.keySet();
    }

    public static DatabaseCache getInstance() {
        if (cache == null) {
            cache = new DatabaseCache();
        }

        return cache;
    }

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

    public DatabaseCache where(String column, Object value) {
        updateTag = tableClazz.getSimpleName().toLowerCase() + "_query_update_";
        insertTag = tableClazz.getSimpleName().toLowerCase() + "_query_insert_";
        queryTag = tableClazz.getSimpleName().toLowerCase() + "_query";
        queryValue = value;
        queryColumn = column;
        return this;
    }

    public <T> List<T> find() throws NoTagException, NoRecordException {
        boolean isInsert = false;
        List<T> records = new ArrayList<T>();
        Map<Integer, T> recordMap = new HashMap<Integer, T>();
        List<T> dataList = new ArrayList<T>();
        for (String tag : tagSet) {
            int index = getIndex(tag);
            QueryEvent queryEvent = (QueryEvent) eventMap.get(tag);
            if (tag.contains(insertTag)) {
                T object = (T) queryEvent.getRecord();
                records.add(object);
                isInsert = true;
                recordMap.put(index, object);
                continue;
            }
        }

        for (String tag : tagSet) {
            int index = getIndex(tag);
            QueryEvent queryEvent = (QueryEvent) eventMap.get(tag);
            if (tag.contains(updateTag)) {
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
                continue;
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

    public <T> T find(String column, Class<T> clazz) throws NoTagException, NoColumnChangeException, NoRecordException {
        updateTag = tableClazz.getSimpleName().toLowerCase() + "_query_update_" + column;
        boolean isInsert = false;
        List<Object> records = new ArrayList<Object>();
        Map<Integer, Object> recordMap = new HashMap<Integer, Object>();
        for (String tag : tagSet) {
            int index = getIndex(tag);
            QueryEvent queryEvent = (QueryEvent) eventMap.get(tag);
            if (tag.contains(insertTag)) {
                Object object = queryEvent.getRecord();
                records.add(object);
                isInsert = true;
                recordMap.put(index, object);
                continue;
            }
        }

        for (String tag : tagSet) {
            int index = getIndex(tag);
            QueryEvent queryEvent = (QueryEvent) eventMap.get(tag);
            if (tag.contains(updateTag)) {
                Object data = queryEvent.getRecord();
                Object originData = recordMap.get(index);
                Field[] fields = data.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (recordMap.containsKey(index)) {
                        Object newData = setValueFromOther(originData, data, field.getName());
                        recordMap.put(index, newData);
                    }
                }
                continue;
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
                    if (type.equals("String") || type.contains("String")) {
                        String columnData = (String) updateField.get(updateData);
                        originField.set(originData, columnData);
                    } else if (type.equals("Integer") || type.equals("int")) {
                        originField.set(originData, updateField.getInt(updateData));
                    } else if (type.equals("Long") || type.equals("long")) {
                        originField.set(originData, updateField.getLong(updateData));
                    } else if (type.equals("Double") || type.equals("double")) {
                        originField.set(originData, updateField.getDouble(updateData));
                    } else if (type.equals("Float") || type.equals("float")) {
                        originField.set(originData, updateField.getFloat(updateData));
                    } else if (type.equals("Short") || type.equals("short")) {
                        originField.set(originData, updateField.getShort(updateData));
                    }
                } catch (IllegalAccessException e) {
                    LogUtil.e(e.toString());
                }
            }
        }
        return originData;
    }

    private int getIndex(String tag) {
        String[] strArr = tag.split("_");
        int index = Integer.valueOf(strArr[strArr.length - 1]);
        return index;
    }

    private String getColumn(String tag) {
        String[] strArr = tag.split("_");
        String column = strArr[strArr.length - 2];
        return column;
    }

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

    private String getGetMethodName(String column) {
        String firstChar = column.substring(0, 1);
        String methodName = "get" + firstChar.toUpperCase() + column.substring(1, column.length());
        return methodName;
    }
}
