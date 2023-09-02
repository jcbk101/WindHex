package com.GenesysEast.windhex.table_maker;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.PaintCompat;

import com.GenesysEast.windhex.Dialogs.ErrorDialog;
import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.GlobalClasses.Tabler;
import com.GenesysEast.windhex.GlobalClasses.ThemeControl;
import com.GenesysEast.windhex.R;
import com.GenesysEast.windhex.helper.helper_activity;
import com.GenesysEast.windhex.home_screen.HomeActivity;
import com.GenesysEast.windhex.settings_menu.MenuData.MenuData;
import com.GenesysEast.windhex.table_maker.Adapters.TableLister;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;


public class table_activity
        extends AppCompatActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, View.OnClickListener, ErrorDialog.OnErrorListener

{
    private       Globals                    global          = Globals.getInstance();
    private       UniCodeTable               uniTable;
    private       ArrayList<String>          uniList         = new ArrayList<>();
    private       ArrayList<Tabler>          tblList         = new ArrayList<>();
    private       GridAdapter                gridAdapter;
    private       TableLister                listAdapter;
    private       ArrayAdapter<CharSequence> spinAdapter;
    private       EditText                   editText;
    private       ListView                   listView;
    private       GridView                   gridView;
    private       Spinner                    spinView;
    //
    private       AdView                     adView;
    public static int                        curUnicodeTable = -1;
    private       int                        lastSpinPosi;
    public static int                        isEditMode      = -1;
    private       Stack<Integer>             oldTable        = new Stack<>();
    private       int                        stackLog        = 0;
    private       int                        tblType;
    public        String                     text;
    public        byte[]                     str;
    private       File                       tempFile;
    private       boolean                    fileWasSaved    = false;
    private       boolean                    fileChanged     = false;
    private       View                       view_main;
    private       int[]                      sliders         = { R.id.editButton, R.id.eraseButton };
    
    
    /**
     * Activity Creator / Initializer
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        // MUST BE SET BEFORE setContentView
        ThemeControl.onActivityCreateSetTheme( this, global.currentTheme );
        //
        setContentView( R.layout.tabler_main );
        
        //
        // Determine if UniTable data file exist
        // load data based off existing file details
        //
        //        getSupportedTables();
        
        editText = findViewById( R.id.tableInput );
        //
        editText.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
            {
                if ( actionId == EditorInfo.IME_ACTION_DONE )
                {
                    insertTblValue();
                    return true;
                }
                return false;
            }
        } );
        
        Toolbar tb = findViewById( R.id.tableBar );
        setSupportActionBar( tb );
        if ( getSupportActionBar() != null )
        {
            getSupportActionBar().setDisplayShowHomeEnabled( true );
        }
        
        //
        Objects.requireNonNull( getSupportActionBar() ).setTitle( "" );
        tb.setKeepScreenOn( (global.settingsFlag & MenuData.SCREEN_STATUS) != 0 );
        
        uniTable = new UniCodeTable();
        uniTable.blockName = getResources().getStringArray( R.array.uniBlocks );
        uniTable.blockStart = getResources().getIntArray( R.array.blockStart );
        uniTable.blockEnd = getResources().getIntArray( R.array.blockEnd );
        
        
        //############################
        //
        // Default character table
        //
        //############################
        if ( curUnicodeTable == -1 )
        {
            curUnicodeTable = Arrays.asList( uniTable.blockName ).indexOf( "Basic Latin" );
        }
        //
        lastSpinPosi = curUnicodeTable;
        //
        // GridView adapter
        //
        // R.layout.tabler_grid
        gridAdapter = new GridAdapter( uniList );
        gridView = findViewById( R.id.tableGrid );
        gridView.setAdapter( gridAdapter );
        gridView.setOnItemClickListener( this );
        //
        buildCharList( true );
        
        
        //#############################
        //
        // Spinner adapter
        //
        //#############################
        spinAdapter = ArrayAdapter.createFromResource( this, R.array.uniBlocks, R.layout.tabler_spinner_item );
        spinAdapter.setDropDownViewResource( R.layout.tabler_dropdown_list );
        spinView = findViewById( R.id.blockSpinner );
        spinView.setAdapter( spinAdapter );
        spinView.setSelected( false );  // must
        spinView.setSelection( curUnicodeTable, true );  //must
        spinView.setOnItemSelectedListener( this );
        spinView.setPrompt( "Select Character Table" );
        spinView.setOnFocusChangeListener( new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange( View v, boolean hasFocus )
            {
                if ( hasFocus )
                {
                    gridAdapter.notifyDataSetChanged();
                }
            }
        } );
        
        Intent romIntent = getIntent();
        
        if ( romIntent.hasExtra( "Title" ) )
        {
            if ( romIntent.hasExtra( "Title" ) )
            {
                tblType = romIntent.getIntExtra( "Title", 0 );
                tb.setSubtitle( (tblType == 0 ? "Create New File" : "Edit Existing File") );
            }
        }
        
        
        //#############################
        //
        // ListView adapter
        //
        //#############################
        listView = findViewById( R.id.currentTable );
        // Pass parameters as references to use semi-globally
        listAdapter = new TableLister( this, tblList, listView );
        listAdapter.setIsEditMode( isEditMode );
        listAdapter.setEditText( editText );
        listView.setAdapter( listAdapter );
        listView.setOnItemClickListener( this );
        listView.setOnFocusChangeListener( new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange( View v, boolean hasFocus )
            {
                if ( !hasFocus )
                {
                    listAdapter.notifyDataSetChanged();
                }
            }
        } );
        
        
        //
        if ( tblType == 1 && global.tblFile[ global.currentTable ] != null && global.tblFile[ global.currentTable ].exists() )
        {
            // Existing file loaded
            //            loadTableFile( global.tblFile[ global.currentTable ].toString(), false );
            // Transfer current entries over to the adapter
            transferFromTable();
            tempFile = global.tblFile[ global.currentTable ];
            listAdapter.notifyDataSetChanged();
            //
            if ( getSupportActionBar() != null )
            {
                getSupportActionBar().setTitle( tempFile.getName() );
            }
        }
        
        
        //############################
        //
        // Banner Ad support
        //
        //###########################
        view_main = this.findViewById( android.R.id.content );
        //view_main.setVisibility( View.INVISIBLE );
        // Main content view for EVERYTHING in this activity
        
        
        //#################################
        //
        // Handle Ads
        //
        //#################################
        AdView adView;
        
        if ( Globals.inDebug )
        {
            adView = findViewById( R.id.tablerAdViewDebug );
            //            adView.setVisibility( View.VISIBLE );
        }
        else
        {
            adView = findViewById( R.id.tablerAdView );
        }
        
        //
        adView.setVisibility( View.VISIBLE );
        
        AdRequest adRequest = new AdRequest.Builder().build();
        PointF    xy;
        Bundle    extras    = new Bundle();
        
        //
        extras.putString( "max_ad_content_rating", "G" );
        adRequest = new AdRequest.Builder().addNetworkExtrasBundle( AdMobAdapter.class, extras ).build();
        adView.loadAd( adRequest );
        
        xy = HomeActivity.getAdBannerSize( this );
        adView.getLayoutParams().height = ( int ) xy.y;
    }
    
    
    /**
     * Get Intent result
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult( int requestCode, int resultCode, @Nullable Intent data )
    {
        super.onActivityResult( requestCode, resultCode, data );
        
        if ( requestCode == 3 )
        {   // Load Table file from file manager
            if ( data != null && data.hasExtra( "FileName" ) )
            {
                tempFile = new File( data.getStringExtra( "FileName" ) );
                loadTableFile( tempFile.toString(), false );
                transferToTable();
                global.tblFile[ global.currentTable ] = tempFile;
                Toast.makeText( this, "Table #" + global.currentTable + " is now" + tempFile, Toast.LENGTH_SHORT ).show();
                //
                if ( getSupportActionBar() != null )
                {
                    getSupportActionBar().setTitle( tempFile.getName() );
                }
            }
        }
        else if ( requestCode == 4 || requestCode == 5 )
        {   // Save New Table file named in file manager
            if ( data != null && data.hasExtra( "FileName" ) )
            {
                //                global.tblFile[ global.currentTable ] = new File( data.getStringExtra( "FileName" ) );
                tempFile = new File( data.getStringExtra( "FileName" ) );
                saveTableFile( tempFile.toString(), true, false );
                fileWasSaved = true;
                fileChanged = false;
                //
                if ( getSupportActionBar() != null )
                {
                    getSupportActionBar().setTitle( tempFile.getName() );
                }
                //
                if ( requestCode == 5 )
                {
                    finish();
                }
            }
        }
        
    }
    
    
    /**
     * Return a color based on string length
     *
     * @param s
     *
     * @return
     */
    private int setTextColor( String s )
    {
        if ( s.length() == 2 )
        {
            return 0xFF4682B4;
        }
        else if ( s.length() > 2 )
        {
            return Color.YELLOW;
        }
        else
        {
            return Color.WHITE;
        }
    }
    
    
    /**
     * Transfer table when complete
     */
    public void transferToTable()
    {
        for ( int c = 0; c < 0x100; c++ )
        {
            global.oneCode[ c ] = "";
        }
        
        for ( int c = 0; c < 0x10000; c++ )
        {
            global.twoCode[ c ] = "";
        }
        
        global.jList.clear();
        //
        try
        {
            for ( Tabler item : tblList )
            {   // load table data to usable variables
                text = item.getLabel();
                str = item.getLabel().getBytes();
                
                if ( str.length > 2 && str[ 2 ] == '=' )
                {
                    int index = Integer.parseInt( text.substring( 0, 2 ), 16 );
                    global.oneCode[ index ] = text.substring( 3 );
                    global.oneColor[ index ] = setTextColor( global.oneCode[ index ] );
                }
                else if ( str.length > 4 && str[ 4 ] == '=' )
                {
                    int index = Integer.parseInt( text.substring( 0, 4 ), 16 );
                    global.twoCode[ index ] = text.substring( 5 );
                    global.twoColor[ index ] = 0xFFFF8C00;
                }
                else if ( str.length > 0 && str[ 0 ] == '(' && text.contains( "h)" ) )
                {
                    String label  = text.substring( text.indexOf( "h)" ) + 2 );
                    String num    = text.substring( text.indexOf( "(" ) + 1, text.indexOf( "h)" ) );
                    int    offset = Integer.parseInt( num, 16 );
                    //
                    global.jList.add( new Tabler( label, offset ) );
                }
            }
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
    }
    
    /**
     * Transfer table when complete
     */
    public void transferFromTable()
    {
        transferFromTable( false );
    }
    
    
    public void transferFromTable( boolean forceTransfer )
    {
        // Only if we don't want to FORCE a
        // directory loaded table
        if ( global.tblFile[ global.currentTable ].exists() && !forceTransfer )
        {
            loadTableFile( global.tblFile[ global.currentTable ].toString(), false );
            
            return;
        }
        else
        {
            //###########################
            //
            // Forcing a transfer
            //
            //###########################
            try
            {
                tblList.clear();
                //
                for ( int c = 0; c < 256; c++ )
                {   // load table data to usable variables
                    if ( global.oneCode[ c ] != "" )
                    {
                        tblList.add( new Tabler( String.format( "%02X=", c ) + global.oneCode[ c ], 0, false ) );
                    }
                    
                }
                
                for ( int c = 0; c < 0x10000; c++ )
                {   // load table data to usable variables
                    if ( global.twoCode[ c ] != "" )
                    {
                        tblList.add( new Tabler( String.format( "%04X=", c ) + global.twoCode[ c ], 0, false ) );
                    }
                    
                }
                
                //            tblList.addAll( global.jList );
                for ( Tabler i : global.jList )
                {
                    tblList.add( new Tabler( String.format( "(%Xh)%s", i.getOffset(), i.getLabel() ), 0, false ) );
                }
            }
            catch ( Throwable t )
            {
                t.printStackTrace();
            }
        }
    }
    
    
    /**
     * Simply used to clear TEMP table files from cache
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        try
        {
            File dir = getCacheDir();
            
            if ( dir != null && dir.isDirectory() )
            {
                deleteDir( dir, ".tbl" );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Delete cache directory structure on exit
     * for anything with ".tbl" extension
     *
     * @param dir         The file ( path ) to remove
     * @param extToDelete If extension given, only remove them
     *
     * @return
     */
    public static boolean deleteDir( File dir, String extToDelete )
    {
        if ( dir != null && dir.isDirectory() )
        {
            String[] children = dir.list();
            for ( int i = 0; i < children.length; i++ )
            {
                deleteDir( new File( dir, children[ i ] ), extToDelete );
/*
                if ( !success )
                {
                    return false;
                }
*/
            }
        }
        else if ( dir != null && dir.isFile() )
        {
            if ( dir.toString().toUpperCase().contains( extToDelete.toUpperCase() ) )
            {
                return dir.delete();
            }
        }
        
        return false;
    }
    
    
    /**
     * Instance Save / restore
     *
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState( Bundle savedInstanceState )
    {
        super.onRestoreInstanceState( savedInstanceState );
        tblList.clear();
        try
        {
            ArrayList<Tabler> temp = savedInstanceState.getParcelableArrayList( "tblList" );
            if ( temp != null )
            {
                tblList.addAll( temp );
            }
        }
        catch ( NullPointerException e )
        {
            Toast.makeText( this, "Could not restore table file", Toast.LENGTH_LONG ).show();
        }
        
        uniList.clear();
        try
        {
            ArrayList<String> temp = savedInstanceState.getStringArrayList( "uniList" );
            //
            if ( temp != null )
            {
                uniList.addAll( temp );
            }
        }
        catch ( NullPointerException e )
        {
            buildCharList( true );
        }
        
        curUnicodeTable = savedInstanceState.getInt( "curUnicodeTable" );
        lastSpinPosi = savedInstanceState.getInt( "lastSpinPosi" );
        isEditMode = savedInstanceState.getInt( "isEditMode" );
        //
        listAdapter.setEditColor( savedInstanceState.getInt( "editColor" ) );
        listAdapter.setIsEditMode( isEditMode );
        
        if ( isEditMode != -1 )
        {
            editText.setTextColor( Color.RED );
            editText.setTypeface( Typeface.DEFAULT, Typeface.ITALIC );
            //
            listView.setTranscriptMode( AbsListView.TRANSCRIPT_MODE_DISABLED );
            listView.setEnabled( false );
        }
    }
    
    /**
     * Method to preserve activity data upon change
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState( outState );
        outState.putSerializable( "tblList", tblList );
        outState.putStringArrayList( "uniList", uniList );
        outState.putInt( "curUnicodeTable", curUnicodeTable );
        outState.putInt( "lastSpinPosi", lastSpinPosi );
        outState.putInt( "isEditMode", isEditMode );
        outState.putInt( "editColor", listAdapter.getEditColor() );
    }
    
    
    /**
     * Back press handler
     */
    @Override
    public void onBackPressed()
    {
        if ( listAdapter.getIsEditMode() != -1 )
        {
            // If user is currently editing a
            // value, cancel editing
            editText.setTextColor( listAdapter.getEditColor() );
            editText.setTypeface( Typeface.DEFAULT, Typeface.NORMAL );
            if ( editText.getTag() != null )
            {
                String ret = ( String ) editText.getTag();
                editText.setText( "" );
                editText.append( ret );
            }
            
            //
            listView.setEnabled( true );
            listAdapter.setIsEditMode( isEditMode = -1 );
            listAdapter.notifyDataSetChanged();
        }
        else
        {
            // If any EDIT | DELETE boxes
            // open, close them all first
            boolean canLeave = true;
            
            for ( Tabler t : tblList )
            {
                if ( t.isFlag() )
                {
                    t.setFlag( false );
                    canLeave = false;
                }
            }
            
            //
            if ( !canLeave )
            {
                listAdapter.notifyDataSetChanged();
                return;
            }
            
            //###################################################
            //
            // Table data exist: Type = Edit current table mode
            // Leave no messages. Transfer ONLY if the file
            // was previously saved and modified
            //
            //###################################################
            if ( tblType == 1 )
            {
                if ( fileWasSaved )
                {
                    transferToTable();
                    Toast.makeText( this, "Table data updated", Toast.LENGTH_SHORT ).show();
                }
                else if ( fileChanged )
                {   // File changed, but not saved
                    ErrorDialog dialog = new ErrorDialog();
                    
                    dialog.setTitle( "Table File Maker" );
                    dialog.setMessage( "Existing table data not saved. Save these changes?" );
                    dialog.setButtons( ErrorDialog.NO_BUTTON | ErrorDialog.YES_BUTTON );
                    dialog.setYesText( "Yes" ).setNoText( "No" );
                    dialog.setOnErrorListener( this );
                    dialog.show( getSupportFragmentManager(), null );
                    
                    return;
                }
            }
            else
            {
                if ( fileChanged && tempFile == null && tblList.size() > 0 )
                {
                    // New file, save it using the file selector!
                    // Mode: New file
                    ErrorDialog dialog = new ErrorDialog();
                    
                    dialog.setTitle( "Table File Maker" );
                    dialog.setMessage( "New table data not saved. Save these changes?" );
                    dialog.setButtons( ErrorDialog.NO_BUTTON | ErrorDialog.YES_BUTTON );
                    dialog.setYesText( "Yes" ).setNoText( "No" );
                    dialog.setOnErrorListener( this );
                    dialog.show( getSupportFragmentManager(), null );
                    
                    return;
                }
            }
            //
            finish();
        }
    }
    
    /**
     * Options menu data
     *
     * @param menu
     *
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        Toolbar tb          = findViewById( R.id.tableBar );
        Menu    optionsMenu = tb.getMenu();
        //
        
        try
        {
            if ( tb != null && tb.getSubtitle() != null && tb.getSubtitle().toString().toUpperCase().contains( "EDIT" ) )
            {
                getMenuInflater().inflate( R.menu.tabler_menu_edit, optionsMenu );
            }
            else
            {
                getMenuInflater().inflate( R.menu.tabler_menu_new, optionsMenu );
            }
            
            TypedValue a;
            Drawable   drawable;
            int        color;
            
            // Get current Theme's textPrimaryColor
            a = new TypedValue();
            getTheme().resolveAttribute( R.attr.myIconColor, a, true );
            color = ContextCompat.getColor( this, a.resourceId );
            
            for ( int i = 0; i < menu.size(); i++ )
            {
                drawable = menu.getItem( i ).getIcon();
                if ( drawable != null )
                {
                    drawable.mutate();
                    drawable.setColorFilter( color, PorterDuff.Mode.SRC_ATOP );
                }
            }
            
            //
            updateUndoButton();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        
        return true;
    }
    
    
    /**
     * @param item Item clicked / selected from options menu
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        int id = item.getItemId();
        
        //###############################
        //
        // Main menu bar
        //
        //###############################
        if ( id == android.R.id.home )
        {
            if ( listAdapter.getIsEditMode() != -1 )
            {
                editText.setTextColor( listAdapter.getEditColor() );
                editText.setTypeface( Typeface.DEFAULT, Typeface.NORMAL );
                listView.setEnabled( true );
                listAdapter.setIsEditMode( isEditMode = -1 );
                listAdapter.notifyDataSetChanged();
            }
            //
            finish();
            return true;
        }
        
/*
        //-------------------------------
        //
        // Options Menu ( Top / Bottom )
        //
        //-------------------------------
        if ( id == R.id.btnOpen || id == R.id.btnSave || id == R.id.optOpen || id == R.id.optSave )
        {
            if ( id == R.id.btnOpen || id == R.id.optOpen )
            {
                Intent actIntent = new Intent( this, file_activity.class );
                actIntent.putExtra( "SubTitle", "Load table file..." );
                startActivityForResult( actIntent, 3 );
            }
            else
            {
                // Save the table file
                //                if ( global.tblFile[ global.currentTable ] == null )
                if ( tempFile == null )
                {   // If no file exist, create a new file
                    Intent actIntent = new Intent( this, file_activity.class );
                    actIntent.putExtra( "SubTitle", "Save table file as..." );
                    startActivityForResult( actIntent, 4 );
                    return true;
                }
                //
                //                return saveTableFile( global.tblFile[ global.currentTable ].toString(), true, false );
                return saveTableFile( tempFile.toString(), true, false );
            }
        }
        else if ( id == R.id.btnErase )
        {
            BaseInputConnection inputConnection = new BaseInputConnection( editText, true );
            inputConnection.sendKeyEvent( new KeyEvent( KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL ) );
            return true;
        }
        else if ( id == R.id.btnDone )
        {
            insertTblValue();
            return true;
        }
*/
        
        //#################################
        //
        // Options Menu
        //
        //#################################
        if ( id == R.id.optSort )
        {
            // Sort the items
            Collections.sort( tblList, new Comparator<Tabler>()
            {
                @Override
                public int compare( Tabler o1, Tabler o2 )
                {
                    return o1.getLabel().compareTo( o2.getLabel() );
                }
            } );
            //
            // Data sorted and cleared
            //
            listAdapter.notifyDataSetChanged();
            Toast.makeText( this, "Table file sorted!", Toast.LENGTH_SHORT ).show();
            //
            fileWasSaved = false;
            
            return true;
        }
        else if ( id == R.id.optUndo )
        {
            // Save temp table file for 'UNDO'
            if ( !oldTable.isEmpty() )
            {
                stackLog = oldTable.pop();
                tblList.clear();
                loadTableFile( "__temptbl_" + stackLog + "_.tbl", true );
                listAdapter.notifyDataSetChanged();
                updateUndoButton();
                tempFile = null;
                //
                fileWasSaved = false;
            }
            
            return true;
        }
        else if ( id == R.id.optClear )
        {
            // Save temp table file for 'UNDO'
            if ( stackLog < 10 && saveTableFile( "__temptbl_" + stackLog + "_.tbl", false, true ) )
            {
                oldTable.push( stackLog );
                stackLog++;
                updateUndoButton();
                tempFile = null;
            }
            else
            {
                Toast.makeText( this, "Maximum undo count reached.\nCurrent table NOT cleared.", Toast.LENGTH_SHORT ).show();
                return false;
            }
            
            //
            //
            fileWasSaved = false;
            tblList.clear();
            listAdapter.notifyDataSetChanged();
            
            return true;
        }
        else if ( id == R.id.optConvert )
        {
            if ( global.tblFile[ global.currentTable ] != null )
            {
                loadTableFile( global.tblFile[ global.currentTable ].toString(), false, "Shift_Jis" );
            }
        }
        else if ( id == R.id.optDefault )
        {
            if ( !tblList.isEmpty() )
            {
                // save current table temporarily for restore
                // Save temp table file for 'UNDO'
                if ( stackLog < 10 && saveTableFile( "__temptbl_" + stackLog + "_.tbl", false, true ) )
                {
                    oldTable.push( stackLog );
                    stackLog++;
                    updateUndoButton();
                    tempFile = null;
                }
                else
                {
                    Toast.makeText( this, "Maximum undo count reached.\nTable not created", Toast.LENGTH_SHORT ).show();
                    return false;
                }
            }
            
            //#############################
            //
            //
            //#############################
            tblList.clear();
            
            // Create ASCII table
            for ( int c = 0x20; c < 0x7F; c++ )
            {
                tblList.add( new Tabler( String.format( "%02X=%c", c, c ), 0, false ) );
            }
            
            //
            listAdapter.notifyDataSetChanged();
            //
            fileWasSaved = false;
            fileChanged = true;
            
            return true;
        }
        else if ( id == R.id.optHelp )
        {
            Intent intent = new Intent( this, helper_activity.class );
            startActivity( intent );
        }
        
        return false;
    }
    
    /**
     * Extended OptionsMenu Onclick for Menu Button Bar
     *
     * @param v
     */
    @Override
    public void onClick( View v )
    {
        int id = v.getId();
        
        //-------------------------------
        //
        // Options Menu ( Top / Bottom )
        //
        //-------------------------------
        if ( id == R.id.btnOpen || id == R.id.btnSave || id == R.id.optOpen || id == R.id.optSave )
        {
            if ( id == R.id.btnOpen || id == R.id.optOpen )
            {
/*
                Intent actIntent = new Intent( this, file_activity.class );
                actIntent.putExtra( "SubTitle", "Load table file..." );
                startActivityForResult( actIntent, 3 );
*/
            }
            else
            {
                // Save the table file
                //                if ( global.tblFile[ global.currentTable ] == null )
                if ( tempFile == null )
                {   // If no file exist, create a new file
/*
                    Intent actIntent = new Intent( this, file_activity.class );
                    actIntent.putExtra( "SubTitle", "Save table file as..." );
                    startActivityForResult( actIntent, 4 );
*/
                    return;
                }
                //
                //                return saveTableFile( global.tblFile[ global.currentTable ].toString(), true, false );
                saveTableFile( tempFile.toString(), true, false );
            }
        }
        else if ( id == R.id.btnErase )
        {
            boolean   hasFocus  = editText.hasFocus();
            ImageView imageView = findViewById( R.id.btnErase );
            
            int cursorPosition = editText.getSelectionStart();
            if ( cursorPosition > 0 )
            {
                editText.setText( editText.getText().delete( cursorPosition - 1, cursorPosition ) );
                editText.setSelection( cursorPosition - 1 );
            }
            else
            {
                cursorPosition = editText.length();
                if ( cursorPosition > 0 )
                {
                    editText.setText( editText.getText().delete( cursorPosition - 1, cursorPosition ) );
                    editText.setSelection( cursorPosition - 1 );
                }
            }
            if ( !hasFocus )
            {
                imageView.requestFocus();
            }
        }
        else if ( id == R.id.btnDone )
        {
            insertTblValue();
        }
    }
    
    /**
     * Item Click handler for the grid view
     * Item Click handler for the list view
     *
     * @param parent   calling view for the function
     * @param view     object passed by the parent caller
     * @param position index or location within the adapter that was clicked
     * @param id
     */
    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id )
    {
        if ( parent == listView )
        {   // process list view clicks
            TextView tv     = view.findViewById( R.id.tableListItem );
            View     Layout = view.findViewById( R.id.tableListLayout );
            View     v      = view.findViewById( R.id.editButton );
            int[]    dur    = { 2, 1 };
            
            // Animate the movement
            if ( v.getVisibility() != View.VISIBLE )
            {
                // Display buttons
                for ( int i = 0; i < sliders.length; i++ )
                {
                    final View s = view.findViewById( sliders[ i ] );
                    
                    s.setTranslationX( s.getWidth() * dur[ i ] );
                    s.setVisibility( View.VISIBLE );
                    s.animate().translationX( 0 ).setDuration( TableLister.SLIDE_DURATION ).setInterpolator( new LinearInterpolator() );
                    s.animate().setStartDelay( dur[ i ] ).start();
                }
                //
                tblList.get( position ).setFlag( true );
            }
            else
            {
                // Hide buttons
                for ( int i = 0; i < sliders.length; i++ )
                {
                    final View s = view.findViewById( sliders[ i ] );
                    
                    s.setVisibility( View.VISIBLE );
                    s.animate().translationX( s.getWidth() * dur[ i ] ).setDuration( TableLister.SLIDE_DURATION );
                    s.animate().setInterpolator( new LinearInterpolator() );
                    s.animate().setStartDelay( dur[ i ] );
                    s.animate().withEndAction( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            s.setVisibility( View.INVISIBLE );
                        }
                    } ).start();
                }
                //                Layout.animate().setDuration( 200 ).translationX( moveValue ).setListener( null );
                tblList.get( position ).setFlag( false );
            }
        }
        else
        {   // Process grid view clicks
            String char_item = uniList.get( position );
            editText.append( char_item );
        }
    }
    
    
    /**
     * Item Selected handler for Spinner
     *
     * @param parent   calling view for the function
     * @param view     object passed by the parent caller
     * @param position index or location within the adapter that was clicked
     * @param id
     */
    @Override
    public void onItemSelected( AdapterView<?> parent, View view, int position, long id )
    {
        if ( lastSpinPosi != position )
        {
            curUnicodeTable = position;
            //
            uniList.clear();
            buildCharList( true );
            //            spinAdapter.setSelection(position);
            //
            gridAdapter.notifyDataSetChanged();
            lastSpinPosi = curUnicodeTable;
        }
    }
    
    @Override
    public void onNothingSelected( AdapterView<?> parent )
    {
    
    }
    
    
    /**
     * Response from Dialog click when file not saved
     *
     * @param dialog
     * @param buttonClicked
     */
    @Override
    public void onErrorExitClick( ErrorDialog dialog, int buttonClicked )
    {
        if ( buttonClicked == ErrorDialog.YES_BUTTON && tblType == 0 )
        {   // Save to a NEW file
/*
            Intent actIntent = new Intent( this, file_activity.class );
            actIntent.putExtra( "SubTitle", "Save table file as..." );
            startActivityForResult( actIntent, 5 );
*/
            return;
        }
        else if ( buttonClicked == ErrorDialog.YES_BUTTON && tblType == 1 )
        {   // Save to an existing file
            try
            {
                saveTableFile( global.tblFile[ global.currentTable ].toString(), true, false );
                transferToTable();
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
            }
        }
        
        //
        fileWasSaved = true;
        fileChanged = false;
        dialog.dismiss();
        finish();
    }
    
    
    /**
     * @param dialog
     * @param which
     */
