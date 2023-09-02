package com.GenesysEast.windhex.Dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
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
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.PaintCompat;
import androidx.fragment.app.DialogFragment;

import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.R;
import com.GenesysEast.windhex.WindHexActivity;
import com.GenesysEast.windhex.table_maker.UniCodeTable;


import java.util.ArrayList;
import java.util.Arrays;

public class SearchDialog
        extends DialogFragment
        implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, TextWatcher, View.OnKeyListener
{
    private       Globals global           = Globals.getInstance();
    public static int     LIST_FINDS       = 0x10;
    public static int     SEARCH_BEGINNING = 0x20;
    public static int     MAX_FINDS        = 0x40;
    
    private        OnSearchClick     onSearchClick;
    private static String[]          saveText        = { "", "", "", "" };
    private        String[]          convertedText   = new String[ 4 ];
    private        EditText          edit;
    public static  int               curUnicodeTable = -1;
    private        GridView          gridView;
    private        Spinner           spinType;
    private        UniCodeTable      uniTable;
    private        int               lastSpinPosi;
    private        GridAdapter       adapter;
    private        ArrayList<String> uniList         = new ArrayList<>();
    private        View              uniLayout;
    private        int               animWidth       = 0;
    
    /**
     * Interface method declarations
     */
    public interface OnSearchClick
    {
        void getFindText( byte[] findText );
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
    
    public SearchDialog()
    {
    }
    
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        View                       view;
        ArrayList<String>          spinList;
        Spinner                    spinView;
        ArrayAdapter<CharSequence> spinAdapter;
        ArrayAdapter<String>       typeAdapter;
        int                        index      = 0;
        int[]                      boxIDs     = { R.id.listResults, R.id.searchFromStart, R.id.maxFindList };
        String[]                   list       = { "Hexadecimal input", "Table file input", "ASCII input", "Unicode input" };
        CheckBox                   box;
        View                       find;
        ImageView                  exit;
        int[]                      BITS_SPIN  = { 1, 2, 4, 8 };
        int[]                      BITS_CHECK = { LIST_FINDS, SEARCH_BEGINNING, MAX_FINDS };
        
        // Get the view
        view = View.inflate( getContext(), R.layout.search_layout, null );
        
        //------------------------------
        //
        // Set up functions for Unicode
        // searching. .:sigh:.
        //
        //------------------------------
        uniTable = new UniCodeTable();
        uniTable.blockName = getResources().getStringArray( R.array.uniBlocks );
        uniTable.blockStart = getResources().getIntArray( R.array.blockStart );
        uniTable.blockEnd = getResources().getIntArray( R.array.blockEnd );
        
        if ( curUnicodeTable == -1 )
        {
            curUnicodeTable = Arrays.asList( uniTable.blockName ).indexOf( "Basic Latin" );
        }
        //
        lastSpinPosi = curUnicodeTable;
        //
        // GridView adapter
        //
        assert getContext() != null;
        /*        adapter = new ArrayAdapter<>( getContext(), R.layout.tabler_grid, uniList );*/
        adapter = new GridAdapter( uniList );
        
        gridView = view.findViewById( R.id.searchGrid );
        gridView.setAdapter( adapter );
        gridView.setOnItemClickListener( this );
        //
        buildCharList();
        
        //
        spinAdapter = ArrayAdapter.createFromResource( getContext(), R.array.uniBlocks, R.layout.tabler_spinner_item );
        spinAdapter.setDropDownViewResource( R.layout.tabler_dropdown_list );
        spinView = view.findViewById( R.id.searchSpinner );
        spinView.setAdapter( spinAdapter );
        spinView.setSelected( false );  // must
        spinView.setSelection( curUnicodeTable, true );  //must
        spinView.setOnItemSelectedListener( this );
        spinView.setPrompt( "Select Character Table" );
        
        
        //################################
        // Linear layout holding all the
        // unicode stuff
        //################################
        uniLayout = view.findViewById( R.id.uniLayout );
        uniLayout.setVisibility( View.GONE );
        
        
        // Set clickers for all
        for ( int c = 0; c < boxIDs.length; c++ )
        {
            box = view.findViewById( boxIDs[ c ] );
            box.setOnClickListener( this );
            box.setChecked( (WindHexActivity.searchFlags & BITS_CHECK[ c ]) != 0 );
        }
        
