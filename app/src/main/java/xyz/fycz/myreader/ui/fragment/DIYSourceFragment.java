package xyz.fycz.myreader.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseFragment;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.FragmentImportSourceBinding;
import xyz.fycz.myreader.greendao.DbManager;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.greendao.gen.BookSourceDao;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.ui.activity.BookSourceActivity;
import xyz.fycz.myreader.ui.activity.SourceEditActivity;
import xyz.fycz.myreader.ui.adapter.BookSourceAdapter;
import xyz.fycz.myreader.ui.adapter.helper.ItemTouchCallback;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.util.ShareUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.ClipBoardUtil;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.widget.DividerItemDecoration;
import xyz.fycz.myreader.widget.swipemenu.SwipeMenuLayout;

import static android.app.Activity.RESULT_OK;
import static android.text.TextUtils.isEmpty;
import static xyz.fycz.myreader.util.UriFileUtil.getPath;

/**
 * @author fengyue
 * @date 2021/2/10 12:05
 */
public class DIYSourceFragment extends BaseFragment {
    private FragmentImportSourceBinding binding;

    private final BookSourceActivity sourceActivity;
    private List<BookSource> mBookSources;
    private BookSourceAdapter mAdapter;
    private boolean isSearch;
    private PopupMenu featuresMenu;
    private ItemTouchCallback itemTouchCallback;

    public DIYSourceFragment() {
        sourceActivity = (BookSourceActivity) getActivity();
    }

    public DIYSourceFragment(BookSourceActivity sourceActivity) {
        this.sourceActivity = sourceActivity;
    }

