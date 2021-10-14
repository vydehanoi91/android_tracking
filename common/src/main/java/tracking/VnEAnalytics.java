package tracking;

import android.content.Context;
import android.provider.Settings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class VnEAnalytics {

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
}
