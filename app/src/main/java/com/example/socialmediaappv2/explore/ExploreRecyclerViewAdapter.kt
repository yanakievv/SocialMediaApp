package com.example.socialmediaappv2.explore

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.data.ImageModel
import com.example.socialmediaappv2.profile.ProfileActivity


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
        holder.dateView.text = item.date.take(10)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.explore_item_image_view)
        val contentView: TextView = view.findViewById(R.id.publisher_display_name)
        val dateView: TextView = view.findViewById(R.id.date)

        init {
            imageView.setOnClickListener { v ->
                if (v?.id == imageView.id) {
                    val popup = PopupMenu(context, this.contentView)
                    popup.inflate(R.menu.explore_menu)
                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.preview_image -> {
                               (context as ExploreActivity).displayFragment(values[adapterPosition])
                                true
                            }
                            R.id.visit_profile -> {
                                val intent = Intent(context, ProfileActivity::class.java)
                                intent.putExtra("userId", values[adapterPosition].publisherId)
                                startActivity(context, intent, null)
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}