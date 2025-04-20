package com.example.naariv2;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private com.google.android.gms.maps.model.Polyline currentRoute;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;

    private LatLng userLocation;
    private View infoBox;
    private TextView distanceText, timeText;

    // Add CheckBoxes for toggling place visibility
    private CheckBox policeStationCheckbox;
    private CheckBox hospitalCheckbox;

    // Store markers for police stations and hospitals
    private List<Marker> policeMarkers = new ArrayList<>();
    private List<Marker> hospitalMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize views
        infoBox = findViewById(R.id.infoBox);
        distanceText = findViewById(R.id.distanceText);
        timeText = findViewById(R.id.timeText);

        // Initialize CheckBoxes
        policeStationCheckbox = findViewById(R.id.policeStationCheckbox);
        hospitalCheckbox = findViewById(R.id.hospitalCheckbox);

        // Set default state to checked (true)
        policeStationCheckbox.setChecked(true);
        hospitalCheckbox.setChecked(true);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestQueue = Volley.newRequestQueue(this);

        // Load the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Map fragment is null", Toast.LENGTH_SHORT).show();
        }

        // Add listeners for checkboxes
        policeStationCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> updateMarkers("police", isChecked));
        hospitalCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> updateMarkers("hospital", isChecked));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        mMap.setMyLocationEnabled(true);

        // LocationRequest for more accurate location fetching
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull com.google.android.gms.location.LocationResult locationResult) {
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    userLocation = new LatLng(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                    fetchNearbyPlaces(userLocation.latitude, userLocation.longitude, "police");
                    fetchNearbyPlaces(userLocation.latitude, userLocation.longitude, "hospital");
                } else {
                    Toast.makeText(MapsActivity.this, "Could not get current location", Toast.LENGTH_SHORT).show();
                }
            }
        }, Looper.getMainLooper());

        mMap.setOnMarkerClickListener(marker -> {
            LatLng markerPosition = marker.getPosition();
            getTravelDistanceUsingGoogleApi(userLocation, markerPosition);
            return true; // return true to indicate the event was handled
        });
    }

    private void fetchNearbyPlaces(double latitude, double longitude, String placeType) {
        String apiKey = getApiKey();
        if (apiKey == null) {
            Toast.makeText(this, "API key missing in AndroidManifest.xml", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + latitude + "," + longitude +
                "&radius=2000" +
                "&type=" + placeType +
                "&key=" + apiKey;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);
                            JSONObject geometry = place.getJSONObject("geometry");
                            JSONObject location = geometry.getJSONObject("location");

                            double lat = location.getDouble("lat");
                            double lng = location.getDouble("lng");
                            String name = place.getString("name");

                            LatLng latLng = new LatLng(lat, lng);
                            MarkerOptions marker = new MarkerOptions()
                                    .position(latLng)
                                    .title(name)
                                    .icon(placeType.equals("police") ?
                                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED) :
                                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                            if (placeType.equals("police")) {
                                policeMarkers.add(mMap.addMarker(marker));
                            } else {
                                hospitalMarkers.add(mMap.addMarker(marker));
                            }
                        }
                        updateMarkers(placeType, placeType.equals("police") ? policeStationCheckbox.isChecked() : hospitalCheckbox.isChecked());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing " + placeType + " data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to fetch " + placeType, Toast.LENGTH_SHORT).show());

        requestQueue.add(request);
    }

    private void updateMarkers(String placeType, boolean show) {
        if (placeType.equals("police")) {
            for (Marker marker : policeMarkers) {
                marker.setVisible(show);
            }
        } else if (placeType.equals("hospital")) {
            for (Marker marker : hospitalMarkers) {
                marker.setVisible(show);
            }
        }
    }

    private void getTravelDistanceUsingGoogleApi(LatLng origin, LatLng destination) {
        String apiKey = getApiKey();
        if (apiKey == null) {
            Toast.makeText(this, "API key missing in AndroidManifest.xml", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&key=" + apiKey;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray routes = response.getJSONArray("routes");
                        JSONObject route = routes.getJSONObject(0);
                        JSONArray legs = route.getJSONArray("legs");
                        JSONObject leg = legs.getJSONObject(0);

                        String distance = leg.getJSONObject("distance").getString("text");
                        String duration = leg.getJSONObject("duration").getString("text");

                        distanceText.setText("Distance: " + distance);
                        timeText.setText("Time: " + duration);
                        infoBox.setVisibility(View.VISIBLE);

                        String polyline = route.getJSONObject("overview_polyline").getString("points");
                        drawRouteOnMap(decodePolyline(polyline));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing Directions API response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to fetch directions", Toast.LENGTH_SHORT).show());

        requestQueue.add(request);
    }

    private void drawRouteOnMap(List<LatLng> route) {
        if (currentRoute != null) {
            currentRoute.remove();  // Clear previous route
        }

        PolylineOptions options = new PolylineOptions().color(Color.BLUE).width(5);
        for (LatLng point : route) {
            options.add(point);
        }

        currentRoute = mMap.addPolyline(options); // Save reference to current route
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> polyline = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int shift = 0, result = 0;
            while (true) {
                int b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
                if (b < 0x20) break;
            }
            int dLat = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            lat += dLat;

            shift = 0;
            result = 0;
            while (true) {
                int b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
                if (b < 0x20) break;
            }
            int dLng = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            lng += dLng;

            polyline.add(new LatLng((lat / 1E5), (lng / 1E5)));
        }
        return polyline;
    }

    private String getApiKey() {
        try {
            ApplicationInfo appInfo = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            return bundle.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        } else {
            Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show();
        }
    }
}
