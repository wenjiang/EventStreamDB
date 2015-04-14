package com.zwb.args.dbpratice.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zwb.args.dbpratice.annotation.Column;
import com.zwb.args.dbpratice.annotation.ColumnType;
import com.zwb.args.dbpratice.annotation.Table;
import com.zwb.args.dbpratice.db.BaseSQLiteOpenHelper;
import com.zwb.args.dbpratice.event.BaseEvent;
import com.zwb.args.dbpratice.event.EventStream;
import com.zwb.args.dbpratice.event.InsertEvent;
import com.zwb.args.dbpratice.event.QueryEvent;
import com.zwb.args.dbpratice.exception.NoRecordException;
import com.zwb.args.dbpratice.exception.NoSuchTableException;
import com.zwb.args.dbpratice.exception.NoTableException;
import com.zwb.args.dbpratice.exception.NoTagException;
import com.zwb.args.dbpratice.model.BaseTable;
import com.zwb.args.dbpratice.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    private Context context;
    private SQLiteDatabase db;
    private BaseSQLiteOpenHelper helper;

    private DatabaseCache(Context context) {
        this.context = context;
        insertEventMap = EventStream.getInstance().getInsertEventMap();
        insertTagSet = insertEventMap.keySet();
        updateEventMap = EventStream.getInstance().getUpdateEventMap();
        updateTagSet = updateEventMap.keySet();
        if (helper == null) {
            helper = BaseSQLiteOpenHelper.getInstance(context);
            db = helper.getWritableDatabase();
        }
    }

    /**
     * DatabaseCache的单例方法
     *
     * @return DatabaseCache的单例
     */
    public static DatabaseCache getInstance(Context context) {
        if (cache == null) {
            cache = new DatabaseCache(context);
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
        updateTag = tableClazz.getSimpleName().toLowerCase() + "_query_update_";
        insertTag = tableClazz.getSimpleName().toLowerCase() + "_query_insert_";
        queryTag = tableClazz.getSimpleName().toLowerCase() + "_query";
        return this;
    }

    /**
     * 查询所有数据
     *
     * @param <T> 查询的表对象的类型
     * @return 查询的数据的List
     * @throws NoTableException
     * @throws NoTagException
     * @throws NoRecordException
     */
    public <T> List<T> findAll() throws NoTableException, NoTagException, NoRecordException {
        boolean isInsert = false;
        List<T> records = new ArrayList<T>();
        Map<Integer, T> recordMap = getInsertRecord(records);
        if (records.size() > 0) {
            isInsert = true;
        }
        List<T> dataList = new ArrayList<T>();

        updateRecord(recordMap);

        if (!isInsert) {
            throw new NoTagException("There is no event match the tag");
        }

        if (recordMap.size() <= 0) {
            throw new NoRecordException("There is no record");
        }

        Set<Integer> indexSet = recordMap.keySet();
        for (int index : indexSet) {
            T data = (T) recordMap.get(index);
            dataList.add(data);
        }

        return dataList;
    }

    private <T> Map<Integer, T> getInsertRecord(List<T> records) {
        Map<Integer, T> recordMap = new HashMap<Integer, T>();
        for (String tag : insertTagSet) {
            if (tag.contains(tableClazz.getSimpleName().toLowerCase())) {
                int index = getIndex(tag);
                QueryEvent queryEvent = (QueryEvent) insertEventMap.get(tag);
                T object = (T) queryEvent.getRecord();
                records.add(object);
                recordMap.put(index, object);
            }
        }

        return recordMap;
    }

    private <T> void updateRecord(Map<Integer, T> recordMap) {
        for (String tag : updateTagSet) {
            if (tag.contains(tableClazz.getSimpleName().toLowerCase())) {
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
        }
    }

    /**
     * where条件的构建
     *
     * @param column 列名
     * @param value  值
     * @return DatabaseCache的单例
     */
    public DatabaseCache where(String column, Object value) {
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
        Map<Integer, T> recordMap = getInsertRecord(records);
        if (records.size() > 0) {
            isInsert = true;
        }
        List<T> dataList = new ArrayList<T>();

        updateRecord(recordMap);

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
        List<T> records = new ArrayList<T>();
        Map<Integer, T> recordMap = getInsertRecord(records);
        if (records.size() > 0) {
            isInsert = true;
        }

        updateRecord(recordMap);

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

    public <T> void insertToDb(Class<T> clazz) throws NoTagException, NoRecordException, NoSuchTableException, NoTableException {
        List<T> dataList = from(clazz).findAll();
        db.beginTransaction();
        BaseTable[] array = dataList.toArray(new BaseTable[0]);
        for (BaseTable data : array) {
            save(data);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * 保存
     *
     * @throws NoSuchTableException
     */
    public <T> void save(T data) throws NoSuchTableException {
        String tableName = "";
        Field[] fields = data.getClass().getDeclaredFields();
        if (data.getClass().isAnnotationPresent(Table.class)) {
            Table table = data.getClass().getAnnotation(Table.class);
            tableName = table.table();
            if (tableName.length() == 0) {
                throw new NoSuchTableException("The table + " + getClass().getSimpleName() + " is not exist");
            }
        }

        ContentValues values = new ContentValues();
        for (java.lang.reflect.Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column meta = field.getAnnotation(Column.class);
                String column = meta.column();
                if (column.equals("")) {
                    column = field.getName();
                }

                String type = "";
                if (field.isAnnotationPresent(ColumnType.class)) {
                    ColumnType fieldType = field.getAnnotation(ColumnType.class);
                    type = fieldType.ColumnType();
                } else {
                    type = field.getType().getName();
                }
                field.setAccessible(true);
                if (!type.equals("")) {
                    Object value = null;
                    try {
                        value = field.get(data);
                    } catch (IllegalAccessException e) {
                        LogUtil.e(e.toString());
                    }
                    if (type.contains("String")) {
                        values.put(column, value.toString());
                    } else if (type.equals("int")) {
                        values.put(column, (int) value);
                    } else if (type.equals("double")) {
                        values.put(column, (double) value);
                    } else if (type.equals("float")) {
                        values.put(column, (float) value);
                    } else if (type.equals("boolean")) {
                        values.put(column, (boolean) value);
                    } else if (type.equals("long")) {
                        values.put(column, (long) value);
                    } else if (type.equals("short")) {
                        values.put(column, (short) value);
                    }
                }
            }
        }
        db.insert(tableName, null, values);
    }

    public <T> List<T> readFromDb(Class<T> clazz) throws NoTableException {
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<String>();
        Map<String, String> types = new HashMap<String, String>();
        for (Field field : fields) {
            fieldNames.add(field.getName());
            if (field.isAnnotationPresent(ColumnType.class)) {
                ColumnType fieldType = field.getAnnotation(ColumnType.class);
                types.put(field.getName().toLowerCase(), fieldType.ColumnType());
            }
        }

        List<Method> setMethods = getSetMethods(clazz);
        Cursor cursor = db.query(clazz.getSimpleName(), null, null, null, null, null, null);//查询并获得游标
        List<T> list = getList(clazz, cursor, setMethods, fieldNames, fields, types);
        for (T data : list) {
            InsertEvent insertEvent = new InsertEvent();
            insertEvent.to(clazz).insert(data);
        }
        cursor.close();
        return list;
    }

    /**
     * 获取数据
     *
     * @param clazz      表对象的class对象
     * @param cursor     光标
     * @param methods    方法List
     * @param fieldNames 字段名
     * @param fields     字段数组
     * @param types      字段类型数组
     * @param <T>        表对象的类型
     * @return 表对象的List
     */
    private <T> List<T> getList(Class<T> clazz, Cursor cursor, List<Method> methods, List<String> fieldNames, Field[] fields, Map<String, String> types) {
        List<T> list = new ArrayList<T>();
        Constructor<?> constructor = findBestSuitConstructor(clazz);
        Set<String> keySet = types.keySet();
        while (cursor.moveToNext()) {
            try {
                T data = (T) constructor
                        .newInstance();
                for (Method method : methods) {
                    String name = method.getName();
                    String valueName = name.substring(3).substring(0, 1).toLowerCase() + name.substring(4);
                    String type = null;
                    String fieldType = null;
                    int index = 0;
                    if (fieldNames.contains(valueName)) {
                        index = fieldNames.indexOf(valueName);
                        type = fields[index].getGenericType().toString();
                        if (keySet.contains(valueName)) {
                            fieldType = types.get(valueName);
                        }
                    }
                    Object value = getColumnValue(cursor, valueName, type, fieldType);
                    fields[index].setAccessible(true);
                    fields[index].set(data, value);
                }

                list.add(data);
            } catch (InstantiationException e) {
                LogUtil.e(e.toString());
            } catch (IllegalAccessException e) {
                LogUtil.e(e.toString());
            } catch (InvocationTargetException e) {
                LogUtil.e(e.toString());
            } catch (JSONException e) {
                LogUtil.e(e.toString());
            }
        }
        return list;
    }

    /**
     * 获取列值
     *
     * @param cursor    光标
     * @param column    列名
     * @param fieldType 字段类型
     * @param dbType    数据库字段类型
     * @return 列值
     * @throws JSONException
     */
    public Object getColumnValue(Cursor cursor, String column, String fieldType, String dbType) throws JSONException {
        Object data = null;

        if (dbType == null) {
            dbType = fieldType;
        }
        if (dbType.equals("String") || dbType.contains("String")) {
            data = cursor.getString(cursor.getColumnIndex(column));
            if (fieldType != null && fieldType.contains("JSONObject")) {
                data = new JSONObject((String) data);
            } else if (fieldType != null && fieldType.contains("JSONArray")) {
                data = new JSONArray((String) data);
            }
        } else if (dbType.equals("Integer") || dbType.equals("int")) {
            data = cursor.getInt(cursor.getColumnIndex(column));
        } else if (dbType.equals("Long") || dbType.equals("long")) {
            data = cursor.getLong(cursor.getColumnIndex(column));
        } else if (dbType.equals("Double") || dbType.equals("double")) {
            data = cursor.getDouble(cursor.getColumnIndex(column));
        } else if (dbType.equals("Float") || dbType.equals("float")) {
            data = cursor.getFloat(cursor.getColumnIndex(column));
        } else if (dbType.equals("Short") || dbType.equals("short")) {
            data = cursor.getShort(cursor.getColumnIndex(column));
        }
        return data;
    }

    /**
     * 寻找最适合的构造器
     *
     * @param modelClass 表对象的class对象
     * @return 构造器
     */
    private Constructor<?> findBestSuitConstructor(Class<?> modelClass) {
        Constructor<?> finalConstructor = null;
        Constructor<?>[] constructors = modelClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (finalConstructor == null) {
                finalConstructor = constructor;
            } else {
                int finalParamLength = finalConstructor.getParameterTypes().length;
                int newParamLength = constructor.getParameterTypes().length;
                if (newParamLength < finalParamLength) {
                    finalConstructor = constructor;
                }
            }
        }
        finalConstructor.setAccessible(true);
        return finalConstructor;
    }

    /**
     * 获取Set方法
     *
     * @param clazz 表对象的class对象
     * @return 方法数组
     */
    private List<Method> getSetMethods(Class clazz) {
        Method[] allMethods = clazz.getMethods();
        List<Method> setMethods = new ArrayList<Method>();
        for (Method method : allMethods) {
            String name = method.getName();

            if (name.contains("set") && !name.equals("offset")) {
                setMethods.add(method);
                continue;
            }
        }

        return setMethods;
    }

    public Set<Class<?>> getTableSet() {
        Set<Class<?>> tableClazz = new HashSet<Class<?>>();
        Set<String> tableSet = helper.getTableSet();
        for (String table : tableSet) {
            try {
                Class clazz = Class.forName(table);
                tableClazz.add(clazz);
            } catch (ClassNotFoundException e) {
                LogUtil.e(e.toString());
            }
        }

        return tableClazz;
    }
}
