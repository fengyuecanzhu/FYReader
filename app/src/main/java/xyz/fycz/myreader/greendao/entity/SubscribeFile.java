package xyz.fycz.myreader.greendao.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author fengyue
 * @date 2022/3/3 14:40
 */
@Entity
public class SubscribeFile {
    @Id
    private String id;

    private String name;

    private String url;

    private String date;

    private String size;

    @Generated(hash = 1850023033)
    public SubscribeFile(String id, String name, String url, String date,
            String size) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.date = date;
        this.size = size;
    }

    @Generated(hash = 1590903919)
    public SubscribeFile() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSize() {
        return this.size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
