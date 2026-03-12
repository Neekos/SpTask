package com.example.pz4nointernet

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductRecyclerAdapter(
    private var productList: MutableList<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductRecyclerAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val productTitle: TextView = itemView.findViewById(R.id.productTitle)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.productTitle.text = product.title
        holder.productPrice.text = "${product.price}, Руб"

        // Загрузка изображения из ресурсов
        holder.productImage.setImageResource(product.imageResId)

        holder.cardView.setOnClickListener {
            onItemClick(product)
        }

        holder.btnEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddEditProductActivity::class.java)
            intent.putExtra("PRODUCT_ID", product.id)
            holder.itemView.context.startActivity(intent)
        }

        holder.btnDelete.setOnClickListener {
            removeItem(position)
        }
    }

    override fun getItemCount(): Int = productList.size

    fun addProduct(product: Product) {
        productList.add(product)
        notifyItemInserted(productList.size - 1)
    }

    fun removeItem(position: Int) {
        productList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateProduct(position: Int, product: Product) {
        productList[position] = product
        notifyItemChanged(position)
    }

    fun findPositionById(productId: Int): Int {
        return productList.indexOfFirst { it.id == productId }
    }
}