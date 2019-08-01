package com.bjorn.vanctrl

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.Navigation

class MainActivity : AppCompatActivity() {

//    private lateinit var textMessage: TextView
    private lateinit var nav_ctrl: NavController

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_battery -> {

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_light -> {
//                changeLights()
//                textMessage.setText(R.string.title_light)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_fridge -> {
//                changeFridge()
//                textMessage.setText(R.string.title_fridge)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_radio -> {
//                textMessage.setText(R.string.title_radio)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

//        textMessage = findViewById(R.id.message)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        nav_ctrl = Navigation.findNavController(this, R.id.nav_host_fragment)//.navigate(R.id.lightSwitch)
    }

//    fun changeLights() {
////        val editText = findViewById<EditText>(R.id.editText)
//        val intent = Intent(this, ChangeLightsActivity::class.java)
////        intent.putExtra(STUFF, value)
//        startActivity(intent)
//    }
//
//    fun changeFridge() {
////        val editText = findViewById<EditText>(R.id.editText)
////        val message = R.string.title_light
//        val intent = Intent(this, MainActivity::class.java).apply {
////            putExtra(EXTRA_MESSAGE, message)
//        }
//        startActivity(intent)
//    }

}
