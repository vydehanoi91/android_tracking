package http;

import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cache.JsonCache;
import fpt.vne.common.R;
import model.ApiGroup;
import model.ApiHolder;
import model.Catcher;
import model.PushHolder;
import model.ResponseHolder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import task.Action;
import task.Task;
import tracking.VnEAnalytics;

public class ApiAdapter {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddkkmmss", Locale.CHINA);
    private static OkHttpClient okHttpClient;
    private static SparseArray<ApiHolder> api;
    private static String appId;

    /*private static String getAppId(Context context) {
        try {
            if (appId == null)
                appId = context.getString(R.string.android_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appId;
    }*/

    public static ApiHolder getHolder(Context context, int id) {
        if (api == null || api.size() == 0) {
            String constants = "";
            final String json = VnEAnalytics.getRawData(context, R.raw.data_api);
            if (json == null || json.trim().length() < 10)
                return null;
            api = new SparseArray<>();
            final ApiGroup[] groups = VnEAnalytics.GSON.fromJson(json, ApiGroup[].class);
            for (ApiGroup group : groups) {
                final ApiHolder[] holders = group.holders;
                if (holders != null) {
                    for (ApiHolder h : holders) {
                        h.baseUrl = group.baseUrl;
                        String key = "public static final int "
                                + h.name.replaceAll("\\s+", "_").toUpperCase()
                                + " = " + h.id + ";\n";
                        constants += key;
                        api.put(h.id, h);
                    }
                }
            }
        }
        return api.get(id, null);
    }

