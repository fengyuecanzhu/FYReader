package xyz.fycz.dynamic;

import android.content.pm.ApplicationInfo;

/**
 * @author fengyue
 * @date 2022/3/29 11:31
 */
public class AppParam {
    /** The name of the package being loaded. */
    public String packageName;

    /** The ClassLoader used for this package. */
    public ClassLoader classLoader;

    /** More information about the application being loaded. */
    public ApplicationInfo appInfo;
}
