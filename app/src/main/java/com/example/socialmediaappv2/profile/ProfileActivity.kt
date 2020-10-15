package com.example.socialmediaappv2.profile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.contract.Contract
import com.example.socialmediaappv2.home.HomeActivity
import com.example.socialmediaappv2.upload.Camera2Activity

import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.home_button
import kotlinx.android.synthetic.main.activity_home.upload_button
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.File

internal lateinit var presenter: Contract.ProfileInfoPresenter

class ProfileActivity : AppCompatActivity(), Contract.ProfileView {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setPresenter(ProfileInfoPresenter(this))

        /*val path: File = File(presenter.getPicture())
        val myBitmap = BitmapFactory.decodeFile(path.absolutePath)
        logo2.setImageBitmap(myBitmap)*/



        upload_button.setOnClickListener {
            startActivity(Intent(this, Camera2Activity::class.java))
        }
        home_button.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    override fun inflate() {
    }

    override fun setPresenter(_presenter: Contract.ProfileInfoPresenter) {
        presenter = _presenter
    }


}