package com.GenesysEast.windhex.helper;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.InflateException;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.GenesysEast.windhex.GlobalClasses.Globals;
import com.GenesysEast.windhex.GlobalClasses.ThemeControl;
import com.GenesysEast.windhex.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class helper_activity
        extends AppCompatActivity
{
    private Globals global = Globals.getInstance();
    
    /**
     * Main entry for activity
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        
        // MUST BE SET BEFORE setContentView
        ThemeControl.onActivityCreateSetTheme( this, global.currentTheme );
        AdView adView;
        
        //
        try
        {
            // Do AFTER theme set
            setContentView( R.layout.helper_view );
            //
            WebView webView = findViewById( R.id.helpWebView );
            
            //            WebView.setWebContentsDebuggingEnabled( false );
            if ( getPackageManager().hasSystemFeature( "android.software.webview" ) )
            {
                webView.setWebViewClient( new WebViewClient() );
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled( true );
                settings.setDomStorageEnabled( true );
                settings.setBuiltInZoomControls( false );
                webView.addJavascriptInterface( new JavaScriptInterface( this ), "Android" );
                //
                webView.loadUrl( "file:///android_asset/main_page.html" );
    
/*
                adView = findViewById( R.id.webAdView );
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd( adRequest );
*/
            }
            else
            {
                Toast.makeText( this, "Missing important file system: android.webkit.Webview", Toast.LENGTH_SHORT ).show();
            }
        }
        catch ( Exception ie )
        {
            Toast.makeText( this, "Error displaying help file", Toast.LENGTH_SHORT ).show();
        }
    }
    
    /**
     * //########################################
     * <p>
     * Interface to speak back and forth
     * with Webview
     * <p>
     * //########################################
     */
    public class JavaScriptInterface
    {
        Context mContext;
        
        JavaScriptInterface( Context context )
        {
            this.mContext = context;
        }
        
        @JavascriptInterface
        public String getFromAndroid( int type )
        {
            TypedValue a;
            String     color;
            int        iData;
            
            // Get current Theme's textPrimaryColor
            a = new TypedValue();
            
            if ( type == 0 )
            {   // For the body
                getTheme().resolveAttribute( android.R.attr.windowBackground, a, true );
                iData = ContextCompat.getColor( mContext, a.resourceId );
            }
            else if ( type == 1 )
            {   // For main message box
                getTheme().resolveAttribute( R.attr.colorPrimaryDark, a, true );
                iData = ContextCompat.getColor( mContext, a.resourceId );
            }
            else if ( type == 3 )
            {   // For buttons
                getTheme().resolveAttribute( R.attr.colorButtonNormal, a, true );
                iData = ContextCompat.getColor( mContext, a.resourceId );
            }
            else
            {   // for "sub topic" divs
                getTheme().resolveAttribute( R.attr.colorPrimary, a, true );
                iData = ContextCompat.getColor( mContext, a.resourceId );
            }
            
            //
            color = String.format( "#%06X", (iData & 0x00FFFFFF) );
            return color;
        }
    }
}
