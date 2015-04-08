package com.osm.mapbudo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;

public class ValuesActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_values);

		Bundle b=getIntent().getExtras();
		
		PlaceholderFragmentValue fra=new PlaceholderFragmentValue();
		fra.setArguments(b);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, fra).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.values, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragmentValue extends Fragment {

		public PlaceholderFragmentValue() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
			this.getActivity().getIntent();
			
			
			View rootView = inflater.inflate(R.layout.fragment_values,container, false);
            GridLayout gl = (GridLayout)getView().findViewById(R.id.grid);
            //rootView.findViewById(R.id.grid);
            Field f=new Field("titol","");

            rootView = f.addView(this.getActivity(),gl);
			//Bundle b=this.getActivity().getIntent().getExtras();
			Button save=(Button) rootView.findViewById(R.id.save);


			final Float lat=this.getActivity().getIntent().getExtras().getFloat("_lat");
			final Float lon=this.getActivity().getIntent().getExtras().getFloat("_lon");

			//final EditText etName=(EditText)rootView.findViewById(R.id.etName);
			//final EditText etAdress=(EditText)rootView.findViewById(R.id.etAdress);
			//final EditText etDescription=(EditText)rootView.findViewById(R.id.etDescription);
			//final EditText etHours=(EditText)rootView.findViewById(R.id.etHours);
			//final EditText etPhone=(EditText)rootView.findViewById(R.id.etPhone);
			//final EditText etWeb=(EditText)rootView.findViewById(R.id.etWeb);

            /*etHours.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(final View vi, boolean hasFocus) {
                    if (!hasFocus)
                    {
                        FieldHour d = new FieldHour(vi.getContext(), ((EditText) vi).getText().toString());
                        d.onOK(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((EditText) vi).setText((String) v.getTag());
                            }
                        });
                        d.show();
                    }
                }
            });*/

			save.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent returnIntent = new Intent();
					returnIntent.putExtra("_lat",lat);
					returnIntent.putExtra("_lon",lon);
					/*if (! etName.getText().toString().equalsIgnoreCase(""))
					{
						returnIntent.putExtra("name",etName.getText().toString());
					}
					if (! etAdress.getText().toString().equalsIgnoreCase(""))
					{
						returnIntent.putExtra("adress",etAdress.getText().toString());
					}
					if (! etDescription.getText().toString().equalsIgnoreCase(""))
					{
						returnIntent.putExtra("description",etDescription.getText().toString());
					}
					if (! etHours.getText().toString().equalsIgnoreCase(""))
					{
						returnIntent.putExtra("opening_hours",etHours.getText().toString());
					}
					if (! etPhone.getText().toString().equalsIgnoreCase(""))
					{
						returnIntent.putExtra("phone",etPhone.getText().toString());
					}
					if (! etWeb.getText().toString().equalsIgnoreCase(""))
					{
						returnIntent.putExtra("web",etWeb.getText().toString());
					}*/
					getActivity().setResult(RESULT_OK,returnIntent);
					getActivity().finish();
					
				}
			});
			return rootView;
		}
	}

}
