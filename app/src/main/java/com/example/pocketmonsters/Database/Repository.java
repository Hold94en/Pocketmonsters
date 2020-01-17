package com.example.pocketmonsters.Database;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.example.pocketmonsters.Model.MapObject;
import com.example.pocketmonsters.Model.ModelSingleton;
import com.example.pocketmonsters.Model.SignedUser;
import com.example.pocketmonsters.Utilis.AsyncTaskCallback;
import com.example.pocketmonsters.Utilis.VolleyCallback;
import com.example.pocketmonsters.Utilis.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Repository {
    private final static String SHARED_PREFS = "sharedPrefs";
    private static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    private static final String REGISTER = "register.php";
    private static final String GET_MAP = "getmap.php";
    private static final String GET_IMAGE = "getimage.php";
    private static final String FIGHT_EAT = "fighteat.php";
    private static final String SET_PROFILE = "setprofile.php";
    private static final String GET_RANKING = "ranking.php";

    private SharedPreferences sharedPreferences;
    private VolleySingleton volleySingleton;
    private MapObjectDao mapObjectDao;

    private static Repository instance;

    private Repository(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context.getApplicationContext());
        this.volleySingleton = VolleySingleton.getInstance(context.getApplicationContext());
        this.sharedPreferences = context.getApplicationContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        this.mapObjectDao = appDatabase.mapObjectDao();
    }

    public static synchronized Repository getInstance(Context context) {
        if (instance == null) {
            instance = new Repository(context);
        }
        return instance;
    }

    // LOCAL

    public boolean isUserInSharedPrefs() {
        return sharedPreferences.contains("session_id");
    }

    public SignedUser getUserFromSharedPrefs() {
        SignedUser usr = new SignedUser(sharedPreferences.getString("session_id", ""));
        usr.setLifePoints(sharedPreferences.getInt("life_points", 100));
        usr.setExpPoints(sharedPreferences.getInt("exp_points", 100));

        if (sharedPreferences.contains("user_name")) {
            usr.setUsername(
                sharedPreferences.getString("user_name", "")
            );
        }

        if (sharedPreferences.contains("user_image")) {
            usr.setBase64Image(sharedPreferences.getString("user_image", ""));
        }

        return usr;
    }

    public void saveUserToSharedPrefs(SignedUser user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("session_id", user.getSessionId());
        editor.putInt("life_points", user.getLifePoints());
        editor.putInt("exp_points", user.getExpPoints());

        if (user.getUsername() != null) {
            editor.putString("user_name", user.getUsername());
        }

        if (user.getBase64Image() != null) {
            editor.putString("user_image", user.getBase64Image());
        }

        editor.apply();
    }

    public void getMapObjectsFromDb(AsyncTaskCallback asyncTaskCallback) {
        new GetAllObjectsAsyncTask(mapObjectDao, asyncTaskCallback).execute();
    }

    public void insertMapObject(MapObject mapObject) {
        new InsertMapObjectAsyncTask(mapObjectDao).execute(mapObject);
    }

    public void insertMapObjectsToDb(List<MapObject> mapObjects) {
        new InsertMapObjectsAsyncTask(mapObjectDao).execute(mapObjects);
    }

    public void deleteMapObject(MapObject mapObject) {
        new DeleteMapObjectAsyncTask(mapObjectDao).execute(mapObject);
    }

    private static class GetAllObjectsAsyncTask extends AsyncTask<Void, Void, List<MapObject>> {
        private MapObjectDao mapObjectDao;
        private AsyncTaskCallback asyncTaskCallback;

        private GetAllObjectsAsyncTask(MapObjectDao mapObjectDao, AsyncTaskCallback asyncTaskCallback) {
            this.mapObjectDao = mapObjectDao;
            this.asyncTaskCallback = asyncTaskCallback;
        }

        @Override
        protected List<MapObject> doInBackground(Void... voids) {
            return mapObjectDao.getMapObjects();
        }

        @Override
        protected void onPostExecute(List<MapObject> mapObjects) {
            asyncTaskCallback.onPostExecution(mapObjects);
        }
    }

    public static class InsertMapObjectsAsyncTask extends AsyncTask<List<MapObject>, Void, Void> {
        private MapObjectDao mapObjectDao;

        private InsertMapObjectsAsyncTask(MapObjectDao mapObjectDao) {
            this.mapObjectDao = mapObjectDao;
        }

        @Override
        protected Void doInBackground(List<MapObject>... lists) {
            Log.d("DBG", "doInBackground: inserting a list of " + lists[0].size() + "elements");
            mapObjectDao.insertAll(lists[0]);
            return null;
        }
    }

    public static class InsertMapObjectAsyncTask extends AsyncTask<MapObject, Void, Void> {
        private MapObjectDao mapObjectDao;

        private InsertMapObjectAsyncTask(MapObjectDao mapObjectDao) {
            this.mapObjectDao = mapObjectDao;
        }

        @Override
        protected Void doInBackground(MapObject... mapObjects) {
            Log.d("DBG", "doInBackground: inserting " + mapObjects[0].toString());
            mapObjectDao.insert(mapObjects[0]);
            return null;
        }
    }

    public static class DeleteMapObjectAsyncTask extends AsyncTask<MapObject, Void, Void> {
        private MapObjectDao mapObjectDao;

        private DeleteMapObjectAsyncTask(MapObjectDao mapObjectDao) {
            this.mapObjectDao = mapObjectDao;
        }

        @Override
        protected Void doInBackground(MapObject... mapObjects) {
            Log.d("DBG", "doInBackground: deleting " + mapObjects[0].toString());
            mapObjectDao.delete(mapObjects[0]);
            return null;
        }
    }













    // REMOTE

    public void requestSessionId(VolleyCallback volleyCallback) {
        JsonObjectRequest jsonObjectRequest = volleySingleton
                .createRequest(BASE_URL + REGISTER, null, volleyCallback);
        volleySingleton.addToRequestQueue(jsonObjectRequest);
    }

    public void requestMap(VolleyCallback volleyCallback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("session_id", ModelSingleton.getInstance().getSignedUser().getSessionId());
        } catch (JSONException e) {
            Log.d("DBG", "requestMap: " + jsonObject);
        }

        JsonObjectRequest jsonObjectRequest = volleySingleton.createRequest
                (BASE_URL + GET_MAP, jsonObject, volleyCallback);
        volleySingleton.addToRequestQueue(jsonObjectRequest);
    }


    public void requestImage(VolleyCallback volleyCallback, final MapObject mapObject) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("session_id", ModelSingleton.getInstance().getSignedUser().getSessionId());
            jsonObject.put("target_id", mapObject.getId());
        } catch (JSONException e) {
            Log.d("DBG", "requestImage: " + jsonObject);
        }

        JsonObjectRequest jsonObjectRequest = volleySingleton.createRequest(
                BASE_URL + GET_IMAGE, jsonObject, volleyCallback);
        volleySingleton.addToRequestQueue(jsonObjectRequest);
    }

    public void requestFightEatResults(VolleyCallback volleyCallback, int mapObjectId) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("session_id", ModelSingleton.getInstance().getSignedUser().getSessionId());
            jsonObject.put("target_id", mapObjectId);
        } catch (JSONException e) {
            Log.d("DBG", "requestFightEatResults: " + jsonObject);
        }


        JsonObjectRequest jsonObjectRequest = volleySingleton.createRequest(
                BASE_URL + FIGHT_EAT, jsonObject, volleyCallback);
        volleySingleton.addToRequestQueue(jsonObjectRequest);
    }

    public void sendUserData(VolleyCallback volleyCallback, String data[]) {

        List<String> values = new ArrayList<>();
        values.add("username");
        values.add("img");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("session_id", ModelSingleton.getInstance().getSignedUser().getSessionId());
            for (int i = 0; i < data.length; i++) {
                jsonObject.put(values.get(i), data[i]);
            }
        } catch (JSONException e) {
            Log.d("DBG", "sendUserName: " + jsonObject);
        }

        JsonObjectRequest jsonObjectRequest = volleySingleton.createRequest(
                BASE_URL + SET_PROFILE, jsonObject, volleyCallback);
        volleySingleton.addToRequestQueue(jsonObjectRequest);
    }

    public void sendUserImage(VolleyCallback volleyCallback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("session_id", ModelSingleton.getInstance().getSignedUser().getSessionId());
            jsonObject.put("username", "");
            jsonObject.put("img", ModelSingleton.getInstance().getSignedUser().getBase64Image());
        } catch (JSONException e) {
            Log.d("DBG", "sendUserImage: " + jsonObject);
        }

        JsonObjectRequest jsonObjectRequest = volleySingleton.createRequest(
                BASE_URL + SET_PROFILE, jsonObject, volleyCallback);
        volleySingleton.addToRequestQueue(jsonObjectRequest);
    }

    public void requestRanking(VolleyCallback volleyCallback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("session_id", ModelSingleton.getInstance().getSignedUser().getSessionId());
        } catch (JSONException e) {
            Log.d("DBG", "requestRanking: " + jsonObject);
        }

        JsonObjectRequest jsonObjectRequest = volleySingleton.createRequest(
                BASE_URL + GET_RANKING, jsonObject, volleyCallback);
        volleySingleton.addToRequestQueue(jsonObjectRequest);
    }
}
