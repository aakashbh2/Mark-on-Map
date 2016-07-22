package com.example.aakash.realmnkinvey;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyPingCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.model.FileMetaData;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    String appSecret = "1d8edbc552194baaacbc5f45fb7cc935";
    String appKey = "kid_By2TVtAV";
    Client kinveyClient;
    String dir;
    LinearLayout btnholder;
    private GoogleMap mMap;
    private int mapcount = 0, countsearch = 0, counttype = 0;
    Button btndraw, btnsattelite;
    private View draggableView;
    private List<LatLng> polylinePoints = new ArrayList<>();
    private Polyline polyline;
    List<LatLng> LatLnglist;
    CheckConnection cd;
    Boolean isInternetPresent;
    AutoCompleteTextView autoCompleteTextView;
    ImageView searcimage;
    Toolbar toolbar;
    TextView sc;
    String query;
    Marker marker = null;
    LatLng search;
    private CoordinatorLayout coordinatorLayout;
    List<LatlngValues> LatLngcluster;
    private ClusterManager<LatlngValues> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar();
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        autoCompleteTextView = (AutoCompleteTextView) toolbar.findViewById(R.id.autoCompleteTextView1);
        searcimage = (ImageView) toolbar.findViewById(R.id.searchbtn);
        sc = (TextView) toolbar.findViewById(R.id.sc);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        draggableView = findViewById(R.id.draggable);

        btndraw = (Button) findViewById(R.id.btndraw);
        btnholder = (LinearLayout) findViewById(R.id.btnholder);
        btnsattelite = (Button) findViewById(R.id.btnsattelite);

        btnsattelite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counttype++;
                if ((counttype % 2) == 0) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    btnsattelite.setText("Map View");
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    btnsattelite.setText("Satellite View");
                }
            }
        });

        cd = new CheckConnection(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        mapFragment.getMapAsync(this);

        autoCompleteTextView.setAdapter(new GooglePlacesAutocompleteAdapter(getApplicationContext(), R.layout.list_item));
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) parent.getItemAtPosition(position);
                countsearch++;
                showonMap(str);
            }
        });

        kinveyClient = new Client.Builder(appKey, appSecret, this).build();

        kinveyClient.ping(new KinveyPingCallback() {
            public void onFailure(Throwable t) {
                Log.e("return value", "Kinvey Ping Failed", t);
            }

            public void onSuccess(Boolean b) {
                Log.d("return value", "Kinvey Ping Success");
            }
        });

        if (!kinveyClient.user().isUserLoggedIn()) {
            kinveyClient.user().login("aakash", "aakash", new KinveyUserCallback() {
                @Override
                public void onFailure(Throwable t) {
                    CharSequence text = "Wrong username or password.";
                    Log.d("return value dude", "" + t);
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(User u) {
                    CharSequence text = "Welcome back," + u.getUsername() + ".";
                    Log.d("return value", text.toString());
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                    try {
                        getJsonFile();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            try {
                getJsonFile();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


        searcimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countsearch++;
                if (countsearch == 1) {
                    sc.setVisibility(View.INVISIBLE);
                    autoCompleteTextView.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(autoCompleteTextView, InputMethodManager.SHOW_IMPLICIT);
                }
                if (countsearch > 1) {
                    query = autoCompleteTextView.getText().toString();
                    if (query.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please Enter Region Name", Toast.LENGTH_LONG).show();
                    } else {
                        showonMap(query);
                    }
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }


    private void showonMap(String questr) {
        if (isInternetPresent) {
            Log.d("mystring", questr);
            autoCompleteTextView.setText(questr);
            List<Address> addressList = null;
            if (questr != null || !questr.equals("")) {
                Geocoder geocoder = new Geocoder(getApplicationContext());
                try {
                    addressList = geocoder.getFromLocationName(questr, 1);


                } catch (IOException e) {
                    e.printStackTrace();
                }

                Address address = addressList.get(0);

                Double lati = address.getLatitude();
                Double longi = address.getLongitude();
                String name = questr;
                search = new LatLng(lati, longi);
                //LatLnglist.add(search);

                IconGenerator iconFactory = new IconGenerator(this);
                iconFactory.setStyle(IconGenerator.STYLE_RED);
                addIcon(iconFactory, name, search);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(search, 14.0f));
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please Check Connectivity", Toast.LENGTH_LONG).show();
        }
    }

    private void addIcon(IconGenerator iconFactory, CharSequence text, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        marker = mMap.addMarker(markerOptions);
    }

    @Override
    public void onBackPressed() {
        if (countsearch != 0) {
            countsearch = 0;
            autoCompleteTextView.setText("");
            if (marker != null) {
                marker.remove();
            }
            autoCompleteTextView.setVisibility(View.INVISIBLE);
            sc.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // methods of cluster
        mClusterManager = new ClusterManager<LatlngValues>(this, mMap);
        mMap.setOnCameraChangeListener(mClusterManager);

        new GetMap().execute();
        btndraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapcount == 0) {
                    mapcount = 1;
                    draggableView.setVisibility(View.VISIBLE);
                    btndraw.setText("Stop Marking");
                    draggableView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            LatLng position = mMap.getProjection().fromScreenLocation(
                                    new Point((int) motionEvent.getX(), (int) motionEvent.getY()));

                            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                polylinePoints.add(position);
                                polyline = mMap.addPolyline(new PolylineOptions().addAll(polylinePoints).color(Color.BLUE));

                            } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                                polylinePoints.add(position);
                                polyline.setPoints(polylinePoints);

                            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                                if (!(polylinePoints.get(0).equals((polylinePoints.get((polylinePoints.size()) - 1))))) {
                                    LatLng newvalue = polylinePoints.get(0);
                                    polylinePoints.add(newvalue);
                                    draggableView.setVisibility(View.INVISIBLE);
                                    mMap.clear();
                                    polyline = mMap.addPolyline(new PolylineOptions().addAll(polylinePoints).color(Color.BLUE));
                                }

                                for (int i = 0; i < LatLnglist.size(); i++) {
                                    if (isPointInPolygon(LatLnglist.get(i), polylinePoints)) {
                                        mMap.addMarker(new MarkerOptions().position(LatLnglist.get(i)));
                                    }
                                }
                            }
                            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                                @Override
                                public void onCameraChange(CameraPosition cameraPosition) {

                                }
                            });
                            return true;
                        }
                    });
                } else {
                    draggableView.setVisibility(View.INVISIBLE);
                    btndraw.setText("Mark Manually");
                    mMap.clear();
                    mClusterManager = new ClusterManager<LatlngValues>(getApplicationContext(), mMap);
                    mMap.setOnCameraChangeListener(mClusterManager);
                    new GetMap().execute();
                    mapcount = 0;
                    if (polyline != null) {
                        polyline.remove();
                        polyline = null;
                        polylinePoints.clear();
                    }
                }
            }
        });
    }

    private boolean isPointInPolygon(LatLng tap, List<LatLng> vertices) {
        int intersectCount = 0;
        for (int j = 0; j < vertices.size() - 1; j++) {
            if (LineIntersect(tap, vertices.get(j), vertices.get(j + 1))) {
                intersectCount++;
            }
        }
        return (intersectCount % 2) == 1; // odd = inside, even = outside;
    }

    private boolean LineIntersect(LatLng tap, LatLng vertA, LatLng vertB) {
        double aY = vertA.latitude;
        double bY = vertB.latitude;
        double aX = vertA.longitude;
        double bX = vertB.longitude;
        double pY = tap.latitude;
        double pX = tap.longitude;
        if ((aY > pY && bY > pY) || (aY < pY && bY < pY) || (aX < pX && bX < pX)) {
            return false;
        }
        double m = (aY - bY) / (aX - bX);
        double bee = (-aX) * m + aY;                // y = mx + b
        double x = (pY - bee) / m;
        return x > pX;
    }


    private void getJsonFile() throws FileNotFoundException {
        dir = "/data/data/" + getPackageName() + "/files/file2.txt";
        Log.d("download", dir);
        isInternetPresent = cd.isConnectingToInternet();

        if (isInternetPresent) {

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)btnholder.getLayoutParams();
            params.setMargins(0, 0, 0, 15);
            btnholder.setLayoutParams(params);
            File file = new File(dir);

            FileMetaData fileMetaData = new FileMetaData("51c5125c-819b-40aa-98de-5db04543d8d6");
            //f02d538e-a815-4e38-aa96-f42180c89bdc
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            kinveyClient.file().download(fileMetaData, fileOutputStream, new DownloaderProgressListener() {

                @Override
                public void onSuccess(Void result) {
                    Log.d("return value", "successfully downloaded file");
                    new GetMap().execute();
                }

                @Override
                public void onFailure(Throwable error) {
                    Log.e("return value", "failed to downloaded file.", error);
                }

                @Override
                public void progressChanged(MediaHttpDownloader downloader) throws IOException {
                    Log.i("return value", "progress updated: " + downloader.getDownloadState());
                    // any updates to UI widgets must be done on the UI thread
                }
            });
        } else if (!dir.isEmpty()) {

            new GetMap().execute();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)btnholder.getLayoutParams();
            params.setMargins(0, 0, 0, 80);
            btnholder.setLayoutParams(params);
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "No internet connection! ", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                getJsonFile();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });

            // Changing action button text color
            snackbar.setActionTextColor(Color.RED);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);

            snackbar.show();

        } else if (dir.isEmpty()){
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)btnholder.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            btnholder.setLayoutParams(params);
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Plz check Internet Connection!!! ", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                getJsonFile();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });

            // Changing action button text color
            snackbar.setActionTextColor(Color.RED);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);

            snackbar.show();


        }
    }

    public class GetMap extends AsyncTask<Void, Void, List<LatlngValues>> {

        BufferedReader reader;

        @Override
        protected List<LatlngValues> doInBackground(Void... params) {

            InputStream is = null;
            try {
                is = new FileInputStream(dir);
                Writer writer = new StringWriter();
                char[] buffer = new char[1024];

                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
                String jsonString = writer.toString();

                JSONObject parent_object = new JSONObject(jsonString);
                JSONArray results_array = parent_object.getJSONArray("result");

                LatLnglist = new ArrayList<>();
                LatLngcluster = new ArrayList<>();

                for (int i = 0; i < results_array.length(); i++) {
                   JSONObject c = results_array.getJSONObject(i);

                    String lati = c.getString("lat");
                    String longi = c.getString("lng");
                    double LAT = Double.parseDouble(lati);
                    double LON = Double.parseDouble(longi);
                    LatLngcluster.add(new LatlngValues(LAT, LON));
                    LatLnglist.add(new LatLng(LAT, LON));
                }
                return LatLngcluster;

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<LatlngValues> result) {
            super.onPostExecute(result);

            if (result == null) {
                Log.d("return value1", "error getting data");
                return;
            } else if (result.size() == 0) {
                Log.d("return value2", "error getting data");
                return;
            }

            mClusterManager.clearItems();
            mClusterManager.addItems(result);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(result.get(0).getPosition()).zoom(14.0f).bearing(5).build()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(result.get(0).getPosition(), 14.0f));
        }
    }
}