package xyz.fycz.myreader.util.llog;

import java.util.concurrent.LinkedBlockingQueue;


/**
 * @author luocan
 * @version 1.0
 *          </p>
 *          Created on 15/12/25.
 */
public class LQueue {

    private final LinkedBlockingQueue<LMsg> mLogQueue = new LinkedBlockingQueue<LMsg>();
    private LExecutor logExecutor;


    public void add(LMsg logMsg) {
        try {
            mLogQueue.add(logMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void start() {
        stop();
        try {
            logExecutor = new LExecutor(mLogQueue);
            logExecutor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (logExecutor != null) {
                logExecutor.quit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
