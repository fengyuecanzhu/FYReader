package xyz.fycz.myreader.ui.adapter.holder;

import android.widget.TextView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.ChapterService;

/**
 * @author fengyue
 * @date 2020/8/17 15:07
 */
public class CatalogHolder extends ViewHolderImpl<Chapter> {
    private TextView tvTitle;
    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_chapter_title_item;
    }

    @Override
    public void initView() {
        tvTitle = findById(R.id.tv_chapter_title);
    }

    @Override
    public void onBind(Chapter data, int pos) {
        if (ChapterService.isChapterCached(data.getBookId(), data.getTitle())) {
            tvTitle.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.selector_category_load), null, null, null);
        } else {
            tvTitle.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.selector_category_unload), null, null, null);
        }
        tvTitle.setText(data.getTitle());
    }
}
