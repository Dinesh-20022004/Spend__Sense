package com.example.spendsense.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.spendsense.R
import com.example.spendsense.databinding.ItemTransactionBinding
import com.example.spendsense.models.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.tvTitle.text = transaction.title
            binding.tvCategory.text = transaction.category
            binding.tvDate.text = formatDate(transaction.date)

            val amountText = if (transaction.type == "income") {
                "+₹${String.format("%.2f", transaction.amount)}"
            } else {
                "-₹${String.format("%.2f", transaction.amount)}"
            }
            binding.tvAmount.text = amountText

            val amountColor = if (transaction.type == "income") {
                R.color.income_green
            } else {
                R.color.expense_red
            }
            binding.tvAmount.setTextColor(
                ContextCompat.getColor(binding.root.context, amountColor)
            )

            val iconRes = when (transaction.category.lowercase()) {
                "food" -> R.drawable.ic_food
                "transport" -> R.drawable.ic_transport
                "shopping" -> R.drawable.ic_shopping
                "bills" -> R.drawable.ic_bills
                "salary" -> R.drawable.ic_income
                "freelance" -> R.drawable.ic_income
                else -> R.drawable.ic_other
            }
            binding.ivIcon.setImageResource(iconRes)

            // Click listener for edit
            binding.root.setOnClickListener {
                onItemClick(transaction)
            }

            // Long press for delete
            binding.root.setOnLongClickListener {
                onDeleteClick(transaction)
                true
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateString
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    // Method to update transactions
    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
    // In TransactionAdapter.kt

    fun addTransaction(transaction: Transaction) {
        val newTransactions = mutableListOf(transaction)
        newTransactions.addAll(transactions)
        transactions = newTransactions
        notifyItemInserted(0)
    }

    fun removeTransaction(position: Int) {
        val mutableTransactions = transactions.toMutableList()
        mutableTransactions.removeAt(position)
        transactions = mutableTransactions
        notifyItemRemoved(position)
    }
}