package limk.p2pchat.basic;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Constant {

    public static final int RECEIVE_PICTURE_MESSAGE = 0;
    public static final int RECEIVE_WORD_MESSAGE = 1;
    public static final int RECEIVE_OTHER_MESSAGE = 2;
    public static final int RECEIVE_HELLO_MESSAGE = 3;
    public static final int RECEIVE_ACK_MESSAGE = 4;
    public static final int MESSAGE_SEND_FAIL = 9;

    public static final int UDP_PORT = 6000;
    public static final int TCP_PORT = 6001;

    public static final String PICTURE_MESSAGE_INTENT = "limk.p2pchat.message.picture";
    public static final String ACK_MESSAGE_INTENT = "limk.p2pchat.message.ack";
    public static final String HELLO_MESSAGE_INTENT = "limk.p2pchat.message.hello";
    public static final String WORD_MESSAGE_INTENT = "limk.p2pchat.message.word";
    public static final String OTHER_MESSAGE_INTENT = "limk.p2pchat.message.other";
    public static final String SENDING_FAIL_INTENT = "limk.p2pchat.send.fail";

    public static final int PICTURE_SEND_PROGRESS = 20;
    public static final int NOTIFICATION_ID = 30;
    public static final int RESEND_MESSAGE = 31;
    public static final int VOICE_SEND_PROGRESS = 32;
    public static final int PLAY_VOICE = 33;

    @SuppressLint("SimpleDateFormat")
    public static String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date curDate = new Date();
        return dateFormat.format(curDate);
    }

    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // cancel_record the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) &&
                (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static Bitmap getVideoThumbnail(String videoPath, int width, int height,
                                     int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public static int getAudioLength(String audioPath) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        int length = 0;
        try {
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
            length = mediaPlayer.getDuration();
            mediaPlayer.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return length;
    }
}
