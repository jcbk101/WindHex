/**
 * Copyright 2013 Maarten Pennings
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * If you use this software in a product, an acknowledgment in the product
 * documentation would be appreciated but is not required.
 */

package com.GenesysEast.windhex.Keyboard;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.widget.LinearLayout;

import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.GlobalClasses.PointXYZ;
import com.GenesysEast.windhex.GlobalClasses.RomFileAccess;
import com.GenesysEast.windhex.Hex_Drawer.DrawHexView;
import com.GenesysEast.windhex.WindHexActivity;

/**
 * When an activity hosts a keyboardView, this class allows several EditText's to register for it.
 *
 * @author Maarten Pennings
 * @date 2012 December 23
 */
public class HexKeyboard

{
    private        Globals       global = Globals.getInstance();
    private        RomFileAccess rom    = RomFileAccess.getInstance();
    private        KeyboardView  mKeyboardView;
    private static Activity      activity;
    private        Context       mContext;
    private        DrawHexView   hView;
    private        LinearLayout  av;
    
    
    /**
     * Get access to the Hex Drawer Class
     *
     * @param hView
     */
    public void sethView( DrawHexView hView )
    {
        this.hView = hView;
    }
    
    
    /**
     * The key (code) handler.
     */
    private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener()
    {
        
        final static int CodeCancel = -3; // Keyboard.KEYCODE_CANCEL
        final static int CodeUp = 16;
        final static int CodeDown = 17;
        final static int CodeLeft = 18;
        final static int CodeRight = 19;
        
        @Override
        public void onKey( int primaryCode, int[] keyCodes )
        {   // Wait until scrolling is done
            if ( global.isScrolling )
            {
                return;
            }
            
            if ( primaryCode == CodeLeft )
            {
                codeLeft( hView );
            }
            else if ( primaryCode == CodeRight )
            {
                codeRight( hView );
            }
            else if ( primaryCode == CodeUp )
            {
                codeUp( hView );
            }
            else if ( primaryCode == CodeDown )
            {
                codeDown( hView );
            }
            else
            {   // insert character
                if ( inputHex( hView, primaryCode ) )
                {
                    // Align screen with current offsets
                    if ( global.xyHex.x >= (global.getPosition() + (global.columnCount * global.rowCount)) )
                    {
                        global.setPosition( global.getPosition() + global.columnCount );
                        global.xyHex.z = global.getPosition();
                        hView.invalidate();
                    }
                    
                    // Reset data
                    rom.curByte = -1;
                    rom.curIndex = -1;
                    rom.curOffset = -1;
                    rom.oldOffset = -1;
                }
            }
        }
        
        
        @Override
        public void onPress( int arg0 )
        {
        }
        
        @Override
        public void onRelease( int primaryCode )
        {
        }
        
        @Override
        public void onText( CharSequence text )
        {
        }
        
        @Override
        public void swipeDown()
        {
        }
        
        @Override
        public void swipeLeft()
        {
        }
        
        @Override
        public void swipeRight()
        {
        }
        
        @Override
        public void swipeUp()
        {
        }
    };
    
    /**
     * Create a custom keyboard, that uses the KeyboardView (with resource id <var>viewid</var>) of the <var>host</var> activity,
     * and load the keyboard layout from xml file <var>layoutid</var> (see {@link Keyboard} for description).
     * Note that the <var>host</var> activity must have a <var>KeyboardView</var> in its layout (typically aligned with the bottom of the activity).
     * Note that the keyboard layout xml file may include key codes for navigation; see the constants in this class for their values.
     *
     * @param activity The hosting activity.
     * @param viewid   The id of the KeyboardView.
     * @param layoutid The id of the xml file containing the keyboard layout.
     */
    public HexKeyboard( Activity activity, int viewid, int layoutid, int barHeight, int barWidth )
    {
        this.activity = activity;
        this.mContext = activity.getApplicationContext();
        mKeyboardView = activity.findViewById( viewid );
        mKeyboardView.setKeyboard( new Keyboard( activity, layoutid,0, barWidth, barHeight ) );
        
/*
        // Set up key sizes
        int width = barWidth;
        int height = barHeight;
*/

        mKeyboardView.setPreviewEnabled( false ); // NOTE Do not show the preview balloons
        mKeyboardView.setOnKeyboardActionListener( mOnKeyboardActionListener );
    }
    