        //
        // Set the spinner text indexing list for search types
        // IE: Hex, Table, ASCII, Unicode
        //
        spinList = new ArrayList<>( Arrays.asList( list ) );
        
        // Setup the adapter
        typeAdapter = new ArrayAdapter<>( getContext(), R.layout.tabler_spinner_item, spinList );
        typeAdapter.setDropDownViewResource( R.layout.searcher_dropdown_list );
        
        // Add to spinner
        spinType = view.findViewById( R.id.replaceSpinner );
        spinType.setAdapter( typeAdapter );
        spinType.setOnItemSelectedListener( this );
        //
        for ( int c = 0; c < list.length; c++ )
        {
            if ( (WindHexActivity.searchFlags & BITS_SPIN[ c ]) != 0 )
            {
                spinType.setSelection( c );
                index = c;
                break;
            }
        }
        
        edit = view.findViewById( R.id.searchText );
        find = view.findViewById( R.id.findButton );
        exit = view.findViewById( R.id.cancelFind );
        
        //
        edit.setText( "" );
        edit.addTextChangedListener( this );
        if ( saveText[ index ] != null )
        {
            edit.append( saveText[ index ] );
            //saveText[ index ] = searchForMatches( saveText[ index ], index );
        }
        //
        //        edit.addTextChangedListener( this );
        //
        find.setOnClickListener( this );
        exit.setOnClickListener( this );
        view.setOnKeyListener( this );
        
        //
        if ( getDialog() != null && getDialog().getWindow() != null )
        {
            getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
            getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
        }
        
        //
        return view;
    }
    
    
    /**
     * Enable pressing Back
     *
     * @param view    N/A
     * @param keyCode N/A
     * @param event   N/A
     *
     * @return N/A
     */
    @Override
    public boolean onKey( View view, int keyCode, KeyEvent event )
    {
        if ( keyCode == KeyEvent.KEYCODE_BACK )
        {
            dismiss();
            return true;
        }
        //
        return false;
    }
    
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        Dialog dialog      = getDialog();
        int    width;
        int    height;
        int    displayMode = getResources().getConfiguration().orientation;
        int    index       = spinType.getSelectedItemPosition();
        
        
        //#############################
        //
        // Display new dialog layout
        //
        //#############################
        if ( dialog != null && dialog.getWindow() != null )
        {
            width = ViewGroup.LayoutParams.MATCH_PARENT;
            height = ViewGroup.LayoutParams.MATCH_PARENT;
            
            dialog.getWindow().setLayout( width, height );
        }
        
        
        // Set input filters
        edit.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 32 ) } );
        //
        if ( index == 0 )
        {   // Hex
            edit.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 64 ), filter } );
        }
        
        //
        //-----------------------------
        //
