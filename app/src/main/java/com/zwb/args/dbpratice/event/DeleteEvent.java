package com.zwb.args.dbpratice.event;

import com.zwb.args.dbpratice.annotation.Key;
import com.zwb.args.dbpratice.exception.NoRecordException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 删除事件
 * Created by pc on 2015/4/9.
 */
public class DeleteEvent extends BaseDataChangeEvent {
    private Map<Integer, Object> dataMap;
    private String column;
    private Object value;

    public DeleteEvent() {
        dataMap = new HashMap<Integer, Object>();
    }

    @Override
    public <T> DeleteEvent to(Class<T> clazz) {
        this.tableClazz = clazz;
        return this;
    }


    public <T> void delete() throws NoRecordException, NoSuchFieldException, IllegalAccessException {
        dataMap = stream.getRecordMap(tableClazz);
        if (dataMap == null) {
            throw new NoRecordException("There is no record in " + tableClazz.getSimpleName());
        }

        deleteData();
    }

    public <T> void deleteAll(List<T> dataList) throws NoRecordException, NoSuchFieldException, IllegalAccessException {
        dataMap = stream.getRecordMap(tableClazz);
        if (dataMap == null) {
            throw new NoRecordException("There is no record in " + tableClazz.getSimpleName());
        }

        for (T data : dataList) {
            deleteData(data);
        }
    }

    public void deleteAll() throws NoRecordException {
        dataMap = stream.getRecordMap(tableClazz);
        if (dataMap == null) {
            throw new NoRecordException("There is no record in " + tableClazz.getSimpleName());
        }

        Map<String, BaseEvent> insertEventMap = stream.getInsertEventMap();
        Iterator<Map.Entry<String, BaseEvent>> eventIterator = insertEventMap.entrySet().iterator();
        while (eventIterator.hasNext()) {
            Map.Entry<String, BaseEvent> entry = eventIterator.next();
            String tag = entry.getKey();
            if (tag.contains(tableClazz.getSimpleName().toLowerCase())) {
                eventIterator.remove();
            }
        }
    }

    private <T> void deleteData(T data) throws NoSuchFieldException, IllegalAccessException {
        Map<String, BaseEvent> insertEventMap = stream.getInsertEventMap();
        Set<String> tagSet = insertEventMap.keySet();
        String[] tagArr = tagSet.toArray(new String[]{});
        Iterator<Map.Entry<String, BaseEvent>> eventIterator = insertEventMap.entrySet().iterator();
        String key = getKey(data);
        while (eventIterator.hasNext()) {
            Map.Entry<String, BaseEvent> entry = eventIterator.next();
            String tag = entry.getKey();
            int index = getIndex(tag);
            T originData = (T) dataMap.get(index);
            String originKey = null;
            if (originData != null) {
                originKey = getKey(originData);
            }
            if (key.equals(originKey) && tagArr[index].contains(tableClazz.getSimpleName().toLowerCase())) {
                eventIterator.remove();
            }
        }

        stream.setInsertRecords(insertEventMap);
    }

    private <T> void deleteData() throws NoSuchFieldException, IllegalAccessException {
        Map<String, BaseEvent> insertEventMap = stream.getInsertEventMap();
        Set<String> tagSet = insertEventMap.keySet();
        String[] tagArr = tagSet.toArray(new String[]{});
        Iterator<Map.Entry<String, BaseEvent>> eventIterator = insertEventMap.entrySet().iterator();
        while (eventIterator.hasNext()) {
            Map.Entry<String, BaseEvent> entry = eventIterator.next();
            String tag = entry.getKey();
            int index = getIndex(tag);
            T originData = (T) dataMap.get(index);
            Object originValue = null;
            if (originData != null) {
                originValue = getValue(originData);
            }
            if ((value.toString()).equals(originValue.toString()) && tagArr[index].contains(tableClazz.getSimpleName().toLowerCase())) {
                eventIterator.remove();
            }
        }

        stream.setInsertRecords(insertEventMap);
    }

    private <T> String getKey(T data) throws NoSuchFieldException, IllegalAccessException {
        String key = "";
        Field field = data.getClass().getDeclaredField(tableClazz.getSimpleName().toLowerCase() + "Id");
        if (field.isAnnotationPresent(Key.class)) {
            field.setAccessible(true);
            key = (String) field.get(data);
        }
        return key;
    }

    private <T> Object getValue(T data) throws NoSuchFieldException, IllegalAccessException {
        Field field = data.getClass().getDeclaredField(column);
        field.setAccessible(true);
        String value = (String) field.get(data);
        return value;
    }

    /**
     * 获取事件对应的数据的位置
     *
     * @param tag 事件的tag
     * @return 对应数据的位置
     */
    private int getIndex(String tag) {
        String[] strArr = tag.split("_");
        int index = Integer.valueOf(strArr[strArr.length - 1]);
        return index;
    }

    public DeleteEvent where(String column, String value) {
        this.column = column;
        this.value = value;
        return this;
    }
}
