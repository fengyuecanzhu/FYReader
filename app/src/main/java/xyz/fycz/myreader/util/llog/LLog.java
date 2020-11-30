package xyz.fycz.myreader.util.llog;

/**
 * @author luocan
 * @version 1.0
 *          </p>
 *          Created on 15/12/25.
 */
public class LLog {

    private static boolean isDebuggable = true;
    private static LQueue mLogQueue;
    private static String mRootPath;


    /**
     * @param rootPath the log file path
     */
    public static void init(String rootPath) {
        mLogQueue = new LQueue();
        mLogQueue.start();
        mRootPath = rootPath;
    }

    public static String getRootPath() {
        return mRootPath;
    }


    /**
     * open or close the function
     *
     * @param isDebuggable if set to false, not print the message.
     */
    public static void setIsDebuggable(boolean isDebuggable) {
        LLog.isDebuggable = isDebuggable;
    }

    /**
     * error log
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void e(String tag, String msg) {
        if (!isDebuggable) {
            return;
        }

        if (mLogQueue == null) {
            throw new NullPointerException("Must call init method first!");
        }

        LMsg logMsg = new LMsg();
        logMsg.setTag(tag);
        logMsg.setMsg(msg);
        logMsg.setPriority(LMsg.ERROR);
        mLogQueue.add(logMsg);
    }


    /**
     * warning log
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void w(String tag, String msg) {
        if (!isDebuggable) {
            return;
        }

        if (mLogQueue == null) {
            throw new NullPointerException("Must call init method first!");
        }

        LMsg logMsg = new LMsg();
        logMsg.setTag(tag);
        logMsg.setMsg(msg);
        logMsg.setPriority(LMsg.WARNING);
        mLogQueue.add(logMsg);
    }


    /**
     * info log
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void i(String tag, String msg) {
        if (!isDebuggable) {
            return;
        }

        if (mLogQueue == null) {
            throw new NullPointerException("Must call init method first!");
        }

        LMsg logMsg = new LMsg();
        logMsg.setTag(tag);
        logMsg.setMsg(msg);
        logMsg.setPriority(LMsg.INFO);
        mLogQueue.add(logMsg);
    }


    /**
     * debug log
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void d(String tag, String msg) {
        if (!isDebuggable) {
            return;
        }

        if (mLogQueue == null) {
            throw new NullPointerException("Must call init method first!");
        }

        LMsg logMsg = new LMsg();
        logMsg.setTag(tag);
        logMsg.setMsg(msg);
        logMsg.setPriority(LMsg.DEBUG);
        mLogQueue.add(logMsg);
    }
}
