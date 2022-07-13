package com.fa.weatherapp

import android.icu.text.SimpleDateFormat
import android.os.AsyncTask
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.fa.weatherapp.databinding.ActivityMainBinding
import org.json.JSONObject
import java.net.URL
import java.util.*
import kotlin.reflect.typeOf

class MainActivity : AppCompatActivity() {

    var CITY: String = "Mexico City, MX"
    val API: String = "45541d49d6c7728b79bb57cc6b29979f"
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        weather().execute()
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
                response =
                    URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&lang=es&appid=$API").readText(
                        Charsets.UTF_8
                    )
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
//                binding.imageWeather.imageAlpha =

                binding.loader.visibility = View.GONE
                binding.mainContainer.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.loader.visibility = View.GONE
                binding.errorText.visibility = View.VISIBLE
            }
        }
    }
}