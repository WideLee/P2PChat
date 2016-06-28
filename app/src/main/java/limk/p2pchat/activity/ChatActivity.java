package limk.p2pchat.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import limk.p2pchat.R;
import limk.p2pchat.adapter.MessageAdapter;
import limk.p2pchat.basic.Constant;
import limk.p2pchat.basic.MessageBasic.MessageEntity;
import limk.p2pchat.basic.MessageBasic.UserEntity;
import limk.p2pchat.basic.PreferenceUtils;
import limk.p2pchat.database.P2PChatDBHelper;
import limk.p2pchat.net.FileManager;
import limk.p2pchat.net.SendMessage;
import limk.p2pchat.net.SendPicture;
import limk.p2pchat.net.SendVideo;
import limk.p2pchat.net.SendVoice;
import limk.p2pchat.receiver.MessageReceiver;
import limk.p2pchat.receiver.MessageReceiver.EventHandler;
import limk.p2pchat.view.RecordButton;

public class ChatActivity extends Activity implements EventHandler {

    private static int USE_CAMERA = 0;
    private static int USE_FILE = 1;
    private static int RECORD_VIDEO = 2;

    private Button mBtnSend;
    private Button mBtnBack;
    private EditText mEditTextContent;
    private boolean isSendMultimedia = true;
    private RecordButton mRecordButton;
    private TextView mNameTextView;
    private ProgressDialog progressDialog;
    private int progressValue;

    private MessageAdapter mAdapter;
    private ListView mListView;

    private List<MessageEntity> mDataArrays = new ArrayList<MessageEntity>();
    private P2PChatDBHelper dbHelper;
    private UserEntity localUser;
    private FileManager fileManager;

    private MediaPlayer mMediaPlayer;
    private String currentMedia;

    private long otherUUID;

