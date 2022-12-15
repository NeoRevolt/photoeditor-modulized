package com.example.photoeditorlib

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.photoeditor_module.ShowDataLibActivity
import com.example.photoeditorlib.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnToLib.setOnClickListener {
//                Intent(this@MainActivity, Class.forName("com.example.my_library.ShowDataLibActivity")).also {
//                    it.putExtra(ShowDataLibActivity.EXTRA_DETAIL,"INI DATA")
//                    startActivity(it)
//                }
                Intent(this@MainActivity, ShowDataLibActivity::class.java).also {
                    it.putExtra(ShowDataLibActivity.EXTRA_DETAIL,"INI DATA")
                    startActivity(it)
                }
            }
        }
    }
}