package xyz.fycz.myreader.ui.popmenu;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.util.BrightUtil;
import xyz.fycz.myreader.widget.CircleCheckBox;

public class BrightnessEyeMenu extends FrameLayout {
    @BindView(R.id.ll_follow_sys)
    LinearLayout llFollowSys;
    @BindView(R.id.cb_follow_sys)
    CircleCheckBox cbFollowSys;
    @BindView(R.id.sb_brightness_progress)
    SeekBar sbBrightness;
    @BindView(R.id.ll_protect_eye)
    LinearLayout llProtectEye;
    @BindView(R.id.cb_protect_eye)
    CircleCheckBox cbProtectEye;
    @BindView(R.id.sb_protect_eye)
    SeekBar sbProtectEye;
    @BindView(R.id.vwNavigationBar)
    View vwNavigationBar;

    private Callback callback;

    private Activity context;

    private Setting setting = SysManager.getSetting();

    public BrightnessEyeMenu(@NonNull Context context) {
        super(context);
        init(context);
    }

    public BrightnessEyeMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BrightnessEyeMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_brightness_eye, this);
        ButterKnife.bind(this, view);
    }

    public void setListener(Activity activity, Callback callback) {
        this.context = activity;
        this.callback = callback;
        initListener();
    }

    public void initWidget() {
        sbBrightness.setProgress(setting.getBrightProgress());
        cbFollowSys.setChecked(setting.isBrightFollowSystem());

        sbProtectEye.setProgress(setting.getBlueFilterPercent() - 10);
        cbProtectEye.setChecked(setting.isProtectEye());
        sbProtectEye.setEnabled(cbProtectEye.isChecked());
    }

    private void initListener() {
        //设置亮度
        sbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    BrightUtil.setBrightness((AppCompatActivity) context, progress);
                    cbFollowSys.setChecked(false);
                    setting.setBrightProgress(progress);
                    setting.setBrightFollowSystem(false);
                    SysManager.saveSetting(setting);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //亮度跟随系统
        cbFollowSys.setOnClickListener(v -> {
            cbFollowSys.setChecked(!cbFollowSys.isChecked());
            if (cbFollowSys.isChecked()) {
                BrightUtil.followSystemBright((AppCompatActivity) context);
                setting.setBrightFollowSystem(true);
            } else {
                BrightUtil.setBrightness((AppCompatActivity) context, setting.getBrightProgress());
                setting.setBrightFollowSystem(false);
            }
            SysManager.saveSetting(setting);
        });

        //设置蓝光过滤
        sbProtectEye.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setting.setBlueFilterPercent(progress + 10);
                    SysManager.saveSetting(setting);
                    callback.upProtectEye();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //是否开启护眼
        cbProtectEye.setOnClickListener(v -> {
            cbProtectEye.setChecked(!cbProtectEye.isChecked());
            sbProtectEye.setEnabled(cbProtectEye.isChecked());
            setting.setProtectEye(cbProtectEye.isChecked());
            SysManager.saveSetting(setting);
            callback.onProtectEyeChange();
        });
    }

    public void setNavigationBarHeight(int height) {
        vwNavigationBar.getLayoutParams().height = height;
    }

    public interface Callback {
        void onProtectEyeChange();

        void upProtectEye();
    }
}
