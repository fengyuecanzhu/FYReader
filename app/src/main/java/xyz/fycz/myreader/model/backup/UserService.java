package xyz.fycz.myreader.model.backup;

import io.reactivex.annotations.NonNull;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.model.storage.Backup;
import xyz.fycz.myreader.model.storage.Restore;
import xyz.fycz.myreader.webapi.callback.ResultCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.util.*;
import xyz.fycz.myreader.util.utils.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fengyue
 * @date 2020/4/26 11:03
 */
public class UserService {
    /**
     * 登录
     * @param userLoginInfo 用户名输入的用户名和密码等登录信息
     * @return 是否成功登录
     */
    public static void login(final Map<String, String> userLoginInfo, final ResultCallback resultCallback) {
        App.getApplication().newThread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(URLCONST.APP_WEB_URL + "login");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(60 * 1000);
                conn.setReadTimeout(60 * 1000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                String params = "username=" + userLoginInfo.get("loginName") +
                        "&password=" + userLoginInfo.get("loginPwd") + makeSignalParam();
                // 获取URLConnection对象对应的输出流
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                out.print(params);
                // flush输出流的缓冲
                out.flush();
                InputStream in = conn.getInputStream();
                BufferedReader bw = new BufferedReader(new InputStreamReader(in, "utf-8"));
                StringBuilder sb = new StringBuilder();
                String line = bw.readLine();
                while (line != null) {
                    sb.append(line);
                    line = bw.readLine();
                }
                resultCallback.onFinish(sb.toString(), 1);
            } catch (IOException e) {
                e.printStackTrace();
                resultCallback.onError(e);
            }finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    public static void register(final Map<String, String> userRegisterInfo, final ResultCallback resultCallback) {
        App.getApplication().newThread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(URLCONST.APP_WEB_URL + "reg");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                String params = "username=" + userRegisterInfo.get("username") + "&password=" +
                        CyptoUtils.encode(APPCONST.KEY, userRegisterInfo.get("password")) + "&key=" +
                        CyptoUtils.encode(APPCONST.KEY, APPCONST.publicKey)  + makeSignalParam();
                // 获取URLConnection对象对应的输出流
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                out.print(params);
                // flush输出流的缓冲
                out.flush();
                BufferedReader bw = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder sb = new StringBuilder();
                String line = bw.readLine();
                while (line != null) {
                    sb.append(line);
                    line = bw.readLine();
                }
                resultCallback.onFinish(sb.toString(), 1);
            } catch (IOException e) {
                e.printStackTrace();
                resultCallback.onError(e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

        });
    }

    /**
     * 写配置
     * @param userLoginInfo
     * @return
     */
    public static boolean writeConfig(Map<String,String> userLoginInfo){
        FileOutputStream fos = null;
        try {
            fos = App.getApplication().openFileOutput("userConfig.fy", App.getApplication().MODE_PRIVATE);
            String userInfo = "username='" + userLoginInfo.get("loginName") + "',\npassword='" + userLoginInfo.get("loginPwd") + "'";
            byte[] bs = userInfo.getBytes();
            fos.write(bs);
            //写完后一定要刷新
            fos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            IOUtils.close(fos);
        }
    }

    /**
     * 读配置
     * @return
     */
    public static Map<String,String> readConfig(){
        File file = App.getApplication().getFileStreamPath("userConfig.fy");
        if (!file.exists()){
            return null;
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String tem;
            StringBuilder config = new StringBuilder();
            while ((tem = br.readLine()) != null){
                config.append(tem);
            }
            String[] user = config.toString().split(",");
            String userName = user[0].substring(user[0].indexOf("'") + 1, user[0].lastIndexOf("'"));
            String password = user[1].substring(user[1].indexOf("'") + 1, user[1].lastIndexOf("'"));
            Map<String,String> userInfo = new HashMap<>();
            userInfo.put("userName", userName);
            userInfo.put("password", password);
            return userInfo;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IOUtils.close(br);
        }
        return null;
    }

