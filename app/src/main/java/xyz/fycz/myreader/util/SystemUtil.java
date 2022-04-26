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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;

import static android.content.Context.POWER_SERVICE;

/**
 */

public class SystemUtil {

    public static double getScreenPhysicalSize(AppCompatActivity ctx) {
        DisplayMetrics dm = new DisplayMetrics();
        ctx.getWindowManager().getDefaultDisplay().getMetrics(dm);
        double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) + Math.pow(dm.heightPixels, 2));
        return diagonalPixels / (160 * dm.density);
    }
    public static int getScreenOffTime(Context context) {
        int screenOffTime = 0;
        try {
            screenOffTime = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenOffTime;
    }

    public static void ignoreBatteryOptimization(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) return;

        PowerManager powerManager = (PowerManager) activity.getSystemService(POWER_SERVICE);
        boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
        //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
        if (!hasIgnored) {
            try {
                @SuppressLint("BatteryLife")
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(intent);
            } catch (Throwable ignored) {
            }
        }
    }
}
