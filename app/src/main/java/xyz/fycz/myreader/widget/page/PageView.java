package xyz.fycz.myreader.widget.page;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.gyf.immersionbar.ImmersionBar;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.entity.Setting;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.greendao.service.ChapterService;
import xyz.fycz.myreader.util.utils.SnackbarUtils;
import xyz.fycz.myreader.webapi.crawler.base.ReadCrawler;
import xyz.fycz.myreader.widget.animation.*;
import xyz.fycz.myreader.widget.page2.TxtLine;

/**
 * Created by Administrator on 2016/8/29 0029.
 * 原作者的GitHub Project Path:(https://github.com/PeachBlossom/treader)
 * 绘制页面显示内容的类
 */
public class PageView extends View {

    private final static String TAG = "BookPageWidget";

    private int mViewWidth = 0; // 当前View的宽
    private int mViewHeight = 0; // 当前View的高
    private int statusBarHeight = 0; //状态栏高度

    private int mStartX = 0;
    private int mStartY = 0;
    private boolean isMove = false;
    // 初始化参数
    private int mBgColor = 0xFFCEC29C;
    private PageMode mPageMode = PageMode.COVER;
    // 是否允许点击
    private boolean canTouch = true;
    // 唤醒菜单的区域
    private RectF mCenterRect = null;
    private boolean isPrepare;
    // 动画类
    private PageAnimation mPageAnim;
    // 动画监听类
    private PageAnimation.OnPageChangeListener mPageAnimListener = new PageAnimation.OnPageChangeListener() {
        @Override
        public boolean hasPrev() {
            return PageView.this.hasPrevPage();
        }

        @Override
        public boolean hasNext() {
            return PageView.this.hasNextPage();
        }

        @Override
        public void pageCancel() {
            PageView.this.pageCancel();
        }
    };

    //点击监听
    private TouchListener mTouchListener;
    //内容加载器
    private PageLoader mPageLoader;


    //文字选择画笔
    private Paint mTextSelectPaint = null;
    //文字选择画笔颜色
    private int TextSelectColor = Color.parseColor("#7787CEFA");
    private Path mSelectTextPath = new Path();
    // 是否发触了长按事件
    private boolean isLongPress = false;
    //第一个选择的文字
    private TxtChar firstSelectTxtChar = null;
    //最后选择的一个文字
    private TxtChar lastSelectTxtChar = null;
    //选择模式
    private SelectMode selectMode = SelectMode.Normal;
    //文本高度
    private float textHeight = 0;
    //长按的runnable
    private Runnable mLongPressRunnable;
    //长按时间
    private static final int LONG_PRESS_TIMEOUT = 800;
    //选择的列
    private List<TxtLine> mSelectLines = new ArrayList<>();

    public PageView(Context context) {
        this(context, null);
    }

