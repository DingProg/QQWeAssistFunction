package ding.in.wechataccessibilty.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2017/1/18.
 */

public class LogUtils {
    public static final String TAG = "WeChatQQAccessbility";
    public static final boolean DEBUG = true;

    public static void i(String msg) {
        if (DEBUG && !TextUtils.isEmpty(msg)) {
            Log.i(TAG, msg);
        }
    }
}
