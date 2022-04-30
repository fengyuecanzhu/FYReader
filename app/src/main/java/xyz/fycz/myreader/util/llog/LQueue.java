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

package xyz.fycz.myreader.util.llog;

import java.util.concurrent.LinkedBlockingQueue;


/**
 * @author luocan
 * @version 1.0
 *          </p>
 *          Created on 15/12/25.
 */
public class LQueue {

    private final LinkedBlockingQueue<LMsg> mLogQueue = new LinkedBlockingQueue<LMsg>();
    private LExecutor logExecutor;


    public void add(LMsg logMsg) {
        try {
            mLogQueue.add(logMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void start() {
        stop();
        try {
            logExecutor = new LExecutor(mLogQueue);
            logExecutor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (logExecutor != null) {
                logExecutor.quit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
