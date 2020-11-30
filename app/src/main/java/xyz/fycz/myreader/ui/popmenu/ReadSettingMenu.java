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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.enums.Language;
import xyz.fycz.myreader.ui.activity.ReadActivity;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.util.BrightUtil;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.widget.custom.CircleImageView;
import xyz.fycz.myreader.widget.page.PageMode;

public class ReadSettingMenu extends FrameLayout {
    @BindView(R.id.tv_reduce_text_size)
    TextView tvReduceTextSize;
    @BindView(R.id.tv_text_size)
    TextView tvTextSize;
    @BindView(R.id.tv_increase_text_size)
    TextView tvIncreaseTextSize;
    @BindView(R.id.tv_switch_st)
    TextView tvSwitchST;
    @BindView(R.id.tv_text_font)
    TextView tvTextFont;
    @BindView(R.id.iv_line_spacing4)
    View ivLineSpacing4;
    @BindView(R.id.iv_line_spacing3)
    View ivLineSpacing3;
    @BindView(R.id.iv_line_spacing2)
    View ivLineSpacing2;
    @BindView(R.id.tv_line_spacing1)
    View tvLineSpacing1;
    @BindView(R.id.tv_line_spacing0)
    View tvLineSpacing0;
    @BindView(R.id.tv_intent)
    TextView tvIntent;
    @BindView(R.id.iv_common_style)
    CircleImageView ivCommonStyle;
    @BindView(R.id.iv_leather_style)
    CircleImageView ivLeatherStyle;
    @BindView(R.id.iv_protect_eye_style)
    CircleImageView ivProtectEyeStyle;
    @BindView(R.id.iv_breen_style)
    CircleImageView ivBreenStyle;
    @BindView(R.id.iv_blue_deep_style)
    CircleImageView ivBlueStyle;
    @BindView(R.id.iv_custom_style)
    ImageView ivCustomStyle;
    @BindView(R.id.read_tv_auto_page)
    TextView tvAutoPage;
    @BindView(R.id.read_tv_page_mode)
    TextView tvPageMode;
    @BindView(R.id.read_tv_hv_screen)
    TextView tvHVScreen;
    @BindView(R.id.read_tv_more_setting)
    TextView tvMoreSetting;
    @BindView(R.id.vwNavigationBar)
    View vwNavigationBar;

    private View vLastLineSpacing = null;

    private Callback callback;

    private Activity context;

    private Setting setting = SysManager.getSetting();


