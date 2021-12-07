package tracking;

import android.content.Context;
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

import common.Utils;
import fpt.vne.common.R;

public final class VnEAnalytics {
    private static VnEAnalytics vnEAnalytics;
    private static Context mContext;
    private static final Object LOCK = new Object();
    private static String deviceId, deviceName, appId, Os;

    public static VnEAnalytics getInstance(Context context) {
        mContext = context;
        deviceId = Utils.getDeviceId(context);
        deviceName = Utils.getDeviceBrand() + " " + Utils.getDeviceModel();

        if (vnEAnalytics == null) {
            synchronized (LOCK) {
                vnEAnalytics = new VnEAnalytics();
            }
        }
        return vnEAnalytics;
    }

    public void setAppId(String appId) {
        VnEAnalytics.appId = appId;
    }

    public void setOs(String os) {
        Os = os;
    }

    public void logEvent(String eventName, Bundle bundle) {
        try {
            String domain = mContext.getString(R.string.str_url) + eventName + "?&os=" + Os + "&app_id=" + appId + "&";
            String url = getRequest(bundle, domain);
            createNewHttpRequest(url);
        } catch (Exception e) {
        }
    }

    public void logLoginOrLogoutEvent(String eventName, Bundle bundle) {
        try {
            String domain = mContext.getString(R.string.str_url) + eventName + "?";
            String url = getRequest(bundle, domain);
            createNewHttpRequest(url);
        } catch (Exception e) {
        }
    }

    private String createNewHttpRequest(String strUrl) {
        try {
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
            String value = entry.getValue();
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

    public static class Event {
        public static final String PAGE_VIEW = "page_view";
        public static final String SCROLL_PERCENTAGE = "scroll_percentage";
        public static final String LOGIN = "login";
        public static final String LOGOUT = "logout";
        public static final String FORGOT_PASSWORD = "forgot_password";
    }
}
