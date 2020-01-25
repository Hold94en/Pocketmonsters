package com.example.pocketmonsters.Database;

import com.example.pocketmonsters.Model.MapObject;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface MapObjectDao {
    @Query("SELECT * FROM map_objects_table")
    List<MapObject> getMapObjects();

    @Query("SELECT * FROM map_objects_table")
    LiveData<List<MapObject>> getAllMapObjecs();

    @Query("UPDATE map_objects_table SET alive = :alive")
    void setAllMapObjectsLifeTo(boolean alive);

    @Query("SELECT * FROM map_objects_table WHERE id = :objectId")
    MapObject getMapObject(int objectId);

    @Query("SELECT COUNT(id) FROM map_objects_table")
    Integer getRowCount();

    @Query("SELECT COUNT(id) FROM map_objects_table WHERE type = 'MO'")
    Integer getMonstersCount();

    @Query("DELETE FROM map_objects_table WHERE type = 'MO'")
    void deleteAllMonsters();

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
