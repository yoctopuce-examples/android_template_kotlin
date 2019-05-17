package com.example.myapplication

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import android.os.Handler
import com.yoctopuce.YoctoAPI.YAPI
import com.yoctopuce.YoctoAPI.YAPI_Exception
import android.widget.TextView
import com.yoctopuce.YoctoAPI.YSensor
import com.yoctopuce.YoctoAPI.YTemperature
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var temperatureTextView: TextView
    lateinit var handler: Handler
    var hardwaredetect = 0
    var sensor: YSensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        temperatureTextView = findViewById(R.id.temperature)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        handler = Handler()
    }


    override fun onStart() {
        super.onStart()
        try {
            YAPI.EnableUSBHost(this)
            YAPI.RegisterHub("usb")
        } catch (e: YAPI_Exception) {
            Snackbar.make(temperatureTextView, "Error:" + e.localizedMessage, Snackbar.LENGTH_INDEFINITE).show()
        }
        handler.postDelayed(_periodicUpdate, 500)
    }


    override fun onStop() {
        handler.removeCallbacks(_periodicUpdate)
        YAPI.FreeAPI()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val _periodicUpdate = object : Runnable {
        override fun run() {
            try {
                if (hardwaredetect == 0) {
                    YAPI.UpdateDeviceList()
                }
                hardwaredetect = (hardwaredetect + 1) % 20
                if (sensor == null) {
                    sensor = YTemperature.FirstTemperature()
                }
                sensor?.let {
                    if (it.isOnline) {
                        val text = String.format(Locale.US, "%.2f %s", it._currentValue, it._unit)
                        temperatureTextView.text = text
                    } else {
                        temperatureTextView.text = "OFFLINE"
                        sensor = null
                    }

                }
            } catch (e: YAPI_Exception) {
                Snackbar.make(temperatureTextView, "Error:" + e.localizedMessage, Snackbar.LENGTH_INDEFINITE).show()
            }

            handler.postDelayed(this, 500)
        }
    }

}
