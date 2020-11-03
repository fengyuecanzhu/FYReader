package xyz.fycz.myreader.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import butterknife.BindView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.util.ShareUtils;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;

/**
 * @author fengyue
 * @date 2020/9/18 22:21
 */
public class AboutActivity extends BaseActivity {
    @BindView(R.id.tv_version_name)
    TextView tvVersionName;
    @BindView(R.id.vm_author)
    CardView vmAuthor;
    @BindView(R.id.vw_share)
    CardView vmShare;
    @BindView(R.id.vw_update)
    CardView vmUpdate;
    @BindView(R.id.vw_update_log)
    CardView vmUpdateLog;
    @BindView(R.id.vw_git)
    CardView vmGit;
    @BindView(R.id.vw_disclaimer)
    CardView vmDisclaimer;

    @Override
    protected int getContentId() {
        return R.layout.activity_about;
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
        tvVersionName.setText("风月读书v" + MyApplication.getStrVersionName());
    }

    @Override
    protected void initClick() {
        super.initClick();
        ClipboardManager mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        vmAuthor.setOnClickListener(v -> {
            //数据
            ClipData mClipData = ClipData.newPlainText("Label", "fy@fycz.xyz");
            //把数据设置到剪切板上
            assert mClipboardManager != null;
            mClipboardManager.setPrimaryClip(mClipData);
            ToastUtils.showSuccess("邮箱复制成功！");
        });
        vmShare.setOnClickListener(v -> ShareUtils.share(this, getString(R.string.share_text) +
                SharedPreUtils.getInstance().getString(getString(R.string.downloadLink, URLCONST.LAN_ZOUS_URL))));
        vmUpdate.setOnClickListener(v -> MyApplication.checkVersionByServer(this, true, null));
        vmUpdateLog.setOnClickListener(v -> DialogCreator.createAssetTipDialog(this, "更新日志", "updatelog.fy"));
        vmGit.setOnClickListener(v -> openIntent(Intent.ACTION_VIEW, getString(R.string.this_github_url)));
        vmDisclaimer.setOnClickListener(v -> DialogCreator.createAssetTipDialog(this, "免责声明", "disclaimer.fy"));

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


}
