package com.zwb.args.dbpratice.event;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pc on 2015/4/9.
 */
public class QueryEvent extends BaseEvent {
    private Map<String, Object> conditionMap;
    private Object record;

    public QueryEvent() {
        conditionMap = new HashMap<String, Object>();
    }

    public void insertRecord(String column, Object value) {
        conditionMap.put(column, value);
    }

    public void insertRecord(Object value) {
        this.record = value;
    }

    public Map<String, Object> getUpdateRecord() {
        return conditionMap;
    }

    public Object getInsertRecord() {
        return record;
    }
}
