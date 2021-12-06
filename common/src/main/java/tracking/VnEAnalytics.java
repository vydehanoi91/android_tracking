package tracking;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import fpt.vne.common.R;

public class VnEAnalytics {
    private static Context mContext;
    public static final Gson GSON = new Gson();
    private static String deviceId, deviceName, appId, Os;

    public static void initializeDefaultValue(Context context) {
        mContext = context;
        deviceId = getDeviceId(context);
        deviceName = getDeviceBrand() + " " + getDeviceModel();
    }

    public static String getRequest(Context context, Bundle bundle) {
        String url = context.getString(R.string.base_url) + "?";
        for (Map.Entry<String, String> entry : bundleToMap(bundle).entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            url = url + key + "=" + value + "&";
        }
        return url.substring(0, url.length() - 1);
    }

    public static Map<String, String> bundleToMap(Bundle extras) {
        Map<String, String> map = new HashMap<String, String>();

        Set<String> ks = extras.keySet();
        Iterator<String> iterator = ks.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            map.put(key, extras.getString(key));
        }
        return map;
    }

    public static String getDeviceId(Context context) {
        return md5(getRealDeviceId(context));
    }

    public static String md5(String target) {
        try {
            if (target == null || target.length() == 0)
                return target;
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(target.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
        }
        return target;
    }

    public static String getRealDeviceId(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    public static String getRawData(Context context, int raw) {
        try {
            final InputStream is = context.getResources().openRawResource(raw);
            String json = null;
            Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
            if (scanner.hasNext())
                json = scanner.next();
            is.close();
            return json;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null)
            return false;
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
        }
        return false;
    }

    public static String getDeviceBrand() {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer.equalsIgnoreCase("HTC"))
            return "HTC";
        return capitalize(manufacturer);
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        try {
            for (char c : arr) {
                if (capitalizeNext && Character.isLetter(c)) {
                    phrase += Character.toUpperCase(c);
                    capitalizeNext = false;
                    continue;
                } else if (Character.isWhitespace(c)) {
                    capitalizeNext = true;
                }
                phrase += c;
            }
        } catch (Exception e) {
            e.printStackTrace();
            phrase = "";
        }
        return phrase;
    }

    public static void logEvent(String eventName, Bundle bundle) {
        try {
            String url = getRequest(mContext, bundle);
            createNewHttpRequest(url);
        } catch (Exception e) {
        }
    }

    public static String createNewHttpRequest(String strUrl) {
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
}
