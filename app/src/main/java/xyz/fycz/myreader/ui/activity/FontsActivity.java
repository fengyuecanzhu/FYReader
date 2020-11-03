package xyz.fycz.myreader.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.enums.Font;
import xyz.fycz.myreader.ui.adapter.FontsAdapter;
import xyz.fycz.myreader.util.IOUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static xyz.fycz.myreader.util.UriFileUtil.getPath;

/**
 * @author fengyue
 * @date 2020/9/19 12:04
 */
public class FontsActivity extends BaseActivity {
    @BindView(R.id.lv_fonts)
    ListView lvFonts;
    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;
    private ArrayList<Font> mFonts;
    private FontsAdapter mFontsAdapter;


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    initWidget();
                    break;
            }
        }
    };
    @Override
    protected int getContentId() {
        return R.layout.activity_fonts;
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle("字体");
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        initFonts();
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        mFontsAdapter = new FontsAdapter(this, R.layout.listview_font_item, mFonts, this);
        lvFonts.setAdapter(mFontsAdapter);
        pbLoading.setVisibility(View.GONE);
    }

    private void initFonts() {
        mFonts = new ArrayList<>();
        mFonts.add(Font.默认字体);
        mFonts.add(Font.方正楷体);
        mFonts.add(Font.经典宋体);
        mFonts.add(Font.方正行楷);
        mFonts.add(Font.迷你隶书);
        mFonts.add(Font.方正黄草);
        mFonts.add(Font.方正硬笔行书);
        mFonts.add(Font.本地字体);
    }

    public void saveLocalFont(String path){
        File fontFile = new File(path);
        if (!fontFile.exists()){
            ToastUtils.showWarring("未找到字体文件！");
            return;
        }
        String fontName = fontFile.getName();
        if (!fontName.endsWith(".ttf")){
            ToastUtils.showError("字体更换失败，请选择ttf格式的字体文件！");
            return;
        }
        String dirPath = fontFile.getParentFile().getAbsolutePath() + "/";
        if (dirPath.equals(APPCONST.FONT_BOOK_DIR)){
            mFontsAdapter.saveLocalFontName(fontName);
            return;
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(fontFile);
            fos = new FileOutputStream(FileUtils.getFile(APPCONST.FONT_BOOK_DIR + fontName));
            byte[] bytes = new byte[1021];
            int len = -1;
            while ((len = fis.read(bytes)) != -1){
                fos.write(bytes, 0, len);
            }
            fos.flush();
            mFontsAdapter.saveLocalFontName(fontName);
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtils.showError("读取字体文件出错！\n" + e.getLocalizedMessage());
        }finally {
            IOUtils.close(fis, fos);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String path;
        if (resultCode == Activity.RESULT_OK && requestCode == APPCONST.SELECT_FILE_CODE) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())){//使用第三方应用打开
                path = uri.getPath();
            }else {
                path = getPath(this, uri);
            }
            saveLocalFont(path);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mFontsAdapter.notifyDataSetChanged();
    }
}