    public ReadSettingMenu(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ReadSettingMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReadSettingMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_read_setting, this);
        ButterKnife.bind(this, view);
    }

    public void setListener(Activity activity, Callback callback) {
        this.context = activity;
        this.callback = callback;
        initWidget();
        initListener();
    }

    public void setNavigationBarHeight(int height) {
        vwNavigationBar.getLayoutParams().height = height;
    }

    private void initWidget() {
        tvTextSize.setText(String.valueOf(setting.getReadWordSize()));
        initSwitchST(false);
        initComposition();
        initStyleImage();
        initStyle();
        initHVScreen();
    }

    private void initListener() {
        //减小字体
        tvReduceTextSize.setOnClickListener(v -> {
            if (setting.getReadWordSize() > 10) {
                tvTextSize.setText(String.valueOf(setting.getReadWordSize() - 1));
                setting.setReadWordSize(setting.getReadWordSize() - 1);
                SysManager.saveSetting(setting);
                if (callback != null) {
                    callback.onTextSizeChange();
                }
            }
        });
        //增大字体
        tvIncreaseTextSize.setOnClickListener(v -> {
            if (setting.getReadWordSize() < 60) {
                tvTextSize.setText(String.valueOf(setting.getReadWordSize() + 1));
                setting.setReadWordSize(setting.getReadWordSize() + 1);
                SysManager.saveSetting(setting);
                if (callback != null) {
                    callback.onTextSizeChange();
                }
            }
        });
        //繁简切换
        tvSwitchST.setOnClickListener(v -> {
            initSwitchST(true);
            callback.onRefreshUI();
        });
        //字体选择
        tvTextFont.setOnClickListener(v -> callback.onFontClick());
        //行距单倍
        ivLineSpacing4.setOnClickListener(v -> setLineSpacing(0.6f, 0.4f, 4));
        //行距双倍
        ivLineSpacing3.setOnClickListener(v -> setLineSpacing(1.2f, 1.1f, 3));
        //行距三倍
        ivLineSpacing2.setOnClickListener(v -> setLineSpacing(1.8f, 1.8f, 2));
        //行距默认
        tvLineSpacing1.setOnClickListener(v -> setLineSpacing(1.0f, 0.9f, 1));
        //自定义行距
        tvLineSpacing0.setOnClickListener(v -> ((ReadActivity) context).showCustomizeMenu());
        //缩进
        tvIntent.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(context, R.style.alertDialogTheme)
                    .setTitle("缩进")
                    .setSingleChoiceItems(context.getResources().getStringArray(R.array.indent),
                            setting.getIntent(),
                            (dialogInterface, i) -> {
                                setting.setIntent(i);
                                SysManager.saveSetting(setting);
                                callback.onRefreshUI();
                                dialogInterface.dismiss();
                            })
                    .create();
            dialog.show();
        });
        //样式选择
        ivCommonStyle.setOnClickListener(v -> selectedStyle(0));
        ivLeatherStyle.setOnClickListener(v -> selectedStyle(1));
        ivProtectEyeStyle.setOnClickListener(v -> selectedStyle(2));
        ivBreenStyle.setOnClickListener(v -> selectedStyle(3));
        ivBlueStyle.setOnClickListener(v -> selectedStyle(4));
        ivCustomStyle.setOnClickListener(v -> {
            setting.saveLayout(5);
            if (setting.isDayStyle()) {
                selectedStyle(5);
            }
            ((ReadActivity) context).showCustomizeLayoutMenu();
        });
        //自动翻页
        tvAutoPage.setOnClickListener(v -> callback.onAutoPageClick());
        //翻页模式
        tvPageMode.setOnClickListener(v -> {
            //显示翻页模式视图
            int checkedItem;
            switch (setting.getPageMode()) {
                case COVER:
                    checkedItem = 0;
                    break;
                case SIMULATION:
                    checkedItem = 1;
                    break;
                case SLIDE:
                    checkedItem = 2;
                    break;
                case VERTICAL_COVER:
                    checkedItem = 3;
                    break;
                case SCROLL:
                    checkedItem = 4;
                    break;
                case NONE:
                    checkedItem = 5;
                    break;
                default:
                    checkedItem = 0;
            }
            MyAlertDialog.build(context)
                    .setTitle("翻页模式")
                    .setSingleChoiceItems(R.array.page_mode, checkedItem, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                setting.setPageMode(PageMode.COVER);
                                break;
                            case 1:
                                setting.setPageMode(PageMode.SIMULATION);
                                break;
                            case 2:
                                setting.setPageMode(PageMode.SLIDE);
                                break;
                            case 3:
                                setting.setPageMode(PageMode.VERTICAL_COVER);
                                break;
                            case 4:
                                setting.setPageMode(PageMode.SCROLL);
                                break;
                            case 5:
                                setting.setPageMode(PageMode.NONE);
                                break;
                        }
                        dialog.dismiss();
                        SysManager.saveSetting(setting);
                        callback.onRefreshUI();
                    }).show();
        });
        //横屏竖屏切换
        tvHVScreen.setOnClickListener(v -> {
            setting.setHorizontalScreen(!setting.isHorizontalScreen());
            initHVScreen();
            SysManager.saveSetting(setting);
            callback.onHVChange();
        });
        //更多设置
        tvMoreSetting.setOnClickListener(v -> callback.onMoreSettingClick());
    }

    private void initSwitchST(boolean isChange) {
        switch (setting.getLanguage()){
            case normal:
                tvSwitchST.setSelected(false);
                tvSwitchST.setText("繁");
                if (isChange){
                    setting.setLanguage(Language.traditional);
                    ToastUtils.showInfo("已设置文本为简转繁");
                    initSwitchST(false);
                }
                break;
            case traditional:
                tvSwitchST.setSelected(true);
                tvSwitchST.setText("繁");
                if (isChange){
                    setting.setLanguage(Language.simplified);
                    DialogCreator.createTipDialog(context, context.getString(R.string.traditional_to_simplified_tip));
                    initSwitchST(false);
                }
                break;
            case simplified:
                tvSwitchST.setSelected(true);
                tvSwitchST.setText("简");
                if (isChange){
                    setting.setLanguage(Language.normal);
                    ToastUtils.showInfo("已取消简转繁/繁转简，当前为原始文本");
                    initSwitchST(false);
                }
                break;
        }
        if (isChange){
            SysManager.saveSetting(setting);
        }
    }

    public void initStyleImage() {
        ivCommonStyle.setImageDrawable(setting.getBgDrawable(0, context, 50, 50));
        ivLeatherStyle.setImageDrawable(setting.getBgDrawable(1, context, 50, 50));
        ivProtectEyeStyle.setImageDrawable(setting.getBgDrawable(2, context, 50, 50));
        ivBreenStyle.setImageDrawable(setting.getBgDrawable(3, context, 50, 50));
        ivBlueStyle.setImageDrawable(setting.getBgDrawable(4, context, 50, 50));
    }

    public void initStyle() {
        if (!setting.isDayStyle()){
            return;
        }
        ivCommonStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        ivLeatherStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        ivProtectEyeStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        ivBreenStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        ivBlueStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        ivCustomStyle.setSelected(false);
        switch (setting.getCurReadStyleIndex()) {
            case 0:
                ivCommonStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 1:
                ivLeatherStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 2:
                ivProtectEyeStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 3:
                ivBreenStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 4:
                ivBlueStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 5:
                ivCustomStyle.setSelected(true);
                break;
        }
    }

    public void initComposition(){
        if (vLastLineSpacing != null) {
            vLastLineSpacing.setSelected(false);
        }
        switch (setting.getComposition()){
            case 0:
                tvLineSpacing0.setSelected(true);
                vLastLineSpacing = tvLineSpacing0;
                break;
            case 1:
                tvLineSpacing1.setSelected(true);
                vLastLineSpacing = tvLineSpacing1;
                break;
            case 2:
                ivLineSpacing2.setSelected(true);
                vLastLineSpacing = ivLineSpacing2;
                break;
            case 3:
                ivLineSpacing3.setSelected(true);
                vLastLineSpacing = ivLineSpacing3;
                break;
            case 4:
                ivLineSpacing4.setSelected(true);
                vLastLineSpacing = ivLineSpacing4;
                break;
            default:
                tvLineSpacing1.setSelected(true);
                vLastLineSpacing = tvLineSpacing1;
                break;
        }
    }


    private void initHVScreen(){
        if (setting.isHorizontalScreen()){
            tvHVScreen.setText("竖屏阅读");
        }else {
            tvHVScreen.setText("横屏阅读");
        }
    }

    private void setLineSpacing(float lineMultiplier, float paragraphSize, int composition){
        setting.setLineMultiplier(lineMultiplier);
        setting.setParagraphSize(paragraphSize);
        setting.setComposition(composition);
        SysManager.saveSetting(setting);
        initComposition();
        callback.onTextSizeChange();
    }

    private void selectedStyle(int readStyleIndex) {
        setting.setCurReadStyleIndex(readStyleIndex);
        SysManager.saveSetting(setting);
        initWidget();
        if (callback != null) {
            callback.onStyleChange();
        }
    }

    public interface Callback{
        void onRefreshUI();
        void onStyleChange();
        void onTextSizeChange();
        void onFontClick();
        void onAutoPageClick();
        void onHVChange();
        void onMoreSettingClick();
    }
}
