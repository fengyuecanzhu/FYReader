package xyz.fycz.myreader.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;

import java.net.URLEncoder;
import java.util.List;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.ActivitySourceEditBinding;
import xyz.fycz.myreader.entity.sourcedebug.DebugEntity;
import xyz.fycz.myreader.entity.sourceedit.EditEntity;
import xyz.fycz.myreader.entity.sourceedit.EditEntityUtil;
import xyz.fycz.myreader.greendao.DbManager;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.ui.adapter.SourceEditAdapter;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.webapi.crawler.source.MatcherCrawler;

/**
 * @author fengyue
 * @date 2021/2/9 10:54
 */
public class SourceEditActivity extends BaseActivity {
    private ActivitySourceEditBinding binding;

    private BookSource source;
    private List<EditEntity> sourceEntities;
    private List<EditEntity> searchEntities;
    private List<EditEntity> infoEntities;
    private List<EditEntity> tocEntities;
    private List<EditEntity> contentEntities;
    private List<EditEntity> findEntities;
    private EditEntityUtil entityUtil;

    private SourceEditAdapter adapter;

    @Override
    protected void bindView() {
        binding = ActivitySourceEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle("书源编辑");
    }

    @Override
    protected boolean initSwipeBackEnable() {
        return false;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        source = getIntent().getParcelableExtra(APPCONST.BOOK_SOURCE);
        if (source == null) {
            source = new BookSource();
            source.setEnable(true);
            source.setSourceType(APPCONST.XPATH);
        }
        entityUtil = EditEntityUtil.INSTANCE;
        adapter = new SourceEditAdapter();
    }

    private void initEntities(BookSource source) {
        sourceEntities = entityUtil.getSourceEntities(source);
        searchEntities = entityUtil.getSearchEntities(source.getSearchRule());
        findEntities = entityUtil.getFindEntities(source.getFindRule());
        infoEntities = entityUtil.getInfoEntities(source.getInfoRule());
        tocEntities = entityUtil.getTocEntities(source.getTocRule());
        contentEntities = entityUtil.getContentEntities(source.getContentRule());
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        binding.cbSourceEnable.setChecked(source.getEnable());
        initSpinner();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        upRecyclerView();
    }

