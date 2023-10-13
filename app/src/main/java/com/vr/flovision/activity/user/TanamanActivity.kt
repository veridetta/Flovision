package com.vr.flovision.activity.user

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.flovision.MainActivity
import com.vr.flovision.R
import com.vr.flovision.activity.adapter.HistoryAdapter
import com.vr.flovision.activity.adapter.PlantAdapter
import com.vr.flovision.activity.admin.LoginActivity
import com.vr.flovision.helper.showSnack
import com.vr.flovision.model.PlantModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class TanamanActivity : AppCompatActivity() {
    lateinit var btnCam : ImageView
    lateinit var btnGallery : LinearLayout
    lateinit var  btnCamera : LinearLayout
    lateinit var lyAksi : RelativeLayout
    lateinit var tvCancel : TextView
    lateinit var tvJumlah : TextView
    lateinit var btnHome : LinearLayout
    private val GALLERY_REQUEST_CODE = 1
    private val CAMERA_REQUEST_CODE = 2
    var izin = false
    private lateinit var plantAdapter: HistoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressDialog: ProgressDialog
    val TAG = "LOAD DATA"
    private val plantList: MutableList<PlantModel> = mutableListOf()
    val mFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tanaman)
        initView()
        initRc()
        initData()
        initClick()
    }
    private fun initView(){
        btnCam = findViewById(R.id.btnCam)
        btnHome = findViewById(R.id.btnHome)
        lyAksi = findViewById(R.id.lyAksi)
        btnGallery = findViewById(R.id.btnGallery)
        btnCamera = findViewById(R.id.btnCamera)
        tvJumlah = findViewById(R.id.tvJumlah)
        tvCancel = findViewById(R.id.tvCancel)
        recyclerView = findViewById(R.id.rcPlants)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
    }

    private fun initClick(){
        btnCam.setOnClickListener {
            // Periksa izin kamera dan penyimpanan eksternal
            if (checkPermissions()) {
                // Izin diberikan, tampilkan aksi untuk memilih gambar
                lyAksi.visibility = View.VISIBLE
            } else {
                // Meminta izin
                requestPermissions()
            }
        }
        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        tvCancel.setOnClickListener {
            lyAksi.visibility = View.GONE
        }
        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
            lyAksi.visibility = View.GONE
        }

        btnCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
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
                showSnack(this@TanamanActivity, "Izin ditolak")
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    val selectedImage = data?.data
                    //Glide.with(this).load(selectedImage).into(imageView)

                    val imageFilePath = getImageFilePath(selectedImage) // Mendapatkan jalur file gambar dari URI

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

                    // Simpan gambar dari kamera ke penyimpanan
                    val imageFilePath = saveImageFromBitmap(imageBitmap)

                    if (imageFilePath != null) {
                        val intent = Intent(this, ResultActivity::class.java)
                        intent.putExtra("imageFilePath", imageFilePath)
                        startActivity(intent)
                    } else {
                        println("Failed to save the image.")
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
    private fun saveImageFromBitmap(bitmap: Bitmap): String? {
        val imageFileName = "image_${System.currentTimeMillis()}.jpg"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(storageDir, imageFileName)

        return try {
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            imageFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    private fun initRc(){
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@TanamanActivity, 1)
            // set the custom adapter to the RecyclerView
            plantAdapter = HistoryAdapter(
                plantList,
                this@TanamanActivity,
                { barang -> clickCard(barang) },
            )
        }
    }
    private fun initData(){
        readData()
        recyclerView.adapter = plantAdapter
    }
    private fun readData() {
        //get uid dari shared preference
        val sharedPref = getSharedPreferences("user", MODE_PRIVATE)
        val uid = sharedPref.getString("uid", "")
        Log.d(TAG, "UID : $uid")
        if (uid != "") {
            progressDialog.show()
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val result = mFirestore.collection("history").get().await()
                    val plants = mutableListOf<PlantModel>()
                    var jum = 0
                    for (document in result) {
                        val plant = document.toObject(PlantModel::class.java)
                        val docId = document.id
                        plant.docId = docId
                        plants.add(plant)
                        Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                        jum++
                    }

                    withContext(Dispatchers.Main) {
                        plantList.addAll(plants)
                        plantAdapter.filteredBarangList.addAll(plants)
                        plantAdapter.notifyDataSetChanged()
                        progressDialog.dismiss()
                        tvJumlah.text = "Jumlah ($jum)"
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.w(TAG, "Error getting documents : $e")
                        progressDialog.dismiss()
                    }
                }
            }
        } else {
            tvJumlah.text = "Tidak ada riwayat"
            showSnack(this@TanamanActivity, "Silahkan scan terlebih dahulu")
        }
    }

    private fun clickCard(plant: PlantModel) {
        //intent ke homeActivity fragment add
        val intent = Intent(this, ResultHistoryActivity::class.java)
        intent.putExtra("docId", plant.docId)
        startActivity(intent)
    }
    companion object {
        private const val PERMISSION_REQUEST_CODE = 3
    }
}