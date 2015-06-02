package com.zwb.args.dbpratice;

import com.zwb.args.dbpratice.util.LogUtil;
import com.zwb.args.dbpratice.util.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 2015/6/1.
 */
public class MockObject {
    private Object obj;
    private List<Class<?>> parameterList;
    private Object value;

    private MockObject() {
    }

    public MockObject(Class<?> clazz) {
        parameterList = new ArrayList<>();
        try {
            obj = clazz.newInstance();
        } catch (InstantiationException e) {
            LogUtil.e(e.toString());
        } catch (IllegalAccessException e) {
            LogUtil.e(e.toString());
        }
    }

    public MockObject setValue(String valueName) throws Exception {
        if (parameterList.size() == 0) {
            throw new Exception("You must call parameter method before");
        }

        if (value == null) {
            throw new Exception("You must call value method before");
        }

        try {
            Method method = obj.getClass().getMethod("set" + StringUtil.getFirstUpperCaseStr(valueName), parameterList.toArray(new Class<?>[]{}));
            method.invoke(obj, value);
        } catch (IllegalAccessException e) {
            LogUtil.e(e.toString());
        } catch (InvocationTargetException e) {
            LogUtil.e(e.toString());
        } catch (NoSuchMethodException e) {
            LogUtil.e(e.toString());
        }
        return this;
    }

    public MockObject value(Object value) {
        this.value = value;
        return this;
    }

    public Object returnValue(String valueName) {
        Object value = null;
        try {
            Method method = obj.getClass().getMethod("get" + StringUtil.getFirstUpperCaseStr(valueName));
            value = method.invoke(obj);
        } catch (IllegalAccessException e) {
            LogUtil.e(e.toString());
        } catch (InvocationTargetException e) {
            LogUtil.e(e.toString());
        } catch (NoSuchMethodException e) {
            LogUtil.e(e.toString());
        }

        return value;
    }

    public MockObject parameter(Class<?>... parameters) {
        for (Class<?> parameter : parameters) {
            parameterList.add(parameter);
        }

        return this;
    }
}
