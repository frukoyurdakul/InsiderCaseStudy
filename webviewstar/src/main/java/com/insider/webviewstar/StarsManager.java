package com.insider.webviewstar;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * The manager that contains the JavascriptInterface attached
 * to the WebView. The addition of the method is done at
 * {@link WebViewStarSDK#initializeWebView()} method.
 *
 * If there is an existing data from the previous sessions,
 * upon initialization the data will be passed into
 * WebView's Javascript provided from {@link JavascriptInitializer},
 * allowing the state to be restored after a successful URL load operation.
 *
 * The {@link #onStarsChanged(String)} method is triggered if
 * a change is detected inside the JS methods that are provided
 * to WebView, or after the initial data pass if one exists already.
 */
class StarsManager {

    private static final String TAG = StarsManager.class.getSimpleName();

    private final Context context;
    private final WebViewFunctionManager functionManager;

    StarsManager(Context context, WebViewFunctionManager functionManager) {
        this.functionManager = functionManager;
        this.context = context;
        init();
    }

    /**
     * Initializes the first data and if not empty, passes it to WebView
     * so that the internal state of the WebView can be initialized
     * once the image is loaded.
     */
    private void init() {
        final String savedStars = StarsDataManager.getInstance(context).getData();
        if (!TextUtils.isEmpty(savedStars)) {
            functionManager.call("initializeStars", savedStars);
        }
    }

    /**
     * Called when there is a change inside the WebView JS star array.
     *
     * @param stars the stars in JSON format.
     */
    @JavascriptInterface
    public void onStarsChanged(String stars) {
        Log.d("WebViewStarSDK", "onStarsChanged: " + stars);
        StarsDataManager.getInstance(context).saveData(stars);
    }
}