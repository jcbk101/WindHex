package com.GenesysEast.windhex.GlobalClasses;

public class ClipBoard
{
    private long    Start;
    public  byte[] buffer;
    
    public long getStart()
    {
        return Start;
    }
    
    public void setStart( long start )
    {
        Start = start;
    }
    
    public int getLength()
    {
        if ( buffer != null )
        {
            return buffer.length;
        }
        else
        {
            return 0;
        }
    }
}
