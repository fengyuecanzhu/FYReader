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

package xyz.fycz.myreader.ui.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import xyz.fycz.myreader.R
import xyz.fycz.myreader.application.SysManager
import xyz.fycz.myreader.base.BitIntentDataManager
import xyz.fycz.myreader.base.adapter2.DiffRecyclerAdapter
import xyz.fycz.myreader.base.adapter2.ItemViewHolder
import xyz.fycz.myreader.databinding.ItemSearchBookBinding
import xyz.fycz.myreader.entity.SearchBookBean
import xyz.fycz.myreader.greendao.entity.Book
import xyz.fycz.myreader.model.mulvalmap.ConMVMap
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager
import xyz.fycz.myreader.ui.activity.BookDetailedActivity
import xyz.fycz.myreader.util.help.StringHelper
import xyz.fycz.myreader.util.utils.KeyWordUtils
import xyz.fycz.myreader.util.utils.StringUtils

/**
 * @author fengyue
 * @date 2021/8/20 17:19
 */

class SearchAdapter(
    context: Context,
    val keyword: String
) : DiffRecyclerAdapter<SearchBookBean, ItemSearchBookBinding>(context) {
    private val mBooks: ConMVMap<SearchBookBean, Book> = ConMVMap()
    private lateinit var mList: List<SearchBookBean>
    private val tagList: MutableList<String> = ArrayList()
    private val handler = Handler(Looper.getMainLooper())
    private var postTime = 0L
    private val sendRunnable = Runnable { upAdapter() }

    override val diffItemCallback: DiffUtil.ItemCallback<SearchBookBean>
        get() = object : DiffUtil.ItemCallback<SearchBookBean>() {
            override fun areItemsTheSame(
                oldItem: SearchBookBean,
                newItem: SearchBookBean
            ): Boolean {
                return when {
                    oldItem.name != newItem.name -> false
                    oldItem.author != newItem.author -> false
                    else -> true
                }
            }

            override fun areContentsTheSame(
                oldItem: SearchBookBean,
                newItem: SearchBookBean
            ): Boolean {
                return false
            }

            override fun getChangePayload(oldItem: SearchBookBean, newItem: SearchBookBean): Any {
                val payload = Bundle()
                payload.putInt("sourceCount", newItem.sourceCount)
                if (oldItem.imgUrl != newItem.imgUrl)
                    payload.putString("imgUrl", newItem.imgUrl)
                if (oldItem.type != newItem.type)
                    payload.putString("type", newItem.type)
                if (oldItem.status != newItem.status)
                    payload.putString("status", newItem.status)
                if (oldItem.wordCount != newItem.wordCount)
                    payload.putString("wordCount", newItem.wordCount)
                if (oldItem.lastChapter != newItem.lastChapter)
                    payload.putString("last", newItem.lastChapter)
                if (oldItem.desc != newItem.desc)
                    payload.putString("desc", newItem.desc)
                return payload
            }
        }

    override fun getViewBinding(parent: ViewGroup): ItemSearchBookBinding {
        return ItemSearchBookBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemSearchBookBinding,
        item: SearchBookBean,
        payloads: MutableList<Any>
    ) {
        val payload = payloads.getOrNull(0) as? Bundle
        if (payload == null) {
            bind(binding, item)
        } else {
            val books = mBooks.getValues(item) ?: arrayListOf()
            books2SearchBookBean(item, books)
            bindChange(binding, item, payload)
        }
    }

    private fun bind(binding: ItemSearchBookBinding, data: SearchBookBean) {
        var aBooks = mBooks.getValues(data)
        if (aBooks == null || aBooks.size == 0) {
            aBooks = ArrayList()
            aBooks.add(searchBookBean2Book(data))
        }
        val book = aBooks.getOrNull(0) ?: return
        val source = BookSourceManager.getBookSourceByStr(book.source)
        data.sourceName = source.sourceName
        books2SearchBookBean(data, aBooks)
        binding.run {
            ivBookImg.load(data.imgUrl, data.name, data.author)
            KeyWordUtils.setKeyWord(tvBookName, data.name, keyword)
            KeyWordUtils.setKeyWord(tvBookAuthor, data.author ?: "", keyword)
            initTagList(this, data)
            upLast(binding, data.lastChapter)
            tvBookDesc.text = String.format("简介:%s", data.desc ?: "暂无简介")
            tvBookSource.text = context.getString(
                R.string.source_title_num,
                data.sourceName,
                data.sourceCount
            )
        }
    }

    private fun bindChange(binding: ItemSearchBookBinding, data: SearchBookBean, payload: Bundle) {
        binding.run {
            initTagList(this, data)
            payload.keySet().forEach {
                when (it) {
                    "sourceCount" -> tvBookSource.text = context.getString(
                        R.string.source_title_num,
                        data.sourceName,
                        data.sourceCount
                    )
                    "imgUrl" -> ivBookImg.load(
                        data.imgUrl,
                        data.name,
                        data.author
                    )
                    "last" -> upLast(binding, data.lastChapter)
                    "desc" -> tvBookDesc.text = String.format("简介:%s", data.desc)
                }
            }
        }
    }

    private fun initTagList(binding: ItemSearchBookBinding, data: SearchBookBean) {
        tagList.clear()
        val type = data.type
        if (!type.isNullOrEmpty()){
            tagList.add(type)
        }
        val wordCount = data.wordCount
        if (!wordCount.isNullOrEmpty()) tagList.add(wordCount)
        val status = data.status
        if (!status.isNullOrEmpty()) tagList.add(status)
        binding.run {
            if (tagList.size == 0) {
                tflBookTag.visibility = View.GONE
            } else {
                tflBookTag.visibility = View.VISIBLE
                tflBookTag.adapter = BookTagAdapter(context, tagList, 11)
            }
        }
    }

    private fun upLast(binding: ItemSearchBookBinding, lastChapter: String?) {
        binding.run {
            if (lastChapter.isNullOrEmpty()) {
                tvBookNewestChapter.visibility = View.GONE
            } else {
                tvBookNewestChapter.visibility = View.VISIBLE
                tvBookNewestChapter.text = context.getString(
                    R.string.newest_chapter,
                    lastChapter
                )
            }
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemSearchBookBinding) {
        binding.root.setOnClickListener {
            getItem(holder.layoutPosition)?.let {
                val books = mBooks.getValues(it)
                if (books == null || books.size == 0) return@let
                books[0] = searchBookBean2Book(it, books[0])
                val intent = Intent(
                    context,
                    BookDetailedActivity::class.java
                )
                BitIntentDataManager.getInstance().putData(intent, books)
                context.startActivity(intent)
            }
        }
    }

    private fun books2SearchBookBean(bookBean: SearchBookBean, books: List<Book>) {
        bookBean.sourceCount = books.size
        for (book in books) {
            if (!StringHelper.isEmpty(bookBean.author)) break
            val author = book.author
            if (!StringHelper.isEmpty(author)) {
                bookBean.author = author
                break
            }
        }
        for (book in books) {
            if (!StringHelper.isEmpty(bookBean.type)) break
            val type = book.type
            if (!StringHelper.isEmpty(type)) {
                bookBean.type = type
                break
            }
        }
        for (book in books) {
            if (!StringHelper.isEmpty(bookBean.desc)) break
            val desc = book.desc
            if (!StringHelper.isEmpty(desc)) {
                bookBean.desc = desc
                break
            }
        }
        for (book in books) {
            if (!StringHelper.isEmpty(bookBean.status)) break
            val status = book.status
            if (!StringHelper.isEmpty(status)) {
                bookBean.status = status
                break
            }
        }
        for (book in books) {
            if (!StringHelper.isEmpty(bookBean.wordCount)) break
            val wordCount = book.wordCount
            if (!StringHelper.isEmpty(wordCount)) {
                bookBean.wordCount = wordCount
                break
            }
        }
        for (book in books) {
            if (!StringHelper.isEmpty(bookBean.lastChapter)) break
            val lastChapter = book.newestChapterTitle
            if (!StringHelper.isEmpty(lastChapter)) {
                bookBean.lastChapter = lastChapter
                break
            }
        }
        for (book in books) {
            if (!StringHelper.isEmpty(bookBean.updateTime)) break
            val updateTime = book.updateDate
            if (!StringHelper.isEmpty(updateTime)) {
                bookBean.updateTime = updateTime
                break
            }
        }
        for (book in books) {
            if (!StringHelper.isEmpty(bookBean.imgUrl)) break
            val imgUrl = book.imgUrl
            if (!StringHelper.isEmpty(imgUrl)) {
                bookBean.imgUrl = imgUrl
                break
            }
        }
    }

    private fun searchBookBean2Book(bean: SearchBookBean, book: Book = Book()): Book {
        book.name = bean.name
        book.author = bean.author
        book.type = bean.type
        book.desc = bean.desc
        book.status = bean.status
        book.updateDate = bean.updateTime
        book.newestChapterTitle = bean.lastChapter
        book.wordCount = bean.wordCount
        return book
    }

    fun addAll(items: ConMVMap<SearchBookBean, Book>, searchKey: String) {
        mBooks.addAll(items)
        addAll(ArrayList(items.keySet()), searchKey)
    }

    fun addAll(newDataS: List<SearchBookBean>, keyWord: String?) {
        val copyDataS: MutableList<SearchBookBean> = ArrayList(getItems())
        val filterDataS: MutableList<SearchBookBean> = ArrayList()
        when (SysManager.getSetting().searchFilter) {
            0 -> filterDataS.addAll(newDataS)
            1 -> for (ssb in newDataS) {
                if (StringUtils.isContainEachOther(ssb.name, keyWord) ||
                    StringUtils.isContainEachOther(ssb.author, keyWord)
                ) {
                    filterDataS.add(ssb)
                }
            }
            2 -> for (ssb in newDataS) {
                if (StringUtils.isEqual(ssb.name, keyWord) ||
                    StringUtils.isEqual(ssb.author, keyWord)
                ) {
                    filterDataS.add(ssb)
                }
            }
            else -> for (ssb in newDataS) {
                if (StringUtils.isContainEachOther(ssb.name, keyWord) ||
                    StringUtils.isContainEachOther(ssb.author, keyWord)
                ) {
                    filterDataS.add(ssb)
                }
            }
        }
        if (filterDataS.size > 0) {
            val searchBookBeansAdd: MutableList<SearchBookBean> = ArrayList()
            if (copyDataS.size == 0) {
                copyDataS.addAll(filterDataS)
            } else {
                //存在
                for (temp in filterDataS) {
                    var hasSame = false
                    var i = 0
                    val size = copyDataS.size
                    while (i < size) {
                        val searchBook = copyDataS[i]
                        if (TextUtils.equals(temp.name, searchBook.name)
                            && TextUtils.equals(temp.author, searchBook.author)
                        ) {
                            hasSame = true
                            break
                        }
                        i++
                    }
                    if (!hasSame) {
                        searchBookBeansAdd.add(temp)
                    }
                }
                //添加
                for (temp in searchBookBeansAdd) {
                    if (TextUtils.equals(keyWord, temp.name)) {
                        for (i in copyDataS.indices) {
                            val searchBook = copyDataS[i]
                            if (!TextUtils.equals(keyWord, searchBook.name)) {
                                copyDataS.add(i, temp)
                                break
                            }
                        }
                    } else if (TextUtils.equals(keyWord, temp.author)) {
                        for (i in copyDataS.indices) {
                            val searchBook = copyDataS[i]
                            if (!TextUtils.equals(keyWord, searchBook.name) &&
                                !TextUtils.equals(keyWord, searchBook.author)
                            ) {
                                copyDataS.add(i, temp)
                                break
                            }
                        }
                    } else {
                        copyDataS.add(temp)
                    }
                }
            }
            mList = copyDataS
            upAdapter()
        }
    }

    @Synchronized
    private fun upAdapter() {
        if (System.currentTimeMillis() >= postTime + 500) {
            handler.removeCallbacks(sendRunnable)
            postTime = System.currentTimeMillis()
            setItems(mList)
        } else {
            handler.removeCallbacks(sendRunnable)
            handler.postDelayed(sendRunnable, 500 - System.currentTimeMillis() + postTime)
        }
    }
}