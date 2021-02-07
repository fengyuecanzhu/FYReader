package xyz.fycz.myreader.enums;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.util.ToastUtils;

/**
 * 小说源
 */

public enum BookSource {
    local("本地书籍"),
    fynovel("风月小说"),
    tianlai(MyApplication.getApplication().getString(R.string.read_tianlai)),
    biquge44(MyApplication.getApplication().getString(R.string.read_biquge44)),
    //pinshu(MyApplication.getApplication().getString(R.string.read_pinshu)),
    biquge(MyApplication.getApplication().getString(R.string.read_biquge)),
    qb5(MyApplication.getApplication().getString(R.string.read_qb5)),
    miqu(MyApplication.getApplication().getString(R.string.read_miqu)),
    jiutao(MyApplication.getApplication().getString(R.string.read_jiutao)),
    miaobi(MyApplication.getApplication().getString(R.string.read_miaobi)),
    dstq(MyApplication.getApplication().getString(R.string.read_dstq)),
    yunzhong(MyApplication.getApplication().getString(R.string.read_yunzhong)),
    sonovel(MyApplication.getApplication().getString(R.string.read_sonovel)),
    quannovel(MyApplication.getApplication().getString(R.string.read_quannovel)),
    //qiqi(MyApplication.getApplication().getString(R.string.read_qiqi)),
    xs7(MyApplication.getApplication().getString(R.string.read_xs7)),
    du1du(MyApplication.getApplication().getString(R.string.read_du1du)),
    paiotian(MyApplication.getApplication().getString(R.string.read_paiotian)),
    laoyao(MyApplication.getApplication().getString(R.string.read_laoyao)),
    xingxing(MyApplication.getApplication().getString(R.string.read_xingxing)),
    shiguang(MyApplication.getApplication().getString(R.string.read_shiguang)),
    //rexue(MyApplication.getApplication().getString(R.string.read_rexue)),
    chuanqi(MyApplication.getApplication().getString(R.string.read_chuanqi)),
    xiagu(MyApplication.getApplication().getString(R.string.read_xiagu)),
    hongchen(MyApplication.getApplication().getString(R.string.read_hongchen)),
    bijian(MyApplication.getApplication().getString(R.string.read_bijian)),
    yanqinglou(MyApplication.getApplication().getString(R.string.read_yanqinglou)),
    wolong(MyApplication.getApplication().getString(R.string.read_wolong)),
    ewenxue(MyApplication.getApplication().getString(R.string.read_ewenxue)),
    shuhaige(MyApplication.getApplication().getString(R.string.read_shuhaige)),
    luoqiu(MyApplication.getApplication().getString(R.string.read_luoqiu)),
    zw37(MyApplication.getApplication().getString(R.string.read_zw37)),
    xbiquge(MyApplication.getApplication().getString(R.string.read_xbiquge)),
    zaishuyuan(MyApplication.getApplication().getString(R.string.read_zaishuyuan)),
    chaoxing(MyApplication.getApplication().getString(R.string.read_chaoxing)),
    zuopin(MyApplication.getApplication().getString(R.string.read_zuopin)),
    cangshu99(MyApplication.getApplication().getString(R.string.read_cangshu99)),
    ben100(MyApplication.getApplication().getString(R.string.read_ben100));
    //liulangcat("流浪猫·实体"),

    public String text;

    BookSource(String text) {
        this.text = text;
    }

    public static String getFromName(String name){
        for (BookSource bookSource : BookSource.values()){
            if (bookSource.text.equals(name)){
                return bookSource.toString();
            }
        }
        return "";
    }

    public static BookSource get(int var0) {
        return values()[var0];
    }

    public static BookSource fromString(String string) {
        try {
            return valueOf(string);
        } catch (Exception e) {
            ToastUtils.showError(e.getLocalizedMessage());
            return fynovel;
        }
    }

}
