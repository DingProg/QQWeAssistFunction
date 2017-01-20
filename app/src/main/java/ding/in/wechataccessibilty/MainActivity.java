package ding.in.wechataccessibilty;


import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;

import java.util.List;

import static ding.in.wechataccessibilty.ChatConstants.RECEIVER_TYPE;
import static ding.in.wechataccessibilty.ChatConstants.START_NOTI;

public class MainActivity extends Activity implements View.OnClickListener {
    private Button btnChatAccess;
    private Intent intent;
    private AccessibilityManager mAccessibilityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAccessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        initView();
        intent = new Intent(this, NotificationService.class);
        intent.putExtra(RECEIVER_TYPE, START_NOTI);
        startService(intent);
    }

    private void initView() {
        btnChatAccess = (Button) findViewById(R.id.btnChatAccess);
        btnChatAccess.setOnClickListener(this);
        findViewById(R.id.addPluginsKeyWord).setOnClickListener(this);
        findViewById(R.id.addRedPluginsKeyWord).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, AddKeyWordActivity.class);
        startService(intent);
        switch (v.getId()) {
            case R.id.btnChatAccess:
                goAccess();
                break;
            case R.id.addPluginsKeyWord:
                intent.putExtra("type", AddKeyWordActivity.CHAT);
                startActivity(intent);
                break;
            case R.id.addRedPluginsKeyWord:
                intent.putExtra("type", AddKeyWordActivity.RED);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkAccessibilityEnabled("ding.in.wechataccessibilty/.accessibility.DingAccessibilityService")) {
            btnChatAccess.setText("开启插件");
        } else {
            btnChatAccess.setText("关闭插件");
        }

    }

    /**
     * Check当前辅助服务是否启用
     *
     * @param serviceName serviceName
     * @return 是否启用
     */
    public boolean checkAccessibilityEnabled(String serviceName) {
        List<AccessibilityServiceInfo> accessibilityServices =
                mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 前往开启辅助服务界面
     */
    public void goAccess() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
