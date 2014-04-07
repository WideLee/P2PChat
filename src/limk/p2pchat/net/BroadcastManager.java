package limk.p2pchat.net;

import android.content.Context;

public class BroadcastManager extends Thread {

	private Context mContext;
	private boolean stopFlag = true;

	public BroadcastManager(Context pContext) {
		this.mContext = pContext;
	}

	@Override
	public void run() {
		try {
			while (stopFlag) {
				new UDPBroadcastSender(mContext).start();
				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void finish() {
		stopFlag = false;
	}
}
