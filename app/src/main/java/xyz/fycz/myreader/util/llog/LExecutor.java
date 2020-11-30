package xyz.fycz.myreader.util.llog;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;

/**
 * @author luocan
 * @version 1.0
 *          </p>
 *          Created on 15/12/25.
 */
public class LExecutor extends Thread {

    private final BlockingQueue<LMsg> mQueue;

    private volatile boolean mQuit = false;

    private static DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static DateFormat mTimeFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

    public LExecutor(BlockingQueue<LMsg> mQueue) {
        this.mQueue = mQueue;
    }

    @Override
    public void run() {
        setThreadPriority(THREAD_PRIORITY_BACKGROUND);

        while (true) {
            LMsg logMsg;
            try {

                logMsg = mQueue.take();

            } catch (InterruptedException e) {

                if (mQuit) {
                    return;
                }
                continue;
            }

            String date = mDateFormat.format(Calendar.getInstance(Locale.CHINA).getTime());

            String fileName = date + "log.html";

            StringBuilder builder = new StringBuilder();

            switch (logMsg.getPriority()) {
                case LMsg.INFO:
                    builder.append("<font color=\"black\">");
                    break;
                case LMsg.DEBUG:
                    builder.append("<font color=\"green\">");
                    break;
                case LMsg.ERROR:
                    builder.append("<font color=\"#fc0d1b\">");
                    break;
                case LMsg.WARNING:
                    builder.append("<font color=\"#FFD306\">");
                    break;
            }

            Calendar calendar = Calendar.getInstance();
            builder.append("[ " + mTimeFormat.format(calendar.getTime()) + " ] ");
            builder.append("[ " + logMsg.getTag() + " ] :");
            builder.append(logMsg.getMsg());
            builder.append("</font><br> ");

            try {
                File file = getLogFile(fileName);
                if (file == null) {
                    return;
                }
                FileOutputStream outputStream = new FileOutputStream(file, true);
                outputStream.write(builder.toString().getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Forces this executor to quit immediately.  If any requests are still in
     * the queue, they are not guaranteed to be processed.
     */
    public void quit() {
        mQuit = true;
        interrupt();
    }


    /**
     * check sd
     *
     * @return
     */
    private static boolean checkSDCard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        }

        return false;
    }

    /**
     * generate the log file
     *
     * @param fileName
     * @return
     */
    private static File getLogFile(String fileName) {

        if (!checkSDCard()) {
            return null;
        }

        File storageRoot = Environment.getExternalStorageDirectory();

        String path = LLog.getRootPath() + File.separator + "log";                //日志文件目录

        File tmpFile = new File(storageRoot.getAbsolutePath().concat(File.separator).concat(path));

        if (!tmpFile.exists()) {

            boolean result = tmpFile.mkdirs();

            if (result) {
                return new File(tmpFile, fileName);
            }

            return null;
        }

        return new File(tmpFile, fileName);
    }
}
