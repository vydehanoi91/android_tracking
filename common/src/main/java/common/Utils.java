package common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Utils {
    public static final Gson GSON = new Gson();

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
}
