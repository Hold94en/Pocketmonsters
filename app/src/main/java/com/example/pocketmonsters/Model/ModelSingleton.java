package com.example.pocketmonsters.Model;

import android.util.Log;

import com.mapbox.mapboxsdk.plugins.annotation.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class ModelSingleton {

    private static ModelSingleton instance = null;

    private List<Symbol> mapSymbols;
    private SignedUser signedUser;
    private Stack<Symbol> symbolsToRemove;

    private ModelSingleton() {
        mapSymbols = new ArrayList<>();
        signedUser = new SignedUser();
        symbolsToRemove = new Stack<>();
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

    public List<Symbol> getMapSymbols() {
        return mapSymbols;
    }

    public Symbol getMapSymbolWithId(int id) {

        for (int i = 0; i < mapSymbols.size(); i++) {
            if (mapSymbols.get(i).getId() == id)
                return mapSymbols.get(i);
        }

        return  null;
    }

    public int getMonsterSymbolsCount() {

        int count = 0;

        for (int i = 0; i < mapSymbols.size(); i++) {
            if (Objects.requireNonNull(mapSymbols.get(i).getData()).getAsJsonObject().get("type").getAsString().equals("MO")) {
                count++;
            }
        }

        return count;
    }

    public void removeSymbolWithId(int id) {
        for (int i = 0; i < mapSymbols.size(); i++) {
            if (Objects.requireNonNull(mapSymbols.get(i).getData()).getAsJsonObject().get("id").getAsInt() == id) {
                symbolsToRemove.push(mapSymbols.get(i));
                mapSymbols.remove(mapSymbols.get(i));
            }
        }
    }

    public Stack<Symbol> getSymbolsToRemove() {
        return symbolsToRemove;
    }
}
