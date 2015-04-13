package com.zwb.args.dbpratice.event;

/**
 * 查询事件
 * Created by pc on 2015/4/9.
 */
public class QueryEvent extends BaseEvent {
    private Object record;

    /**
     * 插入数据
     *
     * @param value 数据
     */
    public void insertRecord(Object value) {
        this.record = value;
    }

    /**
     * 获取数据
     *
     * @return 数据
     */
    public Object getRecord() {
        return record;
    }
}
