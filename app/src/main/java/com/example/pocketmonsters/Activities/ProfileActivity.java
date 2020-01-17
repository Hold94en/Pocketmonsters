package com.example.pocketmonsters.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.example.pocketmonsters.Utilis.ImageUtilities;
import com.example.pocketmonsters.Model.ModelSingleton;
import com.example.pocketmonsters.R;
import com.example.pocketmonsters.Database.Repository;
import com.example.pocketmonsters.Model.User;
import com.example.pocketmonsters.Utilis.VolleyCallback;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ProfileActivity extends AppCompatActivity implements FragmentEditProfile.FragmentEditProfileListener {

    //UI
    private TextView profileTitle;
    private Button editProfileButton;
    private ImageView userImageView;
    private TextView userNameTextView;
    private TextView userLpTextView;
    private TextView userXpTextView;
    private Button buttonClose;
    private Button buttonRankings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        User user = ModelSingleton.getInstance().getSignedUser();

        profileTitle = findViewById(R.id.txt_profile_title);
        editProfileButton = findViewById(R.id.btn_edit_profile);
        buttonClose = findViewById(R.id.btn_close);
        userNameTextView = findViewById(R.id.txt_user_name);
        userImageView = findViewById(R.id.img_user);
        userLpTextView = findViewById(R.id.txt_user_lp);
        userXpTextView = findViewById(R.id.txt_user_xp);
        buttonRankings = findViewById(R.id.btn_rankings);
        editProfileButton.setVisibility(View.VISIBLE);

        if (user.getUsername() != null)
            userNameTextView.setText(user.getUsername());

        if (user.getBase64Image() != null) {
            userImageView.setImageBitmap(ImageUtilities.getBitmapFromString(user.getBase64Image()));
        }

        userLpTextView.setText(getString(R.string.life_points, user.getLifePoints()));
        userXpTextView.setText(getString(R.string.exp_points, user.getExpPoints()));

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFragment(new FragmentEditProfile(), false, "edit_profile_fragment");
            }
        });

        buttonRankings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ActivityRanking.class);
                ProfileActivity.this.startActivity(intent);
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MapActivity.class);
                ProfileActivity.this.startActivity(intent);
                ProfileActivity.this.finish();
            }
        });

    }

    public void toggleFragment(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_from_top, R.anim.slide_to_top);

        FragmentEditProfile myFragment = (FragmentEditProfile) getSupportFragmentManager().findFragmentByTag(tag);
        if (myFragment == null) {
            fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
            fragmentTransaction.commitAllowingStateLoss();

            buttonRankings.animate().alpha(0.0f).setDuration(100);
            buttonClose.animate().alpha(0.0f).setDuration(100);

        } else {
            fragmentTransaction.remove(myFragment).commit();

            buttonRankings.animate().alpha(1.0f).setDuration(100);
            buttonClose.animate().alpha(1.0f).setDuration(100);
        }

    }

    public void updateUserNameTextView(String username) {
        if (username.length() > 2)
            userNameTextView.setText(username);
        else
            Snackbar.make(findViewById(R.id.layout_profile), "Nome utente troppo corto! Inserisci almeno 3 caratteri", Snackbar.LENGTH_SHORT).show();
    }

    public void updateUserImageView(Bitmap bitmap) {
        if (bitmap != null)
            userImageView.setImageBitmap(bitmap);
        else
            Snackbar.make(findViewById(R.id.layout_profile), "Immagine non supportata", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onEditsSaved(final String username, final String base64image) {

        if (username.length() > 0 || base64image.length() > 0) {
            Repository.getInstance(ProfileActivity.this).sendUserData(new VolleyCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    Snackbar.make(findViewById(R.id.layout_profile), "Modifiche effettuate", Snackbar.LENGTH_SHORT).show();

                    if (username.length() > 0) {
                        ModelSingleton.getInstance().getSignedUser().setUsername(username);
                    }

                    if (base64image.length() > 0) {
                        ModelSingleton.getInstance().getSignedUser().setBase64Image(base64image);
                    }
                }

                @Override
                public void onError(VolleyError volleyError) {
                    Snackbar.make(findViewById(R.id.layout_profile), "Si è verificato un problema. Riprova più tardi.", Snackbar.LENGTH_SHORT).show();
                }
            }, new String[]{username, base64image});
        }
    }
}