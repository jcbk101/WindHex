package com.GenesysEast.windhex.GlobalClasses;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.*;
import java.nio.channels.FileChannel;

public class InsertData
{
    private RomFileAccess rom    = RomFileAccess.getInstance();
    private Globals       global = Globals.getInstance();
    private Context       mContext;
    
    /**
     * Constructor
     *
     * @param mContext
     */
    public InsertData( Context mContext )
    {
        this.mContext = mContext;
    }
    
    
    /**
     * Method to insert text data from a file
     *
     * @param mContext
     * @param fileName
     * @param Offset
     *
     * @return
     */
    public int TextFileInsert( Context mContext, File fileName, long Offset, boolean mode )
    {
        boolean         wasSuccessful = true;
        long            offset        = Offset;
        long            temp          = 0;
        int             byteCount     = 0;
        FileInputStream inStream      = null;
        
        try
        {
            InputStreamReader strReader;
            BufferedReader    bufReader;
            String            str;
            
            inStream = new FileInputStream( fileName );
            strReader = new InputStreamReader( inStream );
            bufReader = new BufferedReader( strReader );
            
            rom.fseek( offset );
            //
            while ( (str = bufReader.readLine()) != null )
            {
                if ( (temp = pasteUsingText( str, offset, mode )) == -1 )
                {
                    wasSuccessful = false;
                    break;
                }
                offset += temp;
                byteCount += temp;
            }
            //
            inStream.close();
            
            //
            if ( wasSuccessful )
            {
                return byteCount;
            }
            
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    
    /**
     * Method to insert raw data from a file
     *
     * @param mContext
     * @param fileName
     * @param Offset
     *
     * @return
     */
    public int DataFileInsert( final Context mContext, File fileName, long Offset )
    {
        if ( (fileName.length() + Offset) <= global.fileSize )
        {
            RandomAccessFile infile;
            FileChannel      inChannel;
            FileChannel      outChannel;
            
            try
            {
                infile = new RandomAccessFile( fileName, "r" );
                inChannel = infile.getChannel();
                outChannel = rom.rf.getChannel();
                
                if ( inChannel != null && outChannel != null )
                {
                    long size     = inChannel.size();
                    long position = 0;
                    long count    = 0x10000;
                    
                    // Set the written to file's starting position
                    outChannel.position( Offset );
                    
                    while ( position < size )
                    {
                        position += inChannel.transferTo( position, count, outChannel );
                    }
                }
                //
                infile.close();
                return ( int ) fileName.length();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
        else
        {
            toastThreadHelper( mContext, "Error: Data extends beyond 'End of File'" );
        }
        
        return -1;
    }
    
    
    /**
     * Basic text string insert
     *
     * @param mContext
     * @param textData
     * @param Offset
     *
     * @return
     */
    public int TextInsert( Context mContext, String textData, long Offset, boolean mode )
    {
        if ( textData == null )
        {
            return -1;
        }
        
        return ( int ) pasteUsingText( textData, Offset, mode );
    }
    
    /**
     * Using a text string, paste text to current file location
     * diectly to ROM file
     *
     * @param textString
     * @param startOffset
     *
     * @return
     */
    public long pasteUsingHex( String textString, long startOffset )
    {
        byte[] buffer = null;
        
        if ( textString != null )
        {
            buffer = Hex2Byte( textString );
            
            // Try to insert the data
            if ( buffer.length > 0 && !rom.dataWrite( buffer, startOffset, buffer.length ) )
            {
                //                Toast.makeText( mContext, "Error: Data extends beyond 'End of File'", Toast.LENGTH_SHORT ).show();
                toastThreadHelper( mContext, "Error: Data extends beyond 'End of File'" );
                return -1;
            }
        }
        else
        {
            toastThreadHelper( mContext, "String is empty" );
            //            Toast.makeText( mContext, "String is empty", Toast.LENGTH_SHORT ).show();
            return -1;
        }
        
        return buffer.length;
    }
    
    /**
     * Using a text string, paste text to current file location
     * diectly to ROM file
     *
     * @param textString
     * @param startOffset
     *
     * @return
     */
    public long pasteUsingBytes( byte[] textString, long startOffset )
    {
        if ( textString != null )
        {
            // Try to insert the data
            if ( textString.length > 0 && !rom.dataWrite( textString, startOffset, textString.length ) )
            {
                toastThreadHelper( mContext, "Error: Data extends beyond 'End of File'" );
                return -1;
            }
        }
        else
        {
            toastThreadHelper( mContext, "String is empty" );
            //            Toast.makeText( mContext, "String is empty", Toast.LENGTH_SHORT ).show();
            return -1;
        }
        
        return textString.length;
    }
    
    
    /**
     * Using a text string, paste text to current file location
     * diectly to ROM file
     *
     * @param textString
     * @param startOffset
     *
     * @return
     */
    public long pasteUsingText( String textString, long startOffset, boolean mode )
    {
        String newString;
        byte[] buffer = null;
        
        try
        {
            if ( textString != null )
            {
                if ( !mode )
                {
                    newString = searchForMatches( textString );
                    
                    if ( newString == "" )
                    {
                        newString = text2Ascii( textString );
                    }
                    //
                    buffer = Hex2Byte( newString );
                }
                else
                {
                    newString = text2Ascii( textString );
                    buffer = Hex2Byte( newString );
                }
                
                // Try to insert the data
                if ( buffer != null && buffer.length > 0 && !rom.dataWrite( buffer, startOffset, buffer.length ) )
                {
                    //                Toast.makeText( mContext, "Error: Data extends beyond 'End of File'", Toast.LENGTH_SHORT ).show();
                    toastThreadHelper( mContext, "Error: Data extends beyond 'End of File'" );
                    return -1;
                }
            }
            else
            {
                toastThreadHelper( mContext, "String is empty" );
                //            Toast.makeText( mContext, "String is empty", Toast.LENGTH_SHORT ).show();
                return -1;
            }
        }
        catch ( NullPointerException ex )
        {
            ex.getMessage();
        }
    
        if ( buffer != null )
        {
            return buffer.length;
        }
        else
        {
            return 0;
        }
    }
    
    
    /**
     * Convert any text string to hev version of each character
     *
     * @param str
     *
     * @return
     */
    private String text2Ascii( String str )
    {
        StringBuilder newStr = new StringBuilder();
        String        hex;
        
        for ( int i = 0; i < str.length(); i++ )
        {
            hex = String.format( "%02X", ( int ) str.charAt( i ) );
            if ( (hex.length() & 1) == 1 )
            {   // For UTF above ASCII characters
                hex = "0" + hex;
            }
            //
            newStr.append( hex );
        }
        //
        return newStr.toString();
    }
    
    /**
     * Search for table file matches
     *
     * @param str
     *
     * @return
     */
    public String searchForMatches( String str )
    {
        StringBuilder newStr = new StringBuilder();
        String        chars;
        boolean       wasFound;
        
        for ( int i = 0; i < str.length(); i++ )
        {
            chars = str.substring( i, i + 1 );
            wasFound = false;
            
            for ( int c = 0; c < 256; c++ )
            {
                if ( global.oneCode[ c ] != "" && global.oneCode[ c ].equals( chars ) )
                {
                    newStr.append( String.format( "%02X", c ) );
                    wasFound = true;
                    break;
                }
            }
            
            // Check dual bytes too if not found!
            if ( !wasFound )
            {
                for ( int c = 0; c < 65536; c++ )
                {
                    if ( global.twoCode[ c ] != "" && global.twoCode[ c ].equals( chars ) )
                    {
                        newStr.append( String.format( "%04X", c ) );
                        break;
                    }
                }
            }
        }
        
        //
        return newStr.toString();
    }
    
    
    /**
     * Send the string
     * Convert the hex bytes to char array
     *
     * @param hexString
     *
     * @return
     */
    public byte[] Hex2Byte( String hexString )
    {
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
            
            return chars;
        }
        
        return null;
    }
    
    
    /**
     * Helper method to display Toast with a thread
     *
     * @param message
     */
    private void toastThreadHelper( final Context mContext, final String message )
    {
        new Handler( Looper.getMainLooper() ).post( new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText( mContext, message, Toast.LENGTH_SHORT ).show();
            }
        } );
    }
}