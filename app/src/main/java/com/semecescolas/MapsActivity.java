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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.Task;
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
    LatLng locationsource;
    private int tipo_provider;
    private Polyline polyline;
    private List<LatLng> list;
    private long distance;
    private String tempo;
    Spinner tipo_rota;
    Spinner modalidade;
    Spinner serie;
    String str_serie;
    ArrayAdapter adapterSerie;
    private EditText pesquisa;
    //private final Context mContext;
    // flag for GPS status
    public boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    public boolean canGetLocation = false;
    Location location; // location
    //double latitude; // latitude
    //double longitude; // longitude
    public HashMap<Marker, MyMarker> mMarkersHashMap;
    public ArrayList<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();

    protected int getLayoutId() {
        return R.layout.kml_map;
    }

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private ProgressDialog dialog;

    public void setMarkerEscolas(){
        new Task(this).execute();
        JSONObject escolas = null;
        JsonClass json = new JsonClass();
        escolas = json.getJSONEscolas("http://200.151.25.77/webservice_semec/webservicesemec_teste/escolas");
        if(escolas==null){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MapsActivity.this);
            builder1.setMessage("Não foi possivel conectar o servidor!");
            builder1.setTitle("Atenção!");
            builder1.setCancelable(true);
            AlertDialog alert11 = builder1.create();
            alert11.show();
            return;
        }
        JSONArray arrEsc = null;
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
                mMyMarkersArray.add(new MyMarker("icon1",esc.getString("nome"),esc.getString("CodSemec"),esc.getString("Atendimento"),esc.getString("Telefones"),esc.getString("aluno"),esc.getString("turmas"),esc.getString("salas"), esc.getDouble("latitude"), esc.getDouble("longitude")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mMarkersHashMap = new HashMap<Marker, MyMarker>();

        plotMarkers(mMyMarkersArray);

    }
    class Task extends AsyncTask<Void, Void, String> {

        private Context context;

        public Task(Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setTitle("Realizando o carregamento dos dados");
            dialog.setMessage("Aguarde...");
            dialog.show();
        }
        @Override
        protected String doInBackground(Void... params) {
            return null;
        }
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
                                "Codigo: "+myMarker.getCodSemec()+" - "+
                                "QTD Alunos: "+myMarker.getAluno()+"\n"+
                                "QTD Salas: "+myMarker.getSalas()+" - "+
                                "QTD Turmas: "+myMarker.getTurmas()+"\n "+
                                "Telefone: "+myMarker.getTelefones());
                Marker currentMarker = mMap.addMarker(markerOption);
                mMarkersHashMap.put(currentMarker, myMarker);
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        LinearLayout info = new LinearLayout(getApplicationContext());
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(getApplicationContext());
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());

                        TextView snippet = new TextView(getApplicationContext());
                        snippet.setTextColor(Color.BLUE);
                        snippet.setGravity(Gravity.CENTER);
                        snippet.setText(marker.getSnippet());

                        info.addView(title);
                        info.addView(snippet);

                        return info;
                    }
                });
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        /*Bundle bundle = new Bundle();
                        bundle.putString("codigo", marker.getSnippet().substring(8,13));
                        bundle.putString("nome", marker.getTitle());
                        Intent preMatriculaActivity = new Intent(MapsActivity.this, PreMatriculaActivity.class);
                        preMatriculaActivity.putExtras(bundle);
                        startActivity(preMatriculaActivity);*/
                    }
                });
            }
        }
    }

    public void startMap() {
        tipo_rota = (Spinner) findViewById(R.id.tipo_rota);
        modalidade = (Spinner) findViewById(R.id.modalidade);
        serie = (Spinner) findViewById(R.id.serie);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.tipos_rotas, android.R.layout.simple_spinner_item);
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(this, R.array.modalidade, android.R.layout.simple_spinner_item);
        adapterSerie = ArrayAdapter.createFromResource(this, R.array.Series, android.R.layout.simple_spinner_item);
        tipo_rota.setAdapter(adapter);
        modalidade.setAdapter(adapter1);
        serie.setAdapter(adapterSerie);
        modalidade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                //parent.getItemAtPosition(pos);
                //Toast.makeText(MapsActivity.this, "Entrou no 0 :"+pos, Toast.LENGTH_LONG).show();
                if (pos == 1) {
                    adapterSerie = ArrayAdapter.createFromResource(MapsActivity.this, R.array.Serie_infantil, android.R.layout.simple_spinner_item);
                } else if (pos == 2) {
                    adapterSerie = ArrayAdapter.createFromResource(MapsActivity.this, R.array.Serie_fundamental, android.R.layout.simple_spinner_item);
                } else {
                    adapterSerie = ArrayAdapter.createFromResource(MapsActivity.this, R.array.Series, android.R.layout.simple_spinner_item);
                }
                serie.setAdapter(adapterSerie);
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
            setMarkerEscolas();
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
                            }
                        }
                    }
                }
            }
            dialog.dismiss();
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
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 100, this);
                break;
            case 2:
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 100, this);
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
        JSONObject escolas = null;
        JsonClass json = new JsonClass();

        escolas = json.getJSONFromUrl("http://200.151.25.77/webservice_semec/webservicesemec_teste/escolasprox", locationsource.latitude, locationsource.longitude, str_serie);

        getRoute(locationsource, escolas);

    }

    // WEB CONNECTION
    public void getRoute(final LatLng origin, final JSONObject destination) throws JSONException {
        new Thread() {
            public void run() {
                        /*String url= "http://maps.googleapis.com/maps/api/directions/json?origin="
                                + origin+"&destination="
								+ destination+"&sensor=false";*/
                String url = null;
                String tipo_r = "";
                int tipo = tipo_rota.getSelectedItemPosition();
                switch (tipo) {
                    case 0:
                        tipo_r = "&mode=walking";
                        break;
                    case 1:
                        tipo_r = "&mode=bicycling";
                        break;
                    case 2:
                        tipo_r = "&mode=driving";
                        break;
                    case 3:
                        tipo_r = "&mode=transit";
                        break;
                    default:
                }
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

    public void getDistance(View view) {

        Toast.makeText(MapsActivity.this, "Distancia: " + distance + " metros \n Duração: " + tempo, Toast.LENGTH_LONG).show();
    }

    public void getLocation(View view) {
        Geocoder gc = new Geocoder(MapsActivity.this);

        List<Address> addressList;
        try {
            if (pesquisa.getText().length() > 0) {
                //addressList = gc.getFromLocation(list.get(list.size() - 1).latitude, list.get(list.size() - 1).longitude, 1);
                addressList = gc.getFromLocationName(pesquisa.getText().toString(), 1);
                if(addressList.toString().equals("[]")){
                    Toast.makeText(MapsActivity.this, "Endereço não encontrado!", Toast.LENGTH_LONG).show();
                }else {
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
    public void getLocationEscola(Double lat,Double lng) {
        Geocoder gc = new Geocoder(MapsActivity.this);

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
}
