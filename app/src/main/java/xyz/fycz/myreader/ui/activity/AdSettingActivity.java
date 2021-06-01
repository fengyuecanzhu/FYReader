package xyz.fycz.myreader.ui.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.kongzue.dialogx.dialogs.BottomMenu;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.databinding.ActivityAdSettingBinding;
import xyz.fycz.myreader.ui.dialog.LoadingDialog;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.util.DateHelper;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.AdUtils;
import xyz.fycz.myreader.util.utils.FileUtils;

/**
 * @author fengyue
 * @date 2021/4/23 12:51
 */
public class AdSettingActivity extends BaseActivity {

    private ActivityAdSettingBinding binding;
    private LoadingDialog loadingDialog;
    private SharedPreUtils spu;
    private int curAdTimes;
    private int curAdCount;
    private boolean bookDetailAd;
    private Disposable cancelDis;

    @Override
    protected void bindView() {
        binding = ActivityAdSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle(getString(R.string.ad_setting));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        spu = SharedPreUtils.getInstance();
        curAdTimes = spu.getInt("curAdTimes", 3);
        String splashAdCount = spu.getString("splashAdCount");
        String[] splashAdCounts = splashAdCount.split(":");
        String today = DateHelper.getYearMonthDay1();
        if (today.equals(splashAdCounts[0])){
            curAdCount = Integer.parseInt(splashAdCounts[1]);
        }else {
            curAdCount = 0;
        }
        bookDetailAd = spu.getBoolean("bookDetailAd", false);
    }

    @Override
    protected void initWidget() {
        loadingDialog = new LoadingDialog(this, "正在加载", () -> {
            if (cancelDis != null && !cancelDis.isDisposed()) {
                cancelDis.dispose();
            }
        });
        loadingDialog.show();
        AdUtils.checkHasAd().subscribe(new MySingleObserver<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
                cancelDis = d;
            }

            @Override
            public void onSuccess(@NonNull Boolean aBoolean) {
                binding.scAd.setChecked(aBoolean);
                if (aBoolean) {
                    binding.llAdSetting.setVisibility(View.VISIBLE);
                }
                loadingDialog.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                loadingDialog.dismiss();
            }
        });
        String curAdTimesStr = getAdTimesStr(curAdTimes);
        binding.tvSplashCurAdTimes.setText(getString(R.string.splash_cur_ad_times, curAdTimesStr, curAdCount + "次"));
        binding.scBookDetailAd.setChecked(bookDetailAd);
    }

    @Override
    protected void initClick() {
        binding.llSplashAdTimes.setOnClickListener(v -> {
            loadingDialog.show();
            AdUtils.adTimes().subscribe(new MySingleObserver<int[]>() {
                @Override
                public void onSubscribe(Disposable d) {
                    cancelDis = d;
                }

                @Override
                public void onSuccess(@NonNull int[] ints) {
                    loadingDialog.dismiss();
                    int checked = 0;
                    CharSequence[] adTimes = new CharSequence[ints.length];
                    for (int i = 0; i < ints.length; i++) {
                        int k = ints[i];
                        adTimes[i] = getAdTimesStr(k);
                        if (k == curAdTimes) {
                            checked = i;
                        }
                    }
                    /*MyAlertDialog.build(AdSettingActivity.this)
                            .setTitle(getString(R.string.splash_ad_times))
                            .setSingleChoiceItems(adTimes, checked, (dialog, which) -> {
                                curAdTimes = ints[which];
                                spu.putInt("curAdTimes", curAdTimes);
                                binding.tvSplashCurAdTimes.setText(getString(R.string.splash_cur_ad_times, adTimes[which],  curAdCount + "次"));
                                dialog.dismiss();
                            }).setNegativeButton("取消", null).show();*/
                    BottomMenu.show(getString(R.string.splash_ad_times), adTimes)
                            .setSelection(checked)
                            .setOnMenuItemClickListener((dialog, text, which) -> {
                                curAdTimes = ints[which];
                                spu.putInt("curAdTimes", curAdTimes);
                                binding.tvSplashCurAdTimes.setText(getString(R.string.splash_cur_ad_times, adTimes[which],  curAdCount + "次"));
                                return false;
                            }).setCancelButton(R.string.cancel);
                }

                @Override
                public void onError(Throwable e) {
                    loadingDialog.dismiss();
                }
            });
        });
        binding.rlBookDetailAd.setOnClickListener(v -> {
            bookDetailAd = !bookDetailAd;
            spu.putBoolean("bookDetailAd", bookDetailAd);
            binding.scBookDetailAd.setChecked(bookDetailAd);
        });
        binding.rlDeleteAdFile.setOnClickListener(v -> {
            FileUtils.deleteFile(FileUtils.getFilePath());
            ToastUtils.showSuccess("广告文件删除成功");
        });
    }

    @Override
    protected void processLogic() {
        super.processLogic();
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }

    private String getAdTimesStr(int adTimes) {
        if (adTimes == -1) {
            return "一直显示";
        } else if (adTimes == 0) {
            return "不显示";
        } else {
            return adTimes + "次";
        }
    }
}
