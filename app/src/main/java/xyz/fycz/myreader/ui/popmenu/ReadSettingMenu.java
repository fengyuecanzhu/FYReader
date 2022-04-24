/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.ui.popmenu;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.kongzue.dialogx.dialogs.BottomMenu;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.databinding.MenuReadSettingBinding;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.enums.Language;
import xyz.fycz.myreader.ui.activity.ReadActivity;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.widget.page.PageMode;

public class ReadSettingMenu extends FrameLayout {

    private MenuReadSettingBinding binding;

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
        binding = MenuReadSettingBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setListener(Activity activity, Callback callback) {
        this.context = activity;
        this.callback = callback;
        initWidget();
        initListener();
    }

    public void setNavigationBarHeight(int height) {
        binding.vwNavigationBar.getLayoutParams().height = height;
    }

    private void initWidget() {
        binding.tvTextSize.setText(String.valueOf(setting.getReadWordSize()));
        initSwitchST(false);
        initComposition();
        initStyleImage();
        initStyle();
        initHVScreen();
    }

    private void initListener() {
        //减小字体
        binding.tvReduceTextSize.setOnClickListener(v -> {
            if (setting.getReadWordSize() > 10) {
                binding.tvTextSize.setText(String.valueOf(setting.getReadWordSize() - 1));
                setting.setReadWordSize(setting.getReadWordSize() - 1);
                SysManager.saveSetting(setting);
                if (callback != null) {
                    callback.onTextSizeChange();
                }
            }
        });
        //增大字体
        binding.tvIncreaseTextSize.setOnClickListener(v -> {
            if (setting.getReadWordSize() < 60) {
                binding.tvTextSize.setText(String.valueOf(setting.getReadWordSize() + 1));
                setting.setReadWordSize(setting.getReadWordSize() + 1);
                SysManager.saveSetting(setting);
                if (callback != null) {
                    callback.onTextSizeChange();
                }
            }
        });
        //繁简切换
        binding.tvSwitchSt.setOnClickListener(v -> {
            initSwitchST(true);
            callback.onRefreshPage();
        });
        //字体选择
        binding.tvTextFont.setOnClickListener(v -> callback.onFontClick());
        //行距单倍
        binding.ivLineSpacing4.setOnClickListener(v -> setLineSpacing(0.6f, 0.4f, 4));
        //行距双倍
        binding.ivLineSpacing3.setOnClickListener(v -> setLineSpacing(1.2f, 1.1f, 3));
        //行距三倍
        binding.ivLineSpacing2.setOnClickListener(v -> setLineSpacing(1.8f, 1.8f, 2));
        //行距默认
        binding.tvLineSpacing1.setOnClickListener(v -> setLineSpacing(1.0f, 0.9f, 1));
        //自定义行距
        binding.tvLineSpacing0.setOnClickListener(v -> ((ReadActivity) context).showCustomizeMenu());
        //缩进
        binding.tvIntent.setOnClickListener(v -> {
            /*AlertDialog dialog = new AlertDialog.Builder(context, R.style.alertDialogTheme)
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
            dialog.show();*/
            BottomMenu.show("缩进", getResources().getStringArray(R.array.indent))
                    .setSelection(setting.getIntent())
                    .setOnMenuItemClickListener((dialog, text, which) -> {
                        setting.setIntent(which);
                        SysManager.saveSetting(setting);
                        callback.onRefreshUI();
                        return false;
                    }).setCancelButton(R.string.cancel);
        });
        //样式选择
        binding.ivCommonStyle.setOnClickListener(v -> selectedStyle(0));
        binding.ivLeatherStyle.setOnClickListener(v -> selectedStyle(1));
        binding.ivProtectEyeStyle.setOnClickListener(v -> selectedStyle(2));
        binding.ivBreenStyle.setOnClickListener(v -> selectedStyle(3));
        binding.ivBlueDeepStyle.setOnClickListener(v -> selectedStyle(4));
        binding.ivCustomStyle.setOnClickListener(v -> {
            setting.saveLayout(5);
            if (setting.isDayStyle()) {
                selectedStyle(5);
            }
            ((ReadActivity) context).showCustomizeLayoutMenu();
        });
        //自动翻页
        binding.readTvAutoPage.setOnClickListener(v -> callback.onAutoPageClick());
        //翻页模式
        binding.readTvPageMode.setOnClickListener(v -> {
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
            /*MyAlertDialog.build(context)
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
                        callback.onPageModeChange();
                    }).show();*/
            BottomMenu.show("翻页模式", getResources().getStringArray(R.array.page_mode))
                    .setSelection(checkedItem)
                    .setOnMenuItemClickListener((dialog, text, which) -> {
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
                        callback.onPageModeChange();
                        return false;
                    }).setCancelButton(R.string.cancel);
        });
        //横屏竖屏切换
        binding.readTvHvScreen.setOnClickListener(v -> {
            setting.setHorizontalScreen(!setting.isHorizontalScreen());
            initHVScreen();
            SysManager.saveSetting(setting);
            callback.onHVChange();
        });
        //更多设置
        binding.readTvMoreSetting.setOnClickListener(v -> callback.onMoreSettingClick());
    }

