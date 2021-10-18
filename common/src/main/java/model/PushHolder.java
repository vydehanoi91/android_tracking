package model;

import android.content.Context;

import http.ApiAdapter;
import task.Callback;

public class PushHolder {
    public int id;
    public String path;
    public String token;
    public String[] queries;
    public String[] fields;
    public String[] headers;
    public String body;
    public Callback callback;
    public Catcher catcher;
    public String debugJson;
    public String cacheName;
    public String keyName;
    public boolean clearCache;
    public boolean loadCacheFirst;
    public long expireCacheTime = 0L;

    public static PushHolder get(int id) {
        final PushHolder holder = new PushHolder();
        holder.id = id;
        return holder;
    }

    public PushHolder callBack(Callback callback) {
        this.callback = callback;
        return this;
    }

    public PushHolder catcher(Catcher catcher) {
        this.catcher = catcher;
        return this;
    }

    public PushHolder path(String path) {
        this.path = path;
        return this;
    }

    public PushHolder expireCacheTime(long expireCacheTime) {
        this.expireCacheTime = expireCacheTime;
        return this;
    }

    public PushHolder keyName(String keyName) {
        this.keyName = keyName;
        return this;
    }

    public PushHolder debugJson(String debugJson) {
        this.debugJson = debugJson;
        return this;
    }

    public PushHolder cacheName(String cacheName) {
        this.cacheName = cacheName;
        return this;
    }

    public PushHolder clearCache(boolean clearCache) {
        this.clearCache = clearCache;
        return this;
    }

    public PushHolder loadCacheFirst(boolean loadCacheFirst) {
        this.loadCacheFirst = loadCacheFirst;
        return this;
    }

    public PushHolder token(String token) {
        this.token = token;
        return this;
    }

    public PushHolder queries(String... queries) {
        this.queries = queries;
        return this;
    }

    public PushHolder fields(String... fields) {
        this.fields = fields;
        return this;
    }

    public PushHolder headers(String... headers) {
        this.headers = headers;
        return this;
    }

    public PushHolder body(String body) {
        this.body = body;
        return this;
    }

    public String getUrl(Context context) {
        final ApiHolder ant = ApiAdapter.getHolder(context, id);
        if (ant == null)
            return null;
        String url = ant.baseUrl + ant.route;
        if (ant.path != null && path != null)
            url = url.replace(ant.path, path);
        if (ant.queries != null && ant.queries.length > 0) {
            for (int i = 0; i < ant.queries.length; i++) {
                final String value = queries[i];
                if (value != null) {
                    url += ((!url.contains("?") ? "?" : "&") + ant.queries[i]
                            + "=" + value);
                }
            }
        }
        return url;
    }
}
