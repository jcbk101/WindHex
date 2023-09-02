package com.GenesysEast.windhex.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.GenesysEast.windhex.R;


public class AppRate
        extends DialogFragment
        implements View.OnClickListener
{
    public static boolean                  isShowing;
    // Package Name
    private       String                   APP_PNAME             = "com.GenesysEast.windhex";
    // Min number of days
    public static int                      DAYS_UNTIL_PROMPT     = 3;
    // Min number of launches
    public static int                      LAUNCHES_UNTIL_PROMPT = 3;
    private       Context                  mContext;
    private       SharedPreferences.Editor editor;
    private       View                     view;
    
    
    /**
     * Constructor
     */
    public AppRate( Context mContext, SharedPreferences.Editor editor )
    {
        this.mContext = mContext;
        this.editor = editor;
    }
    
    public AppRate()
    {
    }
    
    
    /**
     * Return if user viewed new message
     *
     * @param context
     *
     * @return
     */
    public static boolean getAppSuggestion( Context context )
    {
        SharedPreferences        prefs;
        SharedPreferences.Editor editor;
        
        // First fail test for this code
        if ( context != null )
        {
            prefs = context.getSharedPreferences( "app_rate_sys", Context.MODE_PRIVATE );
            return prefs.getBoolean( "msg_to_users", false );
        }
        
        return true;
    }
    
    
    /**
     * Set that the user has viewed the new message
     *
     * @param context
     */
    public static void setAppSuggestion( Context context )
    {
        SharedPreferences        prefs;
        SharedPreferences.Editor editor;
        
        // First fail test for this code
        if ( context != null )
        {
            prefs = context.getSharedPreferences( "app_rate_sys", Context.MODE_PRIVATE );
            editor = prefs.edit();
            
            editor.putBoolean( "msg_to_users", true );
            
            editor.commit();
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
    }
    
    
    /**
     * Make sure we can show again after closing.
     * also no second calls on orientation changes
     */
    @Override
    public void dismiss()
    {
        isShowing = false;
    }
    
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        View button;
        
        //
        view = View.inflate( getContext(), R.layout.rate_app_layout, null );
        
        button = view.findViewById( R.id.retaApp );
        button.setOnClickListener( this );
        
        button = view.findViewById( R.id.remindLater );
        button.setOnClickListener( this );
        
        button = view.findViewById( R.id.noThanks );
        button.setOnClickListener( this );
        
        button = view.findViewById( R.id.rateCancel );
        button.setOnClickListener( this );
        
        
        //
        if ( getDialog() != null && getDialog().getWindow() != null )
        {
            getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
            getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
            getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        }
        
        //builder.setCancelable( false );
        return view;
        
    }
    
    
    /**
     * Click listener for the button on
     * rom info form
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        int id = v.getId();
        
        if ( id == R.id.retaApp )
        {   // User clicked "Rate APP"
            if ( editor != null )
            {
                Toast.makeText( mContext, "Thank you very much", Toast.LENGTH_SHORT ).show();
                
/*
                try
                {
                    mContext.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=" + APP_PNAME ) ) );
                }
                catch ( android.content.ActivityNotFoundException nfe )
                {
*/
                try
                {
                    mContext.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "http://play.google.com/store/apps/details?id=" + APP_PNAME ) ) );
                }
                catch ( android.content.ActivityNotFoundException nfe2 )
                {
                    Toast.makeText( mContext, "Could not load Market place", Toast.LENGTH_SHORT ).show();
                }
                //                }
                finally
                {   // At least they clicked the rate button
                    editor.putBoolean( "hide_app_rate", true );
                    editor.commit();
                }
            }
        }
        else if ( id == R.id.remindLater )
        {   // Reset the Rate Me to show again in 1 days
            if ( editor != null )
            {
                Toast.makeText( mContext, "Thanks! I'll try.", Toast.LENGTH_SHORT ).show();
                editor.putLong( "days_to_wait", 1 );
                // Relaunch again after 3 relaunches again
                editor.putLong( "launch_count", 0 );
                editor.commit();
            }
        }
        else
        {   // User clicked "No Thanks"
            if ( editor != null )
            {
                Toast.makeText( mContext, "Thank you. Will not ask again...", Toast.LENGTH_SHORT ).show();
                //
                editor.putBoolean( "hide_app_rate", true );
                editor.commit();
            }
        }
        
        // Close the dialog
        getDialog().dismiss();
    }
}
