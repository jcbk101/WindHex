package com.GenesysEast.windhex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.GenesysEast.windhex.Dialogs.*;
import com.GenesysEast.windhex.GlobalClasses.*;
import com.GenesysEast.windhex.Hex_Drawer.DrawHexView;
import com.GenesysEast.windhex.Hex_Drawer.DrawerList;
import com.GenesysEast.windhex.Hex_Drawer.SubClasses.BookmarkManager;
import com.GenesysEast.windhex.Keyboard.HexKeyboard;
import com.GenesysEast.windhex.file_compare.file_compare;
import com.GenesysEast.windhex.helper.helper_activity;
import com.GenesysEast.windhex.home_screen.HomeActivity;
import com.GenesysEast.windhex.settings_menu.MenuData.MenuData;
import com.GenesysEast.windhex.settings_menu.settings_activity;
import com.GenesysEast.windhex.table_maker.table_activity;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class WindHexActivity
        extends AppCompatActivity
        implements DrawerList.OnItemClicked, JumpDialog.OnInputListener, SearchDialog.OnSearchClick, EditDialog.OnEditClick, SelectDialog.OnSelectListener,
                   ReplaceDialog.OnReplaceClick, SeekBar.OnSeekBarChangeListener, RewardedVideoAdListener
{
    
    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled( true );
    }
    
    // Ad return codes
    public static        boolean rewardGiven    = false;
    public static final  int     AD_LOADED      = 0;
    public static final  int     AD_OPENED      = 1;
    public static final  int     AD_STARTED     = 2;
    public static final  int     AD_CLOSED      = 3;
    public static final  int     AD_REWARDED    = 4;
    public static final  int     AD_LEFT_APP    = 5;
    public static final  int     AD_FAILED_LOAD = 6;
    public static final  int     AD_COMPLETED   = 7;
    //
    private static final int     SAVE_FILE_AS   = 8;
    private static final int     COMPARE_FILE   = 7;
    private static final int     INSERT_TEXT2   = 6;
    private static final int     INSERT_TEXT    = 5;
    private static final int     INSERT_DATA    = 4;
    private static final int     LOAD_TABLE     = 3;
    private static final int     CHANGE_THEME   = 2;
    private static final int     LOAD_ROM       = 1;
    
    private static final int                             MAXIMUM_UNDO     = 300;
    //
    private              Globals                         global           = Globals.getInstance();
    private static       TextView                        badge;
    private              TextView                        tv;
    private              long                            backPressed;
    private              int                             currentTheme     = 0;
    private              Context                         context;
    private              ListView                        navList;
    private              ClipBoard                       clip             = new ClipBoard();
    private static       DrawerList                      navAdapter;
    public static        ArrayList<String>               recentFiles;
    public static        int                             searchFlags      = 1;
    public               RomFileAccess                   rom              = RomFileAccess.getInstance();
    public static        boolean                         canBreakSearch   = false;
    public static        byte[]                          saveSearch;
    private static       byte[]                          saveReplace;
    private              int                             saveSearchCount  = 0;
    private              int                             saveReplaceCount = 0;
    private              BMH                             globalSearcher;
    private              HexKeyboard                     hexKeys;
    public               String                          titleText;
    final private        Thread.UncaughtExceptionHandler oldEx            = Thread.getDefaultUncaughtExceptionHandler();
    private              boolean                         appIsCrashing    = false;
    private              DrawHexView                     hView;
    private              View                            view_main;
    private              SeekBar                         scrollBar;
    //
    public static        RewardedVideoAd                 mRewardedVideoAd;
    private              OnAdReturnListener              onAdReturnListener;
    public static        long                            bannerDisabled   = 0;
    public static        boolean                         bannerMessage    = false;
    private              AdView                          adView;
    
    public interface OnAdReturnListener
    {
        void onAdReturn( int status, int errorFlag );
    }
    
    /**
     * Must callback for simplified listening
     *
     * @param onAdReturnListener
     */
    public void setOnAdReturnListener( OnAdReturnListener onAdReturnListener )
    {
        this.onAdReturnListener = onAdReturnListener;
    }
    
    
    /**
     * If an exception happens and is uncaught
     * handle it here
     *
     * @param thread
     * @param ex
     */
    public void myUncaughtException( Thread thread, Throwable ex )
    {
        saveSettingsData( this );
        rom.fclose();
        
        // Copy old ROM data back.
        // If it was saved, then all is fine
        if ( global != null && global.fileName != null && rom != null && rom.romCopy != null )
        {
            if ( rom.romCopy.exists() )
            {
                try
                {
                    //                        global.fileName.delete();
                    rom.fileCopy( rom.romCopy, global.fileName );
                    //
                    rom.romCopy.delete();
                }
                catch ( Exception x )
                {
                    
                }
            }
        }
        
        // Close out the current activity
        //        global.currentTheme = currentTheme;
        appIsCrashing = true;
        //        Thread.setDefaultUncaughtExceptionHandler( oldEx );
        oldEx.uncaughtException( thread, ex );
    }
    
    
    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        try
        {
            super.onCreate( savedInstanceState );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            
            try
            {
                Toast.makeText( context, "Error loading App. Exiting", Toast.LENGTH_SHORT ).show();
            }
            catch ( Exception exc )
            {
                exc.printStackTrace();
            }
            finally
            {
                finish();
            }
        }
        
        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler( new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException( Thread thread, Throwable e )
            {
                myUncaughtException( thread, e );
            }
        } );
        
        
        // MUST BE SET BEFORE setContentView
        ThemeControl.onActivityCreateSetTheme( this, global.currentTheme );
        
        //        recentFiles.addAll( Arrays.asList( getResources().getStringArray( android.R.array.imProtocols ) ) );
        // Check for ROM
        Intent romIntent = getIntent();
        
        if ( romIntent.hasExtra( "romLoaded" ) )
        {
            if ( !global.romInUse )
            {
                romIntent.getStringExtra( "romLoaded" );
                global.romInUse = rom.loadRomFile( global.fileName, this, this );
            }
            
            // Place the rom in use
            //            global.romInUse = true;
            
            if ( global.romInUse )
            {
                // Make sure the file isn't
                // already listed
                for ( int c = 0; c < recentFiles.size(); c++ )
                {
                    if ( recentFiles.get( c ).toLowerCase().equals( "open_file_button" ) )
                    {
                        recentFiles.remove( c );
                        break;
                    }
                }
                //
                if ( !recentFiles.contains( global.fileName.toString() ) )
                {
                    recentFiles.add( global.fileName.toString() );
                }
            }
        }
        
        
        //###########################
        //
        // Do AFTER theme set
        //
        //###########################
        setContentView( R.layout.hex_activity_main );
        
        context = getApplicationContext();
        
        Toolbar toolbar = findViewById( R.id.appBar );
        setSupportActionBar( toolbar );
        //        getSupportActionBar().setDisplayShowTitleEnabled( false );
        getSupportActionBar().setTitle( "" );
        toolbar.setKeepScreenOn( (global.settingsFlag & MenuData.SCREEN_STATUS) != 0 );
        toolbar.setSubtitleTextColor( Color.WHITE );
        
        try
        {
            if ( global.fileName != null && global.fileName.getName() != null )
            {
                toolbar.setSubtitle( global.fileName.getName() );
            }
            else
            {
                Toast.makeText( context, "Invalid filename...", Toast.LENGTH_SHORT ).show();
            }
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        
        
        //###############################
        //
        // Drawer details
        //
        //###############################
        DrawerLayout          drawer = findViewById( R.id.drawerLayout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        
        drawer.addDrawerListener( toggle );
        toggle.syncState();
        drawer.setDrawerLockMode( DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.START );
        toolbar.setNavigationOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                DrawerLayout drawer = findViewById( R.id.drawerLayout );
                
                if ( toolbar.getTag() == null )
                {
                    drawer.setDrawerLockMode( DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.START );
                    drawer.openDrawer( Gravity.START );
                    toolbar.setTag( 1 );
                }
                else
                {
                    drawer.closeDrawer( Gravity.START );
                    drawer.setDrawerLockMode( DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.START );
                    toolbar.setTag( null );
                }
            }
        } );
        
        // Manually closing? We got that covered!
        drawer.addDrawerListener( new DrawerLayout.SimpleDrawerListener()
        {
            @Override
            public void onDrawerClosed( View drawerView )
            {
                
                drawer.setDrawerLockMode( DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.START );
                toolbar.setTag( null );
                
                super.onDrawerClosed( drawerView );
            }
        } );
        
        
        //###########################
        //
        // Setup the seekbar
        //
        //###########################
        scrollBar = findViewById( R.id.scrollBar );
        scrollBar.setOnSeekBarChangeListener( this );
        
        
        //###################################
        //
        // Main hex display layout
        //
        //###################################
        hView = findViewById( R.id.hexViewer );
        hView.setActivity( this );
        hView.setScrollBar( scrollBar );
        global.setScrollBar( scrollBar );
        global.hView = hView;
        
        // Make sure we init all needed variables
        if ( !global.isInitialized )
        {
            // Use an activity context to get the rewarded video instance.
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance( this );
            mRewardedVideoAd.setRewardedVideoAdListener( this );
            // Get an Ad loaded
            loadRewardedVideoAd();
            
            hView.initHexViewer( global );
        }
        
        
        //###################################
        //
        // Need to get the DEFAULT font size
        //
        //###################################
        //        global.hexInit( getResources().getDimensionPixelSize( R.dimen._16sdp ) );
        
        
        //global.scrollThumb.setOnTouchListener( new ThumbBar() );
        // Hide ? Show lower tools bar
        //        ActionMenuView tb = findViewById( R.id.bottom_toolbar );
        //        BottomNavigationView tb = findViewById( R.id.bottom_toolbar );
        //        tb.setVisibility( global.lowerBarVisible ? View.VISIBLE : View.GONE );
        
        navAdapter = new DrawerList( recentFiles, this );
        navList = findViewById( R.id.navListView );
        navList.setAdapter( navAdapter );
        
        
        //###################################
        //
        // Set up the lower bar buttons
        //
        //###################################
        displayBottomBar();
        ImageButton drop = findViewById( R.id.dropBarButton );
        drop.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                View frame = findViewById( R.id.editBarsFrame );
                
                frame.setTag( (frame.getTag() != null) ? null : 1 );
                frame.setVisibility( frame.getTag() != null ? View.GONE : View.VISIBLE );
                
                v.setRotation( frame.getTag() != null ? 180 : 0 );
            }
        } );
        
        
        currentTheme = global.currentTheme;
        
        // Table file badge
        TextView badger = findViewById( R.id.table_swap_badge );
        badger.setText( "" + (global.currentTable + 1) );
        
        
        //###################################
        //
        // Main content view for EVERYTHING
        // in this activity
        //
        //###################################
        view_main = this.findViewById( android.R.id.content );
        view_main.setVisibility( View.INVISIBLE );
        
        // Build Table view Hex digits list
        View v = findViewById( R.id.bottom_toolbar );
        //
        
        view_main.animate().withStartAction( new Runnable()
        {
            @Override
            public void run()
            {
                view_main.setVisibility( View.VISIBLE );
            }
        } );
        view_main.animate().alpha( 1f ).setDuration( 100 ).withEndAction( new Runnable()
        {
            @Override
            public void run()
            {
                hexKeys = new HexKeyboard( WindHexActivity.this, R.id.keyboardView, R.xml.keys_setup, v.getHeight(), v.getWidth() );
                hexKeys.sethView( global.hView );
            }
        } ).start();
        
        
        //#############################
        //
        // Set up for potential Video
        // and Banner Ads to be played
        //
        //#############################
        if ( Globals.inDebug )
        {
            adView = findViewById( R.id.hexAdViewDebug );
        }
        else
        {
            adView = findViewById( R.id.hexAdView );
        }
        
        //
        // Do we show the banner?
        //
        if ( bannerDisabled == 0 )
        {
            adView.setVisibility( View.VISIBLE );
            
            AdRequest adRequest;
            PointF    xy;
            Bundle    extras = new Bundle();
            
            //
            extras.putString( "max_ad_content_rating", "G" );
            adRequest = new AdRequest.Builder().addNetworkExtrasBundle( AdMobAdapter.class, extras ).build();
            adView.loadAd( adRequest );
            
            xy = HomeActivity.getAdBannerSize( this );
            adView.getLayoutParams().height = ( int ) xy.y;
            
            //
            //##############################
            //
            if ( !bannerMessage )
            {
                try
                {
                    AppRate.setAppSuggestion( this );
                    
                    ErrorDialog dialog = new ErrorDialog();
                    
                    dialog.setAlertIcon( R.drawable.ic_info );
                    dialog.setTitle( "Disable banner Ad" );
                    dialog.setMessage( R.string.banner_message_for_users );
                    dialog.setButtons( ErrorDialog.YES_BUTTON ).setYesText( "Done" );
                    dialog.show( getSupportFragmentManager(), null );
                }
                catch ( Exception ex )
                {
                    ex.printStackTrace();
                }
                
                // User seen message
                setBannerMessageStaus( this, bannerMessage = true );
            }
        }
        
        
        //#######################################
        //
        // Create a new undo list, "Open With"
        // may bypass this
        //
        //#######################################
        if ( rom.undoList == null )
        {
            rom.undoList = new ArrayList<>();
        }
        //
        rom.undoList.clear();
    }
    
    
    /**
     * @param outState
     */
    @Override
    protected void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState( outState );
        View lv = findViewById( R.id.searcherListLayout );
        
        int isVisible = lv.getVisibility();
        
        String s = "";
        if ( saveSearch != null )
        {
            s = new String( saveSearch );
        }
        //
        String r = "";
        if ( saveReplace != null )
        {
            r = new String( saveReplace );
        }
        //
        outState.putInt( "visible", isVisible );
        outState.putInt( "x_posi", ( int ) global.xyHex.x );
        outState.putInt( "y_posi", ( int ) global.xyHex.y );
        outState.putInt( "save_count", ( int ) saveSearchCount );
        outState.putInt( "replace_count", ( int ) saveReplaceCount );
        outState.putInt( "position", ( int ) global.getPosition() );
        outState.putString( "save_search", s );
        outState.putString( "save_replace", r );
        
        //
        try
        {
            outState.putParcelable( "Searcher", globalSearcher );
        }
        catch ( Exception ex )
        {
            Toast.makeText( context, "Search information could not be saved.", Toast.LENGTH_SHORT ).show();
            ex.printStackTrace();
        }
    }
    
    
    /**
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState( Bundle savedInstanceState )
    {
        super.onRestoreInstanceState( savedInstanceState );
        
        int    isVisible;
        String s;
        
        try
        {
            isVisible = savedInstanceState.getInt( "visible" );
            
            global.xyHex.x = savedInstanceState.getInt( "x_posi" );
            global.xyHex.y = savedInstanceState.getInt( "y_posi" );
            saveSearchCount = savedInstanceState.getInt( "save_count" );
            saveReplaceCount = savedInstanceState.getInt( "replace_count" );
            global.setPosition( savedInstanceState.getInt( "position" ) );
            s = savedInstanceState.getString( "save_search" );
            if ( s != null )
            {
                saveSearch = s.getBytes();
            }
            //
            s = savedInstanceState.getString( "save_replace" );
            if ( s != null )
            {
                saveReplace = s.getBytes();
            }
            
            globalSearcher = ( BMH ) savedInstanceState.getSerializable( "Searcher" );
            
            //
            if ( globalSearcher != null && globalSearcher.fList.size() != 0 )
            {
                View     lv = findViewById( R.id.searcherListLayout );
                TextView tv = findViewById( R.id.headerFind );
                
                globalSearcher.findListView = findViewById( R.id.searcherList );
                globalSearcher.findListView.setAdapter( globalSearcher.findAdapter );
                globalSearcher.findListView.setOnItemClickListener( new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick( AdapterView<?> parent, View view, int position, long id )
                    {
                        Tabler item       = globalSearcher.fList.get( position );
                        int    screenSize = global.screenSize;
                        
                        try
                        {
                            if ( screenSize <= 0 )
                            {
                                screenSize = (global.rowCount * global.columnCount);
                            }
                            
                            //
                            global.setPosition( item.getOffset() );
                            global.xyHex.x = global.getPosition();
                            global.xyHex.y = (global.xyHex.x + WindHexActivity.saveSearch.length) - 1;
                            global.setPosition( (item.getOffset() / screenSize) * screenSize );
                            global.hView.invalidate();
                            //
                            globalSearcher.setSelectItem( position );
                            globalSearcher.resetAdapter();
                        }
                        catch ( Exception ex )
                        {
                            Toast.makeText( WindHexActivity.this, "Screen resolution error...", Toast.LENGTH_SHORT ).show();
                            ex.printStackTrace();
                        }
                        
                    }
                } );
                
                // Show the list...again.
                tv.setText( getString( R.string.search_results, globalSearcher.fList.size() ) );
                globalSearcher.resetAdapter();
                
                //
                if ( isVisible == View.VISIBLE && lv.getVisibility() != View.VISIBLE )
                {
                    lv.setVisibility( View.VISIBLE );
                }
            }
            
            //
            global.hView.invalidate();
        }
        catch ( Exception e )
        {
            //            Toast.makeText( context, "Away too long. Reseting...", Toast.LENGTH_SHORT ).show();
            finish();
            startActivity( new Intent( this, getClass() ) );
        }
    }
    
    /**
     * Save the recent files in a text file
     * for accessing whenever app is loaded
     */
    @Override
    public void finish()
    {
        super.finish();
        //
        if ( global.currentTheme == currentTheme && !appIsCrashing )
        {
            saveSettingsData( this );
            rom.fclose();
            
            // Copy old ROM data back.
            // If it was saved, then all is fine
            if ( rom.romCopy != null && rom.romCopy.exists() )
            {
                try
                {
                    //                    global.fileName.delete();
                    rom.fileCopy( rom.romCopy, global.fileName );
                    rom.romCopy.delete();
                }
                catch ( Exception x )
                {
                
                }
            }
            //
            table_activity.deleteDir( getCacheDir(), ".tmp" );
            System.exit( 1 );
        }
    }
    
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
    
    /**
     * Process back button clicks
     */
    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer       = findViewById( R.id.drawerLayout );
        View         tb           = findViewById( R.id.bottom_toolbar );
        View         lv           = findViewById( R.id.searcherListLayout );
        View         normalLayout = findViewById( R.id.bottom_toolbar );
        
        
        if ( drawer.isDrawerOpen( GravityCompat.START ) )
        {
            drawer.closeDrawer( GravityCompat.START );
        }
        else if ( lv.getVisibility() == View.VISIBLE )
        {
            lv.setVisibility( View.GONE );
        }
        else if ( global.xyHex.x != -1 || normalLayout.getVisibility() != View.VISIBLE || saveReplace != null || saveSearch != null )
        {
            TextView tv = findViewById( R.id.byteCount );
            tv.setVisibility( View.GONE );
            
            global.xyHex.x = global.xyHex.y = -1;
            global.hView.invalidate();
            // Set Bottom Menu back to default
            //toggleToolBars(false );
            
            View editLayout = findViewById( R.id.bottom_editbar );
            View hexLayout  = findViewById( R.id.bottom_hexbar );
            View textLayout = findViewById( R.id.bottom_textbar );
            
            normalLayout.setVisibility( View.VISIBLE );
            editLayout.setVisibility( View.GONE );
            hexLayout.setVisibility( View.GONE );
            textLayout.setVisibility( View.GONE );
            
            //
            global.bottomBarMode = 0;
            saveSearch = null;
            saveReplace = null;
            saveSearchCount = 0;
            saveReplaceCount = 0;
            // Make search icon and search again icon
            swapSearchIcon();
            swapReplaceIcon();
        }
        else
        {
            if ( (backPressed + 2000) > System.currentTimeMillis() )
            {
                super.onBackPressed();
            }
            else
            {
                Toast.makeText( getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT ).show();
                swapSearchIcon();
            }
            
            //
            backPressed = System.currentTimeMillis();
        }
    }
    
    
    /**
     * Recent files listview handler
     *
     * @param position
     */
    @Override
    //    public void onItemClick( AdapterView<?> parent, View view, int position, long id )
    public void itemClicked( int position )
    {
        File item = new File( recentFiles.get( position ) );
        
        if ( item.exists() )
        {
/*
            global.fileName = item;
            
            if ( rom.loadRomFile( global.fileName, this, this ) )
            {
                // Close drawer door
                DrawerLayout drawer = findViewById( R.id.drawerLayout );
                if ( drawer.isDrawerOpen( GravityCompat.START ) )
                {
                    drawer.closeDrawer( GravityCompat.START );
                }
                
                //
                global.bottomBarMode = 0;
                global.setPosition( 0 );
                global.hView.invalidate();
                updateUndoButton( this, rom );
                swapReplaceIcon();
                swapSearchIcon();
                displayBottomBar();
                
                // Sort the files, remove the selected, and add
                // it to the top of the heap
                if ( recentFiles.size() > 1 )
                {
                    for ( int i = 0; i < recentFiles.size(); i++ )
                    {
                        if ( recentFiles.get( i ).equals( global.fileName.toString() ) )
                        {
                            recentFiles.remove( i );
                            recentFiles.add( 0, global.fileName.toString() );
                            break;
                        }
                    }
                }
                //
                Toolbar toolbar = findViewById( R.id.appBar );
                toolbar.setSubtitle( global.fileName.getName() );
            }
*/
            
            //
            navAdapter.notifyDataSetChanged();
            global.hView.updateLayout( false );
        }
        else
        {
            Toast.makeText( this, "'" + item + " does not exist...", Toast.LENGTH_SHORT ).show();
        }
    }
    
    
    /**
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button, so long
     * as you specify a parent activity in AndroidManifest.xml.
     *
     * @param item
     *
     * @return
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        int  id = item.getItemId();
        View tb = findViewById( R.id.bottom_toolbar );
        
        //
        if ( id == R.id.action_font )
        {
            // Need to get the DEFAULT font size
            global.fntHeight = getResources().getDimensionPixelSize( R.dimen._10sdp );
            //
            global.textPaint.setTextSize( global.fntHeight );
            global.paint.setTextSize( global.fntHeight );
            //
            global.chrWidth = ( int ) global.paint.measureText( "WW" );
            global.hView.requestLayout();
            global.hView.invalidate();
        }
        else if ( id == R.id.action_open_file )
        {
            loadFile( "*/*", LOAD_ROM, "Open file for editing" );
        }
        else if ( id == R.id.action_save )
        {
            ErrorDialog dialog = new ErrorDialog();
            dialog.setAlertIcon( R.drawable.ic_help );
            dialog.setTitle( "Save File" );
            dialog.setMessage( "Save current file: '" + global.fileName + "'?\n\nAny changes made can no longer be undone." );
            dialog.setButtons( ErrorDialog.NO_BUTTON | ErrorDialog.YES_BUTTON );
            dialog.setYesText( "Yes" ).setNoText( "No" );
            dialog.setOnErrorListener( new ErrorDialog.OnErrorListener()
            {
                @Override
                public void onErrorExitClick( ErrorDialog dialog, int buttonClicked )
                {
                    if ( buttonClicked == ErrorDialog.YES_BUTTON )
                    {
                        rom.saveRomFile();
                        Toast.makeText( WindHexActivity.this, "File saved successfully", Toast.LENGTH_SHORT ).show();
                        updateUndoButton( WindHexActivity.this, rom );
                    }
                    //
                    dialog.dismiss();
                }
            } );
            //
            dialog.show( getSupportFragmentManager(), null );
        }
        else if ( id == R.id.action_save_as )
        {
/*
            // Save the ROM file as new name and file
            Intent actIntent = new Intent( this, file_activity.class );
            actIntent.putExtra( "SubTitle", "Save file as..." );
            startActivityForResult( actIntent, SAVE_FILE_AS );
*/
        }
        else if ( id == R.id.action_settings )
        {
            startActivityForResult( new Intent( this, settings_activity.class ), 2 );
            return true;
        }
        else if ( id == R.id.reset_settings )
        {
            global.currentTheme = 0;
            currentTheme = 1;
            global.settingsFlag = 0;
            Toast.makeText( context, "Settings reset to default", Toast.LENGTH_SHORT ).show();
            //
            finish();
            startActivity( new Intent( this, this.getClass() ) );
            overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
            //
            return true;
        }
        else if ( id == R.id.action_open_table )
        {
            loadFile( "*/*.tbl", LOAD_TABLE, "Load a table file" );
        }
        else if ( id == R.id.action_save_table )
        {
            // Save the table file
            if ( global.tblFile[ global.currentTable ] != null )
            {   // If no file exist, create a new file
                table_activity act = new table_activity();
                
                // Assumes a table is loaded
                act.transferFromTable( true );
                act.saveTableFile( global.tblFile[ global.currentTable ].toString(), false, false );
                act.transferToTable();
                act = null;
                //
                Toast.makeText( context, "Table file was saved", Toast.LENGTH_SHORT ).show();
            }
            else
            {
                Toast.makeText( context, "No table loaded to save", Toast.LENGTH_SHORT ).show();
            }
            //
        }
        else if ( id == R.id.action_about )
        {
            AboutDialog dialog = new AboutDialog();
            dialog.show( getSupportFragmentManager(), "home_dialog" );
        }
        else if ( id == R.id.action_rominfo )
        {
            AboutDialog dialog = new AboutDialog( global.fileName );
            dialog.show( getSupportFragmentManager(), "home_dialog" );
        }
        else if ( id == R.id.action_exit )
        {   //
            ErrorDialog dialog = new ErrorDialog();
            dialog.setTitle( "WindHex Mobile" );
            dialog.setMessage( "Exit the program?" );
            dialog.setButtons( ErrorDialog.NO_BUTTON | ErrorDialog.YES_BUTTON );
            dialog.setYesText( "Yes" ).setNoText( "No" );
            dialog.setOnErrorListener( new ErrorDialog.OnErrorListener()
            {
                @Override
                public void onErrorExitClick( ErrorDialog dialog, int buttonClicked )
                {
                    if ( buttonClicked == ErrorDialog.YES_BUTTON )
                    {
                        moveTaskToBack( true );
                        finish();
                        System.exit( 0 );
                    }
                    //
                    dialog.dismiss();
                }
            } );
            //
            dialog.show( getSupportFragmentManager(), null );
            
            return true;
        }
        else if ( id == R.id.action_search )
        {
            if ( saveSearch != null && saveSearchCount > 0 )
            //            if ( global.xyHex.x != -1 && saveSearch != null )
            {
                if ( (searchFlags & SearchDialog.LIST_FINDS) != 0 && saveSearchCount > 0 )
                {
                    View lv = findViewById( R.id.searcherListLayout );
                    
                    if ( lv.getVisibility() != View.VISIBLE )
                    {
                        lv.setVisibility( View.VISIBLE );
                    }
                }
                else
                {
                    getFindText( saveSearch );
                }
                
                //
                swapSearchIcon();
            }
            else
            {
                SearchDialog dialog = new SearchDialog();
                dialog.setCancelable( false );
                dialog.show( getSupportFragmentManager(), "Searcher" );
            }
        }
        else if ( id == R.id.action_replace )
        {
            if ( saveSearch != null && saveReplace != null && saveReplaceCount > 0 )
            {
                if ( (searchFlags & ReplaceDialog.REPLACE_ALL) != 0 && saveReplaceCount > 0 )
                {
                    View lv = findViewById( R.id.searcherListLayout );
                    
                    if ( lv.getVisibility() != View.VISIBLE )
                    {
                        lv.setVisibility( View.VISIBLE );
                    }
                }
                else
                {
                    getReplaceText( saveSearch, saveReplace );
                    swapReplaceIcon();
                }
            }
            else
            {
                ReplaceDialog dialog = new ReplaceDialog();
                dialog.setCancelable( false );
                dialog.show( getSupportFragmentManager(), "Replacer" );
            }
        }
        else if ( id == R.id.undoHexChanges )
        {
            int            curIndex = rom.undoList.size();
            final PointXYZ undo;
            
            if ( curIndex > 0 )
            {
                undo = rom.undoList.get( curIndex - 1 );
                
                // Is the urrent undo an undo TEMP file?
                if ( undo.undoFile != null )
                {
/*
                    LoadingDialog     loader = new LoadingDialog( this, this );
                    final AlertDialog dialog;
                    dialog = loader.makeDialog( this, this, "Performing undo request..." );
*/
                    final LoadingDialogFrag dialog = new LoadingDialogFrag( "Performing undo request..." );
                    
                    dialog.setCancelable( false );
                    dialog.show( getSupportFragmentManager(), null );
                    
                    //
                    Thread thread = new Thread( new Runnable()
                    {
                        public void run()
                        {
                            rom.readUndoList( undo.undoFile );
                            //
                            runOnUiThread( new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if ( dialog.isShowing() )
                                    {
                                        dialog.dismiss();
                                    }
                                }
                            } );
                        }
                    } );
                    
                    //
                    thread.start();
                    rom.undoList.remove( curIndex - 1 );
                    global.hView.invalidate();
                }
                // Does the currently selected offset
                // match the undo offset?
                else if ( undo.x == global.xyHex.x && undo.y == global.xyHex.y )
                {
                    int count = undo.length;
                    //
                    rom.fileCopy( rom.wf, rom.rf, ( int ) undo.x, count );
                    rom.undoList.remove( curIndex - 1 );
                }
                else
                {
                    int screenSize = global.screenSize;
                    
                    if ( screenSize <= 0 )
                    {
                        screenSize = (global.rowCount * global.columnCount);
                    }
                    
                    try
                    {
                        global.xyHex = undo;
                        global.setPosition( (undo.x / screenSize) * screenSize );
                    }
                    catch ( Exception ex )
                    {
                        Toast.makeText( WindHexActivity.this, "Screen resolution error...", Toast.LENGTH_SHORT ).show();
                        ex.printStackTrace();
                    }
                }
                //
                global.hView.invalidate();
            }
            
            // Update the icon
            updateUndoButton( this, rom );
        }
        else if ( id == R.id.action_table )
        {
            global.tblFile[ global.currentTable ] = null;
            // Clear all data for current table
            global.buildAsciiTable();
            global.hView.invalidate();
        }
        else if ( id == R.id.action_help )
        {
            //Toast.makeText( context, "Help file needed", Toast.LENGTH_SHORT ).show();
            Intent intent = new Intent( this, helper_activity.class );
            startActivity( intent );
        }
        else if ( id == R.id.action_compare )
        {
/*
            Intent actIntent = new Intent( this, file_activity.class );
            actIntent.putExtra( "SubTitle", "Load file to compare" );
            startActivityForResult( actIntent, COMPARE_FILE );
*/
        }
        else if ( id == R.id.action_ad_remove )
        {
            final ErrorDialog dialog = new ErrorDialog();
            
            dialog.setAlertIcon( android.R.drawable.ic_dialog_alert );
            dialog.setTitle( "Remove Banner Ad" ).setMessage( "Would you like to watch a video and remove the banner Ad for three days?" );
            dialog.setYesText( "Sure!" ).setNoText( " No thanks " ).setCancelable( false );
            dialog.setButtons( ErrorDialog.NO_BUTTON | ErrorDialog.YES_BUTTON );
            dialog.setOnErrorListener( new ErrorDialog.OnErrorListener()
            {
                @Override
                public void onErrorExitClick( ErrorDialog d, int buttonClicked )
                {
                    // Show the video
                    if ( mRewardedVideoAd.isLoaded() )
                    {
                        if ( buttonClicked == ErrorDialog.YES_BUTTON )
                        {
                            dialog.dismiss();
                            
                            // Show the Video Ad
                            mRewardedVideoAd.show();
                        }
                        else
                        {
                            dialog.dismiss();
                        }
                    }
                    else
                    {
                        Toast.makeText( WindHexActivity.this, "Error loading video", Toast.LENGTH_SHORT ).show();
                        dialog.dismiss();
                    }
                }
            } );
            dialog.show( getSupportFragmentManager(), "Video" );
        }
        
        return true;
    }
    
    
    /**
     * @param menu
     *
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        TypedValue a;
        Drawable   drawable;
        int        color;
        
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.hex_main_menu, menu );
        
        
        //###################################
        // Get current Theme's
        // textPrimaryColor
        //###################################
        a = new TypedValue();
        getTheme().resolveAttribute( R.attr.myIconColor, a, true );
        color = ContextCompat.getColor( this, a.resourceId );
        for ( int i = 0; i < menu.size(); i++ )
        {
            drawable = menu.getItem( i ).getIcon();
            if ( drawable != null )
            {
                drawable.mutate();
                //                drawable.setColorFilter( getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
                
                drawable.setColorFilter( color, PorterDuff.Mode.SRC_ATOP );
            }
            
            //###################################
            // Check for the banner Disable
            // button
            //###################################
            if ( menu.getItem( i ).getItemId() == R.id.action_ad_remove )
            {
                if ( bannerDisabled == 0 )
                {
                    menu.getItem( i ).setVisible( true );
                }
                else
                {
                    menu.getItem( i ).setVisible( false );
                    FrameLayout cantainer = findViewById( R.id.adContainer );
                    cantainer.removeAllViews();
                }
            }
        }
        
        
        //#################################
        //
        // Custon Undo buttom layout
        //
        //#################################
        final MenuItem menuItem   = menu.findItem( R.id.undoHexChanges );
        View           actionView = MenuItemCompat.getActionView( menuItem );
        
        badge = ( TextView ) actionView.findViewById( R.id.undo_badge );
        actionView.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                onOptionsItemSelected( menuItem );
            }
        } );
        
        //
        swapSearchIcon();
        updateUndoButton( this, rom );
        
        return true;
    }
    
    
    /**
     * Overrides interface from Jump Dialog
     *
     * @param Offset
     */
    @Override
    public void getOffset( long Offset )
    {
        int screenSize = global.screenSize;
        
        if ( screenSize <= 0 )
        {
            screenSize = (global.rowCount * global.columnCount);
        }
        
        try
        {
            global.xyHex.x = Offset;
            global.xyHex.y = Offset;
            global.setPosition( (Offset / screenSize) * screenSize );
            global.hView.invalidate();
        }
        catch ( Exception ex )
        {
            Toast.makeText( WindHexActivity.this, "Screen resolution error...", Toast.LENGTH_SHORT ).show();
            ex.printStackTrace();
        }
    }
    
    
    /**
     * Overrides interface from Search Dialog
     *
     * @param findText
     */
    @Override
    public void getFindText( final byte[] findText )
    {
        try
        {
            if ( findText == null )
            {   // Error, should not be null
                return;
            }
            
            // Code block for custom Alert Dialog
/*
            LoadingDialog     loader = new LoadingDialog( this, this );
            final AlertDialog dialog;
            final long        startOffset;
            
            dialog = loader.makeDialog( this, this, "Searching..." );
*/
            final LoadingAlert dialog = new LoadingAlert( this, this, "Searching..." );
            final long         startOffset;
            
            
            //--------------------------
            //
            // Background thread
            //
            //--------------------------
            final BMH Searcher = new BMH( this, this );
            
            if ( (searchFlags & SearchDialog.SEARCH_BEGINNING) != 0 )
            {
                startOffset = 0;
            }
            else
            {
                startOffset = (global.xyHex.x != -1) ? (global.xyHex.x + 1) : global.getPosition();
            }
            
            //
            // Save the string for search again click
            saveSearch = findText;
            saveSearchCount = 0;
            dialog.setCancelable( false );
            dialog.show();
            
            //
            Thread thread = new Thread( new Runnable()
            {
                public void run()
                {
                    final long foundAt;
                    long       offset       = 0;
                    int        currentCount = 0;
                    int        maxListCount = ((searchFlags & SearchDialog.MAX_FINDS) != 0) ? 128 : 0;
                    
                    // Perform searching
                    if ( (searchFlags & SearchDialog.LIST_FINDS) != 0 )
                    {   // List those finds!
                        while ( offset < rom.fileSize )
                        {
                            offset = Searcher.findTheString( findText, ( int ) offset );
                            
                            if ( offset > -1 )
                            {
                                currentCount++;
                                Searcher.fList.add( new Tabler( String.format( "#" + currentCount + " Found at: %X", offset ), ( int ) offset ) );
                                
                                // Found all we need
                                if ( maxListCount != 0 && currentCount >= maxListCount )
                                {
                                    break;
                                }
                                //
                                offset++;
                            }
                            else
                            {
                                break;
                            }
                        }
                        
                        foundAt = -1;
                    }
                    else
                    {   // A single search will do
                        foundAt = Searcher.findTheString( findText, ( int ) startOffset );
                    }
                    
                    //---------------------------------
                    // Adjust offsets on Main UI thread
                    // since it owns all
                    // the instances of views
                    //---------------------------------
                    runOnUiThread( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if ( dialog.isShowing() )
                            {
                                dialog.dismiss();
                            }
                            
                            // Display what was found
                            if ( foundAt != -1 && (searchFlags & SearchDialog.LIST_FINDS) == 0 )
                            {   // We do not want a list of finds
                                global.xyHex.x = foundAt;
                                global.xyHex.y = (global.xyHex.x + findText.length) - 1;
                                //                          global.xyHex.x = foundAt;
                                //                            global.xyHex.y = (foundAt + findText.length) - 1;
                                
                                int screenSize = global.screenSize;
                                
                                if ( screenSize <= 0 )
                                {
                                    screenSize = (global.rowCount * global.columnCount);
                                }
                                
                                try
                                {
                                    global.setPosition( (foundAt / screenSize) * screenSize );
                                    globalSearcher = null;
                                    global.hView.invalidate();
                                    saveSearchCount = 1;
                                }
                                catch ( Exception ex )
                                {
                                    Toast.makeText( WindHexActivity.this, "Screen resolution error...", Toast.LENGTH_SHORT ).show();
                                    ex.printStackTrace();
                                }
                                
                            }
                            else if ( (searchFlags & SearchDialog.LIST_FINDS) != 0 && Searcher.fList.size() != 0 )
                            {   // We have finds, and we want a list
                                View     rel = findViewById( R.id.searcherListLayout );
                                TextView tv  = findViewById( R.id.headerFind );
                                
                                rel.setVisibility( View.VISIBLE );
                                tv.setText( WindHexActivity.this.getString( R.string.search_results, Searcher.fList.size() ) );
                                Searcher.resetAdapter();
                                //
                                saveSearchCount = Searcher.fList.size();
                                globalSearcher = Searcher;
                            }
                            else
                            {   // No luck! :(
                                saveSearchCount = 0;
                                globalSearcher = null;
                                Toast.makeText( WindHexActivity.this, "Nothing found", Toast.LENGTH_SHORT ).show();
                            }
                            
                            // Make search icon and search again icon
                            swapSearchIcon();
                        }
                    } );
                }
            } );
            
            //
            thread.start();
        }
        catch ( Exception e )
        {
            Toast.makeText( context, "Search reset...", Toast.LENGTH_SHORT ).show();
            saveSearch = null;
            global.xyHex = new PointXYZ( -1, -1, -1 );
        }
    }
    
    
    /**
     * Overrides interface from Replace Dialog
     *
     * @param searchText
     * @param replaceText
     */
    @Override
    public void getReplaceText( final byte[] searchText, final byte[] replaceText )
    {
        try
        {
            if ( searchText == null || replaceText == null )
            {   // Error, should not be null
                return;
            }
            
            // Code block for custom Alert Dialog
/*
            LoadingDialog     loader = new LoadingDialog( this, this );
            final AlertDialog dialog;
            final long        startOffset;
            
            dialog = loader.makeDialog( this, this, "Searching and replacing..." );
*/
            final LoadingDialogFrag dialog = new LoadingDialogFrag( "Searching and replacing..." );
            final long              startOffset;
            
            
            //--------------------------
            //
            // Background thread
            //
            //--------------------------
            final BMH Searcher = new BMH( this, this );
            
            if ( (searchFlags & ReplaceDialog.REPLACE_BEGINNING) != 0 )
            {
                startOffset = 0;
            }
            else
            {
                startOffset = (global.xyHex.x != -1) ? (global.xyHex.x + 1) : global.getPosition();
            }
            
            //
            // Save the string for search again click
            saveSearch = searchText;
            saveReplace = replaceText;
            saveReplaceCount = 0;
            dialog.setCancelable( false );
            dialog.show( getSupportFragmentManager(), null );
            
            //
            Thread thread = new Thread( new Runnable()
            {
                public void run()
                {
                    final long foundAt;
                    long       offset       = 0;
                    int        currentCount = 0;
                    int        maxListCount = ((searchFlags & ReplaceDialog.MAX_REPLACES) != 0) ? 128 : 0;
                    
                    // Perform searching
                    if ( (searchFlags & ReplaceDialog.REPLACE_ALL) != 0 )
                    {   // List those finds!
                        while ( offset < rom.fileSize )
                        {
                            offset = Searcher.findTheString( searchText, ( int ) offset );
                            
                            if ( offset > -1 )
                            {
                                currentCount++;
                                Searcher.fList.add( new Tabler( String.format( "#" + currentCount + " Replaced at: %X", offset ), ( int ) offset ) );
                                
                                // Found all we need
                                if ( maxListCount != 0 && currentCount >= maxListCount )
                                {
                                    break;
                                }
                                //
                                offset++;
                            }
                            else
                            {
                                break;
                            }
                        }
                        
                        foundAt = -1;
                    }
                    else
                    {   // A single search will do
                        foundAt = Searcher.findTheString( searchText, ( int ) startOffset );
                    }
                    
                    //---------------------------------
                    // Adjust offsets on Main UI thread
                    // since it owns all
                    // the instances of views
                    //---------------------------------
                    runOnUiThread( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            
                            // Display what was found
                            if ( foundAt != -1 && (searchFlags & ReplaceDialog.REPLACE_ALL) == 0 )
                            {   // We do not want a list of finds
                                global.xyHex.x = foundAt;
                                global.xyHex.y = (global.xyHex.x + searchText.length) - 1;
                                //                          global.xyHex.x = foundAt;
                                //                            global.xyHex.y = (foundAt + findText.length) - 1;
                                
                                int screenSize = global.screenSize;
                                
                                if ( screenSize <= 0 )
                                {
                                    screenSize = (global.rowCount * global.columnCount);
                                }
                                
                                try
                                {
                                    global.setPosition( (foundAt / screenSize) * screenSize );
                                    
                                    // Write the entry to the ROM file
                                    InsertData inserter = new InsertData( WindHexActivity.this );
                                    inserter.pasteUsingBytes( replaceText, global.xyHex.x );
                                    // Create an UNDO entry
                                    rom.undoList.add( new PointXYZ( global.xyHex.x, global.xyHex.y, global.getPosition(), replaceText.length ) );
                                    // Update undo icon
                                    updateUndoButton( WindHexActivity.this, rom );
                                    //                            global.Position = foundAt;
                                    globalSearcher = null;
                                    global.hView.invalidate();
                                    saveReplaceCount = 1;
                                }
                                catch ( Exception ex )
                                {
                                    Toast.makeText( WindHexActivity.this, "Screen resolution error...", Toast.LENGTH_SHORT ).show();
                                    ex.printStackTrace();
                                }
                            }
                            else if ( (searchFlags & ReplaceDialog.REPLACE_ALL) != 0 && Searcher.fList.size() != 0 )
                            {   // We have finds, and we want to know
                                // how many strings were replaced and where
                                View     rel = findViewById( R.id.searcherListLayout );
                                TextView tv  = findViewById( R.id.headerFind );
                                
                                rel.setVisibility( View.VISIBLE );
                                tv.setText( WindHexActivity.this.getString( R.string.replace_results, Searcher.fList.size() ) );
                                Searcher.resetAdapter();
                                //
                                saveReplaceCount = Searcher.fList.size();
                                globalSearcher = Searcher;
                                //
                                File temp = null;
                                try
                                {
                                    temp = File.createTempFile( "TempUndo", null, getCacheDir() );
                                    rom.undoList.add( new PointXYZ( temp ) );
                                    
                                    // Make all replacement changes
                                    // Write the entry to the ROM file
                                    InsertData inserter = new InsertData( WindHexActivity.this );
                                    // Loop and write the data
                                    long offset;
                                    for ( int c = 0; c < Searcher.fList.size(); c++ )
                                    {   // Write to each found location the new data
                                        offset = Searcher.fList.get( c ).getOffset();
                                        inserter.pasteUsingBytes( replaceText, offset );
                                    }
                                    
                                    // Write the undo data to the file
                                    // Starting with the text that was replaced
                                    // Pass the "inserter" instance to this specialty method
                                    rom.writeUndoList( Searcher.fList, searchText, temp );
                                    // Update undo icon
                                    updateUndoButton( WindHexActivity.this, rom );
                                }
                                catch ( IOException e )
                                {
                                    e.printStackTrace();
                                }
                            }
                            else
                            {   // No luck! :(
                                saveReplaceCount = 0;
                                globalSearcher = null;
                                Toast.makeText( WindHexActivity.this, "Nothing found to replace", Toast.LENGTH_SHORT ).show();
                            }
                            
                            // Make search icon and search again icon
                            swapReplaceIcon();
                            if ( dialog.isShowing() )
                            {
                                dialog.dismiss();
                            }
                        }
                    } );
                }
            } );
            
            //
            thread.start();
        }
        catch ( Exception e )
        {
            Toast.makeText( context, "Replace Text reset...", Toast.LENGTH_SHORT ).show();
            saveSearch = null;
            global.xyHex = new PointXYZ( -1, -1, -1 );
        }
    }
    
    
    /**
     * Overrides interface from Edit Dialog
     *
     * @param editText
     */
    @Override
    public void getEditText( final String editText, final boolean mode )
    {
/*
        final AlertDialog warning;
        LoadingDialog     loader   = new LoadingDialog( this, this );
        final InsertData  inserter = new InsertData( WindHexActivity.this );
        
        //
        warning = loader.makeDialog( this, this, "Searching and replacing..." );
        warning.setCancelable( false );
        warning.show();
*/
        final LoadingDialogFrag warning  = new LoadingDialogFrag( "Searching and replacing..." );
        final InsertData        inserter = new InsertData( WindHexActivity.this );
        
        warning.setCancelable( false );
        warning.show( getSupportFragmentManager(), null );
        
        
        // Run this stuff in the back ground
        Thread thread = new Thread( new Runnable()
        {
            public void run()
            {
                final int result = inserter.TextInsert( WindHexActivity.this, editText, global.xyHex.x, mode );
                
                // UI Thread
                runOnUiThread( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if ( warning.isShowing() )
                        {
                            warning.dismiss();
                        }
                        
                        if ( result != -1 )
                        {
                            rom.undoList.add( new PointXYZ( global.xyHex.x, global.xyHex.x + result, global.getPosition(), result ) );
                            Toast.makeText( context, "[Text Input] " + result + " byte(s) inserted", Toast.LENGTH_SHORT ).show();
                            global.hView.invalidate();
                            updateUndoButton( WindHexActivity.this, rom );
                        }
                    }
                } );
            }
        } );
        
        //
        thread.start();
    }
    
    
    /**
     * Let undo know it can be updated
     *
     * @param status
     */
    @Override
    public void getSelectedResult( boolean status )
    {
        if ( status )
        {
            updateUndoButton( this, rom );
        }
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
        
        if ( requestCode == LOAD_ROM && resultCode == RESULT_OK )
        {
            // Load a ROM file
            if ( data != null && data.hasExtra( "FileName" ) )
            {
//                loadRomHelper( data );
                global.fileName = new File( data.getStringExtra( "FileName" ) );
                rom.loadRomFile( global.fileName, this, this );
                
                //
                for ( int c = 0; c < recentFiles.size(); c++ )
                {
                    if ( recentFiles.contains( "open_file_button" ) )
                    {
                        recentFiles.remove( c );
                        break;
                    }
                }
                
                //
                // Make sure the file isn't
                // already listed
                if ( !recentFiles.contains( global.fileName.toString() ) )
                {
                    recentFiles.add( global.fileName.toString() );
                }
                
                //
                global.bottomBarMode = 0;
                navAdapter.notifyDataSetChanged();
                updateUndoButton( this, rom );
                swapReplaceIcon();
                swapSearchIcon();
                displayBottomBar();
                
                //
                Toolbar toolbar = findViewById( R.id.appBar );
                toolbar.setSubtitle( global.fileName.getName() );
            }
        }
        else if ( requestCode == CHANGE_THEME )
        {   // Change system settings
            if ( global.currentTheme != currentTheme )
            {
                finish();
                ThemeControl.changeToTheme( this, global.currentTheme );
                currentTheme = global.currentTheme;
            }
            else
            {
                Toolbar toolbar = findViewById( R.id.appBar );
                toolbar.setKeepScreenOn( (global.settingsFlag & MenuData.SCREEN_STATUS) != 0 );
            }
        }
        else if ( requestCode == LOAD_TABLE )
        {   // Load Table file
            table_activity act = new table_activity();
            
            if ( data != null && data.hasExtra( "FileName" ) )
            {
                global.tblFile[ global.currentTable ] = new File( data.getStringExtra( "FileName" ) );
                act.loadTableFile( global.tblFile[ global.currentTable ].toString(), false );
                act.transferToTable();
                act = null;
                //
                hView.invalidate();
            }
        }
        else if ( requestCode == INSERT_DATA )
        {   // Load pasting file
            if ( data != null && data.hasExtra( "FileName" ) )
            {
/*
                final File        paste    = new File( data.getStringExtra( "FileName" ) );
                final InsertData  inserter = new InsertData( this );
                final AlertDialog dialog;
                LoadingDialog     loader   = new LoadingDialog( this, this );
                
                //
                dialog = loader.makeDialog( this, this, "Inserting file data..." );
                dialog.setCancelable( false );
                dialog.show();
*/
                final File       paste    = new File( data.getStringExtra( "FileName" ) );
                final InsertData inserter = new InsertData( this );
                //                final LoadingDialogFrag dialog   = new LoadingDialogFrag( "Inserting file data..." );
                final LoadingAlert dialog  = new LoadingAlert( this, this, "Inserting file data..." );
                boolean            didFail = false;
                
                dialog.setCancelable( false );
                
                //##################################
                //
                // Causing a dialog crash on
                // some devices. Show Toast instead
                //
                //##################################
                try
                {
                    //                    dialog.show( getSupportFragmentManager(), null );
                    dialog.show();
                }
                catch ( Exception ex )
                {
                    didFail = true;
                    Toast.makeText( context, "Inserting file data...", Toast.LENGTH_SHORT ).show();
                    ex.printStackTrace();
                }
                
                
                // Run this stuff in the back ground
                boolean finalDidFail = didFail;
                Thread thread = new Thread( new Runnable()
                {
                    public void run()
                    {
                        final int result = inserter.DataFileInsert( WindHexActivity.this, paste, global.xyHex.x );
                        
                        // UI Thread
                        runOnUiThread( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if ( !finalDidFail && dialog.isShowing() )
                                {
                                    dialog.dismiss();
                                }
                                
                                if ( result > -1 )
                                {
                                    rom.undoList.add( new PointXYZ( global.xyHex.x, global.xyHex.x + result, global.getPosition(), result ) );
                                    Toast.makeText( context, "[Data File] " + result + " byte(s) inserted", Toast.LENGTH_SHORT ).show();
                                    global.hView.invalidate();
                                    updateUndoButton( WindHexActivity.this, rom );
                                }
                            }
                        } );
                    }
                } );
                
                //
                thread.start();
                
            }
        }
        else if ( requestCode == INSERT_TEXT || requestCode == INSERT_TEXT + 1 )
        {   // Load pasting file
            if ( data != null && data.hasExtra( "FileName" ) )
            {
/*
                final File        paste    = new File( data.getStringExtra( "FileName" ) );
                final InsertData  inserter = new InsertData( this );
                final AlertDialog dialog;
                final boolean     mode     = (requestCode > INSERT_TEXT);
                LoadingDialog     loader   = new LoadingDialog( this, this );
                
                //
                dialog = loader.makeDialog( this, this, "Inserting text..." );
                dialog.setCancelable( false );
                dialog.show();
*/
                final File         paste    = new File( data.getStringExtra( "FileName" ) );
                final InsertData   inserter = new InsertData( this );
                final boolean      mode     = (requestCode > INSERT_TEXT);
                final LoadingAlert dialog   = new LoadingAlert( this, this, "Inserting text..." );
                
                //
                dialog.setCancelable( false );
                dialog.show();
                
                // Run this stuff in the back ground
                Thread thread = new Thread( new Runnable()
                {
                    public void run()
                    {
                        final int result = inserter.TextFileInsert( WindHexActivity.this, paste, global.xyHex.x, mode );
                        
                        // UI Thread
                        runOnUiThread( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if ( dialog.isShowing() )
                                {
                                    dialog.dismiss();
                                }
                                
                                if ( result != -1 )
                                {
                                    rom.undoList.add( new PointXYZ( global.xyHex.x, global.xyHex.x + result, global.getPosition(), result ) );
                                    Toast.makeText( context, "[Text File] " + result + " byte(s) inserted", Toast.LENGTH_SHORT ).show();
                                    global.hView.invalidate();
                                    updateUndoButton( WindHexActivity.this, rom );
                                }
                            }
                        } );
                    }
                } );
                
                //
                thread.start();
            }
        }
        else if ( requestCode == SAVE_FILE_AS && resultCode == RESULT_OK )
        {
            if ( data != null && data.hasExtra( "FileName" ) )
            {
                File temp = new File( data.getStringExtra( "FileName" ) );
                
                // Save the file as new name
                rom.saveRomFileAs( temp );
                updateUndoButton( this, rom );
                
                // Reload the file as new name
                loadRomHelper( data );
                Toast.makeText( context, "File saved as: " + temp, Toast.LENGTH_SHORT ).show();
            }
        }
        
        //
        if ( requestCode == COMPARE_FILE && resultCode == RESULT_OK )
        {   // Load a ROM file
            if ( data != null && data.hasExtra( "FileName" ) )
            {
                Intent actIntent = new Intent( this, file_compare.class );
                actIntent.putExtra( "TestFile", data.getStringExtra( "FileName" ) );
                startActivity( actIntent );
            }
        }
        
        global.hView.updateLayout( false );
    }
    
    
    /**
     * //##################################
     * <p>
     * Since code is duplicated, create a
     * helper file
     * <p>
     * //###################################
     */
    public void loadRomHelper( Intent data )
    {
/*
        global.fileName = new File( data.getStringExtra( "FileName" ) );
        rom.loadRomFile( global.fileName, this, this );
        
        //
        for ( int c = 0; c < recentFiles.size(); c++ )
        {
            if ( recentFiles.contains( "open_file_button" ) )
            {
                recentFiles.remove( c );
                break;
            }
        }
        
        //
        // Make sure the file isn't
        // already listed
        if ( !recentFiles.contains( global.fileName.toString() ) )
        {
            recentFiles.add( global.fileName.toString() );
        }
        
        //
        global.bottomBarMode = 0;
        navAdapter.notifyDataSetChanged();
        updateUndoButton( this, rom );
        swapReplaceIcon();
        swapSearchIcon();
        displayBottomBar();
        
        //
        Toolbar toolbar = findViewById( R.id.appBar );
        toolbar.setSubtitle( global.fileName.getName() );
*/
    }
    
    
    /**
     * //#############################
     * <p>
     * Load a file to edit
     * <p>
     * //#############################
     */
    public void loadFile( String fileType, int requestCode, String tileMessage )
    {
        if ( permissionsCheck() )
        {
            Intent intent;
            
            intent = new Intent( Intent.ACTION_OPEN_DOCUMENT );
            intent.addCategory( Intent.CATEGORY_OPENABLE );
            intent.setType( fileType );
            
            startActivityForResult( Intent.createChooser( intent, tileMessage ), requestCode );
        }
    }
    
    
    /**
     * //#############################
     * <p>
     * Load a file to edit
     * <p>
     * //#############################
     */
    public void saveFile()
    {
        if ( permissionsCheck() )
        {
/*
            Intent intent;
            
            intent = new Intent( Intent.ACTION_OPEN_DOCUMENT );
            intent.addCategory( Intent.CATEGORY_OPENABLE );
            intent.setType( "image/*" );
            
            startActivityForResult( Intent.createChooser( intent, "Select Picture" ), 10 );
*/
        }
    }
    
    
    /**
     * //#############################
     * <p>
     * MUST have permission to access
     * such files
     * <p>
     * //#############################
     */
    public boolean permissionsCheck()
    {
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions( this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 10 );
        }
        else
        {
            return true;
        }
        
        return false;
    }
    
    
    /**
     * //##############################
     * <p>
     * Handle File access permission
     * results
     * <p>
     * //##############################
     *
     * @param requestCode  N/A
     * @param permissions  N/A
     * @param grantResults N/A
     */
    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults )
    {
        if ( requestCode == 10 )
        {
            if ( grantResults.length > 0 && grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED )
            {
                Intent intent;
                
                intent = new Intent( Intent.ACTION_OPEN_DOCUMENT );
                intent.addCategory( Intent.CATEGORY_OPENABLE );
                intent.setType( "*/*" );
                
                startActivityForResult( Intent.createChooser( intent, "Select A File" ), 10 );
            }
            else
            {
                Toast.makeText( this, "Permission DENIED", Toast.LENGTH_SHORT ).show();
            }
        }
    }
    
    
    /**
     * Helper method for multi use calls
     */
