package com.zwb.args.dbpratice.event;

import android.util.Log;

import com.zwb.args.dbpratice.exception.NoTableException;
import com.zwb.args.dbpratice.exception.NoTagException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pc on 2015/4/8.
 */
public class UpdateEvent extends BaseEvent {
    private Class<?> tableClazz;
    private Map<Integer, Object> recordMap;
    private Map<Class<?>, Map<Integer, Object>> tableMap;
    private int index = 0;

    public UpdateEvent() {
        recordMap = new HashMap<Integer, Object>();
        tableMap = new HashMap<Class<?>, Map<Integer, Object>>();
    }

    public UpdateEvent update(String column, Object value) throws NoTableException {
        if (tableClazz == null) {
            throw new NoTableException("There is no table");
        }

        Object data = getData(column, value);
        recordMap.put(index, data);
        tableMap.put(tableClazz, recordMap);
        index++;
        return this;
    }

    public Map<Class<?>, Map<Integer, Object>> getTableData() {
        return tableMap;
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
            Log.e("UpdateEvent", e.toString());
        } catch (IllegalAccessException e) {
            Log.e("UpdateEvent", e.toString());
        } catch (InvocationTargetException e) {
            Log.e("UpdateEvent", e.toString());
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

    public <T> UpdateEvent to(Class<T> clazz) {
        this.tableClazz = clazz;
        return this;
    }

    public void commit(String tag) throws NoTagException {
        this.tag = tag;
        this.index = 0;
        if (tag == null) {
            throw new NoTagException("There is no tag");
        }
    }
}
