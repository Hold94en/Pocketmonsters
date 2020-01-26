package com.example.pocketmonsters.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.pocketmonsters.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

public class WelcomeDialog extends AppCompatDialogFragment {

    private Context context;

    public WelcomeDialog(Context context) {
        this.context = context;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_welcome, null);

        TextView textViewTitle = view.findViewById(R.id.txt_welcome_title);
        textViewTitle.setText(R.string.welcome_title);

        ViewPager2 viewPager2 = view.findViewById(R.id.viewPager2);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        viewPager2.setAdapter(new ViewPagerAdapter());
        new TabLayoutMediator(tabLayout, viewPager2,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(null);
                    }
                }).attach();


        builder.setView(view).setPositiveButton("Inizia", null);

        return builder.create();
    }
}
