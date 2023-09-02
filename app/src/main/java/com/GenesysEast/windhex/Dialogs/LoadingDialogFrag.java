package com.GenesysEast.windhex.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.GenesysEast.windhex.R;
import com.GenesysEast.windhex.WindHexActivity;

public class LoadingDialogFrag
        extends DialogFragment
        implements DialogInterface.OnDismissListener
{
    private Context  context;
    private Activity activity;
    private View     view;
    private String   message;
    private boolean  isShowing = false;
    
    
    /**
     * Constructors
     */
    public LoadingDialogFrag()
    {
    }
    
    public LoadingDialogFrag( String message )
    {
        this.message = message;
    }
    
    /**
     * Setters and Getters
     *
     * @return
     */
    public String getMessage()
    {
        return message;
    }
    
    public void setMessage( String message )
    {
        this.message = message;
    }
    
    public boolean isShowing()
    {
        return isShowing;
    }
    
    @Override
    public void onDismiss( DialogInterface dialog )
    {
        setLockScreenOrientation( getActivity(), false );
        isShowing = false;
    }
    
    
    @Override
    public void onResume()
    {
        super.onResume();
        Dialog dialog = getDialog();
        int    width;
        int    height;
        //        int    displayMode = getResources().getConfiguration().orientation;
        
        if ( dialog != null && dialog.getWindow() != null )
        {
            width = ViewGroup.LayoutParams.MATCH_PARENT;
            height = ViewGroup.LayoutParams.MATCH_PARENT;
            
            dialog.getWindow().setLayout( width, height );
        }
    }
    
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        view = inflater.inflate( R.layout.loading_layout, null );
        TextView tv = view.findViewById( R.id.progressMessage );
        
        // Build "Searching..." alert
        tv.setText( message );
        
        if ( !message.toUpperCase().equals( "LOADING FILE...PLEASE WAIT" ) )
        {
            View button = view.findViewById( R.id.loadingCancel );
            
            button.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    WindHexActivity.canBreakSearch = true;
                    dismiss();
                }
            } );
        }
        
        // Lock the damn rotation
        setLockScreenOrientation( getActivity(), true );
        
        
        //##############################
        //
        //
        //
        //##############################
        if ( getDialog() != null && getDialog().getWindow() != null )
        {
            getDialog().setOnDismissListener( this );
            //
            getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
            getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
            
        }
        
        // Set it to showing
        isShowing = true;
        
        return view;
        
    }
    
    
    /*
     */
    /**
     * Multi use "Now loading" type dialog builder
     *
     * @param message N/A
     *
     * @return N/A
     *//*

    public AlertDialog makeDialog( Context context, final Activity activity, String message )
    {
        final AlertDialog.Builder builder  = new AlertDialog.Builder( context, R.style.Theme_Alert );
        LayoutInflater            inflater = LayoutInflater.from( context );
        View                      view     = inflater.inflate( R.layout.loading_layout, null );
        TextView                  tv       = view.findViewById( R.id.progressMessage );
        
        // Build "Searching..." alert
        tv.setText( message );
        
        if ( !message.toUpperCase().equals( "LOADING FILE...PLEASE WAIT" ) )
        {
            builder.setNegativeButton( "Cancel", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    WindHexActivity.canBreakSearch = true;
                    dialog.dismiss();
                }
            } );
        }
        
        // Lock the damn rotation
        setLockScreenOrientation( activity, true );
        
        //
        builder.setView( view );
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener( this );
        
        return dialog;
    }
*/
    
    /**
     * Disable / Enable screen rotation
     *
     * @param lock N/A
     */
    public static void setLockScreenOrientation( Activity activity, boolean lock )
    {
        if ( Build.VERSION.SDK_INT >= 18 )
        {
            activity.setRequestedOrientation( lock ? ActivityInfo.SCREEN_ORIENTATION_LOCKED : ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR );
            return;
        }
        
        if ( lock )
        {   // Enable locking until done
            switch ( activity.getWindowManager().getDefaultDisplay().getRotation() )
            {
                case 0:
                    activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
                    break; // value 1
                case 2:
                    activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT );
                    break; // value 9
                case 1:
                    activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
                    break; // value 0
                case 3:
                    activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE );
                    break; // value 8
            }
        }
        else
        {   // Release the lock
            activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR ); // value 10
        }
    }
}



