package com.example.recetario;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.window.SplashScreen;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class splashScreem extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screem);
        //mostramos la pantalla de carga durante 3 seg
        new Handler().postDelayed(() ->{
          Intent intent=new Intent(splashScreem.this, MainActivity.class);
            startActivity(intent);
            finish();
        },3000);
    }
}