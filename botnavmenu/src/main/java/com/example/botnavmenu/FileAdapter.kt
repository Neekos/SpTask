package com.example.botnavmenu

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.botnavmenu.databinding.ItemFileBinding
class FileAdapter(
    private var dataSet: List<FileExample>,
    private val context: Context,
    private val onFileClick: (FileExample) -> Unit
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {
    class ViewHolder(binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root) {
        val id = binding.idFile
        val title = binding.nameFile
        val setting = binding.setId
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = dataSet[position]

        holder.id.text = (position + 1).toString()
        holder.title.text = file.fileName
        holder.setting.text = "..."
        // Клик на весь элемент или на иконку
        holder.itemView.setOnClickListener {
            onFileClick(file)
        }
    }

    override fun getItemCount() = dataSet.size

    fun updateDataSet(newDataSet: List<FileExample>) {
        dataSet = newDataSet
        notifyDataSetChanged()
    }
}