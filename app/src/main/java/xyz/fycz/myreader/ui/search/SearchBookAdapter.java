package xyz.fycz.myreader.ui.search;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.callback.ResultCallback;
import xyz.fycz.myreader.crawler.BookInfoCrawler;
import xyz.fycz.myreader.crawler.ReadCrawler;
import xyz.fycz.myreader.crawler.ReadCrawlerUtil;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.enums.BookSource;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.webapi.CommonApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by zhao on 2017/7/26.
 */

public class SearchBookAdapter extends ArrayAdapter<SearchBookBean> {

    private int mResourceId;

    private ConcurrentMultiValueMap<SearchBookBean, Book> mBooks;

    private ArrayList<SearchBookBean> mTempBooks = new ArrayList<>();

    private Handler mHandle = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            switch (message.what) {
                case 1:
                    ViewHolder holder = (ViewHolder) message.obj;
                    int pos = message.arg1;
                    SearchBookAdapter.this.initOtherInfo(pos, holder);
                    break;
            }
            return false;
        }
    });

    public SearchBookAdapter(Context context, int resourceId, ArrayList<SearchBookBean> datas,
                             ConcurrentMultiValueMap<SearchBookBean, Book> mBooks){
        super(context, resourceId, datas);
        mResourceId = resourceId;
        this.mBooks = mBooks;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(mResourceId,null);
            viewHolder.ivBookImg =  convertView.findViewById(R.id.iv_book_img);
            viewHolder.tvBookName =  convertView.findViewById(R.id.tv_book_name);
            viewHolder.tvAuthor =  convertView.findViewById(R.id.tv_book_author);
            viewHolder.tvDesc =  convertView.findViewById(R.id.tv_book_desc);
            viewHolder.tvType = convertView.findViewById(R.id.tv_book_type);
            viewHolder.tvSource = convertView.findViewById(R.id.tv_book_source);
            viewHolder.tvNewestChapter = convertView.findViewById(R.id.tv_book_newest_chapter);
            //viewHolder.tvNewestChapter =  convertView.findViewById(R.id.tv_book_newest_chapter);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        initView(position,viewHolder);
        return convertView;
    }

    private void initView(final int position, final ViewHolder viewHolder){
        List<Book> aBooks = mBooks.getValues(getItem(position));
        int bookCount = aBooks.size();
        final Book book = aBooks.get(0);
        SearchBookBean ssb = new SearchBookBean(book.getName(), book.getAuthor());

        //判断是否已经加载，防止多次加载
        for (SearchBookBean temp : mTempBooks){//已加载
            if (ssb.equals(temp)){
                viewHolder.tvSource.setText("书源:" + BookSource.fromString(book.getSource()).text
                        + " 共" + bookCount + "个源");
                return;
            }
        }

        if (StringHelper.isEmpty(book.getImgUrl())){
            book.setImgUrl("");
        }
        viewHolder.tvBookName.setText(book.getName());
        viewHolder.tvNewestChapter.setText("最新章节:" + book.getNewestChapterTitle());
        viewHolder.tvAuthor.setText(book.getAuthor());
        viewHolder.tvSource.setText("书源:" + BookSource.fromString(book.getSource()).text
                                    + " 共" + bookCount + "个源");
        viewHolder.tvDesc.setText("");
        viewHolder.tvType.setText("");
        ReadCrawler rc = ReadCrawlerUtil.getReadCrawler(book.getSource());
        if (rc instanceof BookInfoCrawler && StringHelper.isEmpty(book.getImgUrl())){
            BookInfoCrawler bic = (BookInfoCrawler) rc;
            CommonApi.getBookInfo(book, bic, new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    mHandle.sendMessage(mHandle.obtainMessage(1,position,0,viewHolder));
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }else {
            initOtherInfo(position, viewHolder);
        }
        //viewHolder.tvNewestChapter.setText(book.getNewestChapterTitle());
        //添加已经加载的书籍
        mTempBooks.add(ssb);
    }
    private void initOtherInfo(final int position, final ViewHolder holder){
        Book book = mBooks.getValue(getItem(position), 0);
        //图片
        Glide.with(getContext())
                .load(book.getImgUrl())
//                .override(DipPxUtil.dip2px(getContext(), 80), DipPxUtil.dip2px(getContext(), 150))
                .error(R.mipmap.no_image)
                .placeholder(R.mipmap.no_image)
                //设置圆角
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(8)))
                .into(holder.ivBookImg);
        //简介
        holder.tvDesc.setText("简介:" + book.getDesc());
        holder.tvType.setText(book.getType());
    }

    class ViewHolder{
        ImageView ivBookImg;
        TextView tvBookName;
        TextView tvDesc;
        TextView tvAuthor;
        TextView tvType;
        TextView tvSource;
        TextView tvNewestChapter;
        //TextView tvNewestChapter;
    }

}
