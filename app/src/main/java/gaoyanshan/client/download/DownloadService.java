package gaoyanshan.client.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.ciacho.gys_socket.R;

import java.io.File;

public class DownloadService extends Service {

    private DownloadTask downloadTask;

    private String downloadIp;

    private String downloadPort;

    private String downloadFileName;

    private String fileLenght;
    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(String filename,int progress) {
            getNotificationManager().notify(1, getNotification(filename+"下载中...", progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            // 下载成功时将前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载成功", -1));
            Toast.makeText(DownloadService.this, "下载成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            // 下载失败时将前台服务通知关闭，并创建一个下载失败的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载失败", -1));
            Toast.makeText(DownloadService.this, "下载失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            Toast.makeText(DownloadService.this, "暂停", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "取消", Toast.LENGTH_SHORT).show();
        }

    };

    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class DownloadBinder extends Binder {

        public void startDownload(String ip,String port,String filename,String fileLen) {
            if (downloadTask == null) {
                downloadIp = ip;
                downloadPort=port;
                downloadFileName=filename;
                fileLenght=fileLen;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadIp,downloadPort,downloadFileName,fileLenght);
                startForeground(1, getNotification("下载中...", 0));
//                Toast.makeText(DownloadService.this, "下载中...", Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload() {
            if (downloadTask != null) {
                downloadTask.cancelDownload();
            } else {
//                if (downloadUrl != null) {
//                    // 取消下载时需将文件删除，并将通知关闭
//                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
//                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
//                    File file = new File(directory + fileName);
//                    if (file.exists()) {
//                        file.delete();
//                    }
//                    getNotificationManager().cancel(1);
//                    stopForeground(true);
//                    Toast.makeText(DownloadService.this, "取消", Toast.LENGTH_SHORT).show();
//                }
            }
        }

    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, DownloadActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress >= 0) {
            // 当progress大于或等于0时才需显示下载进度
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }

}
