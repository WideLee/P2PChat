package limk.p2pchat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;

import limk.p2pchat.basic.Constant;
import limk.p2pchat.basic.MessageBasic.MessageEntity;

public class MessageReceiver extends BroadcastReceiver {

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

    public interface EventHandler {
        void onReceiveHelloMessage(MessageEntity message);

        void onReceiveACKMessage(MessageEntity message);

        void onReceiveWordMessage(MessageEntity message);

        void onReceivePictureMessage(MessageEntity message);

        void onReceiveOtherMessage(MessageEntity message);

        void onReceiveSendFailMessage(MessageEntity message);
    }
}
