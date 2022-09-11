package com.example.e_mobility;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrefConfig {
    private static final String PREFERENCE_NAME = "myPreference";
    public static final String FAVORITE_LIST = "favoriteList";
    public static final String HISTORY_LIST = "historyList";
    public static final String DEFEKT_LIST = "defektList";
    public static final String SERVICE_LIST = "serviceList";

    public static void saveStringSet(Context context,
                                     Set<String> historyList,
                                     Set<String> favoriteList,
                                     Set<String> defektList,
                                     Set<String> serviceList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putStringSet(FAVORITE_LIST, favoriteList);
        editor.putStringSet(HISTORY_LIST, historyList);
        editor.putStringSet(DEFEKT_LIST, defektList);
        editor.putStringSet(SERVICE_LIST, serviceList);

        editor.apply();
    }

    public static Set<String> loadServiceList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet(SERVICE_LIST, new HashSet<>());
        return set;
    }

    public static Set<String> loadHistoryList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet(HISTORY_LIST, new HashSet<>());
        return set;
    }

    public static Set<String> loadFavoriteList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet(FAVORITE_LIST, new HashSet<>());
        return set;
    }

    public static Set<String> loadDefektList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet(DEFEKT_LIST, new HashSet<>());
        return set;
    }
}
