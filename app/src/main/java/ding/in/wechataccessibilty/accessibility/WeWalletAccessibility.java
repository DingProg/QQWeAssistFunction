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
 *         create by 2016/1/17.
 */
public class WeWalletAccessibility {

    boolean hasAction = false;

    AccessibilityService baseAccessibilityService;
    private List<AccessibilityNodeInfo> parents;
    private List<String> havedGetReadWallet;

    public WeWalletAccessibility(AccessibilityService baseAccessibilityService) {
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
            pressBackButton();
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
        hasAction = true;
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            String content = notification.tickerText.toString();
            if (!content.contains("微信红包")) return;
            LogUtils.i("通知消息:" + content);
            String[] cc = content.split(":");
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
                String str = getWechatStr(accessibilityNodeInfo);
                if (!havedGetReadWallet.contains(str)
                        || !havedGetReadWallet.contains(inofToString(accessibilityNodeInfo))) {
                    //str = 恭喜发财，大吉大利！领取红包微信红包  这个需要在商量
                    havedGetReadWallet.add(str);
                    havedGetReadWallet.add(inofToString(accessibilityNodeInfo));

                    LogUtils.i("点击红包");

                    //领取红包大于30/2个，清理
                    if (havedGetReadWallet.size() > 30) {
                        //havedGetReadWallet.clear();
                        clearAccessInfo();
                    }
                    accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return;
                }
            }
        }
    }

    private String inofToString(AccessibilityNodeInfo accessibilityNodeInfo) {
        return accessibilityNodeInfo.getClass().getName() + "@" + Integer.toHexString(accessibilityNodeInfo.hashCode());
    }

    private void clearAccessInfo() {
        List<String> list = new ArrayList<>();
        for (String s : havedGetReadWallet) {
            if (s.startsWith("TAG_DING")) {
                list.add(s);
            }
        }
        if (list.size() > 3) {
            havedGetReadWallet.removeAll(list);
        }

        if (havedGetReadWallet.size() > 30) {
            havedGetReadWallet.clear();
        }
    }

    /**
     * 获得红包上的字来判断是否已经抢过红包
     */
    private String getWechatStr(AccessibilityNodeInfo info) {
        StringBuilder redMsg = new StringBuilder();
        LinkedList<AccessibilityNodeInfo> linkedList = new LinkedList<>();
        linkedList.add(info);
        while (!linkedList.isEmpty()) {
            AccessibilityNodeInfo accessibilityNodeInfo = linkedList.removeFirst();
            if (accessibilityNodeInfo != null) {
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
        if (info.getChildCount() == 0) {
            if (info.getText() != null) {
                //查看红包，领取自己发的红包
                if ("领取红包".equals(info.getText().toString()) || "查看红包".equals(info.getText().toString())) {
                    if (info.isClickable()) {
                        info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    AccessibilityNodeInfo parent = info.getParent();
                    while (parent != null) {
                        if (parent.isClickable()) {
                            parents.add(parent);
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i));
                }
            }
        }
    }

    /**
     * 打开红包
     */
    private void openButtonWithClick() {
        AccessibilityNodeInfo rootInActiveWindow = baseAccessibilityService.getRootInActiveWindow();
        if (rootInActiveWindow == null) return;
//        boolean hasNodes = hasOneOfThoseNodes(rootInActiveWindow,
//                WECHAT_BETTER_LUCK_CH, WECHAT_EXPIRES_CH, WECHAT_DETAILS_CH);
//        if (hasNodes) {
//            LogUtils.i("执行退出");
//            pressBackButton();
//        }

        AccessibilityNodeInfo openButton = findOpenButton(rootInActiveWindow);
        if (openButton != null && "android.widget.Button".equals(openButton.getClassName().toString())) {
            performViewClick(openButton);
            LogUtils.i("点击打开红包的开");
        }else{
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

    //    private static final String WECHAT_DETAILS_EN = "Details";
    private static final String WECHAT_DETAILS_CH = "看看大家的手气";
    //    private static final String WECHAT_BETTER_LUCK_EN = "Better luck next time!";
    private static final String WECHAT_BETTER_LUCK_CH = "手慢了";
    private static final String WECHAT_EXPIRES_CH = "已超过24小时";

    /**
     * 判断红包是否已经被领取
     */
    private boolean hasOneOfThoseNodes(AccessibilityNodeInfo rootInActiveWindow, String... texts) {
        List<AccessibilityNodeInfo> nodes;
        for (String text : texts) {
            if (text == null) continue;
            nodes = rootInActiveWindow.findAccessibilityNodeInfosByText(text);
            if (nodes != null && !nodes.isEmpty()) return true;
        }
        return false;
    }

}
