package com.example.spendsense.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spendsense.databinding.ItemCategoryStatBinding
import com.example.spendsense.models.CategoryStat

class CategoryStatAdapter(
    private val categories: List<CategoryStat>
) : RecyclerView.Adapter<CategoryStatAdapter.CategoryStatViewHolder>() {

    inner class CategoryStatViewHolder(
        private val binding: ItemCategoryStatBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(categoryStat: CategoryStat) {
            binding.tvCategoryName.text = categoryStat.category
            binding.tvAmount.text = "₹${String.format("%.0f", categoryStat.amount)}"
            binding.tvPercentage.text = "${String.format("%.1f", categoryStat.percentage)}%"
            binding.tvTransactionCount.text = "${categoryStat.count} transaction${if (categoryStat.count != 1) "s" else ""}"

            binding.progressBar.progress = categoryStat.percentage.toInt()

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