package com.fa.weatherapp

import android.icu.text.SimpleDateFormat
import android.os.AsyncTask
import android.os.Bundle
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
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        weather().execute()


    }

    fun update(view: View) {
        val ocity=binding.cityText.text
        if (ocity == null) {
            CITY = "Mexico City, MX"
            Toast.makeText(applicationContext,"Escriba una ciudad",Toast.LENGTH_LONG).show()
        } else {
            CITY = ocity.toString()
        }

//println(ocity)
//        Toast.makeText(applicationContext,"Es la ciudad "+ocity,Toast.LENGTH_LONG).show()
        Toast.makeText(applicationContext,"Es la ciudad "+CITY,Toast.LENGTH_LONG).show()
        weather().execute()
    }

    inner class weather() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        override fun doInBackground(vararg p0: String?): String? {
            var response: String?
            try {

                response =
                    URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API").readText(
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
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                val updateAt: Long = jsonObj.getLong("dt")
                val updateAtText = "Actualizado a las: " + SimpleDateFormat(
                    "dd/MM/yyyy hh:mm a",
                    Locale.ENGLISH
                ).format(Date(updateAt))
                val temp = main.getString("temp") + "ºC"
                val tempMin = "Min Temp" + main.getString("temp_min") + "ºC"
                val tempMax = "Max Temp" + main.getString("temp_max") + "ºC"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")
                val sunrise: Long = sys.getLong("sunrise")
                val sunset: Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")
                val address = jsonObj.getString("name") + ", " + sys.getString("country")

                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.update_at).text = updateAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.sunrise).text =
                    SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
                findViewById<TextView>(R.id.sunset).text =
                    SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity

                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
            } catch (e: Exception) {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }
        }
    }
}