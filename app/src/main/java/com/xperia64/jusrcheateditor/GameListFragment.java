package com.xperia64.jusrcheateditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.xperia64.jusrcheat.R4Cheat;
import com.xperia64.jusrcheat.R4Game;
import com.xperia64.jusrcheat.R4GameMeta;
import com.xperia64.jusrcheat.R4Header;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class GameListFragment extends Fragment implements FileBrowserDialog.FileBrowserDialogListener {


    Activity activity;
    boolean open = false;

    boolean hasChanges = false;
    EditText searchTxt;
    String oldText = "";
    ListView gameList;
    GameListAdapter ada;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_game, container, false);
        searchTxt = (EditText)v.findViewById(R.id.searchText);
        gameList = (ListView)v.findViewById(R.id.gameList);

        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                if(ada!=null) {
                    if(oldText.equals(cs.toString()))
                    {
                        // Sometimes this will fire the previous filter and the new filter at the same time.
                        // This branch is to prevent that.
                        //System.out.println("Warning: Not filtering the same string");
                    }else {
                        ada.getFilter().filter(cs);
                        oldText = cs.toString();
                    }
                }
            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

            @Override
            public void afterTextChanged(Editable arg0) {}
        });
        searchTxt.setEnabled(false);

        if(Globals.gamesAreLoaded)
        {
            postLoad();
        }
        return v;
    }
    @Override
    public void onAttach(Context c)
    {
        super.onAttach(c);
        if(c instanceof  Activity)
        {
            activity = (Activity) c;
        }
        if(open)
        {
            open = false;
            openFileDialog();
        }

    }
    public void openFileDialog()
    {
        if(activity!=null) {
            FileBrowserDialog fdb = new FileBrowserDialog();
            fdb.create(Globals.getSupportedExtensions(), this, activity, activity.getLayoutInflater(), null, "Select usrcheat.dat", true);
        }else{
            open = true;
        }
    }
    public void save()
    {
        hasChanges = false;
        if(new File(Globals.filename).exists())
        {
            AlertDialog.Builder warn = new AlertDialog.Builder(activity);
            warn.setTitle("Warning");
            warn.setMessage("Overwrite file?");
            warn.setNegativeButton("No", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
            warn.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new SaveR4Task().execute(Globals.filename);
                }
            });
            warn.show();
        }else{
            new SaveR4Task().execute(Globals.filename);
        }
    }
    //
    private class SaveR4Task extends AsyncTask<String, Integer, Void> implements R4Cheat.R4ProgressCallback {

        ProgressDialog pd;
        ProgressDialog pd2;
        @Override
        protected Void doInBackground(String... params) {


            try {
                R4Cheat.writeUsrCheat(Globals.filename, Globals.header, Globals.games, this);
            } catch (IOException e) {
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pd.dismiss();
            postLoad();
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activity);
            pd2 = new ProgressDialog(activity);
            pd.setProgress(0);
            pd2.setMessage("Preparing to save...");
            pd2.setCancelable(false);
            pd2.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setMessage("Saving cheats...");
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setCancelable(false);
            pd.show();
            pd2.show();

        }

        boolean set = false;
        @Override
        protected void onProgressUpdate(Integer... values) {
            if(!set)
            {
                set = true;
                pd.setMax(values[1]);
                pd2.dismiss();
            }
            pd.setProgress(values[0]);
        }

        @Override
        public void setProgress(int num, int max) {
            publishProgress(num, max);
        }
    }

    private class LoadR4Task extends AsyncTask<String, Integer, Void> implements R4Cheat.R4ProgressCallback {

        ProgressDialog pd;
        @Override
        protected Void doInBackground(String... params) {


            try {
                Globals.header = new R4Header(Globals.filename);
                if(!Globals.header.isHeaderValid())
                {
                    Log.e("Jusrcheat","Error: Bad Header!");
                    return null;
                }
                Globals.games = R4Cheat.getGames(Globals.filename, Globals.header, this);

            } catch (IOException e) {
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pd.dismiss();
            postLoad();
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activity);
            pd.setProgress(0);
            pd.setMessage("Loading cheats...");
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setCancelable(false);
            pd.show();

        }

        boolean set = false;
        @Override
        protected void onProgressUpdate(Integer... values) {
            if(!set)
            {
                set = true;
                pd.setMax(values[1]);
            }
            pd.setProgress(values[0]);
        }

        @Override
        public void setProgress(int num, int max) {
            publishProgress(num, max);
        }
    }

    @Override
    public void setFile(String s) {
        Globals.filename = s;
        if(new File(Globals.filename).exists())
        {
            new LoadR4Task().execute(s);
        }else{
            // Create a new file
            AlertDialog.Builder alrt = new AlertDialog.Builder(activity);
            alrt.setTitle("New usrcheat.dat");
            View v = activity.getLayoutInflater().inflate(R.layout.dat_dlg, null);
            final EditText ned = (EditText) v.findViewById(R.id.edData);
            final Spinner nsp = (Spinner) v.findViewById(R.id.spEnc);
            final CheckBox ncb = (CheckBox) v.findViewById(R.id.enableDatBox);
            alrt.setView(v);
            alrt.setCancelable(false);
            alrt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
            alrt.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hasChanges = true;
                    Globals.header = new R4Header(ned.getText().toString().isEmpty()?ned.getHint().toString():ned.getText().toString(), nsp.getSelectedItemPosition(), ncb.isChecked());
                    Globals.games = new ArrayList<>();
                    postLoad();
                }
            });
            alrt.show();
        }
    }

    public void editHeader()
    {
        hasChanges = true;
        AlertDialog.Builder alrt = new AlertDialog.Builder(activity);
        alrt.setTitle("Edit usrcheat.dat Info");
        View v = activity.getLayoutInflater().inflate(R.layout.dat_dlg, null);
        final EditText ned = (EditText) v.findViewById(R.id.edData);
        final Spinner nsp = (Spinner) v.findViewById(R.id.spEnc);
        final CheckBox ncb = (CheckBox) v.findViewById(R.id.enableDatBox);


        ned.setText(Globals.header.getDatabaseName());
        nsp.setSelection(Globals.header.getEncoding());
        ncb.setChecked(Globals.header.getCheatEnable());

        alrt.setView(v);
        alrt.setCancelable(false);
        alrt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
        alrt.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Globals.header.setCheatEnable(ncb.isChecked());
                Globals.header.setEncoding(nsp.getSelectedItemPosition());
                Globals.header.setDatabaseName(ned.getText().toString());
            }
        });
        alrt.show();
    }

    public void addGame()
    {
        hasChanges = true;
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
                        }catch(IOException e) {}
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
                if(e==0)
                {
                    hasChanges = true;
                    R4Game newGam = new R4Game(tmpstagedId1, tmpstagedId2, tmpstagedName);
                    newGam.setMasterCode(tmpstagedMaster);
                    newGam.setEnable(tmpstagedEnable);
                    Globals.games.add(newGam);
                    metaGame.add(new R4GameMeta(tmpstagedName, tmpstagedId1, Globals.games.size()-1));
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
    ArrayList<R4GameMeta> metaGame;
    public void postLoad()
    {
        metaGame = new ArrayList<>();
        ((MainActivity)activity).setLoadedEnable(true);
        for(int i = 0; i<Globals.games.size(); i++)
        {
            metaGame.add(new R4GameMeta(Globals.games.get(i).getTitle(), Globals.games.get(i).getGameId(), i));
        }

        searchTxt.setEnabled(true);
        ada = new GameListAdapter(activity, metaGame);
        gameList.setAdapter(ada);
        gameList.setFastScrollEnabled(true);
        gameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                R4GameMeta meat = (R4GameMeta) ada.getItem(i);
                //Toast.makeText(activity, String.format("%s %d",meat.title, meat.realPosition), Toast.LENGTH_SHORT).show();
                ((MainActivity)activity).openGame(Globals.games.get(meat.realPosition), meat.realPosition);
            }
        });
        gameList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                final R4GameMeta meat = ((R4GameMeta)ada.getItem(i));
                final R4Game myGame = Globals.games.get(meat.realPosition);
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


                gname.setText(myGame.getTitle());
                gid1.setText(myGame.getGameId());
                gid2.setText(String.format("%08X",myGame.getGameIdNum()));
                gmas.setText(myGame.getMasterCodeStr());
                gen.setChecked(myGame.getEnable());
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
                alrt.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog.Builder confDel = new AlertDialog.Builder(activity);
                        confDel.setMessage("Delete the game and all cheats in it?");
                        confDel.setTitle("Confirm Deletion");
                        confDel.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
                        confDel.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                hasChanges = true;

                                for (int o = meat.realPosition + 1; o < metaGame.size(); o++) {
                                    metaGame.get(o).pushDown();
                                }
                                Globals.games.remove(meat.realPosition);
                                metaGame.remove(meat.realPosition);
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
                        String tmpstagedName = gname.getText().toString();
                        String tmpstagedId1 = gid1.getText().toString();
                        int tmpstagedId2 = gid2.getText().toString().isEmpty()?0:(int)Long.parseLong(gid2.getText().toString(),16);
                        boolean tmpstagedEnable = gen.isChecked();
                        String tmpstagedMaster = gmas.getText().toString().isEmpty()?R4Game.DEFAULT_MASTER:gmas.getText().toString();
                        int e = R4Cheat.validateGame(tmpstagedName, tmpstagedId1, tmpstagedMaster);
                        if(e == 0)
                        {
                            hasChanges = true;
                            myGame.setTitle(tmpstagedName);
                            myGame.setGameId(tmpstagedId1);
                            myGame.setGameIdNum(tmpstagedId2);
                            myGame.setEnable(tmpstagedEnable);
                            myGame.setMasterCode(tmpstagedMaster);
                            metaGame.set(meat.realPosition, new R4GameMeta(tmpstagedName,tmpstagedId1, meat.realPosition));
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
                return true;
            }
        });
        ada.getFilter().filter(searchTxt.getText());
        oldText = searchTxt.getText().toString();
        Globals.gamesAreLoaded = true;
    }
    public void invalidateThings()
    {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ada.notifyDataSetChanged();
                ada.getFilter().filter(oldText);
            }
        });
    }

    public void clearEverything()
    {
        hasChanges = false;
        Globals.gamesAreLoaded = false;
        if(metaGame!=null)
            metaGame.clear();
        if(Globals.games!=null)
            Globals.games.clear();
        Globals.header = null;
        Globals.filename = null;
        open = false;
        invalidateThings();
        searchTxt.setEnabled(false);
        ((MainActivity)activity).setLoadedEnable(false);
    }


}
