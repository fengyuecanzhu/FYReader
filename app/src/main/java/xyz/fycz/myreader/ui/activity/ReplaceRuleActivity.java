package xyz.fycz.myreader.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
import com.kongzue.dialogx.dialogs.BottomMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import xyz.fycz.myreader.databinding.ActivityReplaceRuleBinding;
import xyz.fycz.myreader.greendao.entity.ReplaceRuleBean;
import xyz.fycz.myreader.model.ReplaceRuleManager;
import xyz.fycz.myreader.ui.adapter.ReplaceRuleAdapter;
import xyz.fycz.myreader.ui.adapter.helper.ItemTouchCallback;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.ui.dialog.ReplaceDialog;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.ClipBoardUtil;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;
import xyz.fycz.myreader.widget.DividerItemDecoration;
import xyz.fycz.myreader.widget.swipemenu.SwipeMenuLayout;

import static android.text.TextUtils.isEmpty;
import static xyz.fycz.myreader.util.UriFileUtil.getPath;

/**
 * @author fengyue
 * @date 2021/1/19 10:02
 */
public class ReplaceRuleActivity extends BaseActivity {

    private ActivityReplaceRuleBinding binding;

    private SearchView searchView;
    private List<ReplaceRuleBean> mReplaceRules;
    private ReplaceRuleAdapter mAdapter;

    @Override
    protected void bindView() {
        binding = ActivityReplaceRuleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        mAdapter = new ReplaceRuleAdapter(this, mReplaceRules, new ReplaceRuleAdapter.OnSwipeListener() {
            @Override
            public void onDel(int which, ReplaceRuleBean bean) {
                mReplaceRules.remove(bean);
                mAdapter.removeItem(which);
                setUpBarTitle();
            }

            @Override
            public void onTop(int which, ReplaceRuleBean bean) {
                if (which > 0 && which < mReplaceRules.size()) {
                    mReplaceRules.remove(bean);
                    mReplaceRules.add(0, bean);
                    mAdapter.toTop(which, bean);
                }
            }
        });
        binding.rvRuleList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRuleList.setAdapter(mAdapter);
        //设置分割线
        binding.rvRuleList.addItemDecoration(new DividerItemDecoration(this));
        //添加数据
        mAdapter.refreshItems(mReplaceRules);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initClick() {
        super.initClick();
        binding.rvRuleList.setOnTouchListener((v, event) -> {
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

        MenuItem search = menu.findItem(R.id.action_search);
        searchView = (SearchView) search.getActionView();
        TextView textView = (TextView) searchView.findViewById(R.id.search_src_text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        searchView.setQueryHint("搜索替换规则");
        searchView.setMaxWidth(getResources().getDisplayMetrics().widthPixels);
        searchView.onActionViewCollapsed();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_add_rule) {
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
        } else if (itemId == R.id.action_import) {
            /*MyAlertDialog.build(this)
                    .setTitle("导入规则")
                    .setItems(R.array.import_rule, (dialog, which) -> {
                        if (which == 0) {
                            String text = ClipBoardUtil.paste(this);
                            if (!isEmpty(text)) {
                                importDataS(text);
                            } else {
                                ToastUtils.showError("剪切板内容为空，导入失败");
                            }
                        } else if (which == 1) {
                            ToastUtils.showInfo("请选择内容替换规则JSON文件");
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("application/json");
                            startActivityForResult(intent, APPCONST.REQUEST_IMPORT_REPLACE_RULE);
                        } else {
                            String[] url = new String[1];
                            MyAlertDialog.createInputDia(this, "网络导入",
                                    "请输入网址", "", true, 200,
                                    text -> url[0] = text,
                                    (dialog1, which1) -> importDataS(url[0]));

                        }
                    }).show();*/
            BottomMenu.show("导入规则", getResources().getStringArray(R.array.import_rule))
                    .setOnMenuItemClickListener((dialog, text, which) -> {
                        if (which == 0) {
                            String pasteText = ClipBoardUtil.paste(this);
                            if (!isEmpty(pasteText)) {
                                importDataS(pasteText);
                            } else {
                                ToastUtils.showError("剪切板内容为空，导入失败");
                            }
                        } else if (which == 1) {
                            ToastUtils.showInfo("请选择内容替换规则JSON文件");
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("application/json");
                            startActivityForResult(intent, APPCONST.REQUEST_IMPORT_REPLACE_RULE);
                        } else {
                            String[] url = new String[1];
                            MyAlertDialog.createInputDia(this, "网络导入",
                                    "请输入网址", "", true, 200,
                                    text0 -> url[0] = text0,
                                    (dialog1, which1) -> importDataS(url[0]));

                        }
                        return false;
                    }).setCancelButton(R.string.cancel);
        } else if (itemId == R.id.action_export) {
            if (mReplaceRules == null || mReplaceRules.size() == 0) {
                ToastUtils.showWarring("当前没有任何规则，无法导出！");
                return true;
            }
            XXPermissions.with(this)
                    .permission(APPCONST.STORAGE_PERMISSIONS)
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if (FileUtils.writeText(GsonExtensionsKt.getGSON().toJson(mReplaceRules),
                                    FileUtils.getFile(APPCONST.FILE_DIR + "ReplaceRule.json"))) {
                                DialogCreator.createTipDialog(ReplaceRuleActivity.this,
                                        "内容替换规则导出成功，导出位置：" + APPCONST.FILE_DIR + "ReplaceRule.json");
                            }
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            ToastUtils.showWarring("没有储存权限！");
                        }
                    });
        } else if (itemId == R.id.action_reverse) {
            for (ReplaceRuleBean ruleBean : mReplaceRules) {
                ruleBean.setEnable(!ruleBean.getEnable());
            }
            ReplaceRuleManager.addDataS(mReplaceRules);
            mAdapter.notifyDataSetChanged();
            refreshUI();
        } else if (itemId == R.id.action_delete) {
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
                        mAdapter.setBeans(mReplaceRules);
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

    @Override
    public void onBackPressed() {
        if (!"".contentEquals(searchView.getQuery())){
            searchView.onActionViewCollapsed();
        }else {
            super.onBackPressed();
        }
    }

    private void refreshUI() {
        Intent result = new Intent();
        result.putExtra(APPCONST.RESULT_NEED_REFRESH, true);
        setResult(AppCompatActivity.RESULT_OK, result);
    }
}
