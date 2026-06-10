package com.routehub.pos.screens.dues

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.routehub.pos.R
import com.routehub.pos.models.DueItem

// DueAdapter.kt
class DueAdapter(
    private val onSelectionChanged: (List<DueItem>) -> Unit
) : RecyclerView.Adapter<DueAdapter.ViewHolder>() {

    private val items = mutableListOf<DueItem>()

    fun submitList(list: List<DueItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun getItems(): List<DueItem> = items

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cb: CheckBox = view.findViewById(R.id.cbSelect)
        val tvMonth: TextView = view.findViewById(R.id.tvMonth)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_due, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvMonth.text = formatMonthYear(item.monthLabel)
        holder.tvAmount.text = "₹${item.amount}"

        holder.cb.setOnCheckedChangeListener(null)
        holder.cb.isChecked = item.isSelected

        holder.cb.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked
            onSelectionChanged(items)
        }

        holder.itemView.setOnClickListener {
            item.isSelected = !item.isSelected
            notifyItemChanged(position)
            onSelectionChanged(items)
        }
    }

    fun formatMonthYear(input: String?): String {
        if (input.isNullOrEmpty()) return "Unknown"

        return try {
            val parts = input.split("-")
            val month = parts[0].toInt()
            val year = parts[1]

            val monthName = java.text.DateFormatSymbols().months[month - 1]

            "$monthName $year"
        } catch (e: Exception) {
            e.printStackTrace()
            input // fallback
        }
    }
}