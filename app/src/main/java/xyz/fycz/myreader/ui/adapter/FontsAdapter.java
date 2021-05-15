package xyz.fycz.myreader.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.webapi.LanZousApi;
import xyz.fycz.myreader.webapi.ResultCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.enums.Font;
import xyz.fycz.myreader.ui.activity.FontsActivity;
import xyz.fycz.myreader.util.IOUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.widget.ProgressButton;


public class FontsAdapter extends ArrayAdapter<Font> {

    private int mResourceId;
    private Setting setting;
    private ArrayList<Font> mFontList;
    private FontsActivity mFontsActivity;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    updateDownloadPro(msg.arg1, msg.arg2, (ViewHolder) msg.obj);
                    break;
                case 2:
                    Font font = (Font) msg.obj;
                    ToastUtils.showSuccess(font.toString() + "字体下载完成");
                    notifyDataSetChanged();
                    break;
                case 3:
                    notifyDataSetChanged();
                    break;
                case 4:
                    ViewHolder viewHolder = (ViewHolder) msg.obj;
                    viewHolder.btnFontUse.setText("连接中...");
                    break;
            }
        }
    };

    public FontsAdapter(Context context, int resourceId, ArrayList<Font> datas, FontsActivity fontsActivity) {
        super(context, resourceId, datas);
        mResourceId = resourceId;
        setting = SysManager.getSetting();
        mFontList = datas;
        this.mFontsActivity = fontsActivity;
    }

    @Override
    public void notifyDataSetChanged() {
        setting = SysManager.getSetting();
        super.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(mResourceId, null);
            viewHolder.tvFontName = (TextView) convertView.findViewById(R.id.tv_font_name);
            viewHolder.btnFontUse = (ProgressButton) convertView.findViewById(R.id.btn_font_use);
            viewHolder.ivExample = (ImageView) convertView.findViewById(R.id.iv_font_example);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        initView(position, viewHolder);
        return convertView;
    }

    private void initView(int position, final ViewHolder viewHolder) {
        final Font font = getItem(position);

        if (font != Font.本地字体 && font != Font.默认字体) {
            try {
                viewHolder.ivExample.setVisibility(View.VISIBLE);
                viewHolder.ivExample.setImageBitmap(BitmapFactory.decodeStream(mFontsActivity.getAssets().open("font_img/" + font.toString() + ".png")));
            } catch (IOException e) {
                e.printStackTrace();
                viewHolder.ivExample.setVisibility(View.GONE);
            }
        } else {
            viewHolder.ivExample.setVisibility(View.GONE);
        }

        viewHolder.tvFontName.setText(font.toString());
        viewHolder.tvFontName.setTextColor(mFontsActivity.getResources().getColor(R.color.textPrimary));
        File fontFile = new File(APPCONST.FONT_BOOK_DIR + font.toString() + ".ttf");
        if (font == Font.本地字体) {
            if (setting.getFont() == Font.本地字体) {
                viewHolder.tvFontName.setText(setting.getLocalFontName());
                viewHolder.btnFontUse.setText(getContext().getString(R.string.font_change));
                viewHolder.btnFontUse.setButtonColor(mFontsActivity.getResources().getColor(R.color.toast_blue));
            } else {
                viewHolder.btnFontUse.setText(getContext().getString(R.string.font_select));
                viewHolder.btnFontUse.setButtonColor(mFontsActivity.getResources().getColor(R.color.sys_blue_littler));
            }
            viewHolder.btnFontUse.setEnabled(true);
            viewHolder.btnFontUse.setOnClickListener(v -> {
                /*ArrayList<File> localFontFiles = getLocalFontList();
                if (localFontFiles == null || localFontFiles.size() == 0) {
                    DialogCreator.createTipDialog(getContext(), getContext().getString(R.string.font_select_tip));
                    return;
                }
                final CharSequence[] fontNames = new CharSequence[localFontFiles.size()];
                int checkedItem = 0;
                for (int i = 0; i < fontNames.length; i++) {
                    fontNames[i] = localFontFiles.get(i).getName();
                    if (font.fileName.equals(fontNames[i])) {
                        checkedItem = i;
                    }
                }
                MyAlertDialog dialog = new MyAlertDialog.Builder(getContext())
                        .setTitle(getContext().getString(R.string.font_select))
                        .setCancelable(true)
                        .setSingleChoiceItems(fontNames, checkedItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                font.fileName = (String) fontNames[which];
                                setting.setFont(font);
                                setting.setLocalFontName(font.fileName);
                                SysManager.saveSetting(setting);
                                notifyDataSetChanged();
                                Intent intent = new Intent();
                                intent.putExtra(APPCONST.FONT, font);
                                ((Activity) getContext()).setResult(Activity.RESULT_OK, intent);
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();*/
                ToastUtils.showInfo("请选择一个ttf格式的字体文件");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                mFontsActivity.startActivityForResult(intent, APPCONST.SELECT_FILE_CODE);
            });
            return;
        }
        if (font != Font.默认字体 && !fontFile.exists()) {
            viewHolder.btnFontUse.setEnabled(true);
            viewHolder.btnFontUse.setButtonColor(mFontsActivity.getResources().getColor(R.color.sys_blue_littler));
            viewHolder.btnFontUse.setText(getContext().getString(R.string.font_download));
            viewHolder.btnFontUse.setOnClickListener(v -> {
                viewHolder.btnFontUse.setEnabled(false);
                addDownloadFont(font, viewHolder);
            });
        } else if (setting.getFont() == font) {
            viewHolder.btnFontUse.setText(getContext().getString(R.string.font_using));
            viewHolder.btnFontUse.setEnabled(false);
            viewHolder.btnFontUse.setButtonColor(mFontsActivity.getResources().getColor(R.color.sys_word_very_little));
        } else {
            viewHolder.btnFontUse.setText(getContext().getString(R.string.font_use));
            viewHolder.btnFontUse.setEnabled(true);
            viewHolder.btnFontUse.setButtonColor(mFontsActivity.getResources().getColor(R.color.toast_blue));
            viewHolder.btnFontUse.setOnClickListener(v -> {
                setting.setFont(font);
                SysManager.saveSetting(setting);
                notifyDataSetChanged();
                Intent intent = new Intent();
                intent.putExtra(APPCONST.FONT, font);
                ((Activity) getContext()).setResult(Activity.RESULT_OK, intent);
            });
        }
    }

    private void addDownloadFont(final Font font, final ViewHolder viewHolder) {
        if (!NetworkUtils.isNetWorkAvailable()) {
            ToastUtils.showWarring("无网络连接!");
            mHandler.sendMessage(mHandler.obtainMessage(3));
            return;
        }
        final String[] url = {URLCONST.FONT_DOWNLOAD_URL + font.toString() + ".ttf"};
        viewHolder.btnFontUse.setText("获取连接...");
        LanZousApi.getUrl(font.downloadPath, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                String downloadUrl = (String) o;
                if (downloadUrl != null) {
                    url[0] = downloadUrl;
                }
                downloadFont(url[0], font, viewHolder);
            }

            @Override
            public void onError(Exception e) {
                downloadFont(url[0], font, viewHolder);
            }
        });
    }

    private void downloadFont(final String url, final Font font, final ViewHolder viewHolder) {
        App.getApplication().newThread(() -> {
            HttpURLConnection con = null;
            InputStream is = null;
            FileOutputStream fos = null;
            File fontFile = null;
            try {
                URL webUrl = new URL(url);
                mHandler.sendMessage(mHandler.obtainMessage(4, viewHolder));
                con = (HttpURLConnection) webUrl.openConnection();
                is = con.getInputStream();
                int fileLength = con.getContentLength();
                String filePath = APPCONST.FONT_BOOK_DIR + font.toString() + ".ttf.temp";
                fontFile = FileUtils.getFile(filePath);
                fos = new FileOutputStream(fontFile);
                byte[] tem = new byte[1024];
                int len = 0;
                int alreadyLen = 0;
                while ((len = is.read(tem)) != -1) {
                    fos.write(tem, 0, len);
                    alreadyLen += len;
                    mHandler.sendMessage(mHandler.obtainMessage(1, alreadyLen, fileLength, viewHolder));
                }
                fos.flush();
                if (fileLength == fontFile.length()) {
                    String newPath = filePath.replace(".temp", "");
                    File newFile = new File(newPath);
                    if (fontFile.renameTo(newFile)) {
                        mHandler.sendMessage(mHandler.obtainMessage(2, font));
                    } else {
                        ToastUtils.showError(font.toString() + "字体下载失败！(Error：fontFile.renameTo(newFile))");
                        fontFile.delete();
                        mHandler.sendMessage(mHandler.obtainMessage(3));
                    }
                } else {
                    ToastUtils.showError(font.toString() + "字体下载失败！(Error：fileLength == fontFile.length())");
                    fontFile.delete();
                    mHandler.sendMessage(mHandler.obtainMessage(3));
                }
            } catch (IOException e) {
                ToastUtils.showError(font.toString() + "字体下载失败！\n" + e.getLocalizedMessage());
                if (fontFile != null) {
                    fontFile.delete();
                }
                mHandler.sendMessage(mHandler.obtainMessage(3));
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
                IOUtils.close(is, fos);
            }
        });
    }

    private void updateDownloadPro(int alreadyLen, int fileLen, ViewHolder viewHolder) {
        int process = alreadyLen * 100 / fileLen;
        viewHolder.btnFontUse.setProgress(process);
        viewHolder.btnFontUse.setText(process + "%");
        viewHolder.btnFontUse.setEnabled(false);
    }

    private ArrayList<File> getLocalFontList() {
        File fontDir = new File(APPCONST.FONT_BOOK_DIR);
        if (!fontDir.exists()) {
            return null;
        }
        ArrayList<File> localFontFiles = new ArrayList<>();
        File[] fontFiles = fontDir.listFiles();
        fontFilesFor:
        for (File fontFile : fontFiles) {
            for (Font font : mFontList) {
                if (font == Font.本地字体) {
                    continue;
                }
                if ((font.toString() + ".ttf").equals(fontFile.getName())) {
                    continue fontFilesFor;
                }
            }
            if (fontFile.getName().endsWith(".ttf")) {
                localFontFiles.add(fontFile);
            }
        }
        return localFontFiles;
    }

    public void saveLocalFontName(String fontName) {
        setting.setFont(Font.本地字体);
        setting.setLocalFontName(fontName);
        SysManager.saveSetting(setting);
        notifyDataSetChanged();
        Intent intent = new Intent();
        intent.putExtra(APPCONST.FONT, Font.本地字体);
        ((AppCompatActivity) getContext()).setResult(AppCompatActivity.RESULT_OK, intent);
    }

    class ViewHolder {
        ImageView ivExample;
        TextView tvFontName;
        ProgressButton btnFontUse;
    }

}
