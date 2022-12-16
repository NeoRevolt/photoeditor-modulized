package com.example.photoeditorlib

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.photoeditor_module.EditImageActivity
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
                Intent(this@MainActivity, Class.forName("com.example.photoeditor_module.EditImageActivity")).also {
                    it.putExtra(EditImageActivity.EXTRA_REQ,"gallery")
                    it.putExtra(EditImageActivity.EXTRA_PHOTO,"https://story-api.dicoding.dev/images/stories/photos-1671171861104_xky0h4lW.jpg")
                    startActivity(it)
                }
//                Intent(this@MainActivity, ShowDataLibActivity::class.java).also {
//                    it.putExtra(ShowDataLibActivity.EXTRA_DETAIL,"INI DATA")
//                    startActivity(it)
//                }
            }
        }
    }
}