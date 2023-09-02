package com.GenesysEast.windhex.Dialogs;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.GenesysEast.windhex.CustomControls.GradientTextView;
import com.GenesysEast.windhex.R;

public class ErrorDialog
        extends DialogFragment
        implements View.OnClickListener
{
    public static int ALL_BUTTONS   = 7;
    public static int YES_BUTTON    = 1;
    public static int NO_BUTTON     = 2;
    public static int CANCEL_BUTTON = 4;
    public static int HIDE_BUTTONS  = -1;
    
    private       int                 buttons;
    private       String              title;
    private       int                 titleRes;
    private       String              message;
    private       int                 messageRes;
    private       String              yesText    = "Yes";
    private       String              noText     = "No";
    private       String              cancelText = "Cancel";
    private       int                 alertIcon  = 0;
    private       View                mainView;
    private       OnErrorListener     onErrorListener;
    private       OnAnimationListener onShakeAnimationListener;
    private       int                 clickSound;
    public static boolean             isShowing  = false;
    private       String[]            list       = null;
    private       ListView             listView;
    private       ArrayAdapter<String> adapter;
    
    
    public interface OnErrorListener
    {
        void onErrorExitClick( ErrorDialog dialog, int buttonClicked );
    }
    
    public interface OnAnimationListener
    {
        void onAnimationEnd();
    }
    
    
    /**
     * Setters
     *
     * @param title
     */
    public ErrorDialog setTitle( String title )
    {
        this.title = title;
        
        if ( mainView != null )
        {
            TextView tv;
            tv = mainView.findViewById( R.id.msgTitle );
            tv.setText( title );
        }
        
        return this;
    }
    
    public ErrorDialog setTitle( int title )
    {
        this.titleRes = title;
        
        if ( titleRes > 0 && mainView != null )
        {
            this.title = getString( titleRes );
            
            TextView tv;
            tv = mainView.findViewById( R.id.msgTitle );
            tv.setText( title );
        }
        
        
        return this;
    }
    
    public ErrorDialog setMessage( String message )
    {
        this.message = message;
        
        if ( mainView != null )
        {
            TextView tv;
            tv = mainView.findViewById( R.id.msgText );
            tv.setText( message );
        }
        
        return this;
    }
    
    public ErrorDialog setMessage( int message )
    {
        this.messageRes = message;
        
        if ( messageRes > 0 && mainView != null )
        {
            this.message = getString( messageRes );
            
            TextView tv;
            tv = mainView.findViewById( R.id.msgText );
            tv.setText( message );
        }
        
        return this;
    }
    
    public ErrorDialog setButtons( int buttons )
    {
        this.buttons = buttons;
        
        if ( buttons < 0 )
        {
            GradientTextView button;
            
            button = mainView.findViewById( R.id.invalidButtonCancel );
            button.setVisibility( View.INVISIBLE );
            button = mainView.findViewById( R.id.invalidButtonNo );
            button.setVisibility( View.INVISIBLE );
            button = mainView.findViewById( R.id.invalidButtonDone );
            button.setVisibility( View.INVISIBLE );
        }
        
        return this;
    }
    
    public ErrorDialog setYesText( String yesText )
    {
        this.yesText = yesText;
        
        if ( mainView != null )
        {
            GradientTextView button;
            int              width;
            
            button = mainView.findViewById( R.id.invalidButtonDone );
            button.setText( yesText );
            
            width = ( int ) button.getPaint().measureText( yesText );
            button.getLayoutParams().width = width + (width / 3);
            //
            button.setTag( YES_BUTTON );
            button.setVisibility( View.VISIBLE );
        }
        
        return this;
    }
    
    public ErrorDialog setNoText( String noText )
    {
        this.noText = noText;
        
        if ( mainView != null )
        {
            GradientTextView button;
            int              width;
            
            button = mainView.findViewById( R.id.invalidButtonNo );
            button.setText( noText );
            
            width = ( int ) button.getPaint().measureText( noText );
            button.getLayoutParams().width = width + (width / 3);
            //
            button.setTag( NO_BUTTON );
            button.setVisibility( View.VISIBLE );
        }
        
        return this;
    }
    
    public ErrorDialog setCancelText( String cancelText )
    {
        this.cancelText = cancelText;
        
        if ( mainView != null )
        {
            GradientTextView button;
            int              width;
            
            button = mainView.findViewById( R.id.invalidButtonCancel );
            button.setText( cancelText );
            
            width = ( int ) button.getPaint().measureText( cancelText );
            button.getLayoutParams().width = width + (width / 3);
            //
            button.setTag( CANCEL_BUTTON );
            button.setVisibility( View.VISIBLE );
        }
        
        return this;
    }
    
    public ErrorDialog setAlertIcon( int alertIcon )
    {
        this.alertIcon = alertIcon;
        
        if ( mainView != null )
        {
            ImageView view = mainView.findViewById( R.id.msgIcon );
            view.setImageResource( alertIcon );
        }
        
        return this;
    }
    
    public ErrorDialog setItems( String[] list )
    {
        this.list = list;
        
        if ( mainView != null && getContext() != null )
        {
            View       v   = mainView.findViewById( R.id.errorListLayout );
            View       msg = mainView.findViewById( R.id.msgText );
            TypedValue a   = new TypedValue();
            //
            v.setVisibility( View.VISIBLE );
            
            // Change the message BG to standard background color
            msg.setBackground( null );
            getContext().getTheme().resolveAttribute( R.attr.colorPrimary, a, true );
            msg.setBackgroundColor( ContextCompat.getColor( getContext(), a.resourceId ) );
            
            
            // List of selection items container
            listView = mainView.findViewById( R.id.errorListView );
            listView.setLayoutParams( new FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT ) );
            listView.requestLayout();
            
            adapter = new ArrayAdapter<>( getContext(), R.layout.jump_item, list );
            listView.setAdapter( adapter );
            adapter.notifyDataSetChanged();
            //
            listView.setOnItemClickListener( new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick( AdapterView<?> parent, View view, int position, long id )
                {
                    if ( onErrorListener != null )
                    {
                        onErrorListener.onErrorExitClick( ErrorDialog.this, position );
                        dismiss();
                    }
                }
            } );
            
            // Hide all buttons
            v = mainView.findViewById( R.id.invalidButtonCancel );
            v.setVisibility( View.INVISIBLE );
            v = mainView.findViewById( R.id.invalidButtonNo );
            v.setVisibility( View.INVISIBLE );
            v = mainView.findViewById( R.id.invalidButtonDone );
            v.setVisibility( View.INVISIBLE );
        }
        
        return this;
        
    }
    
    public void setOnErrorListener( OnErrorListener onErrorListener )
    {
        this.onErrorListener = onErrorListener;
    }
    
    public void setOnShakeAnimationListener( OnAnimationListener onShakeAnimationListener )
    {
        this.onShakeAnimationListener = onShakeAnimationListener;
    }
    
    
    /**
     * All other instantiations
     */
    public ErrorDialog()
    {
    }
    
    @Override
    public void dismiss()
    {
        View dlgView = getView();
        
        if ( dlgView != null && dlgView.getTag() != null )
        {
            (( ObjectAnimator ) dlgView.getTag()).end();
            dlgView.setTag( null );
        }
        
        isShowing = false;
        super.dismiss();
/*
        // Remove from stack
        if ( getActivity() != null )
        {
            getActivity().getSupportFragmentManager().beginTransaction().remove( this ).commit();
            Toast.makeText( getContext(), "Removed from stack", Toast.LENGTH_SHORT ).show();
        }
*/
    }
    
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        try
        {
            mainView = View.inflate( getContext(), R.layout.error_layout, null );
            
            View close = mainView.findViewById( R.id.errorCancel );
            close.setOnClickListener( this );
            close.setTag( -1 );
            
            
            //
            if ( getDialog() != null && getDialog().getWindow() != null )
            {
                getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
                getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
                getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
                
                getDialog().setOnKeyListener( new DialogInterface.OnKeyListener()
                {
                    @Override
                    public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event )
                    {
                        if ( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && getView() != null )
                        {
                            View button1 = getView().findViewById( R.id.invalidButtonCancel );
                            View button2 = getView().findViewById( R.id.invalidButtonNo );
                            View button3 = getView().findViewById( R.id.invalidButtonDone );
                            
                            if ( button1 != null && button1.getVisibility() == View.VISIBLE )
                            {
                                button1.setPressed( true );
                                button1.callOnClick();
                                return true;
                            }
                            
                            if ( button2 != null && button2.getVisibility() == View.VISIBLE )
                            {
                                button2.setPressed( true );
                                button2.callOnClick();
                                return true;
                            }
                            
                            if ( button3 != null && button3.getVisibility() == View.VISIBLE )
                            {
                                button3.setPressed( true );
                                button3.callOnClick();
                                return true;
                            }
                        }
                        
                        return false;
                    }
                } );
            }
            else
            {
                return null;
            }
        }
        catch ( NullPointerException npe )
        {
            npe.getMessage();
            return null;
        }
        //
        return mainView;
    }
    
    
    /**
     * Used to set the screen into fullscreen mode
     */
    @Override
    public void onStart()
    {
        super.onStart();
        
        Dialog dialog = getDialog();
        
        if ( dialog != null )
        {
            int       width  = ViewGroup.LayoutParams.MATCH_PARENT;
            int       height = ViewGroup.LayoutParams.MATCH_PARENT;
            Button    button;
            ImageView icon;
            TextView  tv;
            
            
            // In Displayed status
            isShowing = true;
            
            //
            if ( dialog.getWindow() != null )
            {
                dialog.getWindow().setLayout( width, height );
            }
            
            //
            tv = mainView.findViewById( R.id.msgTitle );
            if ( titleRes > 0 )
            {
                title = getString( titleRes );
            }
            //
            tv.setText( title );
            
            
            tv = mainView.findViewById( R.id.msgText );
            if ( messageRes > 0 )
            {
                message = getString( messageRes );
            }
            //
            tv.setText( message );
            
            // Icon
            icon = mainView.findViewById( R.id.msgIcon );
            icon.setImageResource( alertIcon );
            
            
            //#################################
            //
            // Hide all buttons when requested
            //
            //#################################
            // We have a list???
            if ( list != null )
            {
                if ( getContext() != null )
                {
                    View       v = mainView.findViewById( R.id.errorListLayout );
                    TypedValue a = new TypedValue();
                    
                    //
                    v.setVisibility( View.VISIBLE );
                    
                    // Change the message BG to standard background color
                    tv.setBackground( null );
                    getContext().getTheme().resolveAttribute( R.attr.colorPrimary, a, true );
                    tv.setBackgroundColor( ContextCompat.getColor( getContext(), a.resourceId ) );
                    
                    listView = mainView.findViewById( R.id.errorListView );
                    listView.setLayoutParams( new FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT ) );
                    listView.requestLayout();
                    
                    adapter = new ArrayAdapter<>( getContext(), R.layout.jump_item, list );
                    listView.setAdapter( adapter );
                    adapter.notifyDataSetChanged();
                    //
                    listView.setOnItemClickListener( new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick( AdapterView<?> parent, View view, int position, long id )
                        {
                            if ( onErrorListener != null )
                            {
                                onErrorListener.onErrorExitClick( ErrorDialog.this, position );
                                dismiss();
                            }
                        }
                    } );
                }
            }
            else
            {
                // Cancel, No, and Done button clicker assignment
                button = mainView.findViewById( R.id.invalidButtonCancel );
                if ( (buttons & CANCEL_BUTTON) != 0 )
                {
                    button.setOnClickListener( this );
                    button.setText( cancelText );
                    width = ( int ) button.getPaint().measureText( cancelText );
                    //                button.getLayoutParams().width = width + (width / 4);
                    //
                    button.setTag( CANCEL_BUTTON );
                    button.setVisibility( View.VISIBLE );
                }
                
                button = mainView.findViewById( R.id.invalidButtonNo );
                if ( (buttons & NO_BUTTON) != 0 )
                {
                    button.setOnClickListener( this );
                    button.setText( noText );
                    //                    width = ( int ) button.getPaint().measureText( noText );
                    //                  button.getLayoutParams().width = width + (width / 4);
                    //
                    button.setTag( NO_BUTTON );
                    button.setVisibility( View.VISIBLE );
                }
                
                button = mainView.findViewById( R.id.invalidButtonDone );
                button.setOnClickListener( this );
                button.setText( yesText );
                //                width = ( int ) button.getPaint().measureText( doneText );
                //                button.getLayoutParams().width = width + (width / 4);
                //
                button.setTag( YES_BUTTON );
                button.setVisibility( View.VISIBLE );
            }
        }
    }
    
    /**
     * Click listener for the button on
     * rom info form
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        if ( onErrorListener != null )
        {
            if ( list != null )
            {
                // Return nada
                onErrorListener.onErrorExitClick( this, -1 );
                dismiss();
            }
            else if ( v.getTag() != null )
            {
                onErrorListener.onErrorExitClick( this, ( int ) v.getTag() );
            }
            else
            {
                onErrorListener.onErrorExitClick( this, YES_BUTTON );
            }
        }
        else
        {
            dismiss();
        }
        
        isShowing = false;
    }
    
    
    /**
     * Internal call to simplfy reactions for this dialog
     *
     * @param shakeType       N/A
     * @param readMessageTime N/A
     */
    public void animateShake( boolean shakeType, final int readMessageTime )
    {
        final View dlgView = getView();
        
        if ( dlgView != null )
        {
            if ( !shakeType )
            {
                dlgView.setRotation( 5 );
            }
            else
            {
                dlgView.setRotation( -5 );
            }
            //
            dlgView.setScaleX( .9f );
            dlgView.setScaleY( .9f );
            dlgView.animate().setInterpolator( new BounceInterpolator() );
            dlgView.animate().scaleX( 1 ).scaleX( 1 ).rotation( 0 ).setDuration( 1000 );
            dlgView.animate().withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    if ( onShakeAnimationListener != null )
                    {
                        if ( readMessageTime <= 0 )
                        {
                            onShakeAnimationListener.onAnimationEnd();
                        }
                        else
                        {
                            dlgView.setAlpha( .99f );
                            dlgView.animate().setDuration( readMessageTime );
                            dlgView.animate().alpha( 1f ).withEndAction( new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    onShakeAnimationListener.onAnimationEnd();
                                }
                            } ).start();
                            
                        }
                    }
                }
            } ).start();
        }
    }
    
    
    /**
     * Internal call to simplfy reactions for this dialog
     *
     * @param shakeType N/A
     */
    public void animateShake( boolean shakeType )
    {
        animateShake( shakeType, 0 );
    }
    
}
