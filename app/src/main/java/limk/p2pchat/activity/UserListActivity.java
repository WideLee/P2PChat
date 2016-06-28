package limk.p2pchat.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import limk.p2pchat.R;
import limk.p2pchat.basic.MessageBasic.MessageEntity;
import limk.p2pchat.basic.MessageBasic.UserEntity;
import limk.p2pchat.database.P2PChatDBHelper;
import limk.p2pchat.net.UDPBroadcastSender;
import limk.p2pchat.receiver.MessageReceiver;
import limk.p2pchat.receiver.MessageReceiver.EventHandler;
import limk.p2pchat.service.ServerService;

public class UserListActivity extends Activity implements EventHandler {

    private static final int REQUEST_CODE_CHAT = 0;

    private ListView userListView;
    private ArrayList<HashMap<String, Object>> userData;
    private SimpleAdapter userListAdapter;
    private ArrayList<UserEntity> userList;
    private P2PChatDBHelper dbHelper;
    private Handler mHandler;
    private Button refreshButton;
    private Runnable sendBrocast = new Runnable() {

        @Override
        public void run() {
            new UDPBroadcastSender(UserListActivity.this).start();
        }
    };
    private long firstTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_neighbor);

        mHandler = new Handler();
        userData = new ArrayList<HashMap<String, Object>>();
        userList = new ArrayList<UserEntity>();
        dbHelper = new P2PChatDBHelper(this);
        dbHelper.resetUserAddress();

        userList = dbHelper.getAllUser();

        refreshButton = (Button) this.findViewById(R.id.btn_refresh);
        refreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.post(sendBrocast);
            }
        });
        userListAdapter = new SimpleAdapter(this, userData, R.layout.user_list_item, new String[]{
                "name", "ip_addr", "unread_message", "uuid"}, new int[]{R.id.tv_user_name,
                R.id.tv_ip_addr, R.id.tv_unread, R.id.tv_uuid});
        userListView = (ListView) this.findViewById(R.id.lv_neighbor);
        userListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                long uuid = (Long) userData.get(arg2).get("uuid");
                dbHelper.readMessageByUUID(uuid);

                Intent intent = new Intent(UserListActivity.this, ChatActivity.class);
                intent.putExtra("uuid", uuid);
                startActivityForResult(intent, REQUEST_CODE_CHAT);
            }
        });
        updateList();
        userListView.setAdapter(userListAdapter);
        ActivityManager myManager = (ActivityManager) this
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager
                .getRunningServices(100);
        boolean flag = true;
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals("limk.p2pchat.service.ServerService")) {
                flag = false;
                break;
            }
        }
        if (flag) {
            startService(new Intent(UserListActivity.this, ServerService.class));
        }
    }

    private void updateList() {
        userData.clear();
        userList = dbHelper.getAllUser();
        for (UserEntity user : userList) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("name", user.getName());
            map.put("ip_addr", dbHelper.getUserAddrByUUID(user.getUuid()));
            map.put("unread_message", dbHelper.getUnreadMessageCountByUUID(user.getUuid()));
            map.put("uuid", user.getUuid());
            userData.add(map);
        }
        userListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
        MessageReceiver.handlers.add(this);
    }

    @Override
    protected void onPause() {
        MessageReceiver.handlers.remove(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(UserListActivity.this, ServerService.class));
        super.onDestroy();
    }

    @Override
    public void onReceiveHelloMessage(MessageEntity message) {

        updateList();
        Toast.makeText(UserListActivity.this, "已更新好友列表", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceiveACKMessage(MessageEntity message) {
        updateList();
    }

    @Override
    public void onReceiveWordMessage(MessageEntity message) {
        updateList();
    }

    @Override
    public void onReceivePictureMessage(MessageEntity message) {
        updateList();
    }

    @Override
    public void onReceiveOtherMessage(MessageEntity message) {
        updateList();
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - firstTime < 3000) {

            finish();
        } else {
            firstTime = System.currentTimeMillis();
            Toast.makeText(UserListActivity.this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CHAT && resultCode == RESULT_OK) {
            updateList();
        }
    }

    @Override
    public void onReceiveSendFailMessage(MessageEntity message) {
        updateList();
    }
}
