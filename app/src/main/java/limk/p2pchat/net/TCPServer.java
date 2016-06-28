package limk.p2pchat.net;

import android.os.Handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import limk.p2pchat.basic.Constant;

public class TCPServer extends Thread {

    public static Vector<MessageDownloader> downloaders = new Vector<MessageDownloader>();
    public ServerSocket server;
    private Handler mHandler;
    private boolean stopFlag = true;

    public TCPServer(Handler pHandler) {
        this.mHandler = pHandler;
        try {
            server = new ServerSocket(Constant.TCP_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (stopFlag) {
                Socket socket = server.accept();
                MessageDownloader downloader = new MessageDownloader(socket, mHandler);
                downloader.start();
                if (socket != null) {
                    synchronized (downloaders) {
                        downloaders.addElement(downloader);
                    }
                }
                System.out.println("Current connections: " + downloaders.size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finish() {
        for (int i = 0; i < downloaders.size(); i++) {
            try {
                downloaders.get(i).finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        downloaders.clear();
        stopFlag = false;
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
