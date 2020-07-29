package xyz.fycz.myreader.util;

import android.content.Context;
import android.provider.Settings;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;

/**
 */

public class ScreenHelper {

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
}
