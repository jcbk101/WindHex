package com.GenesysEast.windhex.Dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.GlobalClasses.RomFileAccess;
import com.GenesysEast.windhex.R;


import java.io.File;
import java.text.DateFormat;
import java.util.Locale;

public class AboutDialog
        extends DialogFragment
        implements View.OnClickListener, DialogInterface.OnKeyListener
{
    private       Globals global     = Globals.getInstance();
    private       File    fileName   = null;
    public static boolean whichAbout = false;
    
    /**
     * Only used when ROM Info requested
     *
     * @param fileName N/A
     */
    public AboutDialog( File fileName )
    {
        this.fileName = fileName;
    }
    
    /**
     * All other instantiations
     */
    public AboutDialog()
    {
    }
    
    @Override
    public void dismiss()
    {
        super.dismiss();
        
        whichAbout = false;
        fileName = null;
    }
    
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        View     button;
        View     view;
        String   temp;
        TextView tv;
        
        
        if ( whichAbout )
        {   // fix orientation change
            fileName = global.fileName;
        }
        
        //
        if ( fileName != null )
        {   // Requesting ROM information
            view = View.inflate( getContext(), R.layout.rom_info_layout, null );
            
            // ROM file name
            tv = view.findViewById( R.id.romInfoName );
            tv.setText( Html.fromHtml( getString( R.string.rom_filename_text ) ) );
            tv.append( global.fileName.getName() );
            
            // ROM size
            // Add comma format length in parenthesis
            temp = RomFileAccess.formatFileSize( global.fileName.length(), 0 );
            //
            tv = view.findViewById( R.id.romInfoSize );
            tv.setText( Html.fromHtml( getString( R.string.rom_size_text ) ) );
            tv.append( temp );
            tv.append( String.format( Locale.getDefault(), " ( %,d )", global.fileName.length() ) );
            
            // ROM modification date
            tv = view.findViewById( R.id.romInfoDate );
            //
            tv.setText( Html.fromHtml( getString( R.string.rom_date_text ) ) );
            
            DateFormat df = DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault() );
            temp = df.format( global.fileName.lastModified() );
            tv.append( temp );
            
            // Exit button clicker assignment
            button = view.findViewById( R.id.romExitButton );
            button.setOnClickListener( this );
            // Cloxe "X" button
            button = view.findViewById( R.id.cancelInfo );
            button.setOnClickListener( this );
            
            whichAbout = true;
        }
        else
        {   // Requesting a basic About dialog
            view = View.inflate( getContext(), R.layout.about_layout, null );
            
            // Privacy Policy
            tv = view.findViewById( R.id.privacyLink );
            //            tv.setText( R.string.privacy_link_text );
            tv.setMovementMethod( LinkMovementMethod.getInstance() );
            
            // Exit button clicker assignment
            button = view.findViewById( R.id.aboutExitButton );
            button.setOnClickListener( this );
            
            whichAbout = false;
        }
        
        
        //
        if ( getDialog() != null && getDialog().getWindow() != null )
        {
            getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
            getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
            //
            //            getDialog().getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
            getDialog().setOnKeyListener( this );
        }
        
        return view;
    }
    
    
    /**
     * Enable pressing Back
     *
     * @param dialog  N/A
     * @param keyCode N/A
     * @param event   N/A
     *
     * @return N/A
     */
    @Override
    public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event )
    {
        if ( keyCode == KeyEvent.KEYCODE_BACK )
        {
            dismiss();
            return true;
        }
        
        return false;
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
        if ( getDialog() != null )
        {
            getDialog().dismiss();
        }
        else
        {
            dismiss();
        }
        whichAbout = false;
        fileName = null;
    }
}
