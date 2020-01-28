package com.example.pocketmonsters.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapObjectBottomSheet.BottomSheetListener {
    private Repository repository;
    private ModelSingleton modelSingleton;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private SymbolManager symbolManager;
    private MapObjectBottomSheet mapObjectBottomSheet;
    private ImageButton userImageButton;
    private TextView textViewLifePoints;
    private Style mapStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("DBG", "MapActivity onCreate: ");

        // Mapbox access token
        Mapbox.getInstance(this, getString(R.string.map_box_access_token));
        setContentView(R.layout.activity_map);

        // Mapbox base
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(MapActivity.this);

        // Data
        repository = Repository.getInstance(this);
        modelSingleton = ModelSingleton.getInstance();

        // UI
        FloatingActionButton fabUserLocation = findViewById(R.id.fab_user_location);
        userImageButton = findViewById(R.id.img_button_user);

        // Logic
        textViewLifePoints = findViewById(R.id.txt_lp_short);
        textViewLifePoints.setText(getString(R.string.life_points_short, ModelSingleton.getInstance().getSignedUser().getLifePoints()));

        if (modelSingleton.getSignedUser().getBase64Image() != null) {
            userImageButton.setImageBitmap(ImageUtilities.getBitmapFromString(ModelSingleton.getInstance().getSignedUser().getBase64Image()));
        }

        userImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, ProfileActivity.class);
                MapActivity.this.startActivity(intent);
            }
        });

        fabUserLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (PermissionsManager.areLocationPermissionsGranted(getApplicationContext())) {
                    if (locationComponent.getLastKnownLocation() != null) {

                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(locationComponent.getLastKnownLocation()))
                                .zoom(14)
                                .build();

                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500);

                    } else {
                        Snackbar.make(findViewById(R.id.coordinator_layout), R.string.turn_on_location, Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(findViewById(R.id.coordinator_layout), R.string.location_permission_not_granted, Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        if (ModelSingleton.getInstance().getSignedUser().isFirstRun()) {

            openWelcomeDialog();

        }
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.OUTDOORS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);

                mapStyle = style;

                Log.d("DBG", "onStyleLoaded: map is ready");

                Drawable candyDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cake_24dp, null);
                Drawable monsterDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_android_24dp, null);

                style.addImage("candy-image", ImageUtilities.drawableToBitmap(candyDrawable));
                style.addImage("monster-image", ImageUtilities.drawableToBitmap(monsterDrawable));

                symbolManager = new SymbolManager(mapView, mapboxMap, style);
                symbolManager.setIconAllowOverlap(false);
                symbolManager.setIconIgnorePlacement(false);
                symbolManager.addClickListener(new OnSymbolClickListener() {
                    @Override
                    public void onAnnotationClick(Symbol symbol) {
                        // show bottom sheet if not already open
                        if (mapObjectBottomSheet == null || !mapObjectBottomSheet.isVisible())
                            showObjectDetail(symbol);
                    }
                });

                repository.getMapObjectsFromDb(new AsyncTaskCallback() {
                    @Override
                    public void onPostExecution(List<MapObject> list) {
                        if (list.size() == 0) {
                            Log.d("DBG", "onPostExecution: objects not found in db");
                            repository.requestMap(new VolleyCallback() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    parseRequestMapResponse(response);
                                }

                                @Override
                                public void onError(VolleyError volleyError) {
                                    Log.d("DBG", "onError: " + volleyError);
                                }
                            });
                        } else {
                            Log.d("DBG", "onPostExecution: " + list.size() + " objects found");
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i).isAlive())
                                    addSymbolToMap(list.get(i));
                            }
                        }
                    }

                    @Override
                    public void onPostExecution(MapObject mapObject) {

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

                repository.getMapObjectFromDb(mapObject.getId(), new AsyncTaskCallback() {
                    @Override
                    public void onPostExecution(MapObject dbMapObject) {
                        if (dbMapObject != null) {
                            // mapObject from server is already in room db
                            if (!dbMapObject.isAlive()) {
                                Log.d("DBG", "onPostExecution: dead object found in db");
                                // update object position and resuscitate it, monster or candy
                                dbMapObject.setAlive(true);
                                dbMapObject.setLat(mapObject.getLat());
                                dbMapObject.setLon(mapObject.getLon());
                                repository.updateMapObject(dbMapObject);
                                addSymbolToMap(dbMapObject);
                            }
                        } else {
                            // get mapObject image and insert it into room db
                            requestMapObjectImage(mapObject);
                            Log.d("DBG", "onPostExecution: sto richiedendo l'immage di " + mapObject.getName());
                        }
                    }

                    @Override
                    public void onPostExecution(List<MapObject> mapObjects) {

                    }
                });
            }
        } catch (JSONException e) {
            Log.d("DBG", "parseRequestMapResponse: " + e);
        }
    }


    private void requestMapObjectImage(final MapObject mapObject) {
        repository.requestImage(mapObject, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    mapStyle.addImage(mapObject.getName(), ImageUtilities.getBitmapFromString(response.getString("img")));
                    mapObject.setBase64Image(response.getString("img"));
                    mapObject.setAlive(true);
                    repository.insertMapObject(mapObject);
                    addSymbolToMap(mapObject);
                } catch (JSONException e){
                    Log.d("DBG", "onSuccess: " + e);
                }
            }

            @Override
            public void onError(VolleyError volleyError) {

            }
        });
    }


    private void addSymbolToMap(MapObject mapObject) {

        String iconImage;
        Float iconSize = 1.0f;


        if (mapObject.getType().equals("MO"))
            iconImage = "monster-image";
        else
            iconImage = "candy-image";


        Symbol symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(mapObject.getLat(), mapObject.getLon()))
                .withIconImage(iconImage)
                .withData(new Gson().toJsonTree(mapObject, MapObject.class))
                .withIconSize(iconSize)
        );

        ModelSingleton.getInstance().getMapSymbols().add(symbol);

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

                if (PermissionsManager.areLocationPermissionsGranted(getApplicationContext())) {

                    if (locationComponent.getLastKnownLocation() != null) {

                        lastKnownLocation.setLatitude(locationComponent.getLastKnownLocation().getLatitude());
                        lastKnownLocation.setLongitude(locationComponent.getLastKnownLocation().getLongitude());
                        canInteract = (ModelSingleton.getInstance().getSignedUser().canInteract(lastKnownLocation, mapObject));
                        bundle.putBoolean("canInteract", canInteract);

                    } else {
                        Toast.makeText(MapActivity.this,
                                R.string.turn_on_location, Toast.LENGTH_SHORT).show();
                    }
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
            Log.d("DBG", "onFightEatButtonClick: cliccato su mostro");
            Intent intent = new Intent(this, FightResultsActivity.class);
            intent.putExtra("mapObjectId", mapObject.getId());
            startActivity(intent);
        } else {

            VolleyCallback volleyCallback = new VolleyCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        Log.d("DBG", "onSuccess: "+ ModelSingleton.getInstance().getSignedUser().toString());
                        ModelSingleton.getInstance().getSignedUser().setLifePoints(response.getInt("lp"));
                        ModelSingleton.getInstance().removeSymbolWithId(mapObject.getId());
                        mapObject.setAlive(false);
                        repository.updateMapObject(mapObject);
                        textViewLifePoints.setText(getString(R.string.life_points_short, ModelSingleton.getInstance().getSignedUser().getLifePoints()));
                        updateSymbols();
                    } catch (JSONException e){
                        Log.d("DBG", "requestFightEatResults: " + e);
                    }
                }
                @Override
                public void onError(VolleyError volleyError) {
                    Log.d("DBG", "onError: " + volleyError);
                }
            };

            repository.requestFightEatResults(volleyCallback, mapObject.getId());
        }

        mapObjectBottomSheet.dismiss();
    }


    public void openWelcomeDialog() {

        WelcomeDialog welcomeDialog = new WelcomeDialog(this);
        welcomeDialog.setCancelable(false);
        welcomeDialog.show(getSupportFragmentManager(), "firstRunDialog");
        ModelSingleton.getInstance().getSignedUser().setFirstRun(false);

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
            Snackbar.make(findViewById(R.id.coordinator_layout), R.string.location_permission_explanation, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
    }

    public void updateSymbols() {
        if (!ModelSingleton.getInstance().getSymbolsToRemove().empty()) {
            Symbol symbol = ModelSingleton.getInstance().getSymbolsToRemove().peek();
            symbolManager.delete(symbol);
            symbolManager.updateSource();
            ModelSingleton.getInstance().getSymbolsToRemove().pop();
        }

        if (ModelSingleton.getInstance().getMonsterSymbolsCount() == 0) {

            repository.requestMap(new VolleyCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    parseRequestMapResponse(response);
                }

                @Override
                public void onError(VolleyError volleyError) {
                    Log.d("DBG", "onError: " + volleyError);
                }

            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        repository.saveUserToSharedPrefs(ModelSingleton.getInstance().getSignedUser());
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        updateSymbols();
        enableLocationComponent(mapStyle);
    }

    @Override
    protected void onResume() {

        if (modelSingleton.getSignedUser().getBase64Image() != null) {
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

