package com.routehub.pos.screens.dues

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.routehub.pos.R
import com.routehub.pos.models.DueItem

class DueSelectionActivity : AppCompatActivity() {

    private lateinit var rvDues: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnConfirm: Button

    private lateinit var adapter: DueAdapter

    companion object {
        const val EXTRA_DUES = "extra_dues"
        const val RESULT_SELECTED_DUES = "result_selected_dues"
        const val RESULT_TOTAL = "result_total"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_due_selection)

        rvDues = findViewById(R.id.rvDues)
        tvTotal = findViewById(R.id.tvTotal)
        btnConfirm = findViewById(R.id.btnConfirm)

        val dues = intent.getParcelableArrayListExtra<DueItem>(EXTRA_DUES) ?: arrayListOf()

        adapter = DueAdapter {
            updateTotal(it)
        }

        rvDues.layoutManager = LinearLayoutManager(this)
        rvDues.adapter = adapter

        adapter.submitList(dues)

        updateTotal(dues)

        btnConfirm.setOnClickListener {
            val selected = adapter.getItems().filter { it.isSelected }
            val total = selected.sumOf { it.amount }

            val resultIntent = Intent().apply {
                putParcelableArrayListExtra(
                    RESULT_SELECTED_DUES,
                    ArrayList(selected)
                )
                putExtra(RESULT_TOTAL, total)
            }

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun updateTotal(list: List<DueItem>) {
        val total = list.filter { it.isSelected }.sumOf { it.amount }
        tvTotal.text = "Total: ₹$total"

        btnConfirm.isEnabled = total > 0
        btnConfirm.text = "CONFIRM PAYMENT ₹$total"
    }

}