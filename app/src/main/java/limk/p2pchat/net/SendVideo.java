package limk.p2pchat.net;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import limk.p2pchat.basic.Constant;
import limk.p2pchat.basic.MessageBasic.MessageEntity;
import limk.p2pchat.database.P2PChatDBHelper;

public class SendVideo extends Thread {
    private String mAddress;
    private String videoPath;
    private Handler mHandler;
    private Context mContext;
    private FileManager fileManager;
    private P2PChatDBHelper dbHelper;
    private long otherUUID;
    private long localUUID;

    public SendVideo(Context pContext, String voicePath, long otherUUID, long localUUID,
                     String address, Handler pHandler) {
        this.videoPath = voicePath;
        this.mAddress = address;
        this.mHandler = pHandler;
        this.mContext = pContext;
        this.fileManager = new FileManager(mContext);
        this.dbHelper = new P2PChatDBHelper(mContext);
        this.otherUUID = otherUUID;
        this.localUUID = localUUID;
    }

    @Override
    public void run() {
        updateProgress(0);

        try {
            FileInputStream finStream = new FileInputStream(videoPath);

            updateProgress(5);

            MessageEntity.Builder builder = MessageEntity.newBuilder();
            builder.setIsRead(true).setUUID(otherUUID).setMessageHead(videoPath)
                    .setMessageDate(Constant.getDate())
                    .setMessagePayload(ByteString.copyFromUtf8("Continue"))
                    .setMessageState(MessageEntity.MessageState.SENDING_VALUE)
                    .setMessageType(MessageEntity.MessageType.VIDEO_VALUE)
                    .setMessageView(MessageEntity.MessageView.SEND_VALUE)
                    .setMessageID(System.currentTimeMillis() * 100000 + localUUID);
            Uri uri = fileManager.createMultiMediaCache(builder.build());

            updateProgress(10);

            builder.setMessageHead(uri.getPath());
            dbHelper.insertMessage(builder.build());

            updateProgress(15);

            builder.setUUID(localUUID);

            MessageEntity message = builder.build();
            Socket mSocket;
            if (!mAddress.equals("0.0.0.0")) {
                try {
                    InetAddress addr = InetAddress.getByName(mAddress);
                    mSocket = new Socket(addr, Constant.TCP_PORT);
                    OutputStream outStream = mSocket.getOutputStream();
                    byte messageData[] = message.toByteArray();
                    int size = messageData.length;
                    byte sizeData[] = new byte[4];
                    int temp_int = size;
                    for (int i = 0; i < sizeData.length; i++) {
                        sizeData[i] = Integer.valueOf(temp_int & 0xff).byteValue();
                        temp_int = temp_int >> 8;
                    }
                    outStream.write(sizeData);
                    updateProgress(20);

                    int curSent = 0;
                    int bufferSize = 1024;
                    while (curSent < size) {
                        int remainingData = size - curSent;
                        int length = bufferSize < remainingData ? bufferSize : remainingData;
                        outStream.write(messageData, curSent, length);
                        curSent += length;
                        updateProgress(curSent * 80 / size + 20);
                    }

                    byte[] fileBuffer = new byte[1024];
                    int len = finStream.read(fileBuffer);
                    while (len != -1) {
                        outStream.write(fileBuffer, 0, len);
                        len = finStream.read(fileBuffer);
                    }
                    finStream.close();
                    outStream.close();
                    mSocket.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                updateProgress(100);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    private void updateProgress(int value) {
        Message msg = new Message();
        msg.what = Constant.VOICE_SEND_PROGRESS;
        Bundle bundle = new Bundle();
        bundle.putInt("value", value);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
}