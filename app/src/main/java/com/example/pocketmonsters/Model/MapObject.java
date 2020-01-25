package com.example.pocketmonsters.Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "map_objects_table")
public class MapObject implements Parcelable {
    @Ignore
    private Creator CREATOR;
    @PrimaryKey
    private int id;
    @ColumnInfo
    private double lat;
    @ColumnInfo
    private double lon;
    @ColumnInfo
    private String type;
    @ColumnInfo
    private String size;
    @ColumnInfo
    private String name;
    @ColumnInfo
    private String base64Image;
    @ColumnInfo
    private boolean alive;

    public MapObject(int id, double lat, double lon, String type, String size, String name) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.type = type;
        this.size = size;
        this.name = name;
        this.alive = true;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("id: %s, lat: %s, lon: %s, type: %s, size: %s, name: %s", id, lat, lon, type, size, name);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    @Override
    public int describeContents() {
        return 0;
    }
}
