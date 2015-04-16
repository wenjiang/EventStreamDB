package com.zwb.args.dbpratice.event;

import com.zwb.args.dbpratice.exception.NoTableException;

import java.util.List;

/**
 * 插入事件
 * Created by pc on 2015/4/8.
 */
public class InsertEvent extends BaseDataChangeEvent {
    private static int index = 0;

    public InsertEvent() {

    }

    /**
     * 插入数据
     *
     * @param record 数据
     * @param <T>    泛型参数
     * @throws NoTableException
     */
    public <T> void insert(T record) throws NoTableException {
        if (tableClazz == null) {
            throw new NoTableException("There is no table");
        }

        QueryEvent queryEvent = new QueryEvent();
        queryEvent.insertRecord(record);
        queryEvent.setTag(tableClazz.getSimpleName().toLowerCase() + "_query_insert_" + index);
        stream.insertData(tableClazz, index, record);
        stream.registerInsertEvent(queryEvent);
        index++;
    }

    public <T> void insertAll(List<T> dataList) throws NoTableException {
        if (tableClazz == null) {
            throw new NoTableException("There is no table");
        }

        for (T record : dataList) {
            QueryEvent queryEvent = new QueryEvent();
            queryEvent.insertRecord(record);
            queryEvent.setTag(tableClazz.getSimpleName().toLowerCase() + "_query_insert_" + index);
            stream.insertData(tableClazz, index, record);
            stream.registerInsertEvent(queryEvent);
            index++;
        }
    }

    @Override
    public <T> InsertEvent to(Class<T> clazz) {
        this.tableClazz = clazz;
        return this;
    }
}
