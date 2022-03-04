package xyz.fycz.myreader.model.user;

import androidx.annotation.NonNull;

import xyz.fycz.myreader.util.utils.GsonExtensionsKt;

/**
 * @author fengyue
 * @date 2020/7/12 17:35
 */
public class User {
    private Integer userId;
    private String userName;
    private String password;
    private String email;
    private String backupTime;
    private String noAdTime;
    private String noAdId;

    public User() {
    }

    public User(String userName) {
        this.userName = userName;
    }

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public User(String userName, String password, String email) {
        this.userName = userName;
        this.password = password;
        this.email = email;
    }

    public User(Integer userId, String userName, String password, String email) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.email = email;
    }

    public User(Integer userId, String userName, String password, String email, String backupTime, String noAdTime, String noAdId) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.backupTime = backupTime;
        this.noAdTime = noAdTime;
        this.noAdId = noAdId;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBackupTime() {
        return backupTime;
    }

    public void setBackupTime(String backupTime) {
        this.backupTime = backupTime;
    }

    public String getNoAdTime() {
        return noAdTime;
    }

    public void setNoAdTime(String noAdTime) {
        this.noAdTime = noAdTime;
    }

    public String getNoAdId() {
        return noAdId;
    }

    public void setNoAdId(String noAdId) {
        this.noAdId = noAdId;
    }

    @NonNull
    @Override
    public String toString() {
        return GsonExtensionsKt.getGSON().toJson(this);
    }
}
