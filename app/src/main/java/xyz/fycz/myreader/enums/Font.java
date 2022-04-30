/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.enums;

import xyz.fycz.myreader.common.URLCONST;

public enum Font {

    默认字体( "默认字体"),
    本地字体( "默认字体"),
    方正楷体( URLCONST.LAN_ZOU_URL + "/ilLFMe6kefe"),
    方正行楷( URLCONST.LAN_ZOU_URL + "/imFvne6keji"),
    经典宋体(URLCONST.LAN_ZOU_URL + "/idhI5e6keqf"),
    方正硬笔行书(URLCONST.LAN_ZOU_URL + "/ilVh6ep9xja"),
    包图小白体(URLCONST.LAN_ZOU_URL + "/i5qgAicrirc"),
    仓耳非白W02(URLCONST.LAN_ZOU_URL + "/iHwRnicriuf"),
    仓耳舒圆体W02(URLCONST.LAN_ZOU_URL + "/i3GVPicrj3e"),
    仓耳与墨W02(URLCONST.LAN_ZOU_URL + "/ivhv9icrj7i"),
    方正仿宋简体(URLCONST.LAN_ZOU_URL + "/iEcCHicrjef"),
    方正黑体简体(URLCONST.LAN_ZOU_URL + "/iw8kKicrjij"),
    方正书宋简体(URLCONST.LAN_ZOU_URL + "/i5976icrjmd"),
    品如手写体(URLCONST.LAN_ZOU_URL + "/iZccuicrjyf"),
    千图小兔体(URLCONST.LAN_ZOU_URL + "/iOONMicrkda"),
    手书体(URLCONST.LAN_ZOU_URL + "/iqbmdicrkvi"),
    演示春风楷(URLCONST.LAN_ZOU_URL + "/ioRJSicrldg"),
    演示秋鸿楷(URLCONST.LAN_ZOU_URL + "/i8qnzicrlsb"),
    演示夏行楷(URLCONST.LAN_ZOU_URL + "/iyYUTicrm6f"),
    演示悠然小楷(URLCONST.LAN_ZOU_URL + "/ikKq7icrmrg"),
    杨任东竹石体(URLCONST.LAN_ZOU_URL + "/iiWdVicrnbg"),
    站酷仓耳渔阳体(URLCONST.LAN_ZOU_URL + "/if5weicrnje"),
    迷你隶书( URLCONST.LAN_ZOU_URL + "/ihaXVe6kekj"),
    方正黄草(URLCONST.LAN_ZOU_URL + "/iQg67e6keed");

    public String downloadPath;

    Font(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public static Font get(int var0) {
        return values()[var0];
    }

    public static Font fromString(String string) {
        return Font.valueOf(string);
    }
}
