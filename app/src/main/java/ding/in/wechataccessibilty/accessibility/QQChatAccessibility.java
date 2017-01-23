package ding.in.wechataccessibilty.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import ding.in.wechataccessibilty.utils.LogUtils;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2016/1/16.
 */
public class QQChatAccessibility extends BaseChartAccessibility {
    private AccessibilityService baseAccessibilityService;

    public QQChatAccessibility(AccessibilityService baseAccessibilityService) {
        super(baseAccessibilityService);
        this.baseAccessibilityService = baseAccessibilityService;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        if (event.TYPE_WINDOW_CONTENT_CHANGED == eventType
                || event.TYPE_WINDOW_STATE_CHANGED == eventType) {
            findText();
        }
    }

    @Override
    public void findText() {
        AccessibilityNodeInfo rootInActiveWindow = baseAccessibilityService.getRootInActiveWindow();
        if (rootInActiveWindow == null) return;
        //TODO 去适配QQ的屏蔽功能
//        if(rootInActiveWindow.getContentDescription() != null){
//            String str = rootInActiveWindow.getContentDescription().toString();
//            if(!CompareUtils.hasSame(str)){
//                LogUtils.i("屏蔽的群，直接返回");
//                return;
//            }
//        }else {
//            return;
//        }
        List<AccessibilityNodeInfo> accessibilityNodeInfosByViewId = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/chat_item_content_layout");
        if (accessibilityNodeInfosByViewId == null) return;

        AccessibilityNodeInfo accessibilityNodeInfo = null;
        for (int i = accessibilityNodeInfosByViewId.size() - 1; i >= 0; i--) {
            accessibilityNodeInfo = accessibilityNodeInfosByViewId.get(i);
            if (accessibilityNodeInfo != null) {
                if (accessibilityNodeInfo.getText() != null) {
                    LogUtils.i("accessibilityNodeInfo:" + accessibilityNodeInfo.getClassName().toString());
                    AccessibilityNodeInfo parent = accessibilityNodeInfo.getParent();
                    if (parent != null) {
                        if (parent.getChildCount() > 0) {
                            AccessibilityNodeInfo child = parent.getChild(0);
                            LogUtils.i("控件的名称:" + child.getClassName().toString());
                            if (child.getClassName().toString().contains("android.widget.ImageView")) {
                                sendMsg(accessibilityNodeInfo);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void sendMsg(AccessibilityNodeInfo accessibilityNodeInfo) {
        String sourceMsg = accessibilityNodeInfo.getText().toString();
        LogUtils.i("找到聊天内容:" + sourceMsg);
        mSourceMsg = sourceMsg;
        if (listMsg.size() > 0) {
            LogUtils.i("list内容为:" + listMsg.toString());
            if (listMsg.contains(sourceMsg)) {
                LogUtils.i("没有新的聊天内容，直接返回");
            }
        }
        handlerNewThread.sendMessage(handlerNewThread.obtainMessage(0, sourceMsg));
    }

    @Override
    public void replayContent(String replayMsg) {
        synchronized (localObject) {
            try {
                //String text="第"+listMsg.size()+"条回复："+replayMsg;
                //if(listMsg.size() >0 && listMsg.get(listMsg.size()-1).equals(newMsg)) return;
                AccessibilityNodeInfo rootInActiveWindow = baseAccessibilityService.getRootInActiveWindow();
                List<AccessibilityNodeInfo> accessibilityNodeInfosByViewId = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/input");
                if (accessibilityNodeInfosByViewId == null) return;
                AccessibilityNodeInfo accessibilityNodeInfo = accessibilityNodeInfosByViewId.get(accessibilityNodeInfosByViewId.size() - 1);
                inputText(accessibilityNodeInfo, replayMsg);

                AccessibilityNodeInfo rootChInActiveWindow = baseAccessibilityService.getRootInActiveWindow();
                List<AccessibilityNodeInfo> btnList = rootChInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/fun_btn");
                if (btnList == null) return;
                AccessibilityNodeInfo btn = btnList.get(btnList.size() - 1);
                performClick(btn);
                listMsg.add(mSourceMsg);
            } catch (Exception e) {

            }
        }
    }

    /**
     * 模拟输入
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

}
