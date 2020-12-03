package xyz.fycz.myreader.ui.popmenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.entity.ReadStyle;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.ZipUtils;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;
import xyz.fycz.myreader.util.utils.MeUtils;
import xyz.fycz.myreader.widget.CircleCheckBox;
import xyz.fycz.myreader.widget.HorizontalListView;

public class CustomizeLayoutMenu extends FrameLayout {
    @BindView(R.id.iv_bg_color)
    ImageView ivBgColor;
    @BindView(R.id.iv_font_color)
    ImageView ivFontColor;
    @BindView(R.id.cb_share_layout)
    CircleCheckBox cbShareLayout;
    @BindView(R.id.bgImgList)
    HorizontalListView bgImgList;
    @BindView(R.id.tv_save_layout)
    TextView tvSaveLayout;
    @BindView(R.id.tv_import_layout)
    TextView tvImportLayout;
    @BindView(R.id.tv_export_layout)
    TextView tvExportLayout;
    @BindView(R.id.tv_reset_layout)
    TextView tvResetLayout;
    @BindView(R.id.vwNavigationBar)
    View vwNavigationBar;

    private Callback callback;

    private Activity context;

    private Setting setting = SysManager.getSetting();
    private String bgPath;
    private BgImgListAdapter bgImgListAdapter;

