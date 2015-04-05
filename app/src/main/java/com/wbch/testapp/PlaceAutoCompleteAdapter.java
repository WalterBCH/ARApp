package com.wbch.testapp;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

public class PlaceAutoCompleteAdapter
        extends ArrayAdapter<PlaceAutoCompleteAdapter.PlaceAutoComplete>
        implements Filterable{

    private LatLngBounds mBounds;
    private AutocompleteFilter mPlaceFilter;
    private ArrayList<PlaceAutoComplete> mResultList;

    public PlaceAutoCompleteAdapter(Context context, int resource, LatLngBounds bounds, AutocompleteFilter filter) {
        super(context, resource);
        mBounds = bounds;
        mPlaceFilter = filter;
    }

    public void setBounds(LatLngBounds bounds){
        mBounds = bounds;
    }

    @Override
    public int getCount(){
        return mResultList.size();
    }

    @Override
    public PlaceAutoComplete getItem(int position){
        return mResultList.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results =  new FilterResults();

                if(constraint != null){
                    mResultList = getAutoComplete("hola");
                    if(mResultList != null){
                        results.values = mResultList;
                        results.count = mResultList.size();
                    }
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null && results.count > 0){
                    notifyDataSetChanged();
                }else{
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    private ArrayList<PlaceAutoComplete> getAutoComplete(CharSequence constraint){
        ArrayList resultList = new ArrayList<>(10);
        resultList.add(new PlaceAutoComplete("1", "hola"));
        resultList.add(new PlaceAutoComplete("12", "hola1"));
        resultList.add(new PlaceAutoComplete("123", "hola2"));
        resultList.add(new PlaceAutoComplete("1234", "hola3"));
        resultList.add(new PlaceAutoComplete("12345", "hola4"));
        resultList.add(new PlaceAutoComplete("2", "hola5"));
        resultList.add(new PlaceAutoComplete("22", "hola6"));
        resultList.add(new PlaceAutoComplete("223", "hola7"));
        resultList.add(new PlaceAutoComplete("2234", "hola8"));
        resultList.add(new PlaceAutoComplete("22345", "hola9"));
        return resultList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        View rowList = convertView;

        if(rowList == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowList = inflater.inflate(R.layout.auto_complete_list_item, null);
        }

        PlaceAutoComplete item = mResultList.get(position);

        if(item != null){
            ImageView icon = (ImageView) rowList.findViewById(R.id.icon_list_item);
            TextView text = (TextView) rowList.findViewById(R.id.text_list_item);

            text.setText(item.toString());
        }

        return rowList;
    }

    class PlaceAutoComplete{
        public CharSequence placeId;
        public CharSequence description;

        PlaceAutoComplete(CharSequence placeId, CharSequence description){
            this.placeId = placeId;
            this.description = description;
        }

        @Override
        public String toString() {
            return description.toString();
        }
    }
}
