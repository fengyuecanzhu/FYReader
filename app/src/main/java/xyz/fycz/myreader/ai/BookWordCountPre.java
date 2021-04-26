package xyz.fycz.myreader.ai;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.entity.Chapter;
import xyz.fycz.myreader.greendao.service.ChapterService;

/**
 * 预测书籍字数
 *
 * @author fengyue
 * @date 2021/4/18 16:54
 */
public class BookWordCountPre {
    private static final String TAG = BookWordCountPre.class.getSimpleName();

    private double lr = 0.001;
    private Book book;
    private List<Chapter> chapters;
    private double[][] trainData;
    private double[][] target;
    private double[][] weights1;
    //private double[][] weights2;
    private double[][] testData;
    private double median;
    private static Random rand = new Random();


    public BookWordCountPre(Book book) {
        this.book = book;
        this.chapters = ChapterService.getInstance().findBookAllChapterByBookId(book.getId());
    }

    //进行训练
    public boolean train() {
        if (!preData()) {
            Log.i(TAG, String.format("《%s》缓存章节数量过少，无法进行训练", book.getName()));
            return false;
        }
        Log.i(TAG, String.format("《%s》开始进行训练", book.getName()));
        double loss = 0;
        double eps = 0.0000000001;
        double[][] gradient1;
        //double[][] gradient2;
        double[][] adagrad1 = new double[trainData[0].length][1];
        //double[][] adagrad2 = new double[trainData[0].length][1];
        //double[][] dl_dw = new double[trainData[0].length][1];
        int maxEpoch;
        maxEpoch = 1000 / trainData.length;
        if (maxEpoch < 10) maxEpoch = 10;
        for (int epoch = 0; epoch < maxEpoch; epoch++) {
            shuffle(trainData, target);
            for (int j = 0; j < trainData.length; j++) {
                double[][] oneData = MatrixUtil.to2dMatrix(trainData[j], false);
                double[][] oneTarget = MatrixUtil.to2dMatrix(target[j], true);
                double[][] out = getOut(oneData);
                loss = Math.sqrt(MatrixUtil.sum(MatrixUtil.pow(MatrixUtil.sub(out, oneTarget), 2)) / 2);
                /*dl_dw = MatrixUtil.sub(
                        MatrixUtil.add(
                        MatrixUtil.dot(MatrixUtil.pow(oneData, 2), weights2),
                        MatrixUtil.dot(oneData, weights2)),
                        oneTarget
                );*/

                gradient1 = MatrixUtil.dot(MatrixUtil.transpose(oneData), MatrixUtil.sub(out, oneTarget));
                //gradient1 = MatrixUtil.dot(MatrixUtil.transpose(MatrixUtil.pow(oneData, 2)), dl_dw);
                //gradient2 = MatrixUtil.dot(MatrixUtil.transpose(oneData), dl_dw);
                adagrad1 = MatrixUtil.add(adagrad1, MatrixUtil.pow(gradient1, 2));
                //adagrad2 = MatrixUtil.add(adagrad1, MatrixUtil.pow(gradient2, 2));
                weights1 = MatrixUtil.sub(weights1, MatrixUtil.divide(MatrixUtil.dot(gradient1, lr),
                        MatrixUtil.sqrt(MatrixUtil.add(adagrad1, eps))));
                /*weights2 = MatrixUtil.sub(weights2, MatrixUtil.divide(MatrixUtil.dot(gradient2, lr),
                        MatrixUtil.sqrt(MatrixUtil.add(adagrad2, eps))));*/
            }
            Log.i(TAG, String.format("《%s》-> epoch=%d，loss=%f", book.getName(), epoch, loss));
        }
        return true;
    }

    //进行预测并获得书籍总字数
    public int predict() {
        double[][] pre = getOut(testData);
        double[] preVec = MatrixUtil.toVector(pre);
        Arrays.sort(preVec);
        int k = (int) (preVec[preVec.length / 2 + 1] / median);
        //int k = (int) ((MatrixUtil.sum(pre) / pre.length) / median);
        pre = MatrixUtil.divide(pre, k);
        /*for (int i = 0; i < pre.length; i++) {
            pre[i][0] = median;
        }*/
        Log.i(TAG, String.format("k=%d->《%s》的预测数据%s", k, book.getName(),
                Arrays.toString(MatrixUtil.toVector(pre))));
        return (int) (MatrixUtil.sum(pre) + MatrixUtil.sum(target));
    }

    private double[][] getOut(double[][] data) {
        /*return MatrixUtil.add(MatrixUtil.dot(MatrixUtil.pow(data, 2), weights2),
                MatrixUtil.dot(data, weights1));*/
        return MatrixUtil.dot(data, weights1);
    }

    //准备训练数据
    private boolean preData() {
        rand.setSeed(10);
        List<Chapter> catheChapters = new ArrayList<>();
        List<Chapter> unCatheChapters = new ArrayList<>();
        //章节最长标题长度
        int maxTitleLen = 0;
        //获取已缓存章节
        for (Chapter chapter : chapters) {
            if (ChapterService.isChapterCached(book.getId(), chapter.getTitle())) {
                catheChapters.add(chapter);
            } else {
                unCatheChapters.add(chapter);
            }
            if (maxTitleLen < chapter.getTitle().length()) {
                maxTitleLen = chapter.getTitle().length();
            }
        }
        Log.i(TAG, String.format("《%s》已缓存章节数量：%d，最大章节标题长度：%d",
                book.getName(), catheChapters.size(), maxTitleLen));
        if (catheChapters.size() <= 10) return false;
        //创建训练数据
        trainData = new double[catheChapters.size()][maxTitleLen + 1];
        //创建测试数据
        testData = new double[chapters.size() - catheChapters.size()][maxTitleLen + 1];
        //创建权重矩阵
        weights1 = new double[maxTitleLen + 1][1];
        //weights2 = new double[maxTitleLen + 1][1];
        //创建目标矩阵
        target = new double[catheChapters.size()][1];
        for (int i = 0; i < catheChapters.size(); i++) {
            Chapter chapter = catheChapters.get(i);
            char[] charArr = chapter.getTitle().replaceAll("[(（【{]", "").toCharArray();
            for (int j = 0; j < charArr.length; j++) {
                trainData[i][j] = charArr[j];
            }
            trainData[i][maxTitleLen] = 1;
            target[i][0] = ChapterService.countChar(book.getId(), chapter.getTitle());
        }
        for (int i = 0; i < maxTitleLen + 1; i++) {
            weights1[i][0] = rand.nextDouble();
            //weights2[i][0] = Math.random();
        }
        for (int i = 0; i < unCatheChapters.size(); i++) {
            Chapter chapter = unCatheChapters.get(i);
            char[] charArr = chapter.getTitle().toCharArray();
            for (int j = 0; j < charArr.length; j++) {
                testData[i][j] = charArr[j];
            }
            testData[i][maxTitleLen] = 1;
        }
        /*double[] tem = MatrixUtil.toVector(target);
        Arrays.sort(tem);
        median = tem[tem.length / 2 + 1];*/
        median = MatrixUtil.sum(target) / target.length;
        return true;
    }


    public static <T> void swap(T[] a, int i, int j) {
        T temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    public static <T> void shuffle(T[]... arr) {
        int length = arr[0].length;
        for (int i = length; i > 0; i--) {
            int randInd = rand.nextInt(i);
            for (T[] ts : arr) {
                swap(ts, randInd, i - 1);
            }
        }
    }
}
