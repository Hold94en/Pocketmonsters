package com.example.pocketmonsters.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.VolleyError;
import com.example.pocketmonsters.Model.ModelSingleton;
import com.example.pocketmonsters.R;
import com.example.pocketmonsters.Database.Repository;
import com.example.pocketmonsters.Utilis.VolleyCallback;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.SplashTheme);
        setContentView(R.layout.splash_screen);

        Repository repository = Repository.getInstance(this);
        final Intent intent = new Intent(SplashActivity.this, MapActivity.class);

        if (repository.isUserInSharedPrefs()) {

            ModelSingleton.getInstance().updateSignedUser(repository.getUserFromSharedPrefs());
            Log.d("DBG", "Splash Activity onCreate: user found; " + ModelSingleton.getInstance().getSignedUser().toString());
            startActivity(intent);

        } else {

            Log.d("DBG", "Splash Activity onCreate: user not found");

            repository.requestSessionId(new VolleyCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        String sessionId = response.get("session_id").toString();
                        ModelSingleton.getInstance().getSignedUser().setSessionId(sessionId);
                        SplashActivity.this.startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        Log.d("DBG", "onSuccess: " + e);
                    }
                }

                @Override
                public void onError(VolleyError volleyError) {
                    Log.d("DBG", "onError: " + volleyError);
                }
            });
        }
    }
}
