package com.vr.flovision.activity.adapter
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vr.flovision.R
import com.vr.flovision.model.PlantModel
import java.util.Locale


class PlantAdapter(
    private var barangList: MutableList<PlantModel>,
    val context: Context,
    private val onEditClickListener: (PlantModel) -> Unit,
    private val onHapusClickListener: (PlantModel) -> Unit,
) : RecyclerView.Adapter<PlantAdapter.ProductViewHolder>() {
    public var filteredBarangList: MutableList<PlantModel> = mutableListOf()
    init {
        filteredBarangList.addAll(barangList)
    }
    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && filteredBarangList.isEmpty()) {
            1 // Return 1 for empty state view
        } else {
            0 // Return 0 for regular product view
        }
    }
    fun filter(query: String) {
        filteredBarangList.clear()
        if (query !== null || query !=="") {
            val lowerCaseQuery = query.toLowerCase(Locale.getDefault())
            for (product in barangList) {
                val nam = product.nama?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery)
                Log.d("Kunci ", lowerCaseQuery)
                if (nam == true) {
                    filteredBarangList.add(product)
                    Log.d("Ada ", product.nama.toString())
                }
            }
        } else {
            filteredBarangList.addAll(barangList)
        }
        notifyDataSetChanged()
        Log.d("Data f",filteredBarangList.size.toString())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plants, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredBarangList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentBarang = filteredBarangList[position]

        holder.tvNama.text = currentBarang.nama
        holder.tvNamaLatin.text = " "+currentBarang.latin
        holder.tvKerajaan.text = "Kerajaan " + currentBarang.kerajaan+", Famili "+currentBarang.famili+", Ordo "+currentBarang.ordo+", Spesies "+currentBarang.spesies
        //atur text manfaat maksimal 3 baris setelah itu ditambahkan titik, dan bisa di togle show less more
        holder.tvManfaat.text = currentBarang.manfaat
        var isExpanded = false
        holder.tvManfaat.setOnClickListener {
            isExpanded = !isExpanded
            if (isExpanded) {
                holder.tvManfaat.maxLines = Int.MAX_VALUE // Tampilkan semua baris
            } else {
                holder.tvManfaat.maxLines = 3 // Kembali ke maksimum 3 baris
            }
        }
        Glide.with(context)
            .load(currentBarang.gambar)
            .override(270,270).centerCrop()
            .placeholder(R.drawable.no_image)
            .into(holder.imgCover)
        holder.btnUbah.setOnClickListener { onEditClickListener(currentBarang) }
        holder.btnHapus.setOnClickListener { onHapusClickListener(currentBarang) }
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvNamaLatin: TextView = itemView.findViewById(R.id.tvNamaLatin)
        val tvKerajaan: TextView = itemView.findViewById(R.id.tvKerajaan)
        val tvManfaat: TextView = itemView.findViewById(R.id.tvManfaat)
        //val showMoreTextView: TextView = itemView.findViewById(R.id.showMoreTextView)
        val imgCover: ImageView = itemView.findViewById(R.id.imgCover)
        val btnUbah: LinearLayout = itemView.findViewById(R.id.btnUbah)
        val btnHapus: LinearLayout = itemView.findViewById(R.id.btnHapus)
    }
}
