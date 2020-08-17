package xyz.fycz.myreader.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.ChapterService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ChapterTitleAdapter extends ArrayAdapter<Chapter> {

    private int mResourceId;
    private Setting setting;
    private int curChapterPosition = -1;
    private List<Chapter> mChapters;
    private List<Chapter> mList;
    private Book mBook;

    public ChapterTitleAdapter(Context context, int resourceId, ArrayList<Chapter> datas, Book book) {
        super(context, resourceId, new ArrayList<>(datas));
        mResourceId = resourceId;
        setting = SysManager.getSetting();
        mChapters = datas;
        mList = datas;
        mBook = book;
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
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_chapter_title);
            viewHolder.vLine = (View) convertView.findViewById(R.id.v_line);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        initView(position, viewHolder);
        return convertView;
    }

    private void initView(int postion, final ViewHolder viewHolder) {
        final Chapter chapter = getItem(postion);
//        viewHolder.tvTitle.setText("【" + chapter.getTitle() + "】");
        viewHolder.tvTitle.setText(chapter.getTitle());
        if (ChapterService.isChapterCached(chapter.getBookId(), chapter.getTitle())) {
            viewHolder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.selector_category_load), null, null, null);
        } else {
            viewHolder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.selector_category_unload), null, null, null);
        }
        if (setting.isDayStyle()) {
            viewHolder.tvTitle.setTextColor(getContext().getResources().getColor(setting.getReadWordColor()));
        } else {
            viewHolder.tvTitle.setTextColor(getContext().getResources().getColor(R.color.sys_night_word));
            viewHolder.vLine.setBackground(getContext().getDrawable(R.color.sys_dialog_setting_line));
        }

        if (chapter.getNumber() == mBook.getHisttoryChapterNum()) {
            viewHolder.tvTitle.setTextColor(getContext().getResources().getColor(R.color.colorAccent));
        }

    }

    /**
     * 过滤器，实现搜索
     *
     * @return
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                List<Chapter> mFilterList = new ArrayList<>();
                if (charString.isEmpty()) {
                    //没有过滤的内容，则使用源数据
                    mFilterList = mChapters;
                } else {
                    for (Chapter chapter : mChapters) {
                        //这里根据需求，添加匹配规则
                        if (chapter.getTitle().contains(charString)) {
                            mFilterList.add(chapter);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilterList;
                return filterResults;
            }

            //把过滤后的值返回出来
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mList = (ArrayList<Chapter>) results.values;
                clear();
                addAll(mList);
                notifyDataSetChanged();
            }
        };
    }

    public List<Chapter> getmList() {
        return mList;
    }

    class ViewHolder {

        TextView tvTitle;
        View vLine;

    }

}
