package com.GenesysEast.windhex.home_screen;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.GenesysEast.windhex.Dialogs.AboutDialog;
import com.GenesysEast.windhex.Dialogs.AppRate;
import com.GenesysEast.windhex.Dialogs.ErrorDialog;
import com.GenesysEast.windhex.Dialogs.RecentDialog;
import com.GenesysEast.windhex.GlobalClasses.FileUtils;
import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.GlobalClasses.RomFileAccess;
import com.GenesysEast.windhex.GlobalClasses.ThemeControl;
import com.GenesysEast.windhex.R;
import com.GenesysEast.windhex.WindHexActivity;
import com.GenesysEast.windhex.helper.helper_activity;
import com.GenesysEast.windhex.settings_menu.MenuData.MenuData;
import com.GenesysEast.windhex.settings_menu.settings_activity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity
        extends AppCompatActivity
        implements View.OnClickListener, RecentDialog.OnFileSelected
{
    
    private             Globals       global       = Globals.getInstance();
    private             RomFileAccess rom          = RomFileAccess.getInstance();
    private             int           currentTheme = 0;
    private             long          backPressed;
    private             AdView        adView;
    private             int[]         buttons      = { R.id.fileOpen, R.id.appInfo, R.id.helpLoader, R.id.devApps };
    //
    public static final int           SAVE_FILE_AS = 8;
    public static final int           LOAD_TABLE   = 3;
    public static final int           CHANGE_THEME = 2;
    public static final int           LOAD_ROM     = 1;
    
    
    /**
     * //############################
     * <p>
     * Activity creation
     * <p>
     * //############################
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        
        setTitle( "" );
        //
        // Load settings and recent files
        //
        //        act.loadSettingsData( new File( getCacheDir(), "config.txt" ) );
        WindHexActivity.loadSettingsData( this );
        
        // MUST BE SET BEFORE setContentView
        ThemeControl.onActivityCreateSetTheme( this, global.currentTheme );
        this.currentTheme = global.currentTheme;
        //
        setContentView( R.layout.home_screen_main );
        
        //############################
        //
        // Set in Debug status
        //
        //############################
        Globals.inDebug = true;
        //Globals.inDebug = false;
        
        
        //###############################
        //
        // Toolbar
        //
        //###############################
        Toolbar tb = findViewById( R.id.homeBar );
        tb.setTitle( "WindHex Mobile" );
        tb.setSubtitle( "Please make a selection" );
        //
        tb.setKeepScreenOn( (global.settingsFlag & MenuData.SCREEN_STATUS) != 0 );
        
        setSupportActionBar( tb );
        if ( getSupportActionBar() != null )
        {
            getSupportActionBar().setDisplayShowHomeEnabled( true );
        }
        
        for ( int button : buttons )
        {
            View btn = findViewById( button );
            
            btn.setOnClickListener( this );
            btn.setVisibility( View.VISIBLE );
        }
        
        
        //#############################
        //
        // Get the ADs rollin'
        // Actual Ads / Videos
        //
        //#############################
        // Currently using DEBUG code
        if ( !Globals.inDebug )
        {
            // Supports for children
            RequestConfiguration conf;
            conf = new RequestConfiguration.Builder().build();
            MobileAds.setRequestConfiguration( conf );
            
            // Actual Ads
            MobileAds.initialize( this, "ca-app-pub-7978336361271355~3276179698" );
        }
        else
        {
            List<String> testDevices = new ArrayList<>();
            testDevices.add( AdRequest.DEVICE_ID_EMULATOR );
            
            RequestConfiguration conf = new RequestConfiguration.Builder().setTestDeviceIds( testDevices ).build();
            MobileAds.setRequestConfiguration( conf );
            
            MobileAds.initialize( this, "ca-app-pub-3940256099942544~3347511713" );
        }
        
        
        //###############################
        //
        // Get an intent that started
        // this activity. SDK 28 & lower
        //
        //###############################
/*
        Intent intent = getIntent();
        decodeFileUri( intent.getData() );
*/
        
        
        //###############################
        //
        // Have I suggested viewing my
        // other Apps?
        //
        //###############################
/*
        if ( !AppRate.getAppSuggestion( this ) )
        {
            try
            {
                AppRate.setAppSuggestion( this );
                
                ErrorDialog dialog = new ErrorDialog();
                
                dialog.setAlertIcon( R.drawable.ic_info );
                dialog.setTitle( "A BIG Thank you!" );
                dialog.setMessage( R.string.message_for_users );
                dialog.setButtons( ErrorDialog.YES_BUTTON ).setYesText( "Done" );
                dialog.show( getSupportFragmentManager(), null );
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
            }
        }
        else
*/
        {
            //##########################
            //
            // App Rater
            //
            //##########################
            if ( !AppRate.isShowing )
            {
                rateMyApp();
            }
        }
    }
    
    
    @Override
    public void finish()
    {
        super.finish();
        WindHexActivity act = new WindHexActivity();
        //        act.saveSettingsData( new File( getCacheDir(), "config.txt" ) );
        act.saveSettingsData( this );
    }
    
    
    /**
     *
     */
    @Override
    public void onBackPressed()
    {
        if ( (backPressed + 2000) > System.currentTimeMillis() )
        {
            //super.onBackPressed();
            finish();
            System.exit( 1 );
        }
        else
        {
            Toast.makeText( getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT ).show();
        }
        
        //
        backPressed = System.currentTimeMillis();
    }
    
    
    /**
     * //###############################
     * <p>
     * Check Activity result
     * <p>
     * //###############################
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult( int requestCode, int resultCode, @Nullable Intent data )
    {
        super.onActivityResult( requestCode, resultCode, data );
        
        // Check which request we're responding to
        // Make sure the request was successful
        if ( requestCode == 10 && resultCode == RESULT_OK )
        {
            // The user picked a file.
            // The Intent's data Uri identifies which item was selected.
            if ( data != null )
            {
                // This is the key line item, URI specifies the name of the data
                Uri fileUri = data.getData();
                
                // Make a copy for reloading later
                boolean haveDir = false;
                
                //###########################
                //
                // Check again
                //
                //###########################
                if ( fileUri != null && fileUri.getPath() != null )
                {
                    // Load a rom and create a temp file
                    //                    global.fileName = new File( fileUri.getPath() );
                    FileUtils fileUtils = new FileUtils( this );
                    String fileName =  fileUtils.getPath( fileUri );
                    
//                    String fileName = rom.loadRomFile(  fileUri, this , this);
                    
                    if ( fileName != null )
                    {
                        global.fileName = new File( fileName );
                        
                        finish();
                        //
                        Intent romIntent = new Intent( this, WindHexActivity.class );
                        
                        romIntent.putExtra( "romLoaded", 1 );
                        startActivity( romIntent );
                    }
                    //                    rom.loadRomFile(  ), this, this );
                }
                else
                {
                    Toast.makeText( this, "Error: Could not load the file.", Toast.LENGTH_SHORT ).show();
                }
            }
        }
        else if ( requestCode == 1 && resultCode == Activity.RESULT_OK )
        {
/*
            // Load a ROM file
            if ( data != null && data.hasExtra( "FileName" ) )
            {
                Intent romIntent;
                global.fileName = new File( Objects.requireNonNull( data.getStringExtra( "FileName" ) ) );
                //
                finish();
                //
                romIntent = new Intent( this, WindHexActivity.class );
                romIntent.putExtra( "romLoaded", 1 );
                startActivity( romIntent );
            }
*/
        }
        else if ( requestCode == 2 )
        {
            // Settings changed
            if ( global.currentTheme != currentTheme )
            {
                finish();
                ThemeControl.changeToTheme( this, global.currentTheme );
                currentTheme = global.currentTheme;
            }
            else
            {
                Toolbar tb;
                tb = findViewById( R.id.homeBar );
                tb.setKeepScreenOn( (global.settingsFlag & MenuData.SCREEN_STATUS) != 0 );
            }
        }
    }
    
    
    /**
     * //################################
     * <p>
     * Use this method to create a temp
     * file we can edit
     * <p>
     * //################################
     *
     * @param context
     * @param contentUri
     *
     * @return
     */
