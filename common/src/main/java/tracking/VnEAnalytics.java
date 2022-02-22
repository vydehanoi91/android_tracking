package tracking;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import fpt.vne.common.R;

public final class VnEAnalytics {
    private static VnEAnalytics vnEAnalytics;
    private static Context mContext;
    private static final Object LOCK = new Object();
    private static String appId, os;
    private static boolean debugMode = false;

    public static VnEAnalytics getInstance(Context context) {
        mContext = context;
        if (vnEAnalytics == null) {
            synchronized (LOCK) {
                vnEAnalytics = new VnEAnalytics();
                debugMode = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
            }
        }
        return vnEAnalytics;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setOs(String os) {
        this.os = os;
    }


    public void logEvent(String domain, String eventName, Bundle bundle) {
        try {
            String urlOriginal = domain + eventName + "?&os=" + os + "&app_id=" + appId + "&sdk=1" + "&";
            String url = getRequest(bundle, urlOriginal);
            new Thread(() -> {
                createNewHttpRequest(url);
            }).start();
        } catch (Exception e) {
        }
    }

    public void logLoginOrLogoutEvent(String domain, String eventName, Bundle bundle) {
        try {
            String urlOriginal = domain + eventName + "?";
            String url = getRequest(bundle, urlOriginal);
            new Thread(() -> {
                createNewHttpRequest(url);
            }).start();
        } catch (Exception e) {
        }
    }

    public void setUserProperties(String domain, String eventName, Bundle bundle) {
        try {
            String urlOriginal = domain + eventName + "?";
            String url = getRequest(bundle, urlOriginal);
            new Thread(() -> {
                createNewHttpRequest(url);
            }).start();
        } catch (Exception e) {
        }
    }

    private String createNewHttpRequest(String strUrl) {
        try {
            if (debugMode)
                Log.d("TAG", "createNewHttpRequest: URL-----" + strUrl);
            URL url = new URL(strUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(4000);
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getRequest(Bundle bundle, String url) {
        for (Map.Entry<String, String> entry : bundleToMap(bundle).entrySet()) {
            String key = entry.getKey();
            String value;
            /*if (key.equals("device_id")) {
                value = "dv";
            } else if (key.equals("user_id ")) {
                value = "myvne_id";
            } else if (key.equals("user_email")) {
                value = "vne_email";
            } else {
                value = entry.getValue();
            }*/
            value = entry.getValue();
            url = url + key + "=" + value + "&";
        }
        return url.substring(0, url.length() - 1);
    }

    private Map<String, String> bundleToMap(Bundle extras) {
        Map<String, String> map = new HashMap<String, String>();

        Set<String> ks = extras.keySet();
        Iterator<String> iterator = ks.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            map.put(key, extras.getString(key));
        }
        return map;
    }

    public static class Domain {
        public static final String LA2 = "http://la2.eclick.vn/";
        public static final String LA3 = "https://la3.vnecdn.net/";
        public static final String LOGINANDREGISTER = "https://la2.vnecdn.net/";
        public static final String VIDEO = "http://l.a.eclick.vn/media/";
    }

    public static class Event {
        public static final String LA2_SCREEN_VIEW = "app";
        public static final String PAGE_VIEW = "/page_view/";
        public static final String SCROLL_PERCENTAGE = "scroll_percentage";
        public static final String LOGINORREGISTER = "adp";
        public static final String COMMENT = "/comment/";
        public static final String SAVE = "/save/";
        public static final String SHARE = "/fbshare/";
        public static final String VIDEO = "video";
        public static final String READ = "/read/";
    }
}
