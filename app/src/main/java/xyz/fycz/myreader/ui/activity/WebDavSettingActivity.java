package xyz.fycz.myreader.ui.activity;

import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.model.storage.BackupRestoreUi;
import xyz.fycz.myreader.model.storage.WebDavHelp;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.ToastUtils;

import java.util.ArrayList;

/**
 * @author fengyue
 * @date 2020/10/4 20:44
 */
public class WebDavSettingActivity extends BaseActivity {
    @BindView(R.id.webdav_setting_webdav_url)
    LinearLayout llWebdavUrl;
    @BindView(R.id.tv_webdav_url)
    TextView tvWebdavUrl;
    @BindView(R.id.webdav_setting_webdav_account)
    LinearLayout llWebdavAccount;
    @BindView(R.id.tv_webdav_account)
    TextView tvWebdavAccount;
    @BindView(R.id.webdav_setting_webdav_password)
    LinearLayout llWebdavPassword;
    @BindView(R.id.tv_webdav_password)
    TextView tvWebdavPassword;
    @BindView(R.id.webdav_setting_webdav_restore)
    LinearLayout llWebdavRestore;

    private String webdavUrl;
    private String webdavAccount;
    private String webdavPassword;
    @Override
    protected int getContentId() {
        return R.layout.activity_webdav_setting;
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle(getString(R.string.webdav_setting));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        webdavUrl = SharedPreUtils.getInstance().getString("webdavUrl", APPCONST.DEFAULT_WEB_DAV_URL);
        webdavAccount = SharedPreUtils.getInstance().getString("webdavAccount", "");
        webdavPassword = SharedPreUtils.getInstance().getString("webdavPassword", "");
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        tvWebdavUrl.setText(webdavUrl);
        tvWebdavAccount.setText(StringHelper.isEmpty(webdavAccount) ? "请输入WebDav账号" : webdavAccount);
        tvWebdavPassword.setText(StringHelper.isEmpty(webdavPassword) ? "请输入WebDav授权密码" : "************");
    }

    @Override
    protected void initClick() {
        super.initClick();
        final String[] webdavTexts = new String[3];
        llWebdavUrl.setOnClickListener(v -> {
            MyAlertDialog.createInputDia(this, getString(R.string.webdav_url),
                    "", webdavUrl.equals(APPCONST.DEFAULT_WEB_DAV_URL) ?
                    "" : webdavUrl, true, 100,
                    text -> webdavTexts[0] = text,
                    (dialog, which) -> {
                        webdavUrl = webdavTexts[0];
                        tvWebdavUrl.setText(webdavUrl);
                        SharedPreUtils.getInstance().putString("webdavUrl", webdavUrl);
                        dialog.dismiss();
                    });
        });
        llWebdavAccount.setOnClickListener(v -> {
            MyAlertDialog.createInputDia(this, getString(R.string.webdav_account),
                    "", webdavAccount, true, 100,
                    text -> webdavTexts[1] = text,
                    (dialog, which) -> {
                        webdavAccount = webdavTexts[1];
                        tvWebdavAccount.setText(webdavAccount);
                        SharedPreUtils.getInstance().putString("webdavAccount", webdavAccount);
                        dialog.dismiss();
                    });
        });
        llWebdavPassword.setOnClickListener(v -> {
            MyAlertDialog.createInputDia(this, getString(R.string.webdav_password),
                    "", webdavPassword, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    true, 100,
                    text -> webdavTexts[2] = text,
                    (dialog, which) -> {
                        webdavPassword = webdavTexts[2];
                        tvWebdavPassword.setText("************");
                        SharedPreUtils.getInstance().putString("webdavPassword", webdavPassword);
                        dialog.dismiss();
                    });
        });
        llWebdavRestore.setOnClickListener(v -> {
            Single.create((SingleOnSubscribe<ArrayList<String>>) emitter -> {
                emitter.onSuccess(WebDavHelp.INSTANCE.getWebDavFileNames());
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MySingleObserver<ArrayList<String>>() {
                        @Override
                        public void onSuccess(ArrayList<String> strings) {
                            if (!WebDavHelp.INSTANCE.showRestoreDialog(WebDavSettingActivity.this, strings, BackupRestoreUi.INSTANCE)) {
                                ToastUtils.showWarring("没有备份");
                            }
                        }
                    });
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.webdav_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_tip){
            DialogCreator.createAssetTipDialog(this, "如何使用WebDav进行云备份？", "webdavhelp.fy");
        }
        return super.onOptionsItemSelected(item);
    }
}
