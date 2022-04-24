/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.ui.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.BaseFragment;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.FragmentPrivateBookcaseBinding;
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

    private FragmentPrivateBookcaseBinding binding;

    private boolean openPrivate;
    private boolean openFingerprint;

    @Override
    protected View bindView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentPrivateBookcaseBinding.inflate(inflater, container, false);
        return binding.getRoot();
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
        binding.scPrivateBookcase.setChecked(openPrivate);
        binding.scFingerprint.setChecked(openFingerprint);
        if (openPrivate) binding.llContent.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initClick() {
        super.initClick();
        binding.rlPrivateBookcase.setOnClickListener(v -> {
            if (openPrivate) {
                DialogCreator.createCommonDialog(getContext(), "关闭私密书架",
                        "确定要关闭私密书架吗？\n注意：这将会删除私密书架中的全部书籍！",
                        true, (dialog, which) -> {
                            BookGroupService.getInstance().deletePrivateGroup();
                            SharedPreUtils.getInstance().putString("privatePwd", "");
                            binding.llContent.setVisibility(View.GONE);
                            openPrivate = !openPrivate;
                            openFingerprint = false;
                            SharedPreUtils.getInstance().putBoolean("openPrivate", openPrivate);
                            SharedPreUtils.getInstance().putBoolean("openFingerprint", openFingerprint);
                            binding.scPrivateBookcase.setChecked(openPrivate);
                            binding.scFingerprint.setChecked(openFingerprint);
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
                            binding.llContent.setVisibility(View.VISIBLE);
                            openPrivate = !openPrivate;
                            SharedPreUtils.getInstance().putBoolean("openPrivate", openPrivate);
                            binding.scPrivateBookcase.setChecked(openPrivate);
                        });
            }
        });

        binding.llHideBooks.setOnClickListener(v -> {
            App.runOnUiThread(() -> {
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

        binding.rlChangePwd.setOnClickListener(v -> {
            final String[] pwd = new String[1];
            MyAlertDialog.createInputDia(getContext(), getString(R.string.change_pwd),
                    "", "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    true, 12,
                    text -> pwd[0] = text,
                    (dialog, which) -> {
                        SharedPreUtils.getInstance().putString("privatePwd", CyptoUtils.encode(APPCONST.KEY, pwd[0]));
                        dialog.dismiss();
                        binding.llContent.setVisibility(View.VISIBLE);
                    });
        });

        binding.rlFingerprint.setOnClickListener(
                (v) -> {
                    if (openFingerprint) {
                        openFingerprint = false;
                        binding.scFingerprint.setChecked(openFingerprint);
                        SharedPreUtils.getInstance().putBoolean("openFingerprint", openFingerprint);
                    } else {
                        if (!FingerprintUtils.supportFingerprint(getActivity())) return;
                        FingerprintDialog fd = new FingerprintDialog((AppCompatActivity) getActivity(),false, needGoTo -> {
                            openFingerprint = true;
                            binding.scFingerprint.setChecked(openFingerprint);
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
        binding.scPrivateBookcase.setChecked(openPrivate);
        binding.scFingerprint.setChecked(openFingerprint);
        if (openPrivate) {
            binding.llContent.setVisibility(View.VISIBLE);
        }else {
            binding.llContent.setVisibility(View.GONE);
        }
    }

}
