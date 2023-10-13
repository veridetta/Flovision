package com.vr.flovision.activity.user

import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.flovision.MainActivity
import com.vr.flovision.R
import com.vr.flovision.api.sendPostRequest
import com.vr.flovision.helper.ApiHelper.Companion.API_KEY
import com.vr.flovision.helper.ApiHelper.Companion.BASE_URL
import com.vr.flovision.helper.showSnack
import com.vr.flovision.model.PlantModel
import com.vr.flovision.model.PlantNetModel
import java.util.Locale
import java.util.UUID

class ResultHistoryActivity : AppCompatActivity(), TextToSpeech.OnInitListener  {
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
    var docId = ""
    val TAG = "BarangActivity"
    private lateinit var textToSpeech: TextToSpeech
    var audioSiap = false
    val mFirebase = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_history)
        initView()
        initIntent()
        initClick()
        postFB()
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
        Glide.with(this).load(R.drawable.qr_scan).into(imgScan)
    }
    private fun initIntent(){
        docId = intent.getStringExtra("docId").toString()
    }
    private fun initClick(){
        btnBack.setOnClickListener {
            stopSpeaking()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun postFB(){
        if (docId != "") {
            mFirebase.collection("plant").document(docId).get()
                .addOnSuccessListener { result ->
                    val plant = result.toObject(PlantModel::class.java)
                    if (plant != null) {
                        tvLatin.text = plant.latin
                        tvNama.text = plant.nama
                        tvKerajaan.text = plant.kerajaan
                        tvFamili.text = plant.famili
                        tvOrdo.text = plant.ordo
                        tvSpesies.text = plant.spesies
                        tvManfaat.text = plant.manfaat
                        Glide.with(this).load(plant.gambar).into(imgCover)
                        var text = "Hasil Scan menghasilkan tanaman dengan nama latin ${plant.latin} " +
                                "dan nama lokal ${plant.nama} dengan kerajaan ${plant.kerajaan} " +
                                "famili ${plant.famili} ordo ${plant.ordo} " +
                                "spesies ${plant.spesies} dan memiliki manfaat kesehatan ${plant.manfaat}"
                        speakText(text)
                    }
                }
        }
    }
    private fun speakText(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
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
}