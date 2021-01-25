package xyz.fycz.myreader.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.ActivityCatalogBinding;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.ui.adapter.TabFragmentPageAdapter;
import xyz.fycz.myreader.ui.fragment.BookMarkFragment;
import xyz.fycz.myreader.ui.fragment.CatalogFragment;

/**
 * 书籍目录activity
 */
public class CatalogActivity extends BaseActivity {


    private ActivityCatalogBinding binding;
    private SearchView searchView;

    private Book mBook;

    private TabFragmentPageAdapter tabAdapter;

    /*******************Public**********************************/

    public Book getmBook() {
        return mBook;
    }

    @Override
    protected void bindView() {
        binding = ActivityCatalogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    /*********************Initialization****************************/


    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mBook = (Book) getIntent().getSerializableExtra(APPCONST.BOOK);
    }


    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        setStatusBarColor(R.color.colorPrimary, true);
        tabAdapter = new TabFragmentPageAdapter(getSupportFragmentManager());
        tabAdapter.addFragment(new CatalogFragment(), "目录");
        tabAdapter.addFragment(new BookMarkFragment(), "书签");
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        binding.catalogVp.setAdapter(tabAdapter);
        binding.catalogVp.setOffscreenPageLimit(2);
        binding.catalogTab.setupWithViewPager(binding.catalogVp);
    }


    /*************************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_search, menu);
        MenuItem search = menu.findItem(R.id.action_search);
        searchView = (SearchView) search.getActionView();
        searchView.setMaxWidth(getResources().getDisplayMetrics().widthPixels);
        searchView.onActionViewCollapsed();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                switch (binding.catalogVp.getCurrentItem()){
                    case 0:
                        ((CatalogFragment) tabAdapter.getItem(0)).getmCatalogPresent().startSearch(newText);
                        break;
                    case 1:
                        ((BookMarkFragment) tabAdapter.getItem(1)).getmBookMarkPresenter().startSearch(newText);
                        break;
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
