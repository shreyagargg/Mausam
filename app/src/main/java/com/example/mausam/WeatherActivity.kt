package com.example.mausam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class WeatherActivity : AppCompatActivity() {

    private val weatherModel: WeatherModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Request location permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLocation()
            } else {
                findViewById<TextView>(R.id.weather).text = "Permission Denied"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        // Initialize fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Bind views
        val weatherText = findViewById<TextView>(R.id.weather)
        val cityText = findViewById<TextView>(R.id.city)
        val tempText = findViewById<TextView>(R.id.temp)
//        val minTempText = findViewById<TextView>(R.id.min)
//        val maxTempText = findViewById<TextView>(R.id.max)

        // Observe weather data
        weatherModel.weatherLiveData.observe(this, Observer { weather ->
            if (weather != null) {
                Log.d("WeatherAPI", "API Response: ${weather.main.temp_min} - ${weather.main.temp_max}")
                weatherText.text = weather.weather[0].description.capitalize()
                cityText.text = weather.name
                tempText.text = "Temp: ${weather.main.temp}°C"
//                minTempText.text = "Min: ${weather.main.temp_min}°C"
//                maxTempText.text = "Max: ${weather.main.temp_max}°C"
            } else {
                weatherText.text = "Error fetching weather data"
            }
        })
    }

    private fun getLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("Location", "Lat: ${location.latitude}, Lon: ${location.longitude}")
                    weatherModel.fetchCurrentWeather(location.latitude, location.longitude)
                } else {
                    Log.e("Location", "Location is null")
                    findViewById<TextView>(R.id.weather).text = "Location not found. Enable GPS."
                }
            }.addOnFailureListener { e ->
                Log.e("Location", "Failed to fetch location", e)
                findViewById<TextView>(R.id.weather).text = "Error fetching location"
            }
        } catch (e: SecurityException) {
            Log.e("WeatherActivity", "Permission denied for location access", e)
            findViewById<TextView>(R.id.weather).text = "Location permission required"
        }
    }
}
