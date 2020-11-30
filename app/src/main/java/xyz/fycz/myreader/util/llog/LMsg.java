package xyz.fycz.myreader.util.llog;

/**
 * @author luocan
 * @version 1.0
 *          </p>
 *          Created on 15/12/25.
 */
public class LMsg {
    public static final int INFO = 0x01;
    public static final int DEBUG = 0x02;
    public static final int ERROR = 0x03;
    public static final int WARNING = 0x04;

    protected String mTag;
    protected String mMsg;
    protected int mPriority;


    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        this.mTag = tag;
    }

    public String getMsg() {
        return mMsg;
    }

    public void setMsg(String msg) {
        this.mMsg = msg;
    }

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int priority) {
        this.mPriority = priority;
    }

}