/*
    private String getRealPathFromURI( Context context, Uri contentUri )
    {
        final ContentResolver contentResolver = context.getContentResolver();
        
        if ( contentResolver == null )
        {
            return null;
        }
        
        // Create file path inside app's data dir
        String filePath = context.getApplicationInfo().dataDir + File.separator + "temp.rom";
        
        File file = new File( filePath );
        try
        {
            InputStream inputStream = contentResolver.openInputStream( contentUri );
            if ( inputStream == null )
            {
                return null;
            }
            
            OutputStream outputStream = new FileOutputStream( file );
            byte[]       buf          = new byte[ 1024 ];
            int          len;
            while ( (len = inputStream.read( buf )) > 0 )
                outputStream.write( buf, 0, len );
            
            outputStream.close();
            inputStream.close();
        }
        catch ( IOException ignore )
        {
            return null;
        }
        
        return file.getAbsolutePath();
    }
*/
    
    
    
    /**
     * //#############################
     * <p>
     * Load a file to edit
     * <p>
     * //#############################
     */
    public void loadFile()
    {
        if ( permissionsCheck() )
        {
            Intent intent;
            
            intent = new Intent( Intent.ACTION_OPEN_DOCUMENT );
            intent.addCategory( Intent.CATEGORY_OPENABLE );
            intent.setType( "*/*" );
            
            startActivityForResult( Intent.createChooser( intent, "Select a File" ), 10 );
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
     * Create an options menu and attach to
     * the main AppBar
     *
     * @param menu
     *
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        Toolbar tb          = findViewById( R.id.homeBar );
        Menu    optionsMenu = tb.getMenu();
        //
        getMenuInflater().inflate( R.menu.home_screen_menu, optionsMenu );
        
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
        
        return true;
    }
    
    
    /**
     * Options selected handler
     *
     * @param item
     *
     * @return
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        int id = item.getItemId();
        
        if ( id == android.R.id.home )
        {
            finish();
            return true;
        }
        else if ( id == R.id.optSettings )
        {
            startActivityForResult( new Intent( this, settings_activity.class ), 2 );
            return true;
        }
        else if ( id == R.id.optRecent )
        {
            RecentDialog romLoad = new RecentDialog();
            romLoad.show( getSupportFragmentManager(), "RecentFiles" );
        }
        
        return false;
    }
    
    /**
     * Handler for button clicks
     *
     * @param v
     */
    @Override
    public void onClick( View v )
    {
        int id = v.getId();
        
        //
        if ( id == R.id.fileOpen )
        {
            // Load a ROM file
            loadFile();
        }
        else if ( id == R.id.appInfo )
        {
            AboutDialog dialog = new AboutDialog();
            
            dialog.show( getSupportFragmentManager(), "home_dialog" );
        }
        else if ( id == R.id.helpLoader )
        {
            //            Toast.makeText( this, "Help file Needed", Toast.LENGTH_SHORT ).show();
            Intent intent = new Intent( this, helper_activity.class );
            startActivity( intent );
        }
        else if ( id == R.id.devApps )
        {
            try
            {
                ErrorDialog dialog = new ErrorDialog();
                
                dialog.setTitle( "More Apps" ).setMessage( "You will be redirected to view the developer's collection of Apps. Is this okay?" );
                dialog.setAlertIcon( R.drawable.gamepad );
                dialog.setYesText( "Yes" ).setNoText( "No Thanks" );
                dialog.setButtons( ErrorDialog.YES_BUTTON | ErrorDialog.NO_BUTTON );
                dialog.setOnErrorListener( new ErrorDialog.OnErrorListener()
                {
                    @Override
                    public void onErrorExitClick( ErrorDialog dialog, int buttonClicked )
                    {
                        if ( buttonClicked == ErrorDialog.YES_BUTTON )
                        {
                            try
                            {
                                startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "https://play.google.com/store/apps/dev?id=8645290608649850542&hl=en_US" ) ) );
                            }
                            catch ( Exception ex )
                            {
                                Toast.makeText( HomeActivity.this, "Could not load Dev's collections", Toast.LENGTH_SHORT ).show();
                            }
                        }
                        
                        // Close the box!
                        dialog.dismiss();
                    }
                } );
                
                dialog.show( this.getSupportFragmentManager(), null );
            }
            catch ( android.content.ActivityNotFoundException nfe2 )
            {
                Toast.makeText( this, "Could not load Dev's collections", Toast.LENGTH_SHORT ).show();
            }
            //            }
