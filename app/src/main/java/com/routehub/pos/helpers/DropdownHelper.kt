package com.routehub.pos.helpers

import android.R
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView

class DropdownHelper<T>(
    val autoComplete: AutoCompleteTextView,
    private val displayMapper: (T) -> String
) {

    private var items: List<T> = emptyList()
    var selectedItem: T? = null

    private var externalListener: ((T, Int) -> Unit)? = null

    fun setItems(list: List<T>) {
        items = list

        val adapter = ArrayAdapter(
            autoComplete.context,
            android.R.layout.simple_dropdown_item_1line,
            list.map { displayMapper(it) }
        )

        autoComplete.setAdapter(adapter)

        autoComplete.setOnItemClickListener { _, _, position, _ ->
            selectedItem = items[position]

            // 🔥 call external listener also
            externalListener?.invoke(selectedItem!!, position)
        }
    }

    fun setOnItemSelected(listener: (T, Int) -> Unit) {
        externalListener = listener
    }

    fun clear() {
        autoComplete.setText("")
        selectedItem = null
    }
}