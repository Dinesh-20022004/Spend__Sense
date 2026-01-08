package com.example.spendsense.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spendsense.CurrencyHelper
import com.example.spendsense.databinding.ItemCategoryStatBinding
import com.example.spendsense.models.CategoryStat

class CategoryStatAdapter(
    private val categories: List<CategoryStat>
) : RecyclerView.Adapter<CategoryStatAdapter.CategoryStatViewHolder>() {

    inner class CategoryStatViewHolder(
        private val binding: ItemCategoryStatBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(categoryStat: CategoryStat) {
            // Get the dynamic currency symbol
            val currency = CurrencyHelper.getCurrencySymbol(binding.root.context)

            binding.tvCategoryName.text = categoryStat.category

            // Use correct currency
            binding.tvAmount.text = "$currency${String.format("%.0f", categoryStat.amount)}"

            // HIDE the percentage text view
            binding.tvPercentage.visibility = View.GONE

            // HIDE the progress bar (The red line)
            binding.progressBar.visibility = View.GONE

            binding.tvTransactionCount.text = "${categoryStat.count} transaction${if (categoryStat.count != 1) "s" else ""}"

            // Set icon based on category
            binding.tvCategoryIcon.text = getCategoryIcon(categoryStat.category)
        }

        private fun getCategoryIcon(category: String): String {
            return when (category.lowercase()) {
                "food" -> "🍕"
                "transport" -> "🚗"
                "shopping" -> "🛒"
                "bills" -> "💡"
                "entertainment" -> "🎬"
                "health" -> "🏥"
                "education" -> "📚"
                "salary" -> "💼"
                "freelance" -> "💻"
                "business" -> "🏢"
                "investment" -> "📈"
                "gift" -> "🎁"
                else -> "📊"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryStatViewHolder {
        val binding = ItemCategoryStatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryStatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryStatViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size
}