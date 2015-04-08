package com.zwb.args.dbpratice.event;

/**
 * Created by pc on 2015/4/8.
 */
public class BaseEvent {
    protected String tag;

    protected void setTag(String tag) {
        this.tag = tag;
    }

    protected String getTag() {
        return tag;
    }
}