    @Override
    protected View bindView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentImportSourceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mAdapter = new BookSourceAdapter(getActivity(), new BookSourceAdapter.OnSwipeListener() {
            @Override
            public void onDel(int which, BookSource bean) {
                mBookSources.remove(bean);
                mAdapter.removeItem(which);
            }

            @Override
            public void onTop(int which, BookSource bean) {
                if (which > 0 && which < mBookSources.size()) {
                    mBookSources.remove(bean);
                    mBookSources.add(0, bean);
                    mAdapter.toTop(which, bean);
                }
            }
        });
        refreshSources();
    }

    @Override
    protected void initWidget(Bundle savedInstanceState) {
        super.initWidget(savedInstanceState);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(mAdapter);
        //设置分割线
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        //设置拖拽
        itemTouchCallback = new ItemTouchCallback();
        itemTouchCallback.setOnItemTouchListener(mAdapter.getItemTouchListener());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
        itemTouchCallback.setDragEnable(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initClick() {
        super.initClick();
        binding.recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                SwipeMenuLayout viewCache = SwipeMenuLayout.getViewCache();
                if (null != viewCache) {
                    viewCache.smoothClose();
                }
            }
            return false;
        });

        binding.tvNewSource.setOnClickListener(v ->
                startActivityForResult(new Intent(getContext(), SourceEditActivity.class),
                        APPCONST.REQUEST_EDIT_BOOK_SOURCE));
        binding.tvImportSource.setOnClickListener(v -> {
            MyAlertDialog.build(getContext())
                    .setTitle("导入书源")
                    .setItems(R.array.import_rule, (dialog, which) -> {
                        if (which == 0) {
                            String text = ClipBoardUtil.paste(getContext());
                            if (!isEmpty(text)) {
                                importDataS(text);
                            } else {
                                ToastUtils.showError("剪切板内容为空，导入失败");
                            }
                        } else if (which == 1) {
                            ToastUtils.showInfo("请选择书源JSON文件");
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("application/json");
                            startActivityForResult(intent, APPCONST.REQUEST_IMPORT_BOOK_SOURCE);
                        } else {
                            String[] url = new String[1];
                            MyAlertDialog.createInputDia(getContext(), "网络导入",
                                    "请输入网址", "", true, 200,
                                    text -> url[0] = text,
                                    (dialog1, which1) -> importDataS(url[0]));

                        }
                    }).show();
        });

        binding.tvSourceTip.setOnClickListener(v -> {
            MyAlertDialog.showTipDialogWithLink(getContext(), "书源说明",R.string.DIY_source_tip);
        });

        binding.ivGroup.setOnClickListener(this::showSourceGroupMenu);

        binding.ivMenu.setOnClickListener(v -> {
            if (featuresMenu == null) {
                initFeaturesMenu(v);
            }
            featuresMenu.show();
        });
    }

    public void importDataS(String text) {
        Observable<List<BookSource>> observable = BookSourceManager.importSource(text);
        if (observable != null) {
            observable.subscribe(new MyObserver<List<BookSource>>() {
                @Override
                public void onNext(List<BookSource> sources) {
                    int size = sources.size();
                    if (size > 0) {
                        refreshSources();
                        ToastUtils.showSuccess(String.format("成功导入%s个书源", size));
                    } else {
                        ToastUtils.showError("格式不对");
                    }
                }

                @Override
                public void onError(Throwable e) {
                    ToastUtils.showError("格式不对");
                }
            });
        } else {
            ToastUtils.showError("导入失败");
        }
    }

    /**
     * 刷新书源
     */
    private void refreshSources() {
        Single.create((SingleOnSubscribe<List<BookSource>>) emitter -> {
            List<BookSource> sourceBeanList;
            if (isSearch) {
                if (sourceActivity.getSearchView().getQuery().toString().equals("enabled")) {
                    sourceBeanList = DbManager.getDaoSession().getBookSourceDao().queryBuilder()
                            .where(BookSourceDao.Properties.SourceEName.isNull())
                            .where(BookSourceDao.Properties.Enable.eq(1))
                            .orderRaw(BookSourceManager.getBookSourceSort())
                            .orderAsc(BookSourceDao.Properties.OrderNum)
                            .list();
                } else {
                    String term = "%" + sourceActivity.getSearchView().getQuery() + "%";
                    sourceBeanList = DbManager.getDaoSession().getBookSourceDao().queryBuilder()
                            .where(BookSourceDao.Properties.SourceEName.isNull())
                            .whereOr(BookSourceDao.Properties.SourceName.like(term),
                                    BookSourceDao.Properties.SourceGroup.like(term),
                                    BookSourceDao.Properties.SourceUrl.like(term))
                            .orderRaw(BookSourceManager.getBookSourceSort())
                            .orderAsc(BookSourceDao.Properties.OrderNum)
                            .list();
                }
            } else {
                sourceBeanList = BookSourceManager.getAllNoLocalSource();
            }
            emitter.onSuccess(sourceBeanList);
        }).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<List<BookSource>>() {
            @Override
            public void onSuccess(@NonNull List<BookSource> sources) {
                mBookSources = sources;
                mAdapter.refreshItems(mBookSources);
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showError("数据加载失败\n" + e.getLocalizedMessage());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == APPCONST.REQUEST_IMPORT_BOOK_SOURCE) {
                String path = getPath(getContext(), data.getData());
                String json = FileUtils.readText(path);
                if (!isEmpty(json)) {
                    importDataS(json);
                } else {
                    ToastUtils.showError("文件读取失败");
                }
            } else if (requestCode == APPCONST.REQUEST_EDIT_BOOK_SOURCE) {
                refreshSources();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 显示书源分组菜单
     */
    public void showSourceGroupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(sourceActivity, view, Gravity.END);
        List<String> groupList = BookSourceManager.getNoLocalGroupList();
        popupMenu.getMenu().add(0, 0, 0, "所有书源");
        for (int i = 0; i < groupList.size(); i++) {
            popupMenu.getMenu().add(0, 0, i + 1, groupList.get(i));
        }
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getOrder() > 0) {
                sourceActivity.getSearchView().onActionViewExpanded();
                sourceActivity.getSearchView().clearFocus();
                sourceActivity.getSearchView().setQuery(menuItem.getTitle(), false);
            } else {
                sourceActivity.getSearchView().onActionViewCollapsed();
            }
            return true;
        });
        popupMenu.show();
    }

    private void initFeaturesMenu(View view) {
        featuresMenu = new PopupMenu(sourceActivity, view, Gravity.END);
        //获取菜单填充器
        MenuInflater inflater = featuresMenu.getMenuInflater();
        //填充菜单
        inflater.inflate(R.menu.menu_import_source, featuresMenu.getMenu());
        featuresMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                featuresMenu.getMenu().setGroupVisible(R.id.edit_mode, true);
                featuresMenu.getMenu().setGroupVisible(R.id.non_edit_mode, false);
                mAdapter.setmEditState(true);
                itemTouchCallback.setDragEnable(true);
            } else if (itemId == R.id.action_finish) {
                featuresMenu.getMenu().setGroupVisible(R.id.edit_mode, false);
                featuresMenu.getMenu().setGroupVisible(R.id.non_edit_mode, true);
                mAdapter.setmEditState(false);
                itemTouchCallback.setDragEnable(false);
            } else if (itemId == R.id.action_export) {
                exportSources(mBookSources);
            } else if (itemId == R.id.action_share) {
                shareSources(mBookSources);
            } else if (itemId == R.id.action_reverse) {
                reverseSources(mBookSources);
            } else if (itemId == R.id.action_delete) {
                deleteSources(mBookSources, true);
            } else if (itemId == R.id.action_select_all) {
                mAdapter.setCheckedAll(true);
            } else if (itemId == R.id.action_reverse_selected) {
                mAdapter.reverseChecked();
            } else if (itemId == R.id.action_export_selected) {
                List<BookSource> bookSources = mAdapter.getCheckedBookSources();
                exportSources(bookSources);
            } else if (itemId == R.id.action_share_selected) {
                List<BookSource> bookSources = mAdapter.getCheckedBookSources();
                shareSources(bookSources);
            } else if (itemId == R.id.action_reverse_enable_selected) {
                List<BookSource> bookSources = mAdapter.getCheckedBookSources();
                reverseSources(bookSources);
            } else if (itemId == R.id.action_delete_selected) {
                List<BookSource> bookSources = mAdapter.getCheckedBookSources();
                deleteSources(bookSources, false);
            }
            return true;
        });
    }


    private void exportSources(List<BookSource> sources) {
        if (sources == null || sources.size() == 0) {
            ToastUtils.showWarring("当前没有选择任何书源，无法导出！");
            return;
        }
        Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            emitter.onSuccess(FileUtils.writeText(GsonExtensionsKt.getGSON().toJson(sources),
                    FileUtils.getFile(APPCONST.FILE_DIR + "BookSources.json")));
        }).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<Boolean>() {
            @Override
            public void onSuccess(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    DialogCreator.createTipDialog(sourceActivity,
                            "书源导出成功，导出位置：" + APPCONST.FILE_DIR + "BookSources.json");
                } else {
                    ToastUtils.showError("书源导出失败");
                }
            }
        });
    }

    private void shareSources(List<BookSource> bookSources) {
        if (bookSources == null || bookSources.size() == 0) {
            ToastUtils.showWarring("当前没有选择任何书源，无法分享！");
            return;
        }
        Single.create((SingleOnSubscribe<File>) emitter -> {
            File share = FileUtils.getFile(APPCONST.SHARE_FILE_DIR + "ShareBookSources.json");
            if (FileUtils.writeText(GsonExtensionsKt.getGSON().toJson(bookSources), share)){
                emitter.onSuccess(share);
            }else {
                emitter.onError(new Exception("书源文件写出失败"));
            }
        }).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<File>() {
            @Override
            public void onSuccess(@NonNull File share) {
                ShareUtils.share(sourceActivity, share, "书源分享", "text/plain");
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showError("书源分享失败\n" + e.getLocalizedMessage());
            }
        });
    }

    private void reverseSources(List<BookSource> mBookSources) {
        for (BookSource source : mBookSources) {
            source.setEnable(!source.getEnable());
        }
        DbManager.getDaoSession().getBookSourceDao().insertOrReplaceInTx(mBookSources);
        mAdapter.notifyDataSetChanged();
    }

    private void deleteSources(List<BookSource> mBookSources, boolean isDisabled) {
        if (mBookSources == null || mBookSources.size() == 0) {
            ToastUtils.showWarring("当前没有选择任何书源，无法删除！");
            return;
        }
        String title, msg, successTip;
        if (isDisabled) {
            title = "删除禁用书源";
            msg = "确定要删除所有禁用书源吗？";
            successTip = "禁用书源删除成功";
        } else {
            title = "删除选中书源";
            msg = "确定要删除所有选中书源吗？";
            successTip = "选中书源删除成功";
        }
        DialogCreator.createCommonDialog(sourceActivity, title,
                msg, true,
                (dialog, which) -> {
                    List<BookSource> sources = new ArrayList<>();
                    if (isDisabled) {
                        for (BookSource source : mBookSources) {
                            if (!source.getEnable()) {
                                sources.add(source);
                            }
                        }
                    } else {
                        sources.addAll(mBookSources);
                    }
                    BookSourceManager.removeBookSources(sources);
                    mBookSources.removeAll(sources);
                    mAdapter.removeItems(sources);
                    ToastUtils.showSuccess(successTip);
                }, null);
    }

    public void startSearch(String newText) {
        isSearch = !TextUtils.isEmpty(newText);
        refreshSources();
    }

}
