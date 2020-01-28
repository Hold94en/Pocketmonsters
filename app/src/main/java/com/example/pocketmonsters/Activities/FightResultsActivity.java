package com.example.pocketmonsters.Activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.pocketmonsters.Utilis.AsyncTaskCallback;
import com.example.pocketmonsters.Utilis.ImageUtilities;
import com.example.pocketmonsters.Model.MapObject;
import com.example.pocketmonsters.Model.ModelSingleton;
import com.example.pocketmonsters.R;
import com.example.pocketmonsters.Database.Repository;
import com.example.pocketmonsters.Model.User;
import com.example.pocketmonsters.Utilis.VolleyCallback;
import com.example.pocketmonsters.Utilis.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class FightResultsActivity extends AppCompatActivity {
    // Data
    private ModelSingleton modelSingleton;
    private Repository repository;
    private MapObject mapObject;
    private int userLp;
    private int userXp;

    // UI
    private ImageView userImageView;
    private TextView usernameTextView;
    private TextView userLpTextView;
    private TextView userXpTextView;
    private ImageView mapObjectImageView;
    private TextView mapObjectNameTextView;
    private TextView mapObjectLpTextView;
    private TextView mapObjectSizeTextView;
    private TextView fightStatusTextView;
    private TextView fightLpResultTextView;
    private TextView fightXpResultTextView;
    private ProgressBar progressBar;
    private Button closeButton;
    private Button profileButton;
    private Button rankingsButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fight_results);

        // UI
        userImageView = findViewById(R.id.img_user);
        usernameTextView = findViewById(R.id.txt_user_name);
        userLpTextView = findViewById(R.id.txt_user_lp);
        userXpTextView = findViewById(R.id.txt_user_xp);
        mapObjectImageView = findViewById(R.id.img_map_object);
        mapObjectNameTextView = findViewById(R.id.txt_map_object_name);
        mapObjectLpTextView = findViewById(R.id.txt_map_object_lp);
        mapObjectSizeTextView = findViewById(R.id.txt_map_object_size);
        fightStatusTextView = findViewById(R.id.txt_fight_status);
        fightLpResultTextView = findViewById(R.id.txt_lp_result);
        fightXpResultTextView = findViewById(R.id.txt_xp_result);
        closeButton = findViewById(R.id.btn_close);
        profileButton = findViewById(R.id.btn_profile);
        rankingsButton = findViewById(R.id.btn_rankings);
        progressBar = findViewById(R.id.progressBar);

        Button editProfileButton = findViewById(R.id.btn_edit_profile);
        editProfileButton.setVisibility(View.GONE);

        userLpTextView.setSelected(true);
        userXpTextView.setSelected(true);
        mapObjectSizeTextView.setSelected(true);
        mapObjectLpTextView.setSelected(true);

        // Data
        repository = Repository.getInstance(this);
        modelSingleton = ModelSingleton.getInstance();
        User user = modelSingleton.getSignedUser();
        userLp = user.getLifePoints();
        userXp = user.getExpPoints();

        final int mapObjectId = getIntent().getIntExtra("mapObjectId",0);

        repository.getMapObjectFromDb(mapObjectId, new AsyncTaskCallback() {
            @Override
            public void onPostExecution(MapObject dbMapObject) {
                mapObject = dbMapObject;

                // mapObject UI
                mapObjectImageView.setImageBitmap(ImageUtilities.getBitmapFromString(mapObject.getBase64Image()));
                mapObjectNameTextView.setText(mapObject.getName());
                mapObjectLpTextView.setText(getString(R.string.life_points, 100));
                String objectSize = mapObject.getSize();

                switch (objectSize) {
                    case "L": mapObjectSizeTextView.setText(getString(R.string.object_size, getString(R.string.size_large)));
                        break;
                    case "M": mapObjectSizeTextView.setText(getString(R.string.object_size, getString(R.string.size_medium)));
                        break;
                    case "S": mapObjectSizeTextView.setText(getString(R.string.object_size, getString(R.string.size_small)));
                        break;
                    default: mapObjectSizeTextView.setText(getString(R.string.object_size));
                }

                Drawable draw = getDrawable(R.drawable.progress_bar);
                progressBar.setProgressDrawable(draw);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Wait half a second
                        progressBar.setVisibility(View.INVISIBLE);
                        startFight();
                    }
                }, 500);
            }

            @Override
            public void onPostExecution(List<MapObject> mapObjects) {

            }
        });

        // user UI
        if (user.getBase64Image() != null) {
            userImageView.setImageBitmap(ImageUtilities.getBitmapFromString(user.getBase64Image()));
        }

        if (user.getUsername() != null) {
            usernameTextView.setText(user.getUsername());
        }

        userLpTextView.setText(getString(R.string.life_points, user.getLifePoints()));
        userXpTextView.setText(getString(R.string.exp_points, user.getExpPoints()));

        // Logic
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FightResultsActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        rankingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FightResultsActivity.this, ActivityRanking.class);
                startActivity(intent);
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VolleySingleton.getInstance(FightResultsActivity.this).getRequestQueue().cancelAll("volley");
                FightResultsActivity.this.finish();
            }
        });
    }

    public void startFight() {
        repository.requestFightEatResults(new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                updateModel(response);
                updateUi(response);
            }

            @Override
            public void onError(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), R.string.warning_generic, Toast.LENGTH_SHORT).show();
            }
        }, mapObject.getId());
    }


    public void updateModel(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("died")) {
                modelSingleton.getSignedUser().resetUserStats();
            } else {
                modelSingleton.getSignedUser().setLifePoints(jsonObject.getInt("lp"));
                modelSingleton.getSignedUser().setExpPoints(jsonObject.getInt("xp"));
                modelSingleton.removeSymbolWithId(mapObject.getId());
                mapObject.setAlive(false);
                repository.updateMapObject(mapObject);
            }

        } catch (JSONException e) {
            Log.d("DBG", "onSuccess: " + e);
        }
    }

    public void updateUi(JSONObject jsonObject) {
        ValueAnimator animatorLp = new ValueAnimator();
        ValueAnimator animatorXp = new ValueAnimator();

        profileButton.setVisibility(View.VISIBLE);
        rankingsButton.setVisibility(View.VISIBLE);
        closeButton.setVisibility(View.VISIBLE);

        Log.d("DBG", "after fight: " + modelSingleton.getSignedUser().toString());

        try {
            if (jsonObject.getBoolean("died")) {

                fightStatusTextView.setText(getString(R.string.fight_result_dead));
                userLpTextView.setText(getString(R.string.life_points, 0));
                userXpTextView.setText(getString(R.string.exp_points, 0));
                fightLpResultTextView.setText(R.string.fight_explanation_death);

            } else {

                fightStatusTextView.setText(getString(R.string.fight_result_win));
                int lpLost = userLp - ModelSingleton.getInstance().getSignedUser().getLifePoints();
                int xpGained =  ModelSingleton.getInstance().getSignedUser().getExpPoints() - userXp;

                mapObjectLpTextView.setText(getString(R.string.life_points, 0));

                animatorLp.setDuration(800);
                animatorXp.setDuration(800);

                animatorLp.setObjectValues(0, lpLost);
                animatorLp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        fightLpResultTextView.setText(getString(R.string.life_points_lost, String.valueOf(animation.getAnimatedValue())));
                        userLpTextView.setText(getString(R.string.life_points, ModelSingleton.getInstance().getSignedUser().getLifePoints()));
                    }
                });
                animatorLp.start();

                animatorXp.setObjectValues(0, xpGained);
                animatorXp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        fightXpResultTextView.setText(getString(R.string.xp_points_gained, String.valueOf(animation.getAnimatedValue())));
                        userXpTextView.setText(getString(R.string.exp_points, ModelSingleton.getInstance().getSignedUser().getExpPoints()));
                    }
                });
                animatorXp.start();
            }

        } catch (JSONException e) {
            Log.d("DBG", "onSuccess: " + e);
        }
    }

    public void updateUserUi() {
        if (modelSingleton.getSignedUser().getUsername() != null) {
            usernameTextView.setText(modelSingleton.getSignedUser().getUsername());
        }

        if (modelSingleton.getSignedUser().getBase64Image() != null)
            userImageView.setImageBitmap(ImageUtilities.getBitmapFromString(modelSingleton.getSignedUser().getBase64Image()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserUi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VolleySingleton.getInstance(this).getRequestQueue().cancelAll("volley");
        repository.saveUserToSharedPrefs(modelSingleton.getSignedUser());
    }
}
