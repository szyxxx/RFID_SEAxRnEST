package com.example.rfid_seaxrnest

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.rfid_seaxrnest.model.Item
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ItemsFragment : Fragment() {

    private lateinit var itemsList: LinearLayout
    private lateinit var itemsTitle: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_items, container, false) // Correct layout file here
        itemsList = view.findViewById(R.id.items_list) // Link to the LinearLayout in fragment_items.xml

        fetchItemsData() // Fetch data for all items, no need for nomorRak here

        return view
    }


    private fun fetchItemsData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataBarangCollection = db.collection("data_barang").get().await()

                val items = mutableListOf<Item>()

                for ((index, document) in dataBarangCollection.withIndex()) {
                    val namaBarang = document.getString("nama_barang") ?: "Unknown"
                    val stokSekarang = document.getLong("stok_sekarang")?.toInt() ?: 0

                    items.add(Item(index + 1, namaBarang, stokSekarang))
                }

                withContext(Dispatchers.Main) {
                    displayItems(items) // Call displayItems on the main thread
                }
            } catch (e: Exception) {
                e.printStackTrace() // Log the error to check if the fetching fails
            }
        }
    }

    private fun displayItems(items: List<Item>) {
        itemsList.removeAllViews() // Clear previous views

        for (item in items) {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_table_row, itemsList, false)

            val itemNo = itemView.findViewById<TextView>(R.id.item_no)
            val itemName = itemView.findViewById<TextView>(R.id.item_name)
            val itemStock = itemView.findViewById<TextView>(R.id.item_stock)

            itemNo.text = item.no.toString()
            itemName.text = item.namaBarang
            itemStock.text = item.stokSekarang.toString()

            itemsList.addView(itemView) // Add the item row to the itemsList
        }
    }
}
