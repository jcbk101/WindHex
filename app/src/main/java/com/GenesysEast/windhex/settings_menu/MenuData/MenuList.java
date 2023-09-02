package com.GenesysEast.windhex.settings_menu.MenuData;

public class MenuList
{
    private String headerText;
    private String mainText;
    private String subText;
    private int    Flags;
    private int    Arg0;
    private int    Tag;
    
    public MenuList( String headerText, int flags )
    {
        this.headerText = headerText;
        this.mainText = null;
        this.subText = null;
        this.Flags = flags;
        this.Arg0 = 0;
        this.Tag = 0;
    }
    
    public MenuList( String mainText, String subText, int flags, int arg0 )
    {
        this.headerText = null;
        this.mainText = mainText;
        this.subText = subText;
        this.Flags = flags;
        this.Arg0 = arg0;
        this.Tag = 0;
    }
    
    public MenuList( String mainText, String subText, int flags, int arg0, int tag )
    {
        this.headerText = null;
        this.mainText = mainText;
        this.subText = subText;
        this.Flags = flags;
        this.Arg0 = arg0;
        this.Tag = tag;
    }
    
    public int getTag()
    {
        return Tag;
    }
    
    public String getHeaderText()
    {
        return headerText;
    }
    
    public String getMainText()
    {
        return mainText;
    }
    
    public String getSubText()
    {
        return subText;
    }
    
    public int getFlags()
    {
        return Flags;
    }
    
    public int getArg0()
    {
        return Arg0;
    }
}