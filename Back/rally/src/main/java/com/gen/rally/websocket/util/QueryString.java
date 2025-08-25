package com.gen.rally.websocket.util;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class QueryString {
    public static Map<String, String> parse(URI uri){
        Map<String,String> map = new HashMap<>();
        if (uri == null || uri.getQuery() == null) return map;
        for (String p : uri.getQuery().split("&")) {
            int idx = p.indexOf('=');
            if (idx > 0) {
                String k = URLDecoder.decode(p.substring(0, idx), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(p.substring(idx+1), StandardCharsets.UTF_8);
                map.put(k, v);
            }
        }
        return map;
    }
}