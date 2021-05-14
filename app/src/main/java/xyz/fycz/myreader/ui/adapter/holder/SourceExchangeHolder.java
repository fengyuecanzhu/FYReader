package xyz.fycz.myreader.ui.adapter.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;

/**
 * @author fengyue
 * @date 2020/9/30 18:43
 */
public class SourceExchangeHolder extends ViewHolderImpl<Book> {
    TextView sourceTvTitle;
    TextView sourceTvChapter;
    ImageView sourceIv;
    @Override
    protected int getItemLayoutId() {
        return R.layout.item_change_source;
    }

    @Override
    public void initView() {
        sourceTvTitle = findById(R.id.tv_source_name);
        sourceTvChapter = findById(R.id.tv_lastChapter);
        sourceIv = findById(R.id.iv_checked);
    }

    @Override
    public void onBind(Book data, int pos) {
        sourceTvTitle.setText(BookSourceManager.getSourceNameByStr(data.getSource()));
        sourceTvChapter.setText(data.getNewestChapterTitle());
        if (Boolean.parseBoolean(data.getNewestChapterId()))
            sourceIv.setVisibility(View.VISIBLE);
        else
            sourceIv.setVisibility(View.GONE);
    }
}
