package com.nvp293.cbc

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.hide()

        var name = FirebaseAuth.getInstance().currentUser?.displayName

        displayNameET.setText(name, TextView.BufferType.EDITABLE)

        setDisplayName.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            val displayName = displayNameET.text.toString()
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user?.updateProfile(profileUpdates)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("update", "User profile updated.")
                        var userManager = FirebaseUserManager.newInstance(user?.uid!!)
                        userManager.getUser {
                            if(it != null) {
                                it.blitzId = displayName
                                userManager.updateUser(it)
                            }
                        }
                        Toast.makeText(this, "BlitzID updated to $displayName", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        signOutBut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
            System.exit(0)
        }
    }

}
