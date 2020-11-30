package xyz.fycz.myreader.ui.popmenu;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.ui.dialog.DialogCreator;

import static xyz.fycz.myreader.widget.page.PageLoader.DEFAULT_MARGIN_HEIGHT;
import static xyz.fycz.myreader.widget.page.PageLoader.DEFAULT_MARGIN_WIDTH;

public class CustomizeComMenu extends FrameLayout {
    @BindView(R.id.tv_line_spacing)
    TextView tvLineSpacing;
    @BindView(R.id.iv_line_minus)
    ImageView ivLineMinus;
    @BindView(R.id.sb_line_progress)
    SeekBar sbLineProgress;
    @BindView(R.id.iv_line_add)
    ImageView ivLineAdd;
    @BindView(R.id.tv_para_spacing)
    TextView tvParaSpacing;
    @BindView(R.id.iv_para_minus)
    ImageView ivParaMinus;
    @BindView(R.id.sb_para_progress)
    SeekBar sbParaProgress;
    @BindView(R.id.iv_para_add)
    ImageView ivParaAdd;
    @BindView(R.id.tv_text_spacing)
    TextView tvTextSpacing;
    @BindView(R.id.iv_text_minus)
    ImageView ivTextMinus;
    @BindView(R.id.sb_text_progress)
    SeekBar sbTextProgress;
    @BindView(R.id.iv_text_add)
    ImageView ivTextAdd;
    @BindView(R.id.tv_left_spacing)
    TextView tvLeftSpacing;
    @BindView(R.id.iv_left_minus)
    ImageView ivLeftMinus;
    @BindView(R.id.sb_left_progress)
    SeekBar sbLeftProgress;
    @BindView(R.id.iv_left_add)
    ImageView ivLeftAdd;
    @BindView(R.id.tv_right_spacing)
    TextView tvRightSpacing;
    @BindView(R.id.iv_right_minus)
    ImageView ivRightMinus;
    @BindView(R.id.sb_right_progress)
    SeekBar sbRightProgress;
    @BindView(R.id.iv_right_add)
    ImageView ivRightAdd;
    @BindView(R.id.tv_top_spacing)
    TextView tvTopSpacing;
    @BindView(R.id.iv_top_minus)
    ImageView ivTopMinus;
    @BindView(R.id.sb_top_progress)
    SeekBar sbTopProgress;
    @BindView(R.id.iv_top_add)
    ImageView ivTopAdd;
    @BindView(R.id.tv_bottom_spacing)
    TextView tvBottomSpacing;
    @BindView(R.id.iv_bottom_minus)
    ImageView ivBottomMinus;
    @BindView(R.id.sb_bottom_progress)
    SeekBar sbBottomProgress;
    @BindView(R.id.iv_bottom_add)
    ImageView ivBottomAdd;
    @BindView(R.id.tv_normal_com)
    TextView tvNormalCom;
    @BindView(R.id.tv_tight_com)
    TextView tvTightCom;
    @BindView(R.id.tv_reset)
    TextView tvReset;
    @BindView(R.id.vwNavigationBar)
    View vwNavigationBar;

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
        View view = LayoutInflater.from(context).inflate(R.layout.menu_customize_com, this);
        ButterKnife.bind(this, view);
    }

    public void setListener(Callback callback) {
        this.callback = callback;
        initWidget();
        initListener();
    }

    public void setNavigationBarHeight(int height) {
        vwNavigationBar.getLayoutParams().height = height;
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
        ivLineAdd.setOnClickListener(v -> {
            float tem = setting.getLineMultiplier() + 0.1f;
            if (tem >= -0.3f && tem <= 3.0f) {
                setting.setLineMultiplier(tem);
                initLine();
                callback.onTextPChange();
            }
        });
        ivLineMinus.setOnClickListener(v -> {
            float tem = setting.getLineMultiplier() - 0.1f;
            if (tem >= -0.3f && tem <= 3.0f) {
                setting.setLineMultiplier(tem);
                initLine();
                callback.onTextPChange();
            }
        });
        sbLineProgress.setOnSeekBarChangeListener(new OnSeekBarChange() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    setting.setLineMultiplier((progress - 3f) / 10f);
                    initLine();
                    callback.onTextPChange();
                }
            }
        });
        ivParaAdd.setOnClickListener(v -> {
            float tem = setting.getParagraphSize() + 0.1f;
            if (tem >= 0.0f && tem <= 3.0f) {
                setting.setParagraphSize(tem);
                initPara();
                callback.onTextPChange();
            }
        });
        ivParaMinus.setOnClickListener(v -> {
            float tem = setting.getParagraphSize() - 0.1f;
            if (tem >= 0.0f && tem <=3.0f) {
                setting.setParagraphSize(tem);
                initPara();
                callback.onTextPChange();
            }
        });
        sbParaProgress.setOnSeekBarChangeListener(new OnSeekBarChange() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    setting.setParagraphSize(progress / 10f);
                    initPara();
                    callback.onTextPChange();
                }
            }
        });
        ivTextAdd.setOnClickListener(v -> {
            float tem = setting.getTextLetterSpacing() + 0.01f;
            if (tem >= -0.20f && tem <= 0.50f) {
                setting.setTextLetterSpacing(tem);
                initText();
                callback.onTextPChange();
            }
        });
        ivTextMinus.setOnClickListener(v -> {
            float tem = setting.getTextLetterSpacing() - 0.01f;
            if (tem >= -0.20f && tem <= 0.50f) {
                setting.setTextLetterSpacing(tem);
                initText();
                callback.onTextPChange();
            }
        });
        sbTextProgress.setOnSeekBarChangeListener(new OnSeekBarChange() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    setting.setTextLetterSpacing((progress - 20f) / 100f);
                    initText();
                    callback.onTextPChange();
                }
            }
        });
        ivLeftAdd.setOnClickListener(v -> {
            int tem = setting.getPaddingLeft() + 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingLeft(tem);
                initLeft();
                callback.onMarginChange();
            }
        });
        ivLeftMinus.setOnClickListener(v -> {
            int tem = setting.getPaddingLeft() - 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingLeft(tem);
                initLeft();
                callback.onMarginChange();
            }
        });
        ivRightAdd.setOnClickListener(v -> {
            int tem = setting.getPaddingRight() + 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingRight(tem);
                initRight();
                callback.onMarginChange();
            }
        });
        ivRightMinus.setOnClickListener(v -> {
            int tem = setting.getPaddingRight() - 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingRight(tem);
                initRight();
                callback.onMarginChange();
            }
        });
        ivTopAdd.setOnClickListener(v -> {
            int tem = setting.getPaddingTop() + 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingTop(tem);
                initTop();
                callback.onMarginChange();
            }
        });
        ivTopMinus.setOnClickListener(v -> {
            int tem = setting.getPaddingTop() - 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingTop(tem);
                initTop();
                callback.onMarginChange();
            }
        });
        ivBottomAdd.setOnClickListener(v -> {
            int tem = setting.getPaddingBottom() + 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingBottom(tem);
                initBottom();
                callback.onMarginChange();
            }
        });
        ivBottomMinus.setOnClickListener(v -> {
            int tem = setting.getPaddingBottom() - 1;
            if (tem >= 0 && tem <= 100) {
                setting.setPaddingBottom(tem);
                initBottom();
                callback.onMarginChange();
            }
        });
        sbLeftProgress.setOnSeekBarChangeListener(onPaddingChange);
        sbRightProgress.setOnSeekBarChangeListener(onPaddingChange);
        sbTopProgress.setOnSeekBarChangeListener(onPaddingChange);
        sbBottomProgress.setOnSeekBarChangeListener(onPaddingChange);

        tvNormalCom.setOnClickListener(v -> {
            setting.setTightCom(false);
            initSelect();
            callback.onRefreshUI();
        });
        tvTightCom.setOnClickListener(v -> {
            setting.setTightCom(true);
            initSelect();
            callback.onRefreshUI();
            DialogCreator.createTipDialog(getContext(), getContext().getResources().getString(R.string.tight_com_tip));
        });
        tvReset.setOnClickListener(v -> {
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
        initProgress(tvLineSpacing, sbLineProgress, String.format("行间距(%s)", line), line + 3);
    }

    private void initPara(){
        int para = (int) (setting.getParagraphSize() * 10);
        initProgress(tvParaSpacing, sbParaProgress, String.format("段间距(%s)", para), para);
    }

    private void initText(){
        int text = (int) (setting.getTextLetterSpacing() * 100);
        initProgress(tvTextSpacing, sbTextProgress, String.format("字间距(%s)", text), text + 20);
    }

    private void initLeft(){
        int left = setting.getPaddingLeft();
        initProgress(tvLeftSpacing, sbLeftProgress, String.format("左边距(%s)", left), left);
    }

    private void initRight(){
        int right = setting.getPaddingRight();
        initProgress(tvRightSpacing, sbRightProgress, String.format("右边距(%s)", right), right);
    }

    private void initTop(){
        int top = setting.getPaddingTop();
        initProgress(tvTopSpacing, sbTopProgress, String.format("上边距(%s)", top), top);
    }

    private void initBottom(){
        int bottom = setting.getPaddingBottom();
        initProgress(tvBottomSpacing, sbBottomProgress, String.format("下边距(%s)", bottom), bottom);
    }

    private void initSelect(){
        tvTightCom.setSelected(setting.isTightCom());
        tvNormalCom.setSelected(!setting.isTightCom());
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
