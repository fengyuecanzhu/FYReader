package xyz.fycz.myreader.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.weaction.ddsdk.ad.DdSdkFlowAd;
import com.weaction.ddsdk.ad.DdSdkInterAd;
import com.weaction.ddsdk.ad.DdSdkInterVideoAd;
import com.weaction.ddsdk.ad.DdSdkRewardAd;

import java.util.List;

import io.reactivex.annotations.NonNull;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.databinding.ActivityDonateBinding;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.AdUtils;

/**
 * @author fengyue
 * @date 2021/4/23 21:23
 */
public class DonateActivity extends BaseActivity {

    private ActivityDonateBinding binding;
    private static final String TAG = DonateActivity.class.getSimpleName();

    @Override
    protected void bindView() {
        binding = ActivityDonateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle(getString(R.string.support_author));
    }

    @Override
    protected void initWidget() {
        AdUtils.checkHasAd().subscribe(new MySingleObserver<Boolean>() {
            @Override
            public void onSuccess(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    AdUtils.initAd();
                    initAd();
                }
            }
        });
    }

    private void initAd() {
        binding.llAdSupport.setVisibility(View.VISIBLE);
        new DdSdkFlowAd().getFlowViews(DonateActivity.this, 6, 1, new DdSdkFlowAd.FlowCallback() {
            // 信息流广告拉取完毕后返回的 views
            @Override
            public void getFlowViews(List<View> views) {
                Log.i(TAG, "信息流广告拉取完毕后返回了" + views.size() + "个view");
                binding.llAdSupport.addView(views.get(0), 2);
            }

            // 信息流广告展示后调用
            @Override
            public void show() {
                AdUtils.adRecord("flow","adShow");
                Log.i(TAG, "信息流广告展示成功");
            }

            // 广告拉取失败调用
            @Override
            public void error(String msg) {
                Log.i(TAG, "广告拉取失败\n" + msg);
            }
        });
    }

    @Override
    protected void initClick() {
        binding.llWxZsm.setOnClickListener(v -> goDonate(URLCONST.WX_ZSM));
        binding.llZfbSkm.setOnClickListener(v -> goDonate(URLCONST.ZFB_SKM));
        binding.llQqSkm.setOnClickListener(v -> goDonate(URLCONST.QQ_SKM));
        binding.llRewardedVideo.setOnClickListener(v -> {
            DdSdkRewardAd.show(this, new DdSdkRewardAd.DdSdkRewardCallback() {
                @Override
                public void show() {
                    Log.i(TAG, "激励视频展示成功");
                    AdUtils.adRecord("rewardVideo","adShow");
                }

                @Override
                public void click() {
                    Log.i(TAG, "激励视频被点击");
                    AdUtils.adRecord("rewardVideo","adClick");
                }

                @Override
                public void error(String msg) {
                }

                @Override
                public void skip() {
                    Log.i(TAG, "激励视频被跳过");
                    AdUtils.adRecord("rewardVideo","adSkip");
                }

                @Override
                public void finishCountdown() {
                    Log.i(TAG, "激励视频计时完成");
                    AdUtils.adRecord("rewardVideo","adFinishCount");
                }
            });
        });

        binding.llInterAd.setOnClickListener(v -> {
            /*
             * 参数 1  activity
             * 参数 2  marginDp (float)，插屏默认 margin 全屏幕的 24dp，此处允许开发者手动调节 margin 大小，单位为 dp，允许范围为 0dp (全屏) ~ 48dp，请开发者按需填写
             */
            DdSdkInterAd.show(this, 48f, new DdSdkInterAd.Callback() {
                @Override
                public void show() {
                    Log.i(TAG, "插屏广告展示成功");
                    AdUtils.adRecord("inter","adShow");
                }

                @Override
                public void click() {
                    Log.i(TAG, "插屏广告");
                    AdUtils.adRecord("inter","adClick");
                }

                @Override
                public void error(String msg) {
                }

                @Override
                public void close() {
                    Log.i(TAG, "插屏广告被关闭");
                    AdUtils.adRecord("inter","adClose");
                }
            });
        });
    }


    private void goDonate(String address) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(address));
            startActivity(intent);
        } catch (Exception e) {
            ToastUtils.showError(e.getLocalizedMessage());
        }
    }
}
