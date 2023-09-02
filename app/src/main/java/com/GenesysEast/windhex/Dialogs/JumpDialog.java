package com.GenesysEast.windhex.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.GlobalClasses.Tabler;
import com.GenesysEast.windhex.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

public class JumpDialog
        extends DialogFragment
        implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, TextWatcher
{
    private Globals global = Globals.getInstance();
    
    private EditText offsetInput;
    private Button   gotoButton;
    private Button   cancelButton;
    
    private long    gotoOffset;
    private boolean inHexMode = true;
    private Spinner spinView;
    
    /**
     * Interface method declarations
     */
    public interface OnInputListener
    {
        void getOffset( long Offset );
    }
    
    /**
     * Return requested dialog
     */
    public JumpDialog()
    {
    }
    
    private OnInputListener inputListener;
    
    @Override
    public void onItemSelected( AdapterView<?> parent, View view, int position, long id )
    {
        String value = null;
        
        if ( offsetInput.getText() != null )
        {
            value = offsetInput.getText().toString();
        }
        
        if ( position == 0 && !inHexMode )
        {
            inHexMode = true;
            
            offsetInput.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( ( int ) global.ofsTextWidth.x ) } );
            offsetInput.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD );
            //
            if ( value != null && value.length() != 0 )
            {
                gotoOffset = Long.parseLong( value, 10 );
                if ( gotoOffset > global.fileSize )
                {
                    gotoOffset = global.fileSize;
                }
                //                offsetInput.setText( "" );
                if ( offsetInput.getText() != null )
                {
                    offsetInput.getText().clear();
                }
                offsetInput.append( String.format( "%X", gotoOffset ) );
            }
        }
        else if ( position == 1 && inHexMode )
        {
            inHexMode = false;
            offsetInput.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( ( int ) global.ofsTextWidth.y ) } );
            offsetInput.setInputType( InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD );
            //
            if ( value != null && value.length() != 0 )
            {
                gotoOffset = Long.parseLong( value, 16 );
                if ( gotoOffset > global.fileSize )
                {
                    gotoOffset = global.fileSize;
                }
                //offsetInput.setText( "" );
                if ( offsetInput.getText() != null )
                {
                    offsetInput.getText().clear();
                }
                offsetInput.append( String.format( Locale.getDefault(), "%d", gotoOffset ) );
            }
        }
    }
    
    @Override
    public void onNothingSelected( AdapterView<?> parent )
    {
    
    }
    
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        Dialog dialog = getDialog();
        
        if ( dialog != null && dialog.getWindow() != null )
        {
            int width  = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            
            dialog.getWindow().setLayout( width, height );
        }
    }
    
    
    /**
     * Handle all button clicks
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {   // make sure we have a value too!
        int    index = spinView.getSelectedItemPosition();
        String hexStr;
        String finalStr;
        
        if ( v == gotoButton && offsetInput.length() != 0 )
        {   // Determine is using Hex or Dec mode
            finalStr = offsetInput.getText().toString().toUpperCase();
            hexStr = finalStr.replaceAll( "X", "" );
            finalStr = hexStr.replaceAll( Pattern.quote("$" ), "" );
            
            if ( finalStr != null && finalStr != "" )
            {
                try
                {
                    gotoOffset = Long.parseLong( finalStr, 16 );
    
                    if ( index == 1 )
                    {
                        gotoOffset = Long.parseLong( finalStr );
                    }
    
                    inputListener.getOffset( gotoOffset );
                }catch ( Exception ex )
                {
                    Toast.makeText( getContext(), "Input error. Please try again...", Toast.LENGTH_SHORT ).show();
                    ex.printStackTrace();
                }
            }
            
            // Dismiss dialog either way
            dismiss();
        }
        else if ( v.getId() == R.id.cancelJump || v == cancelButton )
        {
            dismiss();
        }
    }
    
    
    /**
     * Handle spinner item clicks
     *
     * @param parent   N/A
     * @param view     N/A
     * @param position N/A
     * @param id       N/A
     */
    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id )
    {
        int index = spinView.getSelectedItemPosition();
        
        if ( index == 0 )
        {   // Hex
            offsetInput.setText( String.format( "%X", global.jList.get( position ).getOffset() ) );
        }
        else
        {   // print text in decimal mode
            offsetInput.setText( String.format( Locale.getDefault(), "%d", global.jList.get( position ).getOffset() ) );
        }
        
    }
    
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        View                 view;
        ListView             gotoList;
        JumpAdapter          listAdapter;
        ArrayList<String>    spinList;
        ArrayAdapter<String> spinAdapter;
        String[]             list = { "Hexadecimal", "Decimal" };
        ImageView            exit;
        
        // Get the view
        view = View.inflate( getContext(), R.layout.jump_layout, null );
        
        offsetInput = view.findViewById( R.id.offsetInput );
        offsetInput.setFilters( new InputFilter[]{ new InputFilter.LengthFilter( ( int ) global.ofsTextWidth.x ) } );
        //
        offsetInput.setOnFocusChangeListener( new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange( View v, boolean hasFocus )
            {
                if ( !hasFocus )
                {
                    hideKeyboard( v );
                }
            }
        } );
        //
        offsetInput.addTextChangedListener( this );
        
        //
        gotoButton = view.findViewById( R.id.gotoButton );
        cancelButton = view.findViewById( R.id.cancelButton );
        exit = view.findViewById( R.id.cancelJump );
        
        // Set listeners
        gotoButton.setOnClickListener( this );
        cancelButton.setOnClickListener( this );
        exit.setOnClickListener( this );
        
        gotoButton.setText( R.string.jump_done );
        cancelButton.setText( R.string.jump_cancel );
        
        assert getContext() != null;
        listAdapter = new JumpAdapter( global.jList ); //ArrayAdapter<>( getContext(), R.layout.jump2_item, jumpList );
        gotoList = view.findViewById( R.id.gotoList );
        gotoList.setAdapter( listAdapter );
        gotoList.setSelected( false );  // must
        gotoList.setOnItemClickListener( this );
        //
        //########################################
        //
        spinList = new ArrayList<>( Arrays.asList( list ) );
        //
        spinAdapter = new ArrayAdapter<>( getContext(), R.layout.tabler_spinner_item, spinList );
        spinAdapter.setDropDownViewResource( R.layout.searcher_dropdown_list );
        spinView = view.findViewById( R.id.addressSpinner );
        spinView.setAdapter( spinAdapter );
        spinView.setOnItemSelectedListener( this );
        
        //
        if ( getDialog() != null && getDialog().getWindow() != null )
        {
            getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
            getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
            //
            //            getDialog().getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
        }
        
        return view;
        
    }
    
    
    /**
     * Interface: Input listener
     *
     * @param context N/A
     */
    @Override
    public void onAttach( Context context )
    {
        super.onAttach( context );
        
        try
        {
            inputListener = ( OnInputListener ) getActivity();
        }
        catch ( ClassCastException e )
        {
            e.getMessage();
        }
    }
    
    
    /**
     * Text watcher methods
     *
     * @param s     N/A
     * @param start N/A
     * @param count N/A
     * @param after N/A
     */
    @Override
    public void beforeTextChanged( CharSequence s, int start, int count, int after )
    {
    
    }
    
    @Override
    public void onTextChanged( CharSequence s, int start, int before, int count )
    {
    
    }
    
    @Override
    public void afterTextChanged( Editable s )
    {
        String        str     = s.toString().toUpperCase();
        StringBuilder hexDone = new StringBuilder();
        //        String        hex     = "ABCDEF0123456789abcdef";
        int index = spinView.getSelectedItemPosition();
        
        //
        // Using hex input selected in spinner
        if ( index == 0 && str.length() != 0 )
        {
/*
            for ( int c = 0; c < str.length(); c++ )
            {
                if ( hex.indexOf( str.charAt( c ) ) != -1 )
                {
                    hexDone.append( str.charAt( c ) );
                }
            }
*/
            //
            //            str = hexDone.toString();
        }
        //
        offsetInput.removeTextChangedListener( this );
        //offsetInput.setText( "" );
        if ( offsetInput.getText() != null )
        {
            offsetInput.getText().clear();
        }
        offsetInput.append( str );
        offsetInput.addTextChangedListener( this );
    }
    
    
    /**
     * Simply hide keyboard on touches NOT for Edit text
     *
     * @param view N/A
     */
    private void hideKeyboard( View view )
    {
        if ( getContext() != null )
        {
            InputMethodManager inputMethodManager = ( InputMethodManager ) getContext().getSystemService( Activity.INPUT_METHOD_SERVICE );
            inputMethodManager.hideSoftInputFromWindow( view.getWindowToken(), 0 );
        }
    }
    
    
    /**
     * Inner Array adapter for list view
     */
    private class JumpAdapter
            extends BaseAdapter
    {
        private ArrayList<Tabler> jumpList;
        
        private JumpAdapter( ArrayList<Tabler> jList )
        {
            this.jumpList = jList;
        }
        
        @Override
        public int getCount()
        {
            return jumpList.size();
        }
        
        @Override
        public Object getItem( int position )
        {
            return jumpList.get( position );
        }
        
        @Override
        public long getItemId( int position )
        {
            return position;
        }
        
        /**
         * View builder
         *
         * @param position    N/A
         * @param convertView N/A
         * @param parent      N/A
         *
         * @return N/A
         */
        @Override
        public View getView( int position, View convertView, ViewGroup parent )
        {
            View     view;
            TextView jumpText;
            
            if ( convertView == null )
            {
                convertView = View.inflate( getContext(), R.layout.jump_item, null );
            }
            
            view = convertView;
            jumpText = view.findViewById( R.id.jumpText );
            
            if ( getContext() != null )
            {
                jumpText.setText( getContext().getString( R.string.jump_offset, jumpList.get( position ).getLabel() ) );
            }
            
            return view;
        }
    }
}
