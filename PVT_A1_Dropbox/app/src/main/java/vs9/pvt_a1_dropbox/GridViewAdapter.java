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
public class GridViewAdapter extends ArrayAdapter<Item>{
    private Context context;
    private int viewId;
    private ArrayList<Item> mArray;

    public GridViewAdapter(Context context, int custom_gridview, ArrayList<Item> mArray) {
        super(context, custom_gridview, mArray);
        this.context = context;
        viewId = custom_gridview;
        this.mArray = mArray;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View curView = convertView;
        if (curView==null){
            LayoutInflater view = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            curView = view.inflate(R.layout.custom_component_gridview,null);
        }
        Item item = getItem(position);
        ImageView imageIcon = (ImageView) curView.findViewById(R.id.imageIcon_gridview);
        TextView fileName = (TextView) curView.findViewById(R.id.fileName_gridview);
        TextView fileSize = (TextView) curView.findViewById(R.id.fileSize_gridview);

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
