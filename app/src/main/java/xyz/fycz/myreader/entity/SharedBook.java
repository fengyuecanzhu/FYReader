package xyz.fycz.myreader.entity;

import xyz.fycz.myreader.greendao.entity.Book;

/**
 * @author fengyue
 * @date 2020/11/29 19:01
 */
public class SharedBook {
    private String name;
    private String author;
    private String type;
    private String desc;
    private String imgUrl;
    private String chapterUrl;
    private String source;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getChapterUrl() {
        return chapterUrl;
    }

    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public static SharedBook bookToSharedBook(Book book){
        int maxDesc = 208;
        String desc = book.getDesc();
        if (desc.length() > maxDesc){
            desc = desc.substring(0, maxDesc - 1) + "…";
        }
        SharedBook sharedBook = new SharedBook();
        sharedBook.setName(book.getName());
        sharedBook.setAuthor(book.getAuthor());
        sharedBook.setType(book.getType());
        sharedBook.setDesc(desc);
        sharedBook.setImgUrl(book.getImgUrl());
        sharedBook.setChapterUrl(book.getChapterUrl());
        sharedBook.setSource(book.getSource());
        return sharedBook;
    }

    public static Book sharedBookToBook(SharedBook sharedBook){
        Book book = new Book();
        book.setName(sharedBook.name);
        book.setAuthor(sharedBook.author);
        book.setType(sharedBook.type);
        book.setDesc(sharedBook.desc);
        book.setImgUrl(sharedBook.imgUrl);
        book.setChapterUrl(sharedBook.chapterUrl);
        book.setSource(sharedBook.source);
        return book;
    }
}
