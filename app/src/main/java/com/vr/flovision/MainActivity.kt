package com.vr.flovision

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.vr.flovision.activity.admin.LoginActivity
import com.vr.flovision.activity.user.ResultActivity
import com.vr.flovision.activity.user.TanamanActivity
import com.vr.flovision.helper.showSnack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var btnAdmin : ImageView
    lateinit var btnCam : ImageView
    lateinit var btnScan : LinearLayout
    lateinit var btnGallery : LinearLayout
    lateinit var  btnCamera : LinearLayout
    lateinit var  tvCancel : TextView
    lateinit var lyAksi : RelativeLayout
    lateinit var btnTanamanBottom : LinearLayout
    lateinit var btnTanaman : LinearLayout
    private val GALLERY_REQUEST_CODE = 1
    private val CAMERA_REQUEST_CODE = 2
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf<String>(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    var izin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Firebase.initialize(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )
        initView()
        initClick()
    }
    private fun initView(){
        btnAdmin = findViewById(R.id.btnAdmin)
        btnCam = findViewById(R.id.btnCam)
        btnScan = findViewById(R.id.btnScan)
        btnTanamanBottom = findViewById(R.id.btnTanamanBottom)
        tvCancel = findViewById(R.id.tvCancel)
        btnTanaman = findViewById(R.id.btnTanaman)
        lyAksi = findViewById(R.id.lyAksi)
        btnGallery = findViewById(R.id.btnGallery)
        btnCamera = findViewById(R.id.btnCamera)
    }

    private fun initClick(){
        btnAdmin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        btnCam.setOnClickListener {
            verifyStoragePermissions(this)
            // Periksa izin kamera dan penyimpanan eksternal
            if (checkPermissions()) {
                // Izin diberikan, tampilkan aksi untuk memilih gambar
                lyAksi.visibility = View.VISIBLE
            } else {
                // Meminta izin
                requestPermissions()
            }
        }
        tvCancel.setOnClickListener {
            lyAksi.visibility = View.GONE
        }
        btnScan.setOnClickListener {
            verifyStoragePermissions(this)
            if (checkPermissions()) {
                // Izin diberikan, tampilkan aksi untuk memilih gambar
                lyAksi.visibility = View.VISIBLE
            } else {
                // Meminta izin
                requestPermissions()
            }
        }
        btnTanamanBottom.setOnClickListener {
            startActivity(Intent(this, TanamanActivity::class.java))
        }
        btnTanaman.setOnClickListener {
            startActivity(Intent(this, TanamanActivity::class.java))
        }
        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
            lyAksi.visibility = View.GONE
        }

        btnCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            //gambar dari kamera bukan video kualitas HD
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
            lyAksi.visibility = View.GONE
        }
    }
    private fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val storagePermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return cameraPermission && storagePermission
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            PERMISSION_REQUEST_CODE
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Izin diberikan, tampilkan aksi untuk memilih gambar
                izin = true
            } else {
                // Izin ditolak, Anda dapat memberikan pesan atau tindakan lain
                showSnack(this@MainActivity, "Izin ditolak")
            }
        }
    }
    fun verifyStoragePermissions(activity: Activity?) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(
            activity!!,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    val selectedImage = data?.data
                    //Glide.with(this).load(selectedImage).into(imageView)
                    Log.d("selectedImage", selectedImage.toString())
                    val imageFilePath = getImageFilePath(selectedImage) // Mendapatkan jalur file gambar dari URI
                    Log.d("imageFilePath", imageFilePath.toString())
                    if (imageFilePath != null) {
                        val intent = Intent(this, ResultActivity::class.java)
                        intent.putExtra("imageFilePath", imageFilePath)
                        startActivity(intent)
                    } else {
                        println("Failed to get image file path.")
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    GlobalScope.launch(Dispatchers.IO) {
                        val imageFilePath = saveImageFromBitmap(imageBitmap)
                        // Setelah operasi penyimpanan selesai, lanjutkan dengan operasi selanjutnya di UI thread
                        withContext(Dispatchers.Main) {
                            if (imageFilePath != "") {
                                val intent = Intent(this@MainActivity, ResultActivity::class.java)
                                intent.putExtra("imageFilePath", imageFilePath)
                                intent.putExtra("isCamera", false)
                                Log.d("imageFilePath", imageFilePath)
                                startActivity(intent)
                            } else {
                                println("Failed to save the image.")
                            }
                        }
                    }
                }
            }
        }
    }

    // Fungsi untuk mendapatkan jalur berkas dari URI
    private fun getImageFilePath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val filePath = columnIndex?.let { cursor?.getString(it) }
        cursor?.close()
        return filePath
    }

    // Fungsi untuk menyimpan gambar dari kamera ke penyimpanan
    private fun saveImageFromBitmap(bitmap: Bitmap): String {
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())

        // Direktori DCIM/Pictures
        val dcimDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val picturesDirectory = File(dcimDirectory, "Screenshots")

        // Membuat direktori jika belum ada
        if (!picturesDirectory.exists()) {
            picturesDirectory.mkdirs()
        }

        // Membuat file gambar
        val photoFile = File(
            picturesDirectory,
            "IMG_$timeStamp.jpg"
        )

        lateinit var ret : String

        try {
            val fos = photoFile.outputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()

            // Menambahkan gambar ke galeri
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(photoFile)
            sendBroadcast(mediaScanIntent)

            showSnack(this, "Gambar berhasil disimpan di ${photoFile}")
            Log.d("Gambar berhasil disimpan di ${photoFile}", "Gambar berhasil disimpan di ${photoFile}")
            ret = photoFile.absolutePath

        } catch (e: IOException) {
            e.printStackTrace()
            ret = ""
        }

        return ret
    }
    companion object {
        private const val PERMISSION_REQUEST_CODE = 3
    }
}