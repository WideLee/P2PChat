package limk.p2pchat.receiver;

import java.util.ArrayList;

import com.google.protobuf.InvalidProtocolBufferException;

import limk.p2pchat.basic.Constant;
import limk.p2pchat.basic.MessageBasic.MessageEntity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MessageReceiver extends BroadcastReceiver {

	public static interface EventHandler {
		public abstract void onReceiveHelloMessage(MessageEntity message);

		public abstract void onReceiveACKMessage(MessageEntity message);

		public abstract void onReceiveWordMessage(MessageEntity message);

		public abstract void onReceivePictureMessage(MessageEntity message);

		public abstract void onReceiveOtherMessage(MessageEntity message);

		public abstract void onReceiveSendFailMessage(MessageEntity message);
	}

	public static ArrayList<EventHandler> handlers = new ArrayList<MessageReceiver.EventHandler>();

	@Override
	public void onReceive(Context pContext, Intent pIntent) {
		try {
			if (pIntent.getAction().equals(Constant.HELLO_MESSAGE_INTENT)) {
				MessageEntity message;
				message = MessageEntity.parseFrom(pIntent.getByteArrayExtra("messageData"));
				for (int i = 0; i < handlers.size(); i++) {
					handlers.get(i).onReceiveHelloMessage(message);
				}
			} else if (pIntent.getAction().equals(Constant.ACK_MESSAGE_INTENT)) {
				MessageEntity message = MessageEntity.parseFrom(pIntent
						.getByteArrayExtra("messageData"));
				for (int i = 0; i < handlers.size(); i++) {
					handlers.get(i).onReceiveACKMessage(message);
				}
			} else if (pIntent.getAction().equals(Constant.WORD_MESSAGE_INTENT)) {
				MessageEntity message;
				message = MessageEntity.parseFrom(pIntent.getByteArrayExtra("messageData"));
				for (int i = 0; i < handlers.size(); i++) {
					handlers.get(i).onReceiveWordMessage(message);
				}
			} else if (pIntent.getAction().equals(Constant.PICTURE_MESSAGE_INTENT)) {
				MessageEntity message;
				message = MessageEntity.parseFrom(pIntent.getByteArrayExtra("messageData"));
				for (int i = 0; i < handlers.size(); i++) {
					handlers.get(i).onReceivePictureMessage(message);
				}
			} else if (pIntent.getAction().equals(Constant.OTHER_MESSAGE_INTENT)) {
				MessageEntity message;
				message = MessageEntity.parseFrom(pIntent.getByteArrayExtra("messageData"));
				for (int i = 0; i < handlers.size(); i++) {
					handlers.get(i).onReceiveOtherMessage(message);
				}
			} else if (pIntent.getAction().equals(Constant.SENDING_FAIL_INTENT)) {
				MessageEntity message;
				message = MessageEntity.parseFrom(pIntent.getByteArrayExtra("messageData"));
				for (int i = 0; i < handlers.size(); i++) {
					handlers.get(i).onReceiveSendFailMessage(message);
				}
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
}
