package vs9.pvt_a1_dropbox;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_RESULT = 1;
    public DropboxAPI<AndroidAuthSession> dropboxAPI;
    private boolean listview_visible = false;
    private Button Login_btn;
    private TextView txt1;
    ViewStub listview_stub, gridview_stub;
    private ListView listView;
    private GridView gridView;
    private ListViewAdapter lva;
    private GridViewAdapter gva;
    private ArrayAdapter adapter;
    private ArrayList<Item> mArray = new ArrayList<Item>();
    private ArrayList<String> listOfFoldersDropbox = new ArrayList<>();
    CharSequence options[];
    AlertDialog.Builder builder;
    private static Item item;
    FileBrowsing fileBrowsing;
    private String newName;
    Menu menu;
//    MenuItem changeview;


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle saveBundleInstance) {
        super.onCreate(saveBundleInstance);
        setContentView(R.layout.activity_main);

        Login_btn = (Button) findViewById(R.id.login_btn);
        Login_btn.setOnClickListener(this);
        txt1 = (TextView) findViewById(R.id.txt1);
        listview_stub = (ViewStub) findViewById(R.id.listview_stub);
        gridview_stub = (ViewStub) findViewById(R.id.gridview_stub);

        listview_stub.inflate();
        gridview_stub.inflate();

        listView = (ListView) findViewById(R.id.listview);
        gridView = (GridView) findViewById(R.id.gridview);

        // Initially set up the adapter so that it can render content.
        adapter = new ListViewAdapter(this, R.layout.custom_component_listview, mArray);
        listView.setAdapter(adapter);

        // Create app key pair.
        AppKeyPair appKeyPair = new AppKeyPair(Constants.ACCESS_KEY, Constants.ACCESS_SECRET);
        AndroidAuthSession session;
        // Try loading session key pair in shared preference
        SharedPreferences prefs = getSharedPreferences(Constants.DROPBOX_SHAREDPREF, 0);
        String key = prefs.getString(Constants.ACCESS_KEY, null);
        String secret = prefs.getString(Constants.ACCESS_SECRET, null);
        // If session is stored in shared preferences, load it and then user don't have to allow access everytime app starts.
        if (key != null && secret != null) {
            // Generate access token from key-pair get from shared pref & set it with session
            AccessTokenPair tokenPair = new AccessTokenPair(key, secret);
            session = new AndroidAuthSession(appKeyPair, Constants.ACCESS_TYPE, tokenPair);
            showToast("Loading Session");
            loggedIn(true);
            dropboxAPI = new DropboxAPI<>(session);
            showFiles("/");
        } else {
            session = new AndroidAuthSession(appKeyPair, Constants.ACCESS_TYPE);
            loggedIn(false);
            dropboxAPI = new DropboxAPI<>(session);
        }
        // Set initial value for the list of folders. This will be used for move function
        listOfFoldersDropbox.add(0, "/");

//        Toast.makeText(this, String.valueOf(Constants.isUserLoggedIn), Toast.LENGTH_SHORT).show();

        onListItemClickListener();
        onGridItemClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AndroidAuthSession session = dropboxAPI.getSession();
        // Finish authentication and then store session in shared preference
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                TokenPair tokenPair = session.getAccessTokenPair();
                // Store token pair in shared prefs for next app run
                SharedPreferences prefs = getSharedPreferences(Constants.DROPBOX_SHAREDPREF, 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Constants.ACCESS_KEY, tokenPair.key);
                editor.putString(Constants.ACCESS_SECRET, tokenPair.secret);
                editor.commit();

                listView.setVisibility(View.VISIBLE);

                showFiles("/");

                changeView();
                loggedIn(true);
                showToast("Loading dropbox files");
            } catch (IllegalStateException e) {
                showToast("Error during Dropbox authentication");
            }
        }
    }
    // Rename function.
    protected void Rename(final Item selectedItem) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.newname);
        // Set up input;
        final EditText input = new EditText(MainActivity.this);
        // Specify input type
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input); // Customize dialog to have view as bodyview
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newName = input.getText().toString();
                Toast.makeText(MainActivity.this, newName, Toast.LENGTH_SHORT).show();
                // Execute Rename function in background.
                RenameFileDropbox rename = new RenameFileDropbox(dropboxAPI, selectedItem, MainActivity.this, newName);
                rename.execute();
                selectedItem.setFileName(newName);
                selectedItem.setFilePath(selectedItem.getParentPath() + newName);
                adapter.notifyDataSetChanged();
            }
        });
        builder.show();
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {

    }

    // Move function.
    protected void Move(String path) {
        final AlertDialog.Builder moveBuilder = new AlertDialog.Builder(this);
        final String[] originalPath = {path};
        final Item selectedItem = item;
        // Initially, folder chooser always display "/" root first.
        getAllFolders(originalPath[0]);
        moveBuilder.setTitle(R.string.destination);

        final CharSequence[] destinations = listOfFoldersDropbox.toArray(new CharSequence[listOfFoldersDropbox.size()]);

        moveBuilder.setSingleChoiceItems(destinations, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                originalPath[0] = (String) destinations[which];
            }
        });
        moveBuilder.setNegativeButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                // Execute Movefile in background.
                MoveFileDropbox moveFile = new MoveFileDropbox(dropboxAPI, selectedItem, MainActivity.this, originalPath[0]);
                moveFile.execute();
                item = new Item(selectedItem.getParentPath().substring(selectedItem.getParentPath().lastIndexOf("/")));
                showFiles(selectedItem.getParentPath());
