package com.example.pocketmonsters.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.pocketmonsters.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class WelcomeDialog extends AppCompatDialogFragment {
    private EditText editTextUsername;
    private WelcomeDialogListener welcomeDialogListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_welcome, null);

        builder.setView(view)
                .setTitle("Profilo")
                .setNegativeButton("Non ora", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String username = editTextUsername.getText().toString();
                        welcomeDialogListener.onUsernameEntered(username);
                    }
                });

        editTextUsername = view.findViewById(R.id.edit_username);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            welcomeDialogListener = (WelcomeDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "Class must implement FirstRunDialogListener");
        }

    }

    public interface WelcomeDialogListener {
        void onUsernameEntered(String username);
    }
}
