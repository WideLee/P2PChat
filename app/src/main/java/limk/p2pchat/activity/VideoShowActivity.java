package limk.p2pchat.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.VideoView;

import limk.p2pchat.R;

public class VideoShowActivity extends Activity {

    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.video_layout);

        mVideoView = (VideoView) this.findViewById(R.id.iv_video);
        Uri uri = Uri.parse(getIntent().getStringExtra("data"));
        mVideoView.setVideoURI(uri);

        mVideoView.start();
    }
}
