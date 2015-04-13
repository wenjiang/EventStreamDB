package com.zwb.args.dbpratice.event;

/**
 * Created by pc on 2015/4/9.
 */
public abstract class BaseDataChangeEvent extends BaseEvent {
    protected EventStream stream;
    protected Class<?> tableClazz;

    public BaseDataChangeEvent() {
        stream = EventStream.getInstance();
    }

    public abstract <T> BaseDataChangeEvent to(Class<T> clazz);



}
