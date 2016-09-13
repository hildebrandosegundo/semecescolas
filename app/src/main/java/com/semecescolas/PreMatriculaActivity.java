package com.semecescolas;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class PreMatriculaActivity extends AppCompatActivity {
    TextView textcod;
    TextView textnome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_matricula);
        textcod = (TextView) findViewById(R.id.textcod);
        textnome = (TextView) findViewById(R.id.textnome);
        Intent intent = getIntent();
        Bundle dados = new Bundle();
        dados = intent.getExtras();
        textcod.setText(dados.getString("codigo"));
        textnome.setText(dados.getString("nome"));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}
