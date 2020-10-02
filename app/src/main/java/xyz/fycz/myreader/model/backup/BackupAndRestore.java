package xyz.fycz.myreader.model.backup;

import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.service.BookService;
import xyz.fycz.myreader.util.IOUtils;
import xyz.fycz.myreader.util.utils.FileUtils;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fengyue
 * @date 2020/4/25 9:07
 */

public class BackupAndRestore {

    BookService mBookService = BookService.getInstance();

    /**
     * 备份书架
     * @return 是否备份成功
     */
    public boolean backup(String backupName){
        List<Book> books = mBookService.getAllBooks();
        StringBuilder s = new StringBuilder();
        for (Book book : books) {
            s.append(book);
            s.append(",\n");
        }
        s.deleteCharAt(s.lastIndexOf(","));
        File booksFile = FileUtils.getFile(APPCONST.FILE_DIR + backupName + "/books" + FileUtils.SUFFIX_FY);
        File settingFile = FileUtils.getFile(APPCONST.FILE_DIR + backupName + "/setting" + FileUtils.SUFFIX_FY);
        BufferedWriter bw = null;
        ObjectOutputStream oos = null;
        try {
            bw = new BufferedWriter(new FileWriter(booksFile));
            bw.write(s.toString());
            bw.flush();
            oos = new ObjectOutputStream(new FileOutputStream(settingFile));
            oos.writeObject(SysManager.getSetting());
            oos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            IOUtils.close(bw, oos);
        }
    }

    /**
     * 恢复书架
     * @return 是否恢复成功
     */
    public boolean restore(String backupName) {
        File booksFile = FileUtils.getFile(APPCONST.FILE_DIR + backupName + "/books" + FileUtils.SUFFIX_FY);
        File settingFile = FileUtils.getFile(APPCONST.FILE_DIR + backupName + "/setting" + FileUtils.SUFFIX_FY);
        if (!booksFile.exists() || !settingFile.exists()){
            return false;
        }
        BufferedReader br = null;
        ObjectInputStream ois = null;
        try {
            br = new BufferedReader(new FileReader(booksFile));
            String tem = "";
            StringBuilder s = new StringBuilder();
            while ((tem = br.readLine()) != null){
                s.append(tem).append("\n");
            }
            String[] sBooks = s.toString().split("\\},");
            List<Book> books = new ArrayList<>();
            for (String sBook : sBooks){
                sBook.replace("{", "");
                sBook.replace("}", "");
                String[] sBookFields = sBook.split(",\n");
                for (int i = 0; i < sBookFields.length; i++) {
                    sBookFields[i] = sBookFields[i].substring(sBookFields[i].indexOf("'") + 1, sBookFields[i].lastIndexOf("'"));
                }
                String source = "null";
                boolean isCloseUpdate = false;
                boolean isDownloadAll = true;
                String group = "allBook";
                String infoUrl = "";
                if(!sBookFields[2].contains("novel.fycz.xyz")){
                    source = sBookFields[17];
                }
                if ("本地书籍".equals(sBookFields[4])){
                    sBookFields[15] = "0";
                }
                if (sBookFields.length >= 19){
                    isCloseUpdate = Boolean.parseBoolean(sBookFields[18]);
                }
                if (sBookFields.length >= 20){
                    isDownloadAll = Boolean.parseBoolean(sBookFields[19]);
                }
                if (sBookFields.length >= 21){
                    group = sBookFields[20];
                }
                if (sBookFields.length >= 22){
                    infoUrl = sBookFields[21];
                }
                Book book = new Book(sBookFields[0], sBookFields[1], sBookFields[2], infoUrl, sBookFields[3], sBookFields[4],
                        sBookFields[5], sBookFields[6], sBookFields[7], sBookFields[8], sBookFields[9], sBookFields[10],
                        sBookFields[11], Integer.parseInt(sBookFields[12]), Integer.parseInt(sBookFields[13]),
                        Integer.parseInt(sBookFields[14]), Integer.parseInt(sBookFields[15]), Integer.parseInt(sBookFields[16])
                        , source, isCloseUpdate, isDownloadAll, group, 0);
                books.add(book);
            }
            mBookService.deleteAllBooks();
            mBookService.addBooks(books);
            ois = new ObjectInputStream(new FileInputStream(settingFile));
            Object obj = ois.readObject();
            if (obj instanceof Setting){
                Setting setting = (Setting) obj;
                SysManager.saveSetting(setting);
            }
            return true;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } finally {
            IOUtils.close(br, ois);
        }
    }
}