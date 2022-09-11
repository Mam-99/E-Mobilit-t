package com.example.e_mobility;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_mobility.Fragment.KundeFavorite;
import com.example.e_mobility.Fragment.KundeHistory;
import com.example.e_mobility.Fragment.ServiceDefekt;
import com.example.e_mobility.Fragment.ServiceHistory;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GetChargingStationsActivity extends AppCompatActivity implements OnMapReadyCallback {

    final int MY_PERMISSIONS_STORAGE_INTENRET = 1;

    private RequestService mService = null;
    private RequestService.RequestServiceBinder binder;
    boolean mBound = false;

    private static String filePath = "NZSE";
    private static String csvFile; // liefert die Downloadfunktion

    private String ladestationen = "ladestationen.txt"; // Name der lokalen Datei
    // unser Beispiel
    // **********************
    private String
            url = "https://www.bundesnetzagentur.de/SharedDocs/Downloads/DE/Sachgebiete/Energie/Unternehmen_Institutionen/" +
            "E_Mobilitaet/Ladesaeulenregister_CSV.csv?__blob=publicationFile&v=33";
    // **********************

    private static ArrayList<Ladestation> ladestationArrayList = new ArrayList<>();

    private GoogleMap mMap;
    private FusedLocationProviderClient client;

    private LoadingDialog loadingDialog;
    private SupportMapFragment mapFragment;

    private Location myLocation;

    //--------------------------    Serviceverbindung einrichten
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // an den RequestService binden,
            // Service-Objekt casting auf IBinder und LocalService instance erhalten
            binder = (RequestService.RequestServiceBinder) service;
            mService = binder.getService();
            // callback setzen
            mService.setCallback(getHandler());

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    //--------------------------

    //--------------------------Handler einrichten:
    // Callbacks vereinbaren für Service Binding,
    // weiterleiten an bindService() für Ergebnismitteilung

    private Handler getHandler() {
        final Handler callbackHandler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                csvFile = (String) bundle.get(RequestService.FILEPATH);
                // Datei einlesen
                GetChargingStationsActivity.csvRead();

                String uniqueId = (String) bundle.get(RequestService.UNIQUEID);
                String note = (String) bundle.get(RequestService.NOTIFICATION);
                loadingDialog.dimissDialog();
                Toast.makeText(GetChargingStationsActivity.this, uniqueId + " file: "
                        + csvFile + " Bytes: " + note + " Size: "
                        + ladestationArrayList.size(), Toast.LENGTH_LONG).show();

                mapFragment = (SupportMapFragment)
                        getSupportFragmentManager().findFragmentById(R.id.map);

                client = LocationServices.getFusedLocationProviderClient(GetChargingStationsActivity.this);

                if (ActivityCompat.checkSelfPermission(GetChargingStationsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // When permission granted
                    Task<Location> task = client.getLastLocation();
                    task.addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // When success
                            if(location != null) {
                                myLocation = location;
                                mapFragment.getMapAsync(GetChargingStationsActivity.this);
                            }
                        }
                    });
                    //mapFragment.getMapAsync(GetChargingStationsActivity.this);
                } else {
                    ActivityCompat.requestPermissions(GetChargingStationsActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }// handleMessage
        };
        return callbackHandler;
    }

    //--------------------------

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private SearchView searchView;
    private LatLng myPosition;
    private Spinner distanceOption;
    private Spinner stromOption;

    private Set<String> favoriteList = new HashSet<>();
    private Set<String> historyList = new HashSet<>();
    private Set<String> defektList = new HashSet<>();
    private Set<String> doneList = new HashSet<>();

    private double kW = 300;
    private double km = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("E-Mobilität");
        setContentView(R.layout.activity_get_charging_stations);

        myPosition = new LatLng (49.86625273516996, 8.640257820411557);

        historyList = PrefConfig.loadHistoryList(GetChargingStationsActivity.this);
        favoriteList = PrefConfig.loadFavoriteList(GetChargingStationsActivity.this);
        defektList = PrefConfig.loadDefektList(GetChargingStationsActivity.this);
        doneList = PrefConfig.loadServiceList(GetChargingStationsActivity.this);

        Toast.makeText(this, "Loaded data! " + String.valueOf(historyList.size()) + ", "
                        + String.valueOf(favoriteList.size()) + ", " + String.valueOf(defektList.size()) + ", "
                        + String.valueOf(doneList.size())
                , Toast.LENGTH_SHORT).show();

        loadingDialog = new LoadingDialog(GetChargingStationsActivity.this);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(GetChargingStationsActivity.this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()){
                    case R.id.nav_kunde_favorite:
                        intent = new Intent(GetChargingStationsActivity.this, KundeFavorite.class);
                        ArrayList<String> list_favorite = new ArrayList<>();
                        list_favorite.addAll(favoriteList);
                        intent.putExtra("list", list_favorite);
                        startActivityForResult(intent, 2);
                        break;
                    case R.id.nav_kunde_history:
                        intent = new Intent(GetChargingStationsActivity.this, KundeHistory.class);
                        ArrayList<String> list_kunde = new ArrayList<>();
                        list_kunde.addAll(historyList);
                        intent.putExtra("list", list_kunde);
                        startActivityForResult(intent, 2);
                        break;
                    case R.id.nav_service_history:
                        intent = new Intent(GetChargingStationsActivity.this, ServiceHistory.class);
                        ArrayList<String> list_done = new ArrayList<>();
                        list_done.addAll(doneList);
                        intent.putExtra("list", list_done);
                        startActivityForResult(intent, 3);
                        break;
                    case R.id.nav_service_list:
                        intent = new Intent(GetChargingStationsActivity.this, ServiceDefekt.class);
                        ArrayList<String> list_defekt = new ArrayList<>();
                        list_defekt.addAll(defektList);
                        intent.putExtra("list", list_defekt);
                        startActivityForResult(intent, 2);
                        break;
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        searchView = findViewById(R.id.sv_location);
        searchView.onWindowFocusChanged(false);
        searchView.clearFocus();

        distanceOption = findViewById(R.id.distance_spinner);
        stromOption = findViewById(R.id.strom_spinner);

        Button b;
        Button downlaod;
        downlaod = findViewById(R.id.download);
        downlaod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String fPath;
                String fileName;

                System.out.println("DOWNLOAD starten");
                if (mService != null)
                    binder.runURLDownload("download", url, filePath, ladestationen);
                //else
                //    ... Hinweis

                loadingDialog.startLoadingDialog();
                downlaod.setVisibility(View.INVISIBLE);
            }
        });

        // Bind to RequestService
        Intent myIntent = new Intent(this, RequestService.class);
        // startService(myIntent); oder alternativ:
        bindService(myIntent, mConnection, Context.BIND_AUTO_CREATE);

        // Permission grant/gewähren
        String[] permissions =
                {Manifest.permission.INTERNET,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions,
                MY_PERMISSIONS_STORAGE_INTENRET);

        if (mService != null)
            binder.runURLDownload("download", url, filePath, ladestationen);
        else
            System.out.println("onCreate: no service");

    }// onCreate

    @Override
    public void onBackPressed() {
        searchView.clearFocus();
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_STORAGE_INTENRET: {
                // wenn die Anfrage gecancelled wird, sind die Ergnisfelder leer.
                if (grantResults.length > 0) {
                    for (int grant = 0; grant < grantResults.length; grant++) {
                        if (grantResults[grant] == PackageManager.PERMISSION_GRANTED)
                            // Permissions wurden gewährt
                            System.out.println(permissions[grant] + " vorhanden");
                            // Berechtigungen stehen zu Verfügung, Zugriffe ausführen ..
                        else
                            System.out.println(permissions[grant] + "  n i c h t  vorhanden");
                        // Permissions werden abgelehnt,
                        // spezifische Zugriffe werden nicht ausgeführt
                    }
                }
                return;
            }
            // ... u.U. Prüfung anderer/weiterer Permissions
        }
    }

    public static void csvRead() {
        String csvline = ""; // eingelesene csvZeile
        String delimiter = ";"; // Trennzeichen
        File myFile; // Fileobjekt

        try {
            // lokale Datei ansprechen
            myFile = new File(csvFile);
            // Environment.getExternalStorageDirectory().getPath() + );
            // new File(getApplicationContext().getExternalFilesDir("NZSE").getPath() + "/" + csvFile);
            FileInputStream fIn = new FileInputStream(myFile);
            InputStreamReader isr = new InputStreamReader(fIn, "ISO_8859-1");//"Windows-1252");// StandardCharsets.UTF_16);
            BufferedReader myReader = new BufferedReader(isr);

            System.out.println("csv Einlesen starten ");
            System.out.println("... die ersten Zeilen überlesen");
            for (int n = 0; n < 11; n++) {
                myReader.readLine();
            } // for

            int zeilen = 0;
            String[] ladestation = null;

            while ((csvline = myReader.readLine()) != null) // Dateiende?
            {
                zeilen++;
                //System.out.println("Line " + zeilen);
                ladestation = csvline.split(delimiter);
                if (ladestation[0].equals("\"Volkswagen Sachsen GmbH")) continue;
                if (ladestation[0].equals("RhönEnergie Fulda GmbH")) continue;
                if (ladestation[0].equals("Marotech GmbH")) continue;
                if (ladestation[0].equals("\"Gemeinde Erdmannhausen")) continue;
                if (ladestation[0].equals("WSW Energie & Wasser AG")) continue;
                if (ladestation[0].equals("\"Ladestation Berglen &#214")) continue;
                if (ladestation[0].equals("\"Café")) continue;
                if (ladestation[0].equals("\"Hasselberg Elektrotechnik GmbH &amp")) continue;
                if (ladestation[0].equals("Hohenloher Berg Go-Kart und Berg Trampolin Großhandel"))
                    continue;
                if (ladestation.length < 8) continue;
                if (ladestation.length > 25) continue;

                // **********************
                // ...  Objekte anlegen

                String Betreiber = ladestation[0];
                //System.out.println(Betreiber);
                String Straße = ladestation[1];
                //System.out.println(Straße);
                if (Straße.equals("Dorfplatz")) continue;
                if (Straße.equals("Bachgasse")) continue;
                if (Straße.equals("Carl-von-Bach-Straße")) continue;
                if (Straße.equals("\"Weberstraße")) continue;
                if (Straße.equals("Holstenstraße")) continue;
                if (Straße.equals("0")) continue;
                if (Straße.equals(" Privatier\"")) continue;
                String Hausnummer = ladestation[2];
                //System.out.println(Hausnummer);
                String Adresszusatz = ladestation[3];
                if (Adresszusatz.equals("\"Parkplatz Jahnstraße")) continue;
                //System.out.println(Adresszusatz);
                int Postleitzahl = Integer.parseInt(ladestation[4]);
                //System.out.println(Postleitzahl);
                String Ort = ladestation[5];
                //System.out.println(Ort);
                String Bundesland = ladestation[6];
                //System.out.println(Bundesland);
                String Kreis = ladestation[7];
                //System.out.println(Kreis);

                String StringBreitengrad = ladestation[8];
                int index = StringBreitengrad.indexOf(',');
                if (index > 0) {
                    StringBreitengrad = StringBreitengrad.substring(0, index) + '.' + StringBreitengrad.substring(index + 1);
                }
                float Breitengrad = Float.parseFloat(StringBreitengrad);
                //System.out.println(Breitengrad);

                String StringLängengrad = ladestation[9];
                index = StringLängengrad.indexOf(',');
                if (index > 0) {
                    StringLängengrad = StringLängengrad.substring(0, index) + '.' + StringLängengrad.substring(index + 1);
                }
                float Längengrad = Float.parseFloat(StringLängengrad);
                //System.out.println(Längengrad);

                String Inbetriebnahmedatum = ladestation[10];
                //System.out.println(Inbetriebnahmedatum);
                String Anschlussleistung = ladestation[11];
                //System.out.println(Anschlussleistung);
                String Ladeeinrichtung = ladestation[12];
                //System.out.println(Ladeeinrichtung);
                int Anzahl_Ladepunkte = Integer.parseInt(ladestation[13]);
                //System.out.println(Anzahl_Ladepunkte);

                ArrayList<Ladepunkt> ladepunkts = new ArrayList<>();

                if (Anzahl_Ladepunkte == 2 && ladestation.length == 17) {
                    String Steckertype = ladestation[14];
                    //System.out.println(Steckertype);

                    int kW = Integer.parseInt(ladestation[15]);
                    //System.out.println(kW);

                    Ladepunkt ladepunkt1 = new Ladepunkt(Steckertype, kW, "");

                    csvline = myReader.readLine();
                    csvline = myReader.readLine();
                    csvline = myReader.readLine();

                    ladestation = csvline.split(delimiter);

                    Steckertype = ladestation[1];
                    //System.out.println(Steckertype);

                    kW = Integer.parseInt(ladestation[2]);
                    //System.out.println(kW);
                    Ladepunkt ladepunkt2 = new Ladepunkt(Steckertype, kW, "");

                    ladepunkts.add(ladepunkt1);
                    ladepunkts.add(ladepunkt2);

                    csvline = myReader.readLine();
                    csvline = myReader.readLine();
                    csvline = myReader.readLine();

                } else {
                    int start = 13;

                    for (int i = 0; i < Anzahl_Ladepunkte; i++) {
                        String Steckertype = ladestation[start + 1];
                        //System.out.println(Steckertype);

                        String kWStr = ladestation[start + 2];
                        int pos = kWStr.indexOf(',');
                        if (pos > 0) {
                            kWStr = kWStr.substring(0, pos) + '.' + kWStr.substring(pos + 1);
                        }

                        double kW = Double.parseDouble(kWStr);

                        //System.out.println(kW);

                        Ladepunkt ladepunkt = new Ladepunkt(Steckertype, kW, "");
                        ladepunkts.add(ladepunkt);
                        start = start + 3;
                        if (start >= ladestation.length - 1) break;
                    }

                }

                Ladestation station = new Ladestation(Betreiber, Straße, Hausnummer,
                        Adresszusatz, Postleitzahl, Ort, Bundesland, Kreis, Breitengrad, Längengrad,
                        Inbetriebnahmedatum, Anschlussleistung, Ladeeinrichtung,
                        Anzahl_Ladepunkte, ladepunkts);

                ladestationArrayList.add(station);

                //System.out.println("Initialize Ladestation finished...\n");
                // **********************

            } // while

            System.out.println("Finish...");


        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } // try-catch
    } // csvRead

    @Override
    public void finish() {
        System.out.println("******  bye bye");
        unbindService(mConnection);
        super.finish();
    }

    private float distance(float lat1, float lon1, float lat2, float lon2) {
        float dx = (float)71.5 * (lon1 - lon2);
        float dy = (float)111.3 * (lat1 - lat2);

        float km = (float) Math.sqrt(dx * dx + dy * dy);
        return km;
    }

    private BitmapDescriptor bitmapDescriptor(Context context, int vectorID) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorID);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void DisplayTrack(LatLng start, LatLng end) {
        Uri uri = Uri.parse("https://www.google.co.in/maps/dir/" + start.latitude
                + ", " + start.longitude + "/"
                + end.latitude + ", " + end.longitude);
        // initializing a intent with action view.
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        // below line is to set maps package name
        i.setPackage("com.google.android.apps.maps");
        // below line is to set flags
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // start activity
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        searchView.clearFocus();

        if(requestCode == 1){   // List
            if(resultCode == RESULT_OK){
                String ladestation = data.getStringExtra("result");
                historyList.add(ladestation);

                String[] split = ladestation.split("\n");

                String[] splitAgain = split[2].split(" ");

                String lat = splitAgain[1].substring(0, splitAgain[1].length()-1);
                String lon = splitAgain[3];

                LatLng ziel = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));

                DisplayTrack(myPosition, ziel);

                Toast.makeText(this, "Add to History", Toast.LENGTH_SHORT).show();
            }
            if(resultCode == 2){    // Favorite
                String ladestation = data.getStringExtra("result");
                favoriteList.add(ladestation);
                Toast.makeText(this, "Add to favorite", Toast.LENGTH_SHORT).show();
            }
            if(resultCode == 3){    // Defekt
                String ladestation = data.getStringExtra("result");
                defektList.add(ladestation);

                String[] split = ladestation.split("\n");

                String[] splitAgain = split[2].split(" ");

                float lat = Float.parseFloat(splitAgain[1].substring(0, splitAgain[1].length() - 1));
                float lon = Float.parseFloat(splitAgain[3]);

                for(Ladestation l : ladestationArrayList){
                    if(l.getBreitengrad() == lat && l.getLängengrad() == lon){
                        l.setDefekt(true);
                    }
                }
                createUmgebung(myPosition, km, kW);

                Toast.makeText(this, "Add to defekt list", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == 2) {
            if(resultCode == 5) {
                ArrayList<String> list = (ArrayList<String>) data.getSerializableExtra("resultList");
                historyList.clear();
                historyList.addAll(list);
                //Toast.makeText(this, "Update historyList!", Toast.LENGTH_SHORT).show();
            }
            if(resultCode == 6) {
                ArrayList<String> list = (ArrayList<String>) data.getSerializableExtra("resultList");
                favoriteList.clear();
                favoriteList.addAll(list);
                Toast.makeText(this, "Update favoriteList!", Toast.LENGTH_SHORT).show();
            }
            if(resultCode == 7) {
                ArrayList<String> list = (ArrayList<String>) data.getSerializableExtra("resultList");
                ArrayList<String> done = (ArrayList<String>) data.getSerializableExtra("done");
                doneList.addAll(done);
                for(String str : doneList){
                    String[] split = str.split("\n");
                    String[] splitAgain = split[2].split(" ");
                    float lat = Float.parseFloat(splitAgain[1].substring(0, splitAgain[1].length() - 1));
                    float lon = Float.parseFloat(splitAgain[3]);
                    for(Ladestation l : ladestationArrayList){
                        if(l.getBreitengrad() == lat && l.getLängengrad() == lon){
                            l.setDefekt(false);
                        }
                    }
                }
                defektList.clear();
                defektList.addAll(list);
                createUmgebung(myPosition, km, kW);
                Toast.makeText(this, "Update defektList!", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == 3) {
            if(resultCode == 9) {
                ArrayList<String> list = (ArrayList<String>) data.getSerializableExtra("resultList");
                doneList.clear();
                doneList.addAll(list);
                Toast.makeText(this, "Update Service-List!", Toast.LENGTH_SHORT).show();
            }
        }

        // Save data
        PrefConfig.saveStringSet(GetChargingStationsActivity.this, historyList, favoriteList, defektList, doneList);
        Toast.makeText(this, "Saved data! " + String.valueOf(historyList.size()) + ", "
                + String.valueOf(favoriteList.size()) + ", " + String.valueOf(defektList.size()) + ", "
                + String.valueOf(doneList.size())
                , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Setting for google map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;
                if(location != null || !location.equals("")){
                    Geocoder geocoder = new Geocoder(GetChargingStationsActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    myPosition = latLng;

                    createUmgebung(myPosition, km, kW);
                    searchView.clearFocus();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                searchView.clearFocus();
                String info = marker.getSnippet();

                Intent intent = new Intent(GetChargingStationsActivity.this, InfoPopUp.class);
                intent.putExtra("info", info);
                startActivityForResult(intent, 1);
                return true;
            }
        });

        /*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Permission denied!");
            ActivityCompat.requestPermissions(GetChargingStationsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
        mMap.setMyLocationEnabled(true);
         */
        //mMap.setMyLocationEnabled(true);

        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.
                defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .position(myPosition).title("Hochschule Darmstadt"));
        float zoomLevel = (float) 15.0;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, zoomLevel));
        createUmgebung(myPosition, km, kW);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(GetChargingStationsActivity.this,
                R.array.entfernung, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceOption.setAdapter(arrayAdapter);
        distanceOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String text = adapterView.getItemAtPosition(i).toString();
                switch (text){
                    case "30 km":
                        km = 30;
                        break;
                    case "50 km":
                        km = 50;
                        break;
                    case "100 km":
                        km = 100;
                        break;
                }
                searchView.clearFocus();
                //Toast.makeText(adapterView.getContext(), String.valueOf(km), Toast.LENGTH_LONG).show();
                createUmgebung(myPosition, km, kW);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                searchView.clearFocus();
            }
        });

        ArrayAdapter<CharSequence> arrayAdapter1 = ArrayAdapter.createFromResource(GetChargingStationsActivity.this,
                R.array.stromstarke, android.R.layout.simple_spinner_item);
        arrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stromOption.setAdapter(arrayAdapter1);
        stromOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String text = adapterView.getItemAtPosition(i).toString();
                switch (text){
                    case "> 0kW":
                        kW = 0;
                        break;
                    case "> 50kW":
                        kW = 50;
                        break;
                    case "> 100kW":
                        kW = 100;
                        break;
                }
                searchView.clearFocus();
                //Toast.makeText(adapterView.getContext(), String.valueOf(kW), Toast.LENGTH_LONG).show();
                createUmgebung(myPosition, km, kW);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                searchView.clearFocus();
            }
        });
    }

    public void createUmgebung(LatLng position, double setKm, double setKW) {
        mMap.clear();

        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.
                defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).
                position(myPosition).title("My Position"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 15));

        for(int i=0; i<ladestationArrayList.size(); i++) {
            float abstand = distance((float)position.latitude, (float)position.longitude,
                    ladestationArrayList.get(i).getBreitengrad(), ladestationArrayList.get(i).getLängengrad());
            if(abstand <= setKm) {
                boolean ok = false;
                for(Ladepunkt l : ladestationArrayList.get(i).getLadepunkte()){
                    if(l.getkW() >= setKW){
                        ok = true;
                        break;
                    }
                }

                if(ok){
                    LatLng station = new LatLng(ladestationArrayList.get(i).getBreitengrad(),
                            ladestationArrayList.get(i).getLängengrad());
                    MarkerOptions markerOptions;
                    if(ladestationArrayList.get(i).isDefekt()){
                        markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(station)
                                .title("Ladesäule")
                                .snippet(ladestationArrayList.get(i).info());
                    }
                    else {
                        markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).position(station)
                                .title("Ladesäule")
                                .snippet(ladestationArrayList.get(i).info());
                    }
                    mMap.addMarker(markerOptions);
                }
            }
        }
    }

} // GetChargingStationActivity
