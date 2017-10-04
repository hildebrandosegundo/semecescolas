package com.semecescolas;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityDetal extends AppCompatActivity {
    JSONObject arr = null;
    JSONArray arrInfo = null;
    HashMap<String,String> item;
    SimpleAdapter adpAlimento;
    ListView listView;
    TextView textView;
    ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detal);
        Intent intent = getIntent();
        Bundle params = intent.getExtras();
        listView = (ListView) findViewById(R.id.listViewDados);
        textView = (TextView) findViewById(R.id.textViewNome);
        try {
            arr =  new JSONObject(params.getCharSequence("dados").toString());
            Log.i("lista arr",""+arr);
            arrInfo = arr.getJSONArray("dados");
            Log.i("lista view",""+arrInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String[] str_serie = { "nome","Bercario","Maternal I","Maternal II","1 Periodo","2 Periodo","1 Ano","2 Ano","3 Ano","4 Ano","5 Ano","6 Ano","7 Ano","8 Ano","9 Ano","1 Serie",
                "2 Serie","4 Serie","5 Serie","6 Serie","7 Serie","8 Serie","Acelera","AEE","Multisseriado","Multisseriado EJA","Pro Jovem","Se Liga"};
        JSONObject esc = null;
        try {
            textView.setText(arrInfo.getJSONObject(0).getString("nome"));
            esc = arrInfo.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i=1; i < str_serie.length; i++) {
            try {
                if (!esc.getString(str_serie[i]).equals("0"))
                try {
                    item = new HashMap<String,String>();
                    item.put("line1", str_serie[i]);
                    item.put("line2", "Qtd de Alunos: "+esc.getString(str_serie[i]));
                    list.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("lista List",""+list);
        adpAlimento = new SimpleAdapter(this, list,
                android.R.layout.two_line_list_item ,
                new String[] { "line1","line2"},
                new int[] {android.R.id.text1, android.R.id.text2});

        listView.setAdapter(adpAlimento);
    }
}