/*
    public void openRomFile( View view )
    {
        Intent actIntent = new Intent( this, home_activity.class );
        actIntent.putExtra( "SubTitle", "Load file to edit" );
        startActivityForResult( actIntent, 1 );
    }
*/
    
    
    /**
     * Handle clicks for custom menu
     *
     * @param v
     *
     * @return
     */
    public void bottomMenuHandler( View v )
    {
        int  id = v.getId();
        long Offset;
        
        // Wait until scrolling is done
        if ( global.isScrolling )
        {
            return;
        }
        
        //-----------------------------------
        //
        // All options for sub menu-default
        //
        //-----------------------------------
        if ( id == R.id.bottom_goto )
        {
            Bundle     bundle = new Bundle();
            JumpDialog jump   = new JumpDialog();
            //
            if ( global.jList != null )
            {
                bundle.putSerializable( "tale_jumps", global.jList );
            }
            
            jump.show( getSupportFragmentManager(), "Jumper" );
        }
        else if ( id == R.id.hex_new_table || id == R.id.hex_edit_table )
        {
            int    Title     = (id == R.id.hex_new_table ? 0 : 1);
            Intent actIntent = new Intent( this, table_activity.class );
            //
            actIntent.putExtra( "Title", Title );
            startActivity( actIntent );
        }
        else if ( id == R.id.hex_open_table )
        {
            loadFile( "*/*.tbl", LOAD_TABLE, "Load table file..." );
/*
            Intent actIntent = new Intent( this, file_activity.class );
            actIntent.putExtra( "SubTitle", "Load table file..." );
            startActivityForResult( actIntent, LOAD_TABLE );
*/
        }
        else if ( id == R.id.hex_table_swap )
        {
            global.currentTable ^= 1;
            TextView badger = findViewById( R.id.table_swap_badge );
            badger.setText( "" + (global.currentTable + 1) );
            
            //
            if ( global.tblFile[ global.currentTable ] != null && global.tblFile[ global.currentTable ].exists() )
            {
                table_activity act = new table_activity();
                act.loadTableFile( global.tblFile[ global.currentTable ].toString(), false );
                act.transferToTable();
                act = null;
                global.hView.invalidate();
            }
            else
            {
                global.buildAsciiTable();
                global.hView.invalidate();
                Toast.makeText( context, "Table #" + (global.currentTable + 1) + " not active. Using ASCII table data", Toast.LENGTH_SHORT ).show();
                //                global.currentTable ^= 1;
            }
        }
        
        
        //-----------------------------------
        //
        // All options for sub menu-editing
        //
        //-----------------------------------
        if ( id == R.id.bottom_select || id == R.id.bottom_fill )
        {
            SelectDialog select = new SelectDialog();
            select.show( getSupportFragmentManager(), (id == R.id.bottom_fill ? "Filler" : "Selector") );
            return;
        }
        else if ( id == R.id.bottom_paste )
        {
            final String[] list = {
                    //"Paste from buffer", "Paste from clipboard", "Select from clipboard list", "Paste from file"
                    "Paste from buffer", "Paste from file"
            };
            
            //
            ErrorDialog builder = new ErrorDialog();
            builder.setMessage( "Select Pasting function" ).setItems( list ).setTitle( "Paste Text" );
            builder.setOnErrorListener( new ErrorDialog.OnErrorListener()
            {
                @Override
                public void onErrorExitClick( ErrorDialog dialog, int which )
                {
                    if ( which == 0 )
                    {
                        if ( clip.getLength() > 0 )
                        {
                            if ( rom.dataWrite( clip.buffer, global.xyHex.x, clip.getLength() ) )
                            {
                                rom.undoList.add( new PointXYZ( global.xyHex.x, global.xyHex.x + clip.getLength(), global.getPosition(), clip.getLength() ) );
                                Toast.makeText( context, clip.getLength() + " byte(s) were replaced.", Toast.LENGTH_SHORT ).show();
                                //
                                global.hView.invalidate();
                                updateUndoButton( WindHexActivity.this, rom );
                            }
                            else
                            {
                                Toast.makeText( context, "Error: Data extends beyond 'End of File'", Toast.LENGTH_SHORT ).show();
                            }
                            //
                            dialog.dismiss();
                        }
                        else
                        {
                            Toast.makeText( context, "Copy buffer is empty", Toast.LENGTH_SHORT ).show();
                        }
                    }
                    else
                    {
                        loadFile( "*/*", INSERT_DATA, "Load file for pasting..." );
/*
                        Intent actIntent = new Intent( context, file_activity.class );
                        actIntent.putExtra( "SubTitle", "Load file for pasting..." );
                        startActivityForResult( actIntent, INSERT_DATA );
*/
                        dialog.dismiss();
                    }
                }
            } );
            //
            builder.show( getSupportFragmentManager(), null );
            
        }
        else if ( id == R.id.bottom_copy )
        {
            int bufferSize = ( int ) Math.abs( global.xyHex.y - global.xyHex.x ) + 1;
            
            if ( bufferSize > 0x10000 )
            {
                AlertDialog.Builder builder = new AlertDialog.Builder( this, R.style.Theme_Alert );
                builder.setTitle( "Error" ).setMessage( "Copy buffer size cannot exceed (0x10000) " + Integer.toString( 0x10000 ) + " bytes" ).show();
            }
            else
            {
                clip.setStart( global.xyHex.x );
                clip.buffer = new byte[ bufferSize ];
                
                // Copy data from the ROM file
                if ( rom.dataCopy( clip.buffer, clip.getStart(), clip.getLength() ) )
                {
                    Toast.makeText( context, clip.getLength() + " byte(s) copied", Toast.LENGTH_SHORT ).show();
                }
            }
        }
        else if ( id == R.id.bottom_bookmark )
        {
            if ( global.tblFile[ global.currentTable ] != null && global.tblFile[ global.currentTable ].exists() )
            {
                BookmarkManager booker = new BookmarkManager( global.xyHex.x );
                
                booker.show( getSupportFragmentManager(), "null" );
            }
            else
            {
                Toast.makeText( this, "There is no active table loaded...", Toast.LENGTH_SHORT ).show();
            }
        }
        
        
        //-----------------------------------
        //
        // All options for text input
        //
        //-----------------------------------
        if ( id == R.id.bottom_edit )
        {
            EditDialog editor = new EditDialog();
            editor.show( getSupportFragmentManager(), "EditText" );
            return;
        }
        else if ( id == R.id.bottom_paste_text )
        {
            final String[] list = {
                    //"Paste from buffer", "Paste from clipboard", "Select from clipboard list", "Paste from file"
                    "Paste from buffer", "Paste from clipboard", "Paste from clipboard ( No table file )", "Paste from file", "Paste from file ( No table file )"
            };
            
            ErrorDialog builder = new ErrorDialog();
            builder.setMessage( "Select an action" ).setItems( list ).setTitle( "Paste Text" );
            builder.setOnErrorListener( new ErrorDialog.OnErrorListener()
            {
                @Override
                public void onErrorExitClick( ErrorDialog dialog, int which )
                {
                    if ( which == 0 )
                    {
                        if ( clip.getLength() > 0 )
                        {
                            if ( rom.dataWrite( clip.buffer, global.xyHex.x, clip.getLength() ) )
                            {   // Add entry
                                rom.undoList.add( new PointXYZ( global.xyHex.x, global.xyHex.x + clip.getLength(), global.getPosition(), clip.getLength() ) );
                                Toast.makeText( context, clip.getLength() + " byte(s) were replaced.", Toast.LENGTH_SHORT ).show();
                                //
                                global.hView.invalidate();
                                updateUndoButton( WindHexActivity.this, rom );
                            }
                            else
                            {
                                Toast.makeText( context, "Error: Data extends beyond 'End of File'", Toast.LENGTH_SHORT ).show();
                            }
                            
                            //
                            dialog.dismiss();
                        }
                        else
                        {
                            Toast.makeText( context, "Copy buffer is empty", Toast.LENGTH_SHORT ).show();
                        }
                    }
                    else if ( which > 0 )
                    {   // Give warning that insert uses table file data
                        dialog.dismiss();
                        selectPasteType( which, list[ which ].toString() );
                    }
                }
            } );
            //
            builder.show( getSupportFragmentManager(), null );
        }
    }
    
    
    /**
     * Extension of paste request in Options Item Click
     *
     * @param which
     */
    private void selectPasteType( int which, String selectedText )
    {
        final int selection = which;
        
        if ( selection == 1 || selection == 3 )
        {   // Inderting using table, warn user
            ErrorDialog warning = new ErrorDialog();
            warning.setMessage( R.string.text_insert_warning );
            warning.setTitle( selectedText ).setAlertIcon( android.R.drawable.ic_dialog_alert );
            warning.setButtons( ErrorDialog.YES_BUTTON | ErrorDialog.NO_BUTTON );
            warning.setNoText( "Cancel" ).setYesText( "Okay" );
            warning.setOnErrorListener( new ErrorDialog.OnErrorListener()
            {
                @Override
                public void onErrorExitClick( ErrorDialog dialog, int buttonClicked )
                {
                    if ( buttonClicked == ErrorDialog.YES_BUTTON )
                    {
                        multiInsertSelection( selection, which );
                    }
                    
                    //
                    dialog.dismiss();
                }
            } );
            warning.show( getSupportFragmentManager(), null );
        }
        else
        {   // No warning Dialog is needed
            multiInsertSelection( selection, which );
        }
    }
    
    /**
     * Method to help split source code from within Dialog Clicker
     *
     * @param selection
     * @param which
     */
    private void multiInsertSelection( int selection, final int which )
    {
        if ( selection < 3 )
        {    // Insert text from the clipboard
            ClipboardDialog clipper = new ClipboardDialog();
/*
            final String      s        = clipper.getSingleClipObject( WindHexActivity.this );
            final AlertDialog warning;
            final InsertData  inserter = new InsertData( WindHexActivity.this );
            LoadingDialog     loader   = new LoadingDialog( this, this );
            //
            
            warning = loader.makeDialog( this, this, "Inserting text..." );
            warning.setCancelable( false );
            warning.show();
*/
            final String            s        = clipper.getSingleClipObject( WindHexActivity.this );
            final InsertData        inserter = new InsertData( WindHexActivity.this );
            final LoadingDialogFrag warning  = new LoadingDialogFrag( "Inserting text..." );
            //
            warning.setCancelable( false );
            warning.show( getSupportFragmentManager(), null );
            
            // Run this stuff in the back ground
            Thread thread = new Thread( new Runnable()
            {
                public void run()
                {
                    final int result = inserter.TextInsert( WindHexActivity.this, s, global.xyHex.x, (which == 1 ? false : true) );
                    // UI Thread
                    runOnUiThread( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if ( warning.isShowing() )
                            {
                                warning.dismiss();
                            }
                            if ( result != -1 )
                            {
                                rom.undoList.add( new PointXYZ( global.xyHex.x, global.xyHex.x + result, global.getPosition(), result ) );
                                Toast.makeText( context, "[Clipboard] " + result + " byte(s) inserted", Toast.LENGTH_SHORT ).show();
                                global.hView.invalidate();
                                updateUndoButton( WindHexActivity.this, rom );
                            }
                        }
                    } );
                }
            } );
            
            //
            thread.start();
        }
        else if ( selection > 2 )
        {
            // Insert text from a file
            loadFile( "*/*", INSERT_TEXT + (selection - 3), "Load file for pasting..." );
/*
            Intent actIntent = new Intent( context, file_activity.class );
            actIntent.putExtra( "SubTitle", "Load file for pasting..." );
            startActivityForResult( actIntent, INSERT_TEXT + (selection - 3) );
*/
        }
    }
    
    
    /**
     * Select the proper bottom menu
     */
    public void displayBottomBar()
    {
        View     bottomBar = findViewById( R.id.bottom_toolbar );
        View     editBar   = findViewById( R.id.bottom_editbar );
        View     hexBar    = findViewById( R.id.bottom_hexbar );
        View     textBar   = findViewById( R.id.bottom_textbar );
        TextView tv        = findViewById( R.id.byteCount );
        
        
        //
        tv.setVisibility( View.GONE );
        
        //
        if ( global.bottomBarMode == 2 )
        {
            bottomBar.setVisibility( View.INVISIBLE );
            editBar.setVisibility( View.GONE );
            textBar.setVisibility( View.GONE );
            hexBar.setVisibility( View.VISIBLE );
        }
        else if ( global.bottomBarMode == 1 )
        {
            bottomBar.setVisibility( View.INVISIBLE );
            hexBar.setVisibility( View.GONE );
            textBar.setVisibility( View.GONE );
            editBar.setVisibility( View.VISIBLE );
        }
        else if ( global.bottomBarMode == 3 )
        {
            bottomBar.setVisibility( View.INVISIBLE );
            editBar.setVisibility( View.GONE );
            hexBar.setVisibility( View.GONE );
            textBar.setVisibility( View.VISIBLE );
        }
        else
        {   // Standard mode
            hexBar.setVisibility( View.GONE );
            editBar.setVisibility( View.GONE );
            textBar.setVisibility( View.GONE );
            bottomBar.setVisibility( View.VISIBLE );
        }
    }
    
    
    /**
     * Swap the search icon between search and search again
     */
    private void swapSearchIcon()
    {
        Toolbar tb   = findViewById( R.id.appBar );
        Menu    menu = tb.getMenu();
        
        // Get current Theme's textPrimaryColor
        for ( int i = 0; i < menu.size(); i++ )
        {
            if ( menu.getItem( i ).getItemId() == R.id.action_search )
            {
                if ( saveSearch != null && saveSearchCount > 0 )
                {
                    menu.getItem( i ).setIcon( R.drawable.refresh );
                    menu.getItem( i ).setTitle( R.string.action_find_again );
                    break;
                }
                else
                {
                    menu.getItem( i ).setIcon( R.drawable.hex_search );
                    menu.getItem( i ).setTitle( R.string.action_find );
                    break;
                }
            }
        }
    }
    
    /**
     * Swap the search icon between search and search again
     */
    private void swapReplaceIcon()
    {
        Toolbar  tb   = findViewById( R.id.appBar );
        Menu     menu = tb.getMenu();
        Drawable drawable;
        
        
        // Get current Theme's textPrimaryColor
        for ( int i = 0; i < menu.size(); i++ )
        {
            if ( menu.getItem( i ).getItemId() == R.id.action_replace )
            {
                if ( saveSearch != null && saveReplace != null && saveSearchCount > 0 )
                {
                    menu.getItem( i ).setTitle( R.string.action_replace_text_again );
                    drawable = menu.getItem( i ).getIcon();
                    //
                    if ( drawable != null )
                    {
                        drawable.mutate();
                        drawable.setColorFilter( Color.YELLOW, PorterDuff.Mode.SRC_ATOP );
                    }
                    break;
                }
                else
                {
                    menu.getItem( i ).setTitle( R.string.action_replace_text );
                    drawable = menu.getItem( i ).getIcon();
                    //
                    if ( drawable != null )
                    {
                        drawable.mutate();
                        drawable.setColorFilter( Color.WHITE, PorterDuff.Mode.SRC_ATOP );
                    }
                    break;
                }
            }
        }
    }
    
    
    /**
     * There are no parameters
     * Routine changes drawable color whenever
     * a table file can be restored by an 'Undo' action
     * ---------------------------------
     */
    public static void updateUndoButton( Activity activity, RomFileAccess rom )
    {
        try
        {
            Menu       menu;
            TypedValue a;
            //        Drawable   drawable;
            int      color;
            MenuItem item;
            
            // Get the menu for the Action bar
            Toolbar tb = activity.findViewById( R.id.appBar );
            menu = tb.getMenu();
            
            // Get current Theme's textPrimaryColor
            a = new TypedValue();
            activity.getTheme().resolveAttribute( R.attr.myIconColor, a, true );
            color = ContextCompat.getColor( activity, a.resourceId );
            
            // Change the color of the UNDO icon
            MenuItem menuItem   = menu.findItem( R.id.undoHexChanges );
            View     actionView = MenuItemCompat.getActionView( menuItem );
            //        ImageView imageView  = ( ImageView ) actionView.findViewById( R.id.undo_Image );
            //        TextView   textView = actionView.findViewById( R.id.undo_badge );
            Drawable[] drawList = badge.getCompoundDrawables();
            Drawable   drawable = drawList[ 3 ];
            
            
            // Check to see if we can use a backup at all
            if ( !rom.canUndo )
            {
                rom.undoList.clear();
            }
            
            //
            if ( drawable != null )
            {
                drawable.mutate();
                
                if ( rom.undoList != null && rom.undoList.size() > 0 )
                {
                    drawable.setColorFilter( color, PorterDuff.Mode.SRC_ATOP );
                    menuItem.setEnabled( true );
                    //
                    if ( rom.undoList.size() > MAXIMUM_UNDO )
                    {
                        CustomToast toast = new CustomToast( activity );
                        toast.myToaster( "Maximum undo count reached", Toast.LENGTH_LONG ).show();
                        //
                        rom.undoList.remove( rom.undoList.size() - 1 );
                        drawable.setColorFilter( Color.RED, PorterDuff.Mode.SRC_ATOP );
                        menuItem.setEnabled( true );
                    }
                }
                else
                {
                    drawable.setColorFilter( Color.BLACK, PorterDuff.Mode.SRC_ATOP );
                    menuItem.setEnabled( false );
                }
            }
            
            // Set the badge number
            badge.setText( "0\n" + MAXIMUM_UNDO );
            if ( rom.undoList != null && rom.undoList.size() > 0 )
            {
                badge.setText( String.format( "%3d\n%3d", rom.undoList.size(), MAXIMUM_UNDO ) );
            }
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * //#############################
     * <p>
     * Scroll bar management for the hex window
     * <p>
     * //#############################
     *
     * @param seekBar
     * @param progress
     * @param fromUser
     */
    @Override
    public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
    {
        // Force adjustment in 'onDraw'
        //        global.Position = progress * (global.colSize * global.rowSize);
        
        if ( scrollBar.getTag() != null )
        {
            // Show the changes
            global.setPosition( progress * (global.columnCount * global.rowCount) );
            hView.invalidate();
        }
        else
        {
            if ( (global.getPosition() + (global.columnCount * global.rowCount)) >= global.fileSize )
            {
                scrollBar.setProgress( scrollBar.getMax() );
            }
        }
    }
    
    @Override
    public void onStartTrackingTouch( SeekBar seekBar )
    {
        scrollBar.setTag( 1 );
    }
    
    @Override
    public void onStopTrackingTouch( SeekBar seekBar )
    {
        scrollBar.setTag( null );
    }
    
    
    /**
     * ################################
     * <p>
     * Strictly for the video Ad format
     * <p>
     * ################################
     */
    /**
     * Load the Ads
     */
    public static void loadRewardedVideoAd()
    {
        Bundle    extras = new Bundle();
        AdRequest adRequest;
        
        extras.putString( "max_ad_content_rating", "G" );
        
        // Currently using DEBUG code
        if ( !Globals.inDebug )
        {
            // Actual Ads
            adRequest = new AdRequest.Builder().addNetworkExtrasBundle( AdMobAdapter.class, extras ).build();
            mRewardedVideoAd.loadAd( "ca-app-pub-7978336361271355/7011497575", adRequest );
        }
        else
        {
            adRequest = new AdRequest.Builder().addNetworkExtrasBundle( AdMobAdapter.class, extras ).build();
            mRewardedVideoAd.loadAd( "ca-app-pub-3940256099942544/5224354917", adRequest );
        }
    }
    
    
    /**
     * Related to the callback
     */
    @Override
    public void onRewardedVideoAdLoaded()
    {
        if ( onAdReturnListener != null )
        {
            onAdReturnListener.onAdReturn( AD_LOADED, 0 );
        }
    }
    
    @Override
    public void onRewardedVideoAdOpened()
    {
    }
    
    @Override
    public void onRewardedVideoStarted()
    {
    }
    
    @Override
    public void onRewardedVideoAdClosed()
    {
        // Load the next rewarded video ad.
        loadRewardedVideoAd();
        
        if ( !rewardGiven )
        {
            Toast.makeText( context, "Video did not complete. Banner will remain", Toast.LENGTH_SHORT ).show();
            // Video didn't finish!
            setBannerStatus( WindHexActivity.this, 0 );
        }
        else
        {
            // set to launch again in 2 days to the millisecond
            // (3 = DAYS * ) ->,  (24 = Hours * ) ->, (60 = 1 Hour *) ->, (60 * 1000) = 1 minute
            bannerDisabled = System.currentTimeMillis() + (3 * (24 * (60 * (60 * 1000))));
            setBannerStatus( WindHexActivity.this, bannerDisabled );
            
            //
            Toolbar toolbar = findViewById( R.id.appBar );
            Menu    menu    = toolbar.getMenu();
            
            for ( int i = 0; i < menu.size(); i++ )
            {
                //###################################
                // Check for the banner Disable
                // button
                //###################################
                if ( menu.getItem( i ).getItemId() == R.id.action_ad_remove )
                {
                    if ( bannerDisabled == 0 )
                    {
                        menu.getItem( i ).setVisible( true );
                    }
                    else
                    {
                        menu.getItem( i ).setVisible( false );
                        FrameLayout cantainer = findViewById( R.id.adContainer );
                        adView.removeAllViews();
                        adView.destroy();
                        cantainer.removeAllViews();
                    }
                    
                    //
                    break;
                }
            }
        }
    }
    
    @Override
    public void onRewarded( RewardItem rewardItem )
    {
        // Reward the user
        rewardGiven = true;
    }
    
    @Override
    public void onRewardedVideoAdFailedToLoad( int i )
    {
    
    }
    
    @Override
    public void onRewardedVideoCompleted()
    {
    
    }
    
    @Override
    public void onRewardedVideoAdLeftApplication()
    {
    
    }
    
    
    /**
     * //##############################
     * <p>
     * Save settings info
     * <p>
     * //##############################
     */
    public void saveSettingsData( Context mContext )
    {
        SharedPreferences        prefs;
        SharedPreferences.Editor editor;
        int                      font_size;
        Set<String>              recent_files = new HashSet<>();
        
        
        //
        // Make sure we have a global instance
        //
        if ( global == null )
        {
            global = Globals.getInstance();
        }
        
        //
        if ( mContext != null )
        {
            prefs = mContext.getSharedPreferences( "system_settings", Context.MODE_PRIVATE );
            // Open the editor
            editor = prefs.edit();
            
            //
            // Try and get the recent files list should it exist
            //
            if ( recentFiles.size() > 0 )
            {
                recent_files.addAll( recentFiles );
                editor.putStringSet( "recent_files", recent_files );
            }
            
            // System flags
            editor.putInt( "system_flags", global.settingsFlag );
            
            // Saved path string
            if ( global.currentPath != null )
            {
                editor.putString( "current_path", global.currentPath.toString() );
            }
            
            // Theme info
            editor.putInt( "current_theme", global.currentTheme );
            
            // Font size
            editor.putInt( "font_size", global.fntHeight );
            
            // Unicode table set index
            editor.putInt( "unicode_table", table_activity.curUnicodeTable );
            editor.putInt( "unicode_search", SearchDialog.curUnicodeTable );
            //
            editor.commit();
        }
    }
    
    
    /**
     * //##############################
     * <p>
     * Banner disable information
     * <p>
     * //##############################
     */
    public static void setBannerStatus( Context mContext, long ticks )
    {
        SharedPreferences        prefs;
        SharedPreferences.Editor editor;
        
        //
        if ( mContext != null )
        {
            prefs = mContext.getSharedPreferences( "system_settings", Context.MODE_PRIVATE );
            // Open the editor
            editor = prefs.edit();
            
            // Days to leave banner down
            editor.putLong( "round_up", ticks );
            // Ensure no more reminders
            editor.putBoolean( "round_down", true );
            
            //
            editor.commit();
        }
    }
    
    
    /**
     * //##############################
     * <p>
     * Banner disable information
     * <p>
     * //##############################
     */
    public static void setBannerMessageStaus( Context mContext, boolean msgStatus )
    {
        SharedPreferences        prefs;
        SharedPreferences.Editor editor;
        
        //
        if ( mContext != null )
        {
            prefs = mContext.getSharedPreferences( "system_settings", Context.MODE_PRIVATE );
            // Open the editor
            editor = prefs.edit();
            
            // Ensure no more reminders
            editor.putBoolean( "round_down", msgStatus );
            
            //
            editor.commit();
        }
    }
    
    
    /**
     * //##############################
     * <p>
     * Load settings info
     * <p>
     * //##############################
     */
    public static void loadSettingsData( Context mContext )
    {
        SharedPreferences        prefs;
        SharedPreferences.Editor editor;
        String                   current_path;
        int                      font_size;
        Set<String>              recent_files;
        Globals                  global = Globals.getInstance();
        
        
        // First fail test for this code
        if ( mContext != null )
        {
            prefs = mContext.getSharedPreferences( "system_settings", Context.MODE_PRIVATE );
            
            //
            // Try and get the recent files list should it exist
            //
            recent_files = prefs.getStringSet( "recent_files", null );
            if ( recentFiles == null )
            {
                recentFiles = new ArrayList<>();
            }
            
            //
            recentFiles.clear();
            if ( recent_files != null )
            {   // Load the collection of file names
                recentFiles.addAll( recent_files );
            }
            
            // System flags
            global.settingsFlag = prefs.getInt( "system_flags", 0 );
            
            
            // Saved path string
            current_path = prefs.getString( "current_path", null );
            if ( current_path != null )
            {
                global.currentPath = new File( current_path );
            }
            
            // Theme info
            global.currentTheme = prefs.getInt( "current_theme", 0 );
            
            // Font size
            font_size = prefs.getInt( "font_size", 0 );
            if ( font_size > 0 )
            {
                global.fntHeight = font_size;
                global.textPaint.setTextSize( font_size );
                global.paint.setTextSize( font_size );
            }
            
            // Unicode table set index
            table_activity.curUnicodeTable = prefs.getInt( "unicode_table", 0 );
            SearchDialog.curUnicodeTable = prefs.getInt( "unicode_search", 0 );
            
            // If file contains nothing, add a button
            if ( recentFiles.size() == 0 )
            {
                recentFiles.add( "open_file_button" );
            }
            
            // Is the banner for Hex viewer enabbled?
            bannerDisabled = prefs.getLong( "round_up", 0 );
            
            // Has the banner disable message been seen?
            bannerMessage = prefs.getBoolean( "round_down", false );
            
            
            // Wait at least n days before showing Ads again
            if ( System.currentTimeMillis() >= bannerDisabled )
            {
                // Open the editor
                editor = prefs.edit();
                
                bannerDisabled = 0;
                editor.putLong( "round_up", 0 );
                
                editor.commit();
            }
        }
    }
}





