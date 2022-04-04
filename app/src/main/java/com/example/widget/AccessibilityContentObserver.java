package com.example.widget;

import android.database.ContentObserver;
import android.os.Handler;

import java.util.HashMap;
import java.util.Map;

public class AccessibilityContentObserver extends ContentObserver {
    private final Map<Integer, AccessibilityChangeListener> mListeners = new HashMap<>();

    public AccessibilityContentObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        for (Map.Entry<Integer, AccessibilityChangeListener> entries : mListeners.entrySet()) {
            entries.getValue().onChange();
        }
    }

    void addListener(int key, AccessibilityChangeListener listener) {
        mListeners.put(key, listener);
    }

    void removeListener(int key) {
        mListeners.remove(key);
    }

    void clearListeners() {
        mListeners.clear();
    }

    private static final String TAG = "AccessContentObserver";
}
