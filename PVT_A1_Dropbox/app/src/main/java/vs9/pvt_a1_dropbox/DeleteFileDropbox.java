package vs9.pvt_a1_dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

/**
 * Created by VS9 X64Bit on 26/11/2015.
 */
public class DeleteFileDropbox extends AsyncTask<Void, Void, Boolean>{
    DropboxAPI<AndroidAuthSession> dropboxAPI;
    Item item;
    Context context;
    boolean result;

    public DeleteFileDropbox(DropboxAPI<AndroidAuthSession> dropboxAPI, Item item, Context context) {
        this.dropboxAPI = dropboxAPI;
        this.item = item;
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (item.getIsFolder()==1) {
                dropboxAPI.delete(item.getFilePath() + "/");
            } else {
                dropboxAPI.delete(item.getFilePath());
            }
            result = true;
        }
        catch (DropboxException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
//        super.onPostExecute(aBoolean);
        if(result)
            Toast.makeText(context, "Delete item successfully", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Error deleting the item", Toast.LENGTH_SHORT).show();
    }
}
