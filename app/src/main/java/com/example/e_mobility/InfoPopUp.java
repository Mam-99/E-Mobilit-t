package com.example.e_mobility;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e_mobility.Fragment.KundeHistory;

import java.util.ArrayList;
import java.util.List;

public class InfoPopUp extends AppCompatActivity {
    private TextView textView;
    private Button choose;
    private Button favorite;
    private Button defekt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popupwindow);
        setTitle("Infos");

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width * .8), (int)(height * .6));

        setFinishOnTouchOutside(true);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        textView = findViewById(R.id.info);
        textView.setText(info);

        choose = findViewById(R.id.button_choose);
        favorite = findViewById(R.id.button_favorite);
        defekt = findViewById(R.id.button_defekt);

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ladenstation = (String) textView.getText();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("result", ladenstation);

                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ladenstation = (String) textView.getText();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("result", ladenstation);

                setResult(2, resultIntent);
                finish();
            }
        });

        defekt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ladenstation = (String) textView.getText();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("result", ladenstation);

                setResult(3, resultIntent);
                finish();
            }
        });


    }
}
