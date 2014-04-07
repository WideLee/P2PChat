package limk.p2pchat.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import limk.p2pchat.basic.MessageBasic.MessageEntity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MessageDownloader extends Thread {

	private Socket mSocket;
	private InputStream inputStream;
	private Handler mHandler;

	public MessageDownloader(Socket socket, Handler pHandler) {
		this.mSocket = socket;
		this.mHandler = pHandler;
		try {
			inputStream = mSocket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		while (true) {
			try {
				int res = 0;
				int size = 0;
				while (size == 0) {
					byte sizeData[] = new byte[4];
					res = inputStream.read(sizeData);
					size = ((((sizeData[3] & 0xff) << 24) | ((sizeData[2] & 0xff) << 16)
							| ((sizeData[1] & 0xff) << 8) | ((sizeData[0] & 0xff) << 0)));
				}
				byte data[] = new byte[size];
				int isRead = 0;
				byte buffer[] = new byte[1024];
				while (isRead < size) {
					res = inputStream.read(buffer, 0, buffer.length);
					System.arraycopy(buffer, 0, data, isRead, res);
					isRead += res;
				}
				MessageEntity message = MessageEntity.parseFrom(data);

				Log.d("Message Download", message.getMessageHead() + " "
						+ message.getMessagePayload().size());

				if (message.getMessageID() != 0) {
					Message msg = new Message();
					msg.what = message.getMessageType();
					Bundle bundle = new Bundle();
					bundle.putByteArray("messageData", message.toByteArray());
					bundle.putString("address", mSocket.getInetAddress().getHostAddress());
					msg.setData(bundle);
					mHandler.sendMessage(msg);
					TCPServer.downloaders.remove(this);
					finish();
					break;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void finish() throws IOException {
		if (!mSocket.isClosed()) {
			mSocket.close();
		}
		inputStream.close();
	}

}