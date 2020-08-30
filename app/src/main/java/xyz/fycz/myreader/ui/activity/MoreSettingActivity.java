package xyz.fycz.myreader.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.BaseActivity2;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.creator.DialogCreator;
import xyz.fycz.myreader.creator.MultiChoiceDialog;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.util.ToastUtils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by newbiechen on 17-6-6.
 * 阅读界面的更多设置
 */

public class MoreSettingActivity extends BaseActivity2 {
    @BindView(R.id.more_setting_rl_volume)
    RelativeLayout mRlVolume;
    @BindView(R.id.more_setting_sc_volume)
    SwitchCompat mScVolume;
    @BindView(R.id.more_setting_rl_reset_screen)
    RelativeLayout mRlResetScreen;
    @BindView(R.id.more_setting_sc_reset_screen)
    Spinner mScResetScreen;
    @BindView(R.id.more_setting_rl_auto_refresh)
    RelativeLayout mRlAutoRefresh;
    @BindView(R.id.more_setting_sc_auto_refresh)
    SwitchCompat mScAutoRefresh;
    @BindView(R.id.more_setting_ll_close_refresh)
    LinearLayout mLlCloseRefresh;
    @BindView(R.id.more_setting_iv_match_chapter_tip)
    ImageView mIvMatchChapterTip;
    @BindView(R.id.more_setting_rl_match_chapter)
    RelativeLayout mRlMatchChapter;
    @BindView(R.id.more_setting_sc_match_chapter)
    SwitchCompat mScMatchChapter;
    @BindView(R.id.more_setting_rl_match_chapter_suitability)
    RelativeLayout mRlMatchChapterSuitability;
    @BindView(R.id.more_setting_sc_match_chapter_suitability)
    Spinner mScMatchChapterSuitability;
    @BindView(R.id.more_setting_rl_cathe_gap)
    RelativeLayout mRlCatheGap;
    @BindView(R.id.more_setting_sc_cathe_gap)
    Spinner mScCatheGap;
    @BindView(R.id.more_setting_rl_delete_cathe)
    RelativeLayout mRlDeleteCathe;
    @BindView(R.id.more_setting_ll_download_all)
    LinearLayout mLlDownloadAll;
    private Setting mSetting;
    private boolean isVolumeTurnPage;
    private int resetScreenTime;
    private boolean autoRefresh;
    private boolean isMatchChapter;
    private float matchChapterSuitability;
    private int catheCap;

    private ArrayList<Book> mBooks;
    int booksCount;
    CharSequence[] mBooksName;

    //选择禁用更新书籍对话框
    private AlertDialog mCloseRefreshDia;
    //选择一键缓存书籍对话框
    private AlertDialog mDownloadAllDia;

    @Override
    protected int getContentId() {
        return R.layout.activity_more_setting;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mSetting = SysManager.getSetting();
        isVolumeTurnPage = mSetting.isVolumeTurnPage();
        resetScreenTime = mSetting.getResetScreen();
        isMatchChapter = mSetting.isMatchChapter();
        matchChapterSuitability = mSetting.getMatchChapterSuitability();
        catheCap = mSetting.getCatheGap();
        autoRefresh = mSetting.isRefreshWhenStart();
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle("设置");
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        initSwitchStatus();
        if (isMatchChapter) {
            mRlMatchChapterSuitability.setVisibility(View.VISIBLE);
        } else {
            mRlMatchChapterSuitability.setVisibility(View.GONE);
        }
    }

    private void initSwitchStatus() {
        mScVolume.setChecked(isVolumeTurnPage);
        mScMatchChapter.setChecked(isMatchChapter);
        mScAutoRefresh.setChecked(autoRefresh);
    }

