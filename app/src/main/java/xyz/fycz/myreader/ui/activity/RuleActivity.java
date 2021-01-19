package xyz.fycz.myreader.ui.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.greendao.entity.ReplaceRuleBean;
import xyz.fycz.myreader.model.ReplaceRuleManager;
import xyz.fycz.myreader.ui.adapter.ReplaceRuleAdapter;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.ui.dialog.ReplaceDialog;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.ClipBoardUtil;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;
import xyz.fycz.myreader.widget.DividerItemDecoration;

import static android.text.TextUtils.isEmpty;
import static xyz.fycz.myreader.util.UriFileUtil.getPath;

/**
 * @author fengyue
 * @date 2021/1/19 10:02
 */
public class RuleActivity extends BaseActivity {
    @BindView(R.id.rv_rule_list)
    RecyclerView rvRuleList;

    private List<ReplaceRuleBean> mReplaceRules;
    private ReplaceRuleAdapter mAdapter;

    @Override
    protected int getContentId() {
        return R.layout.activity_rule;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        ReplaceRuleManager.getAll().subscribe(new MySingleObserver<List<ReplaceRuleBean>>() {
            @Override
            public void onSuccess(@NonNull List<ReplaceRuleBean> replaceRuleBeans) {
                mReplaceRules = replaceRuleBeans;
                initRuleList();
                setUpBarTitle();
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showError("数据加载失败\n" + e.getLocalizedMessage());
            }
        });
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setUpBarTitle();
        setStatusBarColor(R.color.colorPrimary, true);
    }

    private void setUpBarTitle() {
        getSupportActionBar().setTitle(String.format("%s(共%s个)",
                getString(R.string.replace_rule), mReplaceRules == null ? 0 : mReplaceRules.size()));
    }

    protected void initRuleList() {
        mAdapter = new ReplaceRuleAdapter(this, (which, data) -> {
            mReplaceRules.remove(data);
            mAdapter.notifyItemRemoved(which);
            mAdapter.removeItem2(data);
            setUpBarTitle();
        });
        rvRuleList.setLayoutManager(new LinearLayoutManager(this));
        rvRuleList.setAdapter(mAdapter);
        //设置分割线
        rvRuleList.addItemDecoration(new DividerItemDecoration(this));
        mAdapter.refreshItems(mReplaceRules);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initClick() {
        super.initClick();
        rvRuleList.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                SwipeMenuLayout viewCache = SwipeMenuLayout.getViewCache();
                if (null != viewCache) {
                    viewCache.smoothClose();
                }
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_rule:
                ReplaceRuleBean newRuleBean = new ReplaceRuleBean();
                newRuleBean.setReplaceSummary("");
                newRuleBean.setEnable(true);
                newRuleBean.setRegex("");
                newRuleBean.setIsRegex(false);
                newRuleBean.setReplacement("");
                newRuleBean.setSerialNumber(0);
                newRuleBean.setUseTo("");
                ReplaceDialog replaceDialog = new ReplaceDialog(this, newRuleBean
                        , () -> {
                    ToastUtils.showSuccess("内容替换规则添加成功！");
                    mReplaceRules.add(newRuleBean);
                    mAdapter.addItem(newRuleBean);
                    setUpBarTitle();
                    refreshUI();
                });
                replaceDialog.show(getSupportFragmentManager(), "replaceRule");
                break;
            case R.id.action_import:
                MyAlertDialog.build(this)
                        .setTitle("导入规则")
                        .setItems(R.array.import_rule, (dialog, which) -> {
                            if (which == 0) {
                                String text = ClipBoardUtil.paste(this);
                                if (!isEmpty(text)) {
                                    importDataS(text);
                                } else {
                                    ToastUtils.showError("剪切板内容为空，导入失败");
                                }
                            } else {
                                ToastUtils.showInfo("请选择内容替换规则JSON文件");
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("application/json");
                                startActivityForResult(intent, APPCONST.REQUEST_IMPORT_REPLACE_RULE);
                            }
                        }).show();
                break;
            case R.id.action_export:
                if (mReplaceRules == null || mReplaceRules.size() == 0){
                    ToastUtils.showWarring("当前没有任何规则，无法导出！");
                    return true;
                }
                if (FileUtils.writeText(GsonExtensionsKt.getGSON().toJson(mReplaceRules),
                        FileUtils.getFile(APPCONST.FILE_DIR + "ReplaceRule.json"))) {
                    DialogCreator.createTipDialog(this,
                            "内容替换规则导出成功，导出位置：" + APPCONST.FILE_DIR + "ReplaceRule.json");
                }
                break;
            case R.id.action_reverse:
                for (ReplaceRuleBean ruleBean : mReplaceRules) {
                    ruleBean.setEnable(!ruleBean.getEnable());
                }
                ReplaceRuleManager.addDataS(mReplaceRules);
                mAdapter.notifyDataSetChanged();
                refreshUI();
                break;
            case R.id.action_delete:
                DialogCreator.createCommonDialog(this, "删除禁用规则",
                        "确定要删除所有禁用规则吗？", true,
                        (dialog, which) -> {
                            List<ReplaceRuleBean> ruleBeans = new ArrayList<>();
                            for (ReplaceRuleBean ruleBean : mReplaceRules) {
                                if (!ruleBean.getEnable()) {
                                    ruleBeans.add(ruleBean);
                                }
                            }
                            ReplaceRuleManager.delDataS(ruleBeans);
                            mReplaceRules.removeAll(ruleBeans);
                            mAdapter.removeItems(ruleBeans);
                            ToastUtils.showSuccess("禁用规则删除成功");
                            setUpBarTitle();
                            refreshUI();
                        }, null);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == APPCONST.REQUEST_IMPORT_REPLACE_RULE) {
                String path = getPath(this, data.getData());
                String json = FileUtils.readText(path);
                if (!isEmpty(json)) {
                    importDataS(json);
                } else {
                    ToastUtils.showError("文件读取失败");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void importDataS(String text) {
        Observable<Boolean> observable = ReplaceRuleManager.importReplaceRule(text);
        if (observable != null) {
            observable.subscribe(new MyObserver<Boolean>() {
                @Override
                public void onNext(Boolean aBoolean) {
                    if (aBoolean) {
                        mReplaceRules = ReplaceRuleManager.getAllRules();
                        mAdapter.refreshItems(mReplaceRules);
                        setUpBarTitle();
                        refreshUI();
                        ToastUtils.showSuccess("内容替换规则导入成功");
                    } else {
                        ToastUtils.showError("格式不对");
                    }
                }

                @Override
                public void onError(Throwable e) {
                    ToastUtils.showError("格式不对");
                }
            });
        } else {
            ToastUtils.showError("导入失败");
        }
    }

    private void refreshUI() {
        Intent result = new Intent();
        result.putExtra(APPCONST.RESULT_NEED_REFRESH, true);
        setResult(AppCompatActivity.RESULT_OK, result);
    }
}
