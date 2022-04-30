/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.disposables.Disposable;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.BitIntentDataManager;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.ActivityFindBookBinding;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.ui.adapter.TabFragmentPageAdapter;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.fragment.FindBook1Fragment;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;
import xyz.fycz.myreader.webapi.crawler.source.find.Third3FindCrawler;
import xyz.fycz.myreader.webapi.crawler.source.find.ThirdFindCrawler;

/**
 * @author fengyue
 * @date 2021/7/21 20:10
 */
public class FindBookActivity extends BaseActivity<ActivityFindBookBinding> {
    private BookSource source;
    private FindCrawler findCrawler;
    private List<String> groups;

    @Override
    protected void bindView() {
        binding = ActivityFindBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Intent intent = new Intent();
        BitIntentDataManager.getInstance().putData(intent, findCrawler);
        outState.putParcelable(INTENT, intent);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        Object obj = BitIntentDataManager.getInstance().getData(getIntent());
        if (obj instanceof BookSource) {
            source = (BookSource) obj;
            if (APPCONST.THIRD_3_SOURCE.equals(source.getSourceType())) {
                findCrawler = new Third3FindCrawler(source);
            } else {
                findCrawler = new ThirdFindCrawler(source);
            }
        } else if (obj instanceof FindCrawler) {
            findCrawler = (FindCrawler) obj;
        }
        if (findCrawler == null) {
            ToastUtils.showError("findCrawler为null");
            finish();
            return;
        }
        initData();
    }

    @Override
    protected void initWidget() {
        binding.loading.setOnReloadingListener(this::initData);
    }

    private void initData() {
        findCrawler.initData()
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new MyObserver<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(@NotNull Boolean aBoolean) {
                        if (aBoolean) {
                            groups = findCrawler.getGroups();
                            setUpToolbar();
                            initFragments();
                        } else {
                            ToastUtils.showError("发现规则语法错误");
                        }
                        binding.loading.showFinish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        ToastUtils.showError("数据加载失败\n" + e.getLocalizedMessage());
                        binding.loading.showError();
                    }
                });
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
    }

    private void setUpToolbar() {
        if (groups.size() == 1) {
            binding.tabTlIndicator.setVisibility(View.GONE);
            getSupportActionBar().setTitle(groups.get(0));
        } else {
            binding.tabTlIndicator.setVisibility(View.VISIBLE);
        }
    }

    private void initFragments() {
        TabFragmentPageAdapter adapter = new TabFragmentPageAdapter(getSupportFragmentManager());
        for (String group : groups) {
            adapter.addFragment(new FindBook1Fragment(findCrawler.getKindsByKey(group), findCrawler), group);
        }
        binding.tabVp.setAdapter(adapter);
        binding.tabVp.setOffscreenPageLimit(3);
        binding.tabTlIndicator.setUpWithViewPager(binding.tabVp);
    }
/********************************Event***************************************/
    /**
     * 创建菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_store, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (findCrawler.needSearch()) {
            menu.findItem(R.id.action_tip).setVisible(true);
        }
        return true;
    }

    /**
     * 导航栏菜单点击事件
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_tip) {
            DialogCreator.createTipDialog(this,
                    getResources().getString(R.string.top_sort_tip, "此发现"));
            return true;
        }
        return false;
    }
}
