package com.GenesysEast.windhex.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.GlobalClasses.PointXYZ;
import com.GenesysEast.windhex.GlobalClasses.RomFileAccess;
import com.GenesysEast.windhex.R;

public class SelectDialog
        extends DialogFragment
        implements View.OnClickListener, TextWatcher
{
    private Globals       global  = Globals.getInstance();
    private RomFileAccess rom     = RomFileAccess.getInstance();
    private Handler       handler = new Handler( Looper.getMainLooper() );
    private EditText      startInput;
    private EditText      endInput;
    private EditText      fillInput;
    private int           fillByte;
    private Activity      activity;
    private View          view;
    
    
    /**
     * //################################
     * <p>
     * Interface method declarations
     * <p>
     * //################################
     */
    public interface OnSelectListener
    {
        void getSelectedResult( boolean status );
    }
    
    private OnSelectListener selectListener;
    
    public SelectDialog()
    {
    }
    
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        if ( getActivity() != null )
        {
            Button button;
            
            // Need to get a copy of the activity for the thread.
            // activity must be freed or we have a leak!
            activity = getActivity();
            
            // Get the view
            view = View.inflate( getContext(), R.layout.select_all_layout, null );
            
            startInput = view.findViewById( R.id.startOffset );
            endInput = view.findViewById( R.id.endOffset );
            fillInput = view.findViewById( R.id.fillByte );
            //
            startInput.append( Long.toString( global.xyHex.x, 16 ).toUpperCase() );
            endInput.append( Long.toString( global.xyHex.y, 16 ).toUpperCase() );
            fillInput.setText( "" );
            //
            button = view.findViewById( R.id.selectCancelButton );
            button.setOnClickListener( this );
            
            // Show it?
            fillInput.setVisibility( View.GONE );
            //
            button = view.findViewById( R.id.selectFillButton );
            button.setText( R.string.hex_select_data );
            button.setOnClickListener( this );
            
            startInput.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( ( int ) global.ofsTextWidth.x ) } );
            endInput.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( ( int ) global.ofsTextWidth.x ) } );
            // Text watcher to force caps
            startInput.addTextChangedListener( this );
            endInput.addTextChangedListener( this );
            fillInput.addTextChangedListener( this );
            
            //
            if ( this.getTag() != null && this.getTag().equals( "Filler" ) )
            {
                fillInput.setVisibility( View.VISIBLE );
                button.setText( R.string.select_data_filler );
                fillInput.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 2 ) } );
            }
            
            //
            if ( getDialog() != null && getDialog().getWindow() != null )
            {
                getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
                getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
                //
                //            getDialog().getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
            }
            
            return view;
        }
        else
        {
            return null;
        }
    }
    
    
    /**
     * //################################
     * <p>
     * Handle all button clicks
     * <p>
     * //################################
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        int  startOfs;
        int  endOfs;
        long lNum;
        
        
        if ( v.getId() == R.id.selectCancelButton )
        {
            dismiss();
        }
        else if ( this.getTag() != null && this.getTag().equals( "Filler" ) )
        {
            if ( startInput.length() != 0 && endInput.length() != 0 && fillInput.length() != 0 && getActivity() != null )
            {
                // Close current dialog
                dismiss();
                
                final int               fixStart;
                final int               fixEnd;
                final LoadingDialogFrag warning = new LoadingDialogFrag( "Byte fill..." );
                
                // Create a new dialog
                warning.setCancelable( false );
                warning.show( getActivity().getSupportFragmentManager(), null );
                
                try
                {
                    // Get the needed values
                    fillByte = Integer.parseInt( fillInput.getText().toString(), 16 ) & 0xFF;
                    
                    //
                    //#############################
                    //
                    lNum = Long.parseLong( startInput.getText().toString(), 16 );
                    if ( lNum > global.fileSize )
                    {
                        lNum = global.fileSize;
                    }
                    else if ( lNum < 0 )
                    {
                        lNum = 0;
                    }
                    //
                    startOfs = ( int ) lNum;
                    
                    
                    //
                    //#############################
                    //
                    lNum = Long.parseLong( endInput.getText().toString(), 16 );
                    if ( lNum > global.fileSize )
                    {
                        lNum = global.fileSize;
                    }
                    else if ( lNum < 0 )
                    {
                        lNum = 0;
                    }
                    //
                    endOfs = ( int ) lNum;
                    
                    
                    // Fix negative offsets
                    if ( startOfs > endOfs )
                    {
                        int temp = startOfs;
                        startOfs = endOfs;
                        endOfs = temp;
                    }
                    
                    //
                    fixStart = startOfs;
                    fixEnd = endOfs;
                    
                    // Run this stuff in the background
                    Thread thread = new Thread( new Runnable()
                    {
                        public void run()
                        {
                            final int result = rom.byteFill( ( byte ) fillByte, fixStart, (fixEnd - fixStart) + 1 );
                            // UI Thread
                            handler.post( new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if ( warning.isShowing() )
                                    {
                                        warning.dismiss();
                                    }
                                    
                                    // Undo list addition
                                    rom.undoList.add( new PointXYZ( fixStart, fixEnd, ((fixStart / global.columnCount) * global.columnCount), ((fixEnd - fixStart) + 1) ) );
                                    //
                                    Toast.makeText( activity, " Replaced " + result + " byte(s)", Toast.LENGTH_SHORT ).show();
                                    global.hView.invalidate();
                                    selectListener.getSelectedResult( true );
                                    
                                    // Allow the activity to be leak free
                                    activity = null;
                                }
                            } );
                        }
                    } );
                    
                    
                    //###############################
                    //
                    // Run the code
                    //
                    //###############################
                    thread.start();
                }
                catch ( Exception ex )
                {
                    ex.printStackTrace();
                }
            }
        }
        else
        {   // Select All feature
            if ( startInput.length() != 0 && endInput.length() != 0 )
            {
                //
                //#############################
                //
                lNum = Long.parseLong( startInput.getText().toString(), 16 );
                if ( lNum > global.fileSize )
                {
                    lNum = global.fileSize;
                }
                else if ( lNum < 0 )
                {
                    lNum = 0;
                }
                //
                startOfs = ( int ) lNum;
                
                
                //
                //#############################
                //
                lNum = Long.parseLong( endInput.getText().toString(), 16 );
                if ( lNum > global.fileSize )
                {
                    lNum = global.fileSize;
                }
                else if ( lNum < 0 )
                {
                    lNum = 0;
                }
                //
                endOfs = ( int ) lNum;
                
/*
                startOfs = Integer.parseInt( startInput.getText().toString(), 16 );
                endOfs = Integer.parseInt( endInput.getText().toString(), 16 );
*/
                global.xyHex.x = startOfs;
                global.xyHex.y = endOfs;
                dismiss();
                //
                global.hView.invalidate();
            }
        }
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
/*
        int width       = getResources().getDisplayMetrics().widthPixels;
        int displayMode = getResources().getConfiguration().orientation;
        
        if ( getDialog() != null )
        {
            if ( getDialog().getWindow() != null )
            {
                getDialog().getWindow().setLayout( width - (width / 6), ViewGroup.LayoutParams.WRAP_CONTENT );
            }
            
            if ( displayMode == Configuration.ORIENTATION_LANDSCAPE )
            {
                getDialog().getWindow().setLayout( (width - (width / 4)), ViewGroup.LayoutParams.WRAP_CONTENT );
            }
        }
*/
    }
    
    /**
     * //################################
     * <p>
     * Text watchers
     * <p>
     * //################################
     *
     * @param s     N/A
     * @param start N/A
     * @param count N/A
     * @param after N/A
     */
    @Override
    public void beforeTextChanged( CharSequence s, int start, int count, int after )
    {
        
    }
    
    @Override
    public void onTextChanged( CharSequence s, int start, int before, int count )
    {
        
    }
    
    @Override
    public void afterTextChanged( Editable s )
    {
        String str = s.toString().toUpperCase();
        
        // Which edit text
        if ( startInput.getText().hashCode() == s.hashCode() )
        {
            startInput.removeTextChangedListener( this );
            startInput.getText().clear();
            //startInput.setText( "" );
            startInput.append( str );
            startInput.addTextChangedListener( this );
        }
        else if ( endInput.getText().hashCode() == s.hashCode() )
        {
            endInput.removeTextChangedListener( this );
            endInput.getText().clear();
            //endInput.setText( "" );
            endInput.append( str );
            endInput.addTextChangedListener( this );
        }
        else if ( fillInput.getText().hashCode() == s.hashCode() )
        {
            fillInput.removeTextChangedListener( this );
            fillInput.getText().clear();
            //fillInput.setText( "" );
            fillInput.append( str );
            fillInput.addTextChangedListener( this );
        }
    }
    
    
    /**
     * //################################
     * <p>
     * Attach the interface to whom ever
     * request it's usage
     * <p>
     * //################################
     *
     * @param context N/A
     */
    @Override
    public void onAttach( Context context )
    {
        super.onAttach( context );
        
        try
        {
            selectListener = ( OnSelectListener ) getActivity();
        }
        catch ( ClassCastException e )
        {
            e.getMessage();
        }
    }
}



