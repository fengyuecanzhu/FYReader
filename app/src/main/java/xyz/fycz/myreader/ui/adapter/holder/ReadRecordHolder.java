package xyz.fycz.myreader.ui.adapter.holder;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kongzue.dialogx.dialogs.BottomMenu;
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.greendao.entity.ReadRecord;
import xyz.fycz.myreader.util.help.RelativeDateHelp;
import xyz.fycz.myreader.util.utils.NetworkUtils;
import xyz.fycz.myreader.widget.CoverImageView;

/**
 * @author fengyue
 * @date 2021/6/1 19:31
 */
public class ReadRecordHolder extends ViewHolderImpl<ReadRecord> {
    private OnMenuItemClickListener onMenuItemClickListener;
    private CoverImageView ivBookImg;
    private TextView tvBookName;
    private TextView tvBookAuthor;
    private TextView tvRecord;
    private ImageView ivMore;
    private final String[] menu = new String[]{"移除此记录", "清空阅读时长(不会删除记录)"};

    public ReadRecordHolder(OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_read_record;
    }

    @Override
    public void initView() {
        ivBookImg = findById(R.id.iv_book_img);
        tvBookName = findById(R.id.tv_book_name);
        tvBookAuthor = findById(R.id.tv_book_author);
        tvRecord = findById(R.id.tv_book_read_record);
        ivMore = findById(R.id.iv_more);
    }

    @Override
    public void onBind(RecyclerView.ViewHolder holder, ReadRecord data, int pos) {
        if (!App.isDestroy((Activity) getContext())) {
            ivBookImg.load(data.getBookImg(), data.getBookName(), data.getBookAuthor());
        }
        tvBookName.setText(data.getBookName());
        tvBookAuthor.setText(data.getBookAuthor());
        String lastTime = data.getUpdateTime() == 0 ? "无记录"
                : RelativeDateHelp.format(data.getUpdateTime());
        tvRecord.setText(String.format("%s · %s", lastTime,
                RelativeDateHelp.formatDuring(data.getReadTime())));
        ivMore.setOnClickListener(v -> BottomMenu.show(menu)
                .setTitle(data.getBookName())
                .setOnMenuItemClickListener((dialog, text, which) -> {
                    onMenuItemClickListener.onClick(pos, which);
                    return false;
                })
                .setCancelButton(R.string.cancel));
    }

    public interface OnMenuItemClickListener{
        void onClick(int itemPos, int menuPos);
    }
}
