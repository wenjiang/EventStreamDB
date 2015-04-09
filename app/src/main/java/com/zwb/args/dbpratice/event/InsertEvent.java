package com.zwb.args.dbpratice.event;

import com.zwb.args.dbpratice.exception.NoTableException;
import com.zwb.args.dbpratice.exception.NoTagException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pc on 2015/4/8.
 */
public class InsertEvent extends BaseDataChangeEvent {
    private Class<?> tableClazz;
    private Map<Integer, Object> recordMap;
    private Map<Class<?>, Map<Integer, Object>> tableMap;
    private int index = 0;
    private EventStream stream;

    public InsertEvent() {
        recordMap = new HashMap<Integer, Object>();
        tableMap = new HashMap<Class<?>, Map<Integer, Object>>();
        stream = EventStream.getInstance();
    }

    public <T> InsertEvent insert(T record) throws NoTableException {
        if (tableClazz == null) {
            throw new NoTableException("There is no table");
        }

        recordMap.put(index, record);
        tableMap.put(tableClazz, recordMap);
        QueryEvent queryEvent = new QueryEvent();
        queryEvent.insertRecord(record);
        queryEvent.setTag(tableClazz.getSimpleName().toLowerCase() + "_query" + "_" + index);
        stream.register(queryEvent);
        index++;
        return this;
    }

    public Map<Class<?>, Map<Integer, Object>> getTableData() {
        return tableMap;
    }

    public <T> InsertEvent to(Class<T> clazz) {
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
