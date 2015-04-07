package com.osm.mapbudo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class Settings extends ActionBarActivity {
	CheckBox cbSave;
	Button btExit;
	Button btAbout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		cbSave=(CheckBox) this.findViewById(R.id.cbSave);
		btExit=(Button) this.findViewById(R.id.btExit);
		btAbout=(Button) this.findViewById(R.id.btAbout);
		
		btAbout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), About.class);
				v.getContext().startActivity(intent);
			}
		});
		btExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent returnIntent = new Intent();
				returnIntent.putExtra("finish",true);
				setResult(RESULT_OK,returnIntent);
				finish();
			}
		});
		SharedPreferences preferences =  getSharedPreferences("com.osm.budomap", Context.MODE_PRIVATE);

		if (preferences.getBoolean("save_password", true)) {
			cbSave.setChecked(true);
		} else {
			cbSave.setChecked(false);
		}

		cbSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked){
					save_password();
				} else {
					no_save_password();
				}
			}
		});
	}
	public void save_password() {
		SharedPreferences preferences =  this.getSharedPreferences("com.osm.budomap", Context.MODE_PRIVATE);
		preferences.edit().putBoolean("valid_user", true).commit();
		preferences.edit().putBoolean("save_password", true).commit();
		
	}
	public void no_save_password() {
		SharedPreferences preferences =  this.getSharedPreferences("com.osm.budomap", Context.MODE_PRIVATE);
		preferences.edit().putBoolean("valid_user", false).commit();
		preferences.edit().putBoolean("save_password", false).commit();
		preferences.edit().remove("password").commit();
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
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
}
