package com.GenesysEast.windhex.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.GenesysEast.windhex.CustomControls.GradientTextView;
import com.GenesysEast.windhex.R;
import com.GenesysEast.windhex.WindHexActivity;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class RecentDialog
        extends DialogFragment
        implements AdapterView.OnItemClickListener, View.OnClickListener
{
    private OnFileSelected onFileSelected;
    
    /**
     * Interface method declarations
     */
    public interface OnFileSelected
    {
        void getFileName( String fileName );
    }
    
    public RecentDialog()
    {
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        Dialog dialog = getDialog();
        
        if ( dialog != null && dialog.getWindow() != null )
        {
            int width  = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            
            dialog.getWindow().setLayout( width, height );
        }
    }
    
    
    /**
     * Create the main view
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     *
     * @return
     */
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        ListView         recentView;
        RecentAdapter    listAdapter;
        View             view;
        ImageView        exit;
        GradientTextView text;
        
        
        // Get the view
        view = View.inflate( getContext(), R.layout.recent_layout, null );
        
        listAdapter = new RecentAdapter( WindHexActivity.recentFiles );
        recentView = view.findViewById( R.id.recentView );
        recentView.setAdapter( listAdapter );
        recentView.setOnItemClickListener( this );
        
        // Exit button
        exit = view.findViewById( R.id.cancelRecent );
        exit.setOnClickListener( this );
        
        //
        if ( getDialog() != null && getDialog().getWindow() != null )
        {
            getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
            getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
        }
        
        return view;
    }
    
    /**
     * Clicker for exit button
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        if ( v.getId() == R.id.cancelRecent )
        {
            dismiss();
        }
    }
    
    /**
     * Return the requested filename
     *
     * @param parent   N/A
     * @param view     N/A
     * @param position N/A
     * @param id       N/A
     */
    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id )
    {
        String str = WindHexActivity.recentFiles.get( position );
        
        if ( !str.toUpperCase().equals( "OPEN_FILE_BUTTON" ) )
        {
            onFileSelected.getFileName( WindHexActivity.recentFiles.get( position ) );
            dismiss();
        }
        else
        {
            dismiss();
        }
    }
    
    /**
     * Interface: Input listener
     *
     * @param context N/A
     */
    @Override
    public void onAttach( Context context )
    {
        super.onAttach( context );
        
        try
        {
            onFileSelected = ( OnFileSelected ) getActivity();
        }
        catch ( ClassCastException e )
        {
            e.getMessage();
        }
    }
    
    /**
     * Inner Array adapter for list view
     */
    private class RecentAdapter
            extends BaseAdapter
    {
        private ArrayList<String> recentList;
        
        /**
         * Constructor
         *
         * @param recentFiles N/A
         */
        private RecentAdapter( ArrayList<String> recentFiles )
        {
            this.recentList = recentFiles;
        }
        
        @Override
        public int getCount()
        {
            return recentList.size();
        }
        
        @Override
        public Object getItem( int position )
        {
            return recentList.get( position );
        }
        
        @Override
        public long getItemId( int position )
        {
            return position;
        }
        
        /**
         * View builder
         *
         * @param position    N/A
         * @param convertView N/A
         * @param parent      N/A
         *
         * @return N/A
         */
        @Override
        public View getView( int position, View convertView, ViewGroup parent )
        {
            TextView nameText;
            TextView dateText;
            //            ImageView  exitButton;
            ImageButton exitButton;
            File        item = new File( recentList.get( position ) );
            String      dateStr;
            View        view;
            DateFormat  df;
            
            //
            if ( convertView == null )
            {
                LayoutInflater inflater = LayoutInflater.from( getContext() );
                convertView = inflater.inflate( R.layout.hex_drawer_view, parent, false );
            }
            else if ( convertView.getTag() != null && ( int ) convertView.getTag() == 1 )
            {   // Item / view was deleted, re-inflate the view for use
                LayoutInflater inflater = LayoutInflater.from( getContext() );
                convertView = inflater.inflate( R.layout.hex_drawer_view, parent, false );
                convertView.setTag( 0 );
            }
            view = convertView;
            
            //--------------------------------
            //
            // Fill data tags
            //
            //--------------------------------
            exitButton = view.findViewById( R.id.exitDrawerButton );
            if ( exitButton != null )
            {
                exitButton.setVisibility( View.INVISIBLE );
            }
            //
            nameText = view.findViewById( R.id.nameText );
            dateText = view.findViewById( R.id.dateText );
            //
            nameText.setText( item.getName() );
            dateText.setText( R.string.date_text );
            
            df = DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault() );
            //            formatter = new SimpleDateFormat( "MM/dd/yyyy h:mm aa", Locale.getDefault() );
            //
            if ( item.exists() && item.canRead() )
            {
                dateStr = df.format( item.lastModified() );
                dateText.append( dateStr );
            }
            
            //
            if ( item.getName().toLowerCase().equals( "open_file_button" ) )
            {
                String temp = "Empty list";
                nameText.setText( temp );
                dateText.setText( "" );
            }
            
            return view;
        }
    }
}
