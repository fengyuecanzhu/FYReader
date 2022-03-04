package xyz.fycz.myreader.ui.activity;

import android.annotation.SuppressLint;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.widget.Toolbar;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.databinding.ActivityFeedbackBinding;
import xyz.fycz.myreader.ui.dialog.DialogCreator;

/**
 * @author fengyue
 * @date 2020/12/24 20:48
 */
public class FeedbackActivity extends BaseActivity<ActivityFeedbackBinding> {

    @Override
    protected void bindView() {
        binding =ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        WebSettings webSettings = binding.wvFeedback.getSettings();

        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);
        binding.wvFeedback.loadUrl("https://www.wjx.cn/jq/102348283.aspx");
        binding.wvFeedback.setWebViewClient(new WebViewClient(){
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
                if (!App.isDestroy(FeedbackActivity.this))
                    binding.refreshLayout.showError();
            }
        });
        binding.wvFeedback.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100 && !App.isDestroy(FeedbackActivity.this)){
                    binding.refreshLayout.showFinish();
                }
            }});
    }

    @Override
    protected void initClick() {
        super.initClick();
        binding.refreshLayout.setOnReloadingListener(() -> {
            binding.wvFeedback.loadUrl("https://www.wjx.cn/jq/102348283.aspx");
        });
    }
}