    /**
     * @param rom
     * @param global
     */
    public static void testHexInput( RomFileAccess rom, Globals global )
    {
        if ( rom.curIndex == 1 && rom.curOffset != global.xyHex.x )
        {   // Restore old data
            rom.fseek( rom.oldOffset );
            rom.fputc( rom.oldData );
            rom.curIndex = -1;
            rom.curOffset = -1;
        }
    }
    
    
    /**
     * //#########################################
     * <p>
     * Global access to Left
     * <p>
     * //#########################################
     */
    public static void codeLeft( DrawHexView hView )
    {
        Globals       global = Globals.getInstance();
        RomFileAccess rom    = RomFileAccess.getInstance();
        
        if ( global.xyHex.x > 0 )
        {
            global.xyHex.x--;
        }
        
        //
        if ( global.xyHex.x < global.getPosition() )
        {
            global.setPosition( global.getPosition() - global.columnCount );
        }
        
        global.xyHex.y = global.xyHex.x;
        //
        if ( global.xyHex.x < global.getPosition() || global.xyHex.x > (global.getPosition() + (global.columnCount * global.rowCount)) )
        {
            global.setPosition( (global.xyHex.x / global.columnCount) * global.columnCount );
        }
        //
        HexKeyboard.testHexInput( rom, global );
        //
        global.xyHex.z = global.getPosition();
        hView.invalidate();
    }
    
    /**
     * //#########################################
     * <p>
     * Global access to Right
     * <p>
     * //#########################################
     */
    public static void codeRight( DrawHexView hView )
    {
        Globals       global = Globals.getInstance();
        RomFileAccess rom    = RomFileAccess.getInstance();
        
        global.xyHex.x++;
        if ( global.xyHex.x >= (global.getPosition() + (global.columnCount * global.rowCount)) )
        {
            global.setPosition( global.getPosition() + global.columnCount );
        }
        //
        if ( global.xyHex.x < global.getPosition() || global.xyHex.x > (global.getPosition() + (global.columnCount * global.rowCount)) || global.xyHex.x >= rom.fileSize )
        {
            global.setPosition( (global.xyHex.x / global.columnCount) * global.columnCount );
            global.setPosition( global.getPosition() - (global.columnCount * global.rowCount) - global.columnCount );
            global.xyHex.x--;
        }
        //
        HexKeyboard.testHexInput( rom, global );
        //
        global.xyHex.y = global.xyHex.x;
        global.xyHex.z = global.getPosition();
        hView.invalidate();
    }
    
    /**
     * //#########################################
     * <p>
     * Global access to Up
     * <p>
     * //#########################################
     */
    public static void codeUp( DrawHexView hView )
    {
        Globals       global = Globals.getInstance();
        RomFileAccess rom    = RomFileAccess.getInstance();
        
        if ( (global.xyHex.x - global.columnCount) > 0 )
        {
            global.xyHex.x -= global.columnCount;
            
            if ( global.xyHex.x < global.getPosition() )
            {
                global.setPosition( global.getPosition() - global.columnCount );
            }
            global.xyHex.y = global.xyHex.x;
            //
            if ( global.xyHex.x < global.getPosition() || global.xyHex.x > (global.getPosition() + (global.columnCount * global.rowCount)) )
            {
                global.setPosition( (global.xyHex.x / global.columnCount) * global.columnCount );
            }
            //
            HexKeyboard.testHexInput( rom, global );
            //
            global.xyHex.z = global.getPosition();
            hView.invalidate();
        }
    }
    
