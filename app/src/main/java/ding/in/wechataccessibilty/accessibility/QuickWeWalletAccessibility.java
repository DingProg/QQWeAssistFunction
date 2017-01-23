package ding.in.wechataccessibilty.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

import ding.in.wechataccessibilty.App;
import ding.in.wechataccessibilty.utils.LogUtils;

import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2016/1/17.
 */
public class QuickWeWalletAccessibility {

    boolean hasAction = false;
    AccessibilityService baseAccessibilityService;

    private boolean notifyRed = false;
    private String content;//内容
    private String time;//发红包的时间
    private String sender;//发红包的人
    int hashCode = 0;//发红包时的hashCode



    public QuickWeWalletAccessibility(AccessibilityService baseAccessibilityService) {
        this.baseAccessibilityService = baseAccessibilityService;
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
                            notifyWechat(event);
                        }
                    }
                }
                break;
            default:
                break;
        }
        //提高抢红包的准确度
        String className = event.getClassName().toString();
//        if (className.equals("com.tencent.mm.ui.LauncherUI") || eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
//            // getLastPacket();
//        }
        if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
            //开红包
            LogUtils.i("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI 打开");
            openButtonWithClick();
        } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")) {
            //退出红包
            LogUtils.i("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI 退出");
            //抢一下个红包自动会打开，不用退出
            //pressBackButton();
        } else {
            getLastPacket();
        }

    }

    /**
     * 模拟back按键
     */
    private void pressBackButton() {
        baseAccessibilityService.performGlobalAction(GLOBAL_ACTION_BACK);
    }

    /**
     * 拉起微信界面
     *
     * @param event event
     */
    private void notifyWechat(AccessibilityEvent event) {
        hasAction = false;
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            String content = notification.tickerText.toString();
            if (!content.contains("微信红包")) return;
            LogUtils.i("通知消息:" + content);
            String[] cc = content.split(":");
//            String name = cc[0].trim();
//            String scontent = cc[1].trim();
            notifyRed = true;
            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取List中最后一个没有抢过的红包，并进行模拟点击
     */
    private void getLastPacket() {
        hasAction = true;
        AccessibilityNodeInfo rootNode = baseAccessibilityService.getRootInActiveWindow();
        if (rootNode == null) {
            hasAction = false;
            return;
        }
        AccessibilityNodeInfo redPackage = findRedPackage(rootNode);
        if (redPackage == null) {
            hasAction = false;
            return;
        }
        if (notifyRed) {//如果是通知栏直接去点击最下面的一个
            performViewClick(redPackage);
            notifyRed = false;
            hasAction = false;
            return;
        }

        // 更换策略 每次只去抢一个红包
        if (!haveSameRedMsg(redPackage)) {
            performViewClick(redPackage);
        }
        hasAction = false;
    }


    /**
     * 看是不是新的红包
     *
     * @param info
     * @return false 不是
     */
    public boolean haveSameRedMsg(AccessibilityNodeInfo info) {
        //内容
        String tContent = null;
        //时间
        String tTime = null;
        //发红包的人
        String tSender = null;
        if (info.getChild(0) != null && info.getChild(0).getText() != null) {
            tContent = info.getChild(0).getText().toString();
        }
        AccessibilityNodeInfo node = info.getParent();
        if (node == null) return false;
        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo thisNode = node.getChild(i);
            if ("android.widget.ImageView".equals(thisNode.getClassName()) && tSender == null) {
                CharSequence contentDescription = thisNode.getContentDescription();
                if (contentDescription != null)
                    tSender = contentDescription.toString().replaceAll("头像$", "");
            } else if ("android.widget.TextView".equals(thisNode.getClassName()) && tTime == null) {
                CharSequence thisNodeText = thisNode.getText();
                if (thisNodeText != null) tTime = thisNodeText.toString();
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        String str = stringBuilder.append(tTime)
                .append("|")
                .append(tSender)
                .append("|")
                .append(tContent)
                .append("|")
                .append(info.hashCode())
                .toString();
        LogUtils.i("微信红包获取msg:" + str);
        return equalsByMsg(info, tContent, tTime, tSender);
    }

    private boolean equalsByMsg(AccessibilityNodeInfo info, String tContent, String tTime, String tSender) {
        if (info.hashCode() == hashCode && strEquesls(tContent, content)
                && strEquesls(tTime, time)
                && strEquesls(tSender, sender)) {
            return true;
        } else {
            hashCode = info.hashCode();
            content = tContent;
            time = tTime;
            sender = tSender;
            return false;
        }
    }

    /**
     * 有可能有null 所以重写String的 equals
     *
     * @param str1
     * @param str2
     * @return
     */
    private boolean strEquesls(String str1, String str2) {
        if (str1 != null && str2 != null) {
            return str1.equals(str2);
        } else {
            return str1 == str2;
        }
    }

    /**
     * 查找屏幕内可点击的红包
     *
     * @param info
     */
    public AccessibilityNodeInfo findRedPackage(AccessibilityNodeInfo info) {
        if (info == null) return null;
        List<AccessibilityNodeInfo> nodes;
        AccessibilityNodeInfo lastAccessbility = null;
        Rect bounds = new Rect();
        int bottom = 0;
        String texts[] = new String[]{"查看红包", "领取红包"};
        for (String text : texts) {
            nodes = info.findAccessibilityNodeInfosByText(text);
            if (nodes != null && !nodes.isEmpty()) {
                for (int i = nodes.size() - 1; i >= 0; i--) {
                    AccessibilityNodeInfo parent = nodes.get(i).getParent();
                    //过滤不是红包的消息
                    if (parent != null && parent.getClassName().toString().equals("android.widget.LinearLayout")) {
                        parent.getBoundsInScreen(bounds);
                        if (bounds.bottom > bottom) {
                            bottom = bounds.bottom;
                            lastAccessbility = parent;
                        }
                    }
                }
            }
        }
        return lastAccessbility;
    }

    /**
     * 打开红包
     */
    private void openButtonWithClick() {
        AccessibilityNodeInfo rootInActiveWindow = baseAccessibilityService.getRootInActiveWindow();
        if (rootInActiveWindow == null) return;
        AccessibilityNodeInfo openButton = findOpenButton(rootInActiveWindow);
        if (openButton != null && "android.widget.Button".equals(openButton.getClassName().toString())) {
            performViewClick(openButton);
            LogUtils.i("点击打开红包的开");
        } else {
            LogUtils.i("执行退出");
            pressBackButton();
        }
    }

    /**
     * 模拟点击事件
     *
     * @param nodeInfo nodeInfo
     */
    public void performViewClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        while (nodeInfo != null) {
            if (nodeInfo.isClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            nodeInfo = nodeInfo.getParent();
        }
    }

    private AccessibilityNodeInfo findOpenButton(AccessibilityNodeInfo node) {
        if (node == null)
            return null;

        //非layout元素
        if (node.getChildCount() == 0) {
            if ("android.widget.Button".equals(node.getClassName()))
                return node;
            else
                return null;
        }

        //layout元素，遍历找button
        AccessibilityNodeInfo button;
        for (int i = 0; i < node.getChildCount(); i++) {
            button = findOpenButton(node.getChild(i));
            if (button != null)
                return button;
        }
        return null;
    }
}
