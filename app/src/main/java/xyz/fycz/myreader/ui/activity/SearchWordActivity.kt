package xyz.fycz.myreader.ui.activity

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import xyz.fycz.myreader.R
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.base.BaseActivity
import xyz.fycz.myreader.base.BitIntentDataManager
import xyz.fycz.myreader.base.adapter.BaseListAdapter
import xyz.fycz.myreader.base.adapter.IViewHolder
import xyz.fycz.myreader.base.adapter2.onClick
import xyz.fycz.myreader.common.APPCONST
import xyz.fycz.myreader.databinding.ActivitySearchWordBinding
import xyz.fycz.myreader.greendao.DbManager
import xyz.fycz.myreader.greendao.entity.search.SearchWord1
import xyz.fycz.myreader.greendao.entity.Book
import xyz.fycz.myreader.greendao.entity.Chapter
import xyz.fycz.myreader.greendao.entity.search.SearchWord
import xyz.fycz.myreader.model.SearchWordEngine
import xyz.fycz.myreader.ui.adapter.holder.SearchWord1Holder
import xyz.fycz.myreader.widget.page.PageLoader

/**
 * @author fengyue
 * @date 2021/12/5 19:57
 */
class SearchWordActivity : BaseActivity() {

    private lateinit var binding: ActivitySearchWordBinding
    private lateinit var book: Book
    private lateinit var chapters: List<Chapter>
    private lateinit var pageLoader: PageLoader
    private lateinit var searchWordEngine: SearchWordEngine
    private lateinit var adapter: BaseListAdapter<SearchWord1>
    private var searchWord: SearchWord? = null
    private var keyword: String? = null

    override fun bindView() {
        binding = ActivitySearchWordBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setUpToolbar(toolbar: Toolbar?) {
        super.setUpToolbar(toolbar)
        setStatusBarColor(R.color.colorPrimary, true)
        setUpToolbarTitle()
    }

    private fun setUpToolbarTitle() {
        if (searchWord == null) {
            supportActionBar?.title = getString(R.string.search_word)
        } else {
            var sum = 0
            searchWord?.searchWords?.forEach {
                sum += it.searchWord2List.size
            }
            supportActionBar?.title =
                getString(R.string.search_word) + "(共" + searchWord?.searchWords?.size +
                        "个章节，" + sum + "条结果)"
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        BitIntentDataManager.getInstance().putData(APPCONST.BOOK_KEY, book)
        BitIntentDataManager.getInstance().putData(APPCONST.CHAPTERS_KEY, chapters)
        BitIntentDataManager.getInstance().putData(APPCONST.PAGE_LOADER_KEY, pageLoader)
        super.onSaveInstanceState(outState)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && binding.etSearchKey.text?.isEmpty() == true) {
            App.getHandler().postDelayed({
                binding.etSearchKey.requestFocus()
                val imm =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(
                    binding.etSearchKey,
                    InputMethodManager.SHOW_IMPLICIT
                )
            }, 400)
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        book = BitIntentDataManager.getInstance().getData(APPCONST.BOOK_KEY) as Book
        chapters =
            BitIntentDataManager.getInstance().getData(APPCONST.CHAPTERS_KEY) as List<Chapter>
        pageLoader =
            BitIntentDataManager.getInstance().getData(APPCONST.PAGE_LOADER_KEY) as PageLoader
        searchWordEngine = SearchWordEngine(book, chapters, pageLoader)
        searchWord = DbManager.getDaoSession().searchWordDao.load(book.id)
    }

    override fun initWidget() {
        //enter事件
        binding.etSearchKey.setOnEditorActionListener { _, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_UNSPECIFIED) {
                search()
                return@setOnEditorActionListener keyEvent.keyCode == KeyEvent.KEYCODE_ENTER
            }
            false
        }
        searchWordEngine.setOnSearchListener(object : SearchWordEngine.OnSearchListener {
            override fun loadFinish(isEmpty: Boolean) {
                binding.fabSearchStop.visibility = View.GONE
                binding.rpb.isAutoLoading = false
                if (adapter.itemSize > 0) {
                    searchWord = SearchWord(book.id, keyword, adapter.items)
                    setUpToolbarTitle()
                    DbManager.getDaoSession().searchWordDao.insertOrReplace(searchWord)
                }
            }

            @Synchronized
            override fun loadMore(item: SearchWord1) {
                if (adapter.items.contains(item)) return
                if (adapter.itemSize == 0) {
                    adapter.addItem(item)
                } else {
                    for ((index, searchWord1) in adapter.items.withIndex()) {
                        if (index == 0 && item.chapterNum < searchWord1.chapterNum) {
                            adapter.addItem(0, item)
                            break
                        } else if (index == adapter.itemSize - 1) {
                            adapter.addItem(item)
                            break
                        } else if (item.chapterNum >= searchWord1.chapterNum &&
                            item.chapterNum < adapter.items[index + 1].chapterNum
                        ) {
                            adapter.addItem(index + 1, item)
                            break
                        }
                    }
                }
            }

        })
        adapter = object : BaseListAdapter<SearchWord1>() {
            override fun createViewHolder(viewType: Int): IViewHolder<SearchWord1> {
                return SearchWord1Holder(this@SearchWordActivity)
            }
        }
        binding.rvSearchWord1.layoutManager = LinearLayoutManager(this)
        binding.rvSearchWord1.adapter = adapter
        searchWord?.let {
            binding.etSearchKey.setText(it.keyword)
            keyword = it.keyword
            adapter.refreshItems(it.searchWords)
        }
    }

    override fun initClick() {
        binding.tvSearchConform.onClick { search() }
        binding.fabSearchStop.onClick { stopSearch() }
    }

    private fun search() {
        val keyword = binding.etSearchKey.text.toString()
        if (keyword.isNotEmpty() && this.keyword != keyword) {
            this.keyword = keyword
            adapter.clear()
            binding.fabSearchStop.visibility = View.VISIBLE
            binding.rpb.isAutoLoading = true
            searchWordEngine.search(keyword)
            //收起软键盘
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(
                binding.etSearchKey.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            DbManager.getDaoSession().searchWordDao.deleteByKey(book.id)
            searchWord = null
            setUpToolbarTitle()
        }
    }

    private fun stopSearch() {
        binding.fabSearchStop.visibility = View.GONE
        binding.rpb.isAutoLoading = false
        searchWordEngine.stopSearch()
    }

    override fun onDestroy() {
        super.onDestroy()
        searchWordEngine.stopSearch()
        searchWordEngine.closeSearchEngine()
    }
}