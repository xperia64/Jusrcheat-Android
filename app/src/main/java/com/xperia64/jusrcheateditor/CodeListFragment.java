package com.xperia64.jusrcheateditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.xperia64.jusrcheat.R4Cheat;
import com.xperia64.jusrcheat.R4Code;
import com.xperia64.jusrcheat.R4Folder;
import com.xperia64.jusrcheat.R4Game;
import com.xperia64.jusrcheat.R4Item;

import java.io.IOException;
import java.util.ArrayList;

public class CodeListFragment extends Fragment implements ExpandableListView.OnChildClickListener, AdapterView.OnItemLongClickListener {

    ExpandableListView listss;
    R4Game myGame;

    Activity activity;

    boolean create = false;
    boolean loaded = false;
    boolean hasChanges = false;
    boolean metaStage = false;
    CodeListAdapter ada;


    String stagedName;
    String stagedId1;
    int stagedId2;
    boolean stagedEnable;
    String stagedMaster;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_codes, container, false);
        listss = (ExpandableListView)v.findViewById(R.id.codeList);
        stagedName = null;
        return v;

    }
    @Override
    public void onViewCreated(View v, Bundle b)
    {
        super.onViewCreated(v,b);
        create = true;
        if(!loaded && myGame!=null)
        {
            loadList();
        }

    }
    @Override
    public void onAttach(Context c)
    {
        super.onAttach(c);
        if(c instanceof Activity)
        {
            activity = (Activity) c;
        }
    }

    ArrayList<R4Folder> folders;
    ArrayList<ArrayList<R4Code>> codes;

    ArrayList<String> txtFold;
    ArrayList<ArrayList<String>> txtCodes;

    //ArrayList<Integer> miscIdx;
    public void setGame(R4Game game)
    {
        if(registered&&myReceiver!=null)
        {
            registered = false;
            activity.unregisterReceiver(myReceiver);
        }
        myGame = game;
        if(create&&!loaded)
        {
            loadList();
        }
    }
    public void loadList()
    {
        loaded = true;
        folders = new ArrayList<>();
        codes = new ArrayList<>();
        txtCodes = new ArrayList<>();
        txtFold = new ArrayList<>();
        //miscIdx = new ArrayList<>();
        // Add a "Folder" for misc codes
        folders.add(new R4Folder("(Misc Codes)","",true));
        txtFold.add("(Misc Codes)");
        codes.add(new ArrayList<R4Code>());
        txtCodes.add(new ArrayList<String>());

        //int i = 0;
        for(R4Item item : myGame.getItems())
        {
            if(item instanceof R4Code)
            {
                ( codes.get(0)).add((R4Code)item);
                ( txtCodes.get(0)).add(item.getName());
                //miscIdx.add(i);
            }else{
                folders.add((R4Folder)item);
                txtFold.add(item.getName());
                ArrayList<String> tmps = new ArrayList<>();
                ArrayList<R4Code> tmpc = new ArrayList<>();
                for(R4Code code : ((R4Folder)item).getCodes())
                {
                    tmpc.add(code);
                    tmps.add(code.getName());
                }
                codes.add(tmpc);
                txtCodes.add(tmps);
            }
            //i++;
        }

        ada = new CodeListAdapter(txtFold, txtCodes, activity, activity.getLayoutInflater());
        listss.setAdapter(ada);
        listss.setFastScrollEnabled(true);
        listss.setOnChildClickListener(this);
        listss.setOnItemLongClickListener(this);
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        //TODO show popup with cheat info?

        return true;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long id) {
        int itemType = ExpandableListView.getPackedPositionType(id);
        final int groupPosition = ExpandableListView.getPackedPositionGroup(id);
        final int childPosition = ExpandableListView.getPackedPositionChild(id);
        if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP)
        {
            // Misc folder can't even
            if(groupPosition == 0)
            {
                Toast.makeText(activity, "Misc folder cannot be edited", Toast.LENGTH_SHORT).show();
                return true;
            }

            AlertDialog.Builder alrt = new AlertDialog.Builder(activity);
            alrt.setTitle("Edit Folder");
            View v = activity.getLayoutInflater().inflate(R.layout.fld_dlg, null);
            final EditText fname = (EditText) v.findViewById(R.id.edFold);
            final EditText fdesc = (EditText) v.findViewById(R.id.edFoldDesc);
            final CheckBox fhot = (CheckBox) v.findViewById(R.id.oneHotBox);
            R4Folder tmpFold = folders.get(groupPosition);
            fname.setText(tmpFold.getName());
            fdesc.setText(tmpFold.getDesc());
            fhot.setChecked(tmpFold.getOneHot());

            alrt.setView(v);
            alrt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
            alrt.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    AlertDialog.Builder confDel = new AlertDialog.Builder(activity);
                    confDel.setMessage("Delete the folder and all cheats in it?");
                    confDel.setTitle("Confirm Deletion");
                    confDel.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
                    confDel.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hasChanges = true;
                            folders.remove(groupPosition);
                            codes.remove(groupPosition);
                            txtFold.remove(groupPosition);
                            txtCodes.remove(groupPosition);
                            /*for(int o = 0; o<miscIdx.size(); o++)
                            {
                                if(miscIdx.get(o)>groupPosition)
                                {
                                    miscIdx.set(o,miscIdx.get(o)-1);
                                }
                            }*/
                            invalidateThings();
                        }
                    });
                    confDel.show();
                }
            });
            alrt.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hasChanges = true;
                    folders.get(groupPosition).setName(fname.getText().toString());
                    txtFold.set(groupPosition, fname.getText().toString());
                    folders.get(groupPosition).setDesc(fdesc.getText().toString());
                    folders.get(groupPosition).setOneHot(fhot.isChecked());
                    invalidateThings();
                }
            });
            alrt.show();
            invalidateThings();
            return true;
        }
        if(itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            AlertDialog.Builder alrt = new AlertDialog.Builder(activity);
            alrt.setTitle("Edit Code");
            View v = activity.getLayoutInflater().inflate(R.layout.cod_dlg, null);
            alrt.setView(v);
            final EditText cname = (EditText) v.findViewById(R.id.edCode);
            final EditText cdesc = (EditText) v.findViewById(R.id.edCodeDesc);
            final EditText ccode = (EditText) v.findViewById(R.id.edCodeCode);
            ccode.setFilters(new InputFilter[]{Globals.hexInputFilter});
            final CheckBox cen = (CheckBox) v.findViewById(R.id.enableCodeBox);
            R4Code code = codes.get(groupPosition).get(childPosition);
            cname.setText(code.getName());
            cdesc.setText(code.getDesc());
            ccode.setText(code.getCodeStr());
            cen.setChecked(code.getEnabled());
            alrt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
            alrt.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    AlertDialog.Builder confDel = new AlertDialog.Builder(activity);
                    confDel.setMessage("Delete the code?");
                    confDel.setTitle("Confirm Deletion");
                    confDel.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
                    confDel.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hasChanges = true;
                            codes.get(groupPosition).remove(childPosition);
                            txtCodes.get(groupPosition).remove(childPosition);
                            /*if(groupPosition == 0)
                            {
                                for(int o = 0; o<miscIdx.size(); o++)
                                {
                                    if(miscIdx.get(o)>childPosition) {
                                        miscIdx.set(o, miscIdx.get(o) - 1);
                                    }
                                }
                                miscIdx.remove(childPosition);
                            }*/
                            invalidateThings();
                        }
                    });
                    confDel.show();
                }
            });
            alrt.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            final AlertDialog validateMe = alrt.create();
            validateMe.show();
            validateMe.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tmpstagedName = cname.getText().toString();
                    String tmpstagedDesc = cdesc.getText().toString();
                    String tmpstagedCode =  ccode.getText().toString();
                    boolean tmpstagedCen = cen.isChecked();
                    int e = R4Cheat.validateCode(tmpstagedName, tmpstagedCode);
                    if(e == 0)
                    {
                        hasChanges = true;
                        codes.get(groupPosition).get(childPosition).setName(tmpstagedName);
                        codes.get(groupPosition).get(childPosition).setDesc(tmpstagedDesc);
                        codes.get(groupPosition).get(childPosition).deleteCode();
                        codes.get(groupPosition).get(childPosition).setEnabled(tmpstagedCen);
                        if(!tmpstagedCode.isEmpty()) {
                            codes.get(groupPosition).get(childPosition).addAll(ccode.getText().toString());
                        }
                        txtCodes.get(groupPosition).set(childPosition, tmpstagedName);
                        invalidateThings();
                        validateMe.dismiss();
                    }else{
                        String error = "";
                        switch(e)
                        {
                            case 1:
                                error = "Missing title";
                                break;
                            case 2:
                                error = "Bad code";
                                break;
                        }
                        Toast.makeText(activity, String.format("Error: %s",error), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return true;
        }
        return false;
    }

    public void invalidateThings()
    {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ada.notifyDataSetChanged();
            }
        });
    }

    public void editGame()
    {
        AlertDialog.Builder alrt = new AlertDialog.Builder(activity);
        alrt.setTitle("Edit Game");
        View v = activity.getLayoutInflater().inflate(R.layout.gam_dlg, null);
        alrt.setView(v);
        final EditText gname = (EditText) v.findViewById(R.id.edGam);
        final EditText gid1 = (EditText) v.findViewById(R.id.edGamId1);
        final EditText gid2 = (EditText) v.findViewById(R.id.edGamId2);
        final Button gred = (Button) v.findViewById(R.id.ldGam);
        final EditText gmas = (EditText) v.findViewById(R.id.edGamMas);
        final CheckBox gen = (CheckBox) v.findViewById(R.id.enableGamBox);
        gid2.setFilters(new InputFilter[]{Globals.shortHexInputFilter});
        gmas.setFilters(new InputFilter[]{Globals.hexInputFilter});

        if(stagedName == null) {
            gname.setText(myGame.getTitle());
            gid1.setText(myGame.getGameId());
            gid2.setText(String.format("%08X", myGame.getGameIdNum()));
            gmas.setText(myGame.getMasterCodeStr());
            gen.setChecked(myGame.getEnable());
        }else{
            gname.setText(stagedName);
            gid1.setText(stagedId1);
            gid2.setText(String.format("%08X", stagedId2));
            gmas.setText(stagedMaster);
            gen.setChecked(stagedEnable);
        }

        gred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileBrowserDialog fbd = new FileBrowserDialog();
                fbd.create("*.nds*", new FileBrowserDialog.FileBrowserDialogListener() {
                    @Override
                    public void setFile(String s) {
                        try{
                            String[] ids = R4Cheat.getIds(s);
                            gid1.setText(ids[0]);
                            gid2.setText(ids[1]);
                            gid1.postInvalidate();
                            gid2.postInvalidate();
                        }catch(IOException ignored) {}
                    }
                }, activity, activity.getLayoutInflater(), null, "Select NDS ROM", false);
            }
        });
        alrt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
        alrt.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        final AlertDialog validateMe = alrt.create();
        validateMe.show();
        validateMe.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmpstagedName = gname.getText().toString();
                String tmpstagedId1 = gid1.getText().toString();
                int tmpstagedId2 = gid2.getText().toString().isEmpty()?0:(int)Long.parseLong(gid2.getText().toString(),16);
                boolean tmpstagedEnable = gen.isChecked();
                String tmpstagedMaster = gmas.getText().toString().isEmpty()?R4Game.DEFAULT_MASTER:gmas.getText().toString();
                int e = R4Cheat.validateGame(tmpstagedName, tmpstagedId1, tmpstagedMaster);
                if(e == 0)
                {
                    hasChanges = true;
                    metaStage = true;
                    stagedName = tmpstagedName;
                    stagedEnable = tmpstagedEnable;
                    stagedId1 = tmpstagedId1;
                    stagedId2 = tmpstagedId2;
                    stagedEnable = tmpstagedEnable;
                    stagedMaster = tmpstagedMaster;
                    validateMe.dismiss();
                }else{
                    String error = "";
                    switch(e)
                    {
                        case 1:
                            error = "Missing title";
                            break;
                        case 2:
                            error = "Missing game id";
                            break;
                        case 3:
                            error = "Missing master code";
                            break;
                        case 4:
                            error = "Master code wrong size";
                            break;
                        case 5:
                            error = "Bad master code";
                            break;
                    }
                    Toast.makeText(activity, String.format("Error: %s",error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    boolean registered = false;
    BroadcastReceiver myReceiver;
    public void addItem()
    {
        AlertDialog.Builder alrt = new AlertDialog.Builder(activity);
        alrt.setTitle("Add Item");
        View v = activity.getLayoutInflater().inflate(R.layout.new_intro, null);
        alrt.setView(v);
        final Spinner itemType = (Spinner) v.findViewById(R.id.spnType);
        final Spinner itemPar = (Spinner) v.findViewById(R.id.spnFold);
        //final TextView itemParT = (TextView) v.findViewById(R.id.txtnFolder);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, txtFold);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemPar.setAdapter(spinnerArrayAdapter);
        itemType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0 || i == 2)
                {
                    itemPar.setEnabled(true);
                    //itemParT.setVisibility(View.VISIBLE);
                    //itemPar.setVisibility(View.VISIBLE);
                }else if(i == 1){
                    itemPar.setEnabled(false);
                    //itemParT.setVisibility(View.GONE);
                    //itemPar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        alrt.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(itemType.getSelectedItemPosition() == 0)
                {
                    // New Code
                    AlertDialog.Builder newCode = new AlertDialog.Builder(activity);
                    newCode.setTitle("Add Code");
                    View v = activity.getLayoutInflater().inflate(R.layout.cod_dlg, null);
                    final EditText cname = (EditText) v.findViewById(R.id.edCode);
                    final EditText cdesc = (EditText) v.findViewById(R.id.edCodeDesc);
                    final EditText ccode = (EditText) v.findViewById(R.id.edCodeCode);
                    ccode.setFilters(new InputFilter[]{Globals.hexInputFilter});
                    final CheckBox cen = (CheckBox) v.findViewById(R.id.enableCodeBox);
                    newCode.setView(v);
                    newCode.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
                    newCode.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    final AlertDialog validateMe = newCode.create();
                    validateMe.show();
                    validateMe.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String tmpstagedName = cname.getText().toString();
                            String tmpstagedDesc = cdesc.getText().toString();
                            String tmpstagedCode =  ccode.getText().toString();
                            boolean tmpstagedCen = cen.isChecked();
                            int e = R4Cheat.validateCode(tmpstagedName, tmpstagedCode);
                            if(e == 0)
                            {
                                hasChanges = true;
                                R4Code tmpCode = new R4Code(tmpstagedName, tmpstagedDesc);
                                if(!tmpstagedCode.isEmpty())
                                {
                                    tmpCode.addAll(tmpstagedCode);
                                }
                                tmpCode.setEnabled(tmpstagedCen);
                                codes.get(itemPar.getSelectedItemPosition()).add(tmpCode);
                                txtCodes.get(itemPar.getSelectedItemPosition()).add(cname.getText().toString());
                            /*if(itemPar.getSelectedItemPosition()==0)
                            {
                                if(miscIdx.get(miscIdx.size()-1)>=folders.size())
                                {
                                    miscIdx.add(miscIdx.get(miscIdx.size()-1)+1);
                                }else{
                                    miscIdx.add(folders.size());
                                }
                            }*/
                                invalidateThings();
                                validateMe.dismiss();
                            }else{
                                String error = "";
                                switch(e)
                                {
                                    case 1:
                                        error = "Missing title";
                                        break;
                                    case 2:
                                        error = "Bad code";
                                        break;
                                }
                                Toast.makeText(activity, String.format("Error: %s",error), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else if(itemType.getSelectedItemPosition() == 1){
                    // New Folder
                    AlertDialog.Builder newFold = new AlertDialog.Builder(activity);
                    newFold.setTitle("Add Folder");
                    View v = activity.getLayoutInflater().inflate(R.layout.fld_dlg, null);
                    newFold.setView(v);
                    final EditText fname = (EditText) v.findViewById(R.id.edFold);
                    final EditText fdesc = (EditText) v.findViewById(R.id.edFoldDesc);
                    final CheckBox fhot = (CheckBox) v.findViewById(R.id.oneHotBox);
                    newFold.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
                    newFold.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hasChanges = true;
                            R4Folder tmpFold = new R4Folder(fname.getText().toString().isEmpty()?"New Folder":fname.getText().toString(), fdesc.getText().toString());
                            tmpFold.setOneHot(fhot.isChecked());
                            folders.add(tmpFold);
                            codes.add(new ArrayList<R4Code>());
                            txtFold.add(fname.getText().toString());
                            txtCodes.add(new ArrayList<String>());
                            invalidateThings();
                        }
                    });
                    newFold.show();
                }else if(itemType.getSelectedItemPosition() == 2)
                {
                    AlertDialog.Builder newWcod = new AlertDialog.Builder(activity);
                    newWcod.setTitle("Add WiFi Code");
                    View v = activity.getLayoutInflater().inflate(R.layout.wcod_dlg, null);
                    final EditText wcodname = (EditText) v.findViewById(R.id.edWcod);
                    newWcod.setPositiveButton("Load", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    newWcod.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    newWcod.setView(v);
                    final AlertDialog validateme = newWcod.create();
                    validateme.show();
                    validateme.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                FileBrowserDialog fbd = new FileBrowserDialog();
                                fbd.create("*.nds*", new FileBrowserDialog.FileBrowserDialogListener() {
                                    @Override
                                    public void setFile(String s) {
                                        IntentFilter filter = new IntentFilter("com.xperia64.jusrcheateditor.WFC_RECV");
                                        if(!registered) {
                                            registered = true;
                                            myReceiver = new BroadcastReceiver() {
                                                @Override
                                                public void onReceive(Context context, Intent intent) {
                                                    String cod = intent.getStringExtra("wfc_code");
                                                    activity.unregisterReceiver(myReceiver);
                                                    registered = false;
                                                    if (wcodname.getText().toString().isEmpty()) {
                                                        wcodname.setText(cod.substring(2, cod.indexOf('\n')));
                                                    }
                                                    String realCode = cod.substring(cod.indexOf('\n') + 1);
                                                    int e = R4Cheat.validateCode(wcodname.getText().toString(), realCode);
                                                    if (e == 0) {
                                                        hasChanges = true;
                                                        R4Code tmpCode = new R4Code(wcodname.getText().toString(), "");
                                                        if (!realCode.isEmpty()) {
                                                            tmpCode.addAll(realCode);
                                                        }
                                                        tmpCode.setEnabled(false);
                                                        codes.get(itemPar.getSelectedItemPosition()).add(tmpCode);
                                                        txtCodes.get(itemPar.getSelectedItemPosition()).add(wcodname.getText().toString());
                                                        invalidateThings();
                                                        validateme.dismiss();
                                                    } else {
                                                        String error = "";
                                                        switch (e) {
                                                            case 1:
                                                                error = "Missing title";
                                                                break;
                                                            case 2:
                                                                error = "Bad code";
                                                                break;
                                                        }
                                                        Toast.makeText(activity, String.format("Error: %s", error), Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            };
                                            activity.registerReceiver(myReceiver, filter);
                                        }
                                        Intent i = new Intent();
                                        i.setAction("com.xperia64.wfcreplay.GET_CODE");
                                        i.putExtra("filename",s);
                                        activity.sendBroadcast(i);
                                    }
                                }, activity, activity.getLayoutInflater(), null, "Select NDS ROM", false);
                        }
                    });
                }
            }
        });
        alrt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
        alrt.show();
    }
    public void save()
    {
        if(metaStage)
        {
            myGame.setTitle(stagedName);
            myGame.setGameId(stagedId1);
            myGame.setGameIdNum(stagedId2);
            myGame.setEnable(stagedEnable);
            myGame.setMasterCode(stagedMaster);
        }

        if(registered&&myReceiver!=null)
        {
            registered = false;
            activity.unregisterReceiver(myReceiver);
        }
        stagedName = null;
        myGame.delAll();
        for(int i = 1; i< folders.size(); i++)
        {
            R4Folder fold = folders.get(i);
            fold.delAll();
            for(int o = 0; o<codes.get(i).size(); o++)
            {
                fold.addCode(codes.get(i).get(o));
            }
            myGame.addItem(folders.get(i));
        }
        ArrayList<R4Code> miscCodes = codes.get(0);
        for(int i = 0; i<codes.get(0).size(); i++)
        {
            myGame.addItem(miscCodes.get(i));
        }
    }
}
