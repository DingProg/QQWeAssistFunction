package ding.in.wechataccessibilty.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private List<AccessibilityNodeInfo> redWillClick;
    private List<String> havedGetReadWallet;
    private List<String> doubleCheckHash;
    private final Object lockObject = new Object();

    private AccessibilityNodeInfo tempAccessInfo = null;
    private String tempStr = null;
    private boolean notifyRed = false;

    public WeWalletAccessibility(AccessibilityService baseAccessibilityService) {
        this.baseAccessibilityService = baseAccessibilityService;
        redWillClick = new ArrayList<>();
        havedGetReadWallet = new ArrayList<>();
        doubleCheckHash = new ArrayList<>();
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
            addHaveData();
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
        hasAction = true;
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            String content = notification.tickerText.toString();
            if (!content.contains("微信红包")) return;
            LogUtils.i("通知消息:" + content);
            String[] cc = content.split(":");
//            String name = cc[0].trim();
//            String scontent = cc[1].trim();
            notifyRed=true;
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
        synchronized (lockObject) {
            AccessibilityNodeInfo rootNode = baseAccessibilityService.getRootInActiveWindow();
            if (rootNode == null) return;
            redWillClick.clear();
            findRedPackage(rootNode);
            if (redWillClick.size() > 0) {
                if(notifyRed){//如果是通知栏直接去点击最下面的一个
                    AccessibilityNodeInfo info = redWillClick.get(redWillClick.size() - 1);
                    haveSameRedMsg(info);
                    performViewClick(info);
                    notifyRed = false;
                    return;
                }

                AccessibilityNodeInfo redInfo = null;
                //去查找那个需要抢的红包
                for (int i = redWillClick.size() - 1; i >= 0; i--) {
                    AccessibilityNodeInfo accessibilityNodeInfo = redWillClick.get(i);
                    if (!doubleCheckHash.contains(Integer.toHexString(accessibilityNodeInfo.hashCode()))) {
                        redInfo = accessibilityNodeInfo;
                        break;
                    }
                }

                // 更换策略 每次只去抢一个红包
                if (redInfo != null) {
                    if (haveSameRedMsg(redInfo)) {
                        performViewClick(redInfo);
                    }
                }else{
                    redInfo = redWillClick.get(redWillClick.size() - 1);
                    if (haveSameRedMsg(redInfo)) {
                        performViewClick(redInfo);
                    }
                }
            }
        }
    }


    /**
     * 看是不是新的红包
     *
     * @param info
     * @return false 不是
     */
    public boolean haveSameRedMsg(AccessibilityNodeInfo info) {
        String content = null;//内容
        String time = null;//时间
        String sender = null;//发红包的人

        StringBuilder stringBuilder = new StringBuilder();
        if (info.getChild(0) != null && info.getChild(0).getText() != null) {
            content = info.getChild(0).getText().toString();
        }
        AccessibilityNodeInfo node = info.getParent();
        if (node == null) return false;
        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo thisNode = node.getChild(i);
            if ("android.widget.ImageView".equals(thisNode.getClassName()) && sender == null) {
                CharSequence contentDescription = thisNode.getContentDescription();
                if (contentDescription != null)
                    sender = contentDescription.toString().replaceAll("头像$", "");
            } else if ("android.widget.TextView".equals(thisNode.getClassName()) && time == null) {
                CharSequence thisNodeText = thisNode.getText();
                if (thisNodeText != null) time = thisNodeText.toString();
            }
        }
        String str = stringBuilder.append(time)
                .append("|")
                .append(sender)
                .append("|")
                .append(content)
                .append("|")
                .append(Integer.toHexString(info.hashCode()))
                .toString();
        LogUtils.i("微信红包获取msg:" + str);
        if (!havedGetReadWallet.contains(str)) {
//            havedGetReadWallet.add(str);
//            doubleCheckHash.add(info.hashCode());
            tempStr = str;
            tempAccessInfo = info;
            return true;
        }
        return false;
    }

    /**
     * 查找屏幕内可点击的红包
     *
     * @param info
     */
    public void findRedPackage(AccessibilityNodeInfo info) {
        if (info == null) return;
        List<AccessibilityNodeInfo> nodes;
        Rect bounds = new Rect();
        String texts[] = new String[]{"查看红包","领取红包"};
        for (String text : texts) {
            nodes = info.findAccessibilityNodeInfosByText(text);
            if (nodes != null && !nodes.isEmpty()) {
                for (int i = nodes.size() - 1; i >= 0; i--) {
                    AccessibilityNodeInfo parent = nodes.get(i).getParent();
                    //过滤不是红包的消息
                    if (parent != null && parent.getClassName().toString().equals("android.widget.LinearLayout")) {
                        parent.getBoundsInScreen(bounds);
                        //确定是在屏幕内bounds.top > 0  &&
                        if (!redWillClick.contains(parent)) {
                            redWillClick.add(parent);
                        }
                    }
                }
            }
        }
        sortRedWillClickByBotoom();
    }

    /**
     * 对红包进行排序
     */
    private void sortRedWillClickByBotoom() {
        Collections.sort(redWillClick, new Comparator<AccessibilityNodeInfo>() {
            Rect bounds1 = new Rect();
            Rect bounds2 = new Rect();

            @Override
            public int compare(AccessibilityNodeInfo o1, AccessibilityNodeInfo o2) {
                o1.getBoundsInScreen(bounds1);
                o2.getBoundsInScreen(bounds2);
                return bounds1.bottom - bounds2.bottom;
            }
        });
    }

    /**
     * 打开红包
     */
    private void openButtonWithClick() {
        AccessibilityNodeInfo rootInActiveWindow = baseAccessibilityService.getRootInActiveWindow();
        if (rootInActiveWindow == null) return;
        AccessibilityNodeInfo openButton = findOpenButton(rootInActiveWindow);
        //添加已经抢过的红包
        addHaveData();
        //清楚数据
        clearHaveRedList();
        if (openButton != null && "android.widget.Button".equals(openButton.getClassName().toString())) {
            performViewClick(openButton);
            LogUtils.i("点击打开红包的开");
        } else {
            LogUtils.i("执行退出");
            pressBackButton();
        }
    }

    private void clearHaveRedList() {
        //采用先进先出策略
        if (doubleCheckHash.size() > 5) {
            doubleCheckHash.remove(0);
        }

        //抢到7个红包，开始清理,移除最开始抢到的第一个红包
        if (havedGetReadWallet.size() > 6) {
            havedGetReadWallet.remove(0);
        }
    }

    /**
     * 添加已经抢过红包的标识
     */
    private void addHaveData() {
        if (tempAccessInfo != null && !doubleCheckHash.contains(Integer.toHexString(tempAccessInfo.hashCode()))) {
            doubleCheckHash.add(Integer.toHexString(tempAccessInfo.hashCode()));
            tempAccessInfo = null;
        }
        if (tempStr != null && !havedGetReadWallet.contains(tempStr)) {
            havedGetReadWallet.add(tempStr);
            tempStr = null;
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
