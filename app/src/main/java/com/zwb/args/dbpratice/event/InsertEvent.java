package com.zwb.args.dbpratice.event;

import com.zwb.args.dbpratice.exception.NoTableException;

/**
 * Created by pc on 2015/4/8.
 */
public class InsertEvent extends BaseDataChangeEvent {
    private static int index = 0;

    public InsertEvent() {

    }

    public <T> InsertEvent insert(T record) throws NoTableException {
        if (tableClazz == null) {
            throw new NoTableException("There is no table");
        }

        QueryEvent queryEvent = new QueryEvent();
        queryEvent.insertRecord(record);
        queryEvent.setTag(tableClazz.getSimpleName().toLowerCase() + "_query_insert_" + index);
        stream.insertData(tableClazz, index, record);
        stream.registerInsertEvent(queryEvent);
        index++;
        return this;
    }

    @Override
    public <T> InsertEvent to(Class<T> clazz) {
        this.tableClazz = clazz;
        return this;
    }
}
