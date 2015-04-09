package com.zwb.args.dbpratice.cache;

import com.zwb.args.dbpratice.event.BaseEvent;
import com.zwb.args.dbpratice.event.EventStream;
import com.zwb.args.dbpratice.event.InsertEvent;
import com.zwb.args.dbpratice.event.QueryEvent;
import com.zwb.args.dbpratice.exception.NoColumnChangeException;
import com.zwb.args.dbpratice.exception.NoRecordException;
import com.zwb.args.dbpratice.exception.NoTableException;
import com.zwb.args.dbpratice.exception.NoTagException;
import com.zwb.args.dbpratice.util.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
    private String deleteTag;
    private Object queryValue;
    private Map<String, BaseEvent> eventMap;
    private Set<String> tagSet;
    private String queryColumn;

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

    public <T> List<T> findAll() throws NoTableException {
        if (tableClazz == null) {
            throw new NoTableException("There is no table");
        }

        List<T> dataList = new ArrayList<T>();
        for (String tag : tagSet) {
            if (tag.contains(tableClazz.getSimpleName().toLowerCase()) && tag.contains("update")) {
                Map<Class<?>, Map<Integer, Object>> tableMap = ((InsertEvent) (eventMap.get(tag))).getTableData();
                Map<Integer, Object> dataMap = tableMap.get(tableClazz);
                Set<Integer> indexSet = dataMap.keySet();
                for (Integer index : indexSet) {
                    dataList.add((T) dataMap.get(index));
                }
                break;
            }
        }

        return dataList;
    }

    public DatabaseCache where(String column, Object value) {
        updateTag = tableClazz.getSimpleName().toLowerCase() + "_" + column + "_query";
        insertTag = tableClazz.getSimpleName().toLowerCase() + "_query";
        deleteTag = tableClazz.getSimpleName().toLowerCase() + "_" + column + "_delete";
        queryValue = value;
        queryColumn = column;
        return this;
    }

    public <T> T find(String column, Class<T> clazz) throws NoTagException, NoColumnChangeException, NoRecordException {
        boolean isInsert = false;
        List<Object> records = new ArrayList<Object>();
        for (String tag : tagSet) {
            if (tag.contains(insertTag)) {
                QueryEvent queryEvent = (QueryEvent) eventMap.get(tag);
                records.add(queryEvent.getInsertRecord());
                isInsert = true;
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