    @Override
    protected void initClick() {
        super.initClick();
        mRlVolume.setOnClickListener(
                (v) -> {
                    if (isVolumeTurnPage) {
                        isVolumeTurnPage = false;
                    } else {
                        isVolumeTurnPage = true;
                    }
                    mScVolume.setChecked(isVolumeTurnPage);
                    mSetting.setVolumeTurnPage(isVolumeTurnPage);
                    SysManager.saveSetting(mSetting);
                }
        );
        mRlAutoRefresh.setOnClickListener(
                (v) -> {
                    if (autoRefresh) {
                        autoRefresh = false;
                    } else {
                        autoRefresh = true;
                    }
                    mScAutoRefresh.setChecked(autoRefresh);
                    mSetting.setRefreshWhenStart(autoRefresh);
                    SysManager.saveSetting(mSetting);
                }
        );

        mLlCloseRefresh.setOnClickListener(v -> {
            MyApplication.runOnUiThread(() -> {
                if (mCloseRefreshDia != null){
                    mCloseRefreshDia.show();
                    return;
                }

                initmBooks();

                if (mBooks.size() == 0){
                    ToastUtils.showWarring("当前书架没有支持禁用更新的书籍！");
                    return;
                }

                boolean[] isCloseRefresh = new boolean[booksCount];
                int crBookCount = 0;

                for (int i = 0; i < booksCount; i++) {
                    Book book = mBooks.get(i);
                    isCloseRefresh[i] = book.getIsCloseUpdate();
                    if (isCloseRefresh[i]){
                        crBookCount++;
                    }
                }

                mCloseRefreshDia = new MultiChoiceDialog().create(this, "禁用更新的书籍",
                        mBooksName, isCloseRefresh, crBookCount, (dialog, which) -> {
                            BookService.getInstance().updateBooks(mBooks);
                        }, null, new DialogCreator.OnMultiDialogListener() {
                            @Override
                            public void onItemClick(DialogInterface dialog, int which, boolean isChecked) {
                                mBooks.get(which).setIsCloseUpdate(isChecked);
                            }

                            @Override
                            public void onSelectAll(boolean isSelectAll) {
                                for (Book book : mBooks){
                                    book.setIsCloseUpdate(isSelectAll);
                                }
                            }
                        });

            });
        });

        mRlMatchChapter.setOnClickListener(
                (v) -> {
                    if (isMatchChapter) {
                        isMatchChapter = false;
                        mRlMatchChapterSuitability.setVisibility(View.GONE);
                    } else {
                        isMatchChapter = true;
                        mRlMatchChapterSuitability.setVisibility(View.VISIBLE);
                    }
                    mScMatchChapter.setChecked(isMatchChapter);
                    mSetting.setMatchChapter(isMatchChapter);
                    SysManager.saveSetting(mSetting);
                }
        );


        mLlDownloadAll.setOnClickListener(v -> {
            MyApplication.runOnUiThread(() -> {
                if (mDownloadAllDia != null){
                    mDownloadAllDia.show();
                    return;
                }

                initmBooks();

                if (mBooks.size() == 0){
                    ToastUtils.showWarring("当前书架没有支持缓存的书籍！");
                    return;
                }

                int booksCount = mBooks.size();
                CharSequence[] mBooksName = new CharSequence[booksCount];
                boolean[] isDownloadAll = new boolean[booksCount];
                int daBookCount = 0;
                for (int i = 0; i < booksCount; i++) {
                    Book book = mBooks.get(i);
                    mBooksName[i] = book.getName();
                    isDownloadAll[i] = book.getIsDownLoadAll();
                    if (isDownloadAll[i]){
                        daBookCount++;
                    }
                }

                mDownloadAllDia = new MultiChoiceDialog().create(this, "一键缓存的书籍",
                        mBooksName, isDownloadAll, daBookCount, (dialog, which) -> {
                            BookService.getInstance().updateBooks(mBooks);
                        }, null, new DialogCreator.OnMultiDialogListener() {
                            @Override
                            public void onItemClick(DialogInterface dialog, int which, boolean isChecked) {
                                mBooks.get(which).setIsDownLoadAll(isChecked);
                            }

                            @Override
                            public void onSelectAll(boolean isSelectAll) {
                                for (Book book : mBooks){
                                    book.setIsDownLoadAll(isSelectAll);
                                }
                            }
                        });

            });
        });

        mIvMatchChapterTip.setOnClickListener(v -> DialogCreator.createTipDialog(this, "智能匹配", getString(R.string.match_chapter_tip)));

        mRlMatchChapterSuitability.setOnClickListener(v -> mScMatchChapterSuitability.performClick());
        mRlResetScreen.setOnClickListener(v -> mScResetScreen.performClick());
        mRlCatheGap.setOnClickListener(v -> mScCatheGap.performClick());

        mRlDeleteCathe.setOnClickListener(v -> {
            DialogCreator.createCommonDialog(this, "清除缓存", "确定要清除全部书籍缓存吗？",
                    true, (dialog, which) -> {
                        BookService.getInstance().deleteAllBookCathe();
                        ToastUtils.showSuccess("清除缓存成功！");
                    }, null);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSpinner();
    }

    private void initSpinner() {
        // initSwitchStatus() be called earlier than onCreate(), so setSelection() won't work
        ArrayAdapter<CharSequence> resetScreenAdapter = ArrayAdapter.createFromResource(this,
                R.array.reset_screen_time, android.R.layout.simple_spinner_item);
        resetScreenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mScResetScreen.setAdapter(resetScreenAdapter);

        int resetScreenSelection = 0;
        switch (resetScreenTime) {
            case 0:
                resetScreenSelection = 0;
                break;
            case 1:
                resetScreenSelection = 1;
                break;
            case 3:
                resetScreenSelection = 2;
                break;
            case 5:
                resetScreenSelection = 3;
                break;
        }
        mScResetScreen.setSelection(resetScreenSelection);
        mScResetScreen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        resetScreenTime = 0;
                        break;
                    case 1:
                        resetScreenTime = 1;
                        break;
                    case 2:
                        resetScreenTime = 3;
                        break;
                    case 3:
                        resetScreenTime = 5;
                        break;
                }
                mSetting.setResetScreen(resetScreenTime);
                SysManager.saveSetting(mSetting);
                Intent result = new Intent();
                result.putExtra(APPCONST.RESULT_RESET_SCREEN, resetScreenTime);
                setResult(AppCompatActivity.RESULT_OK, result);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        ArrayAdapter<CharSequence> matchSuiAdapter = ArrayAdapter.createFromResource(this,
                R.array.match_chapter_suitability, android.R.layout.simple_spinner_item);
        matchSuiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mScMatchChapterSuitability.setAdapter(matchSuiAdapter);

        if (matchChapterSuitability == 0.0) {
            matchChapterSuitability = 0.7f;
            mSetting.setMatchChapterSuitability(matchChapterSuitability);
            SysManager.saveSetting(mSetting);
        }
        int matchSuiSelection = (int) (matchChapterSuitability * 10 - 5);

        mScMatchChapterSuitability.setSelection(matchSuiSelection);

        mScMatchChapterSuitability.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                matchChapterSuitability = (position + 5) * 1f / 10f;
                mSetting.setMatchChapterSuitability(matchChapterSuitability);
                SysManager.saveSetting(mSetting);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        ArrayAdapter<CharSequence> catheGapAdapter = ArrayAdapter.createFromResource(this,
                R.array.cathe_chapter_gap, android.R.layout.simple_spinner_item);
        catheGapAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mScCatheGap.setAdapter(catheGapAdapter);

        if (catheCap == 0) {
            catheCap = 150;
            mSetting.setCatheGap(catheCap);
            SysManager.saveSetting(mSetting);
        }
        int catheGapSelection = catheCap / 50 - 1;

        mScCatheGap.setSelection(catheGapSelection);

        mScCatheGap.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                catheCap = (position + 1) * 50;
                mSetting.setCatheGap(catheCap);
                SysManager.saveSetting(mSetting);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initmBooks(){
        if (mBooks != null) {
            return;
        }
        mBooks = (ArrayList<Book>) BookService.getInstance().getAllBooks();

        Iterator<Book> mBooksIter = mBooks.iterator();
        while (mBooksIter.hasNext()){
            Book book = mBooksIter.next();
            if ("本地书籍".equals(book.getType())) {
                mBooksIter.remove();
            }
        }
        booksCount = mBooks.size();
        mBooksName = new CharSequence[booksCount];

        for (int i = 0; i < booksCount; i++) {
            Book book = mBooks.get(i);
            mBooksName[i] = book.getName();
        }
    }
}
