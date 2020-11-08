package xyz.fycz.myreader.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.entity.bookstore.BookType;


import java.util.List;

public class BookStoreBookTypeAdapter extends RecyclerView.Adapter<BookStoreBookTypeAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private View view;
    private List<BookType> mDatas;

    private Context mContext;
    private RecyclerView rvContent;

    private OnItemClickListener onItemClickListener;

    private int selectPos = 0;


    public BookStoreBookTypeAdapter(Context context, List<BookType> datas) {
        mInflater = LayoutInflater.from(context);
        mDatas = datas;
        mContext = context;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTypeName;

        ViewHolder() {
            super(view);
        }


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (rvContent == null) rvContent = (RecyclerView) parent;
        view = mInflater.inflate(R.layout.listview_book_type_item, parent, false);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.tvTypeName = view.findViewById(R.id.tv_type_name);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        initView(position, holder);
        if (position == selectPos) {
            holder.itemView.setBackgroundResource(R.color.colorForeground);
            holder.tvTypeName.setTextColor(mContext.getResources().getColor(R.color.textPrimary));
            holder.tvTypeName.setTextSize(15);
            holder.tvTypeName.getPaint().setFakeBoldText(true);
        } else {
            holder.itemView.setBackgroundResource(R.color.colorBackground);
            holder.tvTypeName.setTextColor(mContext.getResources().getColor(R.color.textSecondary));
            holder.tvTypeName.getPaint().setFakeBoldText(false);
            holder.tvTypeName.setTextSize(14);
        }

        if (onItemClickListener != null) {

            holder.itemView.setOnClickListener(view -> {

                onItemClickListener.onClick(position, view);
                selectPos = position;
                BookStoreBookTypeAdapter.this.notifyDataSetChanged();

            });

        }


    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private void initView(final int position, final ViewHolder holder) {
        BookType bookType = mDatas.get(position);
        holder.tvTypeName.setText(bookType.getTypeName());
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {

        void onClick(int pos, View view);

    }


}
