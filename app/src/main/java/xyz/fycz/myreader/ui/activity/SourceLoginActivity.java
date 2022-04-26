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

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.widget.Toolbar;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.ActivitySourceLoginBinding;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.service.CookieStore;
import xyz.fycz.myreader.util.ToastUtils;

/**
 * @author fengyue
 * @date 2021/5/15 10:43
 */
public class SourceLoginActivity extends BaseActivity<ActivitySourceLoginBinding> {
    private BookSource bookSource;
    private boolean checking = false;

    @Override
    protected void bindView() {
        binding = ActivitySourceLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.webView.destroy();
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle(getString(R.string.text_login));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        bookSource = getIntent().getParcelableExtra(APPCONST.BOOK_SOURCE);
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void initWidget() {
        WebSettings settings = binding.webView.getSettings();
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setLoadWithOverviewMode(true);
        if (bookSource.getHeaderMap(false).containsKey(APPCONST.UA_NAME)){
            settings.setUserAgentString(bookSource.getHeaderMap(false).get(APPCONST.UA_NAME));
        }
        settings.setJavaScriptEnabled(true);
        CookieManager cookieManager = CookieManager.getInstance();
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                String cookie = cookieManager.getCookie(url);
                CookieStore.INSTANCE.setCookie(bookSource.getSourceUrl(), cookie);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                String cookie = cookieManager.getCookie(url);
                CookieStore.INSTANCE.setCookie(bookSource.getSourceUrl(), cookie);
                if (checking)
                    finish();
                else
                    ToastUtils.showInfo(getString(R.string.click_check_after_success));
                super.onPageFinished(view, url);
            }
        });
        binding.webView.loadUrl(bookSource.getLoginUrl());
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_source_login, menu);
        return true;
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_check:
                if (checking) break;
                checking = true;
                ToastUtils.showInfo(getString(R.string.check_host_cookie));
                binding.webView.loadUrl(bookSource.getLoginUrl());
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
