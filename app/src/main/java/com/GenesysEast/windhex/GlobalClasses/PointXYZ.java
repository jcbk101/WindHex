package com.GenesysEast.windhex.GlobalClasses;

import java.io.File;

public class PointXYZ
{
    public long x;
    public long y;
    public long z;
    public int  length;
    public File undoFile;
    
    /**
     * Constructors
     *
     * @param x
     * @param y
     * @param z
     * @param length
     */
    public PointXYZ( long x, long y, long z, int length )
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.length = length;
        this.undoFile = null;
    }
    
    public PointXYZ( long x, long y, long z )
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.length = 0;
        this.undoFile = null;
    }
    
    public PointXYZ( long x, long y )
    {
        this.x = x;
        this.y = y;
        this.z = 0;
        this.length = 0;
        this.undoFile = null;
    }
    
    public PointXYZ( long x )
    {
        this.x = x;
        this.y = 0;
        this.z = 0;
        this.length = 0;
        this.undoFile = null;
    }
    
    public PointXYZ( File fileName )
    {
        this.x = -1;
        this.y = -1;
        this.z = -1;
        this.length = 0;
        this.undoFile = fileName;
    }
    
    public PointXYZ()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.length = 0;
        this.undoFile = null;
    }
    
    public void clear()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.length = 0;
        this.undoFile = null;
    }
}
