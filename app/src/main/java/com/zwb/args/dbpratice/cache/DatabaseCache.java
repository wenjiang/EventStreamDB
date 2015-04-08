package com.zwb.args.dbpratice.cache;

import com.zwb.args.dbpratice.event.BaseEvent;
import com.zwb.args.dbpratice.event.EventStream;
import com.zwb.args.dbpratice.event.UpdateEvent;
import com.zwb.args.dbpratice.exception.NoTableException;

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

    private DatabaseCache() {
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

    public <T> List<T> find(String name) throws NoTableException {
        if (tableClazz == null) {
            throw new NoTableException("There is no table");
        }

        List<T> dataList = new ArrayList<T>();
        Map<String, BaseEvent> eventMap = EventStream.getInstance().getEventMap();
        Set<String> tagSet = eventMap.keySet();
        for (String tag : tagSet) {
            if (tag.contains(tableClazz.getSimpleName().toLowerCase()) && tag.contains("update")) {
                Map<Class<?>, Map<Integer, Object>> tableMap = ((UpdateEvent) (eventMap.get(tag))).getTableData();
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
}
