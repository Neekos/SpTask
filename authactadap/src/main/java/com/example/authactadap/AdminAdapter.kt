package com.example.authactadap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
data class UserItem(val name: String, val age: Int, val email: String)
class AdminAdapter(private val items: List<UserItem>): RecyclerView.Adapter<AdminAdapter.VH>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_users, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int
    ) {
        val u = items[position]
        holder.tvName.text = u.name
        holder.tvAge.text = "Возраст: ${u.age}"
        holder.tvEmail.text = "Email: ${u.email}"
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvAge: TextView = itemView.findViewById(R.id.tvAge)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
    }
}