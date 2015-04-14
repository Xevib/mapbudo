package com.osm.mapbudo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends FragmentActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		SharedPreferences preferences =  getSharedPreferences("com.osm.budomap", Context.MODE_PRIVATE);
		if (!preferences.contains("save_password")) {
			preferences.edit().putBoolean("save_password", true);
		}
		if (preferences.getBoolean("valid_user", false) &&(preferences.getBoolean("save_password", true))&&(preferences.contains("password"))) {
			Intent i=new Intent(this, MainActivity.class);
			startActivity(i);
		} else {
			if (savedInstanceState == null) {
				getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class PlaceholderFragment extends Fragment {

		EditText etUsername;
		EditText etPassword;
		public PlaceholderFragment() {
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

			View rootView = inflater.inflate(R.layout.fragment_login,container, false);
			Button btLogin=(Button)rootView.findViewById(R.id.btLogin);
			SharedPreferences preferences =  getActivity().getSharedPreferences("com.osm.budomap", Context.MODE_PRIVATE);
            etUsername=(EditText)rootView.findViewById(R.id.etUsername);
            etPassword=(EditText)rootView.findViewById(R.id.etPassword);
			if (preferences.getBoolean("save_password", true)) {
				etUsername.setText(preferences.getString("user", ""));
			}


            Toast.makeText(getActivity(),rootView.getContext().getString(R.string.loginMessage), Toast.LENGTH_LONG).show();
			btLogin.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    OSMAPI api = new OSMAPI(v.getContext());
                    etUsername = (EditText) getActivity().findViewById(R.id.etUsername);
                    etPassword = (EditText) getActivity().findViewById(R.id.etPassword);


                    Boolean valid = api.getUserDetails(etUsername.getText().toString(), etPassword.getText().toString());

                    if (valid) {
                        SharedPreferences preferences = getActivity().getSharedPreferences("com.osm.budomap", Context.MODE_PRIVATE);
                        preferences.edit().putBoolean("valid_user", true).commit();
                        preferences.edit().putString("user", etUsername.getText().toString()).commit();
                        preferences.edit().putString("password", etPassword.getText().toString()).commit();
                        Intent i = new Intent(getActivity(), MainActivity.class);
                        startActivity(i);
                    } else {
                        etUsername.setError(getActivity().getResources().getString(R.string.invalid_user_etxt));
                        etPassword.setError(getActivity().getResources().getString(R.string.invalid_password_etxt));
                        Toast.makeText(getActivity().getApplicationContext(), getActivity().getResources().getString(R.string.invalid_user), Toast.LENGTH_LONG).show();
                    }
                }
            });
			return rootView;
		}
	}
	
}
