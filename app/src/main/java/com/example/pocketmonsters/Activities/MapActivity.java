package com.example.pocketmonsters.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.pocketmonsters.Utilis.AsyncTaskCallback;
import com.example.pocketmonsters.Utilis.ImageUtilities;
import com.example.pocketmonsters.Model.MapObject;
import com.example.pocketmonsters.Model.ModelSingleton;
import com.example.pocketmonsters.R;
import com.example.pocketmonsters.Database.Repository;
import com.example.pocketmonsters.Utilis.VolleyCallback;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, WelcomeDialog.WelcomeDialogListener, MapObjectBottomSheet.BottomSheetListener {
    private String TAG;
    private Repository repository;

    private MapboxMap mapboxMap;
    private MapView mapView;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private SymbolManager symbolManager;
    private MapObjectBottomSheet mapObjectBottomSheet;
    private CardView cardViewHud;

    private ImageButton userImageButton;

    private TextView textViewLifePoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token
        Mapbox.getInstance(this, getString(R.string.map_box_access_token));
        setContentView(R.layout.activity_map);

        // Mapbox base
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(MapActivity.this);

        // Data
        repository = Repository.getInstance(this);
        TAG = "DBG";

        // UI
        cardViewHud = findViewById(R.id.hud);
        userImageButton = findViewById(R.id.img_button_user);

        if (ModelSingleton.getInstance().getSignedUser().getBase64Image() != null) {
            userImageButton.setImageBitmap(ImageUtilities.getBitmapFromString(ModelSingleton.getInstance().getSignedUser().getBase64Image()));
        }

        userImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, ProfileActivity.class);
                MapActivity.this.startActivity(intent);
            }
        });

        textViewLifePoints = findViewById(R.id.txt_lp_short);
        textViewLifePoints.setText(getString(R.string.life_points_short, ModelSingleton.getInstance().getSignedUser().getLifePoints()));

    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);

                Log.d(TAG, "onStyleLoaded: map is ready");

                repository.getMapObjectsFromDb(new AsyncTaskCallback() {
                    @Override
                    public void onPostExecution(List<MapObject> list) {
                        if (list.size() == 0) {
                            Log.d(TAG, "onPostExecution: objects not found");
                            repository.requestMap(new VolleyCallback() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    parseRequestMapResponse(response);
                                }

                                @Override
                                public void onError(VolleyError volleyError) {
                                    Log.d(TAG, "onError: " + volleyError);
                                }
                            });
                        } else {
                            Log.d(TAG, "onPostExecution: " + list.size() + " objects found");
                            ModelSingleton.getInstance().setMapObjects(list);
                            addSymbolsToMap(list);
                        }
                    }
                });

                // Set up objects layer
                Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_android_24dp, null);
                Bitmap mapObjectImage = BitmapUtils.getBitmapFromDrawable(drawable);
                style.addImage("object-image", mapObjectImage);

                SymbolLayer objectsLayer = new SymbolLayer("objects-layer", "");
                objectsLayer.setProperties(PropertyFactory.iconImage("object-image"));
                style.addLayer(objectsLayer);

                symbolManager = new SymbolManager(mapView, mapboxMap, style);
                symbolManager.setIconAllowOverlap(true);
                symbolManager.setIconIgnorePlacement(true);
                symbolManager.addClickListener(new OnSymbolClickListener() {
                    @Override
                    public void onAnnotationClick(Symbol symbol) {
                        // show bottom sheet
                        if(mapObjectBottomSheet == null || !mapObjectBottomSheet.isVisible())
                            showObjectDetail(symbol);
                    }
                });
            }
        });
    }


    private void parseRequestMapResponse(JSONObject jsonObject) {
        try {
            JSONArray jsonArray = (JSONArray) jsonObject.get("mapobjects");
            for (int i = 0; i < jsonArray.length(); i++) {
                final MapObject mapObject = new Gson().fromJson(jsonArray.get(i).toString(), MapObject.class);
                repository.requestImage(new VolleyCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            mapObject.setBase64Image(response.getString("img"));
                            ModelSingleton.getInstance().getMapObjects().add(mapObject);
                            repository.insertMapObject(mapObject);
                            addSymbolToMap(mapObject);
                        } catch (JSONException e){
                            Log.d(TAG, "onSuccess: " + e);
                        }
                    }

                    @Override
                    public void onError(VolleyError volleyError) {
                        Log.d(TAG, "onError: " + volleyError);
                    }
                }, mapObject);
            }
        } catch (JSONException e) {
            Log.d(TAG, "parseRequestMapResponse: " + e);
        }
    }

    private void addSymbolsToMap(List<MapObject> mapObjects) {
        for (int i = 0; i < mapObjects.size(); i++) {
            MapObject mapObject = mapObjects.get(i);


            symbolManager.create(new SymbolOptions()
                    .withLatLng(new LatLng(mapObject.getLat(), mapObject.getLon()))
                    .withTextField(mapObject.getName())
                    .withIconImage("object-image")
                    .withData(new Gson().toJsonTree(mapObject, MapObject.class))
                    .withIconSize(1.0f)
            );
        }
    }

    private void addSymbolToMap(MapObject mapObject) {

        symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(mapObject.getLat(), mapObject.getLon()))
                .withTextField(mapObject.getName())
                .withIconImage("object-image")
                .withData(new Gson().toJsonTree(mapObject, MapObject.class))
                .withIconSize(1.0f)
        );
    }

    private void showObjectDetail(Symbol symbol) {

        if (symbol != null) {
            Gson gson = new Gson();
            JsonElement data = symbol.getData();
            MapObject mapObject = gson.fromJson(data, MapObject.class);

            if (mapObject != null) {

                LatLng lastKnownLocation = new LatLng();
                Bundle bundle = new Bundle();

                boolean canInteract;

                try {
                    lastKnownLocation.setLatitude(locationComponent.getLastKnownLocation().getLatitude());
                    lastKnownLocation.setLongitude(locationComponent.getLastKnownLocation().getLongitude());
                    canInteract = (ModelSingleton.getInstance().getSignedUser().canInteract(lastKnownLocation, mapObject));
                    bundle.putBoolean("canInteract", canInteract);
                } catch (Exception e) {
                    Log.d(TAG, "onAnnotationClick: " + e);
                    bundle.putBoolean("canInteract", false);

                    Toast.makeText(MapActivity.this,
                            "Attiva la localizzazione per giocare!", Toast.LENGTH_SHORT).show();
                }

                bundle.putInt("mapObjectId", mapObject.getId());

                mapObjectBottomSheet = new MapObjectBottomSheet();
                mapObjectBottomSheet.setArguments(bundle);
                mapObjectBottomSheet.show(getSupportFragmentManager(), "mapObjectBottomSheet");

            }

        }

    }


    @Override
    public void onFightEatButtonClick(final MapObject mapObject, ImageView imageView) {

        if (mapObject.getType().equals("MO")) {
            Log.d(TAG, "onFightEatButtonClick: cliccato su mostro");
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, imageView, ViewCompat.getTransitionName(imageView));
            Intent intent = new Intent(this, FightResultsActivity.class);
            intent.putExtra("mapObjectId", mapObject.getId());
            startActivity(intent);
        } else {

            VolleyCallback volleyCallback = new VolleyCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        Log.d(TAG, "onSuccess: "+ ModelSingleton.getInstance().getSignedUser().toString());
                        ModelSingleton.getInstance().getSignedUser().setLifePoints(response.getInt("lp"));
                        ModelSingleton.getInstance().removeMapObjectWithId(mapObject.getId());
                        repository.deleteMapObject(mapObject);
                        // Update UI
                        textViewLifePoints.setText(getString(R.string.life_points_short, ModelSingleton.getInstance().getSignedUser().getLifePoints()));
                        updateSymbols();
                    } catch (JSONException e){
                        Log.d(TAG, "requestFightEatResults: " + e);
                    }
                }
                @Override
                public void onError(VolleyError volleyError) {
                    Log.d(TAG, "onError: " + volleyError);
                }
            };

            repository.requestFightEatResults(volleyCallback, mapObject.getId());
        }

        mapObjectBottomSheet.dismiss();
    }


    public void openWelcomeDialog() {

        WelcomeDialog welcomeDialog = new WelcomeDialog();
        welcomeDialog.show(getSupportFragmentManager(), "firstRunDialog");

    }

    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // permissions granted

            locationComponent = mapboxMap.getLocationComponent();

            LocationComponentOptions locationComponentOptions = LocationComponentOptions.builder(this)
                    .elevation(5)
                    .accuracyAlpha(.6f)
                    .build();

            LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions
                    .builder(this, loadedMapStyle)
                    .locationComponentOptions(locationComponentOptions)
                    .build();

            // Activate with options
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            locationComponent.activateLocationComponent(locationComponentActivationOptions);

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, R.string.location_permission_not_granted, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUsernameEntered(String username) {
        ModelSingleton.getInstance().getSignedUser().setUsername(username);
    }

    public void updateSymbols() {

        if (symbolManager != null) {

            for (int i = 0; i < symbolManager.getAnnotations().size(); i++) {

                Symbol symbol = symbolManager.getAnnotations().get(i);

                if (symbol != null) {

                    JsonElement data = symbol.getData();
                    MapObject mapObject = new Gson().fromJson(data, MapObject.class);

                    if (ModelSingleton.getInstance().getMapObjectWithId(mapObject.getId()) == null) {
                        symbolManager.delete(symbol);
                    }

                } else {
                    Log.d(TAG, "updateSymbols: found null symbol" + symbol);
                }

            }

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        repository.saveUserToSharedPrefs(ModelSingleton.getInstance().getSignedUser());
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ON RESUME!");

        updateSymbols();

        if (ModelSingleton.getInstance().getSignedUser().getBase64Image() != null) {
            userImageButton.setImageBitmap(ImageUtilities.getBitmapFromString(ModelSingleton.getInstance().getSignedUser().getBase64Image()));
        }

        textViewLifePoints.setText(getString(R.string.life_points_short, ModelSingleton.getInstance().getSignedUser().getLifePoints()));

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}

