package com.example.spendsense.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.spendsense.R
import com.example.spendsense.databinding.ItemBudgetBinding
import com.example.spendsense.models.Budget

class BudgetAdapter(
    private var budgets: List<Budget>,
    private val onDeleteClick: (Budget) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    inner class BudgetViewHolder(private val binding: ItemBudgetBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(budget: Budget) {
            binding.tvCategory.text = budget.category
            binding.tvSpent.text = "₹${String.format("%.0f", budget.spent)}"
            binding.tvBudget.text = "₹${String.format("%.0f", budget.amount)}"

            val percentage = budget.getPercentage()

            // This line will now work correctly
            binding.tvPercentage.text = "${String.format("%.0f", percentage)}%"

            val progress = percentage.toInt().coerceIn(0, 100)
            binding.progressBar.progress = progress

            val colorRes = when {
                percentage >= 100 -> R.color.expense_red
                percentage >= 80 -> android.R.color.holo_orange_dark
                else -> R.color.income_green
            }

            val colorInt = ContextCompat.getColor(binding.root.context, colorRes)
            binding.progressBar.progressTintList = ColorStateList.valueOf(colorInt)
            binding.tvPercentage.setTextColor(colorInt)

            if (budget.isOverBudget()) {
                val over = budget.spent - budget.amount
                binding.tvStatus.text = "⚠️ Over budget by ₹${String.format("%.0f", over)}"
                binding.tvStatus.setTextColor(colorInt)
            } else {
                binding.tvStatus.text = "₹${String.format("%.0f", budget.getRemaining())} remaining"
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.gray))
            }

            binding.tvCategoryIcon.text = getCategoryIcon(budget.category)

            binding.ivDelete.setOnClickListener {
                onDeleteClick(budget)
            }
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
                else -> "💰"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(budgets[position])
    }

    override fun getItemCount() = budgets.size

    fun updateBudgets(newBudgets: List<Budget>) {
        this.budgets = newBudgets
        notifyDataSetChanged()
    }
}