    private void initSpinner() {
        ArrayAdapter<CharSequence> sourceTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.source_type, android.R.layout.simple_spinner_item);
        sourceTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.sSourceType.setAdapter(sourceTypeAdapter);
        int sourceType = 0;
        if (source.getSourceType() == null) source.setSourceType(APPCONST.MATCHER);
        switch (source.getSourceType()) {
            case APPCONST.MATCHER:
                sourceType = 0;
                break;
            case APPCONST.XPATH:
                sourceType = 1;
                break;
            case APPCONST.JSON_PATH:
                sourceType = 2;
                break;
            case APPCONST.THIRD_SOURCE:
                sourceType = 3;
                break;
            case APPCONST.THIRD_3_SOURCE:
                sourceType = 4;
                break;
        }
        binding.sSourceType.setSelection(sourceType);
    }

    @Override
    protected void initClick() {
        super.initClick();
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setEditEntities(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_source_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            BookSource source = getSource();
            if (checkSource(source)) {
                BookSourceManager.addBookSource(source);
                this.source = source;
                setResult(Activity.RESULT_OK);
                ToastUtils.showSuccess("书源保存成功");
            }
            return true;
        } else if (item.getItemId() == R.id.action_debug_search) {
            debug(item.getTitle(), DebugEntity.SEARCH);
        } else if (item.getItemId() == R.id.action_debug_info) {
            debug(item.getTitle(), DebugEntity.INFO);
        } else if (item.getItemId() == R.id.action_debug_toc) {
            debug(item.getTitle(), DebugEntity.TOC);
        } else if (item.getItemId() == R.id.action_debug_content) {
            debug(item.getTitle(), DebugEntity.CONTENT);
        } else if (item.getItemId() == R.id.action_login) {
            BookSource source = getSource();
            if (!StringHelper.isEmpty(source.getLoginUrl())) {
                Intent intent = new Intent(this, SourceLoginActivity.class);
                intent.putExtra(APPCONST.BOOK_SOURCE, source);
                startActivity(intent);
            } else {
                ToastUtils.showWarring("当前书源没有配置登录地址");
            }
        } else if (item.getItemId() == R.id.action_clear_cookie) {
            DbManager.getDaoSession().getCookieBeanDao().deleteByKey(getSource().getSourceUrl());
            ToastUtils.showSuccess("Cookie清除成功");
        }
        return super.onOptionsItemSelected(item);
    }

    private void debug(CharSequence title, int debugMode) {
        String hint = "请输入URL";
        if (debugMode == DebugEntity.SEARCH) {
            hint = "请输入关键词";
        }
        BookSource source = getSource();
        DebugEntity debugEntity = new DebugEntity();
        debugEntity.setDebugMode(debugMode);
        debugEntity.setBookSource(source);
        MyAlertDialog.createInputDia(this, String.valueOf(title), hint,
                "", true, 500, text -> {
                    if (debugMode == DebugEntity.SEARCH) {
                        debugEntity.setKey(text);
                        try {
                            MatcherCrawler sc = new MatcherCrawler(source);
                            if (!sc.getSearchCharset().toLowerCase().equals("utf-8")) {
                                text = URLEncoder.encode(text, sc.getSearchCharset());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        debugEntity.setUrl(source.getSearchRule().getSearchUrl().replace("{key}", text));
                    } else {
                        debugEntity.setUrl(text);
                    }
                }, (dialog, which) -> {
                    Intent intent = new Intent(this, SourceDebugActivity.class);
                    intent.putExtra("debugEntity", debugEntity);
                    startActivity(intent);
                });
    }

    @Override
    public void finish() {
        BookSource source = getSource();
        if (!source.equals(this.source)) {
            DialogCreator.createThreeButtonDialog(this, "退出"
                    , "当前书源已更改，是否保存？", true,
                    "直接退出", "取消", "保存并退出",
                    (dialog, which) -> super.finish(), null,
                    (dialog, which) -> {
                        if (checkSource(source)) {
                            BookSourceManager.addBookSource(source);
                            setResult(Activity.RESULT_OK);
                            super.finish();
                        }
                    }
            );
        } else {
            super.finish();
        }
    }

    private void setEditEntities(int tabPosition) {
        switch (tabPosition) {
            case 1:
                adapter.refreshItems(searchEntities);
                break;
            case 2:
                adapter.refreshItems(findEntities);
                break;
            case 3:
                adapter.refreshItems(infoEntities);
                break;
            case 4:
                adapter.refreshItems(tocEntities);
                break;
            case 5:
                adapter.refreshItems(contentEntities);
                break;
            default:
                adapter.refreshItems(sourceEntities);
                break;
        }
        binding.recyclerView.scrollToPosition(0);
    }

    private void upRecyclerView() {
        initEntities(source);
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0));
        setEditEntities(0);
    }

    private BookSource getSource() {
        BookSource source = entityUtil.getSource(this.source, sourceEntities);
        source.setEnable(binding.cbSourceEnable.isChecked());
        String sourceType = APPCONST.MATCHER;
        switch (binding.sSourceType.getSelectedItemPosition()) {
            case 0:
                sourceType = APPCONST.MATCHER;
                break;
            case 1:
                sourceType = APPCONST.XPATH;
                break;
            case 2:
                sourceType = APPCONST.JSON_PATH;
                break;
            case 3:
                sourceType = APPCONST.THIRD_SOURCE;
                break;
            case 4:
                sourceType = APPCONST.THIRD_3_SOURCE;
                break;
        }
        source.setSourceType(sourceType);
        source.setSearchRule(entityUtil.getSearchRule(searchEntities));
        source.setFindRule(entityUtil.getFindRule(findEntities));
        source.setInfoRule(entityUtil.getInfoRule(infoEntities));
        source.setTocRule(entityUtil.getTocRule(tocEntities));
        source.setContentRule(entityUtil.getContentRule(contentEntities));
        return source;
    }

    private boolean checkSource(BookSource source) {
        if (StringHelper.isEmpty(source.getSourceName()) ||
                StringHelper.isEmpty(source.getSourceUrl())) {
            ToastUtils.showWarring("书源URL和名称不能为空");
            return false;
        }
        return true;
    }
}
