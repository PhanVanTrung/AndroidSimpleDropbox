package vs9.pvt_a1_dropbox;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by VS9 X64Bit on 24/11/2015.
 */
public class FileBrowsing extends AsyncTask<Void, Void, ArrayList<Item>> {
    private DropboxAPI<?> dropboxAPI;
    private String path;
    private Handler handler;
    Context context;
    String cachePath;
    FileOutputStream mFos;
    Drawable mDrawable;
    public FileBrowsing(Context context, DropboxAPI<?> dropboxAPI, String path, Handler handler) {
        super();
        this.context = context;
        this.dropboxAPI = dropboxAPI;
        this.path = path;
        this.handler = handler;
    }
    @Override
    protected ArrayList<Item> doInBackground(Void... params) {
        ArrayList<Item> mArray = new ArrayList<>();
//        ArrayList<DropboxAPI.Entry> thumbs = new ArrayList<>();
        try{
            DropboxAPI.Entry directory = dropboxAPI.metadata(path, 1000, null, true, null);
            for(DropboxAPI.Entry entry : directory.contents){
                // ============Custom==============
                // Get thumbnail. Save it. Then create a drawable source from the saved path.
                if (entry.thumbExists) {
                    cachePath = context.getCacheDir().getPath() + "/" + entry.fileName();
                    File mfile = new File(cachePath);
                    if(!mfile.exists())
                        mfile.createNewFile();
                    try {
                        mFos = new FileOutputStream(cachePath);
                        dropboxAPI.getThumbnail(entry.path, mFos, DropboxAPI.ThumbSize.ICON_64x64, DropboxAPI.ThumbFormat.PNG,null);
                        mDrawable = Drawable.createFromPath(cachePath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(mFos!=null){
                            try {
                                mFos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
//                    thumbs.add(entry);
                    mArray.add(new Item(entry.fileName(), entry.size, entry.path, entry.parentPath(), entry.icon, 2, mDrawable));
                    // ============End Custom==========
                } else if(entry.isDir)
                    mArray.add(new Item(entry.fileName(), entry.size, entry.path, entry.parentPath(), entry.icon, 1));
                else
                    mArray.add(new Item(entry.fileName(), entry.size, entry.path, entry.parentPath(), entry.icon, 0));

            }

        } catch (DropboxException e) {
            e.printStackTrace();
            Log.e("ERROR", "Something went wrong with metadata. Check it!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mArray;
    }

    @Override
    protected void onPostExecute(ArrayList<Item> result) {
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("data", result);
        message.setData(bundle);
        handler.sendMessage(message);
    }
}
