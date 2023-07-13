package com.example.weatherapplication.weather.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.android.volley.ClientError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.ServerError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapplication.R
import com.example.weatherapplication.databinding.ActivityMainBinding
import com.example.weatherapplication.weather.utils.TOKEN
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)


        binding.apply {
            btnSearch.setOnClickListener {
                val location=etlocation.text.toString().trim()
                if (location.isNotEmpty()){

                    showerror(false)

                    requestToServer(location)

                }else{
                    showerror(true,"Enter Location")
                }
            }
        }
    }

    private fun requestToServer(location:String){
        //volley
        val queue=Volley.newRequestQueue(this)
        val url="https://api.openweathermap.org/data/2.5/weather?q=$location&units=metric&appid=$TOKEN"
        val stringRequest= StringRequest(Request.Method.GET,url,{response->
          try {
              val jsonresponse=JSONObject(response)
              val mainObject:JSONObject=jsonresponse.getJSONObject("main")
              val temperature=mainObject.getDouble("temp")
              val humidity=mainObject.getInt("humidity")
              val windObject:JSONObject=jsonresponse.getJSONObject("wind")
              val windspeed=windObject.getDouble("speed")
              val weatherArray: JSONArray =jsonresponse.getJSONArray("weather")
              var description:String?=null
              var main:String?=null
              if (weatherArray.length() > 0)
              {
                  val weatherObject = weatherArray.getJSONObject(0)
                  description = weatherObject.getString("description")
                  main = weatherObject.getString("main")
              }

              binding.layoutWeather.apply {
                  root.visibility = View.VISIBLE
                  //setData
                  locationTitle.text = location
                  val roundedValue = ceil(temperature).toInt().toString()
                  tvTemperature.text = roundedValue

                  tvWindSpeed.text = "$windspeed m/s"
                  tvHumidityValue.text = "$humidity%"
                  tvDescription.text = description.toString()

                  when(main)
                  {
                      "Clear"->{
                          ivStatus.setImageResource(R.drawable.clear)
                      }
                      "Rain"->{
                          ivStatus.setImageResource(R.drawable.rain)
                      }
                      "Snow"->{
                          ivStatus.setImageResource(R.drawable.snow)
                      }
                      "Clouds"->{
                          ivStatus.setImageResource(R.drawable.cloud)
                      }
                  }

                  //click Listener
                  ivBack.setOnClickListener {
                      root.visibility = View.GONE
                  }

              }


          }catch (e:JSONException){
              Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
          }
        },{
            if(it is ClientError||it is ServerError){
                val response:NetworkResponse=it.networkResponse
                val statuscode=response.statusCode
                if(statuscode==404) showerror(true,"Location NotFound")
                else Toast.makeText(this,"error in connect to api",Toast.LENGTH_SHORT).show()
            }
        })

        queue.add(stringRequest)
    }


    private fun showerror(haveError:Boolean,message:String?=null){
        binding.tverror.apply {
            if(haveError){
                text=message
                visibility= View.VISIBLE
            }else{
                visibility=View.GONE
            }
        }
    }
}