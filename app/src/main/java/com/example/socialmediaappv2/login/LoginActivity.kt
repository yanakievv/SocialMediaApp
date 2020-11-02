package com.example.socialmediaappv2.login

//import com.facebook.stetho.Stetho
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.UserInfoPresenter
import com.example.socialmediaappv2.contract.Contract
import com.example.socialmediaappv2.data.SharedPreference
import com.example.socialmediaappv2.home.HomeActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.stetho.Stetho
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.runBlocking

private const val RC_SIGN_IN = 7
private const val GET_LAT_LONG = "GETLATLONG"
internal lateinit var presenter: Contract.UserInfoPresenter
private lateinit var sharedPref: SharedPreference
internal lateinit var fusedLocationClient: FusedLocationProviderClient

class LoginActivity : AppCompatActivity(), Contract.MainView {

    private lateinit var callbackManager: CallbackManager
    private var publisherId: String? = null
    private var publisherDisplayName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)
        sharedPref = SharedPreference(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContentView(R.layout.activity_login)
        callbackManager = CallbackManager.Factory.create()
        Stetho.initializeWithDefaults(this)
        setPresenter(UserInfoPresenter(this))

        //runBlocking{ UserDatabase.getInstance(applicationContext).imageDAO.nukeAll() }


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
                }.executeAsync()
                updateUI()
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
            if (getLatLong()) {
                runBlocking {
                    presenter.init(publisherId!!, publisherDisplayName!!, applicationContext)

                }

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
            else {
                Snackbar.make(continueButton, "For uploading and viewing others posts the app uses location services. You must enable location permissions for the app before continuing.", Snackbar.LENGTH_LONG).show()
            }
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


    @SuppressLint("SetTextI18n")
    private fun updateUI(account: GoogleSignInAccount) { //Google
        FBMaskButton.visibility = View.INVISIBLE
        GLoginButton.text = "Logout"
        continueButton.visibility = View.VISIBLE
        greet.text = "Hello, " + account.givenName + ". Press the middle button below to continue."
        publisherId = account.id
        publisherDisplayName = account.displayName
    }
    @SuppressLint("SetTextI18n")
    private fun updateUI(account: Profile) { // Facebook
        FBMaskButton.text = "Logout"
        GLoginButton.visibility = View.INVISIBLE
        continueButton.visibility = View.VISIBLE
        greet.text = "Hello, " + account.firstName + ". Press the middle button below to continue."
        publisherId = account.id
        publisherDisplayName = account.firstName + " " + account.lastName

    }
    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        GLoginButton.text = "Google"
        FBMaskButton.text = "Facebook"
        continueButton.visibility = View.INVISIBLE
        FBMaskButton.visibility = View.VISIBLE
        GLoginButton.visibility = View.VISIBLE
        greet.text = "Log in to continue."
    }

    override fun setPresenter(_presenter: Contract.UserInfoPresenter) {
        presenter = _presenter
    }

    private fun getLatLong(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(GET_LAT_LONG, "Permissions not granted.")
            return false
        }
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                sharedPref.save("lat", it.latitude.toString())
                sharedPref.save("long", it.longitude.toString())
                Log.e(GET_LAT_LONG, "Successful fetch. Coordinates are: ${it.latitude} ${it.longitude}.")
            }
        }
        return true
    }
}

