package com.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by android on 30-10-2016.
 */
public class ReviewAdapter extends ArrayAdapter<String> {
    private ArrayList<String> mReviewList;

    public ReviewAdapter(Context context, ArrayList<String> reviewList) {
        super(context,0, reviewList);
        mReviewList= reviewList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View reviewItemView= convertView;
        if(reviewItemView== null){
            reviewItemView= LayoutInflater.from(getContext()).inflate(R.layout.review_list_item, parent, false);
        }

        TextView reviewText= (TextView)reviewItemView.findViewById(R.id.review_text_view);
        reviewText.setText(mReviewList.get(position));

        return reviewItemView;
    }
}
