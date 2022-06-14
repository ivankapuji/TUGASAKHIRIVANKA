package com.ivankapuji.lokasikita

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.provider.Settings
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.ivankapuji.lokasikita.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.*


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    //rekam sudut gambar kompas yang diputar
    private var currentDegree = 0f

    //manajer sensor perangkat
    private var mSendorManager: SensorManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mainBinding.btnLocation.setOnClickListener {
            getLocation()
        }
    }


    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    // mendapatkan data lokasi dari perangkat (latitude, longitude)
                    val location: Location? = task.result
                    if (location != null) {
                        // gunakan geocoder utk mendapatkan data lokasi
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)

                        //menampilkan nilai dari geocoder
                        mainBinding.apply {
                            tvLatitude.text = "Latitude\n${list[0].latitude}"
                            tvLongitude.text = "Longitude\n${list[0].longitude}"
                            tvCountryName.text = "Nama Negara\n${list[0].countryName}"
                            tvLocality.text = "Wilayah/Area\n${list[0].locality}"
                            tvAddress.text = "Alamat\n${list[0].getAddressLine(0)}"
                        }
                    }
                }
            }
            else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    //izin mengakses lokasi
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    private fun initData() {
        mSendorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
    }

    override fun onResume() {
        super.onResume()
        @Suppress("DEPRECATION")
        mSendorManager?.registerListener(
            this,
            mSendorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    //perubahan sensor kompas sesuai rotasi
    override fun onSensorChanged(event: SensorEvent?) {
        val degree = Math.round(event?.values?.get(0)!!)
        val rotateAnimation = RotateAnimation(
            currentDegree,
            (-degree).toFloat(),
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotateAnimation.duration = 210
        rotateAnimation.fillAfter = true

        val imgCompass = null
        imgCompass.startAnimation(rotateAnimation)
        currentDegree = (-degree).toFloat()

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }
}

private fun Nothing?.startAnimation(rotateAnimation: RotateAnimation) {
}
