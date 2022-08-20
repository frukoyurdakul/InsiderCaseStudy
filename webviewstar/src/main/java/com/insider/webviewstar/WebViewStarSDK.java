package com.insider.webviewstar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

/**
 * The core SDK that is responsible for attaching the provided WebView
 * to give functions such as:
 *
 * 1) Displaying the {@link #URL},
 * 2) Adding a small star,
 * 3) Adding a big star,
 * 4) Resetting the stars.
 *
 * The existing alive SDK can be accessed via {@link #getInstance()} and
 * the SDK can be created with a WebView passed using {@link #createInstance(WebView)}.
 *
 * The SDK is configured to destroy itself once a new instance is created
 * using {@link #createInstance(WebView)}, or after the WebView itself
 * is detached from window.
 *
 * Public functions are exposed to manage the WebView JS star array
 * manipulations. The console logs can be checked using "onConsoleMessage"
 * via Logcat.
 *
 * @see #addSmallStar()
 * @see #addBigStar()
 * @see #reset()
 */
public class WebViewStarSDK {

    private static final String URL = "https://img.etimg.com/thumb/msid-72948091,width-650,imgsize-95069,,resizemode-4,quality-100/star_istock.jpg";
    private static final String TAG = WebViewStarSDK.class.getSimpleName();

    private static final Object sLock = new Object();

    /**
     * This static instance should not create a memory leak, due to the existing
     * instance being destroyed with {@link #destroy()}
     * being called after the WebView is detached from window.
     * It will also destroy itself if another instance is
     * created using {@link #createInstance(WebView)}.
     */
    @SuppressLint("StaticFieldLeak")
    private static WebViewStarSDK sInstance;

    private final Application application;
    private final StarWebViewClient webViewClient;
    private final WebViewFunctionManager functionManager;

    @Nullable
    private WebView webView;

    private boolean isDestroyed = false;

    private WebViewStarSDK(@NonNull WebView webView) {
        this.webView = webView;
        this.application = (Application) webView.getContext().getApplicationContext();

        functionManager = new WebViewFunctionManager(webView);
        webViewClient = new StarWebViewClient(() ->
                JavascriptInitializer.initialize(webView, functionManager));
        init();

        synchronized (sLock) {
            sInstance = this;
        }
    }

    /**
     * Gets the existing alive instance if there is one.
     *
     * @return the existing alive instance if there is one.
     */
    @Nullable
    static WebViewStarSDK getInstance() {
        synchronized (sLock) {
            if (sInstance != null && !sInstance.isDestroyed)
                return sInstance;
            else
                return null;
        }
    }

    /**
     * Creates an instance (destroys the previous instance if there is one)
     * using the WebView provided.
     *
     * @param webView the web view inside the layout, or in code.
     * @return the SDK.
     */
    public static WebViewStarSDK createInstance(WebView webView) {
        if (getInstance() != null)
            getInstance().destroy();
        return new WebViewStarSDK(webView);
    }

    /**
     * Determines whether the SDK was destroyed, a.k.a {@link #destroy()} was
     * called.
     *
     * @return if the SDK is destroyed.
     */
    boolean isDestroyed() {
        return isDestroyed;
    }

    /**
     * Adds a small star, once the SDK and WebView is available,
     * a.k.a when the URL is loaded successfully and the JS
     * was injected correctly.
     */
    public void addSmallStar() {
        functionManager.call("addSmallStar");
    }

    /**
     * Adds a big star, once the SDK and WebView is available,
     * a.k.a when the URL is loaded successfully and the JS
     * was injected correctly.
     */
    public void addBigStar() {
        functionManager.call("addBigStar");
    }

    /**
     * Resets the stars (a.k.a clears the data), once the SDK and WebView is available,
     * a.k.a when the URL is loaded successfully and the JS
     * was injected correctly.
     */
    public void reset() {
        functionManager.call("reset");
    }

    /**
     * Initializes the SDK.
     */
    private void init() {
        StarsNotificationPoster.initialize(application);

        // Cancel here too since it might post a notification between 5 seconds.
        StarsNotificationWorker.cancelWorker(application);

        initializeWebView();
        initializeLifecycleCallbacks();
    }

    /**
     * Initializes the WebView.
     */
    private void initializeWebView() {
        if (webView != null) {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.addJavascriptInterface(new StarsManager(webView.getContext().getApplicationContext(),
                    functionManager), "starsManager");
            webView.setWebViewClient(webViewClient);
            webView.setWebChromeClient(new WebChromeClient()
            {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.i(TAG, "onConsoleMessage: " + consoleMessage.message());
                    return true;
                }
            });
            doOnAttach(() -> webView.loadUrl(URL));
        }
    }

    /**
     * Initializes the lifecycle callbacks. By default, the app will be
     * considered as foreground at initialization.
     */
    private void initializeLifecycleCallbacks() {
        application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksAdapter() {

            private int foregroundCount = 0;
            private boolean isForeground = true;

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                foregroundCount++;
                if (foregroundCount > 0 && !isForeground) {
                    isForeground = true;
                    onForeground();
                }
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                foregroundCount = Math.min(0, foregroundCount - 1);
                if (foregroundCount == 0 && isForeground) {
                    isForeground = false;
                    onBackground();
                }
            }
        });
    }

    /**
     * Called when the app goes to background.
     */
    private void onBackground() {
        StarsNotificationWorker.createWorker(application);
    }

    /**
     * Called when the app goes to foreground.
     * By default, the app will be considered as at foreground at initialization
     * state.
     */
    private void onForeground() {
        StarsNotificationWorker.cancelWorker(application);
        StarsNotificationPoster.cancelNotification(application);
    }

    /**
     * Destroys the SDK.
     *
     * If called multiple times, it has no effect.
     */
    private void destroy() {
        if (isDestroyed)
            return;

        isDestroyed = true;
        webViewClient.onDestroy();
        if (webView != null) {
            webView.setWebViewClient(null);
            webView.setWebChromeClient(null);
            webView.removeJavascriptInterface("starsManager");
            webView.loadUrl("about:blank");
        }
        webView = null;
    }

    /**
     * Performs the task after the {@link #webView} is attached to window.
     *
     * @param task the task to be run.
     */
    private void doOnAttach(@NonNull Runnable task) {
        if (webView == null)
            return;

        if (ViewCompat.isAttachedToWindow(webView)) {
            task.run();
        } else {
            webView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View view) {
                    task.run();
                }

                @Override
                public void onViewDetachedFromWindow(View view) {
                    // RecyclerView or any type of adapter view case could be
                    // handled here using a getParent() loop by determining
                    // the parent if necessary. Currently, it is not handled.
                    destroy();
                    view.removeOnAttachStateChangeListener(this);
                }
            });
        }
    }
}