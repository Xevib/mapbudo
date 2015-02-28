package com.osm.mapbudo;
 
//import MainActivitty;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

 
public class ExpandableListAdapter extends BaseExpandableListAdapter {
 
	private Context _context;
	//private Filter filter;
	
    public ExpandableListAdapter(Context context, Filter filt ){
        this._context = context;
        //this.filter=filt;
    }
 
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
    	return ((MainActivity)this._context).getFilter().getType(groupPosition, childPosititon).getName();
    }
 
    @Override
    public long getChildId(int grlblListItemoupPosition, int childPosition) {
        return childPosition;
    }
 
    @Override
    public View getChildView(int groupPosition, final int childPosition,boolean isLastChild, View convertView, ViewGroup parent) {
 
        final String childText = (String) getChild(groupPosition, childPosition);
        
        LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.list_item, null);

        CheckBox txtListChild = (CheckBox) convertView.findViewById(R.id.lblListItem);
        ImageView imgICon=(ImageView) convertView.findViewById(R.id.imgIcon);
        
        Drawable icon=((MainActivity)this._context).getFilter().getIcon(childText);
        if (icon!=null)
        {
        	imgICon.setImageDrawable(icon);
        }
        txtListChild.setText(childText);
        Log.v("filter", childText+"="+((MainActivity)this._context).getFilter().getStatus(childText).toString());
        txtListChild.setChecked(((MainActivity)this._context).getFilter().getStatus(childText));
        
        Pair <String,Integer> tag=new Pair<String,Integer>(((MainActivity)this._context).getFilter().getGroup(groupPosition),childPosition);
        txtListChild.setTag(tag);
        txtListChild.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				CheckBox cb=(CheckBox) buttonView;

				//cb.setChecked(false);
				//1->GroupName
				//2->ChildPosition
				Pair<String,Integer> positions=(Pair<String, Integer>) buttonView.getTag();
				if (((MainActivity) buttonView.getContext()).getModeAddPOI())
				{
					cb.setChecked(!cb.isChecked());
					((MainActivity) buttonView.getContext()).setTypeOfNewPOI(positions.first,positions.second);
					((MainActivity) buttonView.getContext()).showValues();	
				}
				else
				{
					((MainActivity) buttonView.getContext()).changeFilter(positions.first,positions.second,cb.isChecked());
					((MainActivity) buttonView.getContext()).refreshPOIs();
				}
				
				Log.v("test", "uncheck "+String.valueOf(positions.first)+","+String.valueOf(positions.second) );
				
				
			}
		});
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
    	return ((MainActivity)this._context).getFilter().getNumElements(groupPosition);

    }
 
    @Override
    public Object getGroup(int groupPosition) {
    	return ((MainActivity)this._context).getFilter().getGroups().get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return ((MainActivity)this._context).getFilter().getNumGroups();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,View convertView, ViewGroup parent) {
        String headerTitle = (String) this.getGroup(groupPosition);
        if (convertView == null) {
        	LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }
        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
        return convertView;
    }
 
    @Override
    public boolean hasStableIds() {
        return false;
    }	
    
    
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}