package xyz.fycz.myreader.ui.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.widget.Toolbar;

import butterknife.BindView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.widget.RefreshLayout;

/**
 * @author fengyue
 * @date 2020/12/24 20:48
 */
public class FeedbackActivity extends BaseActivity {
    @BindView(R.id.refresh_layout)
    RefreshLayout mRlRefresh;
    @BindView(R.id.wv_feedback)
    WebView wvFeedback;

    @Override
    protected int getContentId() {
        return R.layout.activity_feedback;
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle("建议反馈");
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initWidget() {
        super.initWidget();
        //声明WebSettings子类
        WebSettings webSettings = wvFeedback.getSettings();

        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);
        wvFeedback.loadUrl("https://www.wjx.cn/jq/102348283.aspx");
        wvFeedback.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.endsWith("102348283.aspx")){
                    System.out.println(url);
                    if (url.contains("complete")){
                        DialogCreator.createCommonDialog(FeedbackActivity.this, "意见反馈",
                                "提交成功，感谢您的建议反馈！", false, "知道了",
                                (dialog, which) -> finish());
                    }
                    return true;
                }
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                mRlRefresh.showError();
            }
        });
        wvFeedback.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100){
                    mRlRefresh.showFinish();
                }
            }});
    }

    @Override
    protected void initClick() {
        super.initClick();
        mRlRefresh.setOnReloadingListener(() -> {
            wvFeedback.loadUrl("https://www.wjx.cn/jq/102348283.aspx");
        });
    }
}
