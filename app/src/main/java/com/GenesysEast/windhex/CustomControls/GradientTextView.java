package com.GenesysEast.windhex.CustomControls;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.Nullable;
import androidx.appcompat.text.AllCapsTransformationMethod;
import androidx.appcompat.widget.AppCompatTextView;


import com.GenesysEast.windhex.R;

import java.util.ArrayList;
import java.util.List;

public class GradientTextView
        extends AppCompatTextView
{
    public static final int            HORIZONTAL        = 0;
    public static final int            VERTICAL          = 1;
    private             Context        context;
    private             int[]          colorList;
    private             int[]          strokeColorList;
    private             int            gradient;
    private             int            gradientDirection = 0;
    private             int            strokeWidth;
    private             int            strokeColor;
    private             int[]          colors            = new int[ 2 ];
    private             LinearGradient linearGradient;
    private             Rect           bounds;
    private             LinearGradient strokeGradient = null;
    
    
    /**
     * Constructors
     *
     * @param context
     */
    public GradientTextView( Context context )
    {
        super( context );
    }
    
    public GradientTextView( Context context, @Nullable AttributeSet attrs )
    {
        super( context, attrs );
        readAttr( context, attrs );
        getGradient();
        getStrokeGradient();
    }
    
    public GradientTextView( Context context, @Nullable AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );
        readAttr( context, attrs );
        getGradient();
        getStrokeGradient();
    }
    
    /**
     * Setters
     */
    public void setStartColor( int startColor )
    {
        colors[ 0 ] = startColor;
        getGradient();
    }
    
    public void setEndColor( int endColor )
    {
        colors[ 1 ] = endColor;
        getGradient();
    }
    
    
    /**
     * For INT arrays
     *
     * @param colorsArray
     */
    public void setColorList( int[] colorsArray )
    {
        this.colorList = colorsArray;
    }
    
    /**
     * For res ID of string data
     *
     * @param colorsArrayId
     */
    public void setColorList( int colorsArrayId )
    {
        //        this.colorList = colorList;
        
        String[] nums2str = getResources().getStringArray( colorsArrayId );
        this.colorList = new int[ nums2str.length ];
        
        for ( int i = 0; i < nums2str.length; i++ )
        {
            colorList[ i ] = ( int ) Long.parseLong( nums2str[ i ].substring( 2 ), 16 );
        }
        
        
        if ( colorList != null )
        {
            getGradient();
        }
    }
    
    public void setGradientDirection( int gradientDirection )
    {
        this.gradientDirection = gradientDirection;
    }
    
    public void setGradient( boolean gradient )
    {
        if ( gradient )
        {
            this.gradient = 1;
        }
        else
        {
            this.gradient = 0;
        }
    }
    
    public void setStrokeWidth( int strokeWidth )
    {
        this.strokeWidth = strokeWidth;
    }
    
    
    public void setStrokeColor( int strokeColor )
    {
        this.strokeColor = strokeColor;
    }
    
    /**
     * For font stroke color
     *
     * @param colorsArray
     */
    public void setStrokeColorList( int[] colorsArray )
    {
        this.strokeColorList = colorsArray;
    }
    
    public void setStrokeColorList( int colorsArrayId )
    {
        
        String[] nums2str = getResources().getStringArray( colorsArrayId );
        
        this.strokeColorList = new int[ nums2str.length ];
        
        for ( int i = 0; i < nums2str.length; i++ )
        {
            strokeColorList[ i ] = ( int ) Long.parseLong( nums2str[ i ].substring( 2 ), 16 );
        }
        
        
        if ( strokeColorList != null )
        {
            getStrokeGradient();
        }
    }
    
    
    @Override
    public void setText( CharSequence text, BufferType type )
    {
        super.setText( text, type );
        getGradient();
    }
    
    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom )
    {
        super.onLayout( changed, left, top, right, bottom );
        
        getGradient();
        getStrokeGradient();
    }
    
    
    /**
     * Helper method for gradient effect
     */
    private void getGradient()
    {
        // Get the actual text bounds for gradient drawing
        Paint  p;
        String s = getText().toString();
        int    baseline;
        
        p = this.getPaint();
        bounds = new Rect();
        
        
        //##############################
        //
        // Split the textview strings
        //
        //##############################
        List<CharSequence> lines = new ArrayList<>();
        int                count = getLineCount();
        for ( int line = 0; line < count; line++ )
        {
            int          start     = getLayout().getLineStart( line );
            int          end       = getLayout().getLineEnd( line );
            CharSequence substring = getText().subSequence( start, end );
            lines.add( substring );
            //
            if ( line > 3 )
            {
                break;
            }
        }
        
        // how many lines do we have
        int longest  = 0;
        int curWidth = 0;
        
        for ( int i = 0; i < lines.size(); i++ )
        {
            if ( p.measureText( lines.get( i ).toString() ) > curWidth )
            {
                longest = i;
                curWidth = ( int ) p.measureText( lines.get( i ).toString() );
            }
        }
        
        //
        if ( lines.size() > 0 )
        {
            s = lines.get( longest ).toString();
        }
        
        // Check for ALL CAPS
        TransformationMethod transformationMethod = getTransformationMethod();
        if ( transformationMethod != null )
        {
            if ( transformationMethod.getClass().getSimpleName().equalsIgnoreCase( AllCapsTransformationMethod.class.getSimpleName() ) )
            {
                s = s.toUpperCase();
            }
        }
        
        
        //#########################################
        //
        // Get the pixel width of the text string
        //
        //#########################################
        p.getTextBounds( s, 0, s.length(), bounds );
        baseline = getBaseline();
        
        // Adjust the bound information and translate to canvas coordinates
        bounds.top = baseline + bounds.top;
        bounds.bottom = baseline + bounds.bottom;
        
        int width   = getWidth();
        int gravity = getGravity();//(getGravity() & (Gravity.HORIZONTAL_GRAVITY_MASK | Gravity.VERTICAL_GRAVITY_MASK));
        
        // Determine where the gradient starts and ends
        if ( (gravity & (Gravity.HORIZONTAL_GRAVITY_MASK | Gravity.VERTICAL_GRAVITY_MASK)) == Gravity.CENTER )
        {
            bounds.left += (width - ( int ) p.measureText( s, 0, s.length() )) / 2;
        }
        else if ( (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL )
        {
            bounds.left += (width - ( int ) p.measureText( s, 0, s.length() )) / 2;
        }
        else if ( (getGravity() & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT )
        {
            bounds.left += (width - ( int ) p.measureText( s, 0, s.length() ));
        }
        else
        {
            bounds.left += getPaddingLeft();
        }
        
        //
        bounds.right = ( int ) p.measureText( s, 0, s.length() );
        bounds.right = (bounds.right + bounds.left);
        
        
        //
        if ( colorList != null )
        {
            if ( gradientDirection == 0 )
            {
                linearGradient = new LinearGradient( bounds.left, bounds.top, bounds.right, bounds.bottom, colorList, null,
                                                     Shader.TileMode.CLAMP
                );
            }
            else
            {
                linearGradient = new LinearGradient( 0, bounds.top, 0, bounds.bottom, colorList, null, Shader.TileMode.CLAMP );
            }
        }
        else if ( colors != null )
        {
            if ( gradientDirection == 0 )
            {
                linearGradient = new LinearGradient( bounds.left, bounds.top, bounds.right, bounds.bottom, colors, null,
                                                     Shader.TileMode.CLAMP
                );
            }
            else
            {
                linearGradient = new LinearGradient( 0, bounds.top, 0, bounds.bottom, colors, null, Shader.TileMode.CLAMP );
            }
        }
        
        if ( gradient > 0 )
        {
            getPaint().setShader( linearGradient );
        }
        
    }
    
    
    /**
     * Helper method for gradient effect
     */
    private void getStrokeGradient()
    {
        // Get the actual text bounds for gradient drawing
        Paint  p;
        String s = getText().toString();
        int    baseline;
        
        p = this.getPaint();
        bounds = new Rect();
        
        
        //##############################
        //
        // Split the textview strings
        //
        //##############################
        List<CharSequence> lines = new ArrayList<>();
        int                count = getLineCount();
        for ( int line = 0; line < count; line++ )
        {
            int          start     = getLayout().getLineStart( line );
            int          end       = getLayout().getLineEnd( line );
            CharSequence substring = getText().subSequence( start, end );
            lines.add( substring );
            //
            if ( line > 3 )
            {
                break;
            }
        }
        
        // how many lines do we have
        int longest  = 0;
        int curWidth = 0;
        
        for ( int i = 0; i < lines.size(); i++ )
        {
            if ( p.measureText( lines.get( i ).toString() ) > curWidth )
            {
                longest = i;
                curWidth = ( int ) p.measureText( lines.get( i ).toString() );
            }
        }
        
        //
        if ( lines.size() > 0 )
        {
            s = lines.get( longest ).toString();
        }
        
        // Check for ALL CAPS
        TransformationMethod transformationMethod = getTransformationMethod();
        if ( transformationMethod != null )
        {
            if ( transformationMethod.getClass().getSimpleName().equalsIgnoreCase( AllCapsTransformationMethod.class.getSimpleName() ) )
            {
                s = s.toUpperCase();
            }
        }
        
        
        //#########################################
        //
        // Get the pixel width of the text string
        //
        //#########################################
        p.getTextBounds( s, 0, s.length(), bounds );
        baseline = getBaseline();
        
        // Adjust the bound information and translate to canvas coordinates
        bounds.top = baseline + bounds.top + strokeWidth;
        bounds.bottom = baseline + bounds.bottom - strokeWidth;
        
        int width   = getWidth();
        int gravity = getGravity();//(getGravity() & (Gravity.HORIZONTAL_GRAVITY_MASK | Gravity.VERTICAL_GRAVITY_MASK));
        
        // Determine where the gradient starts and ends
        if ( (gravity & (Gravity.HORIZONTAL_GRAVITY_MASK | Gravity.VERTICAL_GRAVITY_MASK)) == Gravity.CENTER )
        {
            bounds.left += (width - ( int ) p.measureText( s, 0, s.length() )) / 2;
        }
        else if ( (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL )
        {
            bounds.left += (width - ( int ) p.measureText( s, 0, s.length() )) / 2;
        }
        else if ( (getGravity() & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT )
        {
            bounds.left += (width - ( int ) p.measureText( s, 0, s.length() ));
        }
        else
        {
            bounds.left += getPaddingLeft();
        }
        
        //
        bounds.left -= +strokeWidth;
        bounds.right = ( int ) p.measureText( s, 0, s.length() );
        bounds.right = (bounds.right + bounds.left) + strokeWidth;
        
        //
        if ( strokeColorList != null )
        {
            if ( gradientDirection == 0 )
            {
                strokeGradient = new LinearGradient( bounds.left, bounds.top, bounds.right, bounds.bottom, strokeColorList, null,
                                                     Shader.TileMode.CLAMP
                );
            }
            else
            {
                strokeGradient = new LinearGradient( 0, bounds.top, 0, bounds.bottom, strokeColorList, null, Shader.TileMode.CLAMP );
            }
        }
    }
    
    /**
     * Overridden onDraw for custom drawing
     *
     * @param canvas
     */
    @Override
    protected void onDraw( Canvas canvas )
    {
        int    textColor = getCurrentTextColor();
        Shader shader    = getPaint().getShader();
        
        
        if ( strokeWidth > 0 )
        {
            getPaint().clearShadowLayer();
            
            // Stroke can have a gradient too
            getPaint().setShader( strokeGradient );
            
            getPaint().setStrokeWidth( strokeWidth );
            getPaint().setStrokeJoin( Paint.Join.BEVEL );
            getPaint().setStyle( Paint.Style.STROKE );
            setTextColor( strokeColor );
            super.onDraw( canvas );
        }
        
        //
        getPaint().setStyle( Paint.Style.FILL );
        getPaint().setShader( shader );
        setTextColor( textColor );
        super.onDraw( canvas );
        
        
/*
        if ( bounds != null )
        {
            Layout layout = getLayout();
            
            Paint rectPaint = new Paint();
            rectPaint.setColor( 0xFF00FF00 );
            rectPaint.setStyle( Paint.Style.STROKE );
            rectPaint.setStrokeWidth( 3 );
            canvas.drawRect( bounds, rectPaint );
        }
*/
    }
    
    
    /**
     * Load custom attributes
     *
     * @param context
     * @param attrs
     */
    private void readAttr( Context context, AttributeSet attrs )
    {
        TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.GradientTextView );
        
        // Read the colors
        gradientDirection = a.getInt( R.styleable.GradientTextView_gradientOrientation, 0 );
        gradient = a.getInt( R.styleable.GradientTextView_gradient, 0 );
        
        //
        colors[ 0 ] = a.getColor( R.styleable.GradientTextView_startColor, 0xFFFFFFFF ); // White
        colors[ 1 ] = a.getColor( R.styleable.GradientTextView_endColor, 0xFF000000 ); // Black
        //
        strokeColor = a.getColor( R.styleable.GradientTextView_strokeColor, 0 );
        strokeWidth = a.getDimensionPixelSize( R.styleable.GradientTextView_strokeWidth, 0 );
        
        // Regular gradient
        int id = a.getResourceId( R.styleable.GradientTextView_colorList, 0 );
        if ( id != 0 )
        {
            String[] nums2str = getResources().getStringArray( id );
            colorList = new int[ nums2str.length ];
            
            for ( int i = 0; i < nums2str.length; i++ )
            {
                colorList[ i ] = ( int ) Long.parseLong( nums2str[ i ].substring( 2 ), 16 );
            }
        }
        
        
        // Stroke gradient
        id = a.getResourceId( R.styleable.GradientTextView_strokeColorList, 0 );
        if ( id != 0 )
        {
            String[] nums2str = getResources().getStringArray( id );
            strokeColorList = new int[ nums2str.length ];
            
            for ( int i = 0; i < nums2str.length; i++ )
            {
                strokeColorList[ i ] = ( int ) Long.parseLong( nums2str[ i ].substring( 2 ), 16 );
            }
        }
        
        //
        a.recycle();
    }
}