/*
            
            try
            {
                startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "https://play.google.com/store/apps/dev?id=8645290608649850542&hl=en_US" ) ) );
            }
            catch ( android.content.ActivityNotFoundException nfe2 )
            {
                Toast.makeText( this, "Could not load Market place", Toast.LENGTH_SHORT ).show();
            }
*/
        }
        
    }
    
    
    @Override
    public void getFileName( String fileName )
    {
        Intent romIntent;
        
        global.fileName = new File( fileName );
        finish();
        //
        romIntent = new Intent( this, WindHexActivity.class );
        romIntent.putExtra( "romLoaded", 1 );
        startActivity( romIntent );
    }
    
    
    /**
     * App Rater Toast
     */
    private void rateMyApp()
    {
        long              launch_count;
        long              date_firstLaunch;
        long              days_to_wait;
        SharedPreferences prefs = getSharedPreferences( "app_rate_sys", Context.MODE_PRIVATE );
        
        if ( prefs.getBoolean( "hide_app_rate", false ) )
        {
            return;
        }
        
        // Open the editor
        SharedPreferences.Editor editor = prefs.edit();
        
        // Save the Show / Hide info
        editor.putBoolean( "hide_app_rate", false );
        
        // Increment launch counter
        launch_count = prefs.getLong( "launch_count", 0 ) + 1;
        editor.putLong( "launch_count", launch_count );
        
        // Get date of first launch
        date_firstLaunch = prefs.getLong( "date_first_launch", 0 );
        if ( date_firstLaunch == 0 )
        {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong( "date_first_launch", date_firstLaunch );
        }
        
        // Get days to wait
        days_to_wait = prefs.getLong( "days_to_wait", AppRate.DAYS_UNTIL_PROMPT );
        editor.putLong( "days_to_wait", AppRate.DAYS_UNTIL_PROMPT );
        
        // Wait at least n days before opening
        if ( launch_count >= AppRate.LAUNCHES_UNTIL_PROMPT )
        {
            if ( System.currentTimeMillis() >= (date_firstLaunch + (days_to_wait * 24 * 60 * 60 * 1000)) )
            {
                AppRate dialog = new AppRate( this, editor );
                dialog.setCancelable( false );
                dialog.show( getSupportFragmentManager(), "App Rate" );
                AppRate.isShowing = true;
            }
        }
        
        // Save SharePrefs
        editor.commit();
    }
    
    
    /**
     * //############################
     * <p>
     * Get he banner height to be
     * displayed
     * <p>
     * //############################
     *
     * @return
     */
    public static PointF getAdBannerSize( Activity activity )
    {
        DisplayMetrics displayMetrics = (( Context ) activity).getResources().getDisplayMetrics();
        PointF         bannerSize     = getScreenHeightInDP( activity );
        
        if ( bannerSize.y < 400 )
        {
            bannerSize.y = (32 * displayMetrics.density);
        }
        else if ( bannerSize.y <= 720 )
        {
            bannerSize.y = (50 * displayMetrics.density);
        }
        else
        {
            bannerSize.y = (90 * displayMetrics.density);
        }
        
        bannerSize.x *= displayMetrics.density;
        
        return bannerSize;
    }
    
    
    /**
     * Get teh screen's height
     *
     * @param activity
     *
     * @return
     */
    public static PointF getScreenHeightInDP( Activity activity )
    {
        DisplayMetrics displayMetrics = (( Context ) activity).getResources().getDisplayMetrics();
        
        float screenHeightInDP = displayMetrics.heightPixels / displayMetrics.density;
        float screenWidthInDP  = displayMetrics.widthPixels / displayMetrics.density;
        
        return new PointF( Math.round( screenWidthInDP ), Math.round( screenHeightInDP ) );
    }
}
