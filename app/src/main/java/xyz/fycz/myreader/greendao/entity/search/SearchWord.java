package xyz.fycz.myreader.greendao.entity.search;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.List;

import xyz.fycz.myreader.greendao.convert.SearchWord1Convert;

import org.greenrobot.greendao.annotation.Generated;

/**
 * @author fengyue
 * @date 2021/12/7 8:31
 */
@Entity
public class SearchWord {
    @Id
    private String bookId;
    private String keyword;
    @Convert(columnType = String.class, converter = SearchWord1Convert.class)
    private List<SearchWord1> searchWords;

    @Generated(hash = 2054974399)
    public SearchWord(String bookId, String keyword,
            List<SearchWord1> searchWords) {
        this.bookId = bookId;
        this.keyword = keyword;
        this.searchWords = searchWords;
    }

    @Generated(hash = 407254878)
    public SearchWord() {
    }

    public String getBookId() {
        return this.bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public List<SearchWord1> getSearchWords() {
        return this.searchWords;
    }

    public void setSearchWords(List<SearchWord1> searchWords) {
        this.searchWords = searchWords;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
