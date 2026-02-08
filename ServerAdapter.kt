package com.freevpn.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ServerAdapter(
    private val servers: List<VpnServer>,
    private val onItemClick: (VpnServer) -> Unit
) : RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {

    class ServerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flagIcon: TextView = itemView.findViewById(R.id.flagIcon)
        val serverName: TextView = itemView.findViewById(R.id.serverName)
        val serverCountry: TextView = itemView.findViewById(R.id.serverCountry)
        val selectedIndicator: View = itemView.findViewById(R.id.selectedIndicator)
    }

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_server, parent, false)
        return ServerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val server = servers[position]
        
        holder.flagIcon.text = server.flag
        holder.serverName.text = server.name
        holder.serverCountry.text = when(server.country) {
            "DE" -> "Германия"
            "US" -> "США"
            else -> "Неизвестно"
        }
        
        // Подсветка выбранного сервера
        holder.selectedIndicator.visibility = 
            if (position == selectedPosition) View.VISIBLE else View.INVISIBLE
        
        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onItemClick(server)
        }
        
        // Анимация нажатия
        holder.itemView.setOnTouchListener { v, event ->
            v.alpha = if (event.action == android.view.MotionEvent.ACTION_DOWN) 0.7f else 1.0f
            false
        }
    }

    override fun getItemCount(): Int = servers.size
}
