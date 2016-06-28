package limk.p2pchat.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

import limk.p2pchat.R;

public class ImageShowActivity extends Activity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_layout);

        imageView = (ImageView) this.findViewById(R.id.iv_amplify);
        Uri uri = Uri.parse(getIntent().getStringExtra("data"));
        imageView.setImageURI(uri);
    }
}
