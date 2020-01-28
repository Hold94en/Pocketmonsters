package com.example.pocketmonsters.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.pocketmonsters.Activities.ProfileActivity;
import com.example.pocketmonsters.R;
import com.example.pocketmonsters.Utilis.ImageUtilities;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class FragmentEditProfile extends Fragment {
    private static final int PICK_IMAGE = 1;
    private Bitmap bitmapImage;
    private FragmentEditProfileListener listener;
    private EditText usernameEditText;
    private Button saveEditsButton;
    private Boolean usernameSemaphore;
    private Boolean imageSemaphore;
    private ProfileActivity profileActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // UI
        Button importImageButton = v.findViewById(R.id.btn_import_image);
        saveEditsButton = v.findViewById(R.id.btn_save_edits);
        usernameEditText = v.findViewById(R.id.edit_username);

        // Logic
        profileActivity = (ProfileActivity) getActivity();
        usernameSemaphore = imageSemaphore = null;

        usernameEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {

                    usernameSemaphore = false;
                    profileActivity.updateUserNameTextView(usernameEditText.getText().toString());
                    usernameSemaphore = (usernameEditText.getText().toString().length() > 2);

                    if (imageSemaphore == null)
                        saveEditsButton.setEnabled(usernameSemaphore);
                    else
                        saveEditsButton.setEnabled(usernameSemaphore && imageSemaphore);
                }
                return false;
            }
        });


        importImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery, "Seleziona immagine"), PICK_IMAGE);
            }
        });


        saveEditsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String base64string = "";

                if (bitmapImage != null) {
                    base64string = ImageUtilities.getBase64StringFromBitmap(bitmapImage);
                }

                listener.onEditsSaved(usernameEditText.getText().toString(), base64string);
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageSemaphore = false;

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            try {
                bitmapImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                if (bitmapImage != null) {
                    profileActivity.updateUserImageView(bitmapImage);
                    imageSemaphore = true;
                } else {
                    profileActivity.updateUserImageView(BitmapFactory.decodeResource(profileActivity.getResources(),
                            R.drawable.ic_person_128dp));
                    imageSemaphore = false;
                }

                if (usernameSemaphore == null)
                    saveEditsButton.setEnabled(imageSemaphore);
                else
                    saveEditsButton.setEnabled(usernameSemaphore && imageSemaphore);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentEditProfileListener) {
            listener = (FragmentEditProfileListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FragmentEditProfileListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface FragmentEditProfileListener {
        void onEditsSaved(String username, String base64image);
    }
}


