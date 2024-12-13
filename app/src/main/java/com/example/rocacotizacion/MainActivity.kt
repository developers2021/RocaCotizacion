package com.example.rocacotizacion
import android.os.Bundle
import androidx.navigation.ui.AppBarConfiguration
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.rocacotizacion.databinding.ActivityMainBinding
import com.example.rocacotizacion.ui.home.HomeFragment
import androidx.appcompat.app.AppCompatDelegate


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryVariant)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


    }
    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_home)
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)

        if (currentFragment is HomeFragment) {
            // Ignore the back press when on the home fragment
        } else {
            super.onBackPressed()
        }
    }
}