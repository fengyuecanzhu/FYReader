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
import xyz.fycz.myreader.greendao.entity.BookMark;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.ChapterService;

import java.util.ArrayList;
import java.util.List;


public class BookMarkAdapter extends ArrayAdapter<BookMark> {

    private int mResourceId;
    private Setting setting;
    private List<BookMark> mBookMarks;

    public BookMarkAdapter(Context context, int resourceId, ArrayList<BookMark> datas){
        super(context, resourceId, datas);
        mResourceId = resourceId;
        setting = SysManager.getSetting();
        mBookMarks = new ArrayList<>(datas);
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
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(mResourceId,null);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_chapter_title);
            viewHolder.vLine = (View) convertView.findViewById(R.id.v_line);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        initView(position,viewHolder);
        return convertView;
    }

    private void initView(int postion,final ViewHolder viewHolder){
        final BookMark bookMark = getItem(postion);
        viewHolder.tvTitle.setText(bookMark.getTitle() + "[" + (bookMark.getBookMarkReadPosition() + 1) + "]");
        if (ChapterService.isChapterCached(bookMark.getBookId(), bookMark.getTitle())){
            viewHolder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.selector_category_load),null,null,null);
        } else {
            viewHolder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.selector_category_unload),null,null,null);
        }
        viewHolder.tvTitle.setTextColor(getContext().getColor(R.color.textSecondary));
        /*if (!setting.isDayStyle()) {
            viewHolder.tvTitle.setTextColor(getContext().getResources().getColor(R.color.sys_night_word));
            viewHolder.vLine.setBackground(getContext().getDrawable(R.color.sys_dialog_setting_line));
        }else {
            viewHolder.tvTitle.setTextColor(getContext().getColor(R.color.title_black));
        }*/

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
                List<BookMark> mFilterList = new ArrayList<>();
                if (charString.isEmpty()) {
                    //没有过滤的内容，则使用源数据
                    mFilterList = mBookMarks;
                } else {
                    for (BookMark bookMark : mBookMarks) {
                        //这里根据需求，添加匹配规则
                        if (bookMark.getTitle().contains(charString)) {
                            mFilterList.add(bookMark);
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
                clear();
                addAll((ArrayList<BookMark>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    class ViewHolder{

        TextView tvTitle;
        View vLine;
    }

}
