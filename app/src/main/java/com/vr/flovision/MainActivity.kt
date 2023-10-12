package com.vr.flovision

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {
    lateinit var btnAdmin : ImageView
    lateinit var btnCam : ImageView
    lateinit var btnScan : LinearLayout
    lateinit var btnTanamanBottom : LinearLayout
    lateinit var btnTanaman : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}