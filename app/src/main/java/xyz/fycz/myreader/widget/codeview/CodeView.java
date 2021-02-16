package xyz.fycz.myreader.widget.codeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.webkit.WebView;

import xyz.fycz.myreader.R;


public class CodeView extends WebView {


    private final float minFontSize = 4;
    private String code = "";
    private String escapeCode;
    private Language language;
    private float fontSize = 14;
    private ScaleGestureDetector pinchDetector;
    private boolean zoomEnabled = false;

    public CodeView(Context context) {
        this(context, null);
    }

    public CodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //Inicialização.
        init(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isZoomEnabled()) {
            pinchDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    private void init(Context context, AttributeSet attrs) {
        setBackgroundColor(Color.parseColor("#474949"));
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CodeView, 0, 0);
        //Define os atributos
        setFontSize(attributes.getInt(R.styleable.CodeView_cv_font_size, 12));
        setZoomEnabled(attributes.getBoolean(R.styleable.CodeView_cv_zoom_enable, false));
        attributes.recycle();

        pinchDetector = new ScaleGestureDetector(context, new PinchListener());

        getSettings().setJavaScriptEnabled(true);
        getSettings().setLoadWithOverviewMode(true);

    }


    /**
     * Obtém o tamanho da fonte do texto em pixels.
     */
    public float getFontSize() {
        return fontSize;
    }

    /**
     * Define o tamanho da fonte do texto em pixels.
     */
    public CodeView setFontSize(float fontSize) {
        if (fontSize < minFontSize) fontSize = minFontSize;
        this.fontSize = fontSize;
        return this;
    }

    /**
     * Obtém o código exibido.
     */
    public String getCode() {
        return code;
    }

    /**
     * Define o código que será exibido.
     */
    public CodeView setCode(String code) {
        if (code == null) code = "";
        this.code = code;
        this.escapeCode = Html.escapeHtml(code);
        return this;
    }

    /**
     * Obtém a linguagem.
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Define a linguagem.
     */
    public CodeView setLanguage(Language language) {
        this.language = language;
        return this;
    }

    /**
     * Verifica se o zoom está habilitado.
     */
    public boolean isZoomEnabled() {
        return zoomEnabled;
    }

    /**
     * Define que o zoom estará habilitado ou não.
     */
    public CodeView setZoomEnabled(boolean zoomEnabled) {
        this.zoomEnabled = zoomEnabled;
        return this;
    }


    /**
     * Aplica os atributos e exibe o código.
     */
    public void apply() {
        loadDataWithBaseURL("",
                toHtml(),
                "text/html",
                "UTF-8",
                "");
    }

    private String toHtml() {
        StringBuilder sb = new StringBuilder();
        //html
        sb.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n");
        //style
        sb.append("<link rel='stylesheet' href='file:///android_asset/highlightjs/rainbow.css' />\n");
        sb.append("<style>\n");
        //body
        sb.append("body {");
        sb.append("font-size:").append(String.format("%dpx;", (int) getFontSize()));
        sb.append("margin: 0px; line-height: 1.2;");
        sb.append("}\n");
        //.hljs
        sb.append(".hljs {");
        sb.append("}\n");
        //pre
        sb.append("pre {");
        sb.append("margin: 0px; position: relative;");
        sb.append("}\n");
        sb.append("</style>");
        //scripts
        sb.append("<script src='file:///android_asset/highlightjs/highlight.pack.js'></script>");
        sb.append("<script>hljs.initHighlightingOnLoad();</script>");
        sb.append("</head>");
        //code
        sb.append("<body>");
        sb.append("<pre><code class='").append(language.getLanguageName()).append("'>")
                .append(escapeCode)
                .append("</code></pre>\n");
        return sb.toString();
    }

    private void executeJavaScript(String js) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript("javascript:" + js, null);
        } else {
            loadUrl("javascript:" + js);
        }
    }

    private void changeFontSize(int sizeInPx) {
        executeJavaScript("document.body.style.fontSize = '" + sizeInPx + "px'");
    }

    /**
     * Eventos de pinça.
     */
    private class PinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float fontSize;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            fontSize = getFontSize();
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            CodeView.this.fontSize = fontSize;
            super.onScaleEnd(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            fontSize = getFontSize() * detector.getScaleFactor();
            if (fontSize >= minFontSize) {
                changeFontSize((int) fontSize);
            } else {
                fontSize = minFontSize;
            }
            return false;
        }
    }
}
