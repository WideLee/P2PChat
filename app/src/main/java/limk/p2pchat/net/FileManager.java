package limk.p2pchat.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import limk.p2pchat.basic.Constant;
import limk.p2pchat.basic.MessageBasic.MessageEntity;

public class FileManager {

    private Context mContext;

    public FileManager(Context pContext) {
        this.mContext = pContext;
    }

    /**
     * 似乎可以不用，暂且保留
     *
     * @param data
     * @return
     */
    public Uri createImageCache(byte[] data) {
        Uri imageUri = Uri.EMPTY;
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        String fileName = Long.toString(System.currentTimeMillis()) + ".jpeg";
        String path = Environment.getExternalStorageDirectory().getPath() + "/P2PChat/image/";
        File imageFile = new File(path + fileName);

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && bmp != null) {
            try {
                File pathDir = new File(path);
                if (!pathDir.exists()) {
                    pathDir.mkdirs();
                }

                if (!imageFile.exists()) {
                    imageFile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(imageFile);
                bmp.compress(CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                imageUri = Uri.fromFile(imageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imageUri;
    }

    public Uri createFileCache(MessageEntity entity) {
        Uri fileUri = Uri.EMPTY;
        String path = Environment.getExternalStorageDirectory().getPath() + "/P2PChat/temp/";
        File file = new File(path + Long.toString(System.currentTimeMillis()) + ".tmp");

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                File pathDir = new File(path);
                if (!pathDir.exists()) {
                    pathDir.mkdirs();
                }

                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                ByteString imagePayload = entity.getMessagePayload();
                fos.write(imagePayload.toByteArray(), 0, imagePayload.toByteArray().length);
                fos.flush();
                fos.close();
                fileUri = Uri.fromFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileUri;
    }

    public byte[] createByteArray(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                if (bitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    return baos.toByteArray();
                } else {
                    FileInputStream in = new FileInputStream(file);
                    ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
                    byte[] b = new byte[4096];
                    int n;
                    while ((n = in.read(b)) != -1) {
                        out.write(b, 0, n);
                    }
                    in.close();
                    out.close();
                    return out.toByteArray();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new byte[1];
    }

    public Uri createFile(String fileName) {

        String path = Environment.getExternalStorageDirectory().getPath() + "/P2PChat/picture/";
        File file = new File(path + fileName);

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                File pathDir = new File(path);
                if (!pathDir.exists()) {
                    pathDir.mkdirs();
                }

                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Uri uri = Uri.fromFile(file);
        return uri;
    }

    public Uri createMultiMediaCache(MessageEntity build) {
        Uri fileUri = Uri.EMPTY;
        String path = Environment.getExternalStorageDirectory().getPath() + "/P2PChat/temp/";
        File file = new File(path + Long.toString(System.currentTimeMillis()) + ".tmp");

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                File pathDir = new File(path);
                if (!pathDir.exists()) {
                    pathDir.mkdirs();
                }

                if (!file.exists()) {
                    file.createNewFile();
                }

                Constant.copyFile(new File(build.getMessageHead()), file);

                fileUri = Uri.fromFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileUri;
    }

    public static Uri createEmptyMultiMediaCache() {
        Uri fileUri = Uri.EMPTY;
        String path = Environment.getExternalStorageDirectory().getPath() + "/P2PChat/temp/";
        File file = new File(path + Long.toString(System.currentTimeMillis()) + ".tmp");

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                File pathDir = new File(path);
                if (!pathDir.exists()) {
                    pathDir.mkdirs();
                }

                if (!file.exists()) {
                    file.createNewFile();
                }

                fileUri = Uri.fromFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileUri;
    }
}
