package com.example.pz4

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductRecyclerAdapter(
    private var productList: MutableList<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductRecyclerAdapter.ProductViewHolder>() {

    // ViewHolder класс
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: View = itemView.findViewById(R.id.cardView)
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val productTitle: TextView = itemView.findViewById(R.id.productTitle)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    // Создание ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    // Привязка данных к ViewHolder
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        // Установка данных
        holder.productTitle.text = product.title
        holder.productPrice.text = "$${product.price}"

        // Загрузка изображения
        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .centerCrop()
            .into(holder.productImage)

        // Обработка клика на карточке (просмотр)
        holder.cardView.setOnClickListener {
            onItemClick(product)
        }

        // Обработка клика на кнопке редактирования
        holder.btnEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddEditProductActivity::class.java)
            intent.putExtra("PRODUCT_ID", product.id)
            holder.itemView.context.startActivity(intent)
        }

        // Обработка клика на кнопке удаления
        holder.btnDelete.setOnClickListener {
            removeItem(position)
        }
    }

    // Количество элементов
    override fun getItemCount(): Int = productList.size

    // Метод для добавления нового товара
    fun addProduct(product: Product) {
        productList.add(product)
        notifyItemInserted(productList.size - 1)
    }

    // Метод для удаления товара
    fun removeItem(position: Int) {
        productList.removeAt(position)
        notifyItemRemoved(position)
    }

    // Метод для обновления товара
    fun updateProduct(position: Int, product: Product) {
        productList[position] = product
        notifyItemChanged(position)
    }

    // Метод для обновления всего списка
    fun updateList(newList: List<Product>) {
        productList.clear()
        productList.addAll(newList)
        notifyDataSetChanged()
    }

    // Метод для поиска позиции товара по ID
    fun findPositionById(productId: Int): Int {
        return productList.indexOfFirst { it.id == productId }
    }
}