package com.example.botnavmenu

import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColor
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.botnavmenu.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//      setContentView(R.layout.activity_main)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadFragment(HomeFragment(), "homepages")

        bottomNav = binding.botmenu
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.Homeid -> {
                    loadFragment(HomeFragment(), "homepages")
                    true
                }

                R.id.Productid -> {
                    loadFragment(ProductFragment(), "productpages")
                    true
                }

                R.id.Basketid -> {
                    loadFragment(BasketFragment(), "Basketpages")
                    true

                }
            }
            true
        }
    }

    private  fun loadFragment(fragment: Fragment, tag:String){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container,fragment, tag)
        transaction.addToBackStack(tag)
        transaction.commit()
    }
}