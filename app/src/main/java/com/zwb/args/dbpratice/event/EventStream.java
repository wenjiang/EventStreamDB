package com.zwb.args.dbpratice.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pc on 2015/4/8.
 */
public class EventStream {
    private Map<String, BaseEvent> eventMap;
    private static EventStream stream;

    private EventStream() {
        eventMap = new HashMap<String, BaseEvent>();
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

    public void registerAll(List<BaseEvent> events) {
        for (BaseEvent event : events) {
            eventMap.put(event.getTag(), event);
        }
    }

    public Map<String, BaseEvent> getEventMap() {
        return eventMap;
    }
}
