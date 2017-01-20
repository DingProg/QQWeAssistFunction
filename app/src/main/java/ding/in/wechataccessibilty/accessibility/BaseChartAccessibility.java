package ding.in.wechataccessibilty.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ding.in.wechataccessibilty.utils.LogUtils;
import ding.in.wechataccessibilty.utils.OkHttpChartUtils;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2016/11/30.
 */
public abstract class BaseChartAccessibility {
    boolean statue = false;
    public static final int WAT_CHAT = 1;
    public static final int WAT_QQ = 2;

    Handler handlerNewThread = null;
    Handler mMainHandler;

    public List<String> listMsg;
    static String mSourceMsg = "";
    final Object localObject = new Object();

    public AccessibilityService baseAccessibilityService;
    //是否被加载过
    private boolean haslazyInit = false;

    public BaseChartAccessibility(AccessibilityService baseAccessibilityService) {
        this.baseAccessibilityService = baseAccessibilityService;
    }

    public void onServiceLazyConnected() {
        if (haslazyInit) return;
        LogUtils.i("----------------------onServiceConnected--------------------------");
        listMsg = new ArrayList<>();
        mMainHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    replayContent((String) msg.obj);
                }
                return false;
            }
        });
        initNewThread();
        haslazyInit = true;
    }

    protected void initNewThread() {
        HandlerThread handlerThread = new HandlerThread("send_thrand");
        handlerThread.start();
        handlerNewThread = new Handler(handlerThread.getLooper()) {
            String tempMsg = "";
            boolean begin = false;

            @Override
            public void handleMessage(Message msg) {
                if (begin) return;
                begin = true;
                String msgStr = (String) msg.obj;
                if (tempMsg.equals(msgStr)) {
                    LogUtils.i("聊发送内容相同返回:" + msgStr);
                    begin = false;
                    statue = false;
                    return;
                }
                tempMsg = msgStr;
                LogUtils.i("聊天内容内容为:" + msgStr);
                String replayJson = OkHttpChartUtils.request(msgStr, WAT_CHAT);
                LogUtils.i("自动回复的json为:" + replayJson);
                pasreAndSend(replayJson);
                begin = false;
            }
        };
    }

    protected void pasreAndSend(String replayJson) {
        String replayMsg = "";
        try {
            if (TextUtils.isEmpty(replayJson)) {
                statue = false;
                return;
            } else {
                JSONObject jsonObject = new JSONObject(replayJson);
                replayMsg = jsonObject.optString("text");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statue = false;
            return;
        }
        if (replayMsg.contains("图灵机器人") || replayMsg.contains("Turingrobot")) {
            replayMsg = "你好啊";
        }
        LogUtils.i("自动回复的内容为:" + replayMsg);
        mMainHandler.sendMessage(mMainHandler.obtainMessage(0, replayMsg));
    }

    /**
     * 输入
     *
     * @param nodeInfo nodeInfo
     * @param text     text
     */
    public void inputText(AccessibilityNodeInfo nodeInfo, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            ClipboardManager clipboard = (ClipboardManager) baseAccessibilityService.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", text);
            clipboard.setPrimaryClip(clip);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        }
    }


    /**
     * 点击事件
     *
     * @param accessibilityNodeInfo
     */
    public void performClick(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo == null) return;
        if (accessibilityNodeInfo.isClickable()) {
            accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            performClick(accessibilityNodeInfo.getParent());
        }
    }


    /**
     * 处理事件
     *
     * @param event
     */
    public abstract void onAccessibilityEvent(final AccessibilityEvent event);

    /**
     * 查找聊天内容
     */
    public abstract void findText();

    /**
     * 回复
     *
     * @param msg
     */
    public abstract void replayContent(String msg);

    /**
     * 去请求网络消息
     *
     * @param accessibilityNodeInfo
     */
    public abstract void sendMsg(AccessibilityNodeInfo accessibilityNodeInfo);

}
