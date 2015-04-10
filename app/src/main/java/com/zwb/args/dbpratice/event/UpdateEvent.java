package com.zwb.args.dbpratice.event;

import com.zwb.args.dbpratice.util.LogUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by pc on 2015/4/9.
 */
public class UpdateEvent extends BaseDataChangeEvent {
    private Map<Integer, Object> dataMap;

    public UpdateEvent() {
        dataMap = new HashMap<Integer, Object>();
    }

    private <T> Object getData(String column, Object value) {
        Constructor constructor = findBestSuitConstructor(tableClazz);
        T data = null;
        try {
            data = (T) constructor.newInstance();
            Field[] fields = tableClazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(column)) {
                    field.setAccessible(true);
                    field.set(data, value);
                }
            }
        } catch (InstantiationException e) {
            LogUtil.e(e.toString());
        } catch (IllegalAccessException e) {
            LogUtil.e(e.toString());
        } catch (InvocationTargetException e) {
            LogUtil.e(e.toString());
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

    @Override
    public <T> UpdateEvent to(Class<T> clazz) {
        this.tableClazz = clazz;
        return this;
    }

    public <T> UpdateEvent where(String column, Object value) {
        dataMap = stream.getRecordMap();
        Set<Integer> indexSet = dataMap.keySet();
        for (int indexValue : indexSet) {
            T oldData = (T) dataMap.get(indexValue);
            if (isValueMatch(column, value, oldData)) {
                dataMap.put(indexValue, oldData);
            }
        }
        return this;
    }

    private <T> boolean isValueMatch(String column, Object value, T oldData) {
        Field[] fields = oldData.getClass().getDeclaredFields();
        boolean isMatch = false;
        for (Field field : fields) {
            if (field.getName().contains(column)) {
                isMatch = isMatch(field, value, oldData);
                break;
            }
        }
        return isMatch;
    }

    private <T> boolean isMatch(Field field, Object value, T data) {
        boolean isMatch = false;
        try {
            String type = field.getType().getSimpleName();
            field.setAccessible(true);
            if (type.equals("String") || type.contains("String")) {
                String columnData = (String) field.get(data);
                if (columnData.equals((String) value)) {
                    isMatch = true;
                }
            } else if (type.equals("Integer") || type.equals("int")) {
                if (field.getInt(data) == (int) value) {
                    isMatch = true;
                }
            } else if (type.equals("Long") || type.equals("long")) {
                if (field.getLong(data) == (long) value) {
                    isMatch = true;
                }
            } else if (type.equals("Double") || type.equals("double")) {
                if (field.getDouble(data) == (double) value) {
                    isMatch = true;
                }
            } else if (type.equals("Float") || type.equals("float")) {
                if (field.getFloat(data) == (float) value) {
                    isMatch = true;
                }
            } else if (type.equals("Short") || type.equals("short")) {
                if (field.getShort(data) == (short) value) {
                    isMatch = true;
                }
            }
        } catch (IllegalAccessException e) {
            LogUtil.e(e.toString());
        }
        return isMatch;
    }

    public UpdateEvent update(String column, String value) {
        Set<Integer> indexSet = dataMap.keySet();
        for (int indexValue : indexSet) {
            QueryEvent queryEvent = new QueryEvent();
            queryEvent.insertRecord(getData(column, value));
            queryEvent.setTag(tableClazz.getSimpleName().toLowerCase() + "_query_update_" + column + "_" + indexValue);
            stream.register(queryEvent);
        }
        return this;
    }
}
