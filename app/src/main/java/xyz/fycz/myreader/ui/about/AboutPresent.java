package xyz.fycz.myreader.ui.about;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.base.BasePresenter;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.util.ShareUtils;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.TextHelper;

/**
 * @author fengyue
 * @date 2020/7/31 11:39
 */
public class AboutPresent implements BasePresenter {

    private AboutActivity mAboutActivity;

    public AboutPresent(AboutActivity mAboutActivity) {
        this.mAboutActivity = mAboutActivity;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void start() {
        mAboutActivity.getLlTitleBack().setOnClickListener(v -> mAboutActivity.finish());
        mAboutActivity.getTvTitleText().setText("关于");
        mAboutActivity.getTvVersionName().setText("风月读书v" + MyApplication.getStrVersionName());
        mAboutActivity.getVmAuthor().setOnClickListener(v -> {
            ClipboardManager mClipboardManager = (ClipboardManager) mAboutActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            //数据
            ClipData mClipData = ClipData.newPlainText("Label", "fy@fycz.xyz");
            //把数据设置到剪切板上
            assert mClipboardManager != null;
            mClipboardManager.setPrimaryClip(mClipData);
            TextHelper.showText("邮箱复制成功！");
        });
        mAboutActivity.getVmShare().setOnClickListener(v -> ShareUtils.share(mAboutActivity, mAboutActivity.getString(R.string.share_text) +
                SharedPreUtils.getInstance().getString("downloadLink")));
        mAboutActivity.getVmUpdate().setOnClickListener(v -> MyApplication.checkVersionByServer(mAboutActivity, true, null));
        mAboutActivity.getVmUpdateLog().setOnClickListener(v -> DialogCreator.createAssetTipDialog(mAboutActivity, "更新日志", "updatelog.fy"));
        mAboutActivity.getVmGit().setOnClickListener(v -> openIntent(Intent.ACTION_VIEW, mAboutActivity.getString(R.string.this_github_url)));
        mAboutActivity.getVmDisclaimer().setOnClickListener(v -> DialogCreator.createAssetTipDialog(mAboutActivity, "免责声明", "disclaimer.fy"));
    }

    void openIntent(String intentName, String address) {
        try {
            Intent intent = new Intent(intentName);
            intent.setData(Uri.parse(address));
            mAboutActivity.startActivity(intent);
        } catch (Exception e) {
            TextHelper.showText(e.getLocalizedMessage());
        }
    }
}
