package com.semecescolas;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hildebrandosegundo on 17/02/16.
 */
public class MapsActivity extends BaseActivity implements LocationListener {
    private GoogleMap mMap;
    LocationManager locationManager;
    public LatLng locationsource;
    private int tipo_provider;
    private Polyline polyline;
    private List<LatLng> list;
    private long distance;
    private String tempo;
    String tipo_r = "";
    Spinner modalidade;
    Spinner serie;
    String str_codigo;
    String str_serie;
    ArrayAdapter adapterSerie;
    CheckBox checkBoxTrafego;
    JSONArray arrEsc = null;
    JSONArray arrInfo = null;
    Button botaoPe;
    Button botaoBike;
    Button botaoCar;
    Button botaoLog;
    Button botaoEsc;
    Button botaoInfo;
    Marker escola;
    Boolean pesqEscola = false;
    private EditText pesquisa;
    //private final Context mContext;
    // flag for GPS status
    public boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    public boolean canGetLocation = false;
    Location location; // location
    public HashMap<Marker, MyMarker> mMarkersHashMap;
    public ArrayList<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();

    protected int getLayoutId() {
        return R.layout.kml_map;
    }

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5; // 10 seconds
    private ProgressDialog progressDialog;
    public void alertaConnection(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MapsActivity.this);
        builder1.setMessage("Não foi possivel conectar com o servidor!");
        builder1.setTitle("Atenção!");
        builder1.setCancelable(true);
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    public void setMarkerEscolas(){
        JSONObject escolas = null;
        JsonClass json = new JsonClass();
        escolas = json.getJSONEscolas("https://raw.githubusercontent.com/hildebrandosegundo/semecescolas/master/escolas.json");
        //escolas = json.getJSONEscolas("http://10.10.135.178/webservice_semec/webservicesemec_teste/escolas");
        if(escolas==null){
            progressDialog.dismiss();
            alertaConnection();
            return;
        }

        try {
            arrEsc = escolas.getJSONArray("dados");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i=0; i < arrEsc.length(); i++) {
            JSONObject esc = null;
            try {
                esc = arrEsc.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                mMyMarkersArray.add(new MyMarker("icon1",esc.getString("nome"),esc.getString("CodSemec"),esc.getString("Ideb"),esc.getString("Telefones"),esc.getString("aluno"),esc.getString("turmas"),esc.getString("salas"), esc.getDouble("latitude"), esc.getDouble("longitude")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mMarkersHashMap = new HashMap<Marker, MyMarker>();

        plotMarkers(mMyMarkersArray);
        progressDialog.hide();
    }

    private void plotMarkers(final ArrayList<MyMarker> markers)
    {
        if(markers.size() > 0)
        {
            for (final MyMarker myMarker : markers)
            {

                // Create user marker with custom icon and other options
                MarkerOptions markerOption = new MarkerOptions().position(new LatLng(myMarker.getmLatitude(), myMarker.getmLongitude()));
                markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.currentlocation_icon));
                markerOption.title(myMarker.getmLabel());

                markerOption.snippet(
                                "Telefone: "+myMarker.getTelefones()+"\n"+
                                "Codigo: "+myMarker.getCodSemec()+" - "+
                                "Alunos: "+myMarker.getAluno()+"\n"+
                                "Salas: "+myMarker.getSalas()+" - "+
                                "Turmas: "+myMarker.getTurmas()+"\n"+
                                "IDEB: "+myMarker.getIdeb());
                Marker currentMarker = mMap.addMarker(markerOption);
                mMarkersHashMap.put(currentMarker, myMarker);
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        if(marker.getSnippet()!=null) {
                            botaoInfo.setVisibility(View.VISIBLE);
                            str_codigo = marker.getSnippet().substring(8, 13);
                        }
                        return null;}

                    @Override
                    public View getInfoContents(Marker marker) {
                        LinearLayout info = new LinearLayout(getApplicationContext());
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(getApplicationContext());
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());

                        TextView rotahere = new TextView(getApplicationContext());
                        rotahere.setTextColor(Color.argb(255,91, 195, 233));
                        rotahere.setGravity(Gravity.CENTER);
                        rotahere.setTypeface(null, Typeface.BOLD);
                        rotahere.setText("ROTA ATÉ AQUI");

                        TextView snippet = new TextView(getApplicationContext());
                        snippet.setTextColor(Color.argb(255,131, 131, 131));
                        snippet.setGravity(Gravity.CENTER);
                        snippet.setText(marker.getSnippet());

                        info.addView(title);
                        info.addView(snippet);
                        info.addView(rotahere);
                        return info;
                    }
                });
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                    @Override
                    public void onInfoWindowClick(final Marker marker) {
                        progressDialog = new ProgressDialog(MapsActivity.this);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("Aguarde a rota...");
                        progressDialog.show();
                        getLocationEscola(marker.getPosition().latitude,marker.getPosition().longitude);
                        getRoute(locationsource,marker.getPosition());

                    }
                });
            }
        }
    }
    /*public void atualizar(){
        Thread downloadThread = new Thread() {
            public void run() {
                try {
                    Document doc = Jsoup.connect("https://play.google.com/store/apps/details?id=com.semecescolas").get();
                    Elements link = doc.getElementsByAttributeValueContaining("itemprop", "softwareVersion");
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    String version = pInfo.versionName;
                    if (!version.equals(link.text())) {
                        Snackbar snackbar = Snackbar
                                .make(findViewById(android.R.id.content), "Possuímos uma nova versão!", Snackbar.LENGTH_INDEFINITE).setActionTextColor(Color.MAGENTA)
                                .setAction("ATUALIZAR", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.semecescolas");
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(intent);
                                    }
                                });
                        snackbar.show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            };
            downloadThread.start();
    }*/
    public void startMap() {
            //atualizar();
        progressDialog = new ProgressDialog(MapsActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Aguarde...");
        progressDialog.show();
        botaoPe = (Button) findViewById(R.id.tipo_rota_pe);
        botaoBike = (Button) findViewById(R.id.tipo_rota_bike);
        botaoCar = (Button) findViewById(R.id.tipo_rota_car);
        botaoEsc = (Button) findViewById(R.id.pesqescola);
        botaoLog = (Button) findViewById(R.id.pesqlog);
        botaoInfo = (Button) findViewById(R.id.info_escola);
        botaoInfo.setVisibility(View.INVISIBLE);
        checkBoxTrafego = (CheckBox) findViewById(R.id.checkBox);
        modalidade = (Spinner) findViewById(R.id.modalidade);
        serie = (Spinner) findViewById(R.id.serie);
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(this, R.array.tipo_mapa, android.R.layout.simple_spinner_dropdown_item);
        adapterSerie = ArrayAdapter.createFromResource(this, R.array.Series, android.R.layout.simple_spinner_dropdown_item);
        modalidade.setAdapter(adapter1);
        serie.setAdapter(adapterSerie);
        modalidade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                //parent.getItemAtPosition(pos);
                //Toast.makeText(MapsActivity.this, "Entrou no 0 :"+pos, Toast.LENGTH_LONG).show();
                if (pos == 1) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else if (pos == 2) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        serie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                str_serie = serie.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        pesquisa = (EditText) findViewById(R.id.pesquisa);
        try {
            mMap = getMap();
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(

            ) {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    locationsource = latLng;
                    locationSource.setLocation(latLng);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    mMap.setLocationSource(locationSource);
                    Toast.makeText(MapsActivity.this, "Localização definida pelo usuário", Toast.LENGTH_LONG).show();
                }
            });
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(

            ) {
                @Override
                public void onMapClick(LatLng latLng) {
                    botaoInfo.setVisibility(View.INVISIBLE);
                }
            });
            escola = mMap.addMarker(new MarkerOptions().position(new LatLng(-5.154925, -42.767201)).visible(false).icon(BitmapDescriptorFactory.fromResource(R.drawable.dot)));

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            setMarkerEscolas();
                            progressDialog.dismiss();
                        }
                    }, 3000);

            //retrieveFileFromResource();
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Log.v("isGPSEnabled", "=" + isGPSEnabled);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // Log.v("isNetworkEnabled", "=" + isNetworkEnabled);

            if (isGPSEnabled == false && isNetworkEnabled == false) {
                // no network provider is enabled
                showSettingsAlert();
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    tipo_provider = 1;
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            locationSource.setLocation(location);
                            locationsource = new LatLng(location.getLatitude(),location.getLongitude());
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    tipo_provider = 2;
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                locationSource.setLocation(location);
                                locationsource = new LatLng(location.getLatitude(),location.getLongitude());
                            }
                        }
                    }
                }
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationsource, 15));
            mMap.setLocationSource(this.locationSource);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    protected void onPause() {
        super.onPause();
        // Desliga o GPS
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
    }
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Configuração do GPS");

        // Setting Dialog Message
        alertDialog
                .setMessage("GPS não está ativado. Você deseja ativa-lo?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Configurar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }
    @Override
    public void onLocationChanged(Location location) {
        // Centraliza o mapa nesta coordenada
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        locationsource = latLng;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        mMap.setLocationSource(this.locationSource);
        // Atualiza a bolinha azul para a nova coordenada
        this.locationSource.setLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        switch (tipo_provider) {
            case 1:
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                break;
            case 2:
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                break;
            default:
        }
    }


    @Override
    public void onProviderDisabled(String provider) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        showSettingsAlert();
    }
    /* ***************************************** ROTA ***************************************** */


    public void getRouteByGMAV2(View view) throws UnsupportedEncodingException, JSONException {
        progressDialog = new ProgressDialog(MapsActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Aguarde a rota...");
        progressDialog.show();
        botaoInfo.setVisibility(View.INVISIBLE);
        JSONObject escolas = null;
        JsonClass json = new JsonClass();
        if (str_serie.equals("Serie"))
            str_serie = "Todos";
        escolas = json.getJSONFromUrl("http://200.151.25.77/webservice_semec/webservicesemec_teste/escolasprox", locationsource.latitude, locationsource.longitude, str_serie);
        if(escolas==null){
            alertaConnection();
            return;
        }
        getRoute1(locationsource, escolas);

    }
    public void tipoPe(View view) {
        botaoPe.setBackgroundResource(R.drawable.ic_action_man_walking_1);
        botaoBike.setBackgroundResource(R.drawable.ic_action_bike);
        botaoCar.setBackgroundResource(R.drawable.ic_action_car);
        tipo_r = "&mode=walking";
    }
    public void tipoBike(View view) {
        botaoBike.setBackgroundResource(R.drawable.ic_action_bike_1);
        botaoPe.setBackgroundResource(R.drawable.ic_action_man_walking);
        botaoCar.setBackgroundResource(R.drawable.ic_action_car);
        tipo_r = "&mode=bicycling";
    }
    public void tipoCar(View view) {
        botaoCar.setBackgroundResource(R.drawable.ic_action_car_1);
        botaoBike.setBackgroundResource(R.drawable.ic_action_bike);
        botaoPe.setBackgroundResource(R.drawable.ic_action_man_walking);
        tipo_r = "&mode=driving";
    }
    public  void pesqLog(View view){
        botaoEsc.setBackgroundResource(R.drawable.ic_action_escola);
        botaoLog.setBackgroundResource(R.drawable.ic_action_mapa_1);
        pesqEscola = false;
        pesquisa.setHint("Ex.: Rua, numero, cidade");
    }
    public  void pesqEscola(View view){
        botaoEsc.setBackgroundResource(R.drawable.ic_action_escola_1);
        botaoLog.setBackgroundResource(R.drawable.ic_action_mapa);
        pesqEscola = true;
        pesquisa.setHint("Nome da escola");
    }
    public void trafegoVia(View view){
        if(checkBoxTrafego.isChecked()){
            mMap.setTrafficEnabled(true);
        }else{
            mMap.setTrafficEnabled(false);
        }
    }
    public void infoEscola(View view) throws UnsupportedEncodingException, JSONException{
        progressDialog = new ProgressDialog(MapsActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Aguarde...");
        progressDialog.show();
        JSONObject escolas = null;
        JsonClass json = new JsonClass();
        if (str_serie.equals("Serie"))
            str_serie = "Todos";
        escolas = json.getJSONFromCod("http://200.151.25.77/webservice_semec/webservicesemec_teste/getinfoescolas", str_codigo);
        if(escolas==null){
            alertaConnection();
            return;
        }
        try {
            arrInfo = escolas.getJSONArray("dados");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Bundle params = new Bundle();
        params.putCharSequence("dados", escolas.toString());

        Intent intent = new Intent(MapsActivity.this, ActivityDetal.class);

        intent.putExtras(params);

        startActivity(intent);

        progressDialog.dismiss();
    }
    // WEB CONNECTION
    public void getRoute1(final LatLng origin, final JSONObject destination) throws JSONException {
        new Thread() {
            public void run() {

                String url = null;

                try {
                    url = "http://maps.googleapis.com/maps/api/directions/json?origin="
                            + origin.latitude + "," + origin.longitude + "&destination="
                            + destination.getString("latitude") + "," + destination.getString("longitude") + tipo_r;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.i("url", url);
                HttpResponse response;
                HttpGet request;
                AndroidHttpClient client = AndroidHttpClient.newInstance("route");

                request = new HttpGet(url);
                try {
                    response = client.execute(request);
                    final String answer = EntityUtils.toString(response.getEntity());

                    runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                //Log.i("Script", answer);
                                list = buildJSONRoute(answer);
                                drawRoute();
                                getLocationEscola(destination.getDouble("latitude"),destination.getDouble("longitude"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        if(arrEsc.length()<0) {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            setMarkerEscolas();
                        }
                    }, 3000);
        }
    }
    public void getRoute(final LatLng origin, final LatLng destino) {
        new Thread() {
            public void run() {
                        /*String url= "http://maps.googleapis.com/maps/api/directions/json?origin="
                                + origin+"&destination="
								+ destination+"&sensor=false";*/
                String url = null;
                    url = "http://maps.googleapis.com/maps/api/directions/json?origin="
                            + origin.latitude + "," + origin.longitude + "&destination="
                            + destino.latitude + "," + destino.longitude + tipo_r;


                Log.i("url", url);
                HttpResponse response;
                HttpGet request;
                AndroidHttpClient client = AndroidHttpClient.newInstance("route");

                request = new HttpGet(url);
                try {
                    response = client.execute(request);
                    final String answer = EntityUtils.toString(response.getEntity());

                    runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                //Log.i("Script", answer);
                                list = buildJSONRoute(answer);
                                drawRoute();
                                //getLocationEscola(destino.latitude,destino.longitude);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    alertaConnection();
                }
            }
        }.start();

    }
    public void drawRoute() {
        PolylineOptions po;

        if (polyline == null) {
            po = new PolylineOptions();

            for (int i = 0, tam = list.size(); i < tam; i++) {
                po.add(list.get(i));
            }

            po.color(Color.BLUE).width(8);
            polyline = mMap.addPolyline(po);
        } else {
            polyline.setPoints(list);
        }
    }

    // PARSER JSON
    public List<LatLng> buildJSONRoute(String json) throws JSONException {
        JSONObject result = new JSONObject(json);
        JSONArray routes = result.getJSONArray("routes");

        distance = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getInt("value");
        tempo = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text");

        JSONArray steps = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
        List<LatLng> lines = new ArrayList<LatLng>();

        for (int i = 0; i < steps.length(); i++) {
            Log.i("Script", "STEP: LAT: " + steps.getJSONObject(i).getJSONObject("start_location").getDouble("lat") + " | LNG: " + steps.getJSONObject(i).getJSONObject("start_location").getDouble("lng"));


            String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");

            for (LatLng p : decodePolyline(polyline)) {
                lines.add(p);
            }

            Log.i("Script", "STEP: LAT: " + steps.getJSONObject(i).getJSONObject("end_location").getDouble("lat") + " | LNG: " + steps.getJSONObject(i).getJSONObject("end_location").getDouble("lng"));
        }
        progressDialog.dismiss();
        Toast.makeText(MapsActivity.this, "Distancia: " + distance + " metros \n Duração: " + tempo, Toast.LENGTH_LONG).show();
        return (lines);
    }

    // DECODE POLYLINE
    private List<LatLng> decodePolyline(String encoded) {

        List<LatLng> listPoints = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            Log.i("Script", "POL: LAT: " + p.latitude + " | LNG: " + p.longitude);
            listPoints.add(p);
        }
        return listPoints;
    }

    public void getLocation(View view) {
        if(pesqEscola){
            getPesquisaEscola();
        }else {

            Geocoder gc = new Geocoder(MapsActivity.this);

            List<Address> addressList;
            try {
                if (pesquisa.getText().length() > 0) {
                    addressList = gc.getFromLocationName(pesquisa.getText().toString(), 1);
                    if (addressList.toString().equals("[]")) {
                        Toast.makeText(MapsActivity.this, "Endereço não encontrado!", Toast.LENGTH_LONG).show();
                    } else {
                        String address = "Rua: " + addressList.get(0).getThoroughfare() + "\n";
                        address += "Cidade: " + addressList.get(0).getLocality() + "\n";
                        address += "CEP: " + addressList.get(0).getPostalCode() + "\n";
                        address += "Estado: " + addressList.get(0).getAdminArea() + "\n";
                        address += "País: " + addressList.get(0).getCountryName();

                        LatLng ll = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());
                        locationsource = ll;
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 15));
                        mMap.setLocationSource(this.locationSource);
                        // Atualiza a bolinha azul para a nova coordenada
                        this.locationSource.setLocation(locationsource, tipo_provider);
                        //Toast.makeText(MainActivity.this, "Local: "+address, Toast.LENGTH_LONG).show();
                        Toast.makeText(MapsActivity.this, "Endereço: " + address, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    public void getLocationEscola(Double lat,Double lng) {
        Geocoder gc = new Geocoder(MapsActivity.this);
        Log.i("endereco","lat: "+lat+" lng:"+lng);
        List<Address> addressList;
        try {
            if (lat!=null && lng!=null) {
                //addressList = gc.getFromLocation(list.get(list.size() - 1).latitude, list.get(list.size() - 1).longitude, 1);
                addressList = gc.getFromLocation(lat,lng, 1);
                if(addressList.toString().equals("[]")){
                    Toast.makeText(MapsActivity.this, "Endereço não encontrado!", Toast.LENGTH_LONG).show();
                }else {
                    String address = "Endereço da escola \nRua: " + addressList.get(0).getThoroughfare() + "\n";
                    address += "Cidade: " + addressList.get(0).getLocality() + "\n";
                    address += "CEP: " + addressList.get(0).getPostalCode() + "\n";
                    address += "Estado: " + addressList.get(0).getAdminArea() + "\n";
                    address += "País: " + addressList.get(0).getCountryName();

                    Toast.makeText(MapsActivity.this, address, Toast.LENGTH_LONG).show();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void getPesquisaEscola() {
        if (!pesquisa.getText().toString().equals("")) {
            if (arrEsc.length() > 0) {
                for (int i = 0; i < arrEsc.length(); i++) {
                    JSONObject esc = null;
                    try {
                        esc = arrEsc.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (esc.getString("nome").contains(pesquisa.getText().toString().toUpperCase().trim())) {
                            LatLng ll = new LatLng(esc.getDouble("latitude"), esc.getDouble("longitude"));
                            //locationsource = ll;
                            escola.setVisible(true);
                            escola.setPosition(ll);
                            escola.showInfoWindow();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 15));

                            break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                setMarkerEscolas();
                            }
                        }, 3000);
            }
        }
    }
}
