package com.GenesysEast.windhex.Hex_Drawer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.R;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Drawer List adapter
 * Used with recent files list
 */
public class DrawerList
        extends BaseAdapter
        implements View.OnClickListener
{
    private Globals           global = Globals.getInstance();
    private ArrayList<String> recentList;
    private Context           mContext;
    public  OnItemClicked     onItemClicked;
    
    public interface OnItemClicked
    {
        void itemClicked( int position );
    }
    
    /**
     * Basic constructor
     *
     * @param recentList N/A
     * @param mContext   N/A
     */
    public DrawerList( ArrayList<String> recentList, Context mContext )
    {
        this.recentList = recentList;
        this.mContext = mContext;
        onItemClicked = ( OnItemClicked ) mContext;
    }
    
    @Override
    public void onClick( View v )
    {
        int position = v.getId();
        
        onItemClicked.itemClicked( position );
    }
    
    /**
     * Overridden methods
     *
     * @return N/A
     */
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
     * Returns a View built from scratch
     * or already developed
     *
     * @param position    N/A
     * @param convertView N/A
     * @param parent      N/A
     *
     * @return N/A
     */
    @Override
    public View getView( final int position, View convertView, final ViewGroup parent )
    {
        //-----------------------------
        //
        // We display an open button if
        // no files exist
        //
        //-----------------------------
        try
        {
            String text = recentList.get( position ).toUpperCase();
            
            if ( recentList.size() == 1 && text.contentEquals( "OPEN_FILE_BUTTON" ) )
            {
                //            Button openButton;
                
                LayoutInflater inflater = LayoutInflater.from( mContext );
                convertView = inflater.inflate( R.layout.hex_open_button, parent, false );
                
                //            openButton = convertView.findViewById( R.id.openButton );
                
                return convertView;
            }
            else if ( recentList.size() > 0 )
            {
                TextView nameText;
                TextView dateText;
                //            ImageView  exitButton;
                ImageButton exitButton;
                File        item = new File( recentList.get( position ) );
                String      dateStr;
                final View  view;
                
                
                if ( convertView == null )
                {
                    LayoutInflater inflater = LayoutInflater.from( mContext );
                    convertView = inflater.inflate( R.layout.hex_drawer_view, parent, false );
                }
                else if ( convertView.getTag() != null && ( int ) convertView.getTag() == 1 )
                {   // Item / view was deleted, re-inflate the view for use
                    LayoutInflater inflater = LayoutInflater.from( mContext );
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
                nameText = view.findViewById( R.id.nameText );
                dateText = view.findViewById( R.id.dateText );
                //
                // Send needed info to animator
                //
                if ( exitButton != null )
                {
                    exitButton.setOnClickListener( new View.OnClickListener()
                    {
                        @Override
                        public void onClick( View v )
                        {
                            deleteListItem( view, position );
                        }
                    } );
                }
                
                nameText.setText( item.getName() );
                dateText.setText( R.string.date_text );
                
                // Indicate which file is active
                nameText.setTextColor( Color.WHITE );
                if ( item.toString().equals( global.fileName.toString() ) )
                {
                    nameText.setTextColor( Color.YELLOW );
                    //
                    String temp = "Current File:\n" + item.getName();
                    nameText.setText( temp );
                }
                
                DateFormat df = DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault() );
                //
                if ( item.exists() )
                {
                    dateStr = df.format( item.lastModified() );
                    dateText.append( dateStr );
                }
                
                view.setId( position );
                view.setOnClickListener( this );
                return view;
            }
            else
            {
                if ( convertView == null )
                {
                    LayoutInflater inflater = LayoutInflater.from( mContext );
                    convertView = inflater.inflate( R.layout.hex_drawer_view, parent, false );
                }
                
                convertView.setId( position );
                convertView.setOnClickListener( this );
                return convertView;
            }
        }
        catch ( NullPointerException npe )
        {
            npe.getMessage();
            if ( convertView == null )
            {
                LayoutInflater inflater = LayoutInflater.from( mContext );
                convertView = inflater.inflate( R.layout.hex_drawer_view, parent, false );
            }
            
            convertView.setId( position );
            convertView.setOnClickListener( this );
            return convertView;
        }
    }
    
    /**
     * Delete a recent file reference
     *
     * @param view     N/A
     * @param position N/A
     */
    private void deleteListItem( final View view, final int position )
    {
        final int           initialHeight = view.getMeasuredHeight();
        final ValueAnimator animation     = ValueAnimator.ofInt( 0, view.getMeasuredHeight() );
        
        view.animate().setDuration( 300 ).translationX( -view.getMeasuredWidth() ).setListener( new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd( Animator animation1 )
            {
                view.setVisibility( View.GONE );
                animation.start();
            }
        } );
        
        // Second animation
        animation.setDuration( 200 ).addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate( ValueAnimator animation )
            {
                view.getLayoutParams().height = initialHeight - ( int ) animation.getAnimatedValue();
                view.requestLayout();
            }
        } );
        //
        animation.addListener( new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd( Animator animation )
            {
                if ( recentList != null && recentList.size() > 0 )
                {
                    recentList.remove( position );
                    view.setTag( 1 );
                    
                    // If the final file deleted, add OPEN button
                    if ( recentList.size() == 0 )
                    {
                        recentList.add( "open_file_button" );
                    }
                    //
                    notifyDataSetChanged();
                }
            }
        } );
    }
}
