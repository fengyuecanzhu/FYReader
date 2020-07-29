package xyz.fycz.myreader.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by newbiechen on 17-5-11.
 */

public class IOUtils {

    public static void close(Closeable... closeables){
        for (Closeable closeable : closeables){
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
