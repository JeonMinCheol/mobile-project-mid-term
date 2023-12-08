package com.example.mobilewebproject2;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<DataModel> dataList;

    public CustomAdapter(Context context, ArrayList<DataModel> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        DataModel dataModel = (DataModel) getItem(position);

        String ymd = dataModel.getPublised_date().substring(0,10);
        String tm = dataModel.getPublised_date().substring(11,16);
        viewHolder.titleTextView.setText(dataModel.getTitle() + " " + dataModel.getId());
        viewHolder.textTextView.setText("발견 위치" + "\n" + dataModel.getText());
        viewHolder.timeTextView.setText("발견 일자" + "\n" + ymd + "  " + tm);
        Picasso.get().load(dataModel.getImageUrl()).into(viewHolder.imageView);

        return view;
    }

    private static class ViewHolder {
        TextView titleTextView;
        TextView textTextView;
        TextView timeTextView;
        ImageView imageView;



        ViewHolder(View view) {
            titleTextView = view.findViewById(R.id.titleTextView);
            textTextView = view.findViewById(R.id.textTextView);
            timeTextView = view.findViewById(R.id.timeTextView);
            imageView = view.findViewById(R.id.imageView);
        }
    }
}

