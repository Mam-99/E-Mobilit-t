package com.example.e_mobility.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_mobility.R;

import java.util.ArrayList;

public class ServiceDefekt extends AppCompatActivity {
    private RecyclerView recyclerView;
    private  AdapterService adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_defekt);
        setTitle("Service Defekteliste");

        Intent intent = getIntent();
        ArrayList<String> list = (ArrayList<String>) intent.getSerializableExtra("list");
        //Toast.makeText(this, String.valueOf(list.size()), Toast.LENGTH_SHORT).show();

        recyclerView = findViewById(R.id.recyclerView_defekt);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdapterService(list);
        recyclerView.setAdapter(adapter);

        ArrayList<String> doneList = new ArrayList<>();

        adapter.setOnItemClickListener(new AdapterService.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                doneList.add(list.get(position));
                list.remove(position);
                adapter.notifyItemRemoved(position);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("resultList", list);
                resultIntent.putExtra("done", doneList);
                setResult(7, resultIntent);
            }
        });
    }
}
