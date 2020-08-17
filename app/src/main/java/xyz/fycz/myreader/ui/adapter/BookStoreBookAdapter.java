package xyz.fycz.myreader.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.webapi.crawler.BiQuGeReadCrawler;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.CommonApi;


import java.util.List;


public class BookStoreBookAdapter extends RecyclerView.Adapter<BookStoreBookAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private View view;
    private List<Book> mDatas;

    private Context mContext;
    private RecyclerView rvContent;


    private OnItemClickListener onItemClickListener;


    private Handler mHandle = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            switch (message.what) {
                case 1:
                    ViewHolder holder = (ViewHolder) message.obj;
                    int pos = message.arg1;
                    BookStoreBookAdapter.this.initOtherInfo(pos, holder);
                    break;
            }

            return false;

        }
    });


   public BookStoreBookAdapter(Context context, List<Book> datas) {
        mInflater = LayoutInflater.from(context);
        mDatas = datas;
        mContext = context;
   }

    class ViewHolder extends RecyclerView.ViewHolder {
       TextView tvBookName;
       TextView tvBookAuthor;
       TextView tvBookDesc;
       TextView tvBookNewestChapter;
       TextView tvBookSource;
       ImageView ivBookImg;
       ViewHolder() {
           super(view);
       }


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (rvContent == null) rvContent = (RecyclerView) parent;
        view = mInflater.inflate(R.layout.listview_book_store_book_item, parent, false);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.tvBookName = view.findViewById(R.id.tv_book_name);
        viewHolder.tvBookAuthor = view.findViewById(R.id.tv_book_author);
        viewHolder.tvBookDesc = view.findViewById(R.id.tv_book_desc);
        viewHolder.ivBookImg = view.findViewById(R.id.iv_book_img);
        viewHolder.tvBookNewestChapter = view.findViewById(R.id.tv_book_newest_chapter);
        viewHolder.tvBookSource = view.findViewById(R.id.tv_book_source);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        initView(position, holder);
        if (onItemClickListener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onClick(position, view);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private void initView(final int position, final ViewHolder holder) {
       Book book = mDatas.get(position);
       holder.tvBookName.setText(book.getName());
       holder.tvBookAuthor.setText(book.getAuthor());
       holder.tvBookNewestChapter.setText("");
       holder.tvBookSource.setText("书源：" + BookSource.fromString(book.getSource()).text);
       holder.tvBookDesc.setText("");
       if (StringHelper.isEmpty(book.getImgUrl())){
           //获取小说详情
           CommonApi.getBookInfo(book, new BiQuGeReadCrawler(), new ResultCallback() {
               @Override
               public void onFinish(Object o, int code) {
                   mDatas.set(position,(Book) o);
                   //防止列表快速滑动时出现书的信息加载混乱的问题
                   if (holder.tvBookName.getTag() == null || (int) holder.tvBookName.getTag() == position) {
                       mHandle.sendMessage(mHandle.obtainMessage(1,position,0,holder));
                   }
               }
               @Override
               public void onError(Exception e) {
               }
           });
       }else{
           initOtherInfo(position,holder);
       }
    }

    private void initOtherInfo(final int position, final ViewHolder holder){
        Book book = mDatas.get(position);
        //图片
        Glide.with(mContext)
                .load(book.getImgUrl())
//                .override(DipPxUtil.dip2px(getContext(), 80), DipPxUtil.dip2px(getContext(), 150))
                .error(R.mipmap.no_image)
                .placeholder(R.mipmap.no_image)
                //设置圆角
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(8)))
                .into(holder.ivBookImg);
        //简介
        holder.tvBookDesc.setText("简介:" + book.getDesc());
        holder.tvBookNewestChapter.setText("最新章节:" + book.getNewestChapterTitle());
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onClick(int pos, View view);
    }



}
