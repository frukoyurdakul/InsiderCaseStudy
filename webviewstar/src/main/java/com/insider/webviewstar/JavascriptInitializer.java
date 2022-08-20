package com.insider.webviewstar;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The root JS initializer that reads the "assets/starfunctions.js"
 * and injects it into the WebView via the static
 * {@link #initialize(WebView, WebViewFunctionManager)} method,
 * which will be triggered if the WebView successfully loads the
 * URL provided to it.
 *
 * Reads the JS content via a {@link BufferedReader} using
 * try-with-resources and a StringBuilder so that the most
 * efficient way will be used. The {@link #initialize(WebView, WebViewFunctionManager)}
 * method will be called on a worker thread.
 */
class JavascriptInitializer {

    private static final String TAG = JavascriptInitializer.class.getSimpleName();

    /**
     * Initializes the javascript methods for the web view. Called
     * when the WebView is attached to window and a URL is loaded.
     *
     * This method is called on background thread to allow
     * direct access to javascript assets to be able to read
     * via input streams.
     *
     * @param webView the webView to inject JS to.
     * @param functionManager the function manager that this WebView is tied to.
     */
    static void initialize(WebView webView, WebViewFunctionManager functionManager) {
        final Context context = webView.getContext();
        String result;
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("starfunctions.js")))) {
            final StringBuilder builder = new StringBuilder();
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (!first)
                    builder.append('\n');
                builder.append(line);
                first = false;
            }
            result = builder.toString();
        } catch (IOException e) {
            Log.e(TAG, "initialize: Failed to read asset.", e);
            return;
        }
        functionManager.initialize(result);
    }
}