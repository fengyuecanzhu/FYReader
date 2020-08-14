package xyz.fycz.myreader.ui.filesys;

import android.media.MediaScannerConnection;
import android.os.Bundle;

import android.os.Environment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.util.media.MediaStoreHelper;
import xyz.fycz.myreader.widget.DividerItemDecoration;
import xyz.fycz.myreader.widget.RefreshLayout;


/**
 * @author fengyue
 * @date 2020/8/12 20:02
 * 本地书籍
 */

public class LocalBookFragment extends BaseFileFragment {
    @BindView(R.id.refresh_layout)
    RefreshLayout mRlRefresh;
    @BindView(R.id.local_book_rv_content)
    RecyclerView mRvContent;

    @Override
    protected int getContentId() {
        return R.layout.fragment_local_book;
    }

    @Override
    protected void initWidget(Bundle savedInstanceState) {
        super.initWidget(savedInstanceState);
        setUpAdapter();
    }

    private void setUpAdapter() {
        mAdapter = new FileSystemAdapter();
        mRvContent.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvContent.addItemDecoration(new DividerItemDecoration(getContext()));
        mRvContent.setAdapter(mAdapter);
    }

    @Override
    protected void initClick() {
        super.initClick();
        mAdapter.setOnItemClickListener(
                (view, pos) -> {
                    //如果是已加载的文件，则点击事件无效。
                    String path = mAdapter.getItem(pos).getAbsolutePath();
                    if (BookService.getInstance().findBookByPath(path) != null) {
                        return;
                    }

                    //点击选中
                    mAdapter.setCheckedItem(pos);

                    //反馈
                    if (mListener != null) {
                        mListener.onItemCheckedChange(mAdapter.getItemIsChecked(pos));
                    }
                }
        );
    }

    @Override
    protected void processLogic() {
        super.processLogic();
        //更新媒体库
        MediaScannerConnection.scanFile(getContext(), new String[]{Environment
                .getExternalStorageDirectory().getAbsolutePath()}, new String[]{"text/plain"}, null);
        MediaStoreHelper.getAllBookFile(getActivity(),
                (files) -> {
                    if (files.isEmpty()) {
                        mRlRefresh.showEmpty();
                    } else {
                        mAdapter.refreshItems(files);
                        mRlRefresh.showFinish();
                        //反馈
                        if (mListener != null) {
                            mListener.onCategoryChanged();
                        }
                    }
                });
    }
}
