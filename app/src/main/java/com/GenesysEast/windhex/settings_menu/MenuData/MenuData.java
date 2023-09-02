package com.GenesysEast.windhex.settings_menu.MenuData;

import android.content.Context;
import android.util.Log;

import com.GenesysEast.windhex.R;

import java.util.ArrayList;
import java.util.List;

public class MenuData
{
    final int LIST_ITEM     = 1;
    final int LIST_HEADER   = 2;
    final int LIST_CHECKBOX = 4;
    
    public static int SHOW_HIDDEN      = 1;
    public static int SHOW_PROTECTED   = 2;
    public static int SORT_DIRECTORIES = 4;
    public static int SORT_FILES       = 8;
    public static int FILES_FIRST      = 16;
    public static int SCREEN_STATUS    = 32;
    public static int SAVE_PATH        = 64;
    public static int NEW_FIRST        = 128;
    public static int ROOT_ACCESS      = 256;
    
    
    int Flags;
    private int currentTheme;
    
    private String[] themes;
    
    // Main Menu List
    public ArrayList<MenuList> mainMenuList( Context context, int Flags, int currentTheme )
    {   //
        this.Flags = Flags;
        this.currentTheme = currentTheme;
        ArrayList<MenuList> mList = new ArrayList<>();
        
        try
        {
            // String array with names of themes
            themes = context.getResources().getStringArray( R.array.theme_names );
            if ( this.currentTheme >= themes.length )
            {
                this.currentTheme = 0;
            }
            
            mList.clear();
            mList.add( new MenuList( "Themes", LIST_HEADER ) );
            // Will have a pop up radio group.
            // mList.size should be current index
            mList.add( new MenuList( "Current Theme", themes[ currentTheme ], LIST_ITEM, 0, mList.size() ) );
            //
            //-------------------------
            //
            mList.add( new MenuList( "File Manager Settings", LIST_HEADER ) );
            
            mList.add(
                    new MenuList( "Show hidden files", "Default behavior will NOT allow files with hidden file attribute settings to be listed in the file manager.", LIST_CHECKBOX,
                                  SHOW_HIDDEN, mList.size()
                    ) );  // arg0 = checkbox status
            
            mList.add( new MenuList( "Show inaccessible files", "Default behavior will NOT allow files with READ / WRITE restrictions to be listed in the file manager.",
                                     LIST_CHECKBOX, SHOW_PROTECTED, mList.size()
            ) );  // arg0 = checkbox status
            
            mList.add(
                    new MenuList( "Sort all folders", "By default, the file manager will NOT display folders in a sorted fashion. Click this switch to enable directory sorting.",
                                  LIST_CHECKBOX, SORT_DIRECTORIES, mList.size()
                    ) );  // arg0 = checkbox status
            mList.add( new MenuList( "Sort all files", "By default, the file manager will NOT display files in a sorted fashion. Click this switch to enable file sorting.",
                                     LIST_CHECKBOX, SORT_FILES, mList.size()
            ) );  // arg0 = checkbox status
            
            mList.add( new MenuList( "Show files first",
                                     "By default, the file manager will display directories before displaying files. Selecting this option will allow files to be listed before directories.",
                                     LIST_CHECKBOX, FILES_FIRST, mList.size()
            ) );  // arg0 = checkbox status
            
            mList.add( new MenuList( "Show newest folders / files first",
                                     "By default, the file manager will display folders and files in the order in which they are processed. NOTE: by simply opening a file, the date will be modified thus classifying the file as \"newest\".",
                                     LIST_CHECKBOX, NEW_FIRST, mList.size()
            ) );  // arg0 = checkbox status
            //
            //-------------------------
            //
            mList.add( new MenuList( "General Settings", LIST_HEADER ) );
/*
            mList.add(
                    new MenuList( "Root Access", "The application will attempt to gain root access to a rooted device.",
                                  LIST_CHECKBOX, ROOT_ACCESS, mList.size()
                    ) );  // arg0 = checkbox status
*/
            mList.add(
                    new MenuList( "Keep screen on", "The application will allow for the device's screen to be turned off. Enable this selection if the alternative is preferred.",
                                  LIST_CHECKBOX, SCREEN_STATUS, mList.size()
                    ) );  // arg0 = checkbox status
            mList.add( new MenuList( "Save last directory accessed", "The last directory accessed will be saved and used the next time a directory listing is requested.",
                                     LIST_CHECKBOX, SAVE_PATH, mList.size()
            ) );  // arg0 = checkbox status
            //
            //-------------------------
            //
            //        mList.add( new MenuList( "Other Settings",  LIST_HEADER ));
            return mList;
        }
        catch ( Exception ex )
        {
        
        }
        
        return null;
    }
}