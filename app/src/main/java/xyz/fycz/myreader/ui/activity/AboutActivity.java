package xyz.fycz.myreader.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.databinding.ActivityAboutBinding;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.util.ShareUtils;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.ZipUtils;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.RxUtils;

/**
 * @author fengyue
 * @date 2020/9/18 22:21
 */
public class AboutActivity extends BaseActivity {
    private ActivityAboutBinding binding;

    @Override
    protected void bindView() {
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle("关于");
    }


    @Override
    protected void initWidget() {
        super.initWidget();
        binding.il.tvVersionName.setText("风月读书v" + App.getStrVersionName());
    }

    @Override
    protected void initClick() {
        super.initClick();
        ClipboardManager mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        binding.il.vmAuthor.setOnClickListener(v -> {
            //数据
            ClipData mClipData = ClipData.newPlainText("Label", "fy@fycz.xyz");
            //把数据设置到剪切板上
            assert mClipboardManager != null;
            mClipboardManager.setPrimaryClip(mClipData);
            ToastUtils.showSuccess("邮箱复制成功！");
        });
        binding.il.vwShare.setOnClickListener(v -> ShareUtils.share(this, getString(R.string.share_text) +
                SharedPreUtils.getInstance().getString(getString(R.string.downloadLink), URLCONST.LAN_ZOUS_URL)));
        binding.il.vwUpdate.setOnClickListener(v -> App.checkVersionByServer(this, true));
        binding.il.vwUpdateLog.setOnClickListener(v -> DialogCreator.createAssetTipDialog(this, "更新日志", "updatelog.fy"));
        binding.il.vwShareLog.setOnClickListener(v -> shareCrashLog());
        binding.il.vwQq.setOnClickListener(v -> {
            if (!App.joinQQGroup(this, "8PIOnHFuH6A38hgxvD_Rp2Bu-Ke1ToBn")) {
                //数据
                ClipData mClipData = ClipData.newPlainText("Label", "1085028304");
                //把数据设置到剪切板上
                assert mClipboardManager != null;
                mClipboardManager.setPrimaryClip(mClipData);
                ToastUtils.showError("未安装手Q或安装的版本不支持！\n已复制QQ群号，您可自行前往QQ添加！");
            }
        });
        binding.il.vwGit.setOnClickListener(v -> openIntent(Intent.ACTION_VIEW, getString(R.string.this_github_url)));
        binding.il.vwDisclaimer.setOnClickListener(v -> DialogCreator.createAssetTipDialog(this, "免责声明", "disclaimer.fy"));
    }

    void openIntent(String intentName, String address) {
        try {
            Intent intent = new Intent(intentName);
            intent.setData(Uri.parse(address));
            startActivity(intent);
        } catch (Exception e) {
            ToastUtils.showError(e.getLocalizedMessage());
        }
    }


    private void updateCrashLog() {
        File logDir = new File(APPCONST.LOG_DIR);
        if (!logDir.exists() || logDir.listFiles() == null || logDir.listFiles().length == 0) {
            ToastUtils.showWarring("没有日志文件");
            return;
        }
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            String time = String.valueOf(System.currentTimeMillis());
            String fileName = "log-" + time + ".zip";
            String logZip = FileUtils.getCachePath() + File.separator + fileName;
            File zipFile = FileUtils.getFile(logZip);
            if (ZipUtils.zipFile(logDir, zipFile)) {
                emitter.onNext(OkHttpUtils.upload(URLCONST.LOG_UPLOAD_URL, logZip, fileName));
            } else {
                emitter.onError(new Throwable("日志文件压缩失败"));
            }
            emitter.onComplete();
        }).compose(RxUtils::toSimpleSingle).subscribe(new MyObserver<String>() {
            @Override
            public void onNext(@NotNull String s) {
                ToastUtils.showInfo(s);
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showError(e.getLocalizedMessage());
            }

        });
    }

    private void shareCrashLog() {
        File logDir = new File(APPCONST.LOG_DIR);
        if (!logDir.exists() || logDir.listFiles() == null || logDir.listFiles().length == 0) {
            ToastUtils.showWarring("没有日志文件");
            return;
        }
        Observable.create((ObservableOnSubscribe<File>) emitter -> {
            String time = String.valueOf(System.currentTimeMillis());
            String fileName = "log-" + time + ".zip";
            String logZip = FileUtils.getCachePath() + File.separator + fileName;
            File zipFile = FileUtils.getFile(logZip);
            if (ZipUtils.zipFile(logDir, zipFile)) {
                emitter.onNext(zipFile);
            } else {
                emitter.onError(new Throwable("日志文件压缩失败"));
            }
            emitter.onComplete();
        }).compose(RxUtils::toSimpleSingle).subscribe(new MyObserver<File>() {
            @Override
            public void onNext(@NotNull File file) {
                ShareUtils.share(AboutActivity.this, file, file.getName(), "application/x-zip-compressed");
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showError(e.getLocalizedMessage());
            }

        });
    }
}