    public static void writeUsername(String username){
        File file = FileUtils.getFile(APPCONST.QQ_DATA_DIR + "user");
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(username);
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IOUtils.close(bw);
        }
    }

    public static String readUsername(){
        File file = new File(APPCONST.QQ_DATA_DIR + "user");
        if (!file.exists()){
            return "";
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            IOUtils.close(br);
        }
    }

    /**
     * 网络备份
     * @return
     */
    public static void webBackup(ResultCallback rc){
        Map<String,String> userInfo = readConfig();
        if (userInfo == null){
            rc.onFinish(false, 0);
        }
        Backup.INSTANCE.backup(App.getmContext(), APPCONST.FILE_DIR + "webBackup/", new Backup.CallBack() {
            @Override
            public void backupSuccess() {
                App.getApplication().newThread(() ->{
                    File inputFile = FileUtils.getFile(APPCONST.FILE_DIR + "webBackup");
                    if (!inputFile.exists()) {
                        rc.onFinish(false, 0);
                    }
                    File zipFile = FileUtils.getFile(APPCONST.FILE_DIR + "webBackup.zip");
                    FileInputStream fis = null;
                    HttpURLConnection conn = null;
                    try {
                        //压缩文件
                        ZipUtils.zipFile(inputFile, zipFile);
                        fis = new FileInputStream(zipFile);
                        URL url = new URL(URLCONST.APP_WEB_URL + "bak?username=" + userInfo.get("userName") +
                                makeSignalParam());
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-type", "multipart/form-data");
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        OutputStream out = conn.getOutputStream();
                        byte[] bytes = new byte[1024];
                        int len = -1;
                        while ((len = fis.read(bytes)) != -1){
                            out.write(bytes, 0, len);
                        }
                        out.flush();
                        zipFile.delete();
                        BufferedReader bw = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                        StringBuilder sb = new StringBuilder();
                        String line = bw.readLine();
                        while (line != null) {
                            sb.append(line);
                            line = bw.readLine();
                        }
                        String[] info = sb.toString().split(":");
                        int code = Integer.parseInt(info[0].trim());
                        rc.onFinish(code == 104, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        rc.onError(e);
                    } finally {
                        IOUtils.close(fis);
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }
                });
            }

            @Override
            public void backupError(@NonNull String msg) {
                ToastUtils.showError(msg);
                rc.onFinish(false, 0);
            }
        }, false);

    }

    /**
     * 网络恢复
     * @return
     */
    public static void webRestore(ResultCallback rc){
        Map<String,String> userInfo = readConfig();
        if (userInfo == null){
            rc.onFinish(false, 0);
        }
        FileOutputStream fos = null;
        File zipFile = FileUtils.getFile(APPCONST.FILE_DIR + "webBackup.zip");
        HttpURLConnection conn = null;
        try {
            URL url = new URL(URLCONST.APP_WEB_URL + "ret?username=" + userInfo.get("userName") +
                    makeSignalParam());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            InputStream is = conn.getInputStream();
            fos = new FileOutputStream(zipFile);
            //一边读，一边写
            byte[] bytes = new byte[512];
            int readCount = 0;
            while ((readCount = is.read(bytes)) != -1) {
                fos.write(bytes,0, readCount);
            }
            //刷新,输出流一定要刷新
            fos.flush();
            if (zipFile.length() == 0){
                zipFile.delete();
                rc.onFinish(false, 0);
            }
            ZipUtils.unzipFile(zipFile.getAbsolutePath(), APPCONST.FILE_DIR);
            Restore.INSTANCE.restore(APPCONST.FILE_DIR + "webBackup/", new Restore.CallBack() {
                @Override
                public void restoreSuccess() {
                    zipFile.delete();
                    rc.onFinish(true, 0);
                }

                @Override
                public void restoreError(@NonNull String msg) {
                    ToastUtils.showError(msg);
                    rc.onFinish(false, 0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            rc.onError(e);
        }finally {
            IOUtils.close(fos);
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    public static String makeSignalParam(){
        return "&signal=" + AppInfoUtils.getSingInfo(App.getmContext(),
                App.getApplication().getPackageName(), AppInfoUtils.SHA1);
    }

    /**
     * 判断是否登录
     * @return
     */
    public static boolean isLogin(){
        File file = App.getApplication().getFileStreamPath("userConfig.fy");
        return file.exists();
    }
}
