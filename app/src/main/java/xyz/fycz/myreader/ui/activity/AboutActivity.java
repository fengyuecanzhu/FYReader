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
import io.reactivex.disposables.Disposable;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.databinding.ActivityAboutBinding;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.LoadingDialog;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.util.ShareUtils;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.ZipUtils;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.ImageLoader;
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
        binding.il.tvVersionName.setText(String.format("风月读书v%s", App.getStrVersionName()));
    }

    @Override
    protected void initClick() {
        super.initClick();
        ClipboardManager mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        binding.il.rlUpdate.setOnClickListener(v -> App.checkVersionByServer(this, true));
        binding.il.rlUpdateLog.setOnClickListener(v -> DialogCreator.createAssetTipDialog(this, "更新日志", "updatelog.fy"));

        binding.il.rlShare.setOnClickListener(v -> ShareUtils.share(this, getString(R.string.share_text) +
                SharedPreUtils.getInstance().getString(getString(R.string.downloadLink), URLCONST.LAN_ZOUS_URL)));
        binding.il.rlQq.setOnClickListener(v -> {
            if (!App.joinQQGroup(this, "8PIOnHFuH6A38hgxvD_Rp2Bu-Ke1ToBn")) {
                //数据
                ClipData mClipData = ClipData.newPlainText("Label", "1085028304");
                //把数据设置到剪切板上
                mClipboardManager.setPrimaryClip(mClipData);
                ToastUtils.showError("未安装手Q或安装的版本不支持！\n已复制QQ群号，您可自行前往QQ添加！");
            }
        });

        binding.il.rlOfficialWeb.setOnClickListener(v -> {
            MyAlertDialog.showFullWebViewDia(this, URLCONST.OFFICIAL_WEB,
                    true, null);
        });
        binding.il.rlGit.setOnClickListener(v -> {
            MyAlertDialog.showFullWebViewDia(this, getString(R.string.this_github_url),
                    true, null);
        });

        binding.il.rlContactAuthor.setOnClickListener(v -> {
            //数据
            ClipData mClipData = ClipData.newPlainText("Label", "fengyuecanzhu@gmail.com");
            //把数据设置到剪切板上
            mClipboardManager.setPrimaryClip(mClipData);
            ToastUtils.showSuccess("邮箱\"fengyuecanzhu@gmail.com\"已复制到剪切板");
        });
        binding.il.rlShareLog.setOnClickListener(v -> DialogCreator.createCommonDialog(this, "分享崩溃日志",
                "你是希望将日志上传到服务器，还是直接分享给他人？", true,
                "上传服务器", "直接分享", (dialog, which) -> uploadCrashLog(), (dialog, which) -> shareCrashLog()));

        binding.il.rlPrivacyPolicy.setOnClickListener(v -> MyAlertDialog
                .showFullWebViewDia(this, "file:///android_asset/PrivacyPolicy.html",
                        false, null));
        binding.il.rlDisclaimer.setOnClickListener(v -> DialogCreator.createAssetTipDialog(this, "免责声明", "disclaimer.fy"));
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


    private void uploadCrashLog() {
        File logDir = new File(APPCONST.LOG_DIR);
        if (!logDir.exists() || logDir.listFiles() == null || logDir.listFiles().length == 0) {
            ToastUtils.showWarring("没有日志文件");
            return;
        }
        final Disposable[] disposable = new Disposable[1];
        LoadingDialog dialog = new LoadingDialog(this, "正在上传", () -> {
            if (disposable[0] != null) disposable[0].dispose();
        });
        dialog.show();
        String time = String.valueOf(System.currentTimeMillis());
        String fileName = "log-" + time + ".zip";
        String logZip = FileUtils.getCachePath() + File.separator + fileName;
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            File zipFile = FileUtils.getFile(logZip);
            if (ZipUtils.zipFile(logDir, zipFile)) {
                emitter.onNext(OkHttpUtils.upload(URLCONST.LOG_UPLOAD_URL, logZip, fileName));
            } else {
                emitter.onError(new Throwable("日志文件压缩失败"));
            }
            emitter.onComplete();
        }).compose(RxUtils::toSimpleSingle).subscribe(new MyObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable[0] = d;
                addDisposable(d);
            }

            @Override
            public void onNext(@NotNull String s) {
                ToastUtils.showInfo(s);
                FileUtils.deleteFile(APPCONST.LOG_DIR);
                FileUtils.deleteFile(logZip);
                dialog.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showError(e.getLocalizedMessage());
                FileUtils.deleteFile(logZip);
                dialog.dismiss();
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
            public void onSubscribe(Disposable d) {
                addDisposable(d);
            }

            @Override
            public void onNext(@NotNull File file) {
                ShareUtils.share(AboutActivity.this, file, "分享日志文件", "application/x-zip-compressed");
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showError(e.getLocalizedMessage());
            }

        });
    }
}
