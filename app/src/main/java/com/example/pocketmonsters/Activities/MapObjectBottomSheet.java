package com.example.pocketmonsters.Activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pocketmonsters.Model.MapObject;
import com.example.pocketmonsters.Model.ModelSingleton;
import com.example.pocketmonsters.R;
import com.example.pocketmonsters.Utilis.ImageUtilities;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MapObjectBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetListener bottomSheetListener;
    private TextView textViewObjectName;
    private TextView textViewObjectSize;
    private Button buttonMainAction;
    private ImageView imageView;

    private List<MapObject> mapObjects;

    private MapObject mapObject;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_map_object, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI
        bindViews(view);

        // Logic
        final Bundle bundle = getArguments();
        final boolean canInteract = bundle.getBoolean("canInteract");
        final int mapObjectId = bundle.getInt("mapObjectId");

        mapObject = ModelSingleton.getInstance().getMapObjectWithId(mapObjectId);

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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            bottomSheetListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            Log.d("DBG", "onAttach: " + e);
        }
    }

    private void bindViews(View view) {
        imageView = view.findViewById(R.id.img_map_object);
        textViewObjectName = view.findViewById(R.id.txt_map_object_name);
        textViewObjectSize = view.findViewById(R.id.txt_map_object_size);
        buttonMainAction = view.findViewById(R.id.btn_fight_eat);
    }

    public interface BottomSheetListener {
        void onFightEatButtonClick(MapObject mapObject, ImageView imageView);
    }
}
