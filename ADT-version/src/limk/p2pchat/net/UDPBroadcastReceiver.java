package limk.p2pchat.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import limk.p2pchat.basic.Constant;
import limk.p2pchat.basic.MessageBasic.MessageEntity;
import limk.p2pchat.basic.MessageBasic.UserEntity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class UDPBroadcastReceiver extends Thread {

	private MulticastSocket mSocket;
	private InetAddress receiveAddress;
	private Context mContext;
	private Handler mHandler;
	private boolean stopFlag = true;
	private UserEntity localUser;

	public UDPBroadcastReceiver(Context pContext, Handler pHandler) {
		this.mContext = pContext;
		this.mHandler = pHandler;

		/**
		 * 新建当前用户信息
		 */
		SharedPreferences sharedPreferences = mContext.getSharedPreferences("user",
				Context.MODE_PRIVATE);
		UserEntity.Builder userBuilder = UserEntity.newBuilder();
		userBuilder.setName(sharedPreferences.getString("name", "Anonymous"));
		userBuilder.setUuid(sharedPreferences.getLong("uuid", -1));
		localUser = userBuilder.build();

		try {
			this.mSocket = new MulticastSocket(Constant.UDP_PORT);
			this.receiveAddress = InetAddress.getByName("224.0.0.1");
			this.mSocket.joinGroup(receiveAddress);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		byte buffer[] = new byte[2048];
		DatagramPacket dataPacket = new DatagramPacket(buffer, 2048);

		while (stopFlag) {
			try {
				/**
				 * 收到好友发来的Hello消息
				 */
				mSocket.receive(dataPacket);
				InetAddress address = dataPacket.getAddress();
				int size = ((((buffer[3] & 0xff) << 24) | ((buffer[2] & 0xff) << 16)
						| ((buffer[1] & 0xff) << 8) | ((buffer[0] & 0xff) << 0)));
				byte messageDate[] = new byte[size];
				System.arraycopy(buffer, 4, messageDate, 0, messageDate.length);
				MessageEntity msgEntity = MessageEntity.parseFrom(messageDate);

				UserEntity user = UserEntity.parseFrom(msgEntity.getMessagePayload());
				System.out.println(user.getUuid() + "::" + user.getName());

				if (user.getUuid() != localUser.getUuid()) {

					Message msg = new Message();
					msg.what = MessageEntity.MessageType.HELLO_VALUE;
					Bundle bundle = new Bundle();
					bundle.putByteArray("messageData", msgEntity.toByteArray());
					bundle.putString("address", address.getHostAddress());
					msg.setData(bundle);
					mHandler.sendMessage(msg);

					/**
					 * 回复自己的信息给好友
					 */
					MessageEntity.Builder builder = MessageEntity.newBuilder();
					builder.setIsRead(true)
							.setUUID(localUser.getUuid())
							.setMessageDate(Constant.getDate())
							.setMessageID(System.currentTimeMillis() * 100000 + localUser.getUuid())
							.setMessageState(MessageEntity.MessageState.SENDING_VALUE)
							.setMessageView(MessageEntity.MessageView.SEND_VALUE)
							.setMessageType(MessageEntity.MessageType.HELLO_VALUE)
							.setMessagePayload(localUser.toByteString());

					MessageEntity message = builder.build();
					new SendMessage(message, address.getHostAddress(), localUser.getUuid()).start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void finish() {
		if (mSocket == null) {
			return;
		} else {
			stopFlag = false;
			mSocket.close();
		}
	}
}
