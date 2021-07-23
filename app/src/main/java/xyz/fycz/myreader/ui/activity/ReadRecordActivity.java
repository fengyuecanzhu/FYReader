package xyz.fycz.myreader.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kongzue.dialogx.dialogs.BottomMenu;
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener;
import com.kongzue.dialogx.interfaces.OnMenuItemSelectListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.disposables.Disposable;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.base.BitIntentDataManager;
import xyz.fycz.myreader.base.adapter.BaseListAdapter;
import xyz.fycz.myreader.base.adapter.IViewHolder;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.ActivityReadRecordBinding;
import xyz.fycz.myreader.greendao.DbManager;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.ReadRecord;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.greendao.service.ReadRecordService;
import xyz.fycz.myreader.ui.adapter.holder.ReadRecordHolder;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.SourceExchangeDialog;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.help.RelativeDateHelp;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.widget.DividerItemDecoration;

/**
 * @author fengyue
 * @date 2021/6/1 19:07
 */
public class ReadRecordActivity extends BaseActivity {
    private ActivityReadRecordBinding binding;
    private List<ReadRecord> records;
    private long allTime;
    private BaseListAdapter<ReadRecord> mAdapter;
    private SourceExchangeDialog mSourceDia;
    private ReadRecordService recordService;

    @Override
    protected void bindView() {
        binding = ActivityReadRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        getSupportActionBar().setTitle(getString(R.string.read_record));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        recordService = ReadRecordService.getInstance();
        recordService.getAllRecordsByTime().compose(RxUtils::toSimpleSingle)
                .subscribe(new MySingleObserver<List<ReadRecord>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }
                    @Override
                    public void onSuccess(@NotNull List<ReadRecord> readRecords) {
                        records = readRecords;
                        initRecords();
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.showError("数据加载失败\n" + e.getLocalizedMessage());
                    }
                });
    }

    private void initAllTime() {
        allTime = 0;
        for (ReadRecord record : records) {
            allTime += record.getReadTime();
        }
        binding.tvAllRecord.setText(getString(R.string.all_read_time,
                RelativeDateHelp.formatDuring(allTime)));
    }

    private void initRecords() {
        initAllTime();
        mAdapter = new BaseListAdapter<ReadRecord>() {
            @Override
            protected IViewHolder<ReadRecord> createViewHolder(int viewType) {
                return new ReadRecordHolder((itemPos, menuPos) -> {
                    ReadRecord record = records.get(itemPos);
                    switch (menuPos) {
                        case 0:
                            DbManager.getDaoSession().getReadRecordDao().delete(record);
                            records.remove(itemPos);
                            mAdapter.removeItem(record);
                            ToastUtils.showSuccess(String.format("《%s》阅读记录移除成功", record.getBookName()));
                            break;
                        case 1:
                            record.setReadTime(0);
                            record.setUpdateTime(0);
                            DbManager.getDaoSession().getReadRecordDao().insertOrReplace(record);
                            mAdapter.notifyDataSetChanged();
                            ToastUtils.showSuccess(String.format("《%s》阅读时长已清空", record.getBookName()));
                            break;
                    }
                    initAllTime();
                });
            }
        };

        //设置布局
        binding.rvRecords.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecords.setAdapter(mAdapter);

        mAdapter.refreshItems(records);
        mAdapter.setOnItemClickListener((view, pos) -> {
            ReadRecord record = records.get(pos);
            Book book = BookService.getInstance().findBookByAuthorAndName(record.getBookName(), record.getBookAuthor());
            if (book == null) {
                DialogCreator.createCommonDialog(this, "搜索书籍",
                        "当前书籍未加入书架，是否重新搜索？", false, (dialog, which) -> {
                            Book newBook = new Book();
                            newBook.setName(record.getBookName());
                            newBook.setAuthor(record.getBookAuthor());
                            newBook.setImgUrl(record.getBookImg());
                            mSourceDia = new SourceExchangeDialog(this, newBook);
                            mSourceDia.setOnSourceChangeListener((bean, pos1) -> {
                                Intent intent = new Intent(this, BookDetailedActivity.class);
                                BitIntentDataManager.getInstance().putData(intent, mSourceDia.getaBooks());
                                intent.putExtra(APPCONST.SOURCE_INDEX, pos1);
                                ReadRecordActivity.this.startActivity(intent);
                                mSourceDia.dismiss();
                            });
                            mSourceDia.show();
                        }, null);
            } else {
                Intent intent = new Intent(this, BookDetailedActivity.class);
                BitIntentDataManager.getInstance().putData(intent, book);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void initClick() {
        super.initClick();
        binding.ivMore.setOnClickListener(v -> {
            String[] menu = new String[]{"移除所有记录", "清空阅读时长(不会删除记录)"};
            BottomMenu.show(menu)
                    .setTitle("总阅读记录")
                    .setOnMenuItemClickListener((dialog, text, which) -> {
                        if (records == null) {
                            ToastUtils.showWarring("数据未完成加载，无法进行操作！");
                            return false;
                        }
                        switch (which) {
                            case 0:
                                recordService.removeAll().compose(RxUtils::toSimpleSingle)
                                        .subscribe(new MySingleObserver<Boolean>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {
                                                addDisposable(d);
                                            }
                                            @Override
                                            public void onSuccess(@NotNull Boolean aBoolean) {
                                                ToastUtils.showSuccess("阅读记录已全部移除");
                                                records.clear();
                                                mAdapter.refreshItems(records);
                                                initAllTime();
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                super.onError(e);
                                                ToastUtils.showError(e.getLocalizedMessage());
                                            }
                                        });
                                break;
                            case 1:
                                recordService.removeAllTime(records).compose(RxUtils::toSimpleSingle)
                                        .subscribe(new MySingleObserver<Boolean>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {
                                                addDisposable(d);
                                            }
                                            @Override
                                            public void onSuccess(@NotNull Boolean aBoolean) {
                                                ToastUtils.showSuccess("阅读时长已全部清空");
                                                mAdapter.notifyDataSetChanged();
                                                initAllTime();
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                super.onError(e);
                                                ToastUtils.showError(e.getLocalizedMessage());
                                            }
                                        });
                                break;
                        }
                        return false;
                    })
                    .setCancelButton(R.string.cancel);
        });
    }
}