    public CustomizeLayoutMenu(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CustomizeLayoutMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomizeLayoutMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_cumtomize_layout, this);
        ButterKnife.bind(this, view);
    }

    public void setListener(Activity activity, Callback callback) {
        this.context = activity;
        this.callback = callback;
        initWidget();
        initListener();
    }

    private void initWidget() {
        ivBgColor.setImageDrawable(setting.getBgDrawable(setting.getCurReadStyleIndex(), context, 200, 120));
        ivFontColor.setImageDrawable(new ColorDrawable(setting.getTextColor()));
        cbShareLayout.setChecked(setting.isSharedLayout());

        //背景图列表
        bgImgListAdapter = new BgImgListAdapter(context);
        bgImgListAdapter.initList();
        bgImgList.setAdapter(bgImgListAdapter);
    }

    public void upColor() {
        int curStyleIndex = setting.getCurReadStyleIndex();
        if (!setting.isDayStyle()){
            ToastUtils.showInfo("当前正在编辑夜间模式");
            curStyleIndex = 6;
        }
        ivFontColor.setImageDrawable(new ColorDrawable(setting.getTextColor()));
        ivBgColor.setImageDrawable(setting.getBgDrawable(curStyleIndex, context, 200, 120));
    }

    private void initListener() {
        ivBgColor.setOnClickListener(v -> {
            ColorPickerDialog.newBuilder()
                    .setColor(setting.getBgColor())
                    .setShowAlphaSlider(false)
                    .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                    .setDialogId(APPCONST.SELECT_BG_COLOR)
                    .show((FragmentActivity) context);
        });
        //选择文字颜色
        ivFontColor.setOnClickListener(view ->
                ColorPickerDialog.newBuilder()
                        .setColor(setting.getTextColor())
                        .setShowAlphaSlider(false)
                        .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                        .setDialogId(APPCONST.SELECT_TEXT_COLOR)
                        .show((FragmentActivity) context));
        cbShareLayout.setOnClickListener(v -> {
            if (!setting.isSharedLayout()) {
                DialogCreator.createCommonDialog(context, "提示",
                        context.getString(R.string.share_layout_tip),
                        true, (dialog, which) -> {
                            setting.setSharedLayout(true);
                            setting.sharedLayout();
                            SysManager.saveSetting(setting);
                            cbShareLayout.setChecked(true);
                        }, null);
            } else {
                setting.setSharedLayout(false);
                setting.sharedLayout();
                SysManager.saveSetting(setting);
                cbShareLayout.setChecked(false);
            }
        });
        bgImgList.setOnItemClickListener((adapterView, view, i, l) -> {
            if (i == 0) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                context.startActivityForResult(intent, APPCONST.REQUEST_SELECT_BG);
            } else {
                bgPath = bgImgListAdapter.getItemAssetsFile(i - 1);
                setAssetsBg(bgPath);
            }
        });
        tvSaveLayout.setOnClickListener(v -> {
            CharSequence[] items = new CharSequence[6];
            for (int i = 0; i < items.length; i++) {
                if (i == 5) {
                    items[i] = "夜间模式";
                    break;
                }
                items[i] = "布局" + (i + 1);
            }
            MyAlertDialog.build(context)
                    .setTitle("选择布局保存的位置")
                    .setItems(items, (dialog, which) -> {
                        if (which == 5) which = 6;
                        setting.saveLayout(which);
                        SysManager.saveSetting(setting);
                        callback.upStyle();
                        ToastUtils.showSuccess("布局保存成功");
                    }).show();
        });
        tvImportLayout.setOnClickListener(v -> importLayout());
        tvExportLayout.setOnClickListener(v -> exportLayout());
        tvResetLayout.setOnClickListener(v -> {
            DialogCreator.createCommonDialog(context, "重置布局",
                    "重置布局将会恢复所有布局为默认状态，确定要重置吗？",
                    true, (dialog, which) -> {
                        setting.resetLayout();
                        SysManager.saveSetting(setting);
                        callback.upBg();
                        callback.upStyle();
                        upColor();
                        ToastUtils.showSuccess("布局重置成功");
                    }, null);
        });
    }

    public void setNavigationBarHeight(int height) {
        vwNavigationBar.getLayoutParams().height = height;
    }

    public void setAssetsBg(String path) {
        setting.setBgIsColor(false);
        setting.setBgIsAssert(true);
        setting.setBgPath(path);
        SysManager.saveSetting(setting);
        upColor();
        callback.upBg();
    }

    public void setCustomBg(String path) {
        setting.setBgIsColor(false);
        setting.setBgIsAssert(false);
        setting.setBgPath(path);
        SysManager.saveSetting(setting);
        upColor();
        callback.upBg();
    }

    private void importLayout() {
        ToastUtils.showInfo("请选择布局配置压缩文件");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        context.startActivityForResult(intent, APPCONST.REQUEST_IMPORT_LAYOUT);
    }

    private void exportLayout() {
        CharSequence[] items = new CharSequence[5];
        for (int i = 0; i < items.length; i++) {
            items[i] = "布局" + (i + 1);
        }
        MyAlertDialog.build(context)
                .setTitle("选择需要导出的布局")
                .setItems(items, (dialog, which) -> {
                    if (setting.exportLayout(which)){
                        DialogCreator.createTipDialog(context,
                                "布局配置导出成功，导出位置：" + APPCONST.FILE_DIR + "readConfig.zip");
                    }
                }).show();
    }

    public void zip2Layout(String zipPath) {
        try {
            ZipUtils.unzipFile(zipPath, APPCONST.TEM_FILE_DIR);
            String json = FileUtils.readText(APPCONST.TEM_FILE_DIR + "readConfig.fyl");
            if (json == null) {
                ToastUtils.showError("这也许不是一个配置文件，配置导入失败");
                return;
            }
            ReadStyle readStyle = GsonExtensionsKt.getGSON().fromJson(json, ReadStyle.class);
            CharSequence[] items = new CharSequence[5];
            for (int i = 0; i < items.length; i++) {
                items[i] = "布局" + (i + 1);
            }
            MyAlertDialog.build(context)
                    .setTitle("选择要覆盖的布局")
                    .setItems(items, (dialog, which) -> {
                        setting.importLayout(which, readStyle);
                        if (!readStyle.bgIsColor() && !readStyle.bgIsAssert()){
                            FileUtils.copy(APPCONST.TEM_FILE_DIR + "bg.fyl",APPCONST.BG_FILE_DIR + "bg" + which + ".fyl");
                            readStyle.setBgPath(APPCONST.BG_FILE_DIR + "bg" + which + ".fyl");
                        }
                        FileUtils.deleteFile(APPCONST.TEM_FILE_DIR + "readConfig.fyl");
                        FileUtils.deleteFile(APPCONST.TEM_FILE_DIR + "bg.fyl");
                        ToastUtils.showSuccess("配置导入成功");
                        callback.upStyle();
                    }).show();
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtils.showError("配置导入失败" + e.getLocalizedMessage());
        }
    }

    public interface Callback {
        void upBg();
        void upStyle();
    }

    private static class BgImgListAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater mInflater;
        private List<String> assetsFiles;
        final BitmapFactory.Options options = new BitmapFactory.Options();

        BgImgListAdapter(Context context) {
            this.context = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            options.inJustDecodeBounds = false;
            options.inSampleSize = 4;
        }

        void initList() {
            AssetManager am = context.getAssets();
            String[] path;
            try {
                path = am.list("bg");  //获取所有,填入目录获取该目录下所有资源
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            assetsFiles = new ArrayList<>();
            Collections.addAll(assetsFiles, path);
        }

        @Override
        public int getCount() {
            return assetsFiles.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        String getItemAssetsFile(int position) {
            return "bg/" + assetsFiles.get(position);
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item_read_bg, null);
                holder.mImage = convertView.findViewById(R.id.iv_bg);
                holder.mTitle = convertView.findViewById(R.id.tv_desc);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (position == 0) {
                holder.mTitle.setText("选择背景");
                holder.mTitle.setTextColor(Color.parseColor("#CBCBCB"));
                holder.mImage.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_image));
            } else {
                String path = assetsFiles.get(position - 1);
                holder.mTitle.setText(getFileName(path));
                holder.mTitle.setTextColor(Color.parseColor("#909090"));
                try {
                    /*BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.mImage.getDrawable();
                    //如果图片还未回收，先强制回收该图片
                    if (bitmapDrawable != null && !bitmapDrawable.getBitmap().isRecycled()) {
                        bitmapDrawable.getBitmap().recycle();
                    }*/
                    //该变现实的图片
                    Bitmap bmp = MeUtils.getFitAssetsSampleBitmap(context.getAssets(), getItemAssetsFile(position - 1), 256, 256);
                    holder.mImage.setImageBitmap(bmp);
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.mImage.setImageBitmap(null);
                }
            }
            return convertView;
        }

        String getFileName(String path) {
            int start = path.lastIndexOf("/");
            int end = path.lastIndexOf(".");
            if (end < 0) end = path.length();
            return path.substring(start + 1, end);
        }

        private static class ViewHolder {
            private TextView mTitle;
            private ImageView mImage;
        }

    }
}
