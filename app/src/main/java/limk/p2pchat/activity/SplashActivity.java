package limk.p2pchat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import limk.p2pchat.R;
import limk.p2pchat.basic.MessageBasic.UserEntity;
import limk.p2pchat.database.P2PChatDBHelper;

public class SplashActivity extends Activity {

    public Runnable start = new Runnable() {

        @Override
        public void run() {

            SharedPreferences sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
            UserEntity.Builder userBuilder = UserEntity.newBuilder();
            userBuilder.setName(sharedPreferences.getString("name", "Anonymous"));
            userBuilder.setUuid(sharedPreferences.getLong("uuid", -1));
            UserEntity localUser = userBuilder.build();

            Intent intent;
            if (localUser.getUuid() == -1) {
                intent = new Intent(SplashActivity.this, RegisterActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, UserListActivity.class);
            }
            startActivity(intent);
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        Handler mHandler = new Handler();
        mHandler.postDelayed(start, 1000);

        P2PChatDBHelper dbHelper = new P2PChatDBHelper(this);
        dbHelper.getAllUser();
    }
}
