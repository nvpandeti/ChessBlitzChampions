package com.nvp293.cbc

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
//Facebook
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.common.util.IOUtils.toByteArray
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Base64
import com.google.firebase.database.DatabaseReference
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class MainActivity : AppCompatActivity() {

    companion object {

        private const val RC_SIGN_IN = 123
        private const val RC_SETTINGS = 100
    }

    var matchMaker : FirebaseMatchMaker? = null
    var findingMatch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build())

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.LoginTheme)
                .setLogo(R.drawable.logo)
                .build(),
            RC_SIGN_IN)

        findMatchBut.setOnClickListener {
            if(!findingMatch) {
                matchMaker = FirebaseMatchMaker.newInstance (object : FirebaseMatchMaker.OnMatchMadeCallback {
                    override fun run(c: FirebaseMatchMaker) {
                        val findMatchIntent = Intent(applicationContext, ChessBoardActivity::class.java)
                        val myExtras = Bundle()
                        val user = FirebaseAuth.getInstance().currentUser
                        myExtras.putString("mGamePath", c.mGamePath)
                        myExtras.putBoolean("isWhite", c.isThisWhite())
                        findMatchIntent.putExtras(myExtras)
                        findingMatch = false
                        findMatchBut.text = "FIND MATCH"
                        val result = 1
                        startActivityForResult(findMatchIntent, result)
                    }
                })
                matchMaker?.findMatch()
                findMatchBut.text = "Searching"
                findingMatch = true
            } else {
                matchMaker?.stop()
                findMatchBut.text = "FIND MATCH"
                findingMatch = false
            }

        }

        settings.setOnClickListener {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(settingsIntent, RC_SETTINGS)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK && response != null) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                if(response.isNewUser) {
                    var userManager = FirebaseUserManager.newInstance(user?.uid!!)
                    userManager.createNewUser(user?.displayName!!)
                    userManager.getUser {
                        if(it != null)
                            userTV.text = getDisplayString(it)
                    }
                } else {
                    var userManager = FirebaseUserManager.newInstance(user?.uid!!)
                    userManager.getUser {
                        if(it != null)
                            userTV.text = getDisplayString(it)
                    }
                }
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        } else if(requestCode == RC_SETTINGS) {
            val user = FirebaseAuth.getInstance().currentUser
            var userManager = FirebaseUserManager.newInstance(user?.uid!!)
            userManager.getUser {
                if(it != null)
                    userTV.text = getDisplayString(it)
            }
        }
    }

    fun getDisplayString(user: User) : String{
        return "Welcome back, ${user.blitzId}\nELO: ${user.elo}\nWins: ${user.wins}"
    }

    override fun onStop() {
        if(matchMaker != null) {
            matchMaker?.stop()
        }
        super.onStop()
    }

    override fun onDestroy() {
        if(matchMaker != null) {
            matchMaker?.stop()
        }
        super.onDestroy()
    }
}
