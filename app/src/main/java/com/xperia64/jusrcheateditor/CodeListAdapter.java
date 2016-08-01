package com.xperia64.jusrcheateditor;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Bobby on 4/15/2016.
 */
public class CodeListAdapter extends BaseExpandableListAdapter {


    private Activity activity;
    private LayoutInflater lal;
    private ArrayList<String> folders, codes;
    private ArrayList<ArrayList<String>> fcodes;

    public CodeListAdapter(ArrayList<String> folders, ArrayList<ArrayList<String>> fcodes, Activity activity, LayoutInflater lal)
    {
        this.activity = activity;
        this.fcodes = fcodes;
        this.folders = folders;
        this.lal = lal;
    }

    @Override
    public int getGroupCount() {
        return folders.size();
    }

    @Override
    public int getChildrenCount(int group) {
        return (fcodes.get(group)).size();
    }

    @Override
    public Object getGroup(int i) {
        return folders.get(i);
    }

    @Override
    public Object getChild(int group, int child) {
        return (fcodes.get(group)).get(child);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }
    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        if(view==null)
        {
            view = lal.inflate(R.layout.row_code, null);
        }
        ((TextView)view.findViewById(R.id.tvCode)).setText(folders.get(i));
        //((CheckedTextView)view.findViewById(R.id.rowcode)).setChecked(b);
        return view;
    }

    @Override
    public View getChildView(int groupPos, int childPos, boolean isLast, View view, ViewGroup viewGroup) {
        codes = fcodes.get(groupPos);
        TextView codet;

        if(view == null)
        {
            view = lal.inflate(R.layout.row_game, null);
        }
        codet = (TextView) view.findViewById(R.id.tvName);
        codet.setText(codes.get(childPos));
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

}
