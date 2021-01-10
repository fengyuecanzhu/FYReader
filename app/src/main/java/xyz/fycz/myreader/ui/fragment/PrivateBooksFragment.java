package xyz.fycz.myreader.ui.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.List;

import butterknife.BindView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.base.BaseFragment;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.service.BookGroupService;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.ui.dialog.DialogCreator;
import xyz.fycz.myreader.ui.dialog.FingerprintDialog;
import xyz.fycz.myreader.ui.dialog.MultiChoiceDialog;
import xyz.fycz.myreader.ui.dialog.MyAlertDialog;
import xyz.fycz.myreader.util.CyptoUtils;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.FingerprintUtils;

/**
 * @author fengyue
 * @date 2021/1/9 13:53
 */
public class PrivateBooksFragment extends BaseFragment {
    @BindView(R.id.rl_private_bookcase)
    RelativeLayout mRlPrivateBookcase;
    @BindView(R.id.sc_private_bookcase)
    SwitchCompat mScPrivateBookcase;
    @BindView(R.id.ll_content)
    LinearLayout mLlContent;
    @BindView(R.id.ll_hide_books)
    LinearLayout mLlHideBooks;
    @BindView(R.id.rl_change_pwd)
    RelativeLayout mRlChangePwd;
    @BindView(R.id.rl_fingerprint)
    RelativeLayout mRlFingerprint;
    @BindView(R.id.sc_fingerprint)
    SwitchCompat mScFingerprint;

    private boolean openPrivate;
    private boolean openFingerprint;

    @Override
    protected int getContentId() {
        return R.layout.fragment_private_bookcase;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        openPrivate = SharedPreUtils.getInstance().getBoolean("openPrivate");
        openFingerprint = SharedPreUtils.getInstance().getBoolean("openFingerprint");
    }

    @Override
    protected void initWidget(Bundle savedInstanceState) {
        super.initWidget(savedInstanceState);
        mScPrivateBookcase.setChecked(openPrivate);
        mScFingerprint.setChecked(openFingerprint);
        if (openPrivate) mLlContent.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initClick() {
        super.initClick();
        mRlPrivateBookcase.setOnClickListener(v -> {
            if (openPrivate) {
                DialogCreator.createCommonDialog(getContext(), "关闭私密书架",
                        "确定要关闭私密书架吗？\n注意：这将会删除私密书架中的全部书籍！",
                        true, (dialog, which) -> {
                            BookGroupService.getInstance().deletePrivateGroup();
                            SharedPreUtils.getInstance().putString("privatePwd", "");
                            mLlContent.setVisibility(View.GONE);
                            openPrivate = !openPrivate;
                            openFingerprint = false;
                            SharedPreUtils.getInstance().putBoolean("openPrivate", openPrivate);
                            SharedPreUtils.getInstance().putBoolean("openFingerprint", openFingerprint);
                            mScPrivateBookcase.setChecked(openPrivate);
                            mScFingerprint.setChecked(openFingerprint);
                        }, null);
            } else {
                final String[] pwd = new String[1];
                MyAlertDialog.createInputDia(getContext(), getString(R.string.set_private_pwd),
                        "", "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                        true, 12,
                        text -> pwd[0] = text,
                        (dialog, which) -> {
                            BookGroupService.getInstance().createPrivateGroup();
                            SharedPreUtils.getInstance().putString("privatePwd", CyptoUtils.encode(APPCONST.KEY, pwd[0]));
                            dialog.dismiss();
                            mLlContent.setVisibility(View.VISIBLE);
                            openPrivate = !openPrivate;
                            SharedPreUtils.getInstance().putBoolean("openPrivate", openPrivate);
                            mScPrivateBookcase.setChecked(openPrivate);
                        });
            }
        });

        mLlHideBooks.setOnClickListener(v -> {
            MyApplication.runOnUiThread(() -> {
                String privateGroupId = SharedPreUtils.getInstance().getString("privateGroupId");
                List<Book> mBooks = BookService.getInstance().getAllBooks();

                int booksCount = mBooks.size();

                if (booksCount == 0) {
                    ToastUtils.showWarring("当前书架没有任何书籍！");
                    return;
                }

                CharSequence[] mBooksName = new CharSequence[booksCount];

                for (int i = 0; i < booksCount; i++) {
                    Book book = mBooks.get(i);
                    mBooksName[i] = !"本地书籍".equals(book.getType()) ? book.getName() : book.getName() + "[本地]";
                }

                boolean[] isPrivate = new boolean[booksCount];
                int crBookCount = 0;

                for (int i = 0; i < booksCount; i++) {
                    Book book = mBooks.get(i);
                    isPrivate[i] = privateGroupId.equals(book.getGroupId());
                    if (isPrivate[i]) {
                        crBookCount++;
                    }
                }

                new MultiChoiceDialog().create(getContext(), "隐藏的书籍",
                        mBooksName, isPrivate, crBookCount, (dialog, which) -> {
                            BookService.getInstance().updateBooks(mBooks);
                        }, null, new DialogCreator.OnMultiDialogListener() {
                            @Override
                            public void onItemClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    mBooks.get(which).setGroupId(privateGroupId);
                                }else {
                                    mBooks.get(which).setGroupId("");
                                }
                            }

                            @Override
                            public void onSelectAll(boolean isSelectAll) {
                                for (Book book : mBooks) {
                                    book.setGroupId(privateGroupId);
                                }
                            }
                        }).show();

            });
        });

        mRlChangePwd.setOnClickListener(v -> {
            final String[] pwd = new String[1];
            MyAlertDialog.createInputDia(getContext(), getString(R.string.change_pwd),
                    "", "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    true, 12,
                    text -> pwd[0] = text,
                    (dialog, which) -> {
                        SharedPreUtils.getInstance().putString("privatePwd", CyptoUtils.encode(APPCONST.KEY, pwd[0]));
                        dialog.dismiss();
                        mLlContent.setVisibility(View.VISIBLE);
                    });
        });

        mRlFingerprint.setOnClickListener(
                (v) -> {
                    if (openFingerprint) {
                        openFingerprint = false;
                        mScFingerprint.setChecked(openFingerprint);
                        SharedPreUtils.getInstance().putBoolean("openFingerprint", openFingerprint);
                    } else {
                        if (!FingerprintUtils.supportFingerprint(getActivity())) return;
                        FingerprintDialog fd = new FingerprintDialog((AppCompatActivity) getActivity(),false, needGoTo -> {
                            openFingerprint = true;
                            mScFingerprint.setChecked(openFingerprint);
                            SharedPreUtils.getInstance().putBoolean("openFingerprint", openFingerprint);
                        });
                        fd.setCancelable(false);
                        fd.setCipher(FingerprintUtils.initCipher());
                        fd.show(getFragmentManager(), "fingerprint");
                    }
                }
        );
    }

    @Override
    protected void processLogic() {
        super.processLogic();
        if (!SharedPreUtils.getInstance().getBoolean("isReadPrivateTip")){
            DialogCreator.createTipDialog(getContext(), "关于私密书架", getString(R.string.private_bookcase_tip));
            SharedPreUtils.getInstance().putBoolean("isReadPrivateTip", true);
        }
    }

    public void init(){
        openPrivate = SharedPreUtils.getInstance().getBoolean("openPrivate");
        openFingerprint = SharedPreUtils.getInstance().getBoolean("openFingerprint");
        mScPrivateBookcase.setChecked(openPrivate);
        mScFingerprint.setChecked(openFingerprint);
        if (openPrivate) {
            mLlContent.setVisibility(View.VISIBLE);
        }else {
            mLlContent.setVisibility(View.GONE);
        }
    }

}
