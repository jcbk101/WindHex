package com.GenesysEast.windhex.file_compare;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.GlobalClasses.PointXYZ;
import com.GenesysEast.windhex.GlobalClasses.RomFileAccess;
import com.GenesysEast.windhex.R;


public class CompareHexView
        extends View
{
    /**
     * Code by: John Charles
     * Genesys East, LLC
     * Started on September 15th, 2019
     */
    private Globals       global  = Globals.getInstance();
    private Globals       testGlobal;
    private RomFileAccess rom     = RomFileAccess.getInstance();
    private RomFileAccess testRom;
    private StringBuilder dataStr = new StringBuilder();
    private StringBuilder testStr = new StringBuilder();
    
    // Variables
    private String            s        = "";
    private Context           iContext;
    private TypedValue        a        = new TypedValue();
    private Rect              hexRect  = new Rect();
    private Rect              textRect = new Rect();
    private Paint             bgPaint  = new Paint();
    private byte[]            iMain    = new byte[ 32 ];
    private byte[]            iTest    = new byte[ 32 ];
    // Try to attach the interface???
    private OnCompareExcahnge onCompareExcahnge;
    
    
    public interface OnCompareExcahnge
    {
        void sendData( int iType, int iData, String iText );
    }
    
    /**
     * Interface connection
     *
     * @param onCompareExcahnge
     */
    public void setOnCompareExcahnge( OnCompareExcahnge onCompareExcahnge )
    {
        this.onCompareExcahnge = onCompareExcahnge;
    }
    
    /**
     * Main constructor
     *
     * @param context N/A
     * @param attrs   N/A
     */
    public CompareHexView( Context context, @Nullable AttributeSet attrs )
    {
        super( context, attrs );
    }
    
    /**
     * Initialize the needed variables
     *
     * @param global   N/A
     * @param rom      N/A
     * @param iContext N/A
     */
    public void initView( Globals global, RomFileAccess rom, Context iContext )
    {
        this.testGlobal = global;
        this.testRom = rom;
        this.iContext = iContext;
    }
    
    /**
     * Handle Margin alignment
     *
     * @param changed N/A
     * @param left    N/A
     * @param top     N/A
     * @param right   N/A
     * @param bottom  N/A
     */
    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom )
    {
        super.onLayout( changed, left, top, right, bottom );
        //
        
        // Use default font size
        testGlobal.fntHeight = getResources().getDimensionPixelSize( R.dimen._10sdp );
        testGlobal.textPaint.setAntiAlias( true );
        testGlobal.textPaint.setTextSize( testGlobal.fntHeight );
        testGlobal.textPaint.setTypeface( Typeface.MONOSPACE );
        
        
        // Paint settings_menu for Hex, Offset text
        testGlobal.paint.setStyle( Paint.Style.FILL );
        testGlobal.paint.setTextSize( testGlobal.fntHeight );
        // Set the paint font
        //        paint.setTypeface( font );
        testGlobal.paint.setTypeface( Typeface.create( Typeface.MONOSPACE, Typeface.BOLD ) );
        
        //        global.fileSize = 0;
        testGlobal.txtX = 0;
        testGlobal.chrWidth = ( int ) testGlobal.paint.measureText( "WW" );
        
        
        int iWidth    = ( int ) testGlobal.paint.measureText( "0" );
        int iWinWidth = getWidth();
        int baseX;
        int hX        = 0;
        int tX        = 0;
        
        testGlobal.columnCount = 0;
        testGlobal.rowCount = (getHeight() / testGlobal.fntHeight) / 2;
        baseX = (iWidth * ( int ) testGlobal.ofsTextWidth.x) + iWidth;
        
        
        //######################################
        //
        // Get the Hex and Text window widths
        //
        //######################################
        while ( (baseX + hX + tX + iWidth) < iWinWidth )
        {
            hX += (iWidth * 2) + iWidth;
            tX += iWidth;
            testGlobal.columnCount++;
            //
            if ( testGlobal.columnCount >= 16 )
            {
                testGlobal.columnCount = 16;
                break;
            }
            //
        }
        
        if ( (baseX + hX + tX + iWidth) > iWinWidth )
        {
            while ( (baseX + hX + tX + iWidth) >= iWinWidth )
            {
                hX -= (iWidth * 2) + iWidth;
                testGlobal.columnCount--;
            }
        }
        
        // Calculate screen size: Dual screen
//        testGlobal.screenSize = (testGlobal.columnCount * testGlobal.rowCount) / 2;
        testGlobal.screenSize = (testGlobal.columnCount * (testGlobal.rowCount / 2));
        //
//        onCompareExcahnge.sendData( 0, testGlobal.screenSize, null );
    }
    
    
    /**
     * Handle the drawing
     *
     * @param canvas Drawing canvas
     */
    @Override
    protected void onDraw( final Canvas canvas )
    {   //
        super.onDraw( canvas );
        int     mainSize;
        int     testSize;
        int     colorPrimary;
        int     yPos;
        String  code;
        int     codeColor;
        boolean canBreak       = false;
        int     mainX;
        int     testX;
        int     safeScreensize = 0;
        
        
        if ( testGlobal.columnCount <= 0 || testGlobal.rowCount <= 0 )
        {
            return;
        }
        
        testGlobal.iHeight = getHeight();
        bgPaint.setColor( Color.RED );
        //
        // Error checking for positions
        //
        if ( (global.getPosition() + testGlobal.screenSize) >= global.fileSize )
        {
            setVerticalScrollbarPosition( ( int ) (global.fileSize - (testGlobal.rowCount * testGlobal.columnCount)) );
//            testGlobal.setPosition( ( int ) (testGlobal.fileSize - testGlobal.screenSize) );
        }
        
        if ( global.getPosition() < 0 )
        {
            global.setPosition( 0 );
        }
        
        // Test file information
//        if ( (testGlobal.getPosition() + (testGlobal.rowCount * testGlobal.columnCount)) > testGlobal.fileSize )
        if ( (testGlobal.getPosition() + testGlobal.screenSize) >= testGlobal.fileSize )
        {
            testGlobal.setPosition( ( int ) (testGlobal.fileSize - testGlobal.screenSize) );
            global.setPosition( testGlobal.getPosition() );
        }
        
        if ( testGlobal.getPosition() < 0 )
        {
            testGlobal.setPosition( 0 );
        }
        
        //
        // Don't bother if smallest file has reached it's limit
        //
        if ( testGlobal.getPosition() != global.getPosition() )
        {
            if ( testGlobal.fileSize < global.fileSize )
            {
                global.setPosition( testGlobal.getPosition() );
            }
            else
            {
                testGlobal.setPosition( global.getPosition() );
            }
        }
        //--------------------------------------//
        //
        // Draw each row that is visible
        //
        //--------------------------------------//
        
        // Primary color
        iContext.getTheme().resolveAttribute( R.attr.hexColorPrimary, a, true );
        colorPrimary = ContextCompat.getColor( iContext, a.resourceId );
        
        // Secondary color
/*
        iContext.getTheme().resolveAttribute( R.attr.hexColorSecondary, a, true );
        int color = ContextCompat.getColor( iContext, a.resourceId );
*/
        int color = 0xFF888888;
        
        //
        // Read, Test, and draw both files
        //
        int second = testGlobal.rowCount;
        
        // Do the first file then test thos bytes
        for ( int y = 0; y < (testGlobal.rowCount / 2); y++ )
        {
            if ( (global.getPosition() + (y * testGlobal.columnCount) < global.fileSize) )
            {   // Get the data from the ROM file: Main
                mainSize = rom.fread( iMain, global.getPosition() + (y * testGlobal.columnCount), testGlobal.columnCount );
            }
            else
            {   // Passed that file's EOF mark
                break;
            }
            
            if ( (testGlobal.getPosition() + (y * testGlobal.columnCount) < testGlobal.fileSize) )
            {   // Get the data from the ROM file: Test
                testSize = testRom.fread( iTest, testGlobal.getPosition() + (y * testGlobal.columnCount), testGlobal.columnCount );
            }
            else
            {   // Passed that file's EOF mark
                break;
            }
            
            //
            yPos = (y + y);
            //
            // Draw the offset: Test data
            // Secondary color
            testGlobal.paint.setColor( color );
            s = String.format( "%08X ", testGlobal.getPosition() + (y * testGlobal.columnCount) );
            canvas.drawText( s, (testX = 0), ((1 + yPos + second) * testGlobal.fntHeight), testGlobal.paint );
            //
            // Move 'X' over
            testX = ( int ) testGlobal.paint.measureText( s );
            
            //
            // Draw the offset: Main data
            //
            testGlobal.paint.setColor( colorPrimary );
            s = String.format( "%08X ", global.getPosition() + (y * testGlobal.columnCount) );
            canvas.drawText( s, (mainX = 0), ((1 + yPos) * testGlobal.fntHeight), testGlobal.paint );
            //
            // Move 'X' over
            mainX = ( int ) testGlobal.paint.measureText( s );
            
            //------------------------------------------------//
            //
            // Draw Hex values
            //
            //------------------------------------------------//
            //            testGlobal.paint.setColor( colorPrimary );
            
            //
            for ( int x = 0; x < testGlobal.columnCount; x++ )
            {
                if ( (global.getPosition() + (y * testGlobal.columnCount) + x) >= global.fileSize )
                {   // Main Hex string
                    dataStr.append( String.format( "%" + ((testGlobal.columnCount - x) * 3) + "c", 0x20 ) );
                    canBreak = true;
                }
                //
                if ( (testGlobal.getPosition() + (y * testGlobal.columnCount) + x) >= testGlobal.fileSize )
                {   // Test Hex string
                    testStr.append( String.format( "%" + ((testGlobal.columnCount - x) * 3) + "c", 0x20 ) );
                    canBreak = true;
                }
                
                // We need to leave this section of drawing
                // because a ROM has no data left to process
                if ( canBreak )
                {
                    break;
                }
                
                //
                //----------------------------------
                //
                dataStr.append( String.format( "%02X ", iMain[ x ] ) );
                testStr.append( String.format( "%02X ", iTest[ x ] ) );
                
                if ( iMain[ x ] != iTest[ x ] )
                {
                    // Highlight the difference in Main Rom
                    hexRect.left = mainX + (x * ( int ) testGlobal.paint.measureText( "   " ));
                    hexRect.top = (yPos * testGlobal.fntHeight);
                    hexRect.right = (hexRect.left + testGlobal.chrWidth);
                    hexRect.bottom = (hexRect.top + testGlobal.fntHeight) + (testGlobal.fntHeight / 4);
                    hexRect.top -= (testGlobal.fntHeight / 4);
                    
                    bgPaint.setColor( Color.RED );
                    canvas.drawRect( hexRect, bgPaint );
                    
                    // Highlight the difference in Test Rom
                    hexRect.left = testX + (x * ( int ) testGlobal.paint.measureText( "   " ));
                    hexRect.top = ((yPos + second) * testGlobal.fntHeight);
                    hexRect.right = (hexRect.left + testGlobal.chrWidth);
                    hexRect.bottom = (hexRect.top + testGlobal.fntHeight) + (testGlobal.fntHeight / 4);
                    hexRect.top -= (testGlobal.fntHeight / 4);
                    
                    bgPaint.setColor( Color.YELLOW );
                    canvas.drawRect( hexRect, bgPaint );
                }
            }
            
            //
            // Display hex string for both ROMs
            //
            // Primary color
            //            testGlobal.paint.setColor( colorPrimary );
            canvas.drawText( dataStr.toString(), mainX, ((1 + yPos) * testGlobal.fntHeight), testGlobal.paint );
            
            // Secondary color
            testGlobal.paint.setColor( color );
            canvas.drawText( testStr.toString(), testX, ((1 + yPos + second) * testGlobal.fntHeight), testGlobal.paint );
            
            // Move pen to next X position
            mainX += ( int ) testGlobal.paint.measureText( dataStr.toString() );
            testX += ( int ) testGlobal.paint.measureText( testStr.toString() );
            
            // Erase current contents
            dataStr.delete( 0, dataStr.length() );
            testStr.delete( 0, testStr.length() );
            
            //------------------------------------------------//
            //
            // Draw Text values
            //
            //------------------------------------------------//
            testGlobal.paint.setColor( colorPrimary );
            //
            
            for ( int x = 0; x < testGlobal.columnCount; x++ )
            {
                if ( (global.getPosition() + (y * testGlobal.columnCount) + x) >= global.fileSize )
                {   // Main Hex string
                    canBreak = true;
                }
                //
                if ( (testGlobal.getPosition() + (y * testGlobal.columnCount) + x) >= testGlobal.fileSize )
                {   // Test Hex string
                    canBreak = true;
                }
                
                // We need to leave this section of drawing
                // because a ROM has no data left to process
                if ( canBreak )
                {
                    break;
                }
                
                int value = (iMain[ x ] & 0xFF);
                
                // Get the text for Main ROM: Single bytes only
                code = global.oneCode[ value ];
                //                codeColor = global.oneColor[ value ];
                
                testGlobal.paint.setColor( colorPrimary );
                //
                //                if ( (global.Position + (y * global.rowSize) + x) == global.xyHex )
                //
                if ( iMain[ x ] != iTest[ x ] )
                {   // Highlight the difference in Main Rom
                    textRect.left = mainX;
                    textRect.top = (yPos * testGlobal.fntHeight);
                    if ( code != "" )
                    {
                        textRect.right = (textRect.left + ( int ) testGlobal.paint.measureText( code ));
                    }
                    else
                    {
                        textRect.right = (textRect.left + (testGlobal.chrWidth / 2));
                    }
                    textRect.bottom = (textRect.top + testGlobal.fntHeight) + (testGlobal.fntHeight / 4);
                    textRect.top -= (testGlobal.fntHeight / 4);
                    
                    bgPaint.setColor( Color.RED );
                    canvas.drawRect( textRect, bgPaint );
                    
                    // Highlight the difference in Test Rom
                    textRect.left = testX;
                    textRect.top = ((yPos + second) * testGlobal.fntHeight);
                    if ( code != "" )
                    {
                        textRect.right = (textRect.left + ( int ) testGlobal.paint.measureText( code ));
                    }
                    else
                    {
                        textRect.right = (textRect.left + (testGlobal.chrWidth / 2));
                    }
                    textRect.bottom = (textRect.top + testGlobal.fntHeight) + (testGlobal.fntHeight / 4);
                    textRect.top -= (testGlobal.fntHeight / 4);
                    
                    bgPaint.setColor( Color.YELLOW );
                    canvas.drawRect( textRect, bgPaint );
                }
                
                // Draw Main Text
                if ( code != "" )
                {   // Character to draw: Main
                    canvas.drawText( code, mainX, ((1 + yPos) * testGlobal.fntHeight), testGlobal.paint );
                    mainX += ( int ) testGlobal.paint.measureText( code );
                }
                else
                {
                    canvas.drawText( ".", mainX, ((1 + yPos) * testGlobal.fntHeight), testGlobal.paint );
                    mainX += (testGlobal.chrWidth / 2);
                }
                
                //
                // Get the text for Main ROM: Single bytes only
                //
                value = (iTest[ x ] & 0xFF);
                code = global.oneCode[ value ];
                //                codeColor = global.oneColor[ value ];
                //                testGlobal.paint.setColor( codeColor );
                testGlobal.paint.setColor( color );
                
                // Draw Test Text
                if ( code != "" )
                {   // Character to draw: Main
                    canvas.drawText( code, testX, ((1 + yPos + second) * testGlobal.fntHeight), testGlobal.paint );
                    testX += ( int ) testGlobal.paint.measureText( code );
                }
                else
                {
                    canvas.drawText( ".", testX, ((1 + yPos + second) * testGlobal.fntHeight), testGlobal.paint );
                    testX += (testGlobal.chrWidth / 2);
                }
            }
        }
        
        //
        // Display text string
        //
        //            canvas.drawText( dataStr.toString(), global.txtX, ((1 + yPos) * global.fntHeight), global.paint );
        // Clear the text buffer
        dataStr.delete( 0, dataStr.length() );
        testStr.delete( 0, testStr.length() );
    }
}



