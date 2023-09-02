package com.GenesysEast.windhex.GlobalClasses;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.GenesysEast.windhex.Dialogs.LoadingAlert;
import com.GenesysEast.windhex.Dialogs.LoadingDialogFrag;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Locale;

public class RomFileAccess
{
    Globals global = Globals.getInstance();
    
    private static RomFileAccess instance;
    private        Handler       handler = new Handler( Looper.getMainLooper() );
    
    public  RandomAccessFile    rf;
    public  RandomAccessFile    wf;
    public  ArrayList<PointXYZ> undoList          = null;
    public  File                romCopy;
    public  File                oldRom;
    private boolean             romWasSaved       = true;
    private boolean             fileAlreadyOpened = false;
    public  boolean             canUndo           = false;
    private Context             mContext;
    public  long                curOffset         = -1;
    public  int                 curIndex          = -1;
    public  byte                curByte           = -1;
    public  byte                oldData;
    public  long                oldOffset         = -1;
    public  long                fileSize          = 0;
    private boolean             forViewing        = false;
    
    /**
     * Get value of variable
     * return integer or whatever type of variable you're working with
     * return statement returns the value of integer "test"
     */
    public static synchronized RomFileAccess getInstance()
    {
        if ( instance == null )
        {
            instance = new RomFileAccess();
        }
        //
        return instance;
    }
    
    /**
     * Constructor for context
     */
    private RomFileAccess()
    {
    }
    
    public RomFileAccess( boolean forViewing )
    {
        this.forViewing = forViewing;
    }
    
    /**
     * Load a rom, open for reading
     *
     * @param fileName N/A
     *
     * @return N/A
     */
    public boolean loadRomFile( File fileName, Activity activity, Context mContext )
    {
        if ( forViewing )
        {   // View only access not allowed
            return false;
        }
        
        this.mContext = mContext;
        
        long freeSpace;
        int  ext;
        
        try
        {
            // Make sure there is enough space for a file copy
            freeSpace = (romCopy != null && romCopy.exists()) ? romCopy.length() : 0;
            freeSpace += getFreeSpace();
            canUndo = true;
            
            // Test to see if the space left will be suffice
            // to make a working back up of the current rom file
            if ( fileName.length() >= freeSpace )
            //            if ( fileName.length() >= 1 )
            {
                CustomToast toaster = new CustomToast( mContext );
                toaster.myToaster( "Temp storage space is too low.\nAll file changes are permanent", Toast.LENGTH_LONG + Toast.LENGTH_SHORT ).show();
                //
                canUndo = false;
            }
            
            
            //#################################
            //
            // Close the previous file if open
            //
            //#################################
            if ( fileAlreadyOpened )
            {
                fclose();
                
                // Restore the unsaved ROM data
                //                global.fileName.delete();
                fileCopy( romCopy, oldRom );
                
                // delete temp ROM file
                if ( romCopy.exists() && !romCopy.delete() )
                {
                    Toast.makeText( mContext, "[File Load] TEMP File error", Toast.LENGTH_SHORT ).show();
                    romCopy = null;
                    return false;
                }
                //
                romCopy = null;
            }
            
            
            //#################################
            //
            // Copy file name
            //
            //#################################
            oldRom = global.fileName;
            romCopy = new File( global.fileName.getName() );
            
            //
            ext = romCopy.toString().indexOf( "." );
            //
            if ( ext != -1 )
            {
                // Use TMP file extension for easy clean up
                // Get the CORRECT "." position
                String getExt = global.fileName.getName();
                
                for ( int i = 0; i < getExt.length(); i++ )
                {
                    if ( getExt.charAt( i ) == '.' )
                    {
                        ext = i;
                    }
                }
                
                // We have the correct '.' position now
                String temp = romCopy.toString().substring( 0, ext );
                temp = temp + ".tmp";
                romCopy = new File( mContext.getCacheDir(), temp );
            }
            else
            {   // No extension found, create one
                romCopy = new File( mContext.getCacheDir(), global.fileName.getName() + ".tmp" );
            }
            
            //
            final LoadingAlert loader = new LoadingAlert( mContext, activity, "Loading file...Please wait" );
            
            loader.setCancelable( false );
            loader.show();
            
            
            //######################################
            //
            // Need to run this in the background
            //
            //######################################
            Thread thread = new Thread( new Runnable()
            {
                @Override
                public void run()
                {
                    // Make a temp duplicate of the file
                    fileCopy( global.fileName, romCopy );
                    
                    handler.post( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            loader.dismiss();
                            
                            // Get the screen printed when fully ready
                            if ( global.hView != null )
                            {
                                global.hView.requestLayout();
                            }
                        }
                    } );
                }
            } );
            
            
            // Load in the back ground
            thread.start();
            
