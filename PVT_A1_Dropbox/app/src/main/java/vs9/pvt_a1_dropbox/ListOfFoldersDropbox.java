package vs9.pvt_a1_dropbox;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.util.ArrayList;


/**
 * Created by VS9 X64Bit on 27/11/2015.
 */

// Return ArrayList<String>, meaning it includes all subdir in a dir
public class ListOfFoldersDropbox extends AsyncTask<Void, Void, ArrayList<String>> {
    DropboxAPI<AndroidAuthSession> dropboxAPI;
    String path;
    Handler handler;

    public ListOfFoldersDropbox(DropboxAPI<AndroidAuthSession> dropboxAPI, String path, Handler handler) {
        this.dropboxAPI = dropboxAPI;
        this.path = path;
        this.handler = handler;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... params) {
        ArrayList<String> folders = new ArrayList<>();
        try{
            DropboxAPI.Entry entry = dropboxAPI.metadata(path, 1000, null, true, null);
            for (DropboxAPI.Entry ent : entry.contents){
                if (ent.isDir){
                    folders.add(ent.path);
                }
            }
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return folders;
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("data", strings);
        message.setData(bundle);
        handler.sendMessage(message);
    }
}
