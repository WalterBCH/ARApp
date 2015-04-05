package com.wbch.testapp;

import android.content.Context;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends ActionBarActivity implements LocationProvider.LocationCallback{

    public static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private AutoCompleteTextView autoCompleteTextView;
    private ImageView searchButton;
    private ImageView locationButton;
    private PlaceAutoCompleteAdapter mAdapter;
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    private LocationProvider mLocationProvider;

    public static boolean apiTooLowForImmersive = false;

    static {
        apiTooLowForImmersive = (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        mLocationProvider = new LocationProvider(this, this);
        uiChangeListener();

        autoCompleteTextView = ((AutoCompleteTextView)findViewById(R.id.autoText));
        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //Log.i("AutoCompleteTextView", "Evento onEditorAction ... ");
                    search();
                    handled = true;
                }
                Log.i("AutoCompleteTextView", "Evento onEditorAction ... " + actionId);
                return handled;
            }
        });

        mAdapter = new PlaceAutoCompleteAdapter(this, R.layout.auto_complete_list_item, BOUNDS_GREATER_SYDNEY ,null);
        autoCompleteTextView.setAdapter(mAdapter);

        searchButton = (ImageView) findViewById(R.id.buscar);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.i("Buscar", "Se presiono el boton buscar");
                autoCompleteTextView.onEditorAction(EditorInfo.IME_ACTION_DONE);
                hideSoftKey();
            }
        });

        locationButton = (ImageView) findViewById(R.id.location);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMyLocation();
                hideSoftKey();
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.i("Touch", "Se presiono en el mapa");
                hideSoftKey();
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.i("Touch", "Se presiono y se mantuvo en el mapa");
                hideSoftKey();
            }
        });

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.i("Touch", "Se movio la camara");
                hideSoftKey();
            }
        });


    }

    void hideSoftKey(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
        autoCompleteTextView.clearFocus();
    }

    void search(){
        Toast.makeText(this, "Vamos a buscar", Toast.LENGTH_SHORT).show();
    }

    void setMyLocation(){
        Toast.makeText(this, "Centrando posicion", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mLocationProvider.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationProvider.disconnect();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Log.i("Orientacion", "Se volteo el cel");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemBars();
        }
    }

    public void uiChangeListener(){
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                RelativeLayout.LayoutParams bar = (RelativeLayout.LayoutParams)(findViewById(R.id.app_bar)).getLayoutParams();
                float scale = getResources().getDisplayMetrics().density;
                int marginPxTop;
                int marginPxNormal = (int) (8 * scale + 0.5f);

                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    //Log.i("MapsActivity", "Pantalla con teclado");
                    //Esta parte se ejecuta cuando se muestran las barras del sistema
                    //cambiar el margen con interfaz
                    marginPxTop = (int) (32 * scale + 0.5f);
                    hideSystemBars();
                }
                else{
                    //Log.i("MapsActivity", "Pantalla sin teclado");
                    //Esta parte se ejecuta cuando no se muestran las barras del sistema, status bar, nav bar
                    //cambiar el margen sin la interfaz
                    marginPxTop = (int) (8 * scale + 0.5f);
                    autoCompleteTextView.clearFocus();
                }

                bar.setMargins(marginPxNormal, marginPxTop, marginPxNormal, marginPxNormal);
                findViewById(R.id.app_bar).setLayoutParams(bar);
            }
        });
    }

    void hideSystemBars(){
        if (!apiTooLowForImmersive ) {
            getWindow().getDecorView().setSystemUiVisibility(
                      View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

    }

    @Override
    public void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Estoy aquí!!");
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
    }
}
