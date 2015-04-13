package com.zwb.args.dbpratice.event;

/**
 * 创建事件
 * Created by pc on 2015/4/9.
 */
public class CreateEvent extends BaseDataChangeEvent {
    @Override
    public <T> BaseDataChangeEvent to(Class<T> clazz) {
        return null;
    }
}
