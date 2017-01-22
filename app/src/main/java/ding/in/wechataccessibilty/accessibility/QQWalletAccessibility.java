package ding.in.wechataccessibilty.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ding.in.wechataccessibilty.App;
import ding.in.wechataccessibilty.utils.LogUtils;

import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2016/1/19.
 */
public class QQWalletAccessibility {

    boolean hasAction = false;
    AccessibilityService baseAccessibilityService;
    private List<AccessibilityNodeInfo> parents;
    private List<String> havedGetReadWallet;


    public QQWalletAccessibility(AccessibilityService baseAccessibilityService) {
        this.baseAccessibilityService = baseAccessibilityService;
        parents = new ArrayList<>();
        havedGetReadWallet = new ArrayList<>();
    }

    public void onAccessibilityEvent(final AccessibilityEvent event) {
        if (!App.weRead) return;
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        if (!TextUtils.isEmpty(content)) {
                            notifyQQchat(event);
                        }
                    }
                }
            default:
                break;
        }
        //提高抢红包的准确度
        String className = event.getClassName().toString();
        //if(!hasAction) {
        //getLastPacket();
        //}
        if (className.equals("cooperation.qwallet.plugin.QWalletPluginProxyActivity")) {
            //已经存入余额
            back();
        } else {
            getLastPacket();
        }
    }

    private void back() {
        AccessibilityNodeInfo rootInActiveWindow = baseAccessibilityService.getRootInActiveWindow();
        if (rootInActiveWindow != null) {
            List<AccessibilityNodeInfo> havedSave = rootInActiveWindow.findAccessibilityNodeInfosByText("已经存入余额");
            if (havedSave != null && havedSave.size() > 0) {
                closeDialog(rootInActiveWindow);
            } else {
                pressBackButton();
            }
        } else {
            pressBackButton();
        }
    }

    private void closeDialog(AccessibilityNodeInfo rootInActiveWindow) {
        StringBuilder redMsg = new StringBuilder();
        LinkedList<AccessibilityNodeInfo> linkedList = new LinkedList<>();
        linkedList.add(rootInActiveWindow);
        while (!linkedList.isEmpty()) {
            AccessibilityNodeInfo accessibilityNodeInfo = linkedList.removeFirst();
            if (accessibilityNodeInfo.getChildCount() > 0) {
                for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
                    linkedList.add(accessibilityNodeInfo.getChild(i));
                }
            } else {
                if (accessibilityNodeInfo.getClassName() != null && accessibilityNodeInfo.getContentDescription() != null) {
                    if (accessibilityNodeInfo.getClassName().toString().equals("android.widget.ImageButton")
                            && accessibilityNodeInfo.getContentDescription().toString().equals("关闭")) {
                        performClick(accessibilityNodeInfo);
                    }
                }
            }
        }
    }

    /**
     * 模拟back按键
     */
    private void pressBackButton() {
        baseAccessibilityService.performGlobalAction(GLOBAL_ACTION_BACK);
    }

    /**
     * 拉起QQ界面
     *
     * @param event event
     */
    private void notifyQQchat(AccessibilityEvent event) {
        hasAction = false;
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            String content = notification.tickerText.toString();
            if (!content.contains("QQ红包")) return;
            LogUtils.i("通知消息:" + content);
//            String[] cc = content.split(":");
//            String name = cc[0].trim();
//            String scontent = cc[1].trim();
            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取List中最后一个红包，并进行模拟点击
     */
    private void getLastPacket() {
        hasAction = true;
        AccessibilityNodeInfo rootNode = baseAccessibilityService.getRootInActiveWindow();
        if (rootNode == null) return;
        parents.clear();
        recycle(rootNode);
        if (parents.size() > 0) {
            for (int i = parents.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo accessibilityNodeInfo = parents.get(i);
                String redMsg = getQQchatStr(accessibilityNodeInfo);
                if (!havedGetReadWallet.contains(redMsg) || !havedGetReadWallet.contains("TAG_DING"+inofToString(accessibilityNodeInfo))) {
                    accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    havedGetReadWallet.add(redMsg);
                    havedGetReadWallet.add("TAG_DING"+inofToString(accessibilityNodeInfo));

                    //抢30/2次红包清空一下
                    if (havedGetReadWallet.size() > 30) {
                        clearAccessInfo();
                    }
                    return;
                }
            }
        }

        hasAction = false;
    }

    private String inofToString(AccessibilityNodeInfo accessibilityNodeInfo){
        return accessibilityNodeInfo.getClass().getName() + "@" + Integer.toHexString(accessibilityNodeInfo.hashCode());
    }

    private void clearAccessInfo() {
        List<String> list=new ArrayList<>();
        for (String s : havedGetReadWallet) {
            if(s.startsWith("TAG_DING")){
                list.add(s);
            }
        }
        if(list.size() > 3) {
            havedGetReadWallet.removeAll(list);
        }

        if(havedGetReadWallet.size() > 30){
            havedGetReadWallet.clear();
        }
    }

    /**
     * 获得红包上的字来判断是否已经抢过红包
     */
    private String getQQchatStr(AccessibilityNodeInfo info) {
        StringBuilder redMsg = new StringBuilder();
        LinkedList<AccessibilityNodeInfo> linkedList = new LinkedList<>();
        linkedList.add(info);
        while (!linkedList.isEmpty()) {
            AccessibilityNodeInfo accessibilityNodeInfo = linkedList.removeFirst();
            if (accessibilityNodeInfo.getChildCount() > 0) {
                for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
                    linkedList.add(accessibilityNodeInfo.getChild(i));
                }
            } else {
                if (accessibilityNodeInfo.getClassName().equals("android.widget.TextView")) {
                    if (accessibilityNodeInfo.getText() != null) {
                        redMsg.append(accessibilityNodeInfo.getText());
                    }
                    if (accessibilityNodeInfo.getContentDescription() != null) {
                        redMsg.append(accessibilityNodeInfo.getText());
                    }
                }
            }
        }
        return redMsg.toString();
    }

    /**
     * 回归函数遍历每一个节点，并将含有"领取红包"存进List中
     *
     * @param info
     */
    public void recycle(AccessibilityNodeInfo info) {
        if (info == null) return;
        List<AccessibilityNodeInfo> qqRed = info.findAccessibilityNodeInfosByText("QQ红包");
        if (qqRed != null && qqRed.size() > 0) {
            for (AccessibilityNodeInfo accessibilityNodeInfo : qqRed) {
                if (accessibilityNodeInfo.isClickable()) {
                    accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                AccessibilityNodeInfo parent = accessibilityNodeInfo.getParent();
                while (parent != null) {
                    if (parent.isClickable()) {
                        parents.add(parent);
                        break;
                    }
                    parent = parent.getParent();
                }
            }
        }
    }

    private void performClick(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo == null) return;
        if (accessibilityNodeInfo.isClickable()) {
            accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            performClick(accessibilityNodeInfo.getParent());
        }
    }

}
