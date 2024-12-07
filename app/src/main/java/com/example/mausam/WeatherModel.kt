package com.example.mausam

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ViewModel to handle business logic and expose LiveData to the UI
class WeatherModel : ViewModel() {

    val weatherLiveData = MutableLiveData<WeatherData?>()

    private val repository = WeatherAPI()

    fun fetchCurrentWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val currentWeather = repository.getCurrentWeather(lat, lon)
                weatherLiveData.postValue(currentWeather)
            } catch (e: Exception) {
                e.printStackTrace()
                weatherLiveData.postValue(null) // Handle error gracefully
            }
        }
    }
}

// Repository to handle the data-fetching logic
class WeatherAPI {

    private val weatherService: WeatherService by lazy {
        RetrofitClient.weatherService
    }

    suspend fun getCurrentWeather(lat: Double, lon: Double): WeatherData? {
        val apiKey = "8845440db2bcf6d1611c260f18096429" // Replace with your actual API key
        return weatherService.getWeather(lat, lon, apiKey)
    }
}

// Singleton RetrofitClient to initialize Retrofit
object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    val weatherService: WeatherService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }
}

// Retrofit Service Interface for the Weather API
interface WeatherService {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric" // Use Celsius
    ): WeatherData
}

// Data models to map the OpenWeatherMap API response
data class WeatherData(
    val name: String, // City name
    val main: Main,
    val weather: List<WeatherDescription>
)

data class Main(
    val temp: Double, // Current temperature
    val temp_min: Double, // Minimum temperature
    val temp_max: Double, // Maximum temperature
    val humidity: Int // Humidity percentage
)

data class WeatherDescription(
    val description: String, // Weather condition description
    val icon: String // Icon ID for weather condition
)
