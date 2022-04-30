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

package xyz.fycz.myreader.util.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import xyz.fycz.myreader.util.IOUtils;

/**
 * by yangyxd
 * data: 2019.08.29
 */
public class MeUtils {

    /** 获取 assets 中指定目录中的文件名称列表 */
    public static CharSequence[] getAssetsFileList(AssetManager am, String path) throws IOException {
        final String[] fs = am.list(path);
        if (fs == null || fs.length == 0)
            return null;
        final CharSequence[] items = new CharSequence[fs.length];
        for (int i=0; i<fs.length; i++) {
            items[i] = MeUtils.getFileName(fs[i]);
        }
        return items;
    }

    public static String getFileName(String pathandname){
        int start=pathandname.lastIndexOf("/");
        int end=pathandname.lastIndexOf(".");
        if (end < 0) end = pathandname.length();
        return pathandname.substring(start+1,end);
    }

    public static String getOriginalFundData(AssetManager am, String filename) {
        InputStream input = null;
        try {
            input = am.open(filename);
            String json = convertStreamToString(input);
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertStreamToString(InputStream is) {
        String s = null;
        try {
            Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
            if (scanner.hasNext())
                s = scanner.next();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static Bitmap getFitAssetsSampleBitmap(AssetManager am, String file, int width, int height) {
        InputStream assetFile = null;
        try {
            assetFile = am.open(file);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(assetFile, null, options);
            options.inSampleSize = getFitInSampleSize(width, height, options);
            options.inJustDecodeBounds = false;
            assetFile.close();
            assetFile = am.open(file);
            Bitmap bm = BitmapFactory.decodeStream(assetFile, null, options);
            assetFile.close();
            return bm;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (assetFile != null) assetFile.close();
            } catch (Exception ee) {}
            return null;
        }
    }

    public static int getFitInSampleSize(int reqWidth, int reqHeight, BitmapFactory.Options options) {
        int inSampleSize = 1;
        if (options.outWidth > reqWidth || options.outHeight > reqHeight) {
            int widthRatio = Math.round((float) options.outWidth / (float) reqWidth);
            int heightRatio = Math.round((float) options.outHeight / (float) reqHeight);
            inSampleSize = Math.min(widthRatio, heightRatio);
        }
        return inSampleSize;
    }

    /**
     * 从assets文件夹之中读取文件
     *
     * @param am
     * @param assetName 需要后缀名
     */
    public static String getAssetStr(AssetManager am, String assetName) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(am.open(assetName)));
            StringBuilder assetText = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                assetText.append(line);
                assetText.append("\r\n");
            }
            return assetText.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(br);
        }
        return "";
    }
}
