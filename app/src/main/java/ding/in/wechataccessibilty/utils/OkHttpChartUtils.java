package ding.in.wechataccessibilty.utils;

import android.os.Build;

import java.io.IOException;

import ding.in.wechataccessibilty.App;
import ding.in.wechataccessibilty.accessibility.BaseChartAccessibility;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2017/1/16.
 */

public class OkHttpChartUtils {
    static String httpUrl = "http://www.tuling123.com/openapi/api";

    public static String request(String msg,int type){
        String httpArg = "key=5a83253800714a06865718d84b801259&userid=qqc2e"+Build.ID+Build.VERSION.SDK_INT;
        if(type == BaseChartAccessibility.WAT_CHAT){
            httpArg="key=c521936498cc4977bf5684789b4f3c24&userid=we2e"+ Build.ID +Build.VERSION.SDK_INT;
        }else if(type == BaseChartAccessibility.WAT_QQ){
            //httpArg="c521936498cc4977bf5684789b4f3c24";
        }
        StringBuilder strbulder=new StringBuilder(httpUrl);
        strbulder.append("?").append(httpArg)
                .append("&info=")
                .append(msg);
        Request request=new Request.Builder()
                .addHeader("apikey","e089df1379b79b4cfe96ba5ff1180c99")
                .method("GET",null)
                .url(strbulder.toString())
                .build();
        Call call = App.getInstance().getOkHttp().newCall(request);
        try {
            return call.execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
