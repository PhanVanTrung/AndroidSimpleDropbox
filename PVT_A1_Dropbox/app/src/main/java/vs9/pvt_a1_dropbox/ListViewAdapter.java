package vs9.pvt_a1_dropbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by VS9 X64Bit on 24/11/2015.
 */
public class ListViewAdapter extends ArrayAdapter<Item> {

    private Context context;
    private int viewId;
    private ArrayList<Item> mArray;

    public ListViewAdapter(Context context, int custom_listview, ArrayList<Item> mArray) {
        super(context, custom_listview, mArray);
        this.context = context;
        viewId = custom_listview;
        this.mArray = mArray;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View curView = convertView;
        if (curView==null){
            LayoutInflater view = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            curView = view.inflate(R.layout.custom_component_listview,null);
        }
        Item item = getItem(position);

        ImageView imageIcon = (ImageView) curView.findViewById(R.id.imageIcon_listview);
        TextView fileName = (TextView) curView.findViewById(R.id.fileName_listview);
        TextView fileSize = (TextView) curView.findViewById(R.id.fileSize_listview);

//        imageIcon.setImageResource(android.R.drawable.ic_menu_camera);
        if(item.getIsFolder()==2)
            imageIcon.setImageDrawable(item.getmDrawable());
        else if(item.getIsFolder()==1)
            imageIcon.setImageResource(R.drawable.folder);
        else if(item.getFileType().contains("text"))
            imageIcon.setImageResource(R.drawable.text);
        else
            imageIcon.setImageResource(R.drawable.file);
        fileName.setText(item.getFileName());
        fileSize.setText(item.getFileSize());
        return curView;
    }
}
