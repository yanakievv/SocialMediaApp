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
import com.example.socialmediaappv2.explore.content.PublicPictureContent
import com.example.socialmediaappv2.home.HomeActivity
import com.example.socialmediaappv2.home.content.PublisherPictureContent
import com.example.socialmediaappv2.login.LoginActivity
import com.example.socialmediaappv2.upload.Camera2Activity
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.android.synthetic.main.activity_home.home_button
import kotlinx.android.synthetic.main.activity_home.upload_button
import kotlinx.android.synthetic.main.activity_profile.*
import kotlin.math.log

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
                        PublicPictureContent.nuke()
                        PublisherPictureContent.nuke()
                        sharedPref.clearData()
                        finishAffinity()
                        val logoutIntent = Intent(this, LoginActivity::class.java)
                        logoutIntent.putExtra("logout", true)
                        startActivity(logoutIntent)
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
        message_list.setOnClickListener {
            //TODO open messages list
        }
        message.setOnClickListener {
            //TODO change activity to direct chat with currently viewed user
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
        profilePicture.setImageBitmap(BitmapFactory.decodeFile(img?.path))
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
            message_list.visibility = View.VISIBLE
            message.visibility = View.INVISIBLE
            backButton.visibility = View.INVISIBLE
            viewPosts.visibility = View.INVISIBLE
            viewPostsText.visibility = View.INVISIBLE
        }
        else {
            settings.visibility = View.INVISIBLE
            message_list.visibility = View.INVISIBLE
            message.visibility = View.VISIBLE
            backButton.visibility = View.VISIBLE
            viewPosts.visibility = View.VISIBLE
            viewPostsText.visibility = View.VISIBLE
        }
    }

    private fun displayFragment() {
        val editProfileFragment = EditProfileFragment.newInstance(sharedPref.getString("publisherId")!!)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(fragment_container.id, editProfileFragment).commit()
    }

}