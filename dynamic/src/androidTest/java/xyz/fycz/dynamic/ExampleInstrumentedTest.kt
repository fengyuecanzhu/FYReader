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