/*
    @Override
    public void onClick( DialogInterface dialog, int which )
    {
        if ( which == DialogInterface.BUTTON_POSITIVE && tblType == 0 )
        {   // Save to a NEW file
            Intent actIntent = new Intent( this, file_activity.class );
            actIntent.putExtra( "SubTitle", "Save table file as..." );
            startActivityForResult( actIntent, 5 );
            return;
        }
        else if ( which == DialogInterface.BUTTON_POSITIVE && tblType == 1 )
        {   // Save to an existing file
            saveTableFile( global.tblFile[ global.currentTable ].toString(), true, false );
            transferToTable();
        }
        
        fileWasSaved = true;
        fileChanged = false;
        dialog.dismiss();
        finish();
    }
*/
    
    
    /**
     * Table file Handler
     *
     * @param
     */
    public void insertTblValue()
    {
        int    hexValue;
        int    hexLength, oldLength;
        int    andValue;
        String str, hexStr;
        
        if ( editText.length() < 1 )
        {
            return;
        }
        
        //
        str = editText.getText().toString();
        hexLength = oldLength = str.indexOf( "=" );
        if ( hexLength > -1 )
        {   // Good hex string
            hexStr = str.substring( 0, oldLength );
        }
        else
        {   // Bad hex string
            hexStr = "";
        }
        //
        // Make sure we have a number / hex value
        // this area for hexadecimal ONLY
        //
        if ( !hexStr.equals( "" ) && hexStr.matches( "\\p{XDigit}+" ) )
        {
            if ( (hexLength & 1) == 1 )
            {
                hexLength++;
            }
            //
            str = str.substring( 0, oldLength );
            
            hexValue = Integer.parseInt( str, 16 );
            hexStr = String.format( "%0" + hexLength + "X=", hexValue );
            str = editText.getText().toString().substring( oldLength + 1 );
            hexStr = hexStr + str;
            
            
            //###############################
            //
            // Is this entry being edited
            //
            //###############################
            if ( listAdapter.getIsEditMode() != -1 )
            {
                isEditMode = listAdapter.getIsEditMode();
                tblList.set( isEditMode, new Tabler( hexStr, false ) );
                editText.setTextColor( listAdapter.getEditColor() );
                editText.setTypeface( Typeface.DEFAULT, Typeface.NORMAL );
                //
                listView.setEnabled( true );
                listAdapter.setIsEditMode( isEditMode = -1 );
                listAdapter.notifyDataSetChanged();
                // Restore previous text
                editText.setText( ( String ) editText.getTag() );
                fileWasSaved = false;
                fileChanged = true;
                return;
            }
            else
            {   // New hexadecimal table entry
                listAdapter.setIsEditMode( -1 );
                tblList.add( new Tabler( hexStr, false ) );
                listAdapter.notifyDataSetChanged();
                int lastIndex = listView.getCount() - 1;
                listView.setSelection( lastIndex );
                isEditMode = listAdapter.getIsEditMode();
            }
            
            
            //###############################
            //
            // New edit text string
            //
            //###############################
            hexValue++;
            andValue = 0;
            for ( int i = 0; i < hexLength; i++ )
            {
                andValue <<= 4;
                andValue |= 0xF;
            }
            hexValue &= andValue;
            //
            editText.setText( "" );
            editText.append( String.format( "%0" + hexLength + "X=", hexValue ) );
            //
            fileWasSaved = false;
            fileChanged = true;
        }
        else if ( editText.length() > 0 )
        {
            // Just add whatever it is
            // Ex: (012345h)JumpLabel
            if ( listAdapter.getIsEditMode() != -1 )
            {
                isEditMode = listAdapter.getIsEditMode();
                tblList.set( isEditMode, new Tabler( editText.getText().toString(), false ) );
                editText.setTextColor( listAdapter.getEditColor() );
                editText.setTypeface( Typeface.DEFAULT, Typeface.NORMAL );
                //
                listView.setEnabled( true );
                listAdapter.setIsEditMode( isEditMode = -1 );
                listAdapter.notifyDataSetChanged();
                // Restore previous text
                editText.setText( ( String ) editText.getTag() );
            }
            else
            {
                tblList.add( new Tabler( editText.getText().toString(), false ) );
                listAdapter.notifyDataSetChanged();
                int lastIndex = listView.getCount() - 1;
                listView.setSelection( lastIndex );
            }
            //
            fileWasSaved = false;
            fileChanged = true;
        }
    }
    
    
    /**
     * Build unicode list
     *
     * @param
     */
    public int buildCharList( boolean showGrid )
    {
        Paint paint     = new Paint();
        int   listCount = 0;
        
        if ( showGrid )
        {
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
            
            return uniList.size();
        }
        else
        {
            for ( int i = uniTable.blockStart[ curUnicodeTable ]; i <= uniTable.blockEnd[ curUnicodeTable ]; i++ )
            {
                String charPair = String.copyValueOf( Character.toChars( i ) );
                
                if ( !Character.isISOControl( i ) && PaintCompat.hasGlyph( paint, charPair ) )
                {
                    listCount++;
                }
            }
            
            return listCount;
        }
    }
    
    
    /**
     * Text file writer
     *
     * @param fileName  file name to save table data under
     * @param showToast flag to determine if 'Toast' show e displayed
     */
    public boolean saveTableFile( String fileName, boolean showToast, boolean saveTemp )
    {
        //-----------------------------
        // Try and write to the text file
        //-----------------------------
        OutputStreamWriter out;
        File               cacheFile;
        
        try
        {
            if ( saveTemp )
            {   // In cache directory.
                cacheFile = new File( getCacheDir(), fileName );
                // Format for writing a file using a path
                out = new OutputStreamWriter( new FileOutputStream( cacheFile ) );
            }
            else
            {   // This writes data to a table file
                cacheFile = new File( fileName );
                out = new OutputStreamWriter( new FileOutputStream( cacheFile ) );
            }
            
            
            for ( int c = 0; c < tblList.size(); c++ )
            {
                out.write( tblList.get( c ).getLabel() + "\n" );
            }
            
            out.close();
            
            //---------------------------------
            // Only show when user clicks save
            //---------------------------------
            if ( showToast )
            {
                Toast.makeText( this, "'" + cacheFile + "' saved", Toast.LENGTH_LONG ).show();
            }
            
            //
            if ( tblType == 1 )
            {
                transferToTable();
            }
            //
            fileWasSaved = true;
            fileChanged = false;
            return true;
        }
        catch ( Throwable t )
        {
            Toast.makeText( this, "Exception: " + t.toString(), Toast.LENGTH_LONG ).show();
        }
        
        return false;
    }
    
    
    /**
     * Table file loader
     *
     * @param fileName contains string of file to load
     * @param readTemp flag determining if cache folder holds file
     */
    public boolean loadTableFile( String fileName, boolean readTemp )
    {
        try
        {
            FileInputStream inStream;
            
            if ( readTemp )
            {
                File cacheFile = new File( getCacheDir(), fileName );
                fileName = cacheFile.toString();
            }
            
            inStream = new FileInputStream( fileName );
            InputStreamReader strReader;
            BufferedReader    bufReader;
            
            //         if ( inStream != null )
            //{
            // Read in text data
            strReader = new InputStreamReader( inStream );
            bufReader = new BufferedReader( strReader );
            String str;
            
            tblList.clear();
            //
            while ( (str = bufReader.readLine()) != null )
            {
                tblList.add( new Tabler( str, 0, false ) );
            }
            
            inStream.close();
            //
            if ( listAdapter != null )
            {
                listAdapter.notifyDataSetChanged();
            }
            //
            fileChanged = false;
            fileWasSaved = false;
            
            return true;
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
    
    /**
     * Overloaded method: table loader that converts Shift-Jis tables
     * into UTF format for java use
     *
     * @param fileName
     * @param readTemp
     * @param charSet
     *
     * @return
     */
    public boolean loadTableFile( String fileName, boolean readTemp, String charSet )
    {
        try
        {
            FileInputStream   inStream;
            ArrayList<Tabler> tblConvert = new ArrayList<>();
            
            if ( readTemp )
            {
                File cacheFile = new File( getCacheDir(), fileName );
                fileName = cacheFile.toString();
            }
            
            inStream = new FileInputStream( fileName );
            InputStreamReader strReader;
            BufferedReader    bufReader;
            
            strReader = new InputStreamReader( inStream, Charset.forName( charSet ) );
            bufReader = new BufferedReader( strReader );
            String str;
            
            tblConvert.clear();
            //
            while ( (str = bufReader.readLine()) != null )
            {
                tblConvert.add( new Tabler( str, 0, false ) );
            }
            
            inStream.close();
            tblList.clear();
            tblList.addAll( tblConvert );
            
            if ( listAdapter != null )
            {
                listAdapter.notifyDataSetChanged();
            }
            //
            fileChanged = true;
            fileWasSaved = false;
            
            return true;
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * There are no parameters
     * Routine changes drawable color whenever
     * a table file can be restored by an 'Undo' action
     * ---------------------------------
     */
    private void updateUndoButton()
    {
        Menu       menu;
        TypedValue a;
        Drawable   drawable;
        int        color;
        MenuItem   item;
        
        // Get the menu for the Action bar
        Toolbar tb = findViewById( R.id.tableBar );
        menu = tb.getMenu();
        
        // Get current Theme's textPrimaryColor
        a = new TypedValue();
        getTheme().resolveAttribute( R.attr.myIconColor, a, true );
        color = ContextCompat.getColor( this, a.resourceId );
        
        for ( int i = 0; i < menu.size(); i++ )
        {
            item = menu.getItem( i );
            drawable = item.getIcon();
            if ( drawable != null && item.getItemId() == R.id.optUndo )
            {
                drawable.mutate();
                if ( stackLog > 0 )
                {
                    drawable.setColorFilter( color, PorterDuff.Mode.SRC_ATOP );
                    item.setEnabled( true );
                }
                else
                {
                    drawable.setColorFilter( Color.BLACK, PorterDuff.Mode.SRC_ATOP );
                    item.setEnabled( false );
                }
            }
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
            
            if ( convertView == null )
            {
                view = View.inflate( table_activity.this, R.layout.grid_item, null );
            }
            else
            {
                view = convertView;
            }
            
            //
            text = list.get( position );
            TextView tv = view.findViewById( R.id.gridText );
            
            //            tv.setLayoutParams( new AbsListView.LayoutParams( width, width ) );
            tv.setLayoutParams( new FrameLayout.LayoutParams( width, width ) );
            tv.setText( text );
            
            return view;
        }
    }
    
}

