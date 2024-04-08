package com.bohdan2505.htmlziptoweb

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArchiveAdapter(
    private val folders: List<String>,
    private val onDeleteClickListener: (String) -> Unit,
    private val onEditClickListener: (String) -> Unit,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ArchiveAdapter.FolderViewHolder>() {

    inner class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val folderName: TextView = view.findViewById(R.id.folderName)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
        val editButton: ImageButton = view.findViewById(R.id.editButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_archive, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folderName = folders[position]
        holder.folderName.text = folderName

        holder.itemView.setOnClickListener {
            onItemClick.invoke(folderName)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClickListener.invoke(folderName)
        }

        holder.editButton.setOnClickListener {
            onEditClickListener.invoke(folderName)
        }
    }

    override fun getItemCount(): Int {
        return folders.size
    }
}
