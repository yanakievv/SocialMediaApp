package com.example.socialmediaappv2.home

import android.content.Context
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.data.ImageModel
import com.example.socialmediaappv2.home.content.PictureContent
import java.io.IOException


class HomeRecyclerViewAdapter(
    private val values: List<ImageModel>,
    private val context: Context
) : RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_home, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.imageView.setImageBitmap(BitmapFactory.decodeFile(item.image))
        holder.contentView.text = item.date
            }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.item_image_view)
        val contentView: TextView = view.findViewById(R.id.item_date)

        init {
            imageView.setOnClickListener { v ->
                if (v?.id == imageView.id) {
                    val popup = PopupMenu(context, this.contentView)
                    popup.inflate(R.menu.home_menu)
                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.set_as_profile_picture -> {
                                PictureContent.setProfilePicture(values[adapterPosition].picId)
                                true
                            }
                            R.id.delete_post -> {
                                PictureContent.removePost(values[adapterPosition].picId, values[adapterPosition].image)
                                notifyDataSetChanged()
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