package com.insider.webviewstar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * The data manager that saves / clears Star data provided in "assets/starfunctions.js"
 * which is also passed to {@link StarsManager#onStarsChanged(String)} method
 * via a JavascriptInterface.
 *
 * It is solely responsible to save data to preferences or
 * read meaningful data from it.
 */
class StarsDataManager {

    private static final String TAG = StarsDataManager.class.getSimpleName();
    private static final String PREF_NAME = "app_stars";
    private static final String DATA = "data";

    private static final Object sLock = new Object();

    private static StarsDataManager INSTANCE;

    private final SharedPreferences preferences;

    public static StarsDataManager getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null)
                INSTANCE = new StarsDataManager(context);
            return INSTANCE;
        }
    }

    private StarsDataManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Clears the data inside shared preferences.
     */
    void clearData() {
        saveData("");
    }

    /**
     * Saves the data to shared preferences. Uses {@link SharedPreferences.Editor#commit()}
     * if called from a non-UI (or background) thread.
     *
     * @param data is the data to be saved
     */
    @SuppressLint("ApplySharedPref")
    void saveData(String data) {
        // If the calling thread is not in main thread, then save
        // preferences with commit, otherwise save with apply.
        if (Looper.myLooper() == Looper.getMainLooper()) {
            preferences.edit().putString(DATA, data).apply();
        } else {
            preferences.edit().putString(DATA, data).commit();
        }
    }

    /**
     * Returns the existing data inside preferences.
     */
    String getData() {
        return preferences.getString(DATA, "");
    }

    /**
     * Returns the star count. If there is no data, or the data could not be
     * parsed, returns 0.
     */
    int getStarCount() {
        final String data = getData();
        if (TextUtils.isEmpty(data))
            return 0;
        else {
            try {
                return new JSONArray(data).length();
            } catch (JSONException e) {
                Log.e(TAG, "getStarCount: Failed to parse JSON: " + data, e);
                return 0;
            }
        }
    }
}