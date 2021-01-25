package xyz.fycz.myreader.ui.popmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.databinding.MenuCustomizeComBinding;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.ui.dialog.DialogCreator;

import static xyz.fycz.myreader.widget.page.PageLoader.DEFAULT_MARGIN_WIDTH;

public class CustomizeComMenu extends FrameLayout {

    private MenuCustomizeComBinding binding;

    private Callback callback;

    private Setting setting = SysManager.getSetting();

    public CustomizeComMenu(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CustomizeComMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomizeComMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = MenuCustomizeComBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setListener(Callback callback) {
        this.callback = callback;
        initWidget();
        initListener();
    }

    public void setNavigationBarHeight(int height) {
        binding.vwNavigationBar.getLayoutParams().height = height;
    }

    /**
     * 行间距：-0.3~3.0
     * 段间距：0.0~3.0
     * 字间距：-0.20~0.50
     * padding：0~100
     *
     */

    public void initWidget() {
        initLine();
        initPara();
        initText();
        initLeft();
        initRight();
        initTop();
        initBottom();
        initSelect();
    }

    private void initListener() {
        binding.ivLineAdd.setOnClickListener(v -> {
            float tem = setting.getLineMultiplier() + 0.1f;
            if (tem >= -0.3f && tem <= 3.0f) {
                setting.setLineMultiplier(tem);
                initLine();
                callback.onTextPChange();
            }
        });
        binding.ivLineMinus.setOnClickListener(v -> {
            float tem = setting.getLineMultiplier() - 0.1f;
            if (tem >= -0.3f && tem <= 3.0f) {
                setting.setLineMultiplier(tem);
                initLine();
                callback.onTextPChange();
            }
        });
        binding.sbLineProgress.setOnSeekBarChangeListener(new OnSeekBarChange() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    setting.setLineMultiplier((progress - 3f) / 10f);
                    initLine();
                    callback.onTextPChange();
                }
            }
        });
        binding.ivParaAdd.setOnClickListener(v -> {
            float tem = setting.getParagraphSize() + 0.1f;
            if (tem >= 0.0f && tem <= 3.0f) {
                setting.setParagraphSize(tem);
                initPara();
                callback.onTextPChange();
            }
        });
        binding.ivParaMinus.setOnClickListener(v -> {
            float tem = setting.getParagraphSize() - 0.1f;
            if (tem >= 0.0f && tem <=3.0f) {
                setting.setParagraphSize(tem);
                initPara();
                callback.onTextPChange();
            }
        });
        binding.sbParaProgress.setOnSeekBarChangeListener(new OnSeekBarChange() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    setting.setParagraphSize(progress / 10f);
                    initPara();
                    callback.onTextPChange();
                }
            }
        });
        binding.ivTextAdd.setOnClickListener(v -> {
            float tem = setting.getTextLetterSpacing() + 0.01f;
            if (tem >= -0.20f && tem <= 0.50f) {
                setting.setTextLetterSpacing(tem);
                initText();
                callback.onTextPChange();
            }
        });
        binding.ivTextMinus.setOnClickListener(v -> {
            float tem = setting.getTextLetterSpacing() - 0.01f;
            if (tem >= -0.20f && tem <= 0.50f) {
                setting.setTextLetterSpacing(tem);
                initText();
                callback.onTextPChange();
            }
        });
        binding.sbTextProgress.setOnSeekBarChangeListener(new OnSeekBarChange() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    setting.setTextLetterSpacing((progress - 20f) / 100f);
                    initText();
                    callback.onTextPChange();
                }
            }
        });
        binding.ivLeftAdd.setOnClickListener(v -> {
            int tem = setting.getPaddingLeft() + 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingLeft(tem);
                initLeft();
                callback.onMarginChange();
            }
        });
        binding.ivLeftMinus.setOnClickListener(v -> {
            int tem = setting.getPaddingLeft() - 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingLeft(tem);
                initLeft();
                callback.onMarginChange();
            }
        });
        binding.ivRightAdd.setOnClickListener(v -> {
            int tem = setting.getPaddingRight() + 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingRight(tem);
                initRight();
                callback.onMarginChange();
            }
        });
        binding.ivRightMinus.setOnClickListener(v -> {
            int tem = setting.getPaddingRight() - 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingRight(tem);
                initRight();
                callback.onMarginChange();
            }
        });
        binding.ivTopAdd.setOnClickListener(v -> {
            int tem = setting.getPaddingTop() + 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingTop(tem);
                initTop();
                callback.onMarginChange();
            }
        });
        binding.ivTopMinus.setOnClickListener(v -> {
            int tem = setting.getPaddingTop() - 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingTop(tem);
                initTop();
                callback.onMarginChange();
            }
        });
        binding.ivBottomAdd.setOnClickListener(v -> {
            int tem = setting.getPaddingBottom() + 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingBottom(tem);
                initBottom();
                callback.onMarginChange();
            }
        });
        binding.ivBottomMinus.setOnClickListener(v -> {
            int tem = setting.getPaddingBottom() - 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingBottom(tem);
                initBottom();
                callback.onMarginChange();
            }
        });
        binding.sbLeftProgress.setOnSeekBarChangeListener(onPaddingChange);
        binding.sbRightProgress.setOnSeekBarChangeListener(onPaddingChange);
        binding.sbTopProgress.setOnSeekBarChangeListener(onPaddingChange);
        binding.sbBottomProgress.setOnSeekBarChangeListener(onPaddingChange);

        binding.tvNormalCom.setOnClickListener(v -> {
            setting.setTightCom(false);
            initSelect();
            callback.onRefreshUI();
        });
        binding.tvTightCom.setOnClickListener(v -> {
            setting.setTightCom(true);
            initSelect();
            callback.onRefreshUI();
            DialogCreator.createTipDialog(getContext(), getContext().getResources().getString(R.string.tight_com_tip));
        });
        binding.tvReset.setOnClickListener(v -> {
            setting.setLineMultiplier(1);
            setting.setParagraphSize(0.9f);
            setting.setTextLetterSpacing(0);
            setting.setPaddingLeft(DEFAULT_MARGIN_WIDTH);
            setting.setPaddingRight(DEFAULT_MARGIN_WIDTH);
            setting.setPaddingTop(0);
            setting.setPaddingBottom(0);
            setting.setTightCom(false);
            callback.onReset();
            initWidget();
        });
    }

    private void initLine(){
        int line = (int) (setting.getLineMultiplier() * 10);
        initProgress(binding.tvLineSpacing, binding.sbLineProgress, String.format("行间距(%s)", line), line + 3);
    }

    private void initPara(){
        int para = (int) (setting.getParagraphSize() * 10);
        initProgress(binding.tvParaSpacing, binding.sbParaProgress, String.format("段间距(%s)", para), para);
    }

    private void initText(){
        int text = (int) (setting.getTextLetterSpacing() * 100);
        initProgress(binding.tvTextSpacing, binding.sbTextProgress, String.format("字间距(%s)", text), text + 20);
    }

    private void initLeft(){
        int left = setting.getPaddingLeft();
        initProgress(binding.tvLeftSpacing, binding.sbLeftProgress, String.format("左边距(%s)", left), left);
    }

    private void initRight(){
        int right = setting.getPaddingRight();
        initProgress(binding.tvRightSpacing, binding.sbRightProgress, String.format("右边距(%s)", right), right);
    }

    private void initTop(){
        int top = setting.getPaddingTop();
        initProgress(binding.tvTopSpacing, binding.sbTopProgress, String.format("上边距(%s)", top), top);
    }

    private void initBottom(){
        int bottom = setting.getPaddingBottom();
        initProgress(binding.tvBottomSpacing, binding.sbBottomProgress, String.format("下边距(%s)", bottom), bottom);
    }

    private void initSelect(){
        binding.tvTightCom.setSelected(setting.isTightCom());
        binding.tvNormalCom.setSelected(!setting.isTightCom());
    }

    private void initProgress(TextView tvSpacing, SeekBar sbProgress, String text, int value){
        tvSpacing.setText(text);
        if (sbProgress.getMax() >= value)
            sbProgress.setProgress(value);
    }

    private OnSeekBarChange onPaddingChange = new OnSeekBarChange() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser){
                switch (seekBar.getId()){
                    case R.id.sb_left_progress:
                        setting.setPaddingLeft(progress);
                        initLeft();
                        break;
                    case R.id.sb_right_progress:
                        setting.setPaddingRight(progress);
                        initRight();
                        break;
                    case R.id.sb_top_progress:
                        setting.setPaddingTop(progress);
                        initTop();
                        break;
                    case R.id.sb_bottom_progress:
                        setting.setPaddingBottom(progress);
                        initBottom();
                        break;
                }
                callback.onMarginChange();
            }
        }
    };


    public abstract class OnSeekBarChange implements SeekBar.OnSeekBarChangeListener{

        @Override
        public abstract void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    public interface Callback{
        void onTextPChange();
        void onMarginChange();
        void onRefreshUI();
        void onReset();
    }
}
