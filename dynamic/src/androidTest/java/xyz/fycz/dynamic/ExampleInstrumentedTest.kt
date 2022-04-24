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

package xyz.fycz.dynamic

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.fycz.maple.MapleUtils

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import xyz.fycz.myreader.application.App

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun testFix() {
        // Context of the app under test.
        if (App.getVersionCode() == 243) {
            try {
                App243Fix.fixGetAllNoLocalSource()
            } catch (e: Exception) {
                MapleUtils.log(e)
            }
            try {
                App243Fix.fixAdTimeout()
            } catch (e: Exception) {
                MapleUtils.log(e)
            }
        }
    }
}