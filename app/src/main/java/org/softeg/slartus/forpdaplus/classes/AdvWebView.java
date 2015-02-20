package org.softeg.slartus.forpdaplus.classes;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.Calendar;

/**
 * User: slinkin
 * Date: 25.01.12
 * Time: 10:00
 */
public class AdvWebView extends WebView {
    private static final String TOPIC_BODY_KEY = "AdvWebView.TOPIC_BODY_KEY";
    private int actionBarHeight = 72;

    public AdvWebView(Context context) {
        super(context);
        init();
    }

    public AdvWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }


    private void init() {
        // gd = new GestureDetector(context, sogl);
        getSettings().setJavaScriptEnabled(true);

        getSettings().setJavaScriptCanOpenWindowsAutomatically(false);

        getSettings().setDomStorageEnabled(true);
        getSettings().setAllowFileAccess(true);
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        if (Build.VERSION.SDK_INT > 15) {
            getSettings().setAllowFileAccessFromFileURLs(true); //Maybe you don't need this rule
            getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT < 18)
            getSettings().setPluginState(WebSettings.PluginState.ON);// для воспроизведения видео


        if (Preferences.System.isShowWebViewScroll()) {
            setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            setScrollbarFadingEnabled(false);
        }

        setBackgroundColor(App.getInstance().getThemeStyleWebViewBackground());
        loadData("<html><head></head><body bgcolor=" + App.getInstance().getCurrentThemeName() + "></body></html>", "text/html", "UTF-8");
    }

    private Point m_LastMotionEvent = null;

    public Point getLastMotionEvent() {
        return m_LastMotionEvent;
    }

    private OnScrollChangedCallback mOnScrollChangedCallback;


    public void setActionBarheight(int actionBarHeight) {
        this.actionBarHeight = Math.max(actionBarHeight, 72);
    }

    private int mRawY = 0;

    /**
     * @param l    Current horizontal scroll origin
     * @param t    Current vertical scroll origin.
     * @param oldl Previous horizontal scroll origin.
     * @param oldt Previous vertical scroll origin.
     */
    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        try {
            if (!m_ActionBarOnScrollEventsState) return;
            if (mOnScrollChangedCallback == null) return;
            int k = t - oldt;

            mRawY = Math.min(actionBarHeight, k + mRawY);
            mRawY = Math.max(-actionBarHeight, mRawY);
            mRawY = Math.min(getScrollY(), mRawY);
            if (mRawY == actionBarHeight)
                mOnScrollChangedCallback.onScrollDown(m_InTouch);
            else if (mRawY == -actionBarHeight || t <= actionBarHeight)
                mOnScrollChangedCallback.onScrollUp(m_InTouch);
        } catch (Throwable ex) {
            AppLog.e(getContext(), ex);
        }
    }

    public void setOnScrollChangedCallback(final OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    private static final int MAX_CLICK_DURATION = 200;
    private static final int MAX_TOUCH__Y_DISTANCE = 30;
    private long startClickTime;
    private Boolean m_InTouch = false;
    private Point m_MotionDown = null;
    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        Boolean b = super.onTouchEvent(event);
        try {

            if (mOnScrollChangedCallback == null) return b;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    m_MotionDown = new Point((int) event.getX(), (int) event.getY());
                    m_InTouch = true;
                    final WebView.HitTestResult hitTestResult = getHitTestResult();
                    if (hitTestResult == null) {
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                    } else
                        switch (hitTestResult.getType()) {
                            case WebView.HitTestResult.UNKNOWN_TYPE:
                            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                                startClickTime = Calendar.getInstance().getTimeInMillis();
                        }

                    break;
                }
                case MotionEvent.ACTION_UP: {
                    m_InTouch = false;
                    long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                    if (clickDuration < MAX_CLICK_DURATION &&m_MotionDown!=null&& Math.abs(m_MotionDown.y - event.getY()) < MAX_TOUCH__Y_DISTANCE) {
                        mOnScrollChangedCallback.onTouch();
                    }
                }
            }
        } catch (Throwable ex) {
            AppLog.e(getContext(), ex);
        }finally {
            m_LastMotionEvent = new Point((int) event.getX(), (int) event.getY());
        }

        return b;

    }

    @Override
    public android.webkit.WebBackForwardList saveState(Bundle outState) {
        return super.saveState(outState);
    }

    @Override
    public android.webkit.WebBackForwardList restoreState(Bundle outState) {
        return super.restoreState(outState);
    }

    private Boolean m_ActionBarOnScrollEventsState = true;

    public void offActionBarOnScrollEvents() {
        m_ActionBarOnScrollEventsState = false;
    }

    public void onActionBarOnScrollEvents() {
        m_ActionBarOnScrollEventsState = true;
    }

    public void scrollTo(String fragment) {
        evalJs("scrollToElement('" + fragment + "');");
    }

    /**
     * Impliment in the activity/fragment/view that you want to listen to the webview
     */
    public static interface OnScrollChangedCallback {
        public void onScrollDown(Boolean inTouch);

        public void onScrollUp(Boolean inTouch);

        public void onTouch();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void evalJs(String js) {
        try {
            if (Build.VERSION.SDK_INT >= 19 && Preferences.System.isEvaluateJavascriptEnabled()) {
                evaluateJavascript(js, null);
            } else {
                loadUrl("javascript:" + js);
            }
        } catch (IllegalStateException ex) {
            android.util.Log.e("AdvWebView", ex.toString());
            Preferences.System.setEvaluateJavascriptEnabled(false);
        } catch (Throwable ex) {
            android.util.Log.e("AdvWebView", ex.toString());
        }
    }

}
