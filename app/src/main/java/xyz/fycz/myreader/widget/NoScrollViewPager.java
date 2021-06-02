package xyz.fycz.myreader.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * @author fengyue
 * @date 2021/6/2 19:57
 */
//禁止左右滑动的viewpager
public class NoScrollViewPager extends ViewPager {

    private boolean enableScroll = true;

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollViewPager(Context context) {
        super(context);
    }

    //调用此方法 参数为false 即可禁止滑动
    public void setEnableScroll(boolean noScroll) {
        this.enableScroll = noScroll;
    }

    @Override
    public void scrollTo(int x, int y) {
//        if(noScroll){  //加上判断无法用 setCurrentItem 方法切换
        super.scrollTo(x, y);
//        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (!enableScroll)
            return false;
        else
            return super.onTouchEvent(arg0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (!enableScroll)
            return false;
        else
            return super.onInterceptTouchEvent(arg0);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
    }

}