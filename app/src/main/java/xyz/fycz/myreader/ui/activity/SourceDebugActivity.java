package xyz.fycz.myreader.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.databinding.ActivitySourceDebugBinding;
import xyz.fycz.myreader.entity.sourcedebug.DebugBook;
import xyz.fycz.myreader.entity.sourcedebug.DebugChapter;
import xyz.fycz.myreader.entity.sourcedebug.DebugEntity;
import xyz.fycz.myreader.entity.sourcedebug.ListResult;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.ui.dialog.LoadingDialog;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.webapi.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.webapi.crawler.base.BookInfoCrawler;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.widget.codeview.Language;

/**
 * @author fengyue
 * @date 2021/2/12 18:55
 */
public class SourceDebugActivity extends BaseActivity {
    private ActivitySourceDebugBinding binding;

    private DebugEntity debugEntity;
    private ReadCrawler rc;
    private Disposable disposable;
    private LoadingDialog loadingDialog;

    @Override
    protected void bindView() {
        binding = ActivitySourceDebugBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        switch (debugEntity.getDebugMode()) {
            case DebugEntity.SEARCH:
            default:
                getSupportActionBar().setTitle(R.string.debug_search);
                break;
            case DebugEntity.INFO:
                getSupportActionBar().setTitle(R.string.debug_info);
                break;
            case DebugEntity.TOC:
                getSupportActionBar().setTitle(R.string.debug_toc);
                break;
            case DebugEntity.CONTENT:
                getSupportActionBar().setTitle(R.string.debug_content);
                break;
        }
        initDebugEntity();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        debugEntity = getIntent().getParcelableExtra("debugEntity");
        rc = ReadCrawlerUtil.getReadCrawler(debugEntity.getBookSource(), true);
        loadingDialog = new LoadingDialog(this, "正在请求", () -> {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        });
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        binding.tvTip.setText(getString(R.string.debug_desc,
                debugEntity.getBookSource().getSourceName(),
                debugEntity.getBookSource().getSourceUrl(),
                debugEntity.getUrl()));
        binding.rvParseResult.setLanguage(Language.JSON);
        binding.rvSourceCode.setLanguage(Language.HTML);
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    binding.rvSourceCode.setVisibility(View.GONE);
                    binding.rvParseResult.setVisibility(View.VISIBLE);
                } else if (tab.getPosition() == 1) {
                    binding.rvSourceCode.setVisibility(View.VISIBLE);
                    binding.rvParseResult.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_debug, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_re_request) {
            initDebugEntity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initDebugEntity() {
        loadingDialog.show();
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            try {
                String url = debugEntity.getUrl();
                if (debugEntity.getDebugMode() == DebugEntity.SEARCH && rc.isPost()) {
                    String[] urlInfo = url.split(",");
                    url = NetworkUtils.getAbsoluteURL(rc.getNameSpace(), urlInfo[0]);
                    String body = urlInfo[1];
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    RequestBody requestBody = RequestBody.create(mediaType, body);
                    debugEntity.setHtml(OkHttpUtils.getHtml(url, requestBody, rc.getCharset()));
                } else {
                    debugEntity.setHtml(OkHttpUtils.getHtml(NetworkUtils.getAbsoluteURL(rc.getNameSpace(), url), rc.getCharset()));
                }
                ListResult listResult = new ListResult();
                switch (debugEntity.getDebugMode()) {
                    case DebugEntity.SEARCH:
                    default:
                        List<DebugBook> debugBooks = book2DebugBook(rc.getBooksFromSearchHtml(debugEntity.getHtml()).values());
                        listResult.set信息(String.format("解析完毕，共%s本书籍", debugBooks.size()));
                        listResult.set结果(debugBooks);
                        debugEntity.setParseResult(GsonExtensionsKt.getGSON()
                                .toJson(listResult));
                        break;
                    case DebugEntity.INFO:
                        debugEntity.setParseResult(GsonExtensionsKt.getGSON()
                                .toJson(book2DebugBook(((BookInfoCrawler) rc).getBookInfo(debugEntity.getHtml(), new Book()))));
                        break;
                    case DebugEntity.TOC:
                        List<DebugChapter> debugChapters = chapter2DebugChapter(rc.getChaptersFromHtml(debugEntity.getHtml()));
                        listResult.set信息(String.format("解析完毕，共%s个章节", debugChapters.size()));
                        listResult.set结果(debugChapters);
                        debugEntity.setParseResult(GsonExtensionsKt.getGSON()
                                .toJson(listResult));
                        break;
                    case DebugEntity.CONTENT:
                        debugEntity.setParseResult(rc.getContentFormHtml(debugEntity.getHtml()));
                        break;
                }
                emitter.onNext(true);
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }
            emitter.onComplete();
        }).compose(RxUtils::toSimpleSingle).subscribe(new MyObserver<Boolean>() {

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(@NonNull Boolean flag) {
                binding.rvParseResult.setCode(debugEntity.getParseResult()).apply();
                binding.rvSourceCode.setCode(debugEntity.getHtml()).apply();
                loadingDialog.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                binding.rvParseResult.setCode(String.format("{\n\b\b\b\b\"result\": \"error\", \n\b\b\b\b\"msg\": \"%s\"\n}"
                        , e.getLocalizedMessage().replace("\"", "\\\""))).apply();
                binding.rvSourceCode.setCode(debugEntity.getHtml()).apply();
                loadingDialog.dismiss();
            }

        });
    }

    private List<DebugBook> book2DebugBook(List<Book> books) {
        List<DebugBook> debugBooks = new ArrayList<>();
        for (Book book : books) {
            debugBooks.add(book2DebugBook(book));
        }
        return debugBooks;
    }

    private DebugBook book2DebugBook(Book book) {
        DebugBook debugBook = new DebugBook();
        /*debugBook.setName(book.getName());
        debugBook.setAuthor(book.getAuthor());
        debugBook.setType(book.getType());
        debugBook.setDesc(book.getDesc());
        debugBook.setWordCount(book.getWordCount());
        debugBook.setStatus(book.getStatus());
        debugBook.setLastChapter(book.getNewestChapterTitle());
        debugBook.setUpdateTime(book.getUpdateDate());
        debugBook.setImgUrl(book.getImgUrl());
        debugBook.setTocUrl(book.getChapterUrl());
        debugBook.setInfoUrl(book.getInfoUrl());*/
        debugBook.set书名(book.getName());
        debugBook.set作者(book.getAuthor());
        debugBook.set分类(book.getType());
        debugBook.set简介(book.getDesc());
        debugBook.set字数(book.getWordCount());
        debugBook.set连载状态(book.getStatus());
        debugBook.set最新章节(book.getNewestChapterTitle());
        debugBook.set更新时间(book.getUpdateDate());
        debugBook.set封面链接(book.getImgUrl());
        debugBook.set目录链接(book.getChapterUrl());
        debugBook.set详情链接(book.getInfoUrl());
        return debugBook;
    }

    private List<DebugChapter> chapter2DebugChapter(List<Chapter> chapters) {
        List<DebugChapter> debugChapters = new ArrayList<>();
        for (Chapter chapter : chapters) {
            DebugChapter debugChapter = new DebugChapter();
            /*debugChapter.setTitle(chapter.getTitle());
            debugChapter.setUrl(chapter.getUrl());*/
            debugChapter.set章节名称(chapter.getTitle());
            debugChapter.set章节链接(chapter.getUrl());
            debugChapters.add(debugChapter);
        }
        return debugChapters;
    }
}
