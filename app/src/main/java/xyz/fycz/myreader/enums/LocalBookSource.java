/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.enums;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.util.ToastUtils;

/**
 * 小说源
 */

public enum LocalBookSource {
    local("本地书籍"),
    fynovel("风月小说"),
    tianlai(App.getApplication().getString(R.string.read_tianlai)),
    biquge44(App.getApplication().getString(R.string.read_biquge44)),
    //pinshu(App.getApplication().getString(R.string.read_pinshu)),
    biquge(App.getApplication().getString(R.string.read_biquge)),
    qb5(App.getApplication().getString(R.string.read_qb5)),
    miqu(App.getApplication().getString(R.string.read_miqu)),
    jiutao(App.getApplication().getString(R.string.read_jiutao)),
    miaobi(App.getApplication().getString(R.string.read_miaobi)),
    dstq(App.getApplication().getString(R.string.read_dstq)),
    yunzhong(App.getApplication().getString(R.string.read_yunzhong)),
    sonovel(App.getApplication().getString(R.string.read_sonovel)),
    quannovel(App.getApplication().getString(R.string.read_quannovel)),
    //qiqi(App.getApplication().getString(R.string.read_qiqi)),
    xs7(App.getApplication().getString(R.string.read_xs7)),
    du1du(App.getApplication().getString(R.string.read_du1du)),
    paiotian(App.getApplication().getString(R.string.read_paiotian)),
    laoyao(App.getApplication().getString(R.string.read_laoyao)),
    xingxing(App.getApplication().getString(R.string.read_xingxing)),
    shiguang(App.getApplication().getString(R.string.read_shiguang)),
    //rexue(App.getApplication().getString(R.string.read_rexue)),
    chuanqi(App.getApplication().getString(R.string.read_chuanqi)),
    xiagu(App.getApplication().getString(R.string.read_xiagu)),
    hongchen(App.getApplication().getString(R.string.read_hongchen)),
    bijian(App.getApplication().getString(R.string.read_bijian)),
    yanqinglou(App.getApplication().getString(R.string.read_yanqinglou)),
    wolong(App.getApplication().getString(R.string.read_wolong)),
    ewenxue(App.getApplication().getString(R.string.read_ewenxue)),
    shuhaige(App.getApplication().getString(R.string.read_shuhaige)),
    luoqiu(App.getApplication().getString(R.string.read_luoqiu)),
    zw37(App.getApplication().getString(R.string.read_zw37)),
    xbiquge(App.getApplication().getString(R.string.read_xbiquge)),
    zaishuyuan(App.getApplication().getString(R.string.read_zaishuyuan)),
    chaoxing(App.getApplication().getString(R.string.read_chaoxing)),
    zuopin(App.getApplication().getString(R.string.read_zuopin)),
    cangshu99(App.getApplication().getString(R.string.read_cangshu99)),
    ben100(App.getApplication().getString(R.string.read_ben100));
    //liulangcat("流浪猫·实体"),

    public String text;

    LocalBookSource(String text) {
        this.text = text;
    }

    public static String getFromName(String name){
        for (LocalBookSource bookSource : LocalBookSource.values()){
            if (bookSource.text.equals(name)){
                return bookSource.toString();
            }
        }
        return "";
    }

    public static LocalBookSource get(int var0) {
        return values()[var0];
    }

    public static LocalBookSource fromString(String string) {
        try {
            return valueOf(string);
        } catch (Exception e) {
            ToastUtils.showError(e.getLocalizedMessage());
            return fynovel;
        }
    }

}
