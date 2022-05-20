package com.example.step_counter.ui.main

import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.step_counter.R
import com.example.step_counter.databinding.MainFragmentBinding
import com.google.gson.JsonElement
import kotlinx.android.synthetic.main.main_fragment.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody.Part.Companion.createFormData
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import java.io.File
import java.util.*


class MainFragment : Fragment(), SensorEventListener {


    private var sensorManager: SensorManager? = null
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private var pauseOffset = 0
    private lateinit var xAccVector: Vector<Float>
    private lateinit var yAccVector: Vector<Float>
    private lateinit var zAccVector: Vector<Float>
    private lateinit var timeVector: Vector<Long>
    private lateinit var djangoServer: String


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding: MainFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.main_fragment,
            container,
            false
        )
        xAccVector = Vector<Float>(2000, 100)
        yAccVector = Vector<Float>(2000, 100)
        zAccVector = Vector<Float>(2000, 100)
        timeVector = Vector<Long>(2000, 100)

        sensorManager = activity?.getSystemService(SENSOR_SERVICE) as SensorManager
        binding.accNumber.text = 0.toString()

        djangoServer = if(binding.ipTextInput.text.toString() != "IP address..."){
            binding.ipTextInput.text.toString()
        } else "192.168.1.25"


        binding.startButton.setOnClickListener {
            xAccVector.clear()
            yAccVector.clear()
            zAccVector.clear()

            binding.timer.base = SystemClock.elapsedRealtime() - pauseOffset
            binding.timer.start()
            val accSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if(accSensor === null){
                Toast.makeText(activity, "No sensor detected", Toast.LENGTH_SHORT).show()
            }else{
                sensorManager?.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME)
            }

        }

        binding.resetButton.setOnClickListener {

            val file =
                File(context?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "acc_data.csv")

            binding.timer.stop()
            pauseOffset = 0
            previousTotalSteps = totalSteps
            binding.accNumber.text = 0.toString()
            sensorManager!!.unregisterListener(this)

            var djangoSite = "http://$djangoServer:2137/"

            file.printWriter().use { out ->
                out.println("x;y;z;time")
                for (i in 0 until xAccVector.size) {
                    out.println(
                        String.format(
                            "%.2f; %.2f; %.2f; %d",
                            xAccVector[i],
                            yAccVector[i],
                            zAccVector[i],
                            timeVector[i]
                        )
                    )
                }
            }
            uploadFile(file, djangoSite)
        }

        return binding.root
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            xAccVector.add(x)
            yAccVector.add(y)
            zAccVector.add(z)
            timeVector.add(SystemClock.elapsedRealtime())

            acc_number.text = String.format("%.2f %.2f %.2f", x, y, z)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    private fun uploadFile(file: File, djangoSite: String) {

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(djangoSite)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val postApi: FileUploadService = retrofit.create(FileUploadService::class.java)

        val requestBody: RequestBody = file.asRequestBody("text/csv".toMediaTypeOrNull())
        val multiPartBody: MultipartBody.Part =
            createFormData("acc_data", file.name, requestBody)
        val call: Call<RequestBody> = postApi.upload(multiPartBody)

        call.enqueue(object : Callback<RequestBody> {
            override fun onResponse(call: Call<RequestBody?>?, response: retrofit2.Response<RequestBody?>?) {
                Toast.makeText(activity, "Success", Toast.LENGTH_LONG).show()
                Log.d("good", "good")
            }

            override fun onFailure(call: Call<RequestBody?>?, t: Throwable?) {
                Toast.makeText(activity, "Failed: " + t.toString(), Toast.LENGTH_LONG).show()
                Log.d("fail", t.toString())
            }
        })

    }


}