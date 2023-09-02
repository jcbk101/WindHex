package com.GenesysEast.windhex.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.R;
import com.GenesysEast.windhex.WindHexActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class ReplaceDialog
        extends DialogFragment
        implements View.OnClickListener, DialogInterface.OnKeyListener, AdapterView.OnItemSelectedListener
{
    private       Globals global            = Globals.getInstance();
    public static int     REPLACE_ALL       = 0x80;
    public static int     REPLACE_BEGINNING = 0x100;
    public static int     MAX_REPLACES      = 0x200;
    
    private        OnReplaceClick onReplaceClick;
    private static String[]       searchText       = { "", "", "", "" };
    private static String[]       replaceText      = { "", "", "", "" };
    private        String[]       convertedText    = new String[ 4 ];
    private        String[]       convertedReplace = new String[ 4 ];
    private        EditText       search;
    private        EditText       replace;
    private        Spinner        spinner;
    private        TextWatcher    replaceWatcher;
    private        TextWatcher    searchWatcher;
    private        View           view;
    
    
    /**
     * Interface method declarations
     */
    public interface OnReplaceClick
    {
        void getReplaceText( byte[] searchText, byte[] replaceText );
    }
    
    /**
     * Custom InputFilter
     */
    private InputFilter filter = new InputFilter()
    {
        @Override
        public CharSequence filter( CharSequence source, int start, int end, Spanned dest, int dstart, int dend )
        {
            boolean       keepOriginal = true;
            StringBuilder sb           = new StringBuilder( end - start );
            
            for ( int i = start; i < end; i++ )
            {
                char c = source.charAt( i );
                if ( isCharAllowed( c ) ) // put your condition here
                {
                    sb.append( c );
                }
                else
                {
                    keepOriginal = false;
                }
            }
            
            if ( keepOriginal )
            {
                return null;
            }
            else
            {
                if ( source instanceof Spanned )
                {
                    SpannableString sp = new SpannableString( sb );
                    TextUtils.copySpansFrom( ( Spanned ) source, start, sb.length(), null, sp, 0 );
                    return sp;
                }
                else
                {
                    return sb;
                }
            }
        }
        
        private boolean isCharAllowed( char c )
        {
            String hexChars = "0123456789abcedfABCDEF";
            
            return hexChars.indexOf( c ) >= 0;
        }
    };
    
    public ReplaceDialog()
    {
    }
    
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        ArrayAdapter<String> spinAdapter;
        ArrayList<String>    spinList;
        int                  index      = 0;
        int[]                boxIDs     = { R.id.replaceAll, R.id.replaceFromStart, R.id.maxReplaceList };
        CheckBox             box;
        View                 replaceBtn;
        View                 exit;
        TextView             tv;
        String[]             list       = { "Hexadecimal input", "Table file input", "ASCII input" };
        int[]                BITS_SPIN  = { 1, 2, 4 };
        int[]                BITS_CHECK = { REPLACE_ALL, REPLACE_BEGINNING, MAX_REPLACES };
        
        
        //#############################
        //
        // Get the view
        //
        //#############################
        view = View.inflate( getContext(), R.layout.replace_layout, null );
        
        // Set clickers for all
        for ( int c = 0; c < boxIDs.length; c++ )
        {
            box = view.findViewById( boxIDs[ c ] );
            box.setOnClickListener( this );
            box.setChecked( (WindHexActivity.searchFlags & BITS_CHECK[ c ]) != 0 );
        }
        
        //
        // Set the spinner text indexing list
        //
        spinList = new ArrayList<>( Arrays.asList( list ) );
        
        // Setup the adapter
        if ( getContext() != null )
        {
            spinAdapter = new ArrayAdapter<>( getContext(), R.layout.tabler_spinner_item, spinList );
            spinAdapter.setDropDownViewResource( R.layout.searcher_dropdown_list );
            // Add to spinner
            spinner = view.findViewById( R.id.replaceSpinner );
            spinner.setAdapter( spinAdapter );
            spinner.setOnItemSelectedListener( this );
        }
        
        //
        for ( int c = 0; c < list.length; c++ )
        {
            if ( (WindHexActivity.searchFlags & BITS_SPIN[ c ]) != 0 )
            {
                spinner.setSelection( c );
                break;
            }
        }
        
        //
        //#############################
        //
        search = view.findViewById( R.id.searchText );
        replace = view.findViewById( R.id.replaceText );
        
        replaceBtn = view.findViewById( R.id.replaceButton );
        exit = view.findViewById( R.id.cancelReplace );
        
        tv = view.findViewById( R.id.replaceNotice );
        tv.setText( Html.fromHtml( getString( R.string.replace_notice ) ) );
        
        //#############################
        //
        // Text watchers
        //
        //#############################
        setupTextatchers();
        search.setText( "" );
        search.addTextChangedListener( searchWatcher );
        //s
        replace.setText( "" );
        replace.addTextChangedListener( replaceWatcher );
        
        search.append( searchText[ index ] );
        replace.append( replaceText[ index ] );
        //
        //        edit.addTextChangedListener( this );
        //
        replaceBtn.setOnClickListener( this );
        exit.setOnClickListener( this );
        
        // support for Backpress
        getDialog().setOnKeyListener( this );
        
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
    
    /**
     * Spinner item click listener
     *
     * @param parent   N/A
     * @param view     N/A
     * @param position N/A
     * @param id       N/A
     */
    @Override
    public void onItemSelected( AdapterView<?> parent, View view, int position, long id )
    {   // Change the settings flag status
        setSearchFlagStatus( position );
        
        // Input Filters
        if ( position == 0 )
        {   // Hex
            search.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 64 ), filter } );
            replace.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 64 ), filter } );
        }
        else
        {   // Table / ASCII
            search.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 32 ) } );
            replace.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 32 ) } );
        }
        
        // Change the text accordingly
        replace.removeTextChangedListener( replaceWatcher );
        search.removeTextChangedListener( searchWatcher );
        search.setText( "" );
        replace.setText( "" );
        //
        if ( searchText[ position ] != null )
        {
            searchText[ position ] = searchForMatches( searchText[ position ], convertedText, position );
            search.append( searchText[ position ] );
        }
        //
        if ( replaceText[ position ] != null )
        {
            replaceText[ position ] = searchForMatches( replaceText[ position ], convertedReplace, position );
            replace.append( replaceText[ position ] );
        }
        //
        search.addTextChangedListener( searchWatcher );
        replace.addTextChangedListener( replaceWatcher );
    }
    
    @Override
    public void onNothingSelected( AdapterView<?> parent )
    {
    }
    
    
    /**
     * Enable pressing Back
     *
     * @param dialog   N/A
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
    
    
    @Override
    public void onResume()
    {
        super.onResume();
        
/*
        int width       = getResources().getDisplayMetrics().widthPixels;
        int displayMode = getResources().getConfiguration().orientation;
        
        if ( getDialog().getWindow() != null )
        {
            getDialog().getWindow().setLayout( width - (width / 6), ViewGroup.LayoutParams.WRAP_CONTENT );
        }
*/
        
        Dialog dialog = getDialog();
        
        if ( dialog != null && dialog.getWindow() != null )
        {
            int width  = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            
            dialog.getWindow().setLayout( width, height );
        }
        
        // Set input filters
        search.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 32 ) } );
        replace.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 32 ) } );
        
        if ( spinner.getSelectedItemPosition() == 0 )
        {   // View Hexadecimal
            search.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 64 ), filter } );
            replace.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 64 ), filter } );
        }
        
