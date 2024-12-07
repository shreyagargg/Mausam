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
                // Handle the case where permission is denied
                findViewById<TextView>(R.id.weather).text = "Permission Denied"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        // Initialize fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLocation() // Permission granted, fetch location
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Bind views
        val weatherText = findViewById<TextView>(R.id.weather)
        val city = findViewById<TextView>(R.id.city)
        val temp = findViewById<TextView>(R.id.temp)
//        val minTempText = findViewById<TextView>(R.id.min)
//        val maxTempText = findViewById<TextView>(R.id.max)

        // Observe weather data
        weatherModel.weatherLiveData.observe(this, Observer { weather ->
            if (weather != null) {
                Log.d("WeatherAPI", "API Response: ${weather.main.temp_min} - ${weather.main.temp_max}")

                // Update UI with weather data
                weatherText.text = weather.weather[0].description.capitalize()
                city.text = weather.name // City name
                temp.text = "Temperature: ${weather.main.temp}°C" // Current temperature
//                minTempText.text = "Min: ${weather.main.temp_min}°C" // Min temperature
//                maxTempText.text = "Max: ${weather.main.temp_max}°C" // Max temperature
            } else {
                // If weather data is null
                weatherText.text = "Error fetching weather data"
            }
        })
    }

    // Fetch location after permission is granted
    private fun getLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Fetch weather for the current location
                    weatherModel.fetchCurrentWeather(location.latitude, location.longitude)
                } else {
                    findViewById<TextView>(R.id.weather).text = "Location not found"
                }
            }
        } catch (e: SecurityException) {
            // Handle SecurityException if permissions are denied or location is not available
            Log.e("WeatherActivity", "Location permission not granted", e)
            findViewById<TextView>(R.id.weather).text = "Location permission required"
        }
    }
}
