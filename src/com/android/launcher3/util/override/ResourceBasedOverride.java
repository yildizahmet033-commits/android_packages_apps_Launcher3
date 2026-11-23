/*
 * Compatibility helper for the Infinity X Launcher3 (Android 17) on Evolution X.
 * Android 17 removed this interface from Launcher3. Same behaviour as the
 * Android 16 version: the implementation class name comes from a string resource.
 */
package com.android.launcher3.util.override;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;

public interface ResourceBasedOverride {

    class Overrides {

        private static final String TAG = "Overrides";

        public static <T extends ResourceBasedOverride> T getObject(
                Class<T> clazz, Context context, int resId) {
            String className = context.getString(resId);
            if (!TextUtils.isEmpty(className)) {
                try {
                    Class<?> cls = Class.forName(className);
                    return (T) cls.getDeclaredConstructor(Context.class).newInstance(context);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                        | ClassCastException | NoSuchMethodException | InvocationTargetException e) {
                    Log.e(TAG, "Bad overriden class", e);
                }
            }
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
