package com.xperia64.jusrcheateditor;

import android.content.Context;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

import com.xperia64.jusrcheat.R4Game;
import com.xperia64.jusrcheat.R4Header;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by xperia64 on 4/13/16.
 */
public class Globals {


    public static String repeatedSeparatorString = String.format("[%c]+",File.separatorChar);
    public static String parentString = ".."+File.separator;

    public static String getFileExtension(File f)
    {
        int dotPosition = f.getName().lastIndexOf(".");
        if (dotPosition != -1) {
            return (f.getName().substring(dotPosition)).toLowerCase(Locale.US);
        }
        return null;
    }
    public static String getFileExtension(String s)
    {
        int dotPosition = s.lastIndexOf(".");
        if (dotPosition != -1) {
            return (s.substring(dotPosition)).toLowerCase(Locale.US);
        }
        return null;
    }
    public static boolean hasSupportedExtension(File f)
    {
        String ext = getFileExtension(f);
        if(ext!=null && getSupportedExtensions().contains("*"+ext+"*"))
        {
            return true;
        }
        return false;
    }
    public static boolean hasSupportedExtension(String s)
    {
        String ext = getFileExtension(s);
        if(ext!=null && getSupportedExtensions().contains("*"+ext+"*"))
        {
            return true;
        }
        return false;
    }
    public static final String USRCHEAT_FILES = "*.dat*";
    public static String getSupportedExtensions()
    {
        StringBuilder supportedExtensions = new StringBuilder(USRCHEAT_FILES);
        return supportedExtensions.toString().replaceAll("[*]+", "*");
    }
    public static int getPadding(float dp, Context c)
    {
        float scale = c.getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (dp*scale + 0.5f);
        return dpAsPixels;
    }








    public static ArrayList<R4Game> games;
    public static R4Header header;
    public static String filename;

    public static boolean gamesAreLoaded;
    private static String hexFilter = "0123456789ABCDEFabcdef \n";
    public static boolean checkHex(char c)
    {
        return hexFilter.contains(""+c);
    }

    public static InputFilter hexInputFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            boolean keepOriginal = true;
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (Globals.checkHex(c)) // put your condition here
                    sb.append(c);
                else
                    keepOriginal = false;
            }
            if (keepOriginal)
                return null;
            else {
                if (source instanceof Spanned) {
                    SpannableString sp = new SpannableString(sb);
                    TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                    return sp;
                } else {
                    return sb;
                }
            }
        }

    };

    public static String shortHexFilter ="0123456789ABCDEFabcdef";
    public static boolean checkShortHex(char c)
    {
        return shortHexFilter.contains(""+c);
    }
    public static InputFilter shortHexInputFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            boolean keepOriginal = true;
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (Globals.checkShortHex(c)) // put your condition here
                    sb.append(c);
                else
                    keepOriginal = false;
            }
            if (keepOriginal)
                return null;
            else {
                if (source instanceof Spanned) {
                    SpannableString sp = new SpannableString(sb);
                    TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                    return sp;
                } else {
                    return sb;
                }
            }
        }

    };
}
