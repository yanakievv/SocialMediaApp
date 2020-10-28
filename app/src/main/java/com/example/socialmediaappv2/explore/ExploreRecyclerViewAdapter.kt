package com.example.socialmediaappv2.explore

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.data.ImageModel


class ExploreRecyclerViewAdapter(
    private val values: List<ImageModel>,
    private val context: Context
) : RecyclerView.Adapter<ExploreRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_explore, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.imageView.setImageBitmap(BitmapFactory.decodeFile(item.image))
        holder.contentView.text = item.publisherDisplayName
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.explore_item_image_view)
        val contentView: TextView = view.findViewById(R.id.publisher_display_name)

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}