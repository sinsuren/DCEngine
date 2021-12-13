package com.sinsuren.engine.expression.utils;

import com.google.common.base.Joiner;

import java.util.*;

public class Functions {

    public static List keys(Map map) {
        if (map != null) {
            return new ArrayList(map.keySet());
        }

        return Collections.emptyList();
    }

    public static List values(Map map) {
        if(map != null) {
            return new ArrayList(map.values());
        }
        return Collections.emptyList();
    }


    public static Map map(List keys, final List labels, final Map ... maps) {
        if( labels.size() == maps.length) {
            Map map = new HashMap();

            for (final Object key: keys) {
                map.put(key, new HashMap<Object, Object>() {{
                    for(int i = 0; i < labels.size() ; i++) {
                        put(labels.get(i), maps[i].get(key));
                    }
                }});
            }
            return map;
        }

        throw new RuntimeException("Number of labels do not match number of provided maps");
    }

    public static String join(List list) {
        return Joiner.on(", ").join(list);
    }

    public static String join(Object[] list) {
        return Joiner.on(", ").join(list);
    }
}
