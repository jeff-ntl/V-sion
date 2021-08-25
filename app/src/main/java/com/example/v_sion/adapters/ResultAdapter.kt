package com.example.v_sion.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.v_sion.R
import com.example.v_sion.models.ResultModel
import kotlinx.android.synthetic.main.card_result.view.*

class ResultAdapter(private var results: List<ResultModel>):
    RecyclerView.Adapter<ResultAdapter.ViewHolder>() {

    class ViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //data displayed in each card
        fun bind(result: ResultModel) {
            itemView.appIcon.setImageBitmap(result.appIcon)
            itemView.appName.text = result.appName
            itemView.timeInForeground.text = result.timeInForeground.toString()
        }
    }

    // set the view by inflating the CardView layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_result, parent, false))
    }

    override fun getItemCount(): Int {
        return results.size
    }

    // decide the contents
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[holder.adapterPosition]
        holder.bind(result)
    }
}