    /**
     * //#########################################
     * <p>
     * Global access to Down
     * <p>
     * //#########################################
     */
    public static void codeDown( DrawHexView hView )
    {
        Globals       global = Globals.getInstance();
        RomFileAccess rom    = RomFileAccess.getInstance();
        
        
        if ( (global.xyHex.x + global.columnCount) < rom.fileSize )
        {
            global.xyHex.x += global.columnCount;
            if ( global.xyHex.x >= (global.getPosition() + (global.columnCount * global.rowCount)) )
            {
                global.setPosition( global.getPosition() + global.columnCount );
            }
            //
            if ( global.xyHex.x < global.getPosition() || global.xyHex.x > (global.getPosition() + (global.columnCount * global.rowCount)) )
            {
                global.setPosition( (global.xyHex.x / global.columnCount) * global.columnCount );
                global.setPosition( global.getPosition() - (global.columnCount * global.rowCount) - global.columnCount );
                global.xyHex.x -= global.columnCount;
            }
            //
            HexKeyboard.testHexInput( rom, global );
            //
            global.xyHex.y = global.xyHex.x;
            global.xyHex.z = global.getPosition();
            hView.invalidate();
        }
    }
    
    
    /**
     * //#########################################
     * <p>
     * Global access to Up
     * <p>
     * //#########################################
     */
    public static void pageUp( DrawHexView hView )
    {
        Globals       global = Globals.getInstance();
        RomFileAccess rom    = RomFileAccess.getInstance();
        
        if ( (global.xyHex.x - global.pageSize) > 0 )
        {
            global.xyHex.x -= global.pageSize;
            
            if ( global.xyHex.x < global.getPosition() )
            {
                global.setPosition( global.getPosition() - global.pageSize );
            }
            global.xyHex.y = global.xyHex.x;
            //
            if ( global.xyHex.x < global.getPosition() || global.xyHex.x > (global.getPosition() + (global.columnCount * global.rowCount)) )
            {
                global.setPosition( (global.xyHex.x / global.columnCount) * global.columnCount );
            }
            //
            HexKeyboard.testHexInput( rom, global );
            //
            global.xyHex.z = global.getPosition();
            hView.invalidate();
        }
    }
    
    
    /**
     * //#########################################
     * <p>
     * Global access to Down
     * <p>
     * //#########################################
     */
    public static void pageDown( DrawHexView hView )
    {
        Globals       global = Globals.getInstance();
        RomFileAccess rom    = RomFileAccess.getInstance();
        
        if ( (global.xyHex.x + global.pageSize) < rom.fileSize )
        {
            global.xyHex.x += global.pageSize;
            if ( global.xyHex.x >= (global.getPosition() + (global.columnCount * global.rowCount)) )
            {
                global.setPosition( global.getPosition() + global.pageSize );
            }
            //
            if ( global.xyHex.x < global.getPosition() || global.xyHex.x > (global.getPosition() + (global.columnCount * global.rowCount)) )
            {
                global.setPosition( (global.xyHex.x / global.columnCount) * global.columnCount );
                global.setPosition( global.getPosition() - (global.columnCount * global.rowCount) - global.columnCount );
                global.xyHex.x -= global.columnCount;
            }
            //
            HexKeyboard.testHexInput( rom, global );
            //
            global.xyHex.y = global.xyHex.x;
            global.xyHex.z = global.getPosition();
            hView.invalidate();
        }
    }
    
    /**
     * //#########################################
     * <p>
     * Allow global Hex input from a keyboard
     * <p>
     * //#########################################
     *
     * @param primaryCode
     */
    public static boolean inputHex( DrawHexView hView, int primaryCode )
    {
        Globals       global = Globals.getInstance();
        RomFileAccess rom    = RomFileAccess.getInstance();
        
        if ( primaryCode > -1 && primaryCode < 16 )
        {
            int data;
            
            if ( rom.fileSize <= 0 || global.xyHex.x > rom.fileSize )
            {
                return false;
            }
            
            //
            if ( rom.curOffset == global.xyHex.x && rom.curIndex == 1 )
            {   // Writing second nibble
                if ( global.getPosition() != global.xyHex.z )
                {
                    global.setPosition( global.xyHex.z );
                    hView.invalidate();
                }
                //
                rom.curByte &= 0xF0;
                rom.curByte |= primaryCode;
                //
                rom.fseek( global.xyHex.x );
                rom.fputc( ( int ) rom.curByte );
                //
                // Create an UNDO entry
                //
                rom.undoList.add( new PointXYZ( global.xyHex.x, global.xyHex.y, global.getPosition(), 1 ) );
                
                WindHexActivity.updateUndoButton( activity, rom );
                //
                global.xyHex.x++;
                global.xyHex.y++;
                hView.invalidate();
                return true;
            }
            else if ( rom.curOffset == -1 && rom.curIndex == -1 )
            {   // Write first nibble
                if ( global.getPosition() != global.xyHex.z )
                {
                    global.setPosition( global.xyHex.z );
                    hView.invalidate();
                }
                //
                rom.curOffset = global.xyHex.x;
                rom.curIndex = 1;
                rom.fseek( global.xyHex.x );
                rom.curByte = rom.fgetc();
                
                // Change stuff back on move cursor
                rom.oldData = rom.curByte;
                rom.oldOffset = rom.curOffset;
                //
                data = (rom.curByte & 0x0F) | (primaryCode << 4);
                rom.curByte = ( byte ) data;
                //
                rom.fseek( global.xyHex.x );
                rom.fputc( rom.curByte );
                //
                hView.invalidate();
            }
        }
        
        return false;
    }
    
}