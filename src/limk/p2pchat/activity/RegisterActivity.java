package limk.p2pchat.activity;

import limk.p2pchat.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivity extends Activity {

	private EditText nameEditText;
	private TextView uuidTextView;
	private Button okButton;
	private Button continueButton;
	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_register);

		sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();

		nameEditText = (EditText) this.findViewById(R.id.et_name);
		uuidTextView = (TextView) this.findViewById(R.id.tv_display_uuid);
		okButton = (Button) this.findViewById(R.id.btn_ok);
		continueButton = (Button) this.findViewById(R.id.btn_continue);

		okButton.setOnClickListener(okClickListener);
		continueButton.setOnClickListener(continueClickListener);
	}

	private OnClickListener okClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			okButton.setVisibility(View.GONE);
			nameEditText.setEnabled(false);

			long uuid = System.currentTimeMillis() % 100000;
			String name = nameEditText.getText().toString();
			editor.putString("name", name);
			editor.putLong("uuid", uuid);
			editor.commit();
			uuidTextView.setText(getResources().getString(R.string.display_uuid) + uuid);
			uuidTextView.setVisibility(View.VISIBLE);
			continueButton.setVisibility(View.VISIBLE);
		}
	};

	private OnClickListener continueClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(RegisterActivity.this, UserListActivity.class);
			startActivity(intent);
			finish();
		}
	};
}
