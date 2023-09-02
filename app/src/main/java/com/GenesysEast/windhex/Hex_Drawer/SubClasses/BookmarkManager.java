package com.GenesysEast.windhex.Hex_Drawer.SubClasses;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.GlobalClasses.Tabler;
import com.GenesysEast.windhex.R;

import org.w3c.dom.Text;

import java.util.Locale;

public class BookmarkManager
        extends DialogFragment
        implements View.OnClickListener
{
    private EditText           createName;
    private int                position;
    private Globals            global;
    private View               view_main;
    private OnBookmarkListener onBookmarkListener;
    
    
    /**
     * Constructors
     */
    public BookmarkManager( int position )
    {
        this.position = position;
    }
    
    public BookmarkManager( long position )
    {
        this.position = ( int ) position;
    }
    
    
    /**
     * Interface to communicate completion
     * and to notify adapter to update
     */
    public interface OnBookmarkListener
    {
        void bookmarkResult( int position );
    }
    
    
    /**
     * View inflater for the dialog
     *
     * @param savedInstanceState N/A
     *
     * @return N/A
     */
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        View     button;
        TextView header;
        
        // Get the view inflated
        view_main = View.inflate( getContext(), R.layout.hex_bookmark_layout, null );
        
        
        //##############################
        //
        // Main buttons
        //
        //##############################
        button = view_main.findViewById( R.id.cancelBookmark );
        button.setOnClickListener( this );
        button = view_main.findViewById( R.id.bookmarkCreateButton );
        button.setOnClickListener( this );
        button = view_main.findViewById( R.id.bookmarkCancelButton );
        button.setOnClickListener( this );
        
        
        header = view_main.findViewById( R.id.bookmarkHeader );
        header.setText( String.format( Locale.getDefault(), "Bookmark Offset: $%X", position ) );
        
        //
        if ( getDialog() != null && getDialog().getWindow() != null )
        {
            getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
            getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
            getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        }
        
        //
        global = Globals.getInstance();
        
        return view_main;
    }
    
    /**
     * Set dialog size
     */
    @Override
    public void onResume()
    {
        super.onResume();
        
        Dialog dialog = getDialog();
        
        if ( dialog != null )
        {
            int    width  = ViewGroup.LayoutParams.MATCH_PARENT;
            int    height = ViewGroup.LayoutParams.MATCH_PARENT;
            Button button;
            
            //
            if ( dialog.getWindow() != null )
            {
                dialog.getWindow().setLayout( width, height );
            }
        }
    }
    
    
    /**
     * Clicker for the buttons
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        int id = v.getId();
        
        if ( id == R.id.bookmarkCreateButton )
        {
            EditText edit = view_main.findViewById( R.id.bookmarkName );
            
            global.jList.add( new Tabler( edit.getText().toString(), position ) );
        }
        
        // Close it all
        dismiss();
    }
}
