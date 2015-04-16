package com.zwb.args.dbpratice.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事件流的注册机
 * Created by pc on 2015/4/8.
 */
public class EventStream {
    private Map<String, BaseEvent> eventMap;
    private Map<String, BaseEvent> insertEventMap;
    private Map<String, BaseEvent> updateEventMap;
    private static EventStream stream;
    private Map<Class<?>, Map<Integer, Object>> tableMap;

    private EventStream() {
        eventMap = new HashMap<String, BaseEvent>();
        tableMap = new HashMap<Class<?>, Map<Integer, Object>>();
        insertEventMap = new HashMap<String, BaseEvent>();
        updateEventMap = new HashMap<String, BaseEvent>();
    }

    /**
     * 事件流的单例
     *
     * @return 事件流的单例
     */
    public static EventStream getInstance() {
        if (stream == null) {
            stream = new EventStream();
        }

        return stream;
    }

    /**
     * 注册插入事件
     *
     * @param event 插入事件
     */
    public void registerInsertEvent(BaseEvent event) {
        insertEventMap.put(event.getTag(), event);
    }

    /**
     * 获取插入事件
     *
     * @return 插入事件的Map
     */
    public Map<String, BaseEvent> getInsertEventMap() {
        return insertEventMap;
    }

    /**
     * 注册更新事件
     *
     * @param event 更新事件
     */
    public void registerUpdateEvent(BaseEvent event) {
        updateEventMap.put(event.getTag(), event);
    }

    /**
     * 获取更新事件
     *
     * @return 更新事件的Map
     */
    public Map<String, BaseEvent> getUpdateEventMap() {
        return updateEventMap;
    }

    /**
     * 注册全部的插入事件
     *
     * @param events 插入事件的List
     */
    public void registerInsertAll(List<BaseEvent> events) {
        for (BaseEvent event : events) {
            insertEventMap.put(event.getTag(), event);
        }
    }

    /**
     * 注册全部的更新事件
     *
     * @param events 更新事件的List
     */
    public void registerUpdateAll(List<BaseEvent> events) {
        for (BaseEvent event : events) {
            updateEventMap.put(event.getTag(), event);
        }
    }

    /**
     * 获取数据的Map
     *
     * @return 数据的Map
     */
    public Map<Integer, Object> getRecordMap(Class<?> tableClazz) {
        return tableMap.get(tableClazz);
    }

    /**
     * 插入数据
     *
     * @param tableClazz 表对象的class对象
     * @param index      数据的位置
     * @param record     数据
     * @param <T>        泛型参数
     */
    public <T> void insertData(Class<?> tableClazz, int index, T record) {
        Map<Integer, Object> recordMap = tableMap.get(tableClazz);
        if (recordMap == null) {
            recordMap = new HashMap<Integer, Object>();
        }

        recordMap.put(index, record);
        tableMap.put(tableClazz, recordMap);
    }

    public void setInsertRecords(Map<String, BaseEvent> insertRecords) {
        this.insertEventMap = insertRecords;
    }
}
