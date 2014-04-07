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

public class UDPBroadcastSender extends Thread {

	private MulticastSocket mSocket;
	private Context mContext;
	private UserEntity localUser;

	public UDPBroadcastSender(Context pContext) {
		this.mContext = pContext;
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			mSocket.setTimeToLive(8);

			/**
			 * 广播自己的账户信息给局域网的好友
			 */
			MessageEntity.Builder builder = MessageEntity.newBuilder();
			builder.setIsRead(true).setUUID(localUser.getUuid()).setMessageDate(Constant.getDate())
					.setMessageID(System.currentTimeMillis() * 100000 + localUser.getUuid())
					.setMessageState(MessageEntity.MessageState.SENDING_VALUE)
					.setMessageView(MessageEntity.MessageView.SEND_VALUE)
					.setMessageType(MessageEntity.MessageType.HELLO_VALUE)
					.setMessagePayload(localUser.toByteString());

			MessageEntity message = builder.build();

			int size = message.toByteArray().length;
			byte sizeData[] = new byte[4];
			int temp_int = size;
			for (int i = 0; i < sizeData.length; i++) {
				sizeData[i] = Integer.valueOf(temp_int & 0xff).byteValue();
				temp_int = temp_int >> 8;
			}
			byte messageData[] = message.toByteArray();

			byte msgData[] = new byte[sizeData.length + messageData.length];
			System.arraycopy(sizeData, 0, msgData, 0, sizeData.length);
			System.arraycopy(messageData, 0, msgData, sizeData.length, messageData.length);

			InetAddress address = InetAddress.getByName("224.0.0.1");
			DatagramPacket dataPacket = new DatagramPacket(msgData, msgData.length, address,
					Constant.UDP_PORT);
			mSocket.send(dataPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
