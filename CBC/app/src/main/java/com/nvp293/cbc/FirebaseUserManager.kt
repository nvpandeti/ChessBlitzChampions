package com.nvp293.cbc

import android.util.Log
import com.google.firebase.database.*
import java.util.*

class FirebaseUserManager private constructor(
    var mUsersRef: DatabaseReference){
    companion object {
        val USERS_ID = "/Users"

        fun newInstance(uid: String): FirebaseUserManager {
            return FirebaseUserManager(
                FirebaseDatabase.getInstance().getReference("$USERS_ID/$uid")
            )
        }
    }


    fun createNewUser(displayName : String) {
        mUsersRef.setValue(User(displayName))
    }

    fun updateUser(user : User) {
        mUsersRef.setValue(user)
    }

    fun getUser(userCallback: (User?) -> Unit) {
        val userListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                userCallback(user)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("getUser", "loadPost:onCancelled", databaseError.toException())
                // ...
                userCallback(null)
            }
        }
        mUsersRef.addListenerForSingleValueEvent(userListener)
    }

    interface OnGetUserCallback {
        fun run(u: User?)
    }

}
@IgnoreExtraProperties
data class User(var blitzId: String = "", var elo: Int = 1000, var wins: Int = 0) {
    @Exclude
    fun toMap() : Map<String, Any?> {
        return mapOf(
            "blitzId" to blitzId,
            "elo" to elo,
            "wins" to wins)
    }
}