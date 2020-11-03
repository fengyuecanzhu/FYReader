package xyz.fycz.myreader.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import com.google.android.material.tabs.TabLayout;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseActivity;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.ui.adapter.TabFragmentPageAdapter;
import xyz.fycz.myreader.ui.fragment.BookMarkFragment;
import xyz.fycz.myreader.ui.fragment.CatalogFragment;

/**
 * 书籍目录activity
 */
public class CatalogActivity extends BaseActivity {


    @BindView(R.id.catalog_tab)
    TabLayout catalogTab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.catalog_vp)
    ViewPager viewPager;
    private SearchView searchView;

    private Book mBook;

    private TabFragmentPageAdapter tabAdapter;

    /*******************Public**********************************/

    public Book getmBook() {
        return mBook;
    }

    /*********************Initialization****************************/


    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mBook = (Book) getIntent().getSerializableExtra(APPCONST.BOOK);
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_catalog;
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
        viewPager.setAdapter(tabAdapter);
        viewPager.setOffscreenPageLimit(2);
        catalogTab.setupWithViewPager(viewPager);
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
                switch (viewPager.getCurrentItem()){
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
