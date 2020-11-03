package xyz.fycz.myreader.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.enums.ReadStyle;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.util.BrightUtil;
import xyz.fycz.myreader.util.IOUtils;
import xyz.fycz.myreader.util.StringHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static xyz.fycz.myreader.enums.ReadStyle.blueDeep;
import static xyz.fycz.myreader.enums.ReadStyle.breen;
import static xyz.fycz.myreader.enums.ReadStyle.common;
import static xyz.fycz.myreader.enums.ReadStyle.leather;
import static xyz.fycz.myreader.enums.ReadStyle.protectedEye;



public class DialogCreator {

    private static ImageView ivLastSelectd = null;


    /**
     * 阅读详细设置对话框
     * @param context
     * @param setting
     * @param onReadStyleChangeListener
     * @param reduceSizeListener
     * @param increaseSizeListener
     * @param languageChangeListener
     * @param onFontClickListener
     * @return
     */
    public static Dialog createReadDetailSetting(final Context context, final Setting setting,
                                                 final OnReadStyleChangeListener onReadStyleChangeListener,
                                                 final View.OnClickListener reduceSizeListener,
                                                 final View.OnClickListener increaseSizeListener,
                                                 final View.OnClickListener languageChangeListener,
                                                 final View.OnClickListener onFontClickListener,
                                                 final OnPageModeChangeListener onModeClickListener,
                                                 View.OnClickListener autoScrollListener,
                                                 View.OnClickListener moreSettingListener) {
        final Dialog dialog = new Dialog(context, R.style.jmui_default_dialog_style);
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_read_setting_detail, null);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        //触摸外部关闭
        view.findViewById(R.id.ll_bottom_view).setOnClickListener(null);
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dialog.dismiss();
                return false;
            }
        });
        //设置全屏
        Window window = dialog.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setWindowAnimations(R.style.dialogWindowAnim);
        //阅读背景风格
        final ImageView ivCommonStyle = (ImageView) view.findViewById(R.id.iv_common_style);
        final ImageView ivLeatherStyle = (ImageView) view.findViewById(R.id.iv_leather_style);
        final ImageView ivProtectEyeStyle = (ImageView) view.findViewById(R.id.iv_protect_eye_style);
        final ImageView ivBreenStyle = (ImageView) view.findViewById(R.id.iv_breen_style);
        final ImageView ivBlueDeepStyle = (ImageView) view.findViewById(R.id.iv_blue_deep_style);
        switch (setting.getReadStyle()) {
            case common:
                ivCommonStyle.setSelected(true);
                ivLastSelectd = ivCommonStyle;
                break;
            case leather:
                ivLeatherStyle.setSelected(true);
                ivLastSelectd = ivLeatherStyle;
                break;
            case protectedEye:
                ivProtectEyeStyle.setSelected(true);
                ivLastSelectd = ivProtectEyeStyle;
                break;
            case breen:
                ivBreenStyle.setSelected(true);
                ivLastSelectd = ivBreenStyle;
                break;
            case blueDeep:
                ivBlueDeepStyle.setSelected(true);
                ivLastSelectd = ivBlueDeepStyle;
                break;
        }
        ivCommonStyle.setOnClickListener(v -> selectedStyle(ivCommonStyle, common, onReadStyleChangeListener));
        ivLeatherStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedStyle(ivLeatherStyle, leather, onReadStyleChangeListener);
            }
        });
        ivProtectEyeStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedStyle(ivProtectEyeStyle, protectedEye, onReadStyleChangeListener);
            }
        });
        ivBlueDeepStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedStyle(ivBlueDeepStyle, blueDeep, onReadStyleChangeListener);
            }
        });
        ivBreenStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedStyle(ivBreenStyle, breen, onReadStyleChangeListener);
            }
        });

        //字体大小
        TextView tvSizeReduce = (TextView) view.findViewById(R.id.tv_reduce_text_size);
        TextView tvSizeIncrease = (TextView) view.findViewById(R.id.tv_increase_text_size);
        final TextView tvSize = (TextView) view.findViewById(R.id.tv_text_size);
        tvSize.setText(String.valueOf((int) setting.getReadWordSize()));
        tvSizeReduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setting.getReadWordSize() > 1) {
                    tvSize.setText(String.valueOf((int) setting.getReadWordSize() - 1));
                    if (reduceSizeListener != null) {
                        reduceSizeListener.onClick(v);
                    }
                }
            }
        });
        tvSizeIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setting.getReadWordSize() < 41) {
                    tvSize.setText(String.valueOf((int) setting.getReadWordSize() + 1));
                    if (increaseSizeListener != null) {
                        increaseSizeListener.onClick(v);
                    }
                }
            }
        });

        //亮度调节
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.sb_brightness_progress);
        final TextView tvBrightFollowSystem = (TextView) view.findViewById(R.id.tv_system_brightness);
        seekBar.setProgress(setting.getBrightProgress());
        tvBrightFollowSystem.setSelected(setting.isBrightFollowSystem());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                BrightUtil.setBrightness((AppCompatActivity) context, BrightUtil.progressToBright(progress));
                tvBrightFollowSystem.setSelected(false);
                setting.setBrightProgress(progress);
                setting.setBrightFollowSystem(false);
                SysManager.saveSetting(setting);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //亮度跟随系统
        tvBrightFollowSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvBrightFollowSystem.setSelected(!tvBrightFollowSystem.isSelected());
                if (tvBrightFollowSystem.isSelected()) {
                    BrightUtil.followSystemBright((AppCompatActivity) context);
                    setting.setBrightFollowSystem(true);
                    SysManager.saveSetting(setting);
                } else {
                    BrightUtil.setBrightness((AppCompatActivity) context, BrightUtil.progressToBright(setting.getBrightProgress()));
                    setting.setBrightFollowSystem(false);
                    SysManager.saveSetting(setting);
                }
            }
        });
        //音量键翻页
        final TextView tvIsVolumeTurnPage = (TextView) view.findViewById(R.id.tv_isVolumeTurnPage);
        if (setting.isVolumeTurnPage()) {
            tvIsVolumeTurnPage.setText("关");
        } else {
            tvIsVolumeTurnPage.setText("开");
        }
        tvIsVolumeTurnPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvIsVolumeTurnPage.getText().toString().equals("关")) {
                    tvIsVolumeTurnPage.setText("开");
                } else {
                    tvIsVolumeTurnPage.setText("关");
                }
                if (languageChangeListener != null) {
                    languageChangeListener.onClick(v);
                }
            }
        });

        //选择字体
        TextView tvFont = (TextView)view.findViewById(R.id.tv_text_font);
        tvFont.setOnClickListener(onFontClickListener);

        //选择翻页模式
        TextView tvMode = view.findViewById(R.id.tv_page_mode);
        tvMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onModeClickListener != null){
                    onModeClickListener.onChange((TextView) v);
                }
            }
        });
        switch (setting.getPageMode()) {
            case COVER:
                tvMode.setText("覆盖");
                break;
            case SIMULATION:
                tvMode.setText("仿真");
                break;
            case SLIDE:
                tvMode.setText("滑动");
                break;
            case SCROLL:
                tvMode.setText("滚动");
                break;
            case NONE:
                tvMode.setText("无");
                break;
        }

        //自动滚屏速度
        SeekBar sbScrollSpeed = view.findViewById(R.id.sb_auto_scroll_progress);
        TextView tvAutoScroll = view.findViewById(R.id.tv_auto_scroll);
        TextView tvAutoScrollSpeed = view.findViewById(R.id.tv_auto_scroll_speed);
        sbScrollSpeed.setProgress(setting.getAutoScrollSpeed() / 3);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            sbScrollSpeed.setMin(100);
        }
        tvAutoScrollSpeed.setText("每分钟阅读字数(CPM)：" + setting.getAutoScrollSpeed() + "CPM");
        sbScrollSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int speed = progress == 0 ? 300 : progress * 3;
                setting.setAutoScrollSpeed(speed);
                tvAutoScrollSpeed.setText("每分钟阅读字数(CPM)：" + speed + "CPM");
                SysManager.saveSetting(setting);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        tvAutoScroll.setOnClickListener(autoScrollListener);

        view.findViewById(R.id.tv_read_setting_more).setOnClickListener(moreSettingListener);

        return dialog;
    }

    private static void selectedStyle(ImageView curSelected, ReadStyle readStyle, OnReadStyleChangeListener listener) {
        ivLastSelectd.setSelected(false);
        ivLastSelectd = curSelected;
        curSelected.setSelected(true);
        if (listener != null) {
            listener.onChange(readStyle);
        }
    }

    /**
     * 创建一个普通对话框（包含确定、取消按键）
     *
     * @param context
     * @param title
     * @param mesage
     * @param isCancelable     是否允许返回键取消
     * @param positiveListener 确定按键动作
     * @param negativeListener 取消按键动作
     * @return
     */
    public static AlertDialog createCommonDialog(Context context, String title, String mesage, boolean isCancelable,
                                                                        DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {

        final AlertDialog.Builder normalDialog = MyAlertDialog.build(context);
//        normalDialog.setIcon(R.drawable.icon_dialog);
        normalDialog.setTitle(title);
        normalDialog.setCancelable(isCancelable);
        normalDialog.setMessage(mesage);
        normalDialog.setPositiveButton("确定", positiveListener);
        normalDialog.setNegativeButton("取消", negativeListener);
        // 显示
        final AlertDialog alertDialog = normalDialog.create();
        MyApplication.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return alertDialog;

    }

    /**
     * 创建一个普通对话框（包含key1、key2按键）
     *
     * @param context
     * @param title
     * @param mesage
     * @param key1
     * @param key2
     * @param key1Listener key1按键动作
     * @param key2Listener key2按键动作
     */
    public static void createCommonDialog(Context context, String title, String mesage, boolean isCancelable,
                                          String key1, String key2,
                                          DialogInterface.OnClickListener key1Listener,
                                          DialogInterface.OnClickListener key2Listener) {
        try {

            final AlertDialog.Builder normalDialog = MyAlertDialog.build(context);
//        normalDialog.setIcon(R.drawable.icon_dialog);
            normalDialog.setTitle(title);
            normalDialog.setCancelable(isCancelable);
            if (mesage != null) {
                normalDialog.setMessage(mesage);
            }
            normalDialog.setPositiveButton(key1, key1Listener);
            normalDialog.setNegativeButton(key2, key2Listener);
            // 显示
//        final MyAlertDialog alertDialog = normalDialog.create();
            MyApplication.runOnUiThread(() -> {
                try {
//                    final MyAlertDialog alertDialog = normalDialog.create();
                    normalDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return alertDialog;
    }

    /**
     * 单按键对话框
     *
     * @param context
     * @param title
     * @param mesage
     * @param key
     * @param positiveListener
     */
    public static void createCommonDialog(Context context, String title, String mesage, boolean isCancelable,
                                          String key, DialogInterface.OnClickListener positiveListener
    ) {
        try {
            final AlertDialog.Builder normalDialog = MyAlertDialog.build(context);
//        normalDialog.setIcon(R.drawable.icon_dialog);
            normalDialog.setTitle(title);
            normalDialog.setCancelable(isCancelable);
            if (mesage != null) {
                normalDialog.setMessage(mesage);
            }
            normalDialog.setPositiveButton(key, positiveListener);

            // 显示
//        final MyAlertDialog alertDialog = normalDialog.create();
            MyApplication.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
//                    final MyAlertDialog alertDialog = normalDialog.create();
                        normalDialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return alertDialog;
    }


    /**
     * 创建一个进度对话框（圆形、旋转）
     *
     * @param context
     * @param title
     * @param message
     * @return
     */
    public static ProgressDialog createProgressDialog
    (Context context, String title, String message/*,
             DialogInterface.OnClickListener positiveListener,DialogInterface.OnClickListener negativeListener*/) {

        final ProgressDialog progressDialog = new ProgressDialog(context, R.style.alertDialogTheme);
//        normalDialog.setIcon(R.drawable.icon_dialog);
        if (!StringHelper.isEmpty(title)) {
            progressDialog.setTitle(title);
        }
        if (!StringHelper.isEmpty(message)) {
            progressDialog.setMessage(message);
        }
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      /*  progressDialog.setPositiveButton("确定",positiveListener);
        progressDialog.setNegativeButton("取消",negativeListener);*/
        // 显示
        MyApplication.runOnUiThread(() -> {
            try {
                progressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return progressDialog;
    }


    /**
     * 三按键对话框
     *
     * @param context
     * @param title
     * @param msg
     * @param btnText1
     * @param btnText2
     * @param btnText3
     * @param positiveListener
     * @param neutralListener
     * @param negativeListener
     * @return
     */
    public static void createThreeButtonDialog(Context context, String title, String msg, boolean isCancelable,
                                                      String btnText1, String btnText2, String btnText3,
                                                      DialogInterface.OnClickListener neutralListener,
                                                      DialogInterface.OnClickListener negativeListener,
                                                      DialogInterface.OnClickListener positiveListener) {
      /*  final EditText et = new EditText(context);*/
        try {
            final AlertDialog.Builder dialog = MyAlertDialog.build(context);
            dialog.setTitle(title);
            if (!StringHelper.isEmpty(msg)) {
                dialog.setMessage(msg);
            }
            //  第一个按钮
            dialog.setNeutralButton(btnText1, neutralListener);
            //  中间的按钮
            dialog.setNegativeButton(btnText2, negativeListener);
            //  第三个按钮
            dialog.setPositiveButton(btnText3, positiveListener);

            MyApplication.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
//                    final MyAlertDialog alertDialog = normalDialog.create();
                        dialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            dialog.setCancelable(isCancelable);

            //  Diglog的显示
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void createTipDialog(Context mContext, String message){
        DialogCreator.createCommonDialog(mContext, "提示",
                message, true, "知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }
    public static void createTipDialog(Context mContext, String title, String message){
        DialogCreator.createCommonDialog(mContext, title,
                message, true, "知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    /**
     * 从assets文件夹之中读取文件并显示提示框
     * @param mContext
     * @param title
     * @param assetName 需要后缀名
     */
    public static void createAssetTipDialog(Context mContext, String title, String assetName){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(mContext.getAssets().open(assetName)));
            StringBuilder assetText = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                assetText.append(line);
                assetText.append("\r\n");
            }
            DialogCreator.createTipDialog(mContext, title, assetText.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(br);
        }
    }

    public interface OnClickPositiveListener {
        void onClick(Dialog dialog, View view);
    }

    public interface OnClickNegativeListener {
        void onClick(Dialog dialog, View view);
    }

    /**
     * 白天黑夜切换监听
     */
    public interface OnClickNightAndDayListener {
        void onClick(Dialog dialog, View view, boolean isDayStyle);
    }

    /**
     * 阅读style切换监听器
     */
    public interface OnReadStyleChangeListener {
        void onChange(ReadStyle readStyle);
    }

    public interface OnBrightFollowSystemChangeListener {
        void onChange(boolean isFollowSystem);
    }

    public interface OnClickDownloadAllChapterListener {
        void onClick(Dialog dialog, View view,TextView tvDownloadProgress);
    }

    public interface OnPageModeChangeListener {
        void onChange(TextView tvPageMode);
    }

    public interface OnSkipChapterListener{
        void onClick(TextView chapterTitle, TextView chapterUrl, SeekBar sbReadChapterProgress);
    }

    public interface OnMultiDialogListener{
        void onItemClick(DialogInterface dialog,int which,boolean isChecked);
        void onSelectAll(boolean isSelectAll);
    }
}
