package com.GenesysEast.windhex.GlobalClasses;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.opengl.EGLObjectHandle;
import android.text.TextPaint;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.GenesysEast.windhex.Hex_Drawer.DrawHexView;
import com.GenesysEast.windhex.WindHexActivity;

import java.io.File;
import java.util.ArrayList;


public class Globals
{
    private static Globals instance;
    
    public        boolean           isInitialized   = false;
    public        PointXYZ          xyHex           = new PointXYZ();
    public        DrawHexView       hView;
    //    public Activity         activity;
    public        Paint             paint           = new Paint();
    public        TextPaint         textPaint       = new TextPaint();
    // ROM File
    public        File              fileName;
    // Table File(s)
    public        File[]            tblFile         = new File[ 2 ];
    // Current Directory
    public        File              currentPath;
    // Current Table file
    public        int               currentTable    = 0;
    //    public byte[]           iRom;
    public        String[]          oneCode         = new String[ 0x100 ];
    public        int[]             oneColor        = new int[ 0x100 ];
    public        String[]          twoCode         = new String[ 0x10000 ];
    public        int[]             twoColor        = new int[ 0x10000 ];
    //
    public        ArrayList<Tabler> jList           = new ArrayList<>();
    //
    public        int               bottomBarMode   = 0;
    public        int               settingsFlag    = 0;
    public        int               columnCount     = 0;
    public        int               rowCount        = 0;
    public        int               screenSize      = 0;
    public        PointXYZ          ofsTextWidth    = new PointXYZ( 0, 0, 0 );
    public        int               iHeight         = 0;
    public        int               chrWidth        = 0;
    public        int               txtX            = 0;
    public        long              pageSize        = 0;
    private       long              Position        = 0;
    public        int               fntHeight       = 0;
    public        long              fileSize        = 0;
    public        float             iScrollUp       = 0.0f;
    public        float             iScrollDown     = 0.0f;
    public        int               currentTheme    = 0;
    public        boolean           lowerBarVisible = false;
    public        boolean           romModified     = false;
    public        boolean           isScrolling     = false;
    public        boolean           romInUse        = false;
    private       SeekBar           scrollBar;
    // Debug status
    public static boolean           inDebug         = true;
    
    
    /**
     * To stop calling from other classes
     * no instantiating allowed
     */
    private Globals()
    {
        hexInit();
    }
    
    public Globals( boolean forViewing )
    {
        hexInit();
    }
    
    
    /**
     * Scrollbar access
     */
    public void setScrollBar( SeekBar scrollBar )
    {
        this.scrollBar = scrollBar;
    }
    
    /**
     * To gain more control over changes made
     *
     * @param position
     */
    public void setPosition( long position )
    {
        Position = position;
        if ( scrollBar != null && scrollBar.getTag() == null && screenSize > 0 )
        {
            scrollBar.setProgress( ( int ) (position / screenSize) );
        }
    }
    
    public long getPosition()
    {
        return Position;
    }
    
    /**
     * //################################
     * <p>
     * Init the hex engine
     * <p>
     * //################################
     */
    public void hexInit()
    {
        if ( hView != null && !isInitialized )
        {
            hView.initHexViewer( this );
        }
    }
    
    
    /**
     * default table loader
     */
    public void buildAsciiTable()
    {
        for ( int y = 0; y < 0x00100; y++ )
        {
            oneColor[ y ] = Color.WHITE;
            //
            if ( y > 0x1F && y < 0x7F )
            {
                oneCode[ y ] = String.format( "%c", y );
            }
            else
            {
                oneCode[ y ] = "";
            }
        }
        //
        for ( int y = 0; y < 0x10000; y++ )
        {
            twoColor[ y ] = Color.WHITE;
            twoCode[ y ] = "";
        }
    }
    
    
    /**
     * Check for a number btween two numbers
     */
    public boolean valueBetween( long value, long min, long max )
    {
        if ( min > max )
        {
            return (value >= max && value <= min);
        }
        else
        {
            return (value >= min && value <= max);
        }
    }
    
    
    /**
     * Get value of variable
     * return integer or whatever type of variable you're working with
     * return statement returns the value of integer "test"
     */
    public static synchronized Globals getInstance()
    {
        if ( instance == null )
        {
            instance = new Globals();
        }
        return instance;
    }
}