package com.GenesysEast.windhex.Hex_Drawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.OverScroller;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ViewCompat;

import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.GlobalClasses.PointXYZ;
import com.GenesysEast.windhex.GlobalClasses.RomFileAccess;
import com.GenesysEast.windhex.Hex_Drawer.SubClasses.MyPincher;
import com.GenesysEast.windhex.Keyboard.HexKeyboard;
import com.GenesysEast.windhex.R;


public class DrawHexView
        extends View
        implements GestureDetector.OnGestureListener, View.OnTouchListener
{
    /**
     * Code by: John Charles
     * Genesys East, LLC
     * Started on May 27th, 2019
     */
    Globals global = Globals.getInstance();
    
    // Variables
    private        String                s             = "";
    private        StringBuilder         dataStr       = new StringBuilder();
    private        TypedValue            a             = new TypedValue();
    private        GestureDetectorCompat Detector;
    private        ScaleGestureDetector  scaleDetector = null;
    private        OverScroller          viewScroller  = new OverScroller( this.getContext() );
    private        Rect                  hexRect       = new Rect();
    private        Rect                  textRect      = new Rect();
    private        Paint                 bgPaint       = new Paint();
    private        boolean               fingerDown    = false;
    private        byte[]                iRom          = new byte[ 128 ];
    private        Activity              activity;
    private        RomFileAccess         rom           = RomFileAccess.getInstance();
    private        SeekBar               scrollBar;
    private static boolean               moveByArrow   = false;
    
    /**
     * Get access to the attached seekbar
     *
     * @param scrollBar
     */
    public void setScrollBar( SeekBar scrollBar )
    {
        this.scrollBar = scrollBar;
    }
    
    /**
     * @param context
     * @param attrs
     */
    public DrawHexView( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        setOnTouchListener( this );
        
        Detector = new GestureDetectorCompat( getContext(), this );
        
        if ( scaleDetector == null )
        {
            // Create an instance of OnPinchListner custom class.
            MyPincher onPinch = new MyPincher( getContext() );
            onPinch.sethView( this );
            
            // Create the new scale gesture detector object use above pinch zoom gesture listener.
            scaleDetector = new ScaleGestureDetector( getContext(), onPinch );
        }
    }
    
    /**
     * Support for cursor movement using Keyboard arrows or Game pad
     *
     * @param keyCode
     * @param event
     *
     * @return
     */
    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event )
    {
        if ( global.isScrolling )
        {
            return false;
        }
        
        // Hex input keys
        if ( (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) || keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_F )
        {
            int primaryCode = (keyCode - KeyEvent.KEYCODE_A);
            
            if ( keyCode <= KeyEvent.KEYCODE_9 )
            {
                primaryCode = (keyCode - KeyEvent.KEYCODE_0);
            }
            
            // In
            if ( HexKeyboard.inputHex( this, primaryCode ) )
            {
                // Align screen with current offsets
                if ( global.xyHex.x >= (global.getPosition() + (global.columnCount * global.rowCount)) )
                {
                    global.setPosition( global.getPosition() + global.columnCount );
                    global.xyHex.z = global.getPosition();
                    invalidate();
                }
                
                // Reset data
                rom.curByte = -1;
                rom.curIndex = -1;
                rom.curOffset = -1;
                rom.oldOffset = -1;
            }
        }
        
        //#########################################
        //
        // Navigation Keys
        //
        //#########################################
        switch ( keyCode )
        {
            case KeyEvent.KEYCODE_MOVE_HOME:
                if ( moveByArrow )
                {
                    global.xyHex.x = 0;
                    global.xyHex.y = 0;
                    global.setPosition( 0 );
                    invalidate();
                    return true;
                }
                return false;
            
            case KeyEvent.KEYCODE_MOVE_END:
                if ( moveByArrow )
                {
                    global.xyHex.x = rom.fileSize - 1;
                    global.xyHex.y = rom.fileSize - 1;
                    global.setPosition( rom.fileSize );
                    invalidate();
                    return true;
                }
                return false;
            
            case KeyEvent.KEYCODE_PAGE_UP:
                if ( moveByArrow )
                {
                    HexKeyboard.pageUp( this );
                    return true;
                }
                return false;
            case KeyEvent.KEYCODE_PAGE_DOWN:
                if ( moveByArrow )
                {
                    HexKeyboard.pageDown( this );
                    return true;
                }
                return false;
            
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if ( moveByArrow )
                {
                    HexKeyboard.codeLeft( this );
                    return true;
                }
                return false;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if ( moveByArrow )
                {
                    HexKeyboard.codeRight( this );
                    return true;
                }
                return false;
            case KeyEvent.KEYCODE_DPAD_UP:
                if ( moveByArrow )
                {
                    HexKeyboard.codeUp( this );
                    return true;
                }
                return false;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if ( moveByArrow )
                {
                    HexKeyboard.codeDown( this );
                    return true;
                }
                return false;
            case KeyEvent.KEYCODE_BUTTON_A:
            case KeyEvent.KEYCODE_ENTER:
                // Hex window
                if ( !moveByArrow )
                {
                    global.xyHex.x = global.getPosition();
                    global.xyHex.y = global.getPosition();
                    invalidate();
                    moveByArrow = true;
                    return true;
                }
                return false;
            case KeyEvent.KEYCODE_BUTTON_B:
                moveByArrow = false;
                global.xyHex.clear();
                invalidate();
                return true;
            default:
                return super.onKeyUp( keyCode, event );
        }
    }
    
    
    /**
     * The current activity inflated view
     *
     * @param activity
     */
    public void setActivity( Activity activity )
    {
        this.activity = activity;
    }
    
    /**
     * Main Touch handler
     *
     * @param v
     * @param event
     *
     * @return
     */
    @Override
    public boolean onTouch( View v, MotionEvent event )
    {
        switch ( event.getAction() )
        {
            case MotionEvent.ACTION_UP:
                fingerDown = false;
                break;
            case MotionEvent.ACTION_DOWN:
                fingerDown = false;
            case MotionEvent.ACTION_MOVE:
                if ( fingerDown )
                {
                    int yPosi = ( int ) event.getY() / (global.fntHeight * 2);
                    
                    long temp = getXYPosition( event ).x;
                    if ( temp != -1 )
                    {
                        global.xyHex.y = temp;
                        // Scroll if needed
                        if ( yPosi < 0 )
                        {
                            global.setPosition( global.getPosition() - global.columnCount );
                        }
                        else if ( yPosi > global.rowCount )
                        {
                            global.setPosition( global.getPosition() + global.columnCount );
                        }
                        
                        //
                        TextView tv = activity.findViewById( R.id.byteCount );
                        if ( tv.getVisibility() == View.GONE )
                        {
                            tv.setVisibility( View.VISIBLE );
                        }
                        //
                        int byteCount = ( int ) Math.abs( (global.xyHex.y - global.xyHex.x) ) + 1;
                        tv.setText( "Bytes selected: " + byteCount );
                        invalidate();
                    }
                }
            default:
                break;
        }
        
        scaleDetector.onTouchEvent( event );
        Detector.onTouchEvent( event );
        return true;
    }
    
    /**
     * Handle Presses, Taps, etc...
     *
     * @param e
     *
     * @return
     */
    @Override
    public boolean onDown( MotionEvent e )
    {
        if ( !viewScroller.isFinished() )
        {
            viewScroller.forceFinished( true );
            // There is also a compatibility version:
            ViewCompat.postInvalidateOnAnimation( this );
            return true;
        }
        //
        return true;
    }
    
    
    @Override
    public void onShowPress( MotionEvent e )
    {
        
    }
    
    /**
     * Simplify data retrieval for multi call use
     *
     * @param e Motion / Touch data
     *
     * @return address in ROM data where a touch took place
     */
    private PointXYZ getXYPosition( MotionEvent e )
    {
        int    WidthofHex  = ( int ) global.paint.measureText( "   " );
        int    Height      = (global.fntHeight * 2);
        int    xPosi;
        int    yPosi;
        int    maxHexWidth;
        int    maxTextWidth;
        int    offsetWidth = ( int ) global.paint.measureText( String.format( "%0" + global.ofsTextWidth.x + "X", 0 ) );
        int    hexWidth    = ( int ) global.paint.measureText( "00 " );
        long   x           = -1;
        int    curWidth    = 0;
        int    dual        = 0;
        int    value;
        String s           = "";
        
        
        xPosi = ( int ) e.getX() - offsetWidth;
        yPosi = ( int ) e.getY() / Height;
        maxHexWidth = (hexWidth * global.columnCount);
        //        maxTextWidth = (( int ) global.scrollThumb.getX() - offsetWidth);
        maxTextWidth = (getWidth() - offsetWidth);
        
        
        //################################
        //
        // Clicked over Hex values
        //
        //################################
        if ( xPosi >= 0 && xPosi < maxHexWidth )
        {
            xPosi /= WidthofHex;
            //
            x = (xPosi + (yPosi * global.columnCount) + global.getPosition());
            dual = 0;
        }
        //################################
        //
        // Clicked over Text values
        //
        //################################
        else if ( xPosi > maxHexWidth && xPosi < maxTextWidth )
        {
            xPosi -= maxHexWidth;
            
            if ( xPosi == 0 )
            {
                return new PointXYZ( xPosi + (yPosi * global.columnCount) + global.getPosition(), xPosi + (yPosi * global.columnCount) + global.getPosition(), 1 );
            }
            
            // Get the data from the ROM file
            rom.fread( iRom, (yPosi * global.columnCount) + global.getPosition(), global.columnCount );
            // Determine which entry has text at the current location
            x = 0;
            while ( curWidth < xPosi && x < global.columnCount )
            {
                value = iRom[ ( int ) x ] & 0xFF;
                dual = -1;
                //
                if ( (x + 1) < global.columnCount )
                {
                    dual = (iRom[ ( int ) x + 1 ] & 0xFF);
                    dual = ((value << 8) + dual) & 0xFFFF;
                }
                
                // Select text string to append with
                if ( dual != -1 && !global.twoCode[ dual ].equals( "" ) )
                {   // Using dual codes. select both bytes
                    s = s + global.twoCode[ dual ];
                    x++;
                    dual = 2;
                }
                else if ( !global.oneCode[ value ].equals( "" ) )
                {   // Using single codes
                    s = s + global.oneCode[ value ];
                    dual = 1;
                }
                else
                {
                    s = s + ".";
                    dual = 1;
                }
                
                // Get the width, and test it's position to see if it needs to be
                // returned as the text position
                curWidth = ( int ) global.paint.measureText( s );
                if ( curWidth >= xPosi )
                {
                    return new PointXYZ( x + (yPosi * global.columnCount) + global.getPosition(), x + (yPosi * global.columnCount) + global.getPosition(), dual );
                }
                
                x++;
            }
            
            //
            x = ((global.columnCount - 1) + (yPosi * global.columnCount) + global.getPosition());
            
            dual = 1;
        }
        
        return new PointXYZ( x, x, dual );
    }
    
    
    /**
     * Get X, Y position for data highlighting
     *
     * @param e Motion / Touch data
     *
     * @return
     */
    @Override
    public boolean onSingleTapUp( MotionEvent e )
    {
        PointXYZ temp = getXYPosition( e );
        
        if ( temp.x != -1 )
        {
            if ( temp.z == 2 )
            {   // Dual byte was clicked
                global.xyHex = new PointXYZ( temp.x - 1, temp.y, global.getPosition() );
                temp.z = 1;
            }
            else
            {   // Standard data was clicked
                global.xyHex = new PointXYZ( temp.x, temp.y, global.getPosition() );
            }
            
            // Slide in the edit toolbar
            //
            invalidate();
            
            TextView tv = activity.findViewById( R.id.byteCount );
            tv.setVisibility( View.GONE );
            
            View normalLayout = activity.findViewById( R.id.bottom_toolbar );
            View editLayout   = activity.findViewById( R.id.bottom_hexbar );
            //
            normalLayout.setVisibility( View.GONE );
            normalLayout = activity.findViewById( R.id.bottom_editbar );
            normalLayout.setVisibility( View.GONE );
            //
            global.bottomBarMode = 2 + ( int ) temp.z;
            
            if ( temp.z > 0 )
            {   // Text data was clicked
                editLayout.setVisibility( View.GONE );
                editLayout = activity.findViewById( R.id.bottom_textbar );
                editLayout.setVisibility( VISIBLE );
            }
            else
            {   // Hex data was clicked
                editLayout.setVisibility( View.VISIBLE );
                editLayout = activity.findViewById( R.id.bottom_textbar );
                editLayout.setVisibility( View.GONE );
            }
            //
            moveByArrow = true;
            
            //
            HexKeyboard.testHexInput( rom, global );
            
            //
            rom.curOffset = -1;
            rom.curIndex = -1;
            rom.oldOffset = -1;
        }
        
        fingerDown = false;
        return false;
    }
    
    
    /**
     * @param e
     */
    @Override
    public void onLongPress( MotionEvent e )
    {
        //        onSingleTapUp( e );
        PointXYZ temp = getXYPosition( e );
        
        if ( temp.x != -1 )
        {
            global.xyHex = new PointXYZ( temp.x, temp.y, global.getPosition() );
            // Slide in the edit toolbar
            //
            invalidate();
            
            TextView tv = activity.findViewById( R.id.byteCount );
            tv.setVisibility( View.GONE );
            
            View normalLayout = activity.findViewById( R.id.bottom_toolbar );
            normalLayout.setVisibility( View.GONE );
            normalLayout = activity.findViewById( R.id.bottom_hexbar );
            normalLayout.setVisibility( View.GONE );
            normalLayout = activity.findViewById( R.id.bottom_textbar );
            normalLayout.setVisibility( View.GONE );
            
            View editLayout = activity.findViewById( R.id.bottom_editbar );
            editLayout.setVisibility( View.VISIBLE );
            //
            global.bottomBarMode = 1;
            moveByArrow = true;
            
            //
            HexKeyboard.testHexInput( rom, global );
            
            rom.curOffset = -1;
            rom.curIndex = -1;
            rom.oldOffset = -1;
        }
        
        fingerDown = true;
    }
    
    
    /**
     * Handle all scrolling
     *
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     *
     * @return
     */
    @Override
    public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY )
    {
        boolean canScroll = false;
        
        if ( global.columnCount < 0 )
        {
            return false;
        }
        //
        
        if ( distanceY < 0 )
        {
            global.iScrollDown = 0;
            global.iScrollUp += Math.abs( distanceY );
            if ( global.iScrollUp > 20.0f )
            {
                global.setPosition( global.getPosition() - global.columnCount );
                global.iScrollUp = 0;
                canScroll = true;
            }
        }
        else if ( distanceY > -1 )
        {
            global.iScrollUp = 0;
            global.iScrollDown += Math.abs( distanceY );
            if ( global.iScrollDown > 20.0f )
            {
                global.setPosition( global.getPosition() + global.columnCount );
                global.iScrollDown = 0;
                canScroll = true;
            }
        }
        //
        if ( canScroll )
        {
            this.invalidate();
        }
        //
        return canScroll;
    }
    
    
    /**
     * Flinger for hex scrolling
     *
     * @param e1
     * @param e2
     * @param velocityX
     * @param velocityY
     *
     * @return
     */
    @Override
    public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY )
    {
        // Before flinging, abort the current animation.
        viewScroller.forceFinished( true );
        // Begin the scroll animation
        viewScroller.fling(
                // Current scroll position
                0, 0, 0, ( int ) -velocityY, 0, 0, -10000, ( int ) (global.fileSize + 10000), 0, 0 );
        
        // Invalidate to trigger computeScroll()
        ViewCompat.postInvalidateOnAnimation( this );
        return true;
    }
    
    
    /**
     * //################################
     * <p>
     * Results from a fling
     * <p>
     * //################################
     */
    @Override
    public void computeScroll()
    {
        // Compute the current scroll offsets. If this returns true, then the
        // scroll has not yet finished.
        if ( viewScroller.computeScrollOffset() )
        {
            int directionY = (viewScroller.getStartY() - viewScroller.getFinalY());
            int currV      = ( int ) viewScroller.getCurrVelocity();
            int adjustment = (currV / 1000) * global.columnCount;
            
            
            // Actually render the scrolled viewport
            if ( directionY < 0 )
            {
                global.setPosition( global.getPosition() + adjustment );
                /*                global.setPosition( global.getPosition() + (global.columnCount * (currV / 500)) );*/
            }
            
            if ( directionY > -1 )
            {
                global.setPosition( global.getPosition() - adjustment );
                //                global.setPosition( global.getPosition() - (global.columnCount * (currV / 500)) );
            }

            global.isScrolling = true;
            // view using View.scrollTo.
            ViewCompat.postInvalidateOnAnimation( this );
        }
        else
        {
            global.isScrolling = false;
        }
    }
    
    
    /**
     * Handle Margin alignment
     *
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom )
    {
        super.onLayout( changed, left, top, right, bottom );
        //
        updateLayout( false );
    }
    
    
    /**
     * Update the screen settings
     */
    public void updateLayout( boolean alreadyHere )
    {
        // set the dialog components - text, image and button
        View offsetDivider;
        View textDivider;
        
        offsetDivider = activity.findViewById( R.id.divOffset );
        textDivider = activity.findViewById( R.id.divText );
        
/*
        ConstraintLayout.LayoutParams lp1, lp2;
        lp1 = ( ConstraintLayout.LayoutParams ) v1.getLayoutParams();
        lp2 = ( ConstraintLayout.LayoutParams ) v2.getLayoutParams();
*/
        
        
        int iWidth     = ( int ) global.paint.measureText( "0" );
        int iWinWidth  = getWidth();
        int baseX;
        int hexStartX  = 0;
        int textStartX = 0;
        
        global.columnCount = 0;
        global.rowCount = (getHeight() / global.fntHeight) / 2;
        baseX = (iWidth * ( int ) global.ofsTextWidth.x) + iWidth;
        
        
        //######################################
        //
        // Get the Hex and Text window widths
        //
        //######################################
        while ( (baseX + hexStartX + textStartX + iWidth) < iWinWidth )
        {
            hexStartX += (iWidth * 2) + iWidth;
            textStartX += iWidth;
            global.columnCount++;
            //
/*
            if ( global.columnCount >= 16 )
            {
                global.columnCount = 16;
                break;
            }
*/
            //
        }
        
        //#######################################
        //
        // Make sure there is no ASCII overflow.
        // Table files not truly controllable
        //
        //#######################################
        if ( (baseX + hexStartX + textStartX + iWidth) > iWinWidth )
        {
            global.columnCount--;
            hexStartX -= (iWidth * 2) + iWidth;
        }
        
        
        //################################
        //
        // Set Hex layout margin
        //
        //################################
        float xPosition = (iWidth * ( int ) global.ofsTextWidth.x) + (iWidth / 2f);
        
        offsetDivider.setX( xPosition );
        textDivider.setX( xPosition + hexStartX );
        
        // Force an update
        offsetDivider.invalidate();
        textDivider.invalidate();
        
        // Calculate screen size
        global.screenSize = (global.columnCount * global.rowCount);
        
        //
        global.iHeight = getHeight();
        
        
        //##############################
        //
        // Adjust scroll bar increments
        //
        //##############################
        if ( scrollBar != null && global.screenSize > 0 )
        {
            scrollBar.setMax( ( int ) (global.fileSize / global.screenSize) );
        }
    }
    
    
    /**
     * //###############################################
     * <p>
     * Must have a Font height. Need context
     * to get one if no height present
     * All this information is for this class anyways
     * <p>
     * //###############################################
     */
    public void initHexViewer( Globals global )
    {
        if ( global.fntHeight == 0 )
        {
            global.fntHeight = getResources().getDimensionPixelSize( R.dimen._10sdp );
        }
        
        // custom drawing code here     Paint settings_menu for text display
        global.textPaint.setAntiAlias( true );
        global.textPaint.setTextSize( global.fntHeight );
        global.textPaint.setTypeface( Typeface.MONOSPACE );
        
        // Paint settings_menu for Hex, Offset text
        global.paint.setStyle( Paint.Style.FILL );
        global.paint.setTextSize( global.fntHeight );
        // Set the paint font
        //        paint.setTypeface( font );
        global.paint.setTypeface( Typeface.create( Typeface.MONOSPACE, Typeface.BOLD ) );
        
        // Default table data
        global.buildAsciiTable();
        //
        //        global.fileSize = 0;
        global.ofsTextWidth = new PointXYZ( 8, 10, 0 );
        global.txtX = 0;
        global.chrWidth = ( int ) global.paint.measureText( "WW" );
        //
        
        global.isInitialized = true;
        global.lowerBarVisible = false;
/*
        global.currentTheme = 0;
        global.settingsFlag = 0;
*/
        global.romModified = false;
        global.xyHex.x = -1;
        global.xyHex.y = -1;
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
        
        int colorPrimary;
        int color;
        int yPos;
        //
        String code;
        int    codeColor;
        
        
        global.iHeight = getHeight();
        bgPaint.setColor( Color.BLUE );
        
        //
        // Error checking for positions
        //
        //        if ( (global.Position + ((global.iHeight / global.fntHeight) * global.rowSize)) > global.fileSize )
        if ( (global.getPosition() + (global.rowCount * global.columnCount)) > global.fileSize )
        {
            global.setPosition( ( int ) (global.fileSize - (global.rowCount * global.columnCount)) );
        }
        
        if ( global.getPosition() < 0 )
        {
            global.setPosition( 0 );
        }
        
        
        //######################################
        //
        // Draw each row that is visible
        //
        //######################################
        
        // Primary color
        getContext().getTheme().resolveAttribute( R.attr.hexColorPrimary, a, true );
        colorPrimary = ContextCompat.getColor( getContext(), a.resourceId );
        
        // Secondary color
        getContext().getTheme().resolveAttribute( R.attr.hexColorSecondary, a, true );
        color = ContextCompat.getColor( getContext(), a.resourceId );
        
        /*        for ( int  y = 0; y < global.colSize; y++ )*/
        for ( int y = 0; y < global.rowCount; y++ )
        {
            //
            if ( (global.getPosition() + (y * global.columnCount) >= global.fileSize) )
            {   // No need to continue;
                break;
            }
            
            // Get the data from the ROM file
            rom.fread( iRom, global.getPosition() + (y * global.columnCount), global.columnCount );
            
            //
            yPos = (y + y);
            //
            // Draw the offset
            //
            global.paint.setColor( colorPrimary );
            
            s = String.format( "%0" + global.ofsTextWidth.x + "X ", global.getPosition() + (y * global.columnCount) );
            canvas.drawText( s, (global.txtX = 0), ((1 + yPos) * global.fntHeight), global.paint );
            //
            // Move 'X' over
            global.txtX = ( int ) global.paint.measureText( s );
            
            //------------------------------------------------//
            //
            // Draw Hex values
            //
            //------------------------------------------------//
            global.paint.setColor( colorPrimary );
/*
            if ( (y & 1) == 1 )
            {
                global.paint.setColor( color );
            }
            else
            {
                global.paint.setColor( colorPrimary );
            }
*/
            
            //#################################################
            //
            // Display hex string
            //
            //#################################################
            for ( int x = 0; x < global.columnCount; x++ )
            {
                if ( (global.getPosition() + (y * global.columnCount) + x) >= global.fileSize )
                {
                    dataStr.append( String.format( "%" + ((global.columnCount - x) * 3) + "c", 0x20 ) );
                    break;
                }
                //
                //----------------------------------
                //
                dataStr.append( String.format( "%02X ", iRom[ x ] ) );
                //                if ( (global.Position + (y * global.rowSize) + x) == global.xyHex )
                if ( global.valueBetween( (global.getPosition() + (y * global.columnCount) + x), global.xyHex.x, global.xyHex.y ) )
                {
                    hexRect.left = global.txtX + (x * ( int ) global.paint.measureText( "   " ));
                    hexRect.top = (yPos * global.fntHeight);
                    hexRect.right = (hexRect.left + global.chrWidth);
                    hexRect.bottom = (hexRect.top + global.fntHeight) + (global.fntHeight / 4);
                    hexRect.top -= (global.fntHeight / 4);
                    canvas.drawRect( hexRect, bgPaint );
                    
                    // Hex modification
                    if ( rom.curIndex == 1 && global.valueBetween( rom.curOffset, global.xyHex.x, global.xyHex.y ) )
                    {
                        hexRect.right = (hexRect.left + (global.chrWidth / 2));
                        bgPaint.setColor( Color.RED );
                        canvas.drawRect( hexRect, bgPaint );
                        bgPaint.setColor( Color.BLUE );
                    }
                }
            }
            
            //
            canvas.drawText( dataStr.toString(), global.txtX, ((1 + yPos) * global.fntHeight), global.paint );
            
            
            // Move pen to next X position
            global.txtX += ( int ) global.paint.measureText( dataStr.toString() );
            // Erase current contents
            dataStr.delete( 0, dataStr.length() );
            
            //#################################################
            //
            // Draw Text values
            //
            //#################################################
            global.paint.setColor( colorPrimary );
            //
            for ( int x = 0; x < global.columnCount; x++ )
            {
                if ( (global.getPosition() + (y * global.columnCount) + x) >= global.fileSize )
                {
                    break;
                }
                
                int value = (iRom[ x ] & 0xFF);
                int dual;
                
                if ( (x + 1) < global.columnCount && (global.getPosition() + (y * global.columnCount) + x + 1) < global.fileSize )
                {
                    dual = (iRom[ x + 1 ] & 0xFF);
                    dual = ((value << 8) + dual) & 0xFFFF;
                    //
                    code = global.twoCode[ dual ];
                    codeColor = global.twoColor[ dual ];
                    //
                    if ( code == "" )
                    {
                        code = global.oneCode[ value ];
                        codeColor = global.oneColor[ value ];
                        dual = 0;
                    }
                    else
                    {
                        dual = 1;
                    }
                }
                else
                {
                    code = global.oneCode[ value ];
                    codeColor = global.oneColor[ value ];
                    dual = 0;
                }
                
                
                global.paint.setColor( codeColor );
                //
                //                if ( (global.Position + (y * global.rowSize) + x) == global.xyHex )
                //
                if ( code != "" )
                {
                    if ( global.valueBetween( (global.getPosition() + (y * global.columnCount) + x), global.xyHex.x, (global.xyHex.y - dual) ) )
                    {
                        textRect.left = global.txtX;
                        textRect.top = (yPos * global.fntHeight);
                        textRect.right = (textRect.left + ( int ) global.paint.measureText( code ));
                        textRect.bottom = (textRect.top + global.fntHeight) + (global.fntHeight / 4);
                        textRect.top -= (global.fntHeight / 4);
                        //
                        canvas.drawRect( textRect, bgPaint );
                    }
                    //
                    canvas.drawText( code, global.txtX, ((1 + yPos) * global.fntHeight), global.paint );
                    global.txtX += ( int ) global.paint.measureText( code );
                }
                else
                {
                    if ( global.valueBetween( (global.getPosition() + (y * global.columnCount) + x), global.xyHex.x, global.xyHex.y ) )
                    {
                        textRect.left = global.txtX;
                        textRect.top = (yPos * global.fntHeight);
                        textRect.right = (textRect.left + (global.chrWidth / 2));
                        textRect.bottom = (textRect.top + global.fntHeight) + (global.fntHeight / 4);
                        textRect.top -= (global.fntHeight / 4);
                        //
                        canvas.drawRect( textRect, bgPaint );
                    }
                    //
                    canvas.drawText( ".", global.txtX, ((1 + yPos) * global.fntHeight), global.paint );
                    global.txtX += (global.chrWidth / 2);
                }
                
                x += dual;
            }
            
            //
            // Display text string
            //
            //            canvas.drawText( dataStr.toString(), global.txtX, ((1 + yPos) * global.fntHeight), global.paint );
            // Clear the text buffer
            dataStr.delete( 0, dataStr.length() );
        }
    }
}

