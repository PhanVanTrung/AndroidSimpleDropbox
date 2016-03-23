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

// Extends AsyncTask to run in background
public class RenameFileDropbox extends AsyncTask<Void, Void, Boolean>{
    private DropboxAPI<AndroidAuthSession> dropboxAPI;
    private Item item;
    private Context context;
    private String newName;

    public RenameFileDropbox(DropboxAPI<AndroidAuthSession> dropboxAPI, Item item, Context context, String newName) {
        this.dropboxAPI = dropboxAPI;
        this.item = item;
        this.context = context;
        this.newName = newName;
    }



    @Override
    protected Boolean doInBackground(Void... params) {

        try {
            String originalPath = item.getParentPath()+newName;

//            DropboxAPI.Entry tempEntry = dropboxAPI.move(item.getFilePath(), "/");
            // Use move API function to overwrite the same file at the same file path with different name.
            DropboxAPI.Entry destEntry = dropboxAPI.move(item.getFilePath(), originalPath);
//            if(tempEntry==null){
//                Log.e("Rename error:", "Cannot move to the new temporary path");
//                return false;
//            }
            if (destEntry==null){
                Log.e("Rename error:", "Cannot move to the parent path");
                return false;
            }
            else
                return true;
        } catch (DropboxException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if(aBoolean){
            Toast.makeText(context,"Successfull renaming", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(context,"Error :/", Toast.LENGTH_SHORT).show();
    }
}
