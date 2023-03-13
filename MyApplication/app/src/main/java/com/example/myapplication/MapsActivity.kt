package com.example.myapplication
import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.maps.android.PolyUtil
import org.json.JSONObject


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val rEQUESTLOCATIONPERMISSIOn = 1
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var currentLocation: Location? = null
    private var latitude = 0.0
    private var longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        * Initialize Places. For simplicity, the API key is hard-coded. In a production
        * environment we recommend using a secure mechanism to manage API keys.
        */
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "APIKEY")
        }

        // Create a new Places client instance.
        Places.createClient(this)
        // Initialize the AutocompleteSupportFragment.=

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        enableMyLocation()

        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val gpsLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val locationByGps= location
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
//------------------------------------------------------//
        val networkLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val locationByNetwork= location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//------------------------------------------------------//
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (hasGps) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                0F,
                gpsLocationListener
            )
        }
//------------------------------------------------------//
        if (hasNetwork) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                0F,
                networkLocationListener
            )
        }

//changed
        val lastKnownLocationByGps =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

//------------------------------------------------------//
        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        //through here
//------------------------------------------------------//
        if (lastKnownLocationByGps != null) {
            if (lastKnownLocationByGps.accuracy > lastKnownLocationByGps.accuracy) {
                currentLocation = lastKnownLocationByGps
                latitude = currentLocation!!.latitude
                longitude = currentLocation!!.longitude
                // use latitude and longitude as per your need
            } else {
                currentLocation = lastKnownLocationByGps
                latitude = currentLocation!!.latitude
                longitude = currentLocation!!.longitude
                // use latitude and longitude as per your need
            }
        }
        val latLngOrigin = LatLng(latitude,longitude) // Home
        mMap.addMarker(MarkerOptions().position(latLngOrigin).title("Current Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as? AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment?.setPlaceFields(listOf
            (Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: ${place.name}, ${place.id}")
               // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngOrigin))
                val httpkey = "&key=AIzaSyBL3iPwBeAah0OKhA5M7-v50n00fXDgfhk"
                val http1sthalf = "https://maps.googleapis.com/maps/api/directions/json?origin="
                val http2ndhalf = "&destination="
                val path: MutableList<List<LatLng>> = ArrayList()
                val urlDirections = "$http1sthalf$latitude,$longitude$http2ndhalf${place.latLng?.latitude},${place.latLng?.longitude}$httpkey"
                val directionsRequest = object : StringRequest(Method.GET, urlDirections, Response.Listener {
                        response ->
                    val jsonResponse = JSONObject(response)
                    // Get routes
                    val routes = jsonResponse.getJSONArray("routes")
                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                    val steps = legs.getJSONObject(0).getJSONArray("steps")
                    for (i in 0 until steps.length()) {
                        val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                        path.add(PolyUtil.decode(points))
                    }
                    for (i in 0 until path.size) {
                        mMap.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
                    }
                }, Response.ErrorListener {
                }){}
                val requestQueue = Volley.newRequestQueue(this@MapsActivity)
                requestQueue.add(directionsRequest)
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })
    }
    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mMap.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                rEQUESTLOCATIONPERMISSIOn
            )
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == rEQUESTLOCATIONPERMISSIOn) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

}