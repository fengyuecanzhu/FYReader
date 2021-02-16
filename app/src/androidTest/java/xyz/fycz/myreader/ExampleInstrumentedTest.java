package xyz.fycz.myreader;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;


import java.io.IOException;

import xyz.fycz.myreader.model.source.MatcherAnalyzer;
import xyz.fycz.myreader.util.utils.OkHttpUtils;

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
        try {
            String str = OkHttpUtils.getHtml("https://www.xiaobiquge.com/29/29787/101469886.html", "gbk");
            MatcherAnalyzer analyzer = new MatcherAnalyzer();
            String rule = "<div class=\"logo\"><text></div>##@e(<text>,小笔趣阁){@a(<text> + 风月读书)}";
            /*List<String> list = analyzer.matcherInnerString(rule, str);
            for (String s : list) {
                System.out.println(s);
            }*/
            System.out.println(analyzer.getInnerText(rule, str));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}