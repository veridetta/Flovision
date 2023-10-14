package com.vr.flovision.activity.admin

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.vr.flovision.R
import com.vr.flovision.helper.ImageUtils
import com.vr.flovision.helper.showSnack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class AddActivity : AppCompatActivity() {
    private val REQUEST_CODE_COVER = 1
    lateinit var btnBack:ImageView
    lateinit var btnSimpan:Button
    lateinit var btnCover:LinearLayout
    lateinit var coverReplace:ImageView
    lateinit var contentView: RelativeLayout
    lateinit var etPlantNetName: EditText
    lateinit var etLatin: EditText
    lateinit var etNama: EditText
    lateinit var etKerajaan: EditText
    lateinit var etFamili: EditText
    lateinit var etOrdo: EditText
    lateinit var etSpesies: EditText
    lateinit var etManfaat: EditText
    lateinit var tvJudul: TextView

    var type=""
    var plantNetName = ""
    var latin  = ""
    var nama = ""
    var gambar = ""
    var kerajaan = ""
    var famili = ""
    var ordo = ""
    var spesies = ""
    var manfaat = ""
    var docId = ""
    var ubahGambar = false
    lateinit var imageUr : Uri

    lateinit var progressDialog:ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        initView()
        initIntent()
        initClick()


    }
    private fun initView(){
        btnBack = findViewById(R.id.btnBack)
        btnSimpan = findViewById(R.id.btnSimpan)
        etPlantNetName = findViewById(R.id.etPlantNetName)
        etLatin = findViewById(R.id.etLatin)
        etNama = findViewById(R.id.etNama)
        etKerajaan = findViewById(R.id.etKerajaan)
        etFamili = findViewById(R.id.etFamili)
        etOrdo = findViewById(R.id.etOrdo)
        etSpesies = findViewById(R.id.etSpesies)
        etManfaat = findViewById(R.id.etManfaat)
        btnCover = findViewById(R.id.btnCover)
        tvJudul =  findViewById(R.id.tvJudul)
        coverReplace = findViewById(R.id.coverReplace)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
    }
    private fun initIntent(){
        type = intent.getStringExtra("type").toString()
        if(type=="edit"){
            tvJudul.text = "Edit Data"
            plantNetName = intent.getStringExtra("plantNetName").toString()
            latin = intent.getStringExtra("latin").toString()
            nama = intent.getStringExtra("nama").toString()
            kerajaan = intent.getStringExtra("kerajaan").toString()
            famili = intent.getStringExtra("famili").toString()
            ordo = intent.getStringExtra("ordo").toString()
            spesies = intent.getStringExtra("spesies").toString()
            manfaat = intent.getStringExtra("manfaat").toString()
            docId = intent.getStringExtra("docId").toString()
            gambar = intent.getStringExtra("gambar").toString()
            etPlantNetName.setText(plantNetName)
            etLatin.setText(latin)
            etNama.setText(nama)
            etKerajaan.setText(kerajaan)
            etFamili.setText(famili)
            etOrdo.setText(ordo)
            etSpesies.setText(spesies)
            etManfaat.setText(manfaat)
            Glide.with(this).load(gambar).into(coverReplace)
        }
    }
    private fun initClick(){
        btnCover.setOnClickListener {
            // Buka galeri untuk memilih foto sampul
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_COVER)
        }
        btnSimpan.setOnClickListener {
            plantNetName = etPlantNetName.text.toString()
            latin = etLatin.text.toString()
            nama = etNama.text.toString()
            kerajaan = etKerajaan.text.toString()
            famili = etFamili.text.toString()
            ordo = etOrdo.text.toString()
            spesies = etSpesies.text.toString()
            manfaat = etManfaat.text.toString()
            cekData()
        }
    }
    private fun cekData(){
        if(plantNetName.isEmpty()){
            showSnack(this,"PlantNetName tidak boleh kosong")
        }else if(latin.isEmpty()){
            showSnack(this,"Latin tidak boleh kosong")
        }else if(nama.isEmpty()){
            showSnack(this,"Nama tidak boleh kosong")
        }else if(kerajaan.isEmpty()){
            showSnack(this,"Kerajaan tidak boleh kosong")
        }else if(famili.isEmpty()){
            showSnack(this,"Famili tidak boleh kosong")
        }else if(ordo.isEmpty()){
            showSnack(this,"Ordo tidak boleh kosong")
        }else if(spesies.isEmpty()){
            showSnack(this,"Spesies tidak boleh kosong")
        }else if(manfaat.isEmpty()){
            showSnack(this,"Manfaat tidak boleh kosong")
        }else{
            progressDialog.show()
            if(type=="edit"){
                if (ubahGambar) {
                    // Kompres dan unggah gambar di latar belakang
                    lifecycleScope.launch(Dispatchers.IO) {
                        gambar = uploadImage(imageUr)
                        //dispatcher
                        withContext(Dispatchers.Main) {
                            progressDialog.dismiss()
                            editData()
                        }
                    }
                }else{
                    editData()
                }
            }else{
                lifecycleScope.launch(Dispatchers.IO) {
                    gambar = uploadImage(imageUr)
                    //dispatcher
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        tambahData()
                    }
                }
            }
        }
    }
    private suspend fun uploadImage(imageUri: Uri?): String {
        val compressedImageUri = compressImage(this,imageUri)
        val storageReference = FirebaseStorage.getInstance().getReference("foto_plants")
        val imageFileName = UUID.randomUUID().toString()
        val imageRef = storageReference.child("$imageFileName.jpg")
        return try {
            val uploadTask = imageRef.putFile(compressedImageUri).await()
            val imageUrl = imageRef.downloadUrl.await().toString()
            imageUrl
        } catch (e: Exception) {
            throw e
        }
    }
    private suspend fun compressImage(context: Context, imageUri: Uri?): Uri {
        val originalBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        val compressedBitmap = ImageUtils.compressBitmap(originalBitmap)
        val compressedImageUri = ImageUtils.createTempImageFile(context)
        val outputStream = context.contentResolver.openOutputStream(compressedImageUri)
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream!!)
        outputStream?.close()

        return compressedImageUri
    }
    private fun tambahData(){
        progressDialog.show()
        val barangData = hashMapOf(
            "uid" to UUID.randomUUID().toString(),
            "gambar" to gambar,
            "latin" to latin,
            "nama" to nama,
            "kerajaan" to kerajaan,
            "famili" to famili,
            "ordo" to ordo,
            "spesies" to spesies,
            "manfaat" to manfaat,
            "plantNetName" to plantNetName,
        )
        val db = FirebaseFirestore.getInstance()
        // Add the product data to Firestore
        db.collection("plant")
            .add(barangData as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                showSnack(this,"Berhasil menyimpan barang")
                progressDialog.dismiss()
                // Redirect to SellerActivity fragment home
                val intent = Intent(this, AdminActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                progressDialog.dismiss()
                showSnack(this,"Gagal menyimpan barang ${e.message}")
            }
    }
    private fun editData(){
        progressDialog.show()
        val barangData = hashMapOf(
            "gambar" to gambar,
            "latin" to latin,
            "nama" to nama,
            "kerajaan" to kerajaan,
            "famili" to famili,
            "ordo" to ordo,
            "spesies" to spesies,
            "manfaat" to manfaat,
            "plantNetName" to plantNetName,
        )
        val db = FirebaseFirestore.getInstance()
        db.collection("plant")
            .document(docId)
            .update(barangData as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                showSnack(this,"Berhasil menyimpan barang")
                progressDialog.dismiss()
                // Redirect to SellerActivity fragment home
                val intent = Intent(this, AdminActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                progressDialog.dismiss()
                showSnack(this,"Gagal menyimpan barang ${e.message}")
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_COVER -> {
                    // Ambil URI gambar yang dipilih dari galeri
                    val selectedImageUri = data?.data
                    // Tampilkan gambar yang dipilih ke imageView coverReplace
                    //coverReplace.setImageURI(selectedImageUri)
                    Glide.with(this).load(selectedImageUri).into(coverReplace)
                    // Simpan URI gambar ke dalam list untuk penggunaan nanti
                    imageUr = selectedImageUri!!
                    ubahGambar = true
                }
            }
        }
    }
}