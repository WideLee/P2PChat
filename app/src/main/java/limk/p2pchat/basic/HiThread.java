package limk.p2pchat.basic;

import java.util.List;

/**
 * 保证了一个HiThread对象同一时刻只会有一个Thread对象在执行run()方法， 并且为线程的启动可以添加参数
 *
 * @author Tiga
 */
public abstract class HiThread implements Runnable {
    private volatile Thread mThread;
    private final Object M_LOCK = new Object();
    private List<Object> mParams;

    public boolean start() {
        return start(null);
    }

    public boolean start(List<Object> params) {
        if (mThread == null) {
            synchronized (M_LOCK) {
                if (mThread == null) {
                    mThread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                HiThread.this.run();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                HiThread.this.stop();
                            }
                        }
                    };
                    if (mParams != null) {
                        mParams.clear();
                        mParams = null;
                    }
                    mParams = params;
                    mThread.setDaemon(true);
                    mThread.start();
                    return true;
                }
            }
        }
        return false;
    }

    public void stop() {
        if (mThread != null) {
            synchronized (M_LOCK) {
                if (mThread != null) {
                    mThread.interrupt();
                    mThread = null;
                }
            }
        }
    }

    public List<Object> getParams() {
        return mParams;
    }
}
