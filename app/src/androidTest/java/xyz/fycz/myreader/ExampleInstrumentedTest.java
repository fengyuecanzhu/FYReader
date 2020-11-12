package xyz.fycz.myreader;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;


import xyz.fycz.myreader.model.audio.ReadService;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("xyz.fycz.myreader", appContext.getPackageName());
    }

    @Test
    public void test() {
        ReadService readService = new ReadService();
        readService.initSynthesizer();
        readService.startSynthesizer("科幻电影是我们从小就爱看的电影题材，我们也都梦想过生活在那样的科幻世界里。科幻片，顾名思义即“科学幻想片”，是“以科学幻想为内容的故事片，其基本特点是从今天已知的科学原理和科学成就出发，对未来的世界或遥远的过去的情景作幻想式的描述。”");
    }

}