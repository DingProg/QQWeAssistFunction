package ding.in.wechataccessibilty.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

import ding.in.wechataccessibilty.App;
import ding.in.wechataccessibilty.utils.LogUtils;
import ding.in.wechataccessibilty.utils.NetCheckUtils;

/**
 * Description：
 *
 * @author dingdegao
 *         create by 2016/11/30.
 */
public class DingAccessibilityService extends AccessibilityService {
    //抢红包
    private QuickAndDeleteWeWalletAccessibility weWalletAccessibility;
    private QQWalletAccessibility qqWalletAccessibility;

    //自动聊天
    private BaseChartAccessibility qqChatAccessibility;
    private BaseChartAccessibility weChatAccessibilty;


    protected void onServiceConnected() {
        LogUtils.i("----------------------onServiceConnected--------------------------");
        weWalletAccessibility = new QuickAndDeleteWeWalletAccessibility(this);
        qqWalletAccessibility = new QQWalletAccessibility(this);

        qqChatAccessibility = new QQChatAccessibility(this);
        weChatAccessibilty = new WeChatAccessibilty(this);
    }

    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (!NetCheckUtils.hasNet(this)) return;
        if (accessibilityEvent.getClassName() == null) return;
        String claseName = accessibilityEvent.getPackageName().toString();
        if (claseName.equals("com.tencent.mm")) {
            weWalletAccessibility.onAccessibilityEvent(accessibilityEvent);
            if (!App.weChat) return;
            weChatAccessibilty.onServiceLazyConnected();
            weChatAccessibilty.onAccessibilityEvent(accessibilityEvent);
        } else if (claseName.equals("com.tencent.mobileqq")) {
            qqWalletAccessibility.onAccessibilityEvent(accessibilityEvent);
            if (!App.qqChat) return;
            qqChatAccessibility.onServiceLazyConnected();
            qqChatAccessibility.onAccessibilityEvent(accessibilityEvent);
        }
    }

    @Override
    public void onInterrupt() {

    }

}
