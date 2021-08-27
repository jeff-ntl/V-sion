package com.example.v_sion.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.v_sion.R
import com.example.v_sion.models.HistoryModel
import kotlinx.android.synthetic.main.card_history.view.*

interface HistoryListener {
    fun triggerDelete(history: HistoryModel)
}

class HistoryAdapter(private var histories: MutableList<HistoryModel>, private val listener: HistoryListener):
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //data displayed in each card
        fun bind(history: HistoryModel, listener:HistoryListener) {
            itemView.tag =history
            itemView.dateText.text = history.date
            itemView.timeText.text = history.time
            itemView.targetAchievedText.text = history.targetAchieved
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_history, parent, false))
    }

    override fun getItemCount(): Int {
        return histories.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = histories[holder.adapterPosition]
        holder.bind(history,listener)    }

    fun removeAt(position: Int) {
        histories.removeAt(position)
        notifyItemRemoved(position)
    }
}