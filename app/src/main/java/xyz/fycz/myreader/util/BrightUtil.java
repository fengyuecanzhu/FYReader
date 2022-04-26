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

import android.content.ContentResolver;
import android.provider.Settings;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;


public class BrightUtil {
    /**
     * 获取屏幕的亮度
     *
     * @param activity
     * @return
     */
    public static int getScreenBrightness(AppCompatActivity activity) {
        int nowBrightnessValue = 0;
        ContentResolver resolver = activity.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(
                    resolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }
    /**
     * 设置亮度
     *
     * @param activity
     * @param brightness
     */
    public static void setBrightness(AppCompatActivity activity, int brightness) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        activity.getWindow().setAttributes(lp);
    }

    public static int brightToProgress(int brightness){
        return (int)(Float.valueOf(brightness) * (1f / 255f) * 100);
    }

    public static int progressToBright(int progress){
        return progress  * 255 / 100;
    }

    /**
     * 亮度跟随系统
     * @param activity
     */
    public static void followSystemBright(AppCompatActivity activity){
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness =   WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        activity.getWindow().setAttributes(lp);
    }

}