            // Access both files at once for read/writing
            if ( fileName.canWrite() )
            {
                rf = new RandomAccessFile( fileName, "rw" );
                wf = new RandomAccessFile( romCopy, "rw" );
            }
            else
            {
                rf = new RandomAccessFile( fileName, "r" );
                wf = new RandomAccessFile( romCopy, "rw" );
            }
            
            //
            global.fileSize = fileName.length();
            fileSize = fileName.length();
            
            // Restrict offset value input to a length
            // Up to user to fix addressing issues
            // User given extra digit for user error
            global.ofsTextWidth.x = String.format( "%X", fileSize ).length() + 1;
            global.ofsTextWidth.y = String.format( Locale.getDefault(), "%d", fileSize ).length() + 1;
            
            //
            romWasSaved = true;
            fileAlreadyOpened = true;
            
            //
            global.setPosition( 0 );
            global.xyHex.x = -1;
            global.xyHex.y = -1;
            
            // Create a new undo list
            if ( undoList == null )
            {
                undoList = new ArrayList<>();
            }
            //
            undoList.clear();
            
            return true;
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
    
    /**
     * //#############################
     * <p>
     * MUST have permission to access
     * such files
     * <p>
     * //#############################
     */
    public boolean permissionsCheck( Activity activity )
    {
        if ( ContextCompat.checkSelfPermission( mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions( activity, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 10 );
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
     * Save an opened rom file
     * <p>
     * //##############################
     */
    public void saveRomFile()
    {
        if ( forViewing )
        {   // View only access not allowed
            return;
        }
        
        // Delete all undo list information
        if ( undoList == null )
        {
            undoList = new ArrayList<>();
        }
        //
        undoList.clear();
        
        // Delete old TEMP rom and make copy of current data
        if ( romCopy.delete() )
        {
            // Make a temp duplicate of the file
            fileCopy( global.fileName, romCopy );
        }
    }
    
    
    /**
     * //##############################
     * <p>
     * Save an opened rom file
     * <p>
     * //##############################
     */
    public boolean saveRomFileAs( File newName )
    {
        // Tricky bastard using the same ROM
        // Bute saving the new edited data
        if ( newName == oldRom )
        {
            saveRomFile();
            return false;
        }
        
        // Save the EDITED rom data to the new name
        fileCopy( oldRom, newName );
        
        // Can reload file
        return true;
    }
    
    
    /**
     * Fseek method with error checking
     *
     * @param offset
     *
     * @return
     */
    public void fseek( long offset )
    {
        try
        {
            rf.seek( offset );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Copy data from ROM file
     *
     * @param buffer
     * @param copySize
     */
    public boolean dataCopy( byte[] buffer, long offsetStart, int copySize )
    {
        if ( forViewing )
        {   // View only access not allowed
            return false;
        }
        
        fseek( offsetStart );
        //
        if ( (offsetStart + copySize) < global.fileSize )
        {
            try
            {
                rf.read( buffer );
                return true;
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText( mContext, "Requested copy size greater than data remaining", Toast.LENGTH_SHORT ).show();
        }
        //
        return false;
    }
    
    
    /**
     * Write buffered data to rom
     *
     * @param buffer      N/A
     * @param offsetStart N/A
     * @param dataSize    N/A
     *
     * @return N/A
     */
    public boolean dataWrite( byte[] buffer, long offsetStart, int dataSize )
    {
        if ( forViewing )
        {   // View only access not allowed
            return false;
        }
        
        fseek( offsetStart );
        
        if ( (offsetStart + dataSize) < global.fileSize )
        {
            try
            {
                rf.write( buffer, 0, dataSize );
                return true;
            }
            catch ( Exception e )
            {
                try
                {
                    Toast.makeText( mContext, "Error: File not written to.", Toast.LENGTH_SHORT ).show();
                    e.printStackTrace();
                }
                catch ( Exception ex )
                {
                    ex.printStackTrace();
                }
            }
        }
        
        return false;
    }
    
    
    /**
     * Read buffered data to process in Hex drawer
     *
     * @param buffer   N/A
     * @param dataSize N/A
     * @param Position N/A
     */
    public int fread( byte[] buffer, long Position, int dataSize )
    {
        int size = 0;
        
        try
        {
            fseek( Position );
            size = rf.read( buffer, 0, dataSize );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
        return size;
    }
    
    
    /**
     * Close current ROM file
     */
    public void fclose()
    {
        if ( forViewing )
        {   // View only access not allowed
            return;
        }
        
        try
        {
            rf.close();
            wf.close();
            fileAlreadyOpened = false;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
    }
    
    
    /**
     * Write a byte to ROM file, create undo entry
     *
     * @param data
     */
    public void fputc( int data )
    {
        if ( forViewing )
        {   // View only access not allowed
            return;
        }
        
        try
        {
            rf.write( data );
        }
        catch ( Exception e )
        {
            Toast.makeText( mContext, "Error: File not written to.", Toast.LENGTH_SHORT ).show();
            e.printStackTrace();
        }
    }
    
    
    /**
     * Read a byte from ROM
     *
     * @return
     */
    public byte fgetc()
    {
        byte romByte = 0;
        
        try
        {
            romByte = rf.readByte();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
        return romByte;
    }
    
    
    /**
     * Fill a buffer with bytes from rom file
     *
     * @param searchBuffer N/A
     */
    public boolean loadBytes( byte[] searchBuffer )
    {
        try
        {
            rf.readFully( searchBuffer );
            return true;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
    
    /**
     * Returns the free space available on device
     *
     * @return N/a
     */
    public long getFreeSpace()
    {
        StatFs statFs = new StatFs( Environment.getRootDirectory().getAbsolutePath() );
        
        return statFs.getAvailableBlocks() * statFs.getBlockSize();
    }
    
    /**
     * Copy file to another destination quickly
     *
     * @param in  N/A
     * @param out N/A
     *
     * @return
     */
    public boolean fileCopy( File in, File out )
    {
        FileChannel inChannel  = null;
        FileChannel outChannel = null;
        boolean     retFlag;
        
        if ( forViewing )
        {   // View only access not allowed
            return false;
        }
        
        try
        {
            inChannel = new FileInputStream( in ).getChannel();
            outChannel = new FileOutputStream( out ).getChannel();
            
            // Try to change this but this is the number I tried.. for Windows, 64Mb - 32Kb)
            if ( inChannel != null && outChannel != null )
            {
                int  maxCount = (0x10000);
                long size     = inChannel.size();
                long position = 0;
                
                while ( position < size )
                {
                    position += inChannel.transferTo( position, maxCount, outChannel );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if ( inChannel != null )
                {
                    inChannel.close();
                }
                
                if ( outChannel != null )
                {
                    outChannel.close();
                }
                
                retFlag = true;
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                retFlag = false;
            }
        }
        
        return retFlag;
    }
    
    
    /**
     * Copy file to another destination quickly using arguments
     *
     * @param in    N/A
     * @param out   N/A
     * @param start N/A
     * @param count N/A
     *
     * @return N/a
     */
    public boolean fileCopy( RandomAccessFile in, RandomAccessFile out, int start, int count )
    {
        FileChannel inChannel;
        FileChannel outChannel;
        boolean     retFlag;
        
        if ( forViewing )
        {   // View only access not allowed
            return false;
        }
        
        try
        {
            inChannel = in.getChannel();
            outChannel = out.getChannel();
            
            //
            // Process what we have as far as data goes
            //
            if ( inChannel != null && outChannel != null )
            {
                long size     = inChannel.size();
                long position = start;
                
                // Set the written to file's starting position
                outChannel.position( start );
                
                while ( position < (start + count) && position < size )
                {
                    position += inChannel.transferTo( position, count, outChannel );
                }
            }
            retFlag = true;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            retFlag = false;
        }
        
        return retFlag;
    }
    
    
    /**
     * Write a single byte many tiles to a file
     *
     * @param iData N/A
     * @param start N/A
     * @param count N/A
     *
     * @return N/A
     */
    public int byteFill( byte iData, int start, int count )
    {
        FileChannel outChannel;
        int         retFlag;
        byte[]      data   = new byte[ 256 ];
        ByteBuffer  buffer = ByteBuffer.allocate( data.length );
        
        if ( forViewing )
        {   // View only access not allowed
            return 0;
        }
        
        try
        {
            outChannel = rf.getChannel();
            
            // Fill array with required fill byte
            for ( int i = 0; i < 256; i++ )
            {
                data[ i ] = iData;
            }
            
            //
            buffer.put( data );
            buffer.flip();
            
            //
            // Process what we have as far as data goes
            //
            if ( outChannel != null )
            {
                long position = start;
                
                // Set the written to file's starting position
                outChannel.position( start );
                
                while ( (position + data.length) < (start + count) )
                {
                    position += outChannel.write( buffer );
                    outChannel.position( position );
                    buffer.flip();
                }
                
                // If data writing is not complete
                fseek( position );
                while ( position < (start + count) )
                {
                    fputc( iData );
                    position++;
                }
            }
            
            retFlag = count;
        }
        catch ( Exception e )
        {
            Toast.makeText( mContext, "Error: File not written to.", Toast.LENGTH_SHORT ).show();
            e.printStackTrace();
            retFlag = -1;
        }
        
        return retFlag;
    }
    
    
    /**
     * Write an Undo file list for reading when undo with
     * file is requested
     *
     * @param fList    N/A
     * @param Text     N/A
     * @param fileName N/A
     */
    public void writeUndoList( ArrayList<Tabler> fList, byte[] Text, File fileName )
    {
        //-----------------------------
        // Try and write to the temp file
        //-----------------------------
        OutputStreamWriter out;
        File               cacheFile;
        
        if ( forViewing )
        {   // View only access not allowed
            return;
        }
        
        try
        {
            cacheFile = fileName;
            out = new OutputStreamWriter( new FileOutputStream( cacheFile ) );
            
            out.write( "Text = " );
            for ( byte b : Text )
            {
                out.write( String.format( "%02X", (b & 0xFF) ) );
            }
            //
            out.write( "\n" );
            
            // The offsets
            for ( int c = 0; c < fList.size(); c++ )
            {
                out.write( fList.get( c ).getOffset() + "\n" );
            }
            
            out.close();
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
    }
    
    /**
     * Read an Undo file list and copy the data back to the file
     *
     * @param fileName N/A
     */
    public void readUndoList( File fileName )
    {
        if ( forViewing )
        {   // View only access not allowed
            return;
        }
        
        try
        {
            FileInputStream   inStream;
            InputStreamReader strReader;
            BufferedReader    bufReader;
            String            str;
            byte[]            Text     = null;
            int               index;
            InsertData        inserter = new InsertData( mContext );
            
            // Activate the file for reading
            inStream = new FileInputStream( fileName );
            
            // Read in text data
            strReader = new InputStreamReader( inStream );
            bufReader = new BufferedReader( strReader );
            
            //
            while ( (str = bufReader.readLine()) != null )
            {
                if ( str.toUpperCase().contains( "TEXT = " ) )
                {
                    index = str.indexOf( "=" ) + 1;
                    Text = inserter.Hex2Byte( str.substring( index ).trim() );
                }
                else
                {
                    if ( Text != null )
                    {
                        index = Integer.parseInt( str );
                        inserter.pasteUsingBytes( Text, index );
                    }
                }
            }
            
            inStream.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Load a rom, open for reading in view mode only
     *
     * @param fileName N/A
     *
     * @return N/A
     */
    public boolean loadFileToView( File fileName, Globals global )
    {
        try
        {
            // Access both files at once for read/writing
            rf = new RandomAccessFile( fileName, "rw" );
            
            //
            global.fileSize = fileName.length();
            fileSize = fileName.length();
            
            // Restrict offset value input to a length
            // Up to user to fix addressing issues
            // User given extra digit for user error
            global.ofsTextWidth.x = String.format( "%X", fileSize ).length() + 1;
            global.ofsTextWidth.y = String.format( Locale.getDefault(), "%d", fileSize ).length() + 1;
            
            //
            global.setPosition( 0 );
            global.xyHex.x = -1;
            global.xyHex.y = -1;
            
            return true;
        }
        catch ( FileNotFoundException e )
        {
            e.getMessage();
        }
        
        return false;
    }
    
    
    /**
     * Format a number to human readable format
     *
     * @param byteSize N/A
     * @param decimals N/A
     *
     * @return N/A
     */
    public static String formatFileSize( long byteSize, int decimals )
    {
        if ( byteSize == 1 )
        {
            return "1 Byte";
        }
        else
        {
            double temp      = byteSize;
            int    decPlaces = Math.max( decimals, 0 );
            int    index     = 0;
            String[] sizes = {
                    "Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"
            };
            
            // Chop down the value
            while ( temp > 1024 )
            {
                index++;
                temp /= 1024;
            }
            
            // Send the formatted text
            if ( index > 8 )
            {
                return byteSize + " bytes";
            }
            else
            {
                if ( decPlaces > 0 )
                {
                    return String.format( Locale.getDefault(), "%." + decPlaces + "f %s", temp, sizes[ index ] );
                }
                else
                {
                    return String.format( Locale.getDefault(), "%d %s", ( int ) temp, sizes[ index ] );
                }
            }
        }
    }
}
