package ding.in.wechataccessibilty;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ding.in.wechataccessibilty.data.ItemEntity;
import ding.in.wechataccessibilty.data.SimpleDatabaseOperate;

public class AddKeyWordActivity extends Activity implements AdapterView.OnItemLongClickListener, View.OnClickListener {

    public static final int CHAT = 0x01;
    public static final int RED = 0x02;

    private ListView lvShowData;
    private ArrayAdapter<ItemEntity> adapter;
    private EditText editText;
    private Button btnAdd;
    private int type = CHAT;
    private TextView tvTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shield_key_word);
        type = getIntent().getIntExtra("type", CHAT);

        init();
    }

    private void init() {
        tvTip = ((TextView) findViewById(R.id.tvTip));
        lvShowData = ((ListView) findViewById(R.id.lvShwData));
        adapter = new ArrayAdapter<ItemEntity>(this, R.layout.item_lv);
        lvShowData.setAdapter(adapter);
        if (type == CHAT) {
            adapter.addAll(App.getInstance().getData());
        } else {
            tvTip.setText("添加需要屏蔽红包的群关键字,QQ暂时未适配，仅用于可用于微信,长按item自动删除");
            adapter.addAll(App.getInstance().getDataRed());
        }

        editText = ((EditText) findViewById(R.id.editText));
        btnAdd = ((Button) findViewById(R.id.btnAdd));
        btnAdd.setOnClickListener(this);

        lvShowData.setOnItemLongClickListener(this);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (type == CHAT) {
            SimpleDatabaseOperate.delete(App.getInstance().getDatabase(),
                    adapter.getItem(position).id);
        } else {
            SimpleDatabaseOperate.deleteRed(App.getInstance().getDatabase(),
                    adapter.getItem(position).id);
        }

        update();
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAdd:
                String value = editText.getText().toString();
                if (TextUtils.isEmpty(value)) {
                    showToast("添加内容为空");
                    return;
                }
                List<ItemEntity> data;
                if (type == CHAT) {
                    data = App.getInstance().getData();
                } else {
                    data = App.getInstance().getDataRed();
                }
                for (ItemEntity itemEntity : data) {
                    if (itemEntity.value.contains(value)) {
                        showToast("添加内容相同");
                        return;
                    }
                }
                if (type == CHAT) {
                    SimpleDatabaseOperate.insert(App.getInstance().getDatabase(),
                            value);
                } else {
                    SimpleDatabaseOperate.insertRed(App.getInstance().getDatabase(),
                            value);
                }
                update();
                break;
        }
    }

    private void update() {
        if (type == CHAT) {
            App.getInstance().updataData();
        } else {
            App.getInstance().updataDataRed();
        }

        adapter.clear();
        if (type == CHAT) {
            adapter.addAll(App.getInstance().getData());
        } else {
            adapter.addAll(App.getInstance().getDataRed());
        }
    }

    void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
