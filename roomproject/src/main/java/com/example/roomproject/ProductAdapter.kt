package com.example.roomproject

// ProductAdapter.kt
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.roomproject.databinding.ItemProductBinding

class ProductAdapter(
    private var products: List<Product>,
    private val onItemClick: (Product) -> Unit,
    private val onItemLongClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                tvTitle.text = product.title
                tvDescription.text = product.description
                tvPrice.text = "$${product.price}"

                root.setOnClickListener { onItemClick(product) }
                root.setOnLongClickListener {
                    onItemLongClick(product)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateList(newList: List<Product>) {
        products = newList
        notifyDataSetChanged()
    }
}