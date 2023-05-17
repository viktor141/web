package ru.vixtor.server;

import java.util.HashMap;
import java.util.Map;

public enum Query {
    GET,
    POST;

    private Map<String, Handler> map;

    Query(){
        map = new HashMap<>();
    }


    public Map<String, Handler> getMap() {
        return map;
    }

    public void addToMap(String path, Handler handler){
        map.put(path, handler);
    }

}