    public PageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        statusBarHeight = ImmersionBar.getStatusBarHeight((Activity) getContext());
    }
    private void init() {
        //初始化画笔
        mTextSelectPaint = new Paint();
        mTextSelectPaint.setAntiAlias(true);
        mTextSelectPaint.setTextSize(19);
        mTextSelectPaint.setColor(TextSelectColor);

        mLongPressRunnable = () -> {
            if (mPageLoader == null) return;
            performLongClick();
            if (mStartX > 0 && mStartY > 0) {// 说明还没释放，是长按事件
                isLongPress = true;//长按
                TxtChar p = mPageLoader.detectPressTxtChar(mStartX, mStartY);//找到长按的点
                firstSelectTxtChar = p;//设置开始位置字符
                lastSelectTxtChar = p;//设置结束位置字符
                selectMode = SelectMode.PressSelectText;//设置模式为长按选择
                mTouchListener.onLongPress();//响应长按事件，供上层调用
            }
        };
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;

        isPrepare = true;

        if (mPageLoader != null) {
            mPageLoader.prepareDisplay(w, h);
        }
    }

    //设置翻页的模式
    void setPageMode(PageMode pageMode) {
        mPageMode = pageMode;
        //视图未初始化的时候，禁止调用
        if (mViewWidth == 0 || mViewHeight == 0) return;

        switch (mPageMode) {
            case SIMULATION:
                mPageAnim = new SimulationPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case COVER:
                mPageAnim = new CoverPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case SLIDE:
                mPageAnim = new SlidePageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case VERTICAL_COVER:
                mPageAnim = new CoverVerticalPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case NONE:
                mPageAnim = new NonePageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            case SCROLL:
                mPageAnim = new ScrollPageAnim(mViewWidth, mViewHeight, 0,
                        mPageLoader.getMarginTop(), mPageLoader.getMarginBottom(),this, mPageAnimListener);
                break;
            case AUTO:
                mPageAnim = new AutoPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
                break;
            default:
                mPageAnim = new SimulationPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener);
        }
    }

    public Bitmap getNextBitmap() {
        if (mPageAnim == null) return null;
        return mPageAnim.getNextBitmap();
    }

    public int getStatusBarHeight() {
        return statusBarHeight;
    }

    public Bitmap getBgBitmap() {
        if (mPageAnim == null) return null;
        return mPageAnim.getBgBitmap();
    }

    public boolean autoPrevPage() {
        //滚动暂时不支持自动翻页
        if (mPageAnim instanceof ScrollPageAnim) {
            return false;
        } else {
            startPageAnim(PageAnimation.Direction.PRE);
            return true;
        }
    }

    public boolean autoNextPage() {
        if (mPageAnim instanceof ScrollPageAnim) {
            return false;
        } else {
            startPageAnim(PageAnimation.Direction.NEXT);
            return true;
        }
    }

    public void autoPageOnSpeedChange() {
        //是否正在执行动画
        abortAnimation();
        mPageLoader.noAnimationToPrePage();
    }

    private void startPageAnim(PageAnimation.Direction direction) {
        if (mTouchListener == null) return;
        //是否正在执行动画
        abortAnimation();
        if (direction == PageAnimation.Direction.NEXT) {
            int x = mViewWidth;
            int y = mViewHeight;
            //初始化动画
            mPageAnim.setStartPoint(x, y);
            //设置点击点
            mPageAnim.setTouchPoint(x, y);
            //设置方向
            Boolean hasNext = hasNextPage();

            mPageAnim.setDirection(direction);
            if (!hasNext) {
                return;
            }
        } else {
            int x = 0;
            int y = mViewHeight;
            if (mPageAnim instanceof VerticalPageAnim) {
                x = mViewWidth;
                y = 0;
            }
            //初始化动画
            mPageAnim.setStartPoint(x, y);
            //设置点击点
            mPageAnim.setTouchPoint(x, y);
            mPageAnim.setDirection(direction);
            //设置方向方向
            Boolean hashPrev = hasPrevPage();
            if (!hashPrev) {
                return;
            }
        }
        mPageAnim.startAnim();
        this.postInvalidate();
    }

    public void setBgColor(int color) {
        mBgColor = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制动画
        mPageAnim.draw(canvas);
        if (selectMode != SelectMode.Normal && !isRunning() && !isMove) {
            DrawSelectText(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (mPageAnim == null) return true;
        if (mPageLoader == null) return true;

        Paint.FontMetrics fontMetrics = mPageLoader.mTextPaint.getFontMetrics();
        textHeight = Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent);

        if (!canTouch && event.getAction() != MotionEvent.ACTION_DOWN) return true;

        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = x;
                mStartY = y;
                isMove = false;
                //
                if (SysManager.getSetting().isCanSelectText() && mPageLoader.getPageStatus() == PageLoader.STATUS_FINISH) {
                    postDelayed(mLongPressRunnable, LONG_PRESS_TIMEOUT);
                }

                //
                isLongPress = false;

                canTouch = mTouchListener.onTouch();

                if (!canTouch){
                    removeCallbacks(mLongPressRunnable);
                }

                mPageAnim.onTouchEvent(event);
                selectMode = SelectMode.Normal;

                mTouchListener.onTouchClearCursor();
                break;
            case MotionEvent.ACTION_MOVE:
                // 判断是否大于最小滑动值。
                int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                if (!isMove) {
                    isMove = Math.abs(mStartX - event.getX()) > slop || Math.abs(mStartY - event.getY()) > slop;
                }

                // 如果滑动了，则进行翻页。
                if (isMove) {
                    if (SysManager.getSetting().isCanSelectText()) {
                        removeCallbacks(mLongPressRunnable);
                    }
                    mPageAnim.onTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isMove) {
                    //设置中间区域范围
                    if (mCenterRect == null) {
                        mCenterRect = new RectF(mViewWidth / 5, mViewHeight / 3,
                                mViewWidth * 4 / 5, mViewHeight * 2 / 3);
                    }
                    if (SysManager.getSetting().isCanSelectText()) {
                        removeCallbacks(mLongPressRunnable);
                    }
                    //是否点击了中间
                    if (mCenterRect.contains(x, y)) {
                        if (firstSelectTxtChar == null) {
                            if (mTouchListener != null) {
                                mTouchListener.center();
                            }
                        } else {
                            if (mSelectTextPath != null) {//长安选择删除选中状态
                                if (!isLongPress) {
                                    firstSelectTxtChar = null;
                                    mSelectTextPath.reset();
                                    invalidate();
                                }
                            }
                            //清除移动选择状态
                        }
                        return true;
                    }
                }
                if (firstSelectTxtChar == null || isMove) {//长安选择删除选中状态
                    mPageAnim.onTouchEvent(event);
                } else {
                    if (!isLongPress) {
                        //释放了
                        if (LONG_PRESS_TIMEOUT != 0) {
                            removeCallbacks(mLongPressRunnable);
                        }
                        firstSelectTxtChar = null;
                        mSelectTextPath.reset();
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 判断是否存在上一页
     *
     * @return
     */
    private boolean hasPrevPage() {
        mTouchListener.prePage();
        boolean hasPrevPage = mPageLoader.prev();
        if (!hasPrevPage) {
            showSnackBar("已经是第一页了");
        }
        return hasPrevPage;
    }

    /**
     * 判断是否下一页存在
     *
     * @return
     */
    private boolean hasNextPage() {
        boolean hasNextPage = mPageLoader.next();
        mTouchListener.nextPage(hasNextPage);
        if (!hasNextPage) {
            showSnackBar("已经是最后一页了");
        }
        return hasNextPage;
    }

    private void pageCancel() {
        mTouchListener.cancel();
        mPageLoader.pageCancel();
    }

    /**
     * 显示tips
     *
     * @param msg
     */
    public void showSnackBar(String msg) {
        SnackbarUtils.show(this, msg);
    }


    @Override
    public void computeScroll() {
        //进行滑动
        mPageAnim.scrollAnim();
        super.computeScroll();
    }

    //如果滑动状态没有停止就取消状态，重新设置Anim的触碰点
    public void abortAnimation() {
        mPageAnim.abortAnim();
    }

    public boolean isRunning() {
        if (mPageAnim == null) {
            return false;
        }
        return mPageAnim.isRunning();
    }

    public boolean isPrepare() {
        return isPrepare;
    }

    public void setTouchListener(TouchListener mTouchListener) {
        this.mTouchListener = mTouchListener;
    }

    public void drawNextPage() {
        if (!isPrepare) return;

        if (mPageAnim instanceof HorizonPageAnim) {
            ((HorizonPageAnim) mPageAnim).changePage();
        }else if (mPageAnim instanceof VerticalPageAnim){
            ((VerticalPageAnim) mPageAnim).changePage();
        } else if (mPageAnim instanceof AutoPageAnim) {
            ((AutoPageAnim) mPageAnim).changePage();
        }
        mPageLoader.drawPage(getNextBitmap(), false);
    }

    /**
     * 绘制当前页。
     *
     * @param isUpdate
     */
    public void drawCurPage(boolean isUpdate) {
        if (!isPrepare) return;

        if (!isUpdate) {
            if (mPageAnim instanceof ScrollPageAnim) {
                ((ScrollPageAnim) mPageAnim).resetBitmap();
            }
        }

        mPageLoader.drawPage(getNextBitmap(), isUpdate);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPageAnim.abortAnim();
        mPageAnim.clear();

        mPageLoader = null;
        mPageAnim = null;
    }

    /**
     * 获取 PageLoader
     *
     * @param collBook
     * @return
     */
    public PageLoader getPageLoader(Book collBook, ReadCrawler mReadCrawler, Setting setting) {
        // 判是否已经存在
        if (mPageLoader != null) {
            return mPageLoader;
        }
        // 获取具体的加载器
        if ("本地书籍".equals(collBook.getType())) {
            mPageLoader = new LocalPageLoader(this, collBook, ChapterService.getInstance(), setting);
        } else {
            mPageLoader = new NetPageLoader(this, collBook, ChapterService.getInstance(), mReadCrawler, setting);
        }
        // 判断是否 PageView 已经初始化完成
        if (mViewWidth != 0 || mViewHeight != 0) {
            // 初始化 PageLoader 的屏幕大小
            mPageLoader.prepareDisplay(mViewWidth, mViewHeight);
        }

        return mPageLoader;
    }
    private void DrawSelectText(Canvas canvas) {
        if (selectMode == SelectMode.PressSelectText) {
            drawPressSelectText(canvas);
        } else if (selectMode == SelectMode.SelectMoveForward) {
            drawMoveSelectText(canvas);
        } else if (selectMode == SelectMode.SelectMoveBack) {
            drawMoveSelectText(canvas);
        }
    }


    private void drawPressSelectText(Canvas canvas) {
        if (lastSelectTxtChar != null) {// 找到了选择的字符
            mSelectTextPath.reset();
            mSelectTextPath.moveTo(firstSelectTxtChar.getTopLeftPosition().x, firstSelectTxtChar.getTopLeftPosition().y);
            mSelectTextPath.lineTo(firstSelectTxtChar.getTopRightPosition().x, firstSelectTxtChar.getTopRightPosition().y);
            mSelectTextPath.lineTo(firstSelectTxtChar.getBottomRightPosition().x, firstSelectTxtChar.getBottomRightPosition().y);
            mSelectTextPath.lineTo(firstSelectTxtChar.getBottomLeftPosition().x, firstSelectTxtChar.getBottomLeftPosition().y);
            canvas.drawPath(mSelectTextPath, mTextSelectPaint);
            getSelectData();
        }
    }


    public String getSelectStr() {

        if (mSelectLines.size() == 0) {
            return String.valueOf(firstSelectTxtChar.getChardata());
        }
        StringBuilder sb = new StringBuilder();
        for (TxtLine l : mSelectLines) {
            //Log.e("selectline", l.getLineData() + "");
            sb.append(l.getLineData());
        }

        return sb.toString();
    }


    private void drawMoveSelectText(Canvas canvas) {
        if (firstSelectTxtChar == null || lastSelectTxtChar == null)
            return;
        getSelectData();
        drawSelectLines(canvas);
    }

    List<TxtLine> mLinseData = null;

    private void getSelectData() {
        TxtPage txtPage = mPageLoader.curPage();
        if (txtPage != null) {
            mLinseData = txtPage.txtLists;

            Boolean Started = false;
            Boolean Ended = false;

            mSelectLines.clear();

            // 找到选择的字符数据，转化为选择的行，然后将行选择背景画出来
            for (TxtLine l : mLinseData) {

                TxtLine selectline = new TxtLine();
                selectline.setCharsData(new ArrayList<>());

                for (TxtChar c : l.getCharsData()) {
                    if (!Started) {
                        if (c.getIndex() == firstSelectTxtChar.getIndex()) {
                            Started = true;
                            selectline.getCharsData().add(c);
                            if (c.getIndex() == lastSelectTxtChar.getIndex()) {
                                Ended = true;
                                break;
                            }
                        }
                    } else {
                        if (c.getIndex() == lastSelectTxtChar.getIndex()) {
                            Ended = true;
                            if (!selectline.getCharsData().contains(c)) {
                                selectline.getCharsData().add(c);
                            }
                            break;
                        } else {
                            selectline.getCharsData().add(c);
                        }
                    }
                }

                mSelectLines.add(selectline);

                if (Started && Ended) {
                    break;
                }
            }
        }
    }

    public SelectMode getSelectMode() {
        return selectMode;
    }

    public void setSelectMode(SelectMode mCurrentMode) {
        this.selectMode = mCurrentMode;
    }

    private void drawSelectLines(Canvas canvas) {
        drawOaleSeletLinesBg(canvas);
    }

    public void clearSelect() {
        firstSelectTxtChar = null;
        lastSelectTxtChar = null;
        selectMode = SelectMode.Normal;
        mSelectTextPath.reset();
        invalidate();

    }

    //根据当前坐标返回文字
    public TxtChar getCurrentTxtChar(float x, float y) {
        return mPageLoader.detectPressTxtChar(x, y);
    }

    private void drawOaleSeletLinesBg(Canvas canvas) {// 绘制选中背景
        for (TxtLine l : mSelectLines) {
            if (l.getCharsData() != null && l.getCharsData().size() > 0) {

                TxtChar fistchar = l.getCharsData().get(0);
                TxtChar lastchar = l.getCharsData().get(l.getCharsData().size() - 1);

//                float fw = fistchar.getCharWidth();
//                float lw = lastchar.getCharWidth();

                RectF rect = new RectF(fistchar.getTopLeftPosition().x, fistchar.getTopLeftPosition().y,
                        lastchar.getTopRightPosition().x, lastchar.getBottomRightPosition().y);

                /*canvas.drawRoundRect(rect, fw / 4,
                        textHeight /4, mTextSelectPaint);*/
                canvas.drawRect(rect, mTextSelectPaint);
            }
        }
    }

    public TxtChar getFirstSelectTxtChar() {
        return firstSelectTxtChar;
    }

    public void setFirstSelectTxtChar(TxtChar firstSelectTxtChar) {
        this.firstSelectTxtChar = firstSelectTxtChar;
    }

    public TxtChar getLastSelectTxtChar() {
        return lastSelectTxtChar;
    }

    public void setLastSelectTxtChar(TxtChar lastSelectTxtChar) {
        this.lastSelectTxtChar = lastSelectTxtChar;
    }

    public float getTextHeight() {
        return textHeight;
    }

    public enum SelectMode {
        Normal, PressSelectText, SelectMoveForward, SelectMoveBack
    }
    public interface TouchListener {
        boolean onTouch();

        void center();

        void prePage();

        void nextPage(boolean hasNextChange);

        void cancel();

        void onTouchClearCursor();

        void onLongPress();
    }
}
