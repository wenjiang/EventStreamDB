package com.zwb.args.dbpratice.event;

/**
 * 事件的基类
 * Created by pc on 2015/4/8.
 */
public class BaseEvent {
    protected String tag;  //事件的唯一标识

    /**
     * 设置事件的唯一标识
     *
     * @param tag 标识
     */
    protected void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * 获取标识
     *
     * @return 标识
     */
    protected String getTag() {
        return tag;
    }
}
