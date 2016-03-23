package vs9.pvt_a1_dropbox;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by VS9 X64Bit on 24/11/2015.
 */
public class Item implements Parcelable {
    private int isFolder;
    private String fileName, fileSize, filePath, parentPath, fileType;
    Drawable mDrawable;

    public Item(String fileName, String fileSize, String filePath, String parentPath, String fileType, int isFolder) {
        super();
        this.fileType = fileType;
        this.isFolder = isFolder;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.parentPath = parentPath;
    }

    public Drawable getmDrawable() {
        return mDrawable;
    }

    public void setmDrawable(Drawable mDrawable) {
        this.mDrawable = mDrawable;
    }

    public Item(String filePath, String parentPath) {
        this.filePath = filePath;
        this.parentPath = parentPath;
    }

    public Item(String parentPath) {
        filePath = parentPath;
    }

    public Item( String fileName, String fileSize, String filePath, String parentPath,  String fileType, int isFolder, Drawable mDrawable) {
        this.isFolder = isFolder;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.fileType = fileType;
        this.parentPath = parentPath;
        this.mDrawable = mDrawable;
    }


    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {fileType = fileType;}

    public int getIsFolder() {
        return isFolder;
    }

    public void setIsFolder(int isFolder) {
        this.isFolder = isFolder;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    protected Item(Parcel in) {
        this.fileName = in.readString();
        this.fileSize = in.readString();
        this.filePath = in.readString();
        this.parentPath = in.readString();
        fileType = in.readString();
        this.isFolder = in.readInt();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileName);
        dest.writeString(fileSize);
        dest.writeString(filePath);
        dest.writeString(parentPath);
        dest.writeString(fileType);
        dest.writeInt(isFolder);
    }
}
