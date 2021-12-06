package cache;

import android.content.Context;
import android.content.SharedPreferences;

import cache.model.CacheGroup;
import common.Utils;


public class JsonCache {
    public static final String DEFAULT = "cache_api";
    public static final String DETAIL = "cache_detail";

    private static SharedPreferences getSharedPreferences(Context context, String key) {
        if(context == null)
            return null;
        return context.getSharedPreferences(key, Context.MODE_PRIVATE);
    }

    public static String getCacheNoCheckExpire(Context context, String fileName, String key) {
        try {
            if(fileName == null || key == null)
                return null;
            final SharedPreferences sp = getSharedPreferences(context, fileName);
            final String json = sp.getString(key, null);
            if(json != null) {
                final CacheGroup cache = Utils.GSON.fromJson(json, CacheGroup.class);
                return cache.content;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getCache(Context context, String fileName, String key) {
        try {
            if(context == null || fileName == null || key == null)
                return null;
            final SharedPreferences sp = getSharedPreferences(context, fileName);
            if(sp == null)
                return null;
            final String json = sp.getString(key, null);
            if(json != null) {
                final CacheGroup cache = Utils.GSON.fromJson(json, CacheGroup.class);
                if(cache != null && (System.currentTimeMillis() - cache.last < cache.expire
                        || !Utils.isNetworkAvailable(context)))
                    return cache.content;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void put(Context context, String fileName, String key, long expire, String json) {
        if(key == null || fileName == null)
            return;
        try {
            final SharedPreferences sp = getSharedPreferences(context, fileName);
            final CacheGroup cache = new CacheGroup(key);
            cache.expire = expire;
            cache.last = System.currentTimeMillis();
            cache.content = json;
            if(sp != null) {
                sp.edit().putString(key, Utils.GSON.toJson(cache))
                        .apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void remove(Context context, String fileName, String key) {
        try {
            if(fileName == null || key == null)
                return;
            final SharedPreferences sp = getSharedPreferences(context, fileName);
            sp.edit().remove(key).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
