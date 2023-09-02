package com.GenesysEast.windhex.Hex_Drawer.SubClasses;

import android.content.Context;
import android.view.ScaleGestureDetector;

import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.Hex_Drawer.DrawHexView;
import com.GenesysEast.windhex.R;


final public class MyPincher
        extends ScaleGestureDetector.SimpleOnScaleGestureListener
{
    Globals global = Globals.getInstance();
    
    private Context     mContext;
    private int         scaleUp   = 0;
    private int         scaleDown = 0;
    private DrawHexView hView;
    
    
    /**
     * DrawHexView class ref
     *
     * @param hView
     */
    public void sethView( DrawHexView hView )
    {
        this.hView = hView;
    }
    
    
    /**
     * @param context
     */
    public MyPincher( Context context )
    {
        this.mContext = context;
    }
    
    /**
     * Scaler that adjust font height for Hex display
     *
     * @param detector
     *
     * @return
     */
    @Override
    public boolean onScale( ScaleGestureDetector detector )
    {
        int maxFontScale = mContext.getResources().getDimensionPixelSize( R.dimen._36ssp );
        int minFontScale = mContext.getResources().getDimensionPixelSize( R.dimen._7ssp );
        int changeValue  = 1;//mContext.getResources().getDimensionPixelSize( R.dimen._1ssp );
        
        //
        if ( detector != null )
        {
            float scaleFactor = detector.getScaleFactor();
            
            if ( scaleFactor > 1.0f )
            {   // Make font bigger
                scaleDown = 0;
                scaleUp += 1;
                if ( scaleUp >= 4 )
                {
                    global.fntHeight += changeValue;
                    if ( global.fntHeight > maxFontScale )
                    {
                        global.fntHeight = maxFontScale;
                    }
                    //
                    changeFontData();
                    //                    hView.layout( 0, 0, 0, 0 );
                    hView.requestLayout();
                    hView.invalidate();
                    scaleUp = 0;
                }
            }
            else if ( scaleFactor < 1.0f )
            {   // Make font smaller
                scaleUp = 0;
                scaleDown += 1;
                if ( scaleDown >= 4 )
                {
                    global.fntHeight -= changeValue;
                    if ( global.fntHeight < minFontScale )
                    {
                        global.fntHeight = minFontScale;
                    }
                    //
                    changeFontData();
                    //                    hView.layout( 0, 0, 0, 0 );
                    //                    hView.layout( 0, 0, 0, 0 );
                    hView.requestLayout();
                   hView.invalidate();
                }
            }
        }
        
        return true;
    }
    
    
    /**
     * End of Scaler call
     *
     * @param detector
     */
    @Override
    public void onScaleEnd( ScaleGestureDetector detector )
    {
    }
    
    
    /**
     * Changes the font size for the hex display
     */
    private void changeFontData()
    {
        global.paint.setTextSize( global.fntHeight );
        global.chrWidth = ( int ) global.paint.measureText( "WW" );
    }
    
}
