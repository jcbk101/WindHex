package com.GenesysEast.windhex.table_maker.Adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.*;

import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.GlobalClasses.Tabler;
import com.GenesysEast.windhex.R;

import java.util.ArrayList;

public class TableLister
        extends BaseAdapter
{
    final public static int               SLIDE_DURATION = 200;
    private             Globals           global         = Globals.getInstance();
    private             Context           mContext;
    private             ArrayList<Tabler> tableList;
    private             ListView          myLister;
    private             EditText          editText;
    private             int               editColor;
    private             int               isEditMode;
    private             int[]             sliders        = { R.id.editButton, R.id.eraseButton };
    
    
    /**
     * Constructor
     *
     * @param mContext
     * @param tableList
     * @param listView
     */
    public TableLister( Context mContext, ArrayList<Tabler> tableList, ListView listView )
    {
        this.mContext = mContext;
        this.tableList = tableList;
        this.myLister = listView;
    }
    
    public int getEditColor()
    {
        return editColor;
    }
    
    public void setEditColor( int editColor )
    {
        this.editColor = editColor;
    }
    
    /**
     * Getters and Setters for the adapter data
     *
     * @return
     */
    public int getIsEditMode()
    {
        return isEditMode;
    }
    
    public void setIsEditMode( int isEditMode )
    {
        this.isEditMode = isEditMode;
    }
    
    public EditText getEditText()
    {
        return editText;
    }
    
    public void setEditText( EditText editText )
    {
        this.editText = editText;
    }
    
    /**
     * Over ridden methods
     *
     * @return
     */
    @Override
    public int getCount()
    {
        return tableList.size();
    }
    
    @Override
    public Object getItem( int position )
    {
        return tableList.get( position );
    }
    
    @Override
    public long getItemId( int position )
    {
        return position;
    }
    
    /**
     * View builder for table data list
     *
     * @param position
     * @param convertView
     * @param parent
     *
     * @return
     */
    @Override
    public View getView( final int position, View convertView, ViewGroup parent )
    {
        TextView   tv;
        final View view;
        final View editView;
        final View eraseView;
        View       buttons;
        
        String  item     = tableList.get( position ).getLabel();
        boolean selected = tableList.get( position ).isFlag();
        int     width;
        
        //
        // Inflate the layout
        //
        if ( convertView == null )
        {
            //            LayoutInflater inflater = ( LayoutInflater ) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            convertView = View.inflate( mContext, R.layout.tabler_list_item, null );
        }
        // Rebuild after a removal
        else if ( convertView.getTag() != null && ( int ) convertView.getTag() == 1 )
        {
            convertView = View.inflate( mContext, R.layout.tabler_list_item, null );
            convertView.setTag( 0 );
        }
        
        //
        view = convertView;
        
        
        //###############################
        //
        // Move the Edit / Erase buttons
        //
        //###############################
        if ( view != null )
        {
            // Set up the views like buttons
            editView = view.findViewById( R.id.editButton );
            editView.setVisibility( View.INVISIBLE );
            editView.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    processTableData( view, position, true );
                }
            } );
            //
            eraseView = view.findViewById( R.id.eraseButton );
            eraseView.setVisibility( View.INVISIBLE );
            eraseView.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    processTableData( view, position, false );
                }
            } );
            
            
            //#############################
            //
            // Check to see if buttons
            // showing on a view
            //
            //#############################
            if ( selected )
            {
                // User clicked the view, display the Edit / Erase buttons
                // Already animated, just display them
                eraseView.setVisibility( View.VISIBLE );
                editView.setVisibility( View.VISIBLE );
    
/*
                for ( int i : sliders )
                {
                    View s = view.findViewById( i );
                    s.setVisibility( View.VISIBLE );
                    //                    ObjectAnimator expand = ObjectAnimator.ofInt( s, "width", 0, s.getWidth() );
                    
                    //                    expand.setDuration( SLIDE_DURATION ).setInterpolator( new OvershootInterpolator() );
                    //                    expand.start();
                }
*/
            }
            else
            {
                // Collapse any visible buttons
                if ( eraseView.getVisibility() == View.VISIBLE )
                {
                    eraseView.setVisibility( View.INVISIBLE );
                    editView.setVisibility( View.INVISIBLE );
/*
                    for ( int i : sliders )
                    {
                        View           s        = view.findViewById( i );
                        ObjectAnimator collapse = ObjectAnimator.ofInt( s, "width", s.getWidth(), 0 );
                        
                        collapse.setDuration( SLIDE_DURATION ).setInterpolator( new OvershootInterpolator() );
                        collapse.addListener( new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd( Animator animation )
                            {
                                s.setVisibility( View.INVISIBLE );
                                super.onAnimationEnd( animation );
                            }
                        } );
                        collapse.start();
                    }
*/
                }
            }
            
            
            //#############################
            //
            // Display the table data
            // requested
            //
            //#############################
            tv = view.findViewById( R.id.tableListItem );
            tv.setText( item );
            
            
            if ( isEditMode != 1 && position == isEditMode )
            {
                tv.setTextColor( Color.RED );
                tv.setTypeface( Typeface.DEFAULT, Typeface.BOLD_ITALIC );
            }
            else
            {
                tv.setTextColor( Color.BLACK );
                tv.setTypeface( Typeface.DEFAULT, Typeface.NORMAL );
            }
            //
        }
        
        return view;
    }
    
    
    /**
     * Process a table editor's request to
     * delete or edit an entry
     *
     * @param view
     * @param position
     * @param function
     */
    public void processTableData( final View view, final int position, boolean function )
    {
        final String   item     = tableList.get( position ).getLabel();
        final EditText editText = this.editText;
        String         temp;
        
        //
        //#############################
        //
        if ( !item.equals( "" ) && function )
        {
            // Edit the value was requested
            temp = editText.getText().toString();
            editText.setTag( temp );
            editText.setText( "" );
            editText.append( item );
            isEditMode = position;
            editColor = editText.getCurrentTextColor();
            
            //
            editText.setTextColor( Color.RED );
            editText.setTypeface( Typeface.DEFAULT, Typeface.ITALIC );
            
            //############################
            //
            // Hide buttons
            //
            //############################
            tableList.get( position ).setFlag( false );
            
            //
            for ( int i : sliders )
            {
                View           s        = view.findViewById( i );
                ObjectAnimator collapse = ObjectAnimator.ofInt( s, "width", 0, s.getWidth() );
                
                collapse.setDuration( SLIDE_DURATION ).setInterpolator( new LinearInterpolator() );
                //
                if ( i == sliders[ 1 ] )
                {
                    collapse.addListener( new Animator.AnimatorListener()
                    {
                        @Override
                        public void onAnimationStart( Animator animation )
                        {
                            // Disable scrolling until editing is complete
                            myLister.setEnabled( false );
                            notifyDataSetChanged();
                        }
                        
                        @Override
                        public void onAnimationEnd( Animator animation )
                        {
                        
                        }
                        
                        @Override
                        public void onAnimationCancel( Animator animation )
                        {
                        
                        }
                        
                        @Override
                        public void onAnimationRepeat( Animator animation )
                        {
                        
                        }
                    } );
                }
                
                collapse.start();
            }
        }
        else
        {
            // Entry deletion requested
            final View layout = view.findViewById( R.id.tableListLayout );
            
            layout.animate().translationX( -view.getMeasuredWidth() ).setInterpolator( new LinearInterpolator() );
            layout.animate().setDuration( SLIDE_DURATION ).withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    ValueAnimator shrink = ValueAnimator.ofInt( layout.getHeight(), 0 );
                    
                    //
                    // Second animation
                    //
                    shrink.setDuration( SLIDE_DURATION ).setInterpolator( new LinearInterpolator() );
                    shrink.addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                    {
                        @Override
                        public void onAnimationUpdate( ValueAnimator animation )
                        {
                            layout.getLayoutParams().height = ( int ) animation.getAnimatedValue();
                            layout.requestLayout();
                        }
                    } );
                    shrink.addListener( new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd( Animator animation )
                        {
                            tableList.remove( position );
                            view.setTag( 1 );
                            notifyDataSetChanged();
                            
                            super.onAnimationEnd( animation );
                        }
                    } );
                    // Start the shrinker!
                    shrink.start();
                }
            } ).start();
        }
    }
}
