package com.example.pocketmonsters.Model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ModelSingleton {

    private static ModelSingleton instance = null;

    private List<MapObject> mapObjects;
    private SignedUser signedUser;

    private ModelSingleton() {
        mapObjects = new ArrayList<>();
        signedUser = new SignedUser();
    }

    public static synchronized ModelSingleton getInstance() {
        if (instance == null) {
            instance = new ModelSingleton();
        }
        return instance;
    }

    public void updateSignedUser(SignedUser signedUser) {
        this.signedUser = signedUser;
        Log.d("DBG", "updateSignedUser: " + signedUser.toString());
    }

    public SignedUser getSignedUser() {
        return signedUser;
    }

    public void setMapObjects(List<MapObject> mapObjects) {
        this.mapObjects = mapObjects;
    }

    public List<MapObject> getMapObjects() {
        return mapObjects;
    }

    public MapObject getMapObjectWithId(int id) {

        for (int i = 0; i < mapObjects.size(); i++) {
            if (mapObjects.get(i).getId() == id)
                return  mapObjects.get(i);
        }

        return  null;
    }

    public void removeMapObjectWithId(int id) {
        for (int i = 0; i < mapObjects.size(); i++) {
            if (mapObjects.get(i).getId() == id) {
                mapObjects.remove(mapObjects.get(i));
            }
        }
    }
}
