package com.example.socialmediaappv2.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.UserInfoPresenter
import com.example.socialmediaappv2.contract.Contract
import com.example.socialmediaappv2.home.HomeActivity
import com.example.socialmediaappv2.upload.UploadActivity
import kotlinx.android.synthetic.main.activity_home.*

internal lateinit var presenter: Contract.UserInfoPresenter

class ProfileActivity : AppCompatActivity(), Contract.MainView {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setPresenter(UserInfoPresenter(this))

        upload_button.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }
        home_button.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }


    }

    override fun setPresenter(_presenter: Contract.UserInfoPresenter) {
        presenter = _presenter
    }
}