package com.fa.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.fa.weatherapp.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import org.json.JSONObject
import java.net.URL
import java.util.*
import java.util.jar.Manifest
import kotlin.reflect.typeOf

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var CITY: String = "Mexico City, MX"
    val API: String = "45541d49d6c7728b79bb57cc6b29979f"
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    var BAND = 'C'
    var LAT = ""
    var LON = ""
    val PERMISSION_ID = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        weather().execute()
    }

    inner class weather() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            binding.loader.visibility = View.VISIBLE
            binding.mainContainer.visibility = View.GONE
            binding.errorText.visibility = View.GONE
        }

        override fun doInBackground(vararg p0: String?): String? {
            var response: String?
            try {
                if (BAND == 'C') {
                    response =
                        URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&lang=es&appid=$API").readText(
                            Charsets.UTF_8
                        )
                    println(BAND)
                    println("Entro en C")
                } else if (BAND == 'L') {
                    response =
                        URL("https://api.openweathermap.org/data/2.5/weather?lat=$LAT&lon=$LON&units=metric&lang=es&appid=$API").readText(
                            Charsets.UTF_8
                        )
                } else {
                    response = null
                }
            } catch (e: Exception) {
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val cloud = jsonObj.getJSONObject("clouds")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                val updateAt: Long = jsonObj.getLong("dt")
                val updateAtText = "Actualizado a las: " + SimpleDateFormat(
                    "dd/MM/yyyy hh:mm a",
                ).format(Date(updateAt))
                val temp = main.getString("temp") + "ºC"
                val feelsL = main.getString("feels_like") + "ºC"
                val tempMin = "Min Temp " + main.getString("temp_min") + "ºC"
                val tempMax = "Max Temp " + main.getString("temp_max") + "ºC"
                val pressure = "Presión atmosferica: " + main.getString("pressure") + "hPa"
                val humidity = "Humedad: " + main.getString("humidity") + "%"
                val sunrise: Long = sys.getLong("sunrise")
                val sunset: Long = sys.getLong("sunset")
                val windSpeed = "Velocidad viento: " + wind.getString("speed") + "m/s"
                val windDeg = "Dirección viento: " + wind.getString("deg") + "m/s"
                val clouding = "Nubosidad: " + cloud.getString("all") + "%"
                val weatherDescription = weather.getString("description")
//                val icon = weather.
                val address = jsonObj.getString("name") + ", " + sys.getString("country")

                binding.address.text = address
                binding.updateAt.text = updateAtText
                binding.status.text = weatherDescription.capitalize()
                binding.temp.text = temp
                binding.feels.text = feelsL
                binding.tempMin.text = tempMin
                binding.tempMax.text = tempMax
//                findViewById<TextView>(R.id.sunrise).text =
//                    SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
//                findViewById<TextView>(R.id.sunset).text =
//                    SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
                binding.windS.text = windSpeed
                binding.pressure.text = pressure
                binding.humidity.text = humidity
                binding.windD.text = windDeg
                binding.cloud.text = clouding
                Toast.makeText(applicationContext, "Bandera " + BAND, Toast.LENGTH_SHORT).show()
                BAND = 'C'

//                binding.imageWeather.imageAlpha =

                binding.loader.visibility = View.GONE
                binding.mainContainer.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.loader.visibility = View.GONE
                binding.errorText.visibility = View.VISIBLE
            }
        }
    }

    fun expand(view: View) {
        if (binding.layoutDetails.visibility == View.GONE) {
            TransitionManager.beginDelayedTransition(binding.cardView, AutoTransition())
            binding.layoutDetails.visibility = View.VISIBLE
        } else {
            TransitionManager.beginDelayedTransition(binding.cardView, AutoTransition())
            binding.layoutDetails.visibility = View.GONE
        }
    }

    fun update(view: View) {
        val ocity = binding.cityText.text
        if (ocity == null) {
            CITY = "Mexico City, MX"
            Toast.makeText(applicationContext, "Escriba una ciudad", Toast.LENGTH_LONG).show()
        } else {
            CITY = ocity.toString()
        }
        Toast.makeText(applicationContext, "Es la ciudad " + CITY, Toast.LENGTH_LONG).show()
        BAND = 'C'
        weather().execute()
    }

    fun location(view: View) {
        //Preguntar si se tiene permisos
        if (allPermissionsGrantedGPS()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        } else {
            // Si no hay permisos solicitarlos al usuario
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_ID
            )
        }
        leerubicacionactual()
        weather().execute()
    }

    private fun allPermissionsGrantedGPS() = REQUIRED_PERMISSIONS_GPS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun leerubicacionactual() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                        var location: Location? = task.result
                        if (location == null) {
                            requestNewLocationData()
                        } else {
                            LAT = location.latitude.toString()
                            LON = location.longitude.toString()
                            BAND = 'L'
                            Toast.makeText(
                                this,
                                "-Latitud: " + LAT + " -Longitud: " + LON + " Bandera " + BAND,
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }
                }
            } else {
                Toast.makeText(this, "Activar ubicación", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                this.finish()
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_ID
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallBack,
            Looper.myLooper()
        )
    }

    private val mLocationCallBack = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            LAT = mLastLocation.latitude.toString()
            LON = mLastLocation.longitude.toString()
            BAND = 'L'
            Toast.makeText(
                applicationContext, "Latitud: " + LAT + " Longitud: " + LON, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    companion object {
        private val REQUIRED_PERMISSIONS_GPS = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}