    private void initSwitchST(boolean isChange) {
        switch (setting.getLanguage()){
            case normal:
                binding.tvSwitchSt.setSelected(false);
                binding.tvSwitchSt.setText("繁");
                if (isChange){
                    setting.setLanguage(Language.traditional);
                    ToastUtils.showInfo("已设置文本为简转繁");
                    initSwitchST(false);
                }
                break;
            case traditional:
                binding.tvSwitchSt.setSelected(true);
                binding.tvSwitchSt.setText("繁");
                if (isChange){
                    setting.setLanguage(Language.simplified);
                    DialogCreator.createTipDialog(context, context.getString(R.string.traditional_to_simplified_tip));
                    initSwitchST(false);
                }
                break;
            case simplified:
                binding.tvSwitchSt.setSelected(true);
                binding.tvSwitchSt.setText("简");
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
        binding.ivCommonStyle.setImageDrawable(setting.getBgDrawable(0, context, 50, 50));
        binding.ivLeatherStyle.setImageDrawable(setting.getBgDrawable(1, context, 50, 50));
        binding.ivProtectEyeStyle.setImageDrawable(setting.getBgDrawable(2, context, 50, 50));
        binding.ivBreenStyle.setImageDrawable(setting.getBgDrawable(3, context, 50, 50));
        binding.ivBlueDeepStyle.setImageDrawable(setting.getBgDrawable(4, context, 50, 50));
    }

    public void initStyle() {
        if (!setting.isDayStyle()){
            return;
        }
        binding.ivCommonStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        binding.ivLeatherStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        binding.ivProtectEyeStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        binding.ivBreenStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        binding.ivBlueDeepStyle.setBorderColor(context.getResources().getColor(R.color.read_menu_text));
        binding.ivCustomStyle.setSelected(false);
        switch (setting.getCurReadStyleIndex()) {
            case 0:
                binding.ivCommonStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 1:
                binding.ivLeatherStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 2:
                binding.ivProtectEyeStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 3:
                binding.ivBreenStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 4:
                binding.ivBlueDeepStyle.setBorderColor(context.getResources().getColor(R.color.sys_dialog_setting_word_red));
                break;
            case 5:
                binding.ivCustomStyle.setSelected(true);
                break;
        }
    }

    public void initComposition(){
        if (vLastLineSpacing != null) {
            vLastLineSpacing.setSelected(false);
        }
        switch (setting.getComposition()){
            case 0:
                binding.tvLineSpacing0.setSelected(true);
                vLastLineSpacing = binding.tvLineSpacing0;
                break;
            case 1:
                binding.tvLineSpacing1.setSelected(true);
                vLastLineSpacing = binding.tvLineSpacing1;
                break;
            case 2:
                binding.ivLineSpacing2.setSelected(true);
                vLastLineSpacing = binding.ivLineSpacing2;
                break;
            case 3:
                binding.ivLineSpacing3.setSelected(true);
                vLastLineSpacing = binding.ivLineSpacing3;
                break;
            case 4:
                binding.ivLineSpacing4.setSelected(true);
                vLastLineSpacing = binding.ivLineSpacing4;
                break;
            default:
                binding.tvLineSpacing1.setSelected(true);
                vLastLineSpacing = binding.tvLineSpacing1;
                break;
        }
    }


    private void initHVScreen(){
        if (setting.isHorizontalScreen()){
            binding.readTvHvScreen.setText("竖屏阅读");
        }else {
            binding.readTvHvScreen.setText("横屏阅读");
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
        void onRefreshPage();
        void onPageModeChange();
        void onRefreshUI();
        void onStyleChange();
        void onTextSizeChange();
        void onFontClick();
        void onAutoPageClick();
        void onHVChange();
        void onMoreSettingClick();
    }
}
