package com.xperia64.jusrcheateditor;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.xperia64.jusrcheat.R4Game;

public class MainActivity extends AppCompatActivity {


    MenuItem mAdd;
    MenuItem mEdit;
    MenuItem mLoad;
    MenuItem mSave;
    MenuItem mClose;

    boolean fragMode = false;
    GameListFragment gameListFrag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(findViewById(R.id.fragment_container) != null)
        {
            if(savedInstanceState!=null)
            {
                gameListFrag = (GameListFragment) getSupportFragmentManager().findFragmentByTag("gameFrag");
                return;
            }

            gameListFrag = new GameListFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, gameListFrag, "gameFrag").commit();
            fragMode = false;
        }
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment f = getSupportFragmentManager().findFragmentById( R.id.fragment_container );
                if(f instanceof GameListFragment)
                {
                    fragMode = false;
                }else if(f instanceof CodeListFragment)
                {
                    fragMode = true;
                }else{
                    Log.e("Jusrcheat Editor","ERROR: Bad fragment");
                }
            }
        });
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            requestPermissions();
        }else{
            afterPermissions();
        }

    }
    final int PERMISSION_REQUEST = 178;
    final int NUM_PERMISSIONS = 2;
    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                new AlertDialog.Builder(this).setTitle("Permissions").setMessage("Jusrcheat Editor needs to be able to:\n" + "Read your storage to load cheat databases\n\n"+"Write to your storage to save changes made")

                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                actuallyRequestPermissions();

                            }

                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(MainActivity.this).setTitle("Error").setMessage("Jusrcheat Editor cannot proceed without these permissions").setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.this.finish();
                            }

                        }).setCancelable(false).show();

                    }

                }).setCancelable(false).show();

            } else {

                // No explanation needed, we can request the permission.
                actuallyRequestPermissions();
            }
        } else {
            afterPermissions();
        }
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void actuallyRequestPermissions() {
        ActivityCompat.requestPermissions(this, new String[] {  Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSION_REQUEST);
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                boolean good = true;
                if (permissions.length != NUM_PERMISSIONS || grantResults.length != NUM_PERMISSIONS) {
                    good = false;
                }

                for (int i = 0; i < grantResults.length && good; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        good = false;
                    }

                }
                if (!good) {

                    // permission denied, boo! Disable the app.
                    new AlertDialog.Builder(MainActivity.this).setTitle("Error").setMessage("Jusrcheat Editor cannot proceed without these permissions.").setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                        }

                    }).setCancelable(false).show();
                } else {
                    if (!Environment.getExternalStorageDirectory().canRead()) {
                        // Buggy emulator? Try restarting the app
                        AlarmManager alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                        alm.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, PendingIntent.getActivity(this, 237462, new Intent(this, MainActivity.class), 0));
                        System.exit(0);
                    }
                    afterPermissions();
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mAdd = menu.findItem(R.id.action_add);
        mEdit = menu.findItem(R.id.action_edit);
        mLoad = menu.findItem(R.id.action_open);
        mSave = menu.findItem(R.id.action_save);
        mClose = menu.findItem(R.id.action_close);
        mAdd.setVisible(false);
        mEdit.setVisible(false);
        mSave.setVisible(false);
        mClose.setVisible(false);
        return true;
    }

    public void setLoadedEnable(boolean enable)
    {
        if(mAdd == null || mEdit == null || mLoad == null)
            return;
        mAdd.setVisible(enable);
        mEdit.setVisible(enable);
        mSave.setVisible(enable);
        mClose.setVisible(enable);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id)
        {
            case R.id.action_add:
                if(fragMode) {
                    codeFrag.addItem();
                }else{
                    gameListFrag.addGame();
                }
                return true;
            case R.id.action_edit:
                if(fragMode) {
                    codeFrag.editGame();
                }else{
                    gameListFrag.editHeader();
                }
                return true;
            case R.id.action_open:
                checkAndReload();
                return true;
            case R.id.action_save:
                gameListFrag.save();
                return true;
            case R.id.action_close:
                checkAndClose();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        if(fragMode)
        {
            if(codeFrag.hasChanges)
            {
                saveGameDialog();
            }else{
                fragMode = false;
                codeFrag = null;
                mSave.setVisible(true);
                mClose.setVisible(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setHomeButtonEnabled(false);
                getSupportFragmentManager().popBackStack("gameFrag", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }else{
            checkAndFinish();
        }
        //
    }

    public void checkAndReload()
    {
        if(Globals.gamesAreLoaded)
        {
            if(gameListFrag.hasChanges)
            {
                AlertDialog.Builder warn = new AlertDialog.Builder(this);
                warn.setTitle("Warning");
                warn.setMessage("Close current file without saving?");
                warn.setNegativeButton("No", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
                warn.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        gameListFrag.clearEverything();
                        gameListFrag.openFileDialog();
                    }
                });
                warn.show();
            }else {
                gameListFrag.clearEverything();
            }
        }else{
            gameListFrag.openFileDialog();
        }
    }

    public void checkAndClose()
    {
        if(Globals.gamesAreLoaded)
        {
            if(gameListFrag.hasChanges)
            {
                AlertDialog.Builder warn = new AlertDialog.Builder(this);
                warn.setTitle("Warning");
                warn.setMessage("Close current file without saving?");
                warn.setNegativeButton("No", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
                warn.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        gameListFrag.clearEverything();
                    }
                });
                warn.show();
            }else {
                gameListFrag.clearEverything();
            }
        }
    }
    public void checkAndFinish()
    {
        if(Globals.gamesAreLoaded)
        {
            if(gameListFrag.hasChanges)
            {
                AlertDialog.Builder warn = new AlertDialog.Builder(this);
                warn.setTitle("Warning");
                warn.setMessage("Quit app without saving?");
                warn.setNegativeButton("No", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialogInterface, int i) {}});
                warn.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        gameListFrag.clearEverything();
                        MainActivity.this.finish();
                    }
                });
                warn.show();
            }else {
                gameListFrag.clearEverything();
                MainActivity.this.finish();
            }
        }
    }
    public void saveGameDialog()
    {
        AlertDialog.Builder sgc = new AlertDialog.Builder(this);
        sgc.setTitle("Save Changes?");
        sgc.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {@Override public void onClick(DialogInterface dialogInterface, int i) {}});
        sgc.setNeutralButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                fragMode = false;
                mSave.setVisible(true);
                mClose.setVisible(true);
                mLoad.setVisible(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setHomeButtonEnabled(false);
                getSupportFragmentManager().popBackStack("gameFrag", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
        sgc.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                codeFrag.save();
                gameListFrag.hasChanges = true;
                fragMode = false;
                mSave.setVisible(true);
                mClose.setVisible(true);
                mLoad.setVisible(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setHomeButtonEnabled(false);
                getSupportFragmentManager().popBackStack("gameFrag", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
        sgc.show();
    }


    CodeListFragment codeFrag;
    int currGamePos;
    public void openGame(R4Game game, int pos)
    {
        codeFrag = new CodeListFragment();
        codeFrag.setGame(game);
        currGamePos = pos;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, codeFrag, "codeFrag").addToBackStack("gameFrag").commit();
        fragMode = true;
        mSave.setVisible(false);
        mClose.setVisible(false);
        mLoad.setVisible(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportFragmentManager().popBackStack("gameFrag", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
    public void afterPermissions()
    {
        gameListFrag.openFileDialog();
    }
}
