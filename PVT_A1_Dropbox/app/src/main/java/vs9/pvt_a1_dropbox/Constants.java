package vs9.pvt_a1_dropbox;

import com.dropbox.client2.session.Session;

/**
 * Created by VS9 X64Bit on 24/11/2015.
 */
public class Constants {
    public final static String DROPBOX_SHAREDPREF = "dropbox_prefs";
    public final static String ACCESS_KEY = "y8e7ztrqdhuvscs";
    public final static String ACCESS_SECRET = "hk13tl3f3kssqps";
    public final static Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

//    public final static int FOLDER_ICON_ID = 1, FILE_ICON_ID=2, SOUND_ICON_ID=3, PIC_ICON_ID=4, VID_ICON_ID=5;
    public static boolean isUserLoggedIn;
}
