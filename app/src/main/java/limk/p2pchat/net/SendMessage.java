package limk.p2pchat.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import limk.p2pchat.basic.Constant;
import limk.p2pchat.basic.MessageBasic.MessageEntity;

public class SendMessage extends Thread {
	private String mAddress;
	private MessageEntity message;

	public SendMessage(MessageEntity entity, String address, long localUUID) {
		this.message = entity;
		MessageEntity.Builder builder = message.toBuilder();
		builder.setUUID(localUUID);
		message = builder.build();
		this.mAddress = address;
	}

	@Override
	public void run() {
		Socket mSocket;
		if (!mAddress.equals("0.0.0.0")) {
			try {
				InetAddress addr = InetAddress.getByName(mAddress);
				mSocket = new Socket(addr, Constant.TCP_PORT);
				OutputStream outStream = mSocket.getOutputStream();
				int size = message.toByteArray().length;
				byte sizeData[] = new byte[4];
				int temp_int = size;
				for (int i = 0; i < sizeData.length; i++) {
					sizeData[i] = Integer.valueOf(temp_int & 0xff).byteValue();
					temp_int = temp_int >> 8;
				}
				outStream.write(sizeData);
				outStream.write(message.toByteArray());
				outStream.close();
				mSocket.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}