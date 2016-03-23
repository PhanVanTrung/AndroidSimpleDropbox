package vs9.pvt_a1_dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

/**
 * Created by VS9 X64Bit on 27/11/2015.
 */
public class MoveFileDropbox extends AsyncTask<Void, Void, Boolean> {
    private DropboxAPI<AndroidAuthSession> dropboxAPI;
    Item item;
    Context context;
    String toPath;

    public MoveFileDropbox(DropboxAPI<AndroidAuthSession> dropboxAPI, Item item, Context context, String toPath) {
        this.dropboxAPI = dropboxAPI;
        this.item = item;
        this.context = context;
        this.toPath = toPath +"/"+ item.getFileName();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        DropboxAPI.Entry moveFile = null;
        try {
            moveFile = dropboxAPI.move(item.getFilePath(), toPath);
        } catch (DropboxException e) {
            e.printStackTrace();
            return false;
        }
        if ( moveFile == null) {
            Log.e("Rename error:", "Cannot move to the parent path");
            return false;
        } else
            return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean) {
            Toast.makeText(context, "File successfully moved to"+toPath, Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(context, "Error :/", Toast.LENGTH_SHORT).show();
    }
}
