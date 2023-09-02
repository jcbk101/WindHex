package com.GenesysEast.windhex.settings_menu.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.GenesysEast.windhex.GlobalClasses.ExecuteAsRoot;
import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.GlobalClasses.ThemeControl;
import com.GenesysEast.windhex.R;
import com.GenesysEast.windhex.settings_menu.MenuData.MenuData;
import com.GenesysEast.windhex.settings_menu.MenuData.MenuList;

import java.util.List;


public class SettingsAdapter
        extends BaseAdapter
        implements View.OnClickListener
{
    
    Globals global = Globals.getInstance();
    
    private List<MenuList> mList;
    private MenuData       mCalls;
    private Context        mContext;
    private Activity       activity;
    
    final int LIST_ITEM     = 1;
    final int LIST_HEADER   = 2;
    final int LIST_CHECKBOX = 4;
    
    /**
     * Constructor
     *
     * @param mList
     * @param mContext
     */
    public SettingsAdapter( List<MenuList> mList, Context mContext, MenuData mCalls, Activity activity )
    {
        this.mList = mList;
        this.mContext = mContext;
        this.mCalls = mCalls;
        this.activity = activity;
    }
    
    /**
     * Used to return flag data from the menu
     * view to be drawn.
     *
     * @param position
     *
     * @return
     */
    @Override
    public int getItemViewType( int position )
    {
        MenuList menu = mList.get( position );
        
        return menu.getFlags();
    }
    
    @Override
    public int getCount()
    {
        return mList.size();
    }
    
    @Override
    public Object getItem( int position )
    {
        return mList.get( position );
    }
    
    @Override
    public long getItemId( int position )
    {
        return position;
    }
    
    /**
     * Create the requested view
     *
     * @param position
     * @param convertView
     * @param parent
     *
     * @return
     */
    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        View     view;
        MenuList menu      = mList.get( position );
        int      mPosition = menu.getTag();
        int      mArg0     = menu.getArg0();
        
        TextView     headerText, mainText, subText;
        SwitchCompat chkBox;
        
        //
        //--------------------------------
        //
        switch ( getItemViewType( position ) )
        {
            case LIST_HEADER:
                view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.settings_header, parent, false );
                headerText = view.findViewById( R.id.headerText );
                headerText.setText( menu.getHeaderText() );
                break;
            case LIST_CHECKBOX:
                view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.settings_checkbox, parent, false );
                mainText = view.findViewById( R.id.mainText );
                subText = view.findViewById( R.id.subText );
                chkBox = view.findViewById( R.id.checkBox );
                chkBox.setOnClickListener( this );
                chkBox.setId( mArg0 );
                
                //
                mainText.setText( menu.getMainText() );
                subText.setText( menu.getSubText() );
                if ( (global.settingsFlag & mArg0) != 0 && mPosition == position )
                {
                    chkBox.setChecked( true );
                }
                //
                break;
            default:
            case LIST_ITEM:
                view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.settings_item, parent, false );
                mainText = view.findViewById( R.id.mainText );
                subText = view.findViewById( R.id.subText );
                
                mainText.setText( menu.getMainText() );
                subText.setText( menu.getSubText() );
                break;
        }
        
        return view;
    }
    
    
    /**
     * Click listener for Checkboxes / Switches
     *
     * @param v
     */
    @Override
    public void onClick( View v )
    {
        SwitchCompat chkBox        = ( SwitchCompat ) v;
        int          checked_value = v.getId();
        
        if ( chkBox.isChecked() )
        {
            global.settingsFlag |= checked_value;
        }
        else
        {
            global.settingsFlag &= ~checked_value;
        }
        
        //
        Toolbar tb = activity.findViewById( R.id.settingsBar );
        tb.setKeepScreenOn( (global.settingsFlag & MenuData.SCREEN_STATUS) != 0 );
        
        
        //##############################
        //
        // Is it a root request
        //
        //##############################
        if ( (global.settingsFlag & MenuData.ROOT_ACCESS) != 0 )
        {
            if ( !ExecuteAsRoot.canRunRootCommands( mContext ) )
            {
                global.settingsFlag &= ~checked_value;
                chkBox.setChecked( false );
            }
        }
    }
}