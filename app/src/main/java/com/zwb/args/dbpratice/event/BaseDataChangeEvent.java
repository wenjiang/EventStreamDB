package com.zwb.args.dbpratice.event;

/**
 * 基本的数据变更事件
 * Created by pc on 2015/4/9.
 */
public abstract class BaseDataChangeEvent extends BaseEvent {
    protected EventStream stream;
    protected Class<?> tableClazz;

    public BaseDataChangeEvent() {
        stream = EventStream.getInstance();
    }

    /**
     * 保存到哪张表中
     *
     * @param clazz 表对象的class对象
     * @param <T>   泛型参数
     * @return BaseDataChangeEvent的实例
     */
    public abstract <T> BaseDataChangeEvent to(Class<T> clazz);
}
