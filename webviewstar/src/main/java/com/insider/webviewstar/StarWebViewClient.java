package com.insider.webviewstar;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The WebView client that will be used for the WebView passed to
 * {@link WebViewStarSDK}. It is responsible for calling the {@link #callback}
 * once the page is successfully loaded.
 *
 * The callback will be triggered using a single thread executor, on
 * background thread.
 */
class StarWebViewClient extends WebViewClient {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Object mLock = new Object();

    private PageLoadedCallback callback;

    StarWebViewClient(PageLoadedCallback callback) {
        this.callback = callback;
    }

    /**
     * Called when the provided URL is successfully loaded.
     *
     * @param view the web view itself
     * @param url the URL that was loaded.
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        executor.execute(() -> {
            synchronized (mLock) {
                if (callback != null)  {
                    callback.onPageLoaded();
                }
                callback = null;
            }
        });
    }

    /**
     * Destroys the executor and nullifies the callback.
     */
    void onDestroy()
    {
        executor.shutdown();
        synchronized (mLock) {
            callback = null;
        }
    }
}