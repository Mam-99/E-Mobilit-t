package com.example.e_mobility.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_mobility.R;

import java.util.ArrayList;

public class KundeFavorite extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Adapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kunde_favorite);
        setTitle("Kunde Favoritenliste");

        Intent intent = getIntent();
        ArrayList<String> list = (ArrayList<String>) intent.getSerializableExtra("list");
        //Toast.makeText(this, String.valueOf(list.size()), Toast.LENGTH_SHORT).show();

        recyclerView = findViewById(R.id.recyclerView_favorite);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(list);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                list.remove(position);
                adapter.notifyItemRemoved(position);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("resultList", list);
                setResult(6, resultIntent);
            }
        });
    }
}
