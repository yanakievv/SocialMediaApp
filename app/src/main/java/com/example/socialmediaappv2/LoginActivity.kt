package com.example.socialmediaappv2

//import com.facebook.stetho.Stetho
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.android.synthetic.main.activity_login.*

private const val RC_SIGN_IN = 7

class LoginActivity : AppCompatActivity() {

    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext);
        setContentView(R.layout.activity_login)
        callbackManager = CallbackManager.Factory.create()


        val fbAccessToken: AccessToken? = AccessToken.getCurrentAccessToken()
        val googleToken: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)

        if (fbAccessToken != null && !fbAccessToken.isExpired) { //currently logged in with fb
            updateUI(Profile.getCurrentProfile())
        }
        else if (googleToken != null) { // logged in with google
            updateUI(googleToken)
        }
        else {
            updateUI()
        }

        GLoginButton.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

            if (GoogleSignIn.getLastSignedInAccount(this) == null) { // Google Login
                val signInIntent = mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            } else { // Google Logout
                mGoogleSignInClient.signOut()
                updateUI()

            }
        }

        FBMaskButton.setOnClickListener {
            FBLoginButton.performClick()
        }

        FBLoginButton.setOnClickListener {
            // Facebook Logout
            if (AccessToken.getCurrentAccessToken() != null) {
                GraphRequest(
                    AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE
                ) {
                    AccessToken.setCurrentAccessToken(null)
                    LoginManager.getInstance().logOut()
                    updateUI()
                }.executeAsync()
            }

            // Facebook Login
            FBLoginButton.setReadPermissions("email", "public_profile")
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    private lateinit var profileTracker: ProfileTracker

                    override fun onSuccess(loginResult: LoginResult?) {
                        if (Profile.getCurrentProfile() == null) {
                            profileTracker = object : ProfileTracker() {
                                override fun onCurrentProfileChanged(
                                    oldProfile: Profile?,
                                    currentProfile: Profile
                                ) {
                                    Log.v("facebook - profile", currentProfile.firstName)
                                    profileTracker.stopTracking()
                                    updateUI(currentProfile)
                                }
                            }

                        } else {
                            val profile = Profile.getCurrentProfile()
                            Log.v("fbLogin", profile.firstName)
                        }
                    }

                    override fun onCancel() {
                        Log.d("fbLogin", "onCancel.")
                    }

                    override fun onError(error: FacebookException) {
                        Log.d("fbLogin", "onError.")
                    }
                }
            )
        }

        continueButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN && resultCode != 0) {
            val account = GoogleSignIn.getLastSignedInAccount(this)
            if (account != null) {
                updateUI(account)
            }
        }
    }


    private fun updateUI(account: GoogleSignInAccount) { //Google
        FBMaskButton.visibility = View.INVISIBLE
        GLoginButton.text = "Logout"
        continueButton.visibility = View.VISIBLE
        greet.text = "Hello, " + account.givenName + ". Press the middle button below to continue."
    }
    private fun updateUI(account: Profile) { // Facebook
        FBMaskButton.text = "Logout"
        GLoginButton.visibility = View.INVISIBLE
        continueButton.visibility = View.VISIBLE
        greet.text = "Hello, " + account.firstName + ". Press the middle button below to continue."

    }
    private fun updateUI() {
        GLoginButton.text = "Google"
        FBMaskButton.text = "Facebook"
        continueButton.visibility = View.INVISIBLE
        FBMaskButton.visibility = View.VISIBLE
        GLoginButton.visibility = View.VISIBLE
        greet.text = "Log in to continue."
    }
}