    private static void request(final Context context, final PushHolder holder) {
        Task.submit(new Action() {
            String message = null;
            final String cacheName = holder.cacheName;
            final String keyName = holder.keyName;

            @Override
            public Object onRunning() {
                final ApiHolder ant = getHolder(context, holder.id);
                if (ant == null)
                    return null;
                try {
                    String url = ant.baseUrl + ant.route;
                    if (ant.path != null && holder.path != null)
                        url = url.replace(ant.path, holder.path);
                    MultipartBody.Builder body = null;
                    if (ant.queries != null && ant.queries.length > 0) {
                        for (int i = 0; i < ant.queries.length; i++) {
                            final String value = holder.queries[i];
                            if (value != null) {
                                url += ((!url.contains("?") ? "?" : "&") + ant.queries[i]
                                        + "=" + value);
                            }
                        }
                    }

                    if (holder.loadCacheFirst && context instanceof Activity) {
                        final String json = JsonCache.getCacheNoCheckExpire(context, cacheName, keyName);
                        if (json != null && holder.catcher != null) {
                            final ResponseHolder responseHolder = holder.catcher.getData(json);
                            if (responseHolder != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            holder.callback.onResponse(responseHolder.data, null);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    }

                    /*LogUtils.error("API", "==== START REQUEST =============================================");
                    LogUtils.error("API", "Method: " + (ant.isPost() ? "POST" : (ant.isPut() ? "PUT" : "GET")));
                    LogUtils.error("API", "Url: " + url);*/
                    if (ant.fields != null && ant.fields.length > 0 && (ant.isPost() || ant.isPut())) {
                        String logFields = "Fields: [";
                        body = new MultipartBody.Builder().setType(MultipartBody.FORM);
                        for (int i = 0; i < ant.fields.length; i++) {
                            logFields += (ant.fields[i] + "=" + holder.fields[i] + (i < ant.fields.length - 1 ? ", " : ""));
                            body.addFormDataPart(ant.fields[i], holder.fields[i]);
                        }
//                        LogUtils.error("API", logFields + "]");
                    }
                    final Request.Builder builder = new Request.Builder().url(url);
                    if (ant.type != null)
                        builder.addHeader(context.getString(R.string.key_content_type), ant.type);
                    if (ant.headers != null && holder.headers != null) {
                        for (int i = 0; i < holder.headers.length; i++)
                            builder.addHeader(ant.headers[i], holder.headers[i]);
                    }
                    if (ant.type != null && holder.body != null) {
                        if (ant.isPut())
                            builder.put(RequestBody.create(MediaType.parse(ant.type), holder.body));
                        if (ant.isPost())
                            builder.post(RequestBody.create(MediaType.parse(ant.type), holder.body));
                        /*LogUtils.error("API", "Content-type: " + ant.type);
                        LogUtils.error("API", "Body: " + holder.body);*/
                    }
                    if (body != null) {
                        if (ant.isPost()) {
                            builder.post(body.build());
                        } else {
                            builder.put(body.build());
                        }
                    } else if (holder.body == null && (ant.isPost() || ant.isPut())) {
                        builder.method(ant.method.toUpperCase(), RequestBody.create(null, new byte[0]));
                    }
                    if (okHttpClient == null) {
                        final OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
                        okHttpBuilder.connectTimeout(20, TimeUnit.SECONDS);
                        okHttpBuilder.readTimeout(10, TimeUnit.SECONDS);
                        okHttpBuilder.writeTimeout(10, TimeUnit.SECONDS);
                        okHttpClient = okHttpBuilder.build();
                    }

                    if (VnEAnalytics.isNetworkAvailable(context)) {
                        final String cache = holder.clearCache ? null : JsonCache.getCache(context, keyName, cacheName);
                        if (cache != null && holder.expireCacheTime > 0) {
                            if (holder.catcher != null) {
                                final ResponseHolder responseHolder = holder.catcher.getData(cache);
                                if (responseHolder != null) {
                                    if (responseHolder.message != null)
                                        message = responseHolder.message;
                                    return responseHolder.data;
                                }
                            }
                        }
                        if (ant.isPost() || ant.isPut()) {
                            final Response response = okHttpClient.newCall(builder.build()).execute();
                            if (response != null) {
                                if (response.isSuccessful()) {
                                    final ResponseBody responseBody = response.body();
                                    if (responseBody != null) {
                                        final String json = holder.debugJson != null
                                                ? holder.debugJson : responseBody.string();
                                        if (json != null && holder.expireCacheTime > 0)
                                            JsonCache.put(context, keyName, cacheName, holder.expireCacheTime, json);
                                        if (holder.catcher != null) {
                                            final ResponseHolder responseHolder = holder.catcher.getData(json);
                                            if (responseHolder != null) {
                                                if (responseHolder.message != null)
                                                    message = responseHolder.message;
                                                return responseHolder.data;
                                            }
                                        }
                                    } else {
                                        message = response.message();
                                        return getCacheResponse(cacheName);
                                    }
                                } else {
//                                    LogUtils.error("API", "Result: " + response.code());
                                    return getCacheResponse(cacheName);
                                }
                            } else {
                                return getCacheResponse(cacheName);
                            }
                        } else {
                            /*final String json = holder.debugJson != null
                                    ? holder.debugJson : VnEAnalytics.stringFromHttpGet(url);*/
                            final String json = holder.debugJson;
//                            LogUtils.error("API", "Result: " + json);
                            if (json != null && holder.expireCacheTime > 0)
                                JsonCache.put(context, keyName, cacheName, holder.expireCacheTime, json);
                            if (holder.catcher != null) {
                                final ResponseHolder responseHolder = holder.catcher.getData(json);
                                if (responseHolder != null) {
                                    if (responseHolder.message != null)
                                        message = responseHolder.message;
                                    return responseHolder.data;
                                }
                            }
                            return getCacheResponse(cacheName);
                        }
                    } else {
                        return getCacheResponse(cacheName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (cacheName != null)
                        return getCacheResponse(cacheName);
                }
                return null;
            }

            private Object getCacheResponse(String cacheName) {
                final String cache = JsonCache.getCache(context, cacheName, keyName);
                if (cache != null && holder.expireCacheTime > 0) {
                    if (holder.catcher != null) {
                        final ResponseHolder responseHolder = holder.catcher.getData(cache);
                        if (responseHolder != null) {
                            if (responseHolder.message != null)
                                message = responseHolder.message;
                            return responseHolder.data;
                        }
                    }
                }
                return null;
            }

            @Override
            public void onResponse(Object data) {
                try {
                    if (holder.callback != null)
                        holder.callback.onResponse(data, message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //@SuppressWarnings("unchecked")
    private static Catcher getCatcher(final String subKey, final Class clazz) {
        return new Catcher() {
            @Override
            public ResponseHolder getData(String json) {
                try {
                    if (json != null) {
                        final ResponseHolder holder = new ResponseHolder();
                        final JSONObject jsonObject = new JSONObject(json);
                        final boolean success = jsonObject.getInt("error") == 0;
                        if (clazz.equals(Boolean.class)) {
                            holder.data = success;
                        } else if (success && jsonObject.has("data")) {
                            final String jsonData;
                            if (subKey == null) {
                                jsonData = jsonObject.getString("data");
                            } else {
                                final JSONObject keyData = jsonObject.getJSONObject("data");
                                if (keyData.has(subKey)) {
                                    jsonData = keyData.getString(subKey);
                                    if (keyData.has("total"))
                                        holder.message = keyData.getString("total");
                                } else {
                                    jsonData = keyData.toString();
                                }
                            }
                            if (clazz.equals(String.class)) {
                                holder.data = jsonData;
                            } else {
                                holder.data = VnEAnalytics.GSON.fromJson(jsonData, clazz);
                            }
                        }
                        if (holder.message == null) {
                            if (jsonObject.has("total")) {
                                holder.message = jsonObject.getString("total");
                            } else if (jsonObject.has("message"))
                                holder.message = jsonObject.getString("message");
                        }
                        return holder;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    // TODO Api3 ===================================================================================

    /*public static void getHomeArticles(final Context context, boolean clearCache, final Callback<ResponseHome> callback) {
        request(context, PushHolder.get(RequestName.GET_HOME_ARTICLES)
                .clearCache(clearCache)
                .expireCacheTime(300000L)
                .cacheName("home")
                .keyName("home")
                .queries(DeviceUtils.getDeviceId(context), getAppId(context))
                .cacheName(ResponseHome.CACHE_NAME)
                .catcher(getCatcher(null, ResponseHome.class))
                .callBack(callback));
    }*/
}
