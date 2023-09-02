package com.GenesysEast.windhex.GlobalClasses;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.core.content.ContextCompat;

import com.GenesysEast.windhex.R;
import com.GenesysEast.windhex.WindHexActivity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Implementing the Boyer-Moore-Horspool search algorithm
 */
@SuppressLint("ParcelCreator")
public class BMH
        implements Parcelable
        //implements Serializable
{
    private Globals           global        = Globals.getInstance();
    private RomFileAccess     rom           = RomFileAccess.getInstance();
    private int               shift[]       = new int[ 256 ];
    private long              currentOffset = 0;
    private Context           mContext;
    private Activity          activity;
    public  FoundListAdapter  findAdapter;
    public  ArrayList<Tabler> fList         = new ArrayList<>();
    public  ListView          findListView;
    public  int               selectItem    = -1;
    private int               selectedColor;
    
    
    /**
     * Currently selected it
     *
     * @param selectItem N/A
     */
    public void setSelectItem( int selectItem )
    {
        this.selectItem = selectItem;
    }
    
    /**
     * Contructors
     *
     * @param mContext N/A
     */
    public BMH( Context mContext, Activity activity )
    {
        this.mContext = mContext;
        this.activity = activity;
        
        // List view attached to Main content view for WindHex Drawer
        findListView = activity.findViewById( R.id.searcherList );
        findAdapter = new FoundListAdapter( fList, findListView );
        findListView.setAdapter( findAdapter );
        //
        
        TypedValue a = new TypedValue();
        mContext.getTheme().resolveAttribute( R.attr.colorAccent, a, true );
        selectedColor = ContextCompat.getColor( mContext, a.resourceId );
    }
    
    
    @Override
    public int describeContents()
    {
        return 0;
    }
    
    @Override
    public void writeToParcel( Parcel dest, int flags )
    {
    
    }
    
    /**
     * Save the list data
     *
     * @param windHexActivity
     */
    public static void saveData( WindHexActivity windHexActivity )
    {
    }
    
    
    /**
     * A pattern searching function that uses Bad
     * Character Heuristic of Boyer Moore Algorithm
     *
     * @param findText  N/A
     * @param curOffset N/A
     */
    public long findTheString( byte[] findText, int curOffset )
    {
        long   checkOffset = curOffset;
        byte[] searchBuffer;
        
        if ( (rom.fileSize - curOffset) < 0x100000 && (rom.fileSize - curOffset) > 0 )
        {
            searchBuffer = new byte[ ( int ) (rom.fileSize - curOffset) ];
        }
        else if ( rom.fileSize < 0x100000 )
        {
            searchBuffer = new byte[ ( int ) rom.fileSize ];
        }
        else
        {
            searchBuffer = new byte[ 0x100000 ];
        }
        
        
        // Fill the bad char table
        for ( int k = 0; k < 256; k++ )
        {
            shift[ k ] = findText.length;
        }
        
        // Add the codes from the data to search for
        for ( int k = 0; k < findText.length - 1; k++ )
        {
            shift[ findText[ k ] & 0xFF ] = findText.length - 1 - k;
        }
        
        // Fill the buffer and search for the data
        rom.fseek( checkOffset );
        
        // Check for read error
        if ( !rom.loadBytes( searchBuffer ) )
        {
            return -1;
        }
        
        // checkOffset is shift of the pattern with
        // respect to text
        _RefreshLabel_:
        while ( checkOffset < rom.fileSize && !WindHexActivity.canBreakSearch )
        {
            if ( !BMHSearch( findText, searchBuffer, checkOffset ) )
            {
                checkOffset = currentOffset;
                rom.fseek( checkOffset );
                
                if ( (rom.fileSize - checkOffset) < searchBuffer.length )
                {
                    if ( (rom.fileSize - checkOffset) < findText.length )
                    {
                        return -1;
                    }
                    
                    //
                    searchBuffer = new byte[ ( int ) (rom.fileSize - checkOffset) ];
                }
                
                // Check for read error
                if ( !rom.loadBytes( searchBuffer ) )
                {
                    return -1;
                }
                
                //
                continue _RefreshLabel_;
            }
            
            // Data was found
            return currentOffset;
        }
        
        return -1;
    }
    
    /**
     * Boyer-Moore-Horspool Algorithm
     *
     * @param pattern     N/A
     * @param text        N/A
     * @param startOffset N/A
     *
     * @return N/A
     */
    private boolean BMHSearch( byte[] pattern, byte[] text, long startOffset )
    {
        int i    = 0;
        int j    = 0;
        int skip = 0;
        
        while ( (i + pattern.length) <= text.length && !WindHexActivity.canBreakSearch )
        {
            j = pattern.length - 1;
            
            while ( text[ i + j ] == pattern[ j ] )
            {
                j--;
                if ( j < 0 )
                {
                    currentOffset = (startOffset + i);
                    return true;
                }
            }
            //
            i += shift[ text[ i + pattern.length - 1 ] & 0xFF ];
        }
        
        currentOffset = (startOffset + i);
        return false;
    }
    
    
    /**
     * External call to update list
     */
    public void resetAdapter()
    {
        findAdapter.notifyDataSetChanged();
    }
    
    
    /**
     * Adapter for List of finds
     */
    private class FoundListAdapter
            extends BaseAdapter
            implements AdapterView.OnItemClickListener
    {
        private ArrayList<Tabler> foundList;
        private ListView          listView;
        
        private FoundListAdapter( ArrayList<Tabler> foundList, ListView listView )
        {
            this.foundList = foundList;
            this.listView = listView;
            
            this.listView.setOnItemClickListener( this );
        }
        
        /**
         * Select the offset and leave
         *
         * @param parent   N/A
         * @param view     N/A
         * @param position N/A
         * @param id       N/A
         */
        @Override
        public void onItemClick( AdapterView<?> parent, View view, int position, long id )
        {
            Tabler item       = foundList.get( position );
            int    screenSize = global.screenSize;
            
            try
            {
                if ( screenSize <= 0 )
                {
                    screenSize = (global.rowCount * global.columnCount);
                }
                
                //
                global.setPosition( item.getOffset() );
                global.xyHex.x = global.getPosition();
                global.xyHex.y = (global.xyHex.x + WindHexActivity.saveSearch.length) - 1;
                global.setPosition( (item.getOffset() / screenSize) * screenSize );
                global.hView.invalidate();
                //
                selectItem = position;
                notifyDataSetChanged();
            }
            catch ( Exception ex )
            {
                Toast.makeText( mContext, "Screen resolution error...", Toast.LENGTH_SHORT ).show();
                ex.printStackTrace();
            }
        }
        
        
        @Override
        public int getCount()
        {
            if ( foundList == null )
            {
                return 0;
            }
            else
            {
                return foundList.size();
            }
        }
        
        @Override
        public Object getItem( int position )
        {
            return foundList.get( position );
        }
        
        @Override
        public long getItemId( int position )
        {
            return position;
        }
        
        /**
         * Construct a view for each viewable item
         *
         * @param position    N/A
         * @param convertView N/A
         * @param parent      N/A
         *
         * @return N/A
         */
        @Override
        public View getView( int position, View convertView, ViewGroup parent )
        {
            View     view;
            TextView findText;
            Tabler   item = foundList.get( position );
            
            //
            // GridView adapter
            //
            if ( convertView == null )
            {
                LayoutInflater inflater = LayoutInflater.from( mContext );
                convertView = inflater.inflate( R.layout.found_item, parent, false );
                view = convertView;
            }
            else
            {
                view = convertView;
            }
            
            findText = view.findViewById( R.id.searchMainText );
            findText.setText( item.getLabel() );
            //
/*
            if ( position == selectItem )
            {
                view.setBackgroundColor( selectedColor );
            }
            else
            {
                view.setBackgroundColor( 0xFFFFFFFF );
            }
*/
            
            return view;
        }
    }
}
