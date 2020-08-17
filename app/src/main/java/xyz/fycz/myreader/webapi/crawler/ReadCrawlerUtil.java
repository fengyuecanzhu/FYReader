package xyz.fycz.myreader.webapi.crawler;


import java.util.ResourceBundle;

/**
 * @author fengyue
 * @date 2020/5/17 11:45
 */
public class ReadCrawlerUtil {
    private ReadCrawlerUtil() {
    }
    public static ReadCrawler getReadCrawler(String bookSource){
        ResourceBundle rb = ResourceBundle.getBundle("bookcrawler");
        try{
            String readCrawlerPath = rb.getString(bookSource);
            Class clz = Class.forName(readCrawlerPath);
            return (ReadCrawler) clz.newInstance();
        }catch (Exception e){
            return new FYReadCrawler();
        }
    }
}
