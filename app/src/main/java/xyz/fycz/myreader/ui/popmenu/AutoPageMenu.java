package xyz.fycz.myreader.ui.popmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.entity.Setting;

public class AutoPageMenu extends FrameLayout {

    @BindView(R.id.tv_auto_scroll_speed)
    TextView tvAutoPageSpeed;
    @BindView(R.id.sb_auto_scroll_progress)
    SeekBar sbAutoPageSpeed;
    @BindView(R.id.tv_exit_auto_page)
    TextView tvExitAutoPage;
    @BindView(R.id.vwNavigationBar)
    View vwNavigationBar;

    public AutoPageMenu(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AutoPageMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AutoPageMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_auto_page, this);
        ButterKnife.bind(this, view);
    }

    public void setListener(Callback callback) {
        Setting setting = SysManager.getSetting();
        sbAutoPageSpeed.setProgress(110 - setting.getAutoScrollSpeed());
        tvAutoPageSpeed.setText(String.format("翻页速度：%s %%", 110 - setting.getAutoScrollSpeed()));
        sbAutoPageSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvAutoPageSpeed.setText(String.format("翻页速度：%s %%", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                int speed = 110 - progress;
                setting.setAutoScrollSpeed(speed);
                SysManager.saveSetting(setting);
                callback.onSpeedChange();
            }
        });
        tvExitAutoPage.setOnClickListener(v -> callback.onExitClick());
    }

    public void setNavigationBarHeight(int height) {
        vwNavigationBar.getLayoutParams().height = height;
    }

    public interface Callback{
        void onSpeedChange();
        void onExitClick();
    }
}
