package com.zwb.args.dbpratice.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pc on 2015/4/8.
 */
public class EventStream {
    private Map<String, BaseEvent> eventMap;
    private Map<String, BaseEvent> insertEventMap;
    private Map<String, BaseEvent> updateEventMap;
    private static EventStream stream;
    private Map<Integer, Object> recordMap;
    private Map<Class<?>, Map<Integer, Object>> tableMap;

    private EventStream() {
        eventMap = new HashMap<String, BaseEvent>();
        recordMap = new HashMap<Integer, Object>();
        tableMap = new HashMap<Class<?>, Map<Integer, Object>>();
        insertEventMap = new HashMap<String, BaseEvent>();
        updateEventMap = new HashMap<String, BaseEvent>();
    }

    public static EventStream getInstance() {
        if (stream == null) {
            stream = new EventStream();
        }

        return stream;
    }

    public void register(BaseEvent event) {
        eventMap.put(event.getTag(), event);
    }

    public void registerInsertEvent(BaseEvent event) {
        insertEventMap.put(event.getTag(), event);
    }

    public Map<String, BaseEvent> getInsertEventMap() {
        return insertEventMap;
    }

    public void registerUpdateEvent(BaseEvent event) {
        updateEventMap.put(event.getTag(), event);
    }

    public Map<String, BaseEvent> getUpdateEventMap() {
        return updateEventMap;
    }

    public void registerAll(List<BaseEvent> events) {
        for (BaseEvent event : events) {
            eventMap.put(event.getTag(), event);
        }
    }

    public Map<String, BaseEvent> getEventMap() {
        return eventMap;
    }

    public Map<Integer, Object> getRecordMap() {
        return recordMap;
    }

    public <T> void insertData(Class<?> tableClazz, int index, T record) {
        recordMap.put(index, record);
        tableMap.put(tableClazz, recordMap);
    }
}
