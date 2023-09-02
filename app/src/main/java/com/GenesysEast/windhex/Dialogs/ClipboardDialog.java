package com.GenesysEast.windhex.Dialogs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.*;

public class ClipboardDialog
{
    /**
     * Returns a single string of clipboard data from the top of the list
     *
     * @return N/A
     */
    public String getSingleClipObject( Context mContext )
    {
        try
        {
            ClipboardManager clipboard = ( ClipboardManager ) mContext.getSystemService( Context.CLIPBOARD_SERVICE );
            
            if ( clipboard != null && clipboard.hasPrimaryClip() )
            {
                ClipData.Item item;
                ClipData      clipData = clipboard.getPrimaryClip();
                
                if ( clipData != null )
                {
                    item = clipData.getItemAt( 0 );
                    return item.getText().toString();
                }
            }
            else
            {
                Toast.makeText( mContext, "Not a text format", Toast.LENGTH_SHORT ).show();
                return null;
            }
        }
        catch ( NullPointerException e )
        {
            Toast.makeText( mContext, "Clipboard Service not available", Toast.LENGTH_SHORT ).show();
        }
        
        return null;
    }
}
