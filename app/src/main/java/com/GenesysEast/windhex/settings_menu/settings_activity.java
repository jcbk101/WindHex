package com.GenesysEast.windhex.settings_menu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.GenesysEast.windhex.Dialogs.ErrorDialog;
import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.GlobalClasses.ThemeControl;
import com.GenesysEast.windhex.R;
import com.GenesysEast.windhex.settings_menu.Adapters.SettingsAdapter;
import com.GenesysEast.windhex.settings_menu.MenuData.MenuData;
import com.GenesysEast.windhex.settings_menu.MenuData.MenuList;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;


public class settings_activity
        extends AppCompatActivity
        implements AdapterView.OnItemClickListener
{
    private       Globals             global = Globals.getInstance();
    private       MenuData            mCalls = new MenuData();
    public static ArrayList<MenuList> mList;
    private       ListView            listView;
    public        SettingsAdapter     settingsAdapter;
    //
    
    /**
     * Constructor
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        
        // MUST BE SET BEFORE setContentView
        ThemeControl.onActivityCreateSetTheme( this, global.currentTheme );
        
        setContentView( R.layout.settings_main );
        
        // Create the menu list
        mList = mCalls.mainMenuList( this, global.settingsFlag, global.currentTheme );
        
        // Assign ListView and Adapter
        listView = findViewById( R.id.listView );
        settingsAdapter = new SettingsAdapter( mList, this, mCalls, this );
        listView.setAdapter( settingsAdapter );
        listView.setOnItemClickListener( this );
        
        Toolbar tb = findViewById( R.id.settingsBar );
        tb.setKeepScreenOn( (global.settingsFlag & MenuData.SCREEN_STATUS) != 0 );
        tb.setTitle( "Settings" );
    }
    
    
    /**
     * Click listener for all items
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id )
    {
        if ( position == 1 )
        {
            int themes = R.array.theme_names;
            
            ErrorDialog dialog = new ErrorDialog();
            
            dialog.setMessage( "Select a theme" ).setTitle( "Theme Selection" );
            dialog.setItems( getResources().getStringArray( themes ) );
            dialog.setButtons( ErrorDialog.NO_BUTTON );
            dialog.setOnErrorListener( new ErrorDialog.OnErrorListener()
            {
                @Override
                public void onErrorExitClick( ErrorDialog dialog, int buttonClicked )
                {
                    if ( buttonClicked != -1 )
                    {   // Adjust theme
                        global.currentTheme = buttonClicked;
                        dialog.dismiss();
                        // Change data
                        
                        mList.clear();
                        //            mList.addAll( mCalls.mainMenuList( mContext, global.settingsFlag, global.currentTheme ) );
                        mList = mCalls.mainMenuList( settings_activity.this, global.settingsFlag, global.currentTheme );
                        settingsAdapter.notifyDataSetChanged();
                        
                        // Change the theme
                        ThemeControl.changeToTheme( settings_activity.this, global.currentTheme );
                    }
                    else
                    {
                        dialog.dismiss();
                    }
                }
            } );
            //
            dialog.show( getSupportFragmentManager(), null );
            
            //            settingsAdapter.customDialog( this, "Theme Selection", "Select a theme", themes, global.currentTheme );
        }
    }
    
    @Override
    public void onBackPressed()
    {
        //super.onBackPressed();
        Intent retIntent = getIntent();
        setResult( Activity.RESULT_OK, retIntent );
        finish();
    }
    
    

}