/*
        if ( index == 3 )
        {   // Unicode
            curUnicodeTable = index;
            uniLayout.setVisibility( View.VISIBLE );
            
            //
            uniList.clear();
            buildCharList();
            adapter.notifyDataSetChanged();
            lastSpinPosi = curUnicodeTable;
        }
        else
*/
        {
            // All others
            uniLayout.setVisibility( View.GONE );
        }
    }
    
    /**
     * View click for multiple views
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        int id    = v.getId();
        int index = spinType.getSelectedItemPosition();
        
        switch ( id )
        {
            case R.id.listResults:
            case R.id.searchFromStart:
            case R.id.maxFindList:
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
        if ( id == R.id.cancelFind )
        {
            WindHexActivity.canBreakSearch = true;
            dismiss();
            return;
        }
        
        
        // Search for a string of text
        if ( id == R.id.findButton )
        {
            String hexString;
            
            if ( index == 0 )
            {   // Hex
                if ( edit.getText() != null && edit.length() != 0 )
                {
                    hexString = edit.getText().toString();
                    if ( (hexString.length() & 1) != 0 )
                    {
                        Toast.makeText( getContext(), "Hex text missing a hexadecimal digit", Toast.LENGTH_SHORT ).show();
                        return;
                    }
                }
                else
                {
                    return;
                }
            }
            else if ( index == 3 )
            {   // Unicode data
                if ( edit.length() != 0 )
                {
                    hexString = convertedText[ 3 ];
                }
                else
                {
                    return;
                }
            }
            else if ( index == 2 )
            {   // Ascii Text data
                if ( edit.length() != 0 )
                {
                    hexString = convertedText[ 2 ];
                }
                else
                {
                    return;
                }
            }
            else
            {   // Text data
                if ( edit.length() != 0 )
                {
                    hexString = convertedText[ 1 ];
                }
                else
                {
                    return;
                }
            }
            
            //--------------------------------------
            // Send the string
            // Convert the hex bytes to char array
            //--------------------------------------
            if ( hexString != null && hexString.length() > 0 )
            {
                byte[] chars = new byte[ hexString.length() / 2 ];
                String hex;
                int    value;
                
                for ( int i = 0; i < chars.length; i++ )
                {
                    hex = hexString.substring( i + i, i + i + 2 );
                    value = Integer.valueOf( hex, 16 );
                    chars[ i ] = ( byte ) value;
                }
                
                WindHexActivity.canBreakSearch = false;
                getDialog().dismiss();
                onSearchClick.getFindText( chars );
            }
            else
            {
                Toast.makeText( getContext(), "Search string empty", Toast.LENGTH_SHORT ).show();
                //
                for ( int i = 0; i < 4; i++ )
                {
                    saveText[ i ] = "";
                }
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
        {   // Table file
            WindHexActivity.searchFlags |= 2;
            WindHexActivity.searchFlags &= 0b01110010;
        }
        else if ( id == 2 )
        {   // ASCII
            WindHexActivity.searchFlags |= 4;
            WindHexActivity.searchFlags &= 0b01110100;
        }
        else
        {   // Unicode
            WindHexActivity.searchFlags |= 8;
            WindHexActivity.searchFlags &= 0b01111000;
        }
    }
    
    
    /**
     * Item Click handler for the grid view
     *
     * @param parent   calling view for the function
     * @param view     object passed by the parent caller
     * @param position index or location within the adapter that was clicked
     * @param id       N/A
     */
    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id )
    {
        String char_item = uniList.get( position );
        edit.append( char_item );
    }
    
    
    /**
     * Item Selected handler for Spinner
     *
     * @param parent   calling view for the function
     * @param view     object passed by the parent caller
     * @param position index or location within the adapter that was clicked
     * @param id       N/A
     */
    @Override
    public void onItemSelected( AdapterView<?> parent, View view, int position, long id )
    {
        if ( parent.getId() == R.id.searchSpinner )
        {
            // Using the Unicode spinner
            if ( lastSpinPosi != position )
            {
                curUnicodeTable = position;
                //
                uniList.clear();
                buildCharList();
                adapter.notifyDataSetChanged();
                lastSpinPosi = curUnicodeTable;
            }
        }
        else
        {   // Using the data type spinner
            setSearchFlagStatus( position );
            
            try
            {
                // Input Filters
                edit.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 32 ) } );
                //
                if ( position == 0 )
                {   // Hex
                    edit.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( 64 ), filter } );
                }
                
                // Change the text accordingly
                edit.removeTextChangedListener( this );
                edit.setText( "" );
                if ( saveText[ position ] != null )
                {
                    edit.append( saveText[ position ] );
                    saveText[ position ] = searchForMatches( saveText[ position ], position );
                }
                //
                edit.addTextChangedListener( this );
                
                //
                // Can we see Unicode data?
                //
                if ( position == 3 )
                {   // Unicode viewing
                    uniLayout.setVisibility( (uniLayout.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE );
                }
                else
                {
                    //
                    uniLayout.setVisibility( View.GONE );
                }
            }
            catch ( NullPointerException e )
            {
                e.getMessage();
            }
        }
    }
    
    
    @Override
    public void onNothingSelected( AdapterView<?> parent )
    {
    
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
            onSearchClick = ( OnSearchClick ) getActivity();
        }
        catch ( ClassCastException e )
        {
            e.getMessage();
        }
    }
    
    
    /**
     * Text watchers
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
        String str   = s.toString();
        int    index = spinType.getSelectedItemPosition();
        
        if ( index == 0 )
        {   // Hex
            saveText[ 0 ] = str.toUpperCase();
            edit.removeTextChangedListener( this );
            //            edit.setText( "" );
            if ( edit.getText() != null )
            {
                edit.getText().clear();
            }
            edit.append( saveText[ 0 ] );
            edit.addTextChangedListener( this );
        }
        else if ( index == 3 )
        {   // Unicode
            saveText[ 3 ] = searchForMatches( str, 3 );
            //
            edit.removeTextChangedListener( this );
            if ( edit.getText() != null )
            {
                edit.getText().clear();
            }
            edit.append( saveText[ 3 ] );
            edit.addTextChangedListener( this );
        }
        else if ( index == 2 )
        {   // ASCII
            StringBuilder buf = new StringBuilder();
            saveText[ 2 ] = str;
            //
            for ( int c = 0; c < str.length(); c++ )
            {
                buf.append( String.format( "%02X", ( int ) str.charAt( c ) ) );
            }
            //
            convertedText[ 2 ] = buf.toString();
            
            edit.removeTextChangedListener( this );
            if ( edit.getText() != null )
            {
                edit.getText().clear();
            }
            edit.append( saveText[ 2 ] );
            edit.addTextChangedListener( this );
        }
        else
        {   // Table file
            saveText[ 1 ] = searchForMatches( str, 1 );
            //
            edit.removeTextChangedListener( this );
            if ( edit.getText() != null )
            {
                edit.getText().clear();
            }
            edit.append( saveText[ 1 ] );
            edit.addTextChangedListener( this );
        }
    }
    
    
    /**
     * Search for table file matches
     *
     * @param str N/A
     *
     * @return N/A
     */
    private String searchForMatches( String str, int index )
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
     * Build unicode list
     */
    private void buildCharList()
    {
        Paint paint = new Paint();
        
        for ( int i = uniTable.blockStart[ curUnicodeTable ]; i <= uniTable.blockEnd[ curUnicodeTable ]; i++ )
        {
            String charPair = String.copyValueOf( Character.toChars( i ) );
            
            if ( !Character.isISOControl( i ) && PaintCompat.hasGlyph( paint, charPair ) )
            {
                //                uniList.add(new String(charPair));
                uniList.add( charPair );
            }
        }
        
        if ( uniList.isEmpty() )
        {
            gridView.setBackgroundResource( R.drawable.table_not_supported );
        }
        else
        {
            gridView.setBackgroundResource( 0 );
        }
    }
    
    
    /**
     * //####################################
     * <p>
     * Main adpater for the grid display
     * <p>
     * //####################################
     */
    private class GridAdapter
            extends BaseAdapter
    {
        private ArrayList<String> list;
        
        
        public GridAdapter( ArrayList<String> list )
        {
            this.list = list;
        }
        
        public void setList( ArrayList<String> list )
        {
            this.list = list;
        }
        
        
        @Override
        public int getCount()
        {
            if ( list != null )
            {
                return list.size();
            }
            else
            {
                return 0;
            }
        }
        
        @Override
        public Object getItem( int position )
        {
            return list.get( position );
        }
        
        @Override
        public long getItemId( int position )
        {
            return position;
        }
        
        @Override
        public View getView( int position, View convertView, ViewGroup parent )
        {
            int    width = gridView.getColumnWidth();
            View   view  = null;
            String text;
            
            if ( getContext() != null )
            {
                view = View.inflate( getContext(), R.layout.grid_item, null );
                
                text = list.get( position );
                TextView tv = view.findViewById( R.id.gridText );
                
//                tv.setLayoutParams( new AbsListView.LayoutParams( width, width ) );
                tv.setLayoutParams( new FrameLayout.LayoutParams( width, width ) );
                tv.setText( text );
                
            }
            
            return view;
        }
    }
}