//                Toast.makeText(MainActivity.this, "selected folder: " + originalPath[0], Toast.LENGTH_LONG).show();
            }
        });
        moveBuilder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Move(originalPath[0]);
                dialog.dismiss();
            }
        });
        moveBuilder.show();

//        AlertDialog dialog = moveBuilder.create();
//        dialog.show();
    }

    // =============================LruCache========================
//    private LruCache<String, Bitmap> mMemoryCache;
//
//    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
//        @Override
//        protected int sizeOf(String key, Bitmap bitmap) {
//            // The cache size will be measured in kilobytes rather than
//            // number of items.
//            return bitmap.getByteCount() / 1024;
//        }
//    };
//
//    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
//        // Add bitmap to cache memory
//        if (getBitmapFromMemCache(key) == null) {
//            mMemoryCache.put(key, bitmap);
//        }
//    }
//
//    public Bitmap getBitmapFromMemCache(String key) {
//        // Get bitmap from cache memory
//        return mMemoryCache.get(key);
//    }
    // =============================LruCache========================

    // Run this method to update the folder chooser (alert dialog content)
    protected void getAllFolders(String path) {
        ListOfFoldersDropbox listFolders = new ListOfFoldersDropbox(dropboxAPI, path, listFoldersPathHandler);
        listFolders.execute();
    }

    // Delete function
    protected void Delete(final Item selectedItem) {
        final AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(MainActivity.this);
        confirmBuilder.setTitle(R.string.confirm);
        confirmBuilder.setMessage(R.string.confirm_delete);
        // Save parent path so that we can display the parent folder properly after deleting the item.
        String parent = selectedItem.getParentPath().substring(0, selectedItem.getParentPath().lastIndexOf("/"));
        if (parent.equals("") || parent == null) {
            parent = "/";
        }
        item = new Item(parent, parent);
        item.setIsFolder(1);
        confirmBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        // TODO Auto-generated method stub
                        DeleteFileDropbox deleteFileDropbox = new DeleteFileDropbox(dropboxAPI, selectedItem, MainActivity.this);
                        deleteFileDropbox.execute();
//                        Toast.makeText(MainActivity.this, "Deleting", Toast.LENGTH_SHORT).show();
                        showFiles(item.getFilePath());
                    }
                });

        confirmBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        // TODO Auto-generated method stub
                        dialog.cancel();
                    }
                });
        AlertDialog confirmDialog = confirmBuilder.create();    //here goes the real creation of dialog
        confirmDialog.show();
    }

    // Download function. Run it in background
    private void Download(Item selectedItem){
        try {
            DownloadFileDropbox downloadFileDropbox = new DownloadFileDropbox(dropboxAPI, selectedItem, MainActivity.this);
            downloadFileDropbox.execute();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
    }

    // Text editor function here. Start new activity by using intent. Pass all info about the selected item with intent data.
    void EditTextFile(Item item){
        String filename = item.getFileName();
        String filepath = item.getFilePath();
        String parentpath = item.getParentPath();
        Intent mIntent = new Intent(MainActivity.this, EditTextActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putString("filename", filename);
        mBundle.putString("filesize", item.getFileSize());
        mBundle.putString("filepath", filepath);
        mBundle.putString("parentpath", parentpath);
        mBundle.putString("filetype", item.getFileType());
        mBundle.putInt("isfolder", item.getIsFolder());
        mIntent.putExtras(mBundle);
        startActivityForResult(mIntent, REQUEST_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        item = new Item(item.getParentPath().substring(0, item.getParentPath().lastIndexOf("/")));
        showFiles(item.getFilePath());
    }

    void setListener(AdapterView adapterView) {
        adapterView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                item = (Item) adapter.getItem(position);
                if (item.getIsFolder() == 1) {  // If item is a directory, show its content
                    showFiles(item.getFilePath());
                } else if (item.getFileType().contains("text")) {
                    EditTextFile(item);
                } else {
                    showToast("Cannot open this file type.");
                    String parentpath = item.getParentPath().substring(0, item.getParentPath().lastIndexOf("/"));
                    item = new Item(parentpath, parentpath);
                    item.setIsFolder(1);
                }
            }
        });
        adapterView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                item = (Item) adapter.getItem(position);
                if (item.getIsFolder() == 1) {
                    options = new CharSequence[]{"Move", "Delete", "Rename"};
                    builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.options);
                    builder.setIcon(android.R.drawable.stat_notify_more);
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    Move("/");
                                    break;
                                case 1:
                                    Delete(item);
                                    break;
                                case 2:
                                    Rename(item);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                } else {
                    options = new CharSequence[]{"Move", "Delete", "Rename", "Download"};
                    builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.options);
                    builder.setIcon(android.R.drawable.stat_notify_more);
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    Move("/");
                                    break;
                                case 1:
                                    Delete(item);
                                    break;
                                case 2:
                                    Rename(item);
                                    break;
                                case 3:
                                    Download(item);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
                builder.create().show();
                return false;
            }
        });
    }

    private void onListItemClickListener() {
        setListener(listView);
    }

    private void onGridItemClickListener() {
        setListener(gridView);
    }

    // Open Directory and show its content
    private void showFiles(String path) {
        if (path.equals("/") || path.equals(""))
            showToast("Loading dir \"/\"");
        else
            showToast("Loading dir \""+path+"\"");
        fileBrowsing = new FileBrowsing(MainActivity.this, dropboxAPI, path, handler);
        fileBrowsing.execute();
        adapter.notifyDataSetChanged();
    }


    // Displaying the parent directory on Back button pressed
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (item != null && !item.getFilePath().equals("")) {
            String parentPath = item.getFilePath().substring(0, item.getFilePath().lastIndexOf("/"));
            if (parentPath.length() != 0)
                item = new Item(parentPath, parentPath.substring(0, parentPath.lastIndexOf("/")));
            else
                item = new Item(parentPath, "/");
            showFiles(parentPath);
        }
    }


    // Set onClick function for Login_btn
    @SuppressWarnings("deprecation")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                if (Constants.isUserLoggedIn) {
                    dropboxAPI.getSession().unlink();
                    loggedIn(false);
                } else {
                    (dropboxAPI.getSession()).startAuthentication(MainActivity.this);
                }
                break;
            default:
                break;
        }
    }

    // Enable and disable option menu base on isUserLoggedin
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logout = menu.findItem(R.id.logout);
        MenuItem upload = menu.findItem(R.id.upload);
        MenuItem changeview = menu.findItem(R.id.change_view);

        if (Constants.isUserLoggedIn) {
            logout.setEnabled(Constants.isUserLoggedIn);
            changeview.setEnabled(Constants.isUserLoggedIn);
            upload.setEnabled(Constants.isUserLoggedIn);
        } else {
            logout.setEnabled(Constants.isUserLoggedIn);
            changeview.setEnabled(Constants.isUserLoggedIn);
            upload.setEnabled(Constants.isUserLoggedIn);
        }
        this.menu = menu;
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = menuItem.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.change_view) {
            changeView();        // Change from ListView (list) to GridView (grid) or the other way around
            return true;
        } else if (id == R.id.logout) {         // Log out
            listview_visible = !listview_visible;
            changeView();
            listView.setVisibility(View.GONE);
            gridView.setVisibility(View.GONE);
            dropboxAPI.getSession().unlink();
            clearPrefs();
            loggedIn(false);
        } else if (id == R.id.upload) {
            // Run File Chooser class
            FileChooser fc = new FileChooser(MainActivity.this);
            fc.setExtension(".*");
            fc.setFileListener(new FileChooser.FileSelectedListener() {
                @Override
                public void fileSelected(File file) {
                    UploadFileToDropbox upload = new UploadFileToDropbox(dropboxAPI, MainActivity.this, file, item);
                    upload.execute();
                    if (item == null) {
                        showFiles("/");
                    } else if (item.getIsFolder() == 1) {
                        showFiles(item.getFilePath());
                    } else {
                        showFiles(item.getParentPath().substring(0, item.getParentPath().lastIndexOf("/")));
                    }
                }
            }).showDialog();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void loggedIn(boolean b) {
        Constants.isUserLoggedIn = b;
        Login_btn.setVisibility(b ? View.GONE : View.VISIBLE);
        txt1.setText(b ? R.string.welcome : R.string.greeting);
    }

    // Clear shared preference when log out
    private void clearPrefs() {
        SharedPreferences prefs = getSharedPreferences(
                Constants.DROPBOX_SHAREDPREF, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    // Change from grid view to list view and vice versa
    private void changeView() {
        if (Constants.isUserLoggedIn) {
            MenuItem changeview = menu.findItem(R.id.change_view);
            if (changeview.getTitle().toString().contains("Grid")) {
                changeview.setTitle(R.string.list_view);
                listView.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
                listview_visible = false;
            } else {
                changeview.setTitle(R.string.grid_view);
                gridView.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                listview_visible = true;
            }
            setAdapters();
        }
    }

    //  Set adapter for the corresponding view
    private void setAdapters() {
        if (listview_visible) {
            lva = new ListViewAdapter(this, R.layout.custom_component_listview, mArray);
            listView.setAdapter(lva);
            adapter = lva;
        } else {
            gva = new GridViewAdapter(this, R.layout.custom_component_gridview, mArray);
            gridView.setAdapter(gva);
            adapter = gva;
        }
    }

    private void showToast(String message){
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Create and define a handler to handle adding, updating directory path, used for folder chooser
    private final Handler listFoldersPathHandler = new Handler() {
        public void handleMessage(Message message) {
            listOfFoldersDropbox.clear();
            // ============More Customize===========
//            listOfFoldersDropbox.add(0, "..");
            // ============End Customize============
            ArrayList<String> result = message.getData().getStringArrayList("data");
            for (String path : result) {
                listOfFoldersDropbox.add(path);
            }
        }
    };

    // Create and define a handler to handle adding items to ArrayList<Item>.
    private final Handler handler = new Handler() {
        public void handleMessage(Message message) {
            mArray.clear();
            ArrayList<Item> result = message.getData().getParcelableArrayList("data");
            try {
                for (Item item : result) {
                    mArray.add(item);
                }
                adapter.notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
