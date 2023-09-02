package com.GenesysEast.windhex.file_compare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.GlobalClasses.RomFileAccess;
import com.GenesysEast.windhex.GlobalClasses.ThemeControl;
import com.GenesysEast.windhex.R;
import com.GenesysEast.windhex.settings_menu.MenuData.MenuData;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

public class file_compare
        extends AppCompatActivity
        implements View.OnClickListener, CompareHexView.OnCompareExcahnge
{
    private CompareHexView   hexView;
    private Globals          global     = Globals.getInstance();
    private Globals          testGlobal = new Globals( true );
    private RomFileAccess    testRom    = new RomFileAccess( true );
    private long             savePosition;
    private AppCompatSeekBar fileSeeker;
    
    /**
     * Creator method
     *
     * @param savedInstanceState N/A
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        
        // MUST BE SET BEFORE setContentView
        ThemeControl.onActivityCreateSetTheme( this, global.currentTheme );
        //
        setContentView( R.layout.activity_file_compare );
        
        //        recentFiles.addAll( Arrays.asList( getResources().getStringArray( android.R.array.imProtocols ) ) );
        // Check for ROM
        Intent romIntent = getIntent();
        
        if ( romIntent.hasExtra( "TestFile" ) )
        {
            testGlobal.fileName = new File( Objects.requireNonNull( romIntent.getStringExtra( "TestFile" ) ) );
            testRom.loadFileToView( testGlobal.fileName, testGlobal );
            
            TextView tb;
            tb = findViewById( R.id.compareBar );
            
            // Display the two files being compared
            if ( testGlobal.fileName != null && global.fileName != null )
            {
                //                tb.setText( "File comparison" );
                tb.setText( String.format( Locale.getDefault(), "%s < > %s", global.fileName.getName(), testGlobal.fileName.getName() ) );
                tb.setKeepScreenOn( (global.settingsFlag & MenuData.SCREEN_STATUS) != 0 );
            }
            
            // Get the drawing surface
            hexView = findViewById( R.id.compareHexView );
            hexView.setOnCompareExcahnge( this );
            
            
            // Get the dimensions needed to draw
            // Initialize the variables used within class
            hexView.initView( testGlobal, testRom, this );
            
            // Save file browsing position
            savePosition = global.getPosition();
            
            // Find all differences
            global.setPosition( 0 );
            testGlobal.setPosition( 0 );
            
            
            //###########################
            //
            // File seek bar
            //
            //###########################
            fileSeeker = findViewById( R.id.fileSeeker );
            fileSeeker.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
                {
                    testGlobal.setPosition( fileSeeker.getProgress() );
                    global.setPosition( fileSeeker.getProgress() );
                    hexView.invalidate();
                }
                
                @Override
                public void onStartTrackingTouch( SeekBar seekBar )
                {
                
                }
                
                @Override
                public void onStopTrackingTouch( SeekBar seekBar )
                {
                }
            } );
            fileSeeker.setProgress( 0 );
            
            
            //
            //###########################
            //
            if ( testGlobal.fileSize <= global.fileSize )
            {
                fileSeeker.setMax( ( int ) testGlobal.fileSize );
            }
            else
            {
                fileSeeker.setMax( ( int ) global.fileSize );
            }
        }
        else
        {   // Nothing is loaded, leave
            Toast.makeText( this, "File to compare not loaded", Toast.LENGTH_SHORT ).show();
            finish();
        }
    }
    
    /**
     * Interface for communicating any data from the CompareHexView class
     *
     * @param iType N/A
     * @param iData N/A
     * @param iText N/A
     */
    @Override
    public void sendData( int iType, int iData, String iText )
    {
        if ( iType == 0 && fileSeeker != null )
        {   // Set the seek bar increment data
            fileSeeker.incrementProgressBy( iData );
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
        
        // Restore the File position
        global.setPosition( savePosition );
    }
    
    
    @Override
    public void onClick( View v )
    {
        int id = v.getId();
        
        if ( id == R.id.compareBack )
        {
            testGlobal.setPosition( testGlobal.getPosition() - testGlobal.screenSize );
            global.setPosition( global.getPosition() - testGlobal.screenSize );
            //
            if ( testGlobal.fileSize <= global.fileSize )
            {
                fileSeeker.setProgress( ( int ) testGlobal.getPosition() );
            }
            else
            {
                fileSeeker.setProgress( ( int ) global.getPosition() );
            }
            //
            hexView.invalidate();
        }
        else if ( id == R.id.compareNext )
        {
            testGlobal.setPosition( testGlobal.getPosition() + testGlobal.screenSize );
            global.setPosition( global.getPosition() + testGlobal.screenSize );
            fileSeeker.setProgress( ( int ) global.getPosition() );
            //
            if ( testGlobal.fileSize <= global.fileSize )
            {
                fileSeeker.setProgress( ( int ) testGlobal.getPosition() );
            }
            //
            hexView.invalidate();
        }
    }
}
