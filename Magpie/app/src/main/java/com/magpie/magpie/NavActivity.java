package com.magpie.magpie;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.magpie.magpie.CollectionUtils.Collection;
import com.magpie.magpie.CollectionUtils.Element;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NavActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMarkerClickListener {

    private final int REQUEST_LOCATION = 1;
    private final float DEFAULT_ZOOM = 18;

    private ArrayList<Collection> mCollections;
    private Collection mActiveCollection;
    private ArrayList<MarkerOptions> mAllCollectionMarkers;
    private ArrayList<MarkerOptions> mActiveCollectionMarkers;
    private MarkerOptions mActiveMarker;

    /**
     * Map-related member variables
     */
    private GoogleMap mMap;
    private LocationManager mLocManager;
    private Marker mSelectedMarker;
    private Location mMyLocation;
    private String mLastUpdateTime;
    private boolean mRequestingLocationUpdates;

    /**
     * Persistent UI elements.
     * These will persist across fragment changes.
     */
    private Toolbar mTitleBar;
    private RelativeLayout mViewBar;
    private FragmentManager mFragmentMngr;

    /**
     * view bar buttons
     */
    private Button mListViewButton;
    private Button mGridViewButton;
    private Button mMapViewButton;

    /**
     * nav bar buttons
     */
    private ImageButton mMapNavButton;
    private ImageButton mQRNavButton;
    private ImageButton mHomeNavButton;
    private ImageButton mSearchNavButton;
    private ImageButton mAccountNavButton;

    private boolean showingBadgePage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        mCollections = new ArrayList<>();
        mActiveCollection = new Collection();

        mAllCollectionMarkers = new ArrayList<>();
        mActiveCollectionMarkers = new ArrayList<>();
        mActiveMarker = new MarkerOptions();

        mTitleBar = (Toolbar)findViewById(R.id.nav_toolbar);
        mViewBar = (RelativeLayout)findViewById(R.id.view_bar);

        mListViewButton = (Button) findViewById(R.id.list_button);
        mGridViewButton = (Button) findViewById(R.id.grid_button);
        mMapViewButton = (Button) findViewById(R.id.map_button);

        mMapNavButton = (ImageButton) findViewById(R.id.map_nav_button);
        mQRNavButton = (ImageButton) findViewById(R.id.qr_nav_button);
        mHomeNavButton = (ImageButton) findViewById(R.id.home_nav_button);
        mSearchNavButton = (ImageButton) findViewById(R.id.search_nav_button);
        mAccountNavButton = (ImageButton) findViewById(R.id.account_nav_button);

        // TODO: set visibility of view_bar

        mFragmentMngr = getSupportFragmentManager(); // TODO: test this

        if (findViewById(R.id.fragment_container) != null) {

            /**
             * Don't inflate a new fragment if a saved instance state is present
             */
            if (savedInstanceState != null) {
                return;
            }

            /**
             * Get the intent to determine which Fragment to inflate.
             * THIS IS ONLY FOR TESTING!!!!!
             * Once the map test is no longer needed, this section is no longer needed.
             * TODO: remove the below if-else once testing is done
             */
            Intent i = getIntent();

            if (i.hasExtra("MAP_TEST")) {

                startTestMap();
                //startCollectionMapFragment();

            } else {

                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new Local_loc()).commit();
            }


        }

        /*
        if (i.hasExtra(mBundleExtraKey)) {

            Bundle b = i.getBundleExtra(mBundleExtraKey);

            if (b.containsKey(mActiveCollectionKey)) {
                mActiveCollection = (Collection) b.getSerializable(mActiveCollectionKey);
            }
        }
        */
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.list_button:
                // TODO: ensure starting in list view
                Toast.makeText(getApplicationContext(), "Not ready yet", Toast.LENGTH_SHORT).show();
                //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BadgePage()).commit();
                break;

            case R.id.grid_button:
                // TODO: BadgePage in grid view
                Toast.makeText(getApplicationContext(), "Not ready yet", Toast.LENGTH_SHORT).show();
                break;

            case R.id.map_button:
                // TODO: open map with current collection
                Toast.makeText(getApplicationContext(), "Not ready yet", Toast.LENGTH_SHORT).show();
                startCollectionMapFragment();
                break;

            case R.id.map_nav_button:
                // TODO: open map with all collections
                Toast.makeText(getApplicationContext(), "Not ready yet", Toast.LENGTH_SHORT).show();
                break;

            case R.id.qr_nav_button:
                Toast.makeText(getApplicationContext(), "Not ready yet", Toast.LENGTH_SHORT).show();
                break;

            case R.id.home_nav_button:
                Toast.makeText(getApplicationContext(), "Not ready yet", Toast.LENGTH_SHORT).show();
                //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Local_loc()).commit();
                break;

            case R.id.search_nav_button:
                Toast.makeText(getApplicationContext(), "Not ready yet", Toast.LENGTH_SHORT).show();
                break;

            case R.id.account_nav_button:
                Toast.makeText(getApplicationContext(), "Not ready yet", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /**
         * LocationManager instantiated here. LocationListener also attached.
         */
        mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // TODO: add LocationListener

        /*  This portion was added by default
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */


        mMyLocation = getLocation();
        if (mMyLocation != null) {
            moveToLocation(mMyLocation);
            //mTempCoordinateTextView.setText(mMyLocation.getLatitude()+", "+mMyLocation.getLongitude());

            // TESTING. TODO: remove testing parts
            mActiveCollection = Collection.collectionTestBuilder("Test Collection", mMyLocation.getLatitude(), mMyLocation.getLongitude());
            //mCollectionTitleTextView.setText(mCollection.getName());
            //placeTestMarkers();
            createMarkerList();
            placeMarkers();
            // TESTING END
        }

        //placeMarkers();

        initMap();
    }

    private void initMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            /**
             * Calls ActivityCompat.requestPermissions to request missing permissions.
             */
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }
        mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        /**
         * Register the map as a Marker click listener. This is so when a marker is clicked, it will
         * be set as the currently tracked Marker the user will be given relative info for in the
         * map UI
         */
        mMap.setOnMarkerClickListener(this);

        /**
         * After UI settings have been established and markers have been placed, the map begins
         * running.
         */
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                /*
                Location location = getLocation();
                if (location != null)
                    moveToLocation(location);
                */


                mMyLocation = getLocation();
                if (mMyLocation != null) {
                    moveToLocation(mMyLocation);
                    //mTempCoordinateTextView.setText("Lat: "+mMyLocation.getLatitude()+", Lon: "+mMyLocation.getLongitude());

                }

            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable, 200);
    }

    private Location getLocation() {

        Location location = null;

        String provider = getProvider(Criteria.ACCURACY_FINE, mLocManager.GPS_PROVIDER);
        try {
            location = mLocManager.getLastKnownLocation(provider);
        } catch (SecurityException e) {
            Log.e("Permission not granted", "Security Exception: "+e.getMessage());
        }

        if (location==null) {
            provider = getProvider(Criteria.ACCURACY_COARSE,
                    mLocManager.NETWORK_PROVIDER);
            try {
                location = mLocManager.getLastKnownLocation(provider) ;
            } catch(SecurityException e) {
                Log.e("Permission not granted", "Security Exception: "+e.getMessage());
            }
        }

        return location ;
    }

    /**
     * I moved the LocationManager to a member variable. It was originally passed as the first
     * argument in this method. I'm making this note in case I need to switch back for any reason.
     */
    private String getProvider(int accuracy, String defProvider) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(accuracy);

        String providerName = mLocManager.getBestProvider(criteria, false);
        if (providerName == null)
            providerName = defProvider;

        if (!mLocManager.isProviderEnabled(providerName)) {

            View parent = findViewById(R.id.map) ;
            Snackbar snack = Snackbar.make(parent, "Location Provider Not Enabled: Goto Settings?",
                    Snackbar.LENGTH_LONG) ;
            snack.setAction("Confirm", new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) ;
                }

            });

            snack.show();
        }

        return providerName ;
    }

    /**
     * Relocates the camera to be centered on the user's location
     * @param location the user's current location.
     */
    private void moveToLocation(Location location) {

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                location.getLongitude()), DEFAULT_ZOOM));
    }

    /**
     * Handles the case where the user grants location permission request
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMap() ;
            } else {
                /**
                 * TODO: Maybe a small popup warning screen?
                 * Might take user back to either login or list screen if permission is not granted.
                 */
                Toast.makeText(this, "Magpie requires location permissions.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Creates a list of markers from the Elements in the active collection.
     * Markers are saves in the mActiveCollectionMarkers member variable.
     */
    private void createMarkerList() {

        if (mActiveCollection != null) {
            ArrayList<Element> elements = mActiveCollection.getCollectionElements();
            for (Element element : elements) {

                MarkerOptions marker = new MarkerOptions();
                marker.position(new LatLng(element.getLatitude(), element.getLongitude()));
                marker.title(element.getName());
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pinavailable));

                mActiveCollectionMarkers.add(marker);
            }

            placeMarkers();
        }
    }

    /**
     * Places markers on the Map after markers were made in the createMarkerList method
     */
    private void placeMarkers() {

        for (MarkerOptions marker : mActiveCollectionMarkers) {
            mMap.addMarker(marker);
        }
    }

    private void placeAllCollectionMarkers() {

    }

    @Override
    public void onLocationChanged(Location location) {
        mMyLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void startLocationUpdates() {
        //LocationServices.FusedLocationApi.
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        onMarkerSelected(marker);
        return true;
    }

    private void onMarkerSelected(Marker marker) {

        mSelectedMarker = marker;
        float[] results = new float[1];
        Location.distanceBetween(mMyLocation.getLatitude(), mMyLocation.getLongitude(), mSelectedMarker.getPosition().latitude, mSelectedMarker.getPosition().longitude, results);
        //mDistanceTextView.setText("Distance: "+results[0]);
        //mTimeTextView.setText("Time: "+(results[0]/1.4)+"s");
        // TODO: fill in UI elements pertaining to marker.
    }

    /**
     * Starts a version of the map with only a single Marker representing a single badge selected by
     * the user.
     */
    public void startMarkerMapFragment() {
        // TODO: map showing ONE marker from ONE collection

        startMapFragment();
    }

    /**
     * Starts a version of the map populated with Markers for each Element in the currently active
     * collection.
     */
    public void startCollectionMapFragment() {
        // TODO: map showing ALL markers from ONE collection

        startMapFragment();
    }

    /**
     * Starts a version of the map populate with Markers for each Element in each Collection the
     * user is participating in.
     */
    public void startAllCollectionMapFragment() {
        // TODO: map showing ALL markers from ALL collections
        setTitle(getString(R.string.toolbar_badges_near_me));

        startMapFragment();
    }

    /**
     * Changes the fragment currently active in the fragment container
     * @param fr the fragment to be started.
     */
    public void startNewFragment(Fragment fr) {

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fr).commit();

    }

    /**
     * Starts the Map fragment.
     */
    public void startMapFragment() {

        MapFragment mapFragment = new MapFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, mapFragment).commit();
        mapFragment.getMapAsync(this);

    }

    public void setActiveCollection(Collection collection) {
        mActiveCollection = collection;
    }

    public ArrayList<Collection> getCollections() {
        return mCollections;
    }

    public void setCollections(ArrayList<Collection> collections) {
        this.mCollections = collections;
    }

    public void addCollection(Collection collection) {
        this.mCollections.add(collection);
    }

    public void addNewCollections(ArrayList<Collection> newCollections) {
        this.mCollections.addAll(newCollections);
    }

    public void setTitle(String title) {
        mTitleBar.setTitle(title.toUpperCase());
    }

    // TODO: remove this method once testing is done
    private void startTestMap() {
        MapFragment mapFragment = new MapFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, mapFragment).commit();
        mapFragment.getMapAsync(this);
        //mActiveCollection = Collection.collectionTestBuilder("Test Collection", mMyLocation.getLatitude(), mMyLocation.getLongitude());
    }


    /*
    @Override
    public void onBackPressed() {



        switch (getSupportFragmentManager().getFragments().get(0).getId()) {

            case R.id.activity_local_loc:
                super.onBackPressed();
                break;

            case R.id.fragment_obtainable_loc:
                b.putSerializable("CurrentCollections", localCollections);
                Fragment fr = new Obtainable_loc();
                fr.setArguments(b);
                android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.Nav_Activity, fr);
                ft.commit();
                break;
        }
    }
    */
}