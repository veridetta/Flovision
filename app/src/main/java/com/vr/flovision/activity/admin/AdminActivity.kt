package com.vr.flovision.activity.admin

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.flovision.MainActivity
import com.vr.flovision.R
import com.vr.flovision.activity.adapter.PlantAdapter
import com.vr.flovision.helper.showSnack
import com.vr.flovision.model.PlantModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AdminActivity : AppCompatActivity() {
    private lateinit var plantAdapter: PlantAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var contentView: RelativeLayout
    private lateinit var searchLayout: LinearLayout
    private lateinit var btnCari: EditText
    private lateinit var btnLogout: ImageView
    private lateinit var btnTambah: Button
    private lateinit var progressDialog: ProgressDialog
    val TAG = "LOAD DATA"
    private val plantList: MutableList<PlantModel> = mutableListOf()
    lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        initView()
        initRc()
        initData()
        initCari()
        initClick()
    }
    private fun initView(){
        mFirestore = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.rcMateri)
        contentView = findViewById(R.id.contentView)
        searchLayout = findViewById(R.id.searchLayout)
        btnCari = findViewById(R.id.btnCari)
        btnTambah = findViewById(R.id.btnTambah)
        btnLogout = findViewById(R.id.btnLogout)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)

    }
    private fun initRc(){
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@AdminActivity, 1)
            // set the custom adapter to the RecyclerView
            plantAdapter = PlantAdapter(
                plantList,
                this@AdminActivity,
                { barang -> editBarang(barang) },
                { barang -> hapusBarang(barang) }
            )
        }
    }
    private fun initData(){
        readData()
        recyclerView.adapter = plantAdapter
    }
    private fun initCari(){
        plantAdapter.filter("")
        btnCari.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                plantAdapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun readData() {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = mFirestore.collection("plant").get().await()
                val plants = mutableListOf<PlantModel>()
                for (document in result) {
                    val plant = document.toObject(PlantModel::class.java)
                    val docId = document.id
                    plant.docId = docId
                    plants.add(plant)
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                }

                withContext(Dispatchers.Main) {
                    plantList.addAll(plants)
                    plantAdapter.filteredBarangList.addAll(plants)
                    plantAdapter.notifyDataSetChanged()
                    progressDialog.dismiss()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                progressDialog.dismiss()
            }
        }
    }

    private fun editBarang(plant: PlantModel) {
        //intent ke homeActivity fragment add
        val intent = Intent(this, AddActivity::class.java)
        intent.putExtra("type", "edit")
        intent.putExtra("docId", plant.docId)
        intent.putExtra("plantNetName", plant.plantNetName)
        intent.putExtra("gambar", plant.gambar)
        intent.putExtra("latin", plant.latin)
        intent.putExtra("nama", plant.nama)
        intent.putExtra("kerajaan", plant.kerajaan)
        intent.putExtra("famili", plant.famili)
        intent.putExtra("ordo", plant.ordo)
        intent.putExtra("spesies", plant.spesies)
        intent.putExtra("manfaat", plant.manfaat)
        startActivity(intent)
    }
    private fun hapusBarang(plant: PlantModel) {
        //dialog konfirmasi
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Yakin ingin menghapus ${plant.nama}?")
        builder.setPositiveButton("Ya") { dialog, which ->
            //hapus barang dari firestore
            progressDialog.show()
            val db = FirebaseFirestore.getInstance()
            db.collection("plant").document(plant.docId.toString())
                .delete()
                .addOnSuccessListener {
                    showSnack(this,"Berhasil menghapus barang")
                    progressDialog.dismiss()
                    // Redirect to SellerActivity fragment home
                    val intent = Intent(this, AdminActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    // Error occurred while adding product
                    Log.w(TAG, "Error getting documents : $e")
                    progressDialog.dismiss()
                }
        }
    }
    private fun initClick(){
        btnLogout.setOnClickListener {
            // Hapus shared preferences
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            // Arahkan ke MainActivity dengan membersihkan stack aktivitas
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        btnTambah.setOnClickListener {
            //intent ke homeActivity fragment add
            val intent = Intent(this, AddActivity::class.java)
            intent.putExtra("type", "tambah")
            startActivity(intent)
        }
    }
}