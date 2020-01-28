package com.example.pocketmonsters.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.pocketmonsters.R;
import com.example.pocketmonsters.Database.Repository;
import com.example.pocketmonsters.Model.User;
import com.example.pocketmonsters.Utilis.VolleyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ActivityRanking extends AppCompatActivity {

    private List<User> users;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI
        setContentView(R.layout.activity_ranking);
        recyclerView = findViewById(R.id.recycler_view_rankig);
        Button mapActivityButton = findViewById(R.id.btn_close);

        // Logic
        users = new ArrayList<>();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mapActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityRanking.this.finish();
            }
        });

        Repository.getInstance(this).requestRanking(new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                parseRequestRankingResponse(response);
            }

            @Override
            public void onError(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), R.string.warning_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void parseRequestRankingResponse(JSONObject jsonObject) {

        try {

            JSONArray jsonArray = (JSONArray) jsonObject.get("ranking");
            for (int i = 0; i < jsonArray.length(); i ++) {
                JSONObject jsonUser = (JSONObject) jsonArray.get(i);
                User user = new User(jsonUser.getInt("lp"),
                        jsonUser.getInt("xp"),
                        jsonUser.getString("username"),
                        jsonUser.getString("img"));

                Log.d("DBG", "parseRequestRankingResponse: " + user.getExpPoints());
                users.add(user);
            }

            UserAdapter userAdapter = new UserAdapter(users);
            recyclerView.setAdapter(userAdapter);

        } catch (JSONException e) {
            Log.d("DBG", "parseRequestRankingResponse: " + e);
        }
    }
}
