package xyz.fycz.myreader.ui.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.databinding.FragmentFileCategoryBinding;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.ui.adapter.FileSystemAdapter;
import xyz.fycz.myreader.util.FileStack;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.widget.DividerItemDecoration;

/**
 * @author fengyue
 * @date 2020/8/12 20:02
 */

public class FileCategoryFragment extends BaseFileFragment {
    private static final String TAG = "FileCategoryFragment";

    private FragmentFileCategoryBinding binding;

    private FileStack mFileStack;

    @Override
    protected View bindView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentFileCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void initWidget(Bundle savedInstanceState) {
        super.initWidget(savedInstanceState);
        mFileStack = new FileStack();
        setUpAdapter();
    }

    private void setUpAdapter(){
        mAdapter = new FileSystemAdapter();
        binding.fileCategoryRvContent.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.fileCategoryRvContent.addItemDecoration(new DividerItemDecoration(getContext()));
        binding.fileCategoryRvContent.setAdapter(mAdapter);
    }

    @Override
    protected void initClick() {
        super.initClick();
        mAdapter.setOnItemClickListener(
                (view, pos) -> {
                    File file = mAdapter.getItem(pos);
                    if (file.isDirectory()){
                        //保存当前信息。
                        FileStack.FileSnapshot snapshot = new FileStack.FileSnapshot();
                        snapshot.filePath = binding.fileCategoryTvPath.getText().toString();
                        snapshot.files = new ArrayList<>(mAdapter.getItems());
                        snapshot.scrollOffset = binding.fileCategoryRvContent.computeVerticalScrollOffset();
                        mFileStack.push(snapshot);
                        //切换下一个文件
                        toggleFileTree(file);
                    }
                    else {

                        //如果是已加载的文件，则点击事件无效。
                        String path = mAdapter.getItem(pos).getAbsolutePath();
                        if (BookService.getInstance().findBookByPath(path) != null){
                            return;
                        }
                        //点击选中
                        mAdapter.setCheckedItem(pos);
                        //反馈
                        if (mListener != null){
                            mListener.onItemCheckedChange(mAdapter.getItemIsChecked(pos));
                        }
                    }
                }
        );

        binding.fileCategoryTvBackLast.setOnClickListener(v -> backLast());
    }

    public boolean backLast(){
        FileStack.FileSnapshot snapshot = mFileStack.pop();
        int oldScrollOffset = binding.fileCategoryRvContent.computeHorizontalScrollOffset();
        if (snapshot == null) return false;
        binding.fileCategoryTvPath.setText(snapshot.filePath);
        mAdapter.refreshItems(snapshot.files);
        binding.fileCategoryRvContent.scrollBy(0,snapshot.scrollOffset - oldScrollOffset);
        //反馈
        if (mListener != null){
            mListener.onCategoryChanged();
        }
        return true;
    }

    @Override
    protected void processLogic() {
        super.processLogic();
        File root = Environment.getExternalStorageDirectory();
        toggleFileTree(root);
    }

    private void toggleFileTree(File file){
        //路径名
        binding.fileCategoryTvPath.setText(getString(R.string.file_path,file.getPath()));
        //获取数据
        File[] files = file.listFiles(new SimpleFileFilter());
        //转换成List
        List<File> rootFiles = Arrays.asList(files);
        //排序
        Collections.sort(rootFiles, new FileComparator());
        //加入
        mAdapter.refreshItems(rootFiles);
        //反馈
        if (mListener != null){
            mListener.onCategoryChanged();
        }
    }

    @Override
    public int getFileCount(){
        int count = 0;
        Set<Map.Entry<File, Boolean>> entrys = mAdapter.getCheckMap().entrySet();
        for (Map.Entry<File, Boolean> entry:entrys){
            if (!entry.getKey().isDirectory()){
                ++count;
            }
        }
        return count;
    }

    public static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File o1, File o2){
            if (o1.isDirectory() && o2.isFile()) {
                return -1;
            }
            if (o2.isDirectory() && o1.isFile()) {
                return 1;
            }
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    public static class SimpleFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            if (pathname.getName().startsWith(".")) {
                return false;
            }
            //文件夹内部数量为0
            if (pathname.isDirectory() && (pathname.list() == null || pathname.list().length == 0)) {
                return false;
            }

            //文件内容为空,或者不以txt为开头
            return pathname.isDirectory() ||
                    (pathname.length() != 0
                            && (pathname.getName().toLowerCase().endsWith(FileUtils.SUFFIX_TXT)
                            || pathname.getName().toLowerCase().endsWith(FileUtils.SUFFIX_EPUB)));
        }
    }
}
