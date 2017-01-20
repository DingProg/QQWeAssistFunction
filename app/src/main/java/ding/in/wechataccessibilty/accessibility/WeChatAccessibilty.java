package ding.in.wechataccessibilty.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import ding.in.wechataccessibilty.utils.CompareUtils;
import ding.in.wechataccessibilty.utils.LogUtils;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2017/1/18.
 */

public class WeChatAccessibilty extends BaseChartAccessibility {
    private AccessibilityService baseAccessibilityService;

    public WeChatAccessibilty(AccessibilityService baseAccessibilityService) {
        super(baseAccessibilityService);
        this.baseAccessibilityService = baseAccessibilityService;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        int eventType = accessibilityEvent.getEventType();
        if (accessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED == eventType
                || accessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType) {
            if (!statue) {
                findText();
            }
        }
    }

    @Override
    public void findText() {
        AccessibilityNodeInfo rootInActiveWindow = baseAccessibilityService.getRootInActiveWindow();
        if (rootInActiveWindow == null) return;
        if (rootInActiveWindow.getContentDescription() != null) {
            String str = rootInActiveWindow.getContentDescription().toString();
            if (!CompareUtils.hasSame(str)) {
                // LogUtils.i("屏蔽的群，直接返回");
                return;
            }
        } else {
            return;
        }
        List<AccessibilityNodeInfo> accessibilityNodeInfosByViewId = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ib");
        if (accessibilityNodeInfosByViewId == null) return;
        AccessibilityNodeInfo accessibilityNodeInfo = null;
        for (int i = accessibilityNodeInfosByViewId.size() - 1; i >= 0; i--) {
            accessibilityNodeInfo = accessibilityNodeInfosByViewId.get(i);
            if (accessibilityNodeInfo != null) {
                if (accessibilityNodeInfo.getText() != null) {
                    AccessibilityNodeInfo parent = accessibilityNodeInfo.getParent();
                    if (parent != null) {
                        int j = 0;
                        for (j = 0; j < parent.getChildCount(); j++) {
                            AccessibilityNodeInfo child = parent.getChild(j);
                            if (child == null) return;
                            if (child.getClassName().equals("android.widget.ImageView")) {
                                j++;
                                break;
                            }
                        }
                        if (j < parent.getChildCount()) {
                            if (parent.getChild(j).getClassName().equals("android.widget.TextView")) {
                                sendMsg(accessibilityNodeInfo);
                                return;
                            }
                        }
                        //sendMsg(accessibilityNodeInfo);
                        return;
                    }
                }
                return;
            }
        }
    }

    @Override
    public void sendMsg(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (statue) return;
        statue = true;
        String sourceMsg = accessibilityNodeInfo.getText().toString();
        LogUtils.i("找到聊天内容:" + sourceMsg);
        mSourceMsg = sourceMsg;
        if (listMsg.size() > 0) {
            LogUtils.i("list内容为:" + listMsg.toString());
            //大于600 字直接返回
            if (sourceMsg.length() > 600) return;
            if (listMsg.contains(sourceMsg)) {
                LogUtils.i("没有新的聊天内容，直接返回");
                statue = false;
                return;
            }
        }
        handlerNewThread.sendMessage(handlerNewThread.obtainMessage(0, sourceMsg));
    }

    /**
     * 查找输入框
     */
    private boolean findEditText(AccessibilityNodeInfo rootNode, String content) {
        int count = rootNode.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
            if (nodeInfo == null) {
                continue;
            }
            if ("android.widget.EditText".equals(nodeInfo.getClassName())) {
                inputText(nodeInfo, content);
                return true;
            }
            if (findEditText(nodeInfo, content)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 寻找窗体中的“发送”按钮，并且点击。
     */
    private void send() {
        AccessibilityNodeInfo nodeInfo = baseAccessibilityService.getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo
                    .findAccessibilityNodeInfosByText("发送");
            if (list != null && list.size() > 0) {
                for (AccessibilityNodeInfo n : list) {
                    if (n.getClassName().equals("android.widget.Button") && n.isEnabled()) {
                        n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            } else {
                List<AccessibilityNodeInfo> liste = nodeInfo
                        .findAccessibilityNodeInfosByText("Send");
                if (liste != null && liste.size() > 0) {
                    for (AccessibilityNodeInfo n : liste) {
                        if (n.getClassName().equals("android.widget.Button") && n.isEnabled()) {
                            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void replayContent(String replayMsg) {
        synchronized (localObject) {
            try {
                //String text="第"+listMsg.size()+"条回复："+replayMsg;
                //if(listMsg.size() >0 && listMsg.get(listMsg.size()-1).equals(newMsg)) return;
                AccessibilityNodeInfo rootInActiveWindow = baseAccessibilityService.getRootInActiveWindow();
                if (rootInActiveWindow == null) return;
                findEditText(rootInActiveWindow, replayMsg);
                send();
                listMsg.add(mSourceMsg);
                statue = false;
            } catch (Exception e) {

            }
        }
    }
}
