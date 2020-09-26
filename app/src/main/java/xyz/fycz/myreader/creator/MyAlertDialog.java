package xyz.fycz.myreader.creator;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import xyz.fycz.myreader.R;

/**
 * @author fengyue
 * @date 2020/9/20 9:48
 */
public class MyAlertDialog {
    public static AlertDialog.Builder build(Context context){
        return new AlertDialog.Builder(context, R.style.alertDialogTheme);
    }
}
