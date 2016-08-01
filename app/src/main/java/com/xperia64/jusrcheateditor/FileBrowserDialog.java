package com.xperia64.jusrcheateditor;

import java.io.File;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

public class FileBrowserDialog implements OnItemClickListener {

    ListView fbdList;
    LinearLayout fbdLayout;
    String currPath;
    ArrayList<String> fname;
    ArrayList<String> path;
    String extensions;
    Activity context;
    String msg;
    FileBrowserDialogListener onSelectedCallback;
    AlertDialog ddd;

    public interface FileBrowserDialogListener {
        void setFile(String s);
    }

    @SuppressLint("InflateParams")
    public void create(String extensions, FileBrowserDialogListener onSelectedCallback, final Activity context, LayoutInflater layoutInflater, String path, String msg, boolean hasNew) {
        this.onSelectedCallback = onSelectedCallback;
        this.msg = msg;
        this.context = context;
        this.extensions = extensions;
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setCancelable(false);
        fbdLayout = (LinearLayout) layoutInflater.inflate(R.layout.list, null);
        fbdList = (ListView) fbdLayout.findViewById(android.R.id.list);
        fbdList.setOnItemClickListener(this);
        b.setView(fbdLayout);
        b.setCancelable(false);
        b.setTitle("Choose File");
        b.setNegativeButton(context.getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        if(hasNew) {
            b.setNeutralButton("New", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
        if (path == null)
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        else if (!new File(path).exists())
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        getDir(path);
        ddd = b.create();
        ddd.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    up();
                    return true;
                }
                return false;
            }
        });
        ddd.show();
        if(hasNew) {
            ddd.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //System.out.println("Error do not use 2");
                    AlertDialog.Builder newDatDlg = new AlertDialog.Builder(FileBrowserDialog.this.context);
                    newDatDlg.setTitle("Enter Filename");
                    final EditText fileTxt = new EditText(FileBrowserDialog.this.context);
                    fileTxt.setHint("usrcheat.dat");
                    InputFilter filter = new InputFilter() {
                        public CharSequence filter(CharSequence source, int start, int end,
                                                   Spanned dest, int dstart, int dend) {
                            for (int i = start; i < end; i++) {
                                String IC = "*/*\n*\r*\t*\0*\f*`*?***\\*<*>*|*\"*:*";
                                if (IC.contains("*" + source.charAt(i) + "*")) {
                                    return "";
                                }
                            }
                            return null;
                        }
                    };
                    fileTxt.setFilters(new InputFilter[]{filter});
                    newDatDlg.setView(fileTxt);
                    newDatDlg.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (fileTxt.getText().toString().isEmpty()) {
                                FileBrowserDialog.this.onSelectedCallback.setFile(currPath + File.separator + fileTxt.getHint());
                            } else {
                                FileBrowserDialog.this.onSelectedCallback.setFile(currPath + File.separator + fileTxt.getText().toString());
                            }
                            ddd.dismiss();
                        }
                    });
                    newDatDlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    newDatDlg.setCancelable(false);
                    newDatDlg.show();
                    ViewGroup parent = (ViewGroup) fileTxt.getParent();
                    int padding = Globals.getPadding(7, context);
                    parent.setPadding(padding, padding, padding, padding);
                }
            });
        }
    }

    public void getDir(String dirPath) {
        currPath = dirPath;
        fname = new ArrayList<>();
        path = new ArrayList<>();
        if (currPath != null) {
            File f = new File(currPath);
            if (f.exists()) {
                File[] files = f.listFiles();

                if (files!=null && files.length > 0) {
                    Arrays.sort(files, new FileComparator());
                    if (!currPath.matches(Globals.repeatedSeparatorString)) {
                        fname.add(Globals.parentString);
                        // Thank you Marshmallow.
                        // Disallowing access to /storage/emulated has now prevent billions of hacking attempts daily.
                        if (new File(f.getParent()).canRead()||new File(f.getParent()).getAbsolutePath().equals("/")) {
                            path.add(f.getParent() + File.separator);
                        } else {
                            path.add(File.separator);
                        }
                    }
                    for (File file : files)
                    {
                        if (file.isFile() ){
                            String extension = Globals.getFileExtension(file);
                            if (extension != null) {
                                if (extensions.contains("*"+extension+"*")) {
                                    path.add(file.getAbsolutePath());
                                    fname.add(file.getName());
                                }
                            } else if (file.getName().endsWith(File.separator)) {
                                path.add(file.getAbsolutePath() + File.separator);
                                fname.add(file.getName() + File.separator);
                            }
                        } else if (file.isDirectory()) {
                            path.add(file.getAbsolutePath() + File.separator);
                            fname.add(file.getName() + File.separator);
                        }
                    }
                } else {
                    // Root
                    if (!currPath.matches(Globals.repeatedSeparatorString)) {
                        fname.add("../");
                        path.add(f.getParent() + File.separator);

                    }
                }

                ArrayAdapter<String> fileList = new ArrayAdapter<>(context, R.layout.row, fname);
                fbdList.setFastScrollEnabled(true);
                fbdList.setAdapter(fileList);
            }
        }
    }

    public void up()
    {
        if(!new File(currPath).getAbsolutePath().equals("/"))
        {
            getDir(new File(currPath).getParent());
        }
    }
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

        File file = new File(path.get(arg2));
        if (file.isDirectory()) {
            if (file.canRead()) {
                getDir(path.get(arg2));
            } else if(file.getAbsolutePath().equals("/")){
                getDir("/storage/");
            } else if (file.getAbsolutePath().equals("/storage/emulated")&&
                    ((new File("/storage/emulated/0").exists()&&new File("/storage/emulated/0").canRead())||
                            (new File("/storage/emulated/legacy").exists()&&new File("/storage/emulated/legacy").canRead())||
                            (new File("/storage/self/primary").exists()&&new File("/storage/self/primary").canRead())))
            {
                if(new File("/storage/emulated/0").exists()&&new File("/storage/emulated/0").canRead())
                {
                    getDir("/storage/emulated/0");
                }else if((new File("/storage/emulated/legacy").exists()&&new File("/storage/emulated/legacy").canRead())){
                    getDir("/storage/emulated/legacy");
                }else{
                    getDir("/storage/self/primary");
                }
            } else {
                AlertDialog.Builder unreadableDialog = new AlertDialog.Builder(context);
                unreadableDialog = unreadableDialog.setIcon(R.mipmap.ic_launcher);
                unreadableDialog = unreadableDialog.setTitle(String.format("[%1$s] %2$s", file.getName(), context.getResources().getString(R.string.fb_cread)));
                unreadableDialog = unreadableDialog.setPositiveButton(context.getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                unreadableDialog.show();
            }
        } else {
            if (file.canRead()) {
                onSelectedCallback.setFile(file.getAbsolutePath());
                ddd.dismiss();
            }
        }
    }

}
