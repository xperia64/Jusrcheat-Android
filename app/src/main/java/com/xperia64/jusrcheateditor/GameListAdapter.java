package com.xperia64.jusrcheateditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xperia64.jusrcheat.R4GameMeta;

import java.util.ArrayList;

/**
 * Created by xperia64 on 4/14/16.
 */
public class GameListAdapter extends BaseAdapter implements Filterable {

    private ArrayList<R4GameMeta> mOriginalValues; // Original Values
    private ArrayList<R4GameMeta> mDisplayedValues;    // Values to be displayed
    LayoutInflater inflater;

    Context c;
    public GameListAdapter(Context context, ArrayList<R4GameMeta> mR4GameMetaArrayList) {
        this.mOriginalValues = mR4GameMetaArrayList;
        this.mDisplayedValues = mR4GameMetaArrayList;
        c = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDisplayedValues.size();
    }

    @Override
    public Object getItem(int position) {
        return mDisplayedValues.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        LinearLayout llContainer;
        TextView tvTitle;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        ViewHolder holder = null;

        if (convertView == null) {

            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.row_game, null);
            holder.llContainer = (LinearLayout)convertView.findViewById(R.id.llContainer);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvTitle.setText(mDisplayedValues.get(position).title);

        /*holder.llContainer.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                Toast.makeText(c, mDisplayedValues.get(position).title, Toast.LENGTH_SHORT).show();
            }
        });*/

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mDisplayedValues = (ArrayList<R4GameMeta>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<R4GameMeta> FilteredArrList = new ArrayList<>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<>(mDisplayedValues); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        String data = mOriginalValues.get(i).title;
                        if (data.toLowerCase().contains(constraint.toString())) {
                            FilteredArrList.add(new R4GameMeta(mOriginalValues.get(i).title, mOriginalValues.get(i).realPosition));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }

}

