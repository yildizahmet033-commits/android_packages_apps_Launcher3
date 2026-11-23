/*
 * Compatibility helper for the Infinity X Launcher3 (Android 17) on Evolution X.
 * Android 17 removed this class from Launcher3; DrawableFactory and AppDatabase
 * still use it. Same behaviour as the Android 16 version: lazily creates the
 * object on the main thread and keeps a single instance.
 */
package com.android.launcher3.util.override;

import static com.android.launcher3.util.Executors.MAIN_EXECUTOR;

import android.content.Context;
import android.os.Looper;

import java.util.concurrent.ExecutionException;

public class MainThreadInitializedObject<T> {

    private final ObjectProvider<T> mProvider;
    private volatile T mValue;

    public MainThreadInitializedObject(ObjectProvider<T> provider) {
        mProvider = provider;
    }

    public T get(Context context) {
        if (mValue == null) {
            Context app = context.getApplicationContext();
            if (Looper.myLooper() == Looper.getMainLooper()) {
                synchronized (this) {
                    if (mValue == null) {
                        mValue = mProvider.get(app);
                    }
                }
            } else {
                try {
                    return MAIN_EXECUTOR.submit(() -> get(app)).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return mValue;
    }

    /** Creates an object whose class name is read from a string resource. */
    public static <T extends ResourceBasedOverride> MainThreadInitializedObject<T> forOverride(
            Class<T> clazz, int resourceId) {
        return new MainThreadInitializedObject<>(
                c -> ResourceBasedOverride.Overrides.getObject(clazz, c, resourceId));
    }

    public interface ObjectProvider<T> {
        T get(Context context);
    }
}
