package xyz.fycz.myreader.greendao.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author fengyue
 * @date 2022/1/18 10:20
 */
@Entity
public class Cache {
    @Id
    private String key;

    private String value;

    private long deadLine;

    @Generated(hash = 1252535078)
    public Cache(String key, String value, long deadLine) {
        this.key = key;
        this.value = value;
        this.deadLine = deadLine;
    }

    @Generated(hash = 1305017356)
    public Cache() {
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getDeadLine() {
        return this.deadLine;
    }

    public void setDeadLine(long deadLine) {
        this.deadLine = deadLine;
    }
}
