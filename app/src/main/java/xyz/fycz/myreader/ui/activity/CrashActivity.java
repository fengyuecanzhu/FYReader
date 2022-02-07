package xyz.fycz.myreader.ui.activity;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;

import com.gyf.immersionbar.ImmersionBar;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.databinding.ActivityCrashBinding;
import xyz.fycz.myreader.util.ShareUtils;
import xyz.fycz.myreader.util.ToastUtils;

/**
 * @author fengyue
 * @date 2022/1/22 9:15
 */
public class CrashActivity extends BaseActivity {
    private ActivityCrashBinding binding;

    /**
     * 报错代码行数正则表达式
     */
    private static final Pattern CODE_REGEX = Pattern.compile("\\(\\w+\\.\\w+:\\d+\\)");
    public static final String INTENT_CRASH_KEY = "crash_exception";
    public static final String INTENT_LOG_KEY = "log_file_path";
    private String mStackTrace;
    private String logFilePath;

    public static void start(Application application, Throwable throwable, String logFilePath) {
        if (throwable == null) {
            return;
        }
        Intent intent = new Intent(application, CrashActivity.class);
        intent.putExtra(INTENT_CRASH_KEY, throwable);
        intent.putExtra(INTENT_LOG_KEY, logFilePath);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.startActivity(intent);
    }

    @Override
    protected void bindView() {
        binding = ActivityCrashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        // 设置状态栏沉浸
        ImmersionBar.setTitleBar(this, binding.llCrashBar);
    }

    @Override
    protected boolean initSwipeBackEnable() {
        return false;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        ToastUtils.showError("程序发生致命错误");
        Throwable throwable = (Throwable) getIntent().getSerializableExtra(INTENT_CRASH_KEY);
        logFilePath = getIntent().getStringExtra(INTENT_LOG_KEY);
        if (throwable == null) {
            return;
        }
        binding.tvCrashTitle.setText(throwable.getClass().getSimpleName());

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        Throwable cause = throwable.getCause();
        if (cause != null) {
            cause.printStackTrace(printWriter);
        }
        mStackTrace = stringWriter.toString();
        Matcher matcher = CODE_REGEX.matcher(mStackTrace);
        SpannableStringBuilder spannable = new SpannableStringBuilder(mStackTrace);
        if (spannable.length() > 0) {
            for (int index = 0; matcher.find(); index++) {
                // 不包含左括号（
                int start = matcher.start() + "(".length();
                // 不包含右括号 ）
                int end = matcher.end() - ")".length();
                // 设置前景
                spannable.setSpan(new ForegroundColorSpan(index < 3 ? 0xFF287BDE : 0xFF999999), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                // 设置下划线
                spannable.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            binding.tvCrashMessage.setText(spannable);
        }

        Resources res = getResources();
        DisplayMetrics displayMetrics = res.getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        String targetResource;
        if (displayMetrics.densityDpi > 480) {
            targetResource = "xxxhdpi";
        } else if (displayMetrics.densityDpi > 320) {
            targetResource = "xxhdpi";
        } else if (displayMetrics.densityDpi > 240) {
            targetResource = "xhdpi";
        } else if (displayMetrics.densityDpi > 160) {
            targetResource = "hdpi";
        } else if (displayMetrics.densityDpi > 120) {
            targetResource = "mdpi";
        } else {
            targetResource = "ldpi";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("设备品牌：\t").append(Build.BRAND)
                .append("\n设备型号：\t").append(Build.MODEL)
                .append("\n设备类型：\t").append(isTablet() ? "平板" : "手机");

        builder.append("\n屏幕宽高：\t").append(screenWidth).append(" x ").append(screenHeight)
                .append("\n屏幕密度：\t").append(displayMetrics.densityDpi)
                .append("\n目标资源：\t").append(targetResource);

        builder.append("\n安卓版本：\t").append(Build.VERSION.RELEASE)
                .append("\nAPI 版本：\t").append(Build.VERSION.SDK_INT)
                .append("\nCPU 架构：\t").append(Build.SUPPORTED_ABIS[0]);

        builder.append("\n应用版本：\t").append(App.getStrVersionName())
                .append("\n版本代码：\t").append(App.getVersionCode());

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            builder.append("\n首次安装：\t").append(dateFormat.format(new Date(packageInfo.firstInstallTime)))
                    .append("\n最近安装：\t").append(dateFormat.format(new Date(packageInfo.lastUpdateTime)))
                    .append("\n崩溃时间：\t").append(dateFormat.format(new Date()));
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        binding.tvCrashInfo.setText(builder);
    }

    @Override
    protected void initClick() {
        binding.ivCrashInfo.setOnClickListener(v -> binding.dlCrashDrawer.openDrawer(GravityCompat.START));
        binding.ivCrashShare.setOnClickListener(v -> {
            File logFile;
            if (logFilePath != null && (logFile = new File(logFilePath)).exists()) {
                ShareUtils.share(this, logFile, "崩溃日志", "text/plain");
            } else {
                ShareUtils.share(this, mStackTrace);
            }
        });
        binding.ivCrashRestart.setOnClickListener(v -> {
            // 重启应用
            ToastUtils.showInfo("重启应用");
            RestartActivity.Companion.restart(this);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        binding.ivCrashRestart.performClick();
    }

    /**
     * 判断当前设备是否是平板
     */
    public boolean isTablet() {
        return (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
