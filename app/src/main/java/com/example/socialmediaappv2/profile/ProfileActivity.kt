package com.example.socialmediaappv2.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.contract.Contract
import com.example.socialmediaappv2.data.SharedPreference
import com.example.socialmediaappv2.explore.ExploreActivity
import com.example.socialmediaappv2.explore.MapsActivity
import com.example.socialmediaappv2.explore.content.PublicPictureContent
import com.example.socialmediaappv2.home.HomeActivity
import com.example.socialmediaappv2.home.content.PublisherPictureContent
import com.example.socialmediaappv2.login.LoginActivity
import com.example.socialmediaappv2.upload.Camera2Activity
import kotlinx.android.synthetic.main.activity_home.home_button
import kotlinx.android.synthetic.main.activity_home.upload_button
import kotlinx.android.synthetic.main.activity_profile.*

private lateinit var presenter: Contract.ProfileInfoPresenter
private lateinit var userId: String
private lateinit var sharedPref: SharedPreference

class ProfileActivity : AppCompatActivity(), Contract.ProfileView {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        sharedPref = SharedPreference(this)
        setPresenter(ProfileInfoPresenter(this))
        if (intent.hasExtra("userId")) {
            userId = intent.getStringExtra("userId")!!
        }
        else {
            userId = sharedPref.getString("publisherId")!!
        }
        presenter.init(userId, this)
        update()

        viewPosts.setOnClickListener {
            if (userId == sharedPref.getString("publisherId")!!) {
                startActivity(Intent(this, HomeActivity::class.java))
            }
            else {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("userId", userId)
                intent.putExtra("displayName", presenter.getDisplayName())
                startActivity(intent)
            }
        }
        backButton.setOnClickListener {
            finish()
        }
        explore_button.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }
        upload_button.setOnClickListener {
            startActivity(Intent(this, Camera2Activity::class.java))
        }
        home_button.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        profile_button.setOnClickListener {
            finish()
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        settings.setOnClickListener {
            val popup = PopupMenu(this, this.settings)
            popup.inflate(R.menu.profile_settings_menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.logout -> {
                        PublicPictureContent.initLoaded = false
                        PublisherPictureContent.initLoaded = false
                        sharedPref.clearData()
                        PublisherPictureContent.TEMP.clear()
                        finishAffinity()
                        startActivity(Intent(this, LoginActivity::class.java))
                        true
                    }
                    R.id.edit_profile -> {
                        displayFragment()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun onResume(){
        super.onResume()
        update()
    }

    @SuppressLint("SetTextI18n")
    override fun update() {
        presenter.reInit(sharedPref.getString("publisherId")!!)
        val img = presenter.getProfilePic()
        profilePicture.setImageBitmap(BitmapFactory.decodeFile(img?.image))
        displayName.text = presenter.getDisplayName()
        posts.text = "Posts: " + presenter.getNumberOfPosts()
        birthDate.text = "Born: " + presenter.getBirthDate()
        bioContent.text = presenter.getBio()
        settingsButtonView(presenter.isCurrentProfile())
    }

    override fun setPresenter(_presenter: Contract.ProfileInfoPresenter) {
        presenter = _presenter
    }

    private fun settingsButtonView(visible: Boolean) {
        if (visible) {
            settings.visibility = View.VISIBLE
            backButton.visibility = View.INVISIBLE
        }
        else {
            settings.visibility = View.INVISIBLE
            backButton.visibility = View.VISIBLE
        }
    }

    private fun displayFragment() {
        val editProfileFragment = EditProfileFragment.newInstance(sharedPref.getString("publisherId")!!)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(fragment_container.id, editProfileFragment).commit()
    }

}