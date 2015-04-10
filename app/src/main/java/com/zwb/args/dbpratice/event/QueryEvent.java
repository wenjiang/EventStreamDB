package com.zwb.args.dbpratice.event;

/**
 * Created by pc on 2015/4/9.
 */
public class QueryEvent extends BaseEvent {
    private Object record;

    public void insertRecord(Object value) {
        this.record = value;
    }

    public Object getRecord() {
        return record;
    }
}
