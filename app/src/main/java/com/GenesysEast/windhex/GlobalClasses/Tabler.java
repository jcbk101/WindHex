package com.GenesysEast.windhex.GlobalClasses;

import android.os.Parcel;
import android.os.Parcelable;

public class Tabler
        implements Parcelable
{
    String  Label;
    int     Offset;
    boolean Flag;
    
    
    /**
     * All the constructors
     *
     * @param label
     * @param offset
     */
    public Tabler( String label, int offset )
    {
        Label = label;
        Offset = offset;
    }
    
    public Tabler( String label, long offset )
    {
        Label = label;
        Offset = ( int ) offset;
    }
    
    public Tabler( String label, boolean flag )
    {
        Label = label;
        Flag = flag;
    }
    
    public Tabler( String label, int offset, boolean flag )
    {
        Label = label;
        Offset = offset;
        Flag = flag;
    }
    
    
    protected Tabler( Parcel in )
    {
        Label = in.readString();
        Offset = in.readInt();
        Flag = in.readByte() != 0;
    }
    
    public static final Creator<Tabler> CREATOR = new Creator<Tabler>()
    {
        @Override
        public Tabler createFromParcel( Parcel in )
        {
            return new Tabler( in );
        }
        
        @Override
        public Tabler[] newArray( int size )
        {
            return new Tabler[ size ];
        }
    };
    
    @Override
    public int describeContents()
    {
        return 0;
    }
    
    @Override
    public void writeToParcel( Parcel dest, int flags )
    {
    
        dest.writeString( Label );
        dest.writeInt( Offset );
        dest.writeByte( ( byte ) (Flag ? 1 : 0) );
    }
    
    public String getLabel()
    {
        return Label;
    }
    
    public void setLabel( String label )
    {
        Label = label;
    }
    
    public int getOffset()
    {
        return Offset;
    }
    
    public void setOffset( int offset )
    {
        Offset = offset;
    }
    
    public boolean isFlag()
    {
        return Flag;
    }
    
    public void setFlag( boolean flag )
    {
        Flag = flag;
    }
}
