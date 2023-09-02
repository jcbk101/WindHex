package com.GenesysEast.windhex.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

import com.GenesysEast.windhex.R;
import com.GenesysEast.windhex.WindHexActivity;

public class EditDialog
        extends DialogFragment
        implements View.OnClickListener, View.OnKeyListener
{
    private SwitchCompat switcher;
    private OnEditClick  onEditClick;
    private View         view;
    
    /**
     * Interface method declarations
     */
    public interface OnEditClick
    {
        void getEditText( String editText, boolean mode );
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        Dialog dialog = getDialog();
        int    width;
        int    height;
        
        //
        if ( dialog != null && dialog.getWindow() != null )
        {
            TextView tv = dialog.findViewById( R.id.warningText );
            
            width = ViewGroup.LayoutParams.MATCH_PARENT;
            height = ViewGroup.LayoutParams.MATCH_PARENT;
            //
            dialog.getWindow().setLayout( width, height );
            
            
            //
            tv.setVisibility( View.GONE );
            if ( switcher.isChecked() )
            {
                tv.setVisibility( View.VISIBLE );
            }
            
            try
            {
                WindHexActivity wa = ( WindHexActivity ) getActivity();
                if ( wa != null )
                {
                    wa.displayBottomBar();
                }
            }
            catch ( NullPointerException e )
            {
                e.getMessage();
            }
        }
    }
    
    
    public EditDialog()
    {
    }
    
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        View     button;
        TextView note;
        
        // Get the view
        view = View.inflate( getContext(), R.layout.edit_layout, null );
        
        switcher = view.findViewById( R.id.editSwitch );
        switcher.setOnClickListener( this );
        
        button = view.findViewById( R.id.cancelButton );
        button.setOnClickListener( this );
        button = view.findViewById( R.id.insertButton );
        button.setOnClickListener( this );
        button = view.findViewById( R.id.cancelEdit );
        button.setOnClickListener( this );
        
        //
        note = view.findViewById( R.id.warningText );
        note.setText( Html.fromHtml( getString( R.string.edit_warning ) ) );
    
        // Edit Text
        EditText     editText = view.findViewById( R.id.editString );
        editText.setText( "" );
        
        view.setOnKeyListener( this );
    
        //
        if ( getDialog() != null && getDialog().getWindow() != null )
        {
            getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
            getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
        }
        
        return view;
    }
    
    
    /**
     * Enable pressing Back
     *
     * @param view    N/A
     * @param keyCode N/A
     * @param event   N/A
     *
     * @return N/A
     */
    @Override
    public boolean onKey( View view, int keyCode, KeyEvent event )
    {
        if ( keyCode == KeyEvent.KEYCODE_BACK )
        {
            dismiss();
            return true;
        }
        
        return false;
    }
    
    /**
     * View click for multiple views
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        SwitchCompat switcher = view.findViewById( R.id.editSwitch );
        int          id       = v.getId();
        EditText     editText = view.findViewById( R.id.editString );
        TextView     note     = view.findViewById( R.id.warningText );
        
        // Test views
        if ( id == R.id.editSwitch )
        {
            if ( !switcher.isChecked() )
            {
                note.setVisibility( View.GONE );
            }
            else
            {
                note.setVisibility( View.VISIBLE );
            }
        }
        else if ( id == R.id.cancelButton || id == R.id.cancelEdit )
        {
            dismiss();
        }
        else if ( id == R.id.insertButton )
        {
            if ( editText != null && !editText.getText().toString().matches( "" ) )
            {
                String str = editText.getText().toString();
                
                dismiss();
                onEditClick.getEditText( str, !switcher.isChecked() );
            }
        }
    }
    
    /**
     * Interface: Insert / Edit listener
     *
     * @param context N/A
     */
    @Override
    public void onAttach( Context context )
    {
        super.onAttach( context );
        
        try
        {
            onEditClick = ( OnEditClick ) getActivity();
        }
        catch ( ClassCastException e )
        {
            e.getMessage();
        }
    }
}
