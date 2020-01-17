package com.example.pocketmonsters.Database;

import com.example.pocketmonsters.Model.MapObject;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface MapObjectDao {
    @Query("SELECT * FROM map_objects_table")
    List<MapObject> getMapObjects();

    @Insert
    void insertAll(List<MapObject> mapObjects);

    @Insert
    void insert(MapObject mapObject);

    @Update
    void update(MapObject mapObject);

    @Delete
    void delete(MapObject mapObject);

    @Delete
    void deleteAll(MapObject... mapObjects);
}
