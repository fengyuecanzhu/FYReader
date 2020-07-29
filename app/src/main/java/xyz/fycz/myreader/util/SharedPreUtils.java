package xyz.fycz.myreader.util;

import android.content.Context;
import android.content.SharedPreferences;

import xyz.fycz.myreader.application.MyApplication;

/**
 * SharedPreferences工具类
 */

public class SharedPreUtils {
    public static final String SHARED_NAME = "FYReader_pref";
    private static SharedPreUtils sInstance;
    private SharedPreferences sharedReadable;
    private SharedPreferences.Editor sharedWritable;

    private SharedPreUtils(){
        sharedReadable = MyApplication.getmContext()
                .getSharedPreferences(SHARED_NAME, Context.MODE_MULTI_PROCESS);
        sharedWritable = sharedReadable.edit();
    }

    public static SharedPreUtils getInstance(){
        if(sInstance == null){
            synchronized (SharedPreUtils.class){
                if (sInstance == null){
                    sInstance = new SharedPreUtils();
                }
            }
        }
        return sInstance;
    }

    public SharedPreferences getSharedReadable(){
        return sharedReadable;
    }

    public void putString(String key,String value){
        sharedWritable.putString(key,value);
        sharedWritable.commit();
    }

    public void putInt(String key,int value){
        sharedWritable.putInt(key, value);
        sharedWritable.commit();
    }

    public void putBoolean(String key,boolean value){
        sharedWritable.putBoolean(key, value);
        sharedWritable.commit();
    }

    public String getString(String key){
        return getString(key,"");
    }

    public String getString(String key, String def){
        return sharedReadable.getString(key,def);
    }

    public int getInt(String key){
        return getInt(key,0);
    }

    public int getInt(String key, int def){
        return sharedReadable.getInt(key, def);
    }

    public boolean getBoolean(String key){
        return getBoolean(key,false);
    }

    public boolean getBoolean(String key,boolean def){
        return sharedReadable.getBoolean(key, false);
    }
}
