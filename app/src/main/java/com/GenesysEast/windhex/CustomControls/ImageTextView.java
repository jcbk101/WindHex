package com.GenesysEast.windhex.CustomControls;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;

import com.GenesysEast.windhex.R;


public class ImageTextView
        extends AppCompatImageView
{
    private Context  context;
    //    public  View    textView = null;
    //    private int     textViewId;
    private Paint    paint;
    //
    private String   text = null;
    private int      textColor;
    private int      textSize;
    private Typeface typeface;
    private int      strokeColor;
    private int      strokeWidth;
    
    
    /**
     * Constructors
     *
     * @param context
     */
    public ImageTextView( Context context )
    {
        super( context );
        readAttr( getContext(), null );
        
        paint = new Paint( Paint.ANTI_ALIAS_FLAG );
        paint.setColor( textColor );
        paint.setStrokeWidth( strokeWidth );
        paint.setTextSize( textSize );
        //paint.setTextAlign( Paint.Align.CENTER );
        
        //
        if ( typeface != null )
        {
            paint.setTypeface( typeface );
        }
    }
    
    public ImageTextView( Context context, @Nullable AttributeSet attrs )
    {
        super( context, attrs );
        readAttr( getContext(), attrs );
        
        paint = new Paint( Paint.ANTI_ALIAS_FLAG );
        paint.setColor( textColor );
        paint.setStrokeWidth( strokeWidth );
        paint.setTextSize( textSize );
        //paint.setTextAlign( Paint.Align.CENTER );
        
        if ( typeface != null )
        {
            paint.setTypeface( typeface );
        }
    }
    
    
    /**
     * Gettets and setters
     *
     * @return N/A
     */
    public String getText()
    {
        return text;
    }
    
    public void setText( String text )
    {
        this.text = text;
        invalidate();
    }
    
    public int getTextColor()
    {
        return textColor;
    }
    
    public void setTextColor( int textColor )
    {
        this.textColor = textColor;
        invalidate();
    }
    
    public int getTextSize()
    {
        return textSize;
    }
    
    public void setTextSize( int textSize )
    {
        this.textSize = textSize;
        invalidate();
    }
    
    public Typeface getTypeface()
    {
        return typeface;
    }
    
    public void setTypeface( Typeface typeface )
    {
        this.typeface = typeface;
        invalidate();
    }
    
    public int getStrokeColor()
    {
        return strokeColor;
    }
    
    public void setStrokeColor( int strokeColor )
    {
        this.strokeColor = strokeColor;
        invalidate();
    }
    
    public int getStrokeWidth()
    {
        return strokeWidth;
    }
    
    public void setStrokeWidth( int strokeWidth )
    {
        this.strokeWidth = strokeWidth;
        invalidate();
    }
    
    /**
     * Overridden onDraw for custom drawing
     *
     * @param canvas N/A
     */
    @Override
    protected void onDraw( Canvas canvas )
    {
        super.onDraw( canvas );
        
        if ( text != null )
        {
            int imgWidth  = getWidth();
            int imgHeight = getHeight();
            int txtWidth  = ( int ) paint.measureText( text );
            
            //            int x = (imgWidth / 1) - ( int ) (paint.measureText( text ) / 2);
            //            int x = (imgWidth / 2) - (txtWidth / 2);
            int x = imgWidth - txtWidth;
            //            int y = ( int ) ((imgHeight / 2) - (paint.ascent() / 2));
            int y = ( int ) (imgHeight - paint.ascent() - textSize);
            
            if ( strokeWidth > 0 )
            {
                paint.clearShadowLayer();
                paint.setShader( null );
                paint.setStrokeWidth( strokeWidth );
                paint.setStrokeJoin( Paint.Join.ROUND );
                paint.setStyle( Paint.Style.STROKE );
                paint.setColor( strokeColor );
                canvas.drawText( text, x, y, paint );
            }
            
            //
            paint.setStyle( Paint.Style.FILL );
            //            paint.setShader( shader );
            paint.setColor( textColor );
            canvas.drawText( text, x, y, paint );
        }
    }
    
    
    /**
     * Load custom attributes
     *
     * @param context N/A
     * @param attrs   N/A
     */
    private void readAttr( Context context, AttributeSet attrs )
    {
        TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.ImageTextView );
        
        // Read the colors
        strokeColor = a.getColor( R.styleable.ImageTextView_strokeColor, 0 );
        strokeWidth = a.getDimensionPixelSize( R.styleable.ImageTextView_strokeWidth, 0 );
        
        textColor = a.getInt( R.styleable.ImageTextView_textColor, 0 );
        textSize = ( int ) a.getDimension( R.styleable.ImageTextView_textSize, 0 );
        text = a.getString( R.styleable.ImageTextView_text );
        //
        int id = a.getResourceId( R.styleable.ImageTextView_fontFamily, 0 );
        if ( id != 0 )
        {
            try
            {
                typeface = ResourcesCompat.getFont( getContext(), id );
            }
            catch ( Resources.NotFoundException nfex )
            {
                try
                {
                    typeface = Typeface.create( a.getString( R.styleable.ImageTextView_fontFamily ), Typeface.NORMAL );
                }
                catch ( Resources.NotFoundException ex )
                {
                    ex.getMessage();
                }
            }
        }
        else
        {
            // Try as a string
            typeface = Typeface.create( a.getString( R.styleable.ImageTextView_fontFamily ), Typeface.NORMAL );
        }
        
        //
        a.recycle();
    }
    
}
