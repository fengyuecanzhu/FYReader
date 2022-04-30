/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.util;



import xyz.fycz.myreader.application.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;



public class CacheHelper {

    private static Hashtable<String, Object> memCacheRegion = new Hashtable<String, Object>();
    private static final int CACHE_TIME = 60*60000;
    public static String WRITING_OR_READING_FILE_NAME = "";

    /**
     * 读取对象（Serializable）
     * @param file
     * @return
     * @throws IOException
     */
    public static Serializable readObject(String file){
        if(!isExistDataCache(file))  return null;
        while(WRITING_OR_READING_FILE_NAME.equals(file)){
            try {
                Thread.sleep(100);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        WRITING_OR_READING_FILE_NAME = file;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try{
            fis = App.getApplication().openFileInput(file);
            ois = new ObjectInputStream(fis);
            return (Serializable)ois.readObject();
        }catch(FileNotFoundException e){
            e.printStackTrace();
            //   return null;
        }catch(Exception e){
            e.printStackTrace();
            //反序列化失败 - 删除缓存文件
            if(e instanceof InvalidClassException){
                File data = App.getApplication().getFileStreamPath(file);
                data.delete();
            }
            //   return null;
        }finally{
            try {
                ois.close();
                fis.close();
                WRITING_OR_READING_FILE_NAME = "";
            } catch (Exception e) {
                e.printStackTrace();
                WRITING_OR_READING_FILE_NAME = "";
            }
        }
        return null;
    }


    public static boolean deleteFile(String file){
        while(WRITING_OR_READING_FILE_NAME.equals(file)){
            try {
                Thread.sleep(100);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        WRITING_OR_READING_FILE_NAME = file;
        boolean flag = App.getApplication().deleteFile(file);
        WRITING_OR_READING_FILE_NAME = "";
        return flag;
    }

    /**
     * 保存对象
     * @param ser
     * @param file
     * @throws IOException
     */
    public static boolean saveObject(Serializable ser, String file) {
        while(WRITING_OR_READING_FILE_NAME.equals(file)){
            try {
                Thread.sleep(100);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        WRITING_OR_READING_FILE_NAME = file;
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try{
            fos = App.getApplication().openFileOutput(file, App.getApplication().MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ser);
            oos.flush();
            fos.flush();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally{
            try {
                oos.close();
                fos.close();
                WRITING_OR_READING_FILE_NAME = "";
            } catch (Exception e) {
                e.printStackTrace();
                WRITING_OR_READING_FILE_NAME = "";

            }

        }
    }

    /**
     * 判断缓存是否存在
     * @param cachefile
     * @return
     */
    private static boolean isExistDataCache(String cachefile){
        boolean exist = false;
        File data = App.getApplication().getFileStreamPath(cachefile);
        if(data.exists())
            exist = true;
        return exist;
    }
}