    private String address;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == Constant.PICTURE_SEND_PROGRESS
                    || msg.what == Constant.VOICE_SEND_PROGRESS) {
                Bundle bundle = msg.getData();
                progressValue = bundle.getInt("value");
                progressDialog.setProgress(progressValue);
                if (progressValue >= 98) {
                    progressDialog.dismiss();
                    updateList();
                }
            } else if (msg.what == Constant.RESEND_MESSAGE) {
                long messageID = (Long) msg.obj;
                MessageEntity message = dbHelper.getMessageByID(messageID);
                dbHelper.ChangeMessageStateByID(messageID, MessageEntity.MessageState.SENDING_VALUE);
                String address = dbHelper.getUserAddrByUUID(message.getUUID());
                new SendMessage(message, address, localUser.getUuid()).start();
                updateList();
            } else if (msg.what == Constant.PLAY_VOICE) {
                String mediaStr = (String) msg.obj;
                if (mediaStr.equals(currentMedia)) {
                    mMediaPlayer.reset();
                    currentMedia = "";
                } else {
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(mediaStr);
                        currentMedia = mediaStr;
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private OnClickListener backButtonOnClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            setResult(RESULT_OK);
            finish();
        }
    };
    private DialogInterface.OnClickListener onImageClick = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0:
                    Intent intent_1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri uri = fileManager.createFile("Picture.jpeg");
                    intent_1.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent_1, USE_CAMERA);
                    break;
                case 1:
                    Intent intent_2 = new Intent();
                    intent_2.setType("image/*");
                    intent_2.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent_2,
                            "Select Picture"), USE_FILE);
                    break;
                case 2:
                    Intent mIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0.5);
                    startActivityForResult(mIntent, RECORD_VIDEO);
                default:
                    break;
            }
        }
    };

    private OnClickListener sendButtonOnClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (isSendMultimedia) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setItems(new String[]{"拍照", "从相册选择", "视频"}, onImageClick);
                builder.setNegativeButton("Cancel", null);
                builder.create().show();
            } else {
                String contString = mEditTextContent.getText().toString();
                if (contString.length() > 0) {
                    sendMessage(contString);
                }
            }
        }
    };
    private TextWatcher contentWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mEditTextContent.getText().toString().equals("")) {
                isSendMultimedia = true;
                mBtnSend.setText("图片");
            } else {
                isSendMultimedia = false;
                mBtnSend.setText("发送");
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        dbHelper = new P2PChatDBHelper(this);
        fileManager = new FileManager(this);
        mMediaPlayer = new MediaPlayer();

        /**
         * 新建当前用户信息
         */
        SharedPreferences sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        UserEntity.Builder userBuilder = UserEntity.newBuilder();
        userBuilder.setName(sharedPreferences.getString("name", "Anonymous"));
        userBuilder.setUuid(sharedPreferences.getLong("uuid", -1));
        localUser = userBuilder.build();

        mNameTextView = (TextView) findViewById(R.id.tv_username);
        mListView = (ListView) findViewById(R.id.listview);
        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnBack.setOnClickListener(backButtonOnClick);
        mBtnSend = (Button) findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(sendButtonOnClick);
        mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);
        mEditTextContent.addTextChangedListener(contentWatcher);

        Intent intent = getIntent();
        otherUUID = intent.getLongExtra("uuid", -1);
        mNameTextView.setText(dbHelper.getUserByUUID(otherUUID).getName());

        address = dbHelper.getUserAddrByUUID(otherUUID);
        mAdapter = new MessageAdapter(this, mHandler, mDataArrays);
        mListView.setAdapter(mAdapter);
        updateList();

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("正在处理图片...");
        progressDialog.setIndeterminate(false);
        progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "后台发送",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressValue = 0;
                        dialog.dismiss();
                    }
                });

        mRecordButton = (RecordButton) findViewById(R.id.btn_voice);
        File voiceDirectory = new File(Environment.getExternalStorageDirectory()
                + "/P2PChat/voice/");
        voiceDirectory.mkdirs();

        final String filePath = Environment.getExternalStorageDirectory()
                + "/P2PChat/voice/Record.amr";
        mRecordButton.setSavePath(filePath);
        mRecordButton
                .setOnFinishedRecordListener(new RecordButton.OnFinishedRecordListener() {

                    @Override
                    public void onFinishedRecord(String audioPath) {
                        Toast.makeText(getApplicationContext(), "录音完成", Toast.LENGTH_SHORT).show();

                        progressDialog.show();
                        new SendVoice(getBaseContext(), filePath, otherUUID, localUser.getUuid(), address, mHandler)
                                .start();
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        String draftContent = PreferenceUtils.getStringValue(this, PreferenceUtils.KEY_MESSAGE_DRAFT);
        mEditTextContent.setText(draftContent);
        updateList();
        MessageReceiver.handlers.add(this);
    }

    @Override
    protected void onPause() {
        MessageReceiver.handlers.remove(this);
        PreferenceUtils.saveStringValue(this, PreferenceUtils.KEY_MESSAGE_DRAFT, mEditTextContent.getText().toString());
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMediaPlayer.release();
        super.onDestroy();
    }

    public void updateList() {
        mDataArrays.clear();
        dbHelper.readMessageByUUID(otherUUID);
        ArrayList<MessageEntity> msgEntities = dbHelper.getUserMessageByUUID(otherUUID);
        for (MessageEntity entity : msgEntities) {
            mDataArrays.add(entity);
        }
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(mListView.getCount() - 1);
    }

    /**
     * 发送文字信息
     *
     * @throws ParseException
     */
    private void sendMessage(String sendContent) throws ParseException {

        MessageEntity.Builder builder = MessageEntity.newBuilder();
        builder.setIsRead(true).setUUID(otherUUID).setMessageDate(Constant.getDate())
                .setMessageID(System.currentTimeMillis() * 100000 + localUser.getUuid())
                .setMessageState(MessageEntity.MessageState.SENDING_VALUE)
                .setMessageView(MessageEntity.MessageView.SEND_VALUE)
                .setMessageType(MessageEntity.MessageType.WORD_VALUE).setMessageHead(sendContent)
                .setMessagePayload(ByteString.copyFromUtf8(sendContent));

        MessageEntity message = builder.build();

        dbHelper.insertMessage(message);
        mEditTextContent.setText("");
        updateList();

        builder.setUUID(localUser.getUuid());
        new SendMessage(builder.build(), address, localUser.getUuid()).start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == USE_CAMERA && resultCode == RESULT_OK) {

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String filePath = Environment.getExternalStorageDirectory()
                        + "/P2PChat/picture/Picture.jpeg";
                progressDialog.show();
                new SendPicture(this, filePath, otherUUID, localUser.getUuid(), address, mHandler)
                        .start();
            }
        } else if (requestCode == USE_FILE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            String filePath = imageUri.getPath();
            progressDialog.show();
            new SendPicture(this, filePath, otherUUID, localUser.getUuid(), address, mHandler)
                    .start();
        } else if (requestCode == RECORD_VIDEO && resultCode == RESULT_OK) {
            try {
                AssetFileDescriptor videoAsset = getContentResolver()
                        .openAssetFileDescriptor(data.getData(), "r");
                FileInputStream fis = videoAsset.createInputStream();
                File tmpDir = new File(Environment.getExternalStorageDirectory()
                        + "/P2PChat/video/");
                tmpDir.mkdirs();
                File tmpFile = new File(
                        Environment.getExternalStorageDirectory()
                                + "/P2PChat/video/Video.mp4");
                FileOutputStream fos = new FileOutputStream(tmpFile);

                byte[] buf = new byte[1024];
                int len;
                while ((len = fis.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
                fis.close();
                fos.close();

                progressDialog.show();
                new SendVideo(getBaseContext(), tmpFile.getAbsolutePath(), otherUUID, localUser.getUuid(), address, mHandler)
                        .start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateList();
    }

    @Override
    public void onReceiveHelloMessage(MessageEntity message) {
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
    public void onReceiveSendFailMessage(MessageEntity message) {
        updateList();
    }
}
