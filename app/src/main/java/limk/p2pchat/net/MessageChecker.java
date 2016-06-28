package limk.p2pchat.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.HashMap;

import limk.p2pchat.basic.Constant;
import limk.p2pchat.basic.MessageBasic.MessageEntity;
import limk.p2pchat.database.P2PChatDBHelper;

public class MessageChecker extends Thread {

    private boolean stopFlag = true;
    private Handler mHandler;
    private HashMap<Long, Integer> messageCollection;
    private Context mContext;
    private P2PChatDBHelper dbHelper;

    public MessageChecker(Context pContext, Handler pHandler) {
        this.mHandler = pHandler;
        this.mContext = pContext;
        this.messageCollection = new HashMap<Long, Integer>();
        this.dbHelper = new P2PChatDBHelper(mContext);
    }

    @Override
    public void run() {
        while (stopFlag) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ArrayList<MessageEntity> sendingMessage = dbHelper.getSendingMessage();
            for (MessageEntity messageEntity : sendingMessage) {
                long messageID = messageEntity.getMessageID();
                Integer count = messageCollection.get(messageID);
                String address = dbHelper.getUserAddrByUUID(messageEntity.getUUID());
                if (count == null) {
                    count = 0;
                }
                if (count >= 2) {
                    dbHelper.ChangeMessageStateByID(messageEntity.getMessageID(),
                            MessageEntity.MessageState.SEND_FAIL_VALUE);

                    Message msg = new Message();
                    msg.what = Constant.MESSAGE_SEND_FAIL;
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("messageData", messageEntity.toByteArray());
                    bundle.putString("address", address);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                } else {
                    messageCollection.put(messageID, count + 1);
                    SharedPreferences sharedPreferences = mContext.getSharedPreferences("user",
                            Context.MODE_PRIVATE);
                    long uuid = sharedPreferences.getLong("uuid", -1);
                    new SendMessage(messageEntity, address, uuid).start();
                }
            }
        }
    }

    public void finish() {
        stopFlag = false;
    }
}
