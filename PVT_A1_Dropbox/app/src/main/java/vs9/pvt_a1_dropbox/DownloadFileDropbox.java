package vs9.pvt_a1_dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by VS9 X64Bit on 26/11/2015.
 */
public class DownloadFileDropbox extends AsyncTask<Void, Long, Boolean> {
    private DropboxAPI<AndroidAuthSession> dropboxAPI;
    private Item item;
    Context context;
    FileOutputStream os;
    private long fileSize;
    ProgressDialog dialog;
    boolean isAborted, cont;
    String localFileDest;
    File localFile;
    String dropDir = Environment.getExternalStorageDirectory().getPath()+"/DropboxDowload/";

    public DownloadFileDropbox(DropboxAPI<AndroidAuthSession> dropboxAPI, Item item, Context context) throws DropboxException {
        this.dropboxAPI = dropboxAPI;
        this.item = item;
        this.context = context;
        showProgress();
        File file = new File(dropDir);
        if (!file.exists()){
            file.mkdirs();
        }
        localFile = new File(dropDir+item.getFileName());
    }

    private void showProgress() {
        dialog = new ProgressDialog(context);
        dialog.setMax(100);
        dialog.setMessage("Downloading");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Abort", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isAborted = true;
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        dialog.show();
    }

    public String getPath() {
        String path = "";
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getPath();
        } else if ((new File("/mnt/emmc")).exists()) {
            path = "/mnt/emmc";
        } else {
            path = Environment.getExternalStorageDirectory().getPath();
        }
        return path + "/Dropbox/" + item.getFileName();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        super.onProgressUpdate();
        int percent = (int) (100 * (double) progress[0] / fileSize + 0.5);
        dialog.setProgress(percent);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (isAborted) {
            return false;
        }
        try {
            if (!localFile.exists())
                localFile.createNewFile();  // otherwise app closes silently
            else {
                os = new FileOutputStream(localFile);       // where the file is writen in
                DropboxAPI.DropboxFileInfo fileInfo = dropboxAPI.getFile(item.getFilePath(), null, os, null);       // go download the file, pass outputstream as param
                fileSize = fileInfo.getFileSize();
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return false;
    }

//    private void showFileExitsDialog(String filePath, File localFile) {
//        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
//        alertBuilder.setMessage("File name with this name already exists.Do you want to replace this file?");
//        alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                cont = true;
//            }
//        });
//        alertBuilder.create().show();
//    }

    @Override
    protected void onPostExecute(Boolean result) {
        dialog.dismiss();
        if (result) {
            Toast.makeText(context, "File saved at " + localFile.getPath(), Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();

    }
}
