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

import com.android.volley.VolleyError;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class FightResultsActivity extends AppCompatActivity {
    // Data
    private MapObject monster;
    private User user;
    private int userLp;
    private int userXp;

    // UI
    private Handler handler;
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
    private Button editProfileButton;

    private Repository repository;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fight_results);

        // Data
        repository = Repository.getInstance(this);
        user = ModelSingleton.getInstance().getSignedUser();
        userLp = user.getLifePoints();
        userXp = user.getExpPoints();
        monster = ModelSingleton.getInstance().getMapObjectWithId(getIntent().getIntExtra("mapObjectId",0));

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
        editProfileButton = findViewById(R.id.btn_edit_profile);
        editProfileButton.setVisibility(View.GONE);

        userLpTextView.setSelected(true);
        userXpTextView.setSelected(true);
        mapObjectSizeTextView.setSelected(true);
        mapObjectLpTextView.setSelected(true);

        // user
        if (user.getBase64Image() != null) {
            userImageView.setImageBitmap(ImageUtilities.getBitmapFromString(user.getBase64Image()));
        }

        if (user.getUsername() != null) {
            usernameTextView.setText(user.getUsername());
        }

        userLpTextView.setText(getString(R.string.life_points, user.getLifePoints()));
        userXpTextView.setText(getString(R.string.exp_points, user.getExpPoints()));

        // monster
        mapObjectImageView.setImageBitmap(ImageUtilities.getBitmapFromString(monster.getBase64Image()));
        mapObjectNameTextView.setText(monster.getName());
        mapObjectLpTextView.setText(getString(R.string.life_points, 100));
        String objectSize = monster.getSize();
        switch (objectSize) {
            case "L": mapObjectSizeTextView.setText(getString(R.string.object_size, getString(R.string.size_large)));
                break;
            case "M": mapObjectSizeTextView.setText(getString(R.string.object_size, getString(R.string.size_medium)));
                break;
            case "S": mapObjectSizeTextView.setText(getString(R.string.object_size, getString(R.string.size_small)));
                break;
            default: mapObjectSizeTextView.setText(getString(R.string.object_size));
        }


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


        Drawable draw = getDrawable(R.drawable.progress_bar);
        progressBar.setProgressDrawable(draw);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Wait 1 second
                Log.d("DBG", "before fight: " + user.toString());
                progressBar.setVisibility(View.INVISIBLE);
                startFight();
            }
        }, 1000);
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

            }
        }, monster.getId());
    }


    public void updateModel(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("died")) {
                ModelSingleton.getInstance().getSignedUser().resetLifePoints();
            } else {
                // TODO remove object only when I'll know how to remove symbol
                ModelSingleton.getInstance().getSignedUser().setLifePoints(jsonObject.getInt("lp"));
                ModelSingleton.getInstance().getSignedUser().setExpPoints(jsonObject.getInt("xp"));
                ModelSingleton.getInstance().removeMapObjectWithId(monster.getId());
                repository.deleteMapObject(monster);
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

        Log.d("DBG", "after fight: " + ModelSingleton.getInstance().getSignedUser().toString());

        try {
            if (jsonObject.getBoolean("died")) {
                fightStatusTextView.setText(getString(R.string.fight_result_dead));
                userLpTextView.setText(getString(R.string.life_points, 0));
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
        if (ModelSingleton.getInstance().getSignedUser().getUsername() != null) {
            usernameTextView.setText(ModelSingleton.getInstance().getSignedUser().getUsername());
        }

        if (ModelSingleton.getInstance().getSignedUser().getBase64Image() != null)
            userImageView.setImageBitmap(ImageUtilities.getBitmapFromString(ModelSingleton.getInstance().getSignedUser().getBase64Image()));
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
        repository.saveUserToSharedPrefs(ModelSingleton.getInstance().getSignedUser());
    }
}
