package xyz.fycz.myreader.ui.presenter;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BasePresenter;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.enums.Font;
import xyz.fycz.myreader.ui.activity.FontsActivity;
import xyz.fycz.myreader.ui.adapter.FontsAdapter;
import xyz.fycz.myreader.util.IOUtils;
import xyz.fycz.myreader.util.TextHelper;
import xyz.fycz.myreader.util.utils.FileUtils;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by zhao on 2017/8/7.
 */

public class FontsPresenter implements BasePresenter {

    private FontsActivity mFontsActivity;
    private ArrayList<Font> mFonts;
    private FontsAdapter mFontsAdapter;


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    init();
                    break;
            }
        }
    };

    public FontsPresenter(FontsActivity fontsActivity) {
        mFontsActivity = fontsActivity;
    }

    @Override
    public void start() {
        mFontsActivity.getLlTitleBack().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFontsActivity.finish();
            }
        });
        mFontsActivity.getTvTitleText().setText(mFontsActivity.getString(R.string.font));
        init();
    }

    private void init() {
        initFonts();
        mFontsAdapter = new FontsAdapter(mFontsActivity, R.layout.listview_font_item, mFonts, mFontsActivity);
        mFontsActivity.getLvFonts().setAdapter(mFontsAdapter);
        mFontsActivity.getPbLoading().setVisibility(View.GONE);
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

    public void notifyChange(){
        mFontsAdapter.notifyDataSetChanged();
    }

    public void saveLocalFont(String path){
        File fontFile = new File(path);
        if (!fontFile.exists()){
            TextHelper.showText("未找到字体文件！");
            return;
        }
        String fontName = fontFile.getName();
        if (!fontName.endsWith(".ttf")){
            TextHelper.showText("字体更换失败，请选择ttf格式的字体文件！");
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
            TextHelper.showText("读取字体文件出错！\n" + e.getLocalizedMessage());
        }finally {
            IOUtils.close(fis, fos);
        }
    }
}
