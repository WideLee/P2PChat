package limk.p2pchat.service;

import limk.p2pchat.R;
import limk.p2pchat.activity.UserListActivity;
import limk.p2pchat.basic.Constant;
import limk.p2pchat.basic.MessageBasic.MessageEntity;
import limk.p2pchat.basic.MessageBasic.UserEntity;
import limk.p2pchat.database.P2PChatDBHelper;
import limk.p2pchat.net.BroadcastManager;
import limk.p2pchat.net.FileManager;
import limk.p2pchat.net.MessageChecker;
import limk.p2pchat.net.SendMessage;
import limk.p2pchat.net.TCPServer;
import limk.p2pchat.net.UDPBroadcastReceiver;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class ServerService extends Service {

	private UDPBroadcastReceiver mUDPReceiver;
	private TCPServer mTCPServer;
	private P2PChatDBHelper dbHelper;
	private FileManager fileManager;
	private MessageChecker messageChecker;
	private NotificationManager notificationManager;
	private PendingIntent pendingIntent;
	private Notification baseNF = new Notification();
	private BroadcastManager broadcastManager;

	private Handler mHandler;
	private UserEntity localUser;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferences sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
		UserEntity.Builder userBuilder = UserEntity.newBuilder();
		userBuilder.setName(sharedPreferences.getString("name", "Anonymous"));
		userBuilder.setUuid(sharedPreferences.getLong("uuid", -1));
		localUser = userBuilder.build();

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Intent intent = new Intent(this, UserListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		pendingIntent = PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				super.handleMessage(msg);
				Bundle bundle = msg.getData();
				byte message[] = bundle.getByteArray("messageData");

				MessageEntity messageEntity = MessageEntity.getDefaultInstance();
				try {
					messageEntity = MessageEntity.parseFrom(message);
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}
				MessageEntity.Builder messageBuilder = messageEntity.toBuilder();
				messageBuilder.setMessageState(MessageEntity.MessageState.SEND_OK_VALUE);
				messageBuilder.setMessageView(MessageEntity.MessageView.RECEIVE_VALUE);
				messageBuilder.setIsRead(false);
				messageEntity = messageBuilder.build();
				String address = bundle.getString("address");

				switch (msg.what) {
				case MessageEntity.MessageType.HELLO_VALUE:
					/**
					 * 收到承载用户信息的Hello消息
					 */
					UserEntity user = UserEntity.getDefaultInstance();
					try {
						user = UserEntity.parseFrom(messageEntity.getMessagePayload());
					} catch (InvalidProtocolBufferException e) {
						e.printStackTrace();
					}

					dbHelper.insertUser(user);
					dbHelper.updateUserAddressByUUID(address, user.getUuid());

					Intent intent = new Intent(Constant.HELLO_MESSAGE_INTENT);
					intent.putExtra("messageData", messageEntity.toByteArray());
					sendBroadcast(intent);
					break;
				case MessageEntity.MessageType.ACK_VALUE:
					/**
					 * 收到消息确认ACK
					 */
					String idString = messageEntity.getMessagePayload().toStringUtf8();

					long messageID = Long.parseLong(idString);
					long uuid = messageEntity.getUUID();
					if (uuid != localUser.getUuid()) {
						dbHelper.ChangeMessageStateByID(messageID,
								MessageEntity.MessageState.SEND_OK_VALUE);
					}

					Intent intent_2 = new Intent(Constant.ACK_MESSAGE_INTENT);
					intent_2.putExtra("messageData", messageEntity.toByteArray());
					sendBroadcast(intent_2);
					break;
				case MessageEntity.MessageType.WORD_VALUE:
					/**
					 * 收到文字消息
					 */
					dbHelper.insertMessage(messageEntity);
					sendACKMessage(messageEntity, address);

					Intent intent_3 = new Intent(Constant.WORD_MESSAGE_INTENT);
					intent_3.putExtra("messageData", messageEntity.toByteArray());
					sendBroadcast(intent_3);

					sendNotification("P2PChat", dbHelper.getUserByUUID(messageEntity.getUUID())
							.getName() + ": " + messageEntity.getMessageHead());
					break;
				case MessageEntity.MessageType.PICTURE_VALUE:
					/**
					 * 收到图片消息
					 */
					Uri uri_1 = fileManager.createFileCache(messageEntity);

					messageBuilder = messageEntity.toBuilder();
					messageBuilder.setMessageHead(uri_1.getPath());
					messageEntity = messageBuilder.build();

					dbHelper.insertMessage(messageEntity);
					sendACKMessage(messageEntity, address);

					Intent intent_4 = new Intent(Constant.PICTURE_MESSAGE_INTENT);
					intent_4.putExtra("messageData", messageEntity.toByteArray());
					sendBroadcast(intent_4);

					sendNotification("P2PChat", dbHelper.getUserByUUID(messageEntity.getUUID())
							.getName() + ": [图片]");
					break;
				case MessageEntity.MessageType.OTHER_VALUE:
					/**
					 * 收到其他消息
					 */
					Uri uri_2 = fileManager.createFileCache(messageEntity);

					messageBuilder = messageEntity.toBuilder();
					messageBuilder.setMessageHead(uri_2.getPath());
					messageEntity = messageBuilder.build();

					dbHelper.insertMessage(messageEntity);
					sendACKMessage(messageEntity, address);

					Intent intent_5 = new Intent(Constant.OTHER_MESSAGE_INTENT);
					intent_5.putExtra("messageData", messageEntity.toByteArray());
					sendBroadcast(intent_5);

					sendNotification("P2PChat", dbHelper.getUserByUUID(messageEntity.getUUID())
							.getName() + ": [文件]");
					break;
				case Constant.MESSAGE_SEND_FAIL:
					Intent intent_6 = new Intent(Constant.SENDING_FAIL_INTENT);
					intent_6.putExtra("messageData", messageEntity.toByteArray());
					sendBroadcast(intent_6);
				default:
					break;
				}
			}
		};

		mUDPReceiver = new UDPBroadcastReceiver(this, mHandler);
		mTCPServer = new TCPServer(mHandler);
		dbHelper = new P2PChatDBHelper(this);
		fileManager = new FileManager(this);
		messageChecker = new MessageChecker(this, mHandler);
		broadcastManager = new BroadcastManager(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mUDPReceiver.start();
		mTCPServer.start();
		messageChecker.start();
		broadcastManager.start();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		mUDPReceiver.finish();
		mTCPServer.finish();
		messageChecker.finish();
		broadcastManager.finish();
		super.onDestroy();
	}

	/**
	 * 发送ACK确认消息
	 * 
	 * @param msgEntity
	 *            : 确认收到的消息
	 * @param address
	 *            : 要将确认消息发送到的IP地址
	 */
	public void sendACKMessage(MessageEntity msgEntity, String address) {

		String msgId = Long.toString(msgEntity.getMessageID());

		MessageEntity.Builder ackBuilder = MessageEntity.newBuilder();
		ackBuilder.setIsRead(true).setMessageDate(Constant.getDate())
				.setMessageID(System.currentTimeMillis() * 100000 + localUser.getUuid())
				.setMessageState(MessageEntity.MessageState.SENDING_VALUE)
				.setMessageView(MessageEntity.MessageView.SEND_VALUE)
				.setMessageType(MessageEntity.MessageType.ACK_VALUE)
				.setMessagePayload(ByteString.copyFromUtf8(msgId)).setUUID(localUser.getUuid());

		MessageEntity ackMessage = ackBuilder.build();
		new SendMessage(ackMessage, address, localUser.getUuid()).start();
	}

	@SuppressWarnings("deprecation")
	private void sendNotification(String title, String text) {

		baseNF.icon = R.drawable.ic_launcher;
		baseNF.tickerText = text;
		baseNF.defaults = Notification.DEFAULT_ALL;
		baseNF.flags |= Notification.FLAG_AUTO_CANCEL;
		baseNF.setLatestEventInfo(this, title, text, pendingIntent);
		notificationManager.notify(Constant.NOTIFICATION_ID, baseNF);
	}
}
