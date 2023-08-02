package com.svper.supvpn.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.svper.supvpn.R
import com.svper.supvpn.beans.MyServer
import com.svper.supvpn.databinding.ItemServerBinding
import com.svper.supvpn.utils.ServersManager

class ServerRecyclerAdapter(private val serverList : List<MyServer>,private val onSelectListener : (MyServer)->Unit) : RecyclerView.Adapter<ServerRecyclerAdapter.VH>() {
    class VH(val binding : ItemServerBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemServerBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int = serverList.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.flagImg.setImageResource(ServersManager.getFlagDrawableId(serverList[position]))
        holder.binding.serverName.text = serverList[position].getName()

        val selectIcon = if(ServersManager.selectServer == serverList[position]) R.drawable.select_icon else R.drawable.unselect_icon
        holder.binding.selectIcon.setImageResource(selectIcon)

        holder.binding.root.setOnClickListener{
            onSelectListener.invoke(serverList[position])
        }
    }
}