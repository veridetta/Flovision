package com.vr.flovision.activity.user

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.vr.flovision.MainActivity
import com.vr.flovision.R
import com.vr.flovision.api.sendPostRequest
import com.vr.flovision.helper.ApiConstant.baseUrl
import com.vr.flovision.helper.ApiHelper.Companion.API_KEY
import com.vr.flovision.helper.ApiHelper.Companion.BASE_URL
import com.vr.flovision.helper.ApiInstance
import com.vr.flovision.helper.showSnack
import com.vr.flovision.model.PlantModel
import com.vr.flovision.model.PlantNetModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.Locale
import java.util.UUID

class ResultActivity : AppCompatActivity(), TextToSpeech.OnInitListener  {
    lateinit var contentView: RelativeLayout
    lateinit var btnBack: ImageView
    lateinit var imgCover: ImageView
    lateinit var tvLatin: TextView
    lateinit var tvNama: TextView
    lateinit var tvKerajaan: TextView
    lateinit var tvFamili: TextView
    lateinit var tvOrdo: TextView
    lateinit var tvSpesies: TextView
    lateinit var tvManfaat: TextView
    lateinit var lyScan : RelativeLayout
    lateinit var imgScan : ImageView
    var plantNetName = ""
    var imageFilePath =""
    val TAG = "BarangActivity"
    private lateinit var textToSpeech: TextToSpeech
    var audioSiap = false
    val mFirebase = FirebaseFirestore.getInstance()
    val REQUEST_CODE = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        initView()
        initIntent()
        initClick()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            showSnack(this, "Izin akses penyimpanan dibutuhkan")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
        } else {
            postFB()
        }

    }
    private fun initView(){
        contentView = findViewById(R.id.contentView)
        tvLatin = findViewById(R.id.tvLatin)
        tvNama = findViewById(R.id.tvNama)
        tvKerajaan = findViewById(R.id.tvKerajaan)
        tvFamili = findViewById(R.id.tvFamili)
        tvOrdo = findViewById(R.id.tvOrdo)
        tvSpesies = findViewById(R.id.tvSpesies)
        tvManfaat = findViewById(R.id.tvManfaat)
        imgCover = findViewById(R.id.imgCover)
        btnBack = findViewById(R.id.btnBack)
        lyScan = findViewById(R.id.lyScan)
        imgScan = findViewById(R.id.imgScan)
        textToSpeech = TextToSpeech(this, this)
        Glide.with(this).load(R.drawable.qr_scan).into(imgScan)
    }
    private fun initIntent(){
        imageFilePath = intent.getStringExtra("imageFilePath").toString()
    }
    private fun initClick(){
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun postFB() {
        if (imageFilePath != "") {
            Log.d(TAG, "imageFilePath: $imageFilePath")
            GlobalScope.launch(Dispatchers.IO) {
                // Kirim gambar ke server
                val responseDeferred = sendPostRequest(baseUrl, imageFilePath, API_KEY, false, File(imageFilePath))
                val response = responseDeferred.await()
                withContext(Dispatchers.Main) {
                    if (response != null) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            if (responseBody != null) {
                                val responseString = responseBody.toString()
                                // Log successful response
                                println("POST Berhasil: $responseString")

                                val responseModel = parsePlantNetModel(responseString)
                                if (responseModel != null) {
                                    // Tanggapan sukses, lakukan sesuatu dengan respons
                                    // Misalnya, tampilkan informasi dari respons
                                    println("Bahasa: ${responseModel.language}")
                                    println("Tanaman Terbaik: ${responseModel.bestMatch}")
                                    plantNetName = responseModel.bestMatch
                                    if (plantNetName != "") {
                                        mFirebase.collection("plant")
                                            .whereEqualTo("plantNetName", plantNetName)
                                            .get()
                                            .addOnSuccessListener { result ->
                                                if(result.isEmpty){
                                                    lyScan.visibility = View.GONE
                                                    showSnack(this@ResultActivity, "Tanaman tidak ditemukan")
                                                    finish()
                                                }else{
                                                    for (document in result) {
                                                        lyScan.visibility = View.GONE
                                                        tvLatin.text = document.data["latin"].toString()
                                                        tvNama.text = document.data["nama"].toString()
                                                        tvKerajaan.text =
                                                            document.data["kerajaan"].toString()
                                                        tvFamili.text =
                                                            document.data["famili"].toString()
                                                        tvOrdo.text = document.data["ordo"].toString()
                                                        tvSpesies.text =
                                                            document.data["spesies"].toString()
                                                        tvManfaat.text =
                                                            document.data["manfaat"].toString()
                                                        Glide.with(this@ResultActivity)
                                                            .load(document.data["gambar"].toString())
                                                            .into(imgCover)
                                                        val plant =
                                                            document.toObject(PlantModel::class.java)
                                                        val docId = document.id
                                                        plant.docId = docId
                                                        saveToHistory(plant)
                                                        //siapkan semua data untuk dibuat speaktext
                                                        var text =
                                                            "Hasil Scan menghasilkan tanaman dengan nama latin ${document.data["latin"].toString()} " +
                                                                    "dan nama lokal ${document.data["nama"].toString()} dengan kerajaan ${document.data["kerajaan"].toString()} " +
                                                                    "famili ${document.data["famili"].toString()} ordo ${document.data["ordo"].toString()} " +
                                                                    "spesies ${document.data["spesies"].toString()} dan memiliki manfaat kesehatan ${document.data["manfaat"].toString()}"
                                                        speakText(text)
                                                    }
                                                }
                                            }
                                    } else {
                                        showSnack(this@ResultActivity, "Tanaman tidak ditemukan")
                                    }
                                    println("Versi: ${responseModel.version}")
                                } else {
                                    showSnack(this@ResultActivity, "Tidak dapat mengenali tanaman")
                                }
                            }
                        } else {
                            // Log error response
                            println("POST Error Response: ${response.errorBody().toString()}")
                            // Tanggapan gagal
                            showSnack(this@ResultActivity, "Gagal melakukan permintaan jaringan")
                        }
                    } else {
                        // Log error
                        println("POST Error: respons null ini?")
                    }
                }
            }
        }
    }

    private fun parsePlantNetModel(responseString: String): PlantNetModel? {
        // Parsing respons yang diharapkan
        val language = getAttributeValue(responseString, "language")
        val preferedReferential = getAttributeValue(responseString, "preferedReferential")
        val bestMatch = getAttributeValue(responseString, "bestMatch")
        val version = getAttributeValue(responseString, "version")
        return PlantNetModel(language, preferedReferential, bestMatch, version)
    }

    private fun getAttributeValue(responseString: String, attribute: String): String {
        val attributeIndex = responseString.indexOf("$attribute=")
        if (attributeIndex >= 0) {
            val attributeStart = attributeIndex + attribute.length + 1
            val attributeEnd = responseString.indexOf(",", attributeStart)
            if (attributeEnd >= 0) {
                return responseString.substring(attributeStart, attributeEnd)
            }
        }
        return ""
    }
    fun getFilePathFromUri(contentResolver: ContentResolver, uri: Uri): String? {
        val filePath: String?
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            filePath = cursor.getString(columnIndex)
            cursor.close()
        } else {
            filePath = uri.path
        }
        return filePath
    }

    private fun speakText(text: String) {
        val handler = Handler()
        handler.postDelayed(Runnable {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            if(!textToSpeech.isSpeaking()) {
                textToSpeech = TextToSpeech(this, this)
                System.out.println("tts restarted")
            }
        }, 3000) // 3 sec
    }
    private fun stopSpeaking() {
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
    }
    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
    override fun onInit(status: Int) {
        audioSiap = status == TextToSpeech.SUCCESS
    }
    override fun onResume() {
        super.onResume();
        textToSpeech = TextToSpeech(this, this)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showSnack(this, "Izin akses penyimpanan diberikan")
                    postFB()
                } else {
                    showSnack(this, "Izin akses penyimpanan ditolak")
                }
            }
        }
    }

    private fun saveToHistory(plant :PlantModel){
        //check shared prefrences apakah uid sudah ada
        val sharedPref = getSharedPreferences("user", MODE_PRIVATE)
        val uid = sharedPref.getString("uid", UUID.randomUUID().toString())
        //save uid ke shared prefrences
        val editor = sharedPref.edit()
        editor.putString("uid", uid)
        editor.apply()

        val data = hashMapOf(
            "uid" to uid,
            "nama" to plant.nama,
            "latin" to plant.latin,
            "kerajaan" to plant.kerajaan,
            "famili" to plant.famili,
            "ordo" to plant.ordo,
            "spesies" to plant.spesies,
            "gambar" to plant.gambar,
            "manfaat" to plant.manfaat
        )
        mFirebase.collection("history").document().set(data)
            .addOnSuccessListener {
                println("Sukses")
            }
            .addOnFailureListener { e ->
                println("Gagal")
            }
    }
}