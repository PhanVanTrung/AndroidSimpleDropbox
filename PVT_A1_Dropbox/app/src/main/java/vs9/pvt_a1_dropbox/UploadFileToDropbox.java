package vs9.pvt_a1_dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by VS9 X64Bit on 29/11/2015.
 */

// Using AsyncTask to run connection in background so that it won't affect main UI thread.
public class UploadFileToDropbox extends AsyncTask<Void, Long, Boolean> {
    DropboxAPI<AndroidAuthSession> dropboxAPI;
    Context context;
    File file;
    Item item;
    ProgressDialog mProgress;
    DropboxAPI.UploadRequest mRequest;

    public UploadFileToDropbox(DropboxAPI<AndroidAuthSession> dropboxAPI, Context context, File file, Item item) {
        this.dropboxAPI = dropboxAPI;
        this.context = context;
        this.file = file;
        // Handle if no directory is selected. File is uploaded in root "/"
        if(item==null){
            this.item = new Item("/","/");
            this.item.setIsFolder(0);
        }
        else
            this.item = item;
        // Set up progress dialog
        mProgress = new ProgressDialog(context);
        mProgress.setMax(100);
        mProgress.setMessage("Uploading " + file.getName());
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgress.setProgress(0);
        mProgress.setButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRequest.abort();
            }
        });
        mProgress.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            String newFilePath;
            if (item.getIsFolder() == 1)        // if item is a folder
                newFilePath = item.getFilePath() + "/" + file.getName();
            else if (item!=null){       // if item is not a folder
                newFilePath = item.getParentPath() + "/"+file.getName();
            }else
                newFilePath = "/"+file.getName();
            // Create uploadrequest
            mRequest = dropboxAPI.putFileOverwriteRequest(newFilePath, inputStream, file.length(), new ProgressListener() {
                @Override
                public void onProgress(long bytes, long total) {
                    publishProgress(bytes);
                }   // show progress

                @Override
                public long progressInterval() {
                    return 500;
                }   // Upload progress every 0.5 second
            });
            if (mRequest != null) {
                mRequest.upload();
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DropboxException e) {
            Toast.makeText(context, e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }

        return false;

    }
    // Set up percent
    @Override
    protected void onProgressUpdate(Long... values) {
        int percent = (int) (100.0 * (double) values[0] / file.length() + 0.5);
        mProgress.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        mProgress.dismiss();
        if (aBoolean) {
            Toast.makeText(context, "Succeed", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
    }
}