/*
        if ( displayMode == Configuration.ORIENTATION_LANDSCAPE )
        {
            getDialog().getWindow().setLayout( width - (width / 3), ViewGroup.LayoutParams.WRAP_CONTENT );
        }
*/
    }
    
    /**
     * View click for multiple views
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        int id = v.getId();
        
        switch ( id )
        {
            case R.id.replaceAll:
            case R.id.replaceFromStart:
            case R.id.maxReplaceList:
                try
                {
                    CheckBox box = ( CheckBox ) v;
                    String   str = box.getTag().toString();
                    
                    if ( box.isChecked() )
                    {
                        WindHexActivity.searchFlags |= Integer.parseInt( str );
                    }
                    else
                    {
                        WindHexActivity.searchFlags &= ~Integer.parseInt( str );
                    }
                }
                catch ( NullPointerException e )
                {
                    e.getMessage();
                }
                return;
            default:
                break;
        }
        
        // Close dialog "x" button
        if ( id == R.id.cancelReplace )
        {
            WindHexActivity.canBreakSearch = true;
            dismiss();
            return;
        }
        
        // Search for a string of text
        if ( id == R.id.replaceButton )
        {
            String hexSearch;
            String hexReplace;
            int    index = spinner.getSelectedItemPosition();
            
            if ( index == 0 )
            {   // Hexadecimal
                if ( (search.getText() != null && replace.getText() != null) && (search.length() != 0 && replace.length() != 0) )
                {
                    hexSearch = search.getText().toString();
                    if ( (hexSearch.length() & 1) != 0 )
                    {
                        Toast.makeText( getContext(), "[Search Text] Hex text missing a hexadecimal digit", Toast.LENGTH_SHORT ).show();
                        return;
                    }
                    //
                    hexReplace = replace.getText().toString();
                    if ( (hexReplace.length() & 1) != 0 )
                    {
                        Toast.makeText( getContext(), "[Replace Text] Hex text missing a hexadecimal digit", Toast.LENGTH_SHORT ).show();
                        return;
                    }
                }
                else
                {
                    Toast.makeText( getContext(), "Please fill in both boxes", Toast.LENGTH_SHORT ).show();
                    return;
                }
            }
            else if ( index == 2 )
            {   // Ascii Text data
                if ( search.length() != 0 && replace.length() != 0 )
                {
                    hexSearch = convertedText[ 2 ];
                    hexReplace = convertedReplace[ 2 ];
                }
                else
                {
                    Toast.makeText( getContext(), "Please fill in both boxes", Toast.LENGTH_SHORT ).show();
                    return;
                }
            }
            else
            {   // Table file data
                if ( search.length() != 0 && replace.length() != 0 )
                {
                    hexSearch = convertedText[ 1 ];
                    hexReplace = convertedReplace[ 1 ];
                }
                else
                {
                    Toast.makeText( getContext(), "Please fill in both boxes", Toast.LENGTH_SHORT ).show();
                    return;
                }
            }
            
            //--------------------------------------
            // Send the string
            // Convert the hex bytes to char array
            //--------------------------------------
            if ( (hexSearch != null && hexReplace != null) && (hexSearch.length() > 0 && hexReplace.length() > 0) )
            {
                // Make sure the replacement string is not longer
                // than the search string
                if ( hexSearch.length() < hexReplace.length() )
                {
                    Toast.makeText( getContext(), "Replacement data longer than search data", Toast.LENGTH_SHORT ).show();
                    return;
                }
                //
                //------------------------
                //
                byte[] searchChars  = new byte[ hexSearch.length() / 2 ];
                byte[] replaceChars = new byte[ hexReplace.length() / 2 ];
                String hex;
                int    value;
                
                // Convert the search string
                for ( int i = 0; i < searchChars.length; i++ )
                {
                    hex = hexSearch.substring( i + i, i + i + 2 );
                    value = Integer.valueOf( hex, 16 );
                    searchChars[ i ] = ( byte ) value;
                }
                // Convert the replace string
                for ( int i = 0; i < replaceChars.length; i++ )
                {
                    hex = hexReplace.substring( i + i, i + i + 2 );
                    value = Integer.valueOf( hex, 16 );
                    replaceChars[ i ] = ( byte ) value;
                }
                //
                WindHexActivity.canBreakSearch = false;
                getDialog().dismiss();
                onReplaceClick.getReplaceText( searchChars, replaceChars );
            }
            else
            {
                Toast.makeText( getContext(), "One or more strings empty", Toast.LENGTH_SHORT ).show();
                //
/*
                for ( int i = 0; i < 4; i++ )
                {
                    searchText[ i ] = "";
                    replaceText[ i ] = "";
                }
*/
            }
        }
    }
    
    /**
     * @param id N/A
     */
    private void setSearchFlagStatus( int id )
    {
        // Set checked flags for Radio buttons
        if ( id == 0 )
        {   // Hex
            WindHexActivity.searchFlags |= 1;
            WindHexActivity.searchFlags &= 0b01110001;
        }
        else if ( id == 1 )
        {   // Table
            WindHexActivity.searchFlags |= 2;
            WindHexActivity.searchFlags &= 0b01110010;
        }
        else if ( id == 2 )
        {   // ASCII
            WindHexActivity.searchFlags |= 4;
            WindHexActivity.searchFlags &= 0b01110100;
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
            onReplaceClick = ( OnReplaceClick ) getActivity();
        }
        catch ( ClassCastException e )
        {
            e.getMessage();
        }
    }
    
    
    /**
     * Search for table file matches
     *
     * @param str N/A
     *
     * @return N/A
     */
    private String searchForMatches( String str, String[] convertedText, int index )
    {
        StringBuilder newStr = new StringBuilder();
        String        chars;
        boolean       wasFound;
        EditText      temp   = new EditText( getContext() );
        
        temp.setText( "" );
        
        for ( int i = 0; i < str.length(); i++ )
        {
            chars = str.substring( i, i + 1 );
            wasFound = false;
            
            for ( int c = 0; c < 256; c++ )
            {
                if ( !global.oneCode[ c ].equals( "" ) && global.oneCode[ c ].equals( chars ) )
                {
                    temp.append( String.format( "%02X", c ) );
                    newStr.append( chars );
                    wasFound = true;
                    break;
                }
            }
            
            // Check dual bytes too if not found!
            if ( !wasFound )
            {
                for ( int c = 0; c < 65536; c++ )
                {
                    if ( !global.twoCode[ c ].equals( "" ) && global.twoCode[ c ].equals( chars ) )
                    {
                        temp.append( String.format( "%04X", c ) );
                        newStr.append( chars );
                        break;
                    }
                }
            }
        }
        
        //
        convertedText[ index ] = temp.getText().toString();
        return newStr.toString();
    }
    
    /**
     * Seperate the text watcher code from init block
     */
    private void setupTextatchers()
    {
        searchWatcher = new TextWatcher()
        {
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
                String str   = s.toString();
                int    index = spinner.getSelectedItemPosition();
                
                if ( index == 0 )
                {   // Hexadecimal data. Raw data
                    searchText[ 0 ] = str.toUpperCase();
                    search.removeTextChangedListener( searchWatcher );
                    //search.setText( "" );
                    if ( search.getText() != null )
                    {
                        search.getText().clear();
                    }
                    search.append( searchText[ 0 ] );
                    search.addTextChangedListener( searchWatcher );
                }
                else if ( index == 2 )
                {   // ASCII standard text
                    StringBuilder buf = new StringBuilder();
                    searchText[ 2 ] = str;
                    //
                    for ( int c = 0; c < str.length(); c++ )
                    {
                        buf.append( String.format( "%02X", ( int ) str.charAt( c ) ) );
                    }
                    //
                    convertedText[ 2 ] = buf.toString();
                    
                    search.removeTextChangedListener( searchWatcher );
                    search.getText().clear();
                    search.append( searchText[ 2 ] );
                    search.addTextChangedListener( searchWatcher );
                }
                else
                {   // Table file text
                    searchText[ 1 ] = searchForMatches( str, convertedText, 1 );
                    //
                    search.removeTextChangedListener( searchWatcher );
                    search.getText().clear();
                    search.append( searchText[ 1 ] );
                    search.addTextChangedListener( searchWatcher );
                }
            }
        };
        
        replaceWatcher = new TextWatcher()
        {
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
                String str   = s.toString();
                int    index = spinner.getSelectedItemPosition();
                
                if ( index == 0 )
                {   // Hexadecimal data. Raw data
                    replaceText[ 0 ] = str.toUpperCase();
                    replace.removeTextChangedListener( replaceWatcher );
                    //replace.setText( "" );
                    if ( replace.getText() != null )
                    {
                        replace.getText().clear();
                    }
                    replace.append( replaceText[ 0 ] );
                    replace.addTextChangedListener( replaceWatcher );
                }
                else if ( index == 2 )
                {   // ASCII standard text
                    StringBuilder buf = new StringBuilder();
                    replaceText[ 2 ] = str;
                    //
                    for ( int c = 0; c < str.length(); c++ )
                    {
                        buf.append( String.format( "%02X", ( int ) str.charAt( c ) ) );
                    }
                    //
                    convertedReplace[ 2 ] = buf.toString();
                    
                    replace.removeTextChangedListener( replaceWatcher );
                    replace.getText().clear();
                    replace.append( replaceText[ 2 ] );
                    replace.addTextChangedListener( replaceWatcher );
                }
                else
                {   // Table file text
                    replaceText[ 1 ] = searchForMatches( str, convertedReplace, 1 );
                    //
                    replace.removeTextChangedListener( replaceWatcher );
                    replace.getText().clear();
                    replace.append( replaceText[ 1 ] );
                    replace.addTextChangedListener( replaceWatcher );
                }
            }
        };
    }
}
