package limk.p2pchat.basic;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Set;

public class PreferenceUtils {
    public static final String KEY_MESSAGE_DRAFT = "message_draft";

    public static final String PREFERENCE_NAME = "setting";

    private static SharedPreferences preferences;

    public static void initPreference(Context context) {
        preferences = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
    }

    public static String getStringValue(Context context, String key) {
        Log.i("commonsAPI", "getStringValue...");
        if (preferences == null && context != null)
            initPreference(context);
        return preferences.getString(key, "");
    }

    public static boolean getBooleanValue(Context context, String key,
                                          boolean def) {
        Log.i("commonsAPI", "getBooleanValue...");
        if (preferences == null && context != null)
            initPreference(context);
        return preferences.getBoolean(key, false);
    }

    public static int getIntValue(Context context, String key) {
        Log.i("commonsAPI", "getIntValue...");
        if (preferences == null && context != null)
            initPreference(context);
        return preferences.getInt(key, 0);
    }

    public static long getLongValue(Context context, String key) {
        Log.i("commonsAPI", "getLongValue...");
        if (preferences == null && context != null)
            initPreference(context);
        return preferences.getLong(key, 0);
    }

    public static double getDoubleValue(Context context, String key) {
        Log.i("commonsAPI", "getDoubleValue...");
        if (preferences == null && context != null)
            initPreference(context);
        return Double.valueOf(preferences.getString(key, "0"));
    }

    public static void getStringSet(Context context, String key,
                                    Set<String> value) {
        if (preferences == null && context != null)
            initPreference(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putStringSet(key, value);
        editor.commit();
    }

    public static void saveStringValue(Context context, String key, String info) {
        Log.i("commonsAPI", "saveStringValue...");

        if (preferences == null && context != null)
            initPreference(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, info);
        editor.commit();
    }

    public static void saveBooleanValue(Context context, String key,
                                        Boolean info) {
        Log.i("commonsAPI", "saveBooleanValue...");

        if (preferences == null && context != null)
            initPreference(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, info);
        editor.commit();
    }

    public static void saveIntValue(Context context, String key, int info) {
        Log.i("commonsAPI", "saveIntValue...");
        if (preferences == null && context != null)
            initPreference(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, info);
        editor.commit();
    }

    public static void saveLongValue(Context context, String key, long value) {
        Log.i("commonsAPI", "saveLongValue...");
        if (preferences == null && context != null)
            initPreference(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static void saveDoubleValue(Context context, String key, double value) {
        Log.i("commonsAPI", "saveFloatValue...");
        if (preferences == null && context != null)
            initPreference(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, Double.toString(value));
        editor.commit();
    }

    public static void saveStringSet(Context context, String key,
                                     Set<String> value) {
        if (preferences == null && context != null)
            initPreference(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(key, value);
        editor.commit();
    }
}
