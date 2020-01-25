package com.example.pocketmonsters.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Button mapActivityButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        users = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view_rankig);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mapActivityButton = findViewById(R.id.btn_close);
        mapActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent intent = new Intent(ActivityRanking.this, MapActivity.class);
                ActivityRanking.this.startActivity(intent);
                 */
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

            }
        });
    }

    public void parseRequestRankingResponse(JSONObject jsonObject) {

        try {

            JSONArray jsonArray = (JSONArray) jsonObject.get("ranking");
            for (int i = 0; i < jsonArray.length(); i ++) {
                //User user = new Gson().fromJson(jsonArray.get(i).toString(), User.class);
                JSONObject jsonUser = (JSONObject) jsonArray.get(i);
                User user = new User(jsonUser.getInt("lp"),
                        jsonUser.getInt("xp"),
                        jsonUser.getString("username"),
                        jsonUser.getString("img"));

                Log.d("DBG", "parseRequestRankingResponse: " + user.getExpPoints());
                users.add(user);
            }

            adapter = new UserAdapter(users);
            recyclerView.setAdapter(adapter);

        } catch (JSONException e) {
            Log.d("DBG", "parseRequestRankingResponse: " + e);
        }
    }
}
