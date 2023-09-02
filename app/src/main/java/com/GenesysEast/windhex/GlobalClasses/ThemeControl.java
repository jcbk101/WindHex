package com.GenesysEast.windhex.GlobalClasses;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.GenesysEast.windhex.R;

public class ThemeControl
{
    static Globals global = Globals.getInstance();
    
    private final static int THEME_GRAY_THEME      = 0;
    private final static int THEME_PURPLE_THEME    = 1;
    private final static int THEME_WATER_THEME     = 2;
    private final static int THEME_FIRE_THEME      = 3;
    private final static int THEME_GREEN_THEME     = 4;
    private final static int THEME_DARKGREEN_THEME = 5;
    private final static int THEME_DARKWATER_THEME = 6;
    
    /**
     * Contructor
     */
    public ThemeControl()
    {
    }
    
    /**
     * Request a change of theme for current
     * Activity being displayed
     *
     * @param themeSelected
     */
    public static void changeToTheme( Context mContext, int themeSelected )
    {
        Activity activity = ThemeControl.getThisActivity( mContext );
        // Set new theme value
        global.currentTheme = themeSelected;
        
        // Change the theme if allowed
        if ( activity != null )
        {
            activity.finish();
            activity.startActivity( new Intent( activity, activity.getClass() ) );
            activity.overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
        }
    }
    
    /**
     * Change the themes
     *
     * @param activity
     * @param themeSelected
     */
    public static void onActivityCreateSetTheme( Activity activity, int themeSelected )
    {
        switch ( themeSelected )
        {
            case THEME_PURPLE_THEME:
                activity.setTheme( R.style.PurpleTheme );
                break;
            case THEME_WATER_THEME:
                activity.setTheme( R.style.WaterTheme );
                break;
            case THEME_DARKWATER_THEME:
                activity.setTheme( R.style.DarkWaterTheme );
                break;
            case THEME_FIRE_THEME:
                activity.setTheme( R.style.FireTheme );
                break;
            case THEME_GREEN_THEME:
                activity.setTheme( R.style.GreenTheme );
                break;
            case THEME_DARKGREEN_THEME:
                activity.setTheme( R.style.DarkGreenTheme );
                break;
            case THEME_GRAY_THEME:
            default:
                activity.setTheme( R.style.GrayTheme );
                break;
        }
    }
    
    
    /**
     * Retrieve the active Activity
     *
     * @return
     */
    public static Activity getThisActivity( Context mContext )
    {
        Context context = mContext;
        
        while ( context instanceof ContextWrapper )
        {
            if ( context instanceof Activity )
            {
                return ( Activity ) context;
            }
            context = (( ContextWrapper ) context).getBaseContext();
        }
        return null;
    }
}
