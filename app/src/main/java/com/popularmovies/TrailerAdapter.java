package com.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by android on 28-10-2016.
 */

public class TrailerAdapter extends ArrayAdapter<String> {
    ArrayList<String> mList;
    public TrailerAdapter(Context context, ArrayList<String> list) {
        super(context, 0, list);
        mList= list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View listItemView= convertView;
        if(listItemView== null){
            listItemView= LayoutInflater.from(getContext()).inflate(R.layout.trailer_item, parent, false);
        }
        TextView trailerText= (TextView)listItemView.findViewById(R.id.item_text);
        if(mList.size()==0){
            trailerText.setText("No Trailers Available");
        }
        else {
            trailerText.setText("Trailer " + (position + 1));
        }

        return listItemView;
    }

}
