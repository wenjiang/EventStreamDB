package com.zwb.args.dbpratice.event;

import com.zwb.args.dbpratice.util.LogUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by pc on 2015/4/9.
 */
public class UpdateEvent extends BaseDataChangeEvent {
    private Class<?> tableClazz;

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
}
