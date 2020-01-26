package com.example.pocketmonsters.Activities;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pocketmonsters.Model.User;
import com.example.pocketmonsters.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewPagerAdapter  extends RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder> {

    private Context context;

    public static class PagerViewHolder extends RecyclerView.ViewHolder {

        private TextView titleTextView;
        private ImageView imageView;
        private TextView firstExplanationTextView;
        private TextView secondExplanationTextView;


        public PagerViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.txt_action_title);
            imageView = itemView.findViewById(R.id.img_explanation);
            firstExplanationTextView = itemView.findViewById(R.id.txt_action_explanation_1);
            secondExplanationTextView = itemView.findViewById(R.id.txt_action_explanation_2);
        }
    }

    public ViewPagerAdapter() {

    }

    @NonNull
    @Override
    public ViewPagerAdapter.PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_welcome_dialog, parent, false);
        ViewPagerAdapter.PagerViewHolder pagerViewHolder = new ViewPagerAdapter.PagerViewHolder(v);
        return pagerViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerAdapter.PagerViewHolder holder, int position) {

        if (position == 0) {

            holder.titleTextView.setText(R.string.action_fight);
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_android_24dp));
            holder.firstExplanationTextView.setText(R.string.welcome_fight_explanation_1);
            holder.secondExplanationTextView.setText(R.string.welcome_fight_explanation_2);

        }

        if (position == 1) {

            holder.titleTextView.setText(R.string.action_eat);
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_cake_24dp));
            holder.firstExplanationTextView.setText(R.string.welcome_eat_explanation_1);
            holder.secondExplanationTextView.setText(R.string.welcome_eat_explanation_2);

        }


        if (position == 2) {
            holder.titleTextView.setText(R.string.welcome_rankings_title);
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_people_24dp));
            holder.firstExplanationTextView.setText(R.string.welcome_rankings_explanation_1);
            holder.secondExplanationTextView.setText(R.string.welcome_rankings_explanation_2);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

