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

package xyz.fycz.myreader.util.llog;

/**
 * @author luocan
 * @version 1.0
 *          </p>
 *          Created on 15/12/25.
 */
public class LMsg {
    public static final int INFO = 0x01;
    public static final int DEBUG = 0x02;
    public static final int ERROR = 0x03;
    public static final int WARNING = 0x04;

    protected String mTag;
    protected String mMsg;
    protected int mPriority;


    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        this.mTag = tag;
    }

    public String getMsg() {
        return mMsg;
    }

    public void setMsg(String msg) {
        this.mMsg = msg;
    }

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int priority) {
        this.mPriority = priority;
    }

}
