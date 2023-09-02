package com.GenesysEast.windhex.GlobalClasses;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.GenesysEast.windhex.R;

public class CustomToast
{
    Context context;
    
    public CustomToast( Context context )
    {
        this.context = context;
    }
    
    /**
     * Return a custom toast message
     *
     * @param message
     *
     * @return
     */
    public Toast myToaster( String message, int duration )
    {
        LayoutInflater inflater = LayoutInflater.from( context );
        View view = inflater.inflate( R.layout.toast_layout, null, false );
        
        TextView text = view.findViewById( R.id.textToast );
        text.setText( message );
        //
        Toast toast = new Toast( context );
        toast.setGravity( Gravity.CENTER_VERTICAL, 0, 0 );
        toast.setDuration( duration );
        toast.setView( view );
        
        return toast;
    }
}
