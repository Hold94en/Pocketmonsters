package com.example.pocketmonsters.Activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pocketmonsters.Database.Repository;
import com.example.pocketmonsters.Model.MapObject;
import com.example.pocketmonsters.R;
import com.example.pocketmonsters.Utilis.AsyncTaskCallback;
import com.example.pocketmonsters.Utilis.ImageUtilities;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


import org.w3c.dom.Text;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class MapObjectBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetListener bottomSheetListener;
    private TextView textViewObjectName;
    private TextView textViewObjectSize;
    private TextView textViewObjectDistance;
    private TextView textViewHint;
    private Button buttonMainAction;
    private ImageView imageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_map_object, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI
        imageView = view.findViewById(R.id.img_map_object);
        textViewObjectName = view.findViewById(R.id.txt_map_object_name);
        textViewObjectSize = view.findViewById(R.id.txt_map_object_size);
        textViewObjectDistance = view.findViewById(R.id.txt_map_object_distance);
        textViewHint = view.findViewById(R.id.txt_map_object_hint);
        buttonMainAction = view.findViewById(R.id.btn_fight_eat);

        // Logic
        Bundle bundle = getArguments();
        if (bundle != null) {
            boolean canInteract = bundle.getBoolean("canInteract", false);
            double distance = bundle.getDouble("distance", 0);
            int mapObjectId = bundle.getInt("mapObjectId");
            setupBottomSheet(canInteract, mapObjectId, distance);
        }
    }

    private void setupBottomSheet(final boolean canInteract, final int mapObjectId, final double distance) {
        Repository.getInstance(getContext()).getMapObjectFromDb(mapObjectId, new AsyncTaskCallback() {
            @Override
            public void onPostExecution(final MapObject mapObject) {

                textViewObjectName.setText(mapObject.getName());
                imageView.setImageBitmap(ImageUtilities.getBitmapFromString(mapObject.getBase64Image()));

                String objectSize = mapObject.getSize();

                switch (objectSize) {
                    case "L": textViewObjectSize.setText(getString(R.string.object_size, getString(R.string.size_large)));
                        break;
                    case "M": textViewObjectSize.setText(getString(R.string.object_size, getString(R.string.size_medium)));
                        break;
                    case "S": textViewObjectSize.setText(getString(R.string.object_size, getString(R.string.size_small)));
                        break;
                    default: textViewObjectSize.setText(getString(R.string.object_size));
                }

                buttonMainAction.setEnabled(canInteract);

                if (distance > 1000) {
                    textViewObjectDistance.setText(getString(R.string.object_distance, (int)distance/1000, "km"));
                } else {
                    textViewObjectDistance.setText(getString(R.string.object_distance, distance, "m"));
                }

                if (distance <= 50) {
                    textViewObjectDistance.setTextColor(Color.GREEN);
                }else {
                    textViewHint.setText(R.string.too_far);
                    textViewObjectDistance.setTextColor(Color.RED);
                }

                if (mapObject.getType().equals("MO"))
                    buttonMainAction.setText(R.string.action_fight);
                else if (mapObject.getType().equals("CA"))
                    buttonMainAction.setText(R.string.action_eat);

                buttonMainAction.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        bottomSheetListener.onFightEatButtonClick(mapObject, imageView);
                    }
                });

            }

            @Override
            public void onPostExecution(List<MapObject> mapObjects) {

            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            bottomSheetListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            Log.d("DBG", "onAttach: " + e);
        }
    }

    public interface BottomSheetListener {
        void onFightEatButtonClick(MapObject mapObject, ImageView imageView);
    }
}
