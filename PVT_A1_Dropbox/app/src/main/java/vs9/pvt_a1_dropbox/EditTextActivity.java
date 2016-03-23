package vs9.pvt_a1_dropbox;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by VS9 X64Bit on 01/12/2015.
 */
public class EditTextActivity extends Activity {
    File file, dir;
    Item item;
    TextView fileName_txt;
    EditText file_content_txt;
    Button save_btn, cancel_btn;
    String fileName, fileSize, filePath, parentPath, fileType;
    int isFolder;
//    String dirPath = Environment.getExternalStorageDirectory().getPath() + "/DropboxDownload/";
    private DropboxAPI<AndroidAuthSession> dropboxAPI;
    AsyncTask loadprogress;
    FileInputStream fis;
    FileOutputStream fos;
    InputStreamReader isr;
    BufferedReader br;
    boolean finishDownload, finishSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edittext_activity);

        finishDownload = false;
        finishSave = false;

        AppKeyPair appKeyPair = new AppKeyPair(Constants.ACCESS_KEY, Constants.ACCESS_SECRET);   // Create app key pair.
        AndroidAuthSession session;
        // Try loading session key pair in shared preference
        SharedPreferences prefs = getSharedPreferences(Constants.DROPBOX_SHAREDPREF, 0);
        String key = prefs.getString(Constants.ACCESS_KEY, null);
        String secret = prefs.getString(Constants.ACCESS_SECRET, null);
        // Initially set adapter for the file browsing viewstub

        if (key != null && secret != null) {
            // Generate access token from key-pair get from shared pref & set it with session
            AccessTokenPair tokenPair = new AccessTokenPair(key, secret);
            session = new AndroidAuthSession(appKeyPair, Constants.ACCESS_TYPE, tokenPair);
//            Toast.makeText(this, "Session loaded!!!", Toast.LENGTH_SHORT).show();
            loggedIn(true);
            dropboxAPI = new DropboxAPI<>(session);
//            showFiles("/");
        } else {
//            session = new AndroidAuthSession(appKeyPair, Constants.ACCESS_TYPE);
//            loggedIn(false);
//            dropboxAPI = new DropboxAPI<>(session);
            Toast.makeText(this, "Require login again", Toast.LENGTH_SHORT).show();
        }
        showToast("Loading text content");
        // Get data from bundle
        Bundle receivedBundle = this.getIntent().getExtras();
        fileName = receivedBundle.getString("filename");
        filePath = receivedBundle.getString("filepath");
        parentPath = receivedBundle.getString("parentpath");
        fileType = receivedBundle.getString("filetype");
        fileSize = receivedBundle.getString("filesize");
        isFolder = receivedBundle.getInt("isfolder");
        // Create new item instance
        item = new Item(fileName, fileSize, filePath, parentPath, fileType, isFolder);

        fileName_txt = (TextView) findViewById(R.id.file_name);
        file_content_txt = (EditText) findViewById(R.id.file_content);
        save_btn = (Button) findViewById(R.id.save_edit);
        cancel_btn = (Button) findViewById(R.id.cancel_edit);
        fileName_txt.setText(fileName);

        loadprogress = new DownloadTextFile().execute();

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Saving");
                new UpdateTextFile(finishDownload).execute();
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Discarding changes");
                loadprogress.cancel(true);      // Cancel UploadRequest.
                finish();
            }
        });



//        dir = new File(dirPath);
//        dir.mkdirs();

//        try {
//            DownloadFileDropbox fileDownloaded = new DownloadFileDropbox(dropboxAPI, item, this);
//            fileDownloaded.execute();
//        } catch (DropboxException e) {
//            e.printStackTrace();
//        }


//        file = new File(dirPath + item.getFileName());

//        loadText(file);
    }

//    private void loadText(File file) {
//        try {
//            Toast.makeText(EditTextActivity.this, "opening file at: " + file.getPath(), Toast.LENGTH_SHORT).show();
//            fis = new FileInputStream(file);
//            isr = new InputStreamReader(fis);
//            br = new BufferedReader(isr);
//            String linecount;
//            int atline = 0;
//            while ((linecount = br.readLine()) != null) {
//                atline++;
//            }
//            fis.getChannel().position(0);
//            String[] array = new String[atline];
//            String line;
//            int i = 0;
//            while (((line) = br.readLine()) != null) {
//                array[i] = line;
//                file_content_txt.append(line);
//                i++;
//            }
//            fis.close();
//            isr.close();
//            br.close();
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void loggedIn(boolean b) {
        Constants.isUserLoggedIn = b;
    }

    // Download Text File first. Write it in a file so that we can open and read it later.
    private class DownloadTextFile extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                // Open a file to give output (to write); outputstream is passed to getFile() call.
                fos = openFileOutput(fileName, MODE_PRIVATE);
                dropboxAPI.getFile(filePath, null, fos, null);
                fos.close();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (DropboxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                // Read file that is downloaded and then write to the content_txt EditText
                try {
                    fis = openFileInput(fileName);  // open file to give input (to read)
                    isr = new InputStreamReader(fis);   // Stream that is read from fileinputstream;
                    br = new BufferedReader(isr);       // Create a buffer for read stream;
                    String line = br.readLine();
                    while(line!=null){
                        file_content_txt.append(line+"\n");
                        line = br.readLine();
                    }
                    isr.close();        // close reading stream.
                    file_content_txt.setVisibility(View.VISIBLE);
                    finishDownload = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
                showToast("Error downloading");
        }
    }

    private void showToast(String message){
        Toast.makeText(EditTextActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // Read from downloaded file and then display it on the EditText view
    private class UpdateTextFile extends  AsyncTask<String, Void, Boolean>{
        Boolean finished;
        String updatedContent;
        public UpdateTextFile(boolean finished) {
            this.finished = finished;
            file_content_txt.setEnabled(false);
            if(finished)
                updatedContent = file_content_txt.getText().toString();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if(finished){
                try {
                    fos = openFileOutput(fileName, MODE_PRIVATE);
                    fos.write(updatedContent.getBytes());       // Write bytes to output stream.
                    fos.close();
                    fis = openFileInput(fileName);      // Continue  to read file
                    // Pass the inputstream (what is read) to the request to upload.  Length(): file size. Return null if request failed.
                    DropboxAPI.UploadRequest request = dropboxAPI.putFileOverwriteRequest(filePath, fis, updatedContent.length(), null);
                    if (request!=null){
                        request.upload();
//                        dropboxAPI.metadata(filePath, 1000, null, true, null);
                        fis.close();
                        return true;
                    }
                    else
                        fis.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DropboxException e) {
                    e.printStackTrace();
                }
            }
            else{}
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean) {
                showToast("Save successful");
                finishSave = true;
                finish();
            }
            else
                showToast("Error while saving file");
        }
    }
}
