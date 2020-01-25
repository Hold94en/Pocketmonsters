package com.example.pocketmonsters.Utilis;

import com.example.pocketmonsters.Model.MapObject;

import java.util.List;

public interface AsyncTaskCallback {

    void onPostExecution(MapObject mapObject);
    void onPostExecution(List<MapObject> mapObjects);
    void onPostExecution(Integer integer);

}
