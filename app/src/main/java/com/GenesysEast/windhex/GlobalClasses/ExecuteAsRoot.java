package com.GenesysEast.windhex.GlobalClasses;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public abstract class ExecuteAsRoot
{
    protected abstract ArrayList<String> getCommandsToExecute();
    
    private static Context context;
    
    
    /**
     * //###############################
     * <p>
     * Root access checks
     * <p>
     * //###############################
     *
     * @return
     */
    public static boolean canRunRootCommands( Context mContext )
    {
        boolean retval = false;
        Process suProcess;
        
        context = mContext;
        
        try
        {
            suProcess = Runtime.getRuntime().exec( "su" );
            
            DataOutputStream os    = new DataOutputStream( suProcess.getOutputStream() );
            DataInputStream  osRes = new DataInputStream( suProcess.getInputStream() );
            
            if ( null != os && null != osRes )
            {
                // Getting the id of the current user to check if this is root
                os.writeBytes( "id\n" );
                os.flush();
                
                String  currUid = osRes.readLine();
                boolean exitSu  = false;
                if ( null == currUid )
                {
                    retval = false;
                    exitSu = false;
                    Toast.makeText( context, "Can't get root access or denied by user", Toast.LENGTH_SHORT ).show();
                }
                else if ( currUid.contains( "uid=0" ) )
                {
                    retval = true;
                    exitSu = true;
                    Toast.makeText( context, "Root access granted", Toast.LENGTH_SHORT ).show();
                }
                else
                {
                    retval = false;
                    exitSu = true;
                    Toast.makeText( context, "Root access rejected: " + currUid, Toast.LENGTH_SHORT ).show();
                }
                
                if ( exitSu )
                {
                    os.writeBytes( "exit\n" );
                    os.flush();
                }
            }
        }
        catch ( Exception e )
        {
            // Can't get root !
            // Probably broken pipe exception on trying to write to output stream (os) after su failed, meaning that the device is not rooted
            
            retval = false;
            Toast.makeText( context, "Root access rejected: " + e.getMessage(), Toast.LENGTH_SHORT ).show();
        }
        
        return retval;
    }
    
    
    /**
     * Execute SU commands to gain root access test
     *
     * @return
     */
    public final boolean execute()
    {
        boolean retval = false;
        
        try
        {
            ArrayList<String> commands = getCommandsToExecute();
            
            if ( null != commands && commands.size() > 0 )
            {
                Process suProcess = Runtime.getRuntime().exec( "su" );
                
                DataOutputStream os = new DataOutputStream( suProcess.getOutputStream() );
                
                // Execute commands that require root access
                for ( String currCommand : commands )
                {
                    os.writeBytes( currCommand + "\n" );
                    os.flush();
                }
                
                os.writeBytes( "exit\n" );
                os.flush();
                
                try
                {
                    int suProcessRetval = suProcess.waitFor();
                    if ( 255 != suProcessRetval )
                    {
                        // Root access granted
                        retval = true;
                    }
                    else
                    {
                        // Root access denied
                        retval = false;
                    }
                }
                catch ( Exception ex )
                {
                    ex.printStackTrace();
                    //                    Log.e( "ROOT", "Error executing root action", ex );
                }
            }
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
            //            Log.w( "ROOT", "Can't get root access", ex );
        }
        catch ( SecurityException ex )
        {
            ex.printStackTrace();
            //            Log.w( "ROOT", "Can't get root access", ex );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            //            Log.w( "ROOT", "Error executing internal operation", ex );
        }
        
        return retval;
    }
}