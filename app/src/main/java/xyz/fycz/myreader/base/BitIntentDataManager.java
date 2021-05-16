package xyz.fycz.myreader.base;

import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import xyz.fycz.myreader.common.APPCONST;

public class BitIntentDataManager {
    private static Map<String, Object> bigData;

    private static BitIntentDataManager instance = null;

    private BitIntentDataManager() {
        bigData = new HashMap<>();
    }

    public static BitIntentDataManager getInstance() {
        if (instance == null) {
            synchronized (BitIntentDataManager.class) {
                if (instance == null) {
                    instance = new BitIntentDataManager();
                }
            }
        }
        return instance;
    }

    public Object getData(String key) {
        Object object = bigData.get(key);
        bigData.remove(key);
        return object;
    }

    public Object getData(Intent intent){
        String dataKey = intent.getStringExtra(APPCONST.DATA_KEY);
        return getData(dataKey);
    }

    public void putData(String key, Object data) {
        bigData.put(key, data);
    }

    public void putData(Intent intent, Object data){
        String dataKey = String.valueOf(System.currentTimeMillis());
        intent.putExtra(APPCONST.DATA_KEY, dataKey);
        putData(dataKey, data);
    }
}
