package com.nvp293.cbc

import android.util.Log
import com.google.firebase.database.*
import java.util.*

class FirebaseGameSynchronizer(
    private val mGameRecord: DatabaseReference,
    private var mMessageModulator: Modulator?
) : ChildEventListener {
    private var mMoveIndex: Int = 0

    private var mSelfMoveSoph: Int = 0
    private val mSynced: Boolean = false

    private val mUnsyncBuffer = ArrayDeque<String>()

    internal var isSynced = false

    private val mMovesRecordList: DatabaseReference

    private fun resyncAll() {
        while (!mUnsyncBuffer.isEmpty())
            mMessageModulator!!.onReceiveMove(true, mUnsyncBuffer.pop())
    }

    init {
        mMoveIndex = 0
        mSelfMoveSoph = 0
        mMovesRecordList = mGameRecord.child("/M")
        mMovesRecordList.addChildEventListener(this)
    }

    fun moveCount(): Int {
        return mMoveIndex
    }

    fun recordPath(): String {
        return mMovesRecordList.toString()
    }

    fun attachModulator(modulator: Modulator) {
        mMessageModulator = modulator
    }

    fun detachModulator() {
        mMessageModulator = null
    }

    fun startSync() {
        if (!isSynced) {
            resyncAll()
            isSynced = true
            return
        }
    }

    fun stopSync() {
        isSynced = false
    }

    fun flush() {
        mMovesRecordList.removeEventListener(this)
    }

    fun sendMoveMsg(moveValue: String) {
        ++mSelfMoveSoph
        mMovesRecordList.child("/M" + mMoveIndex)
            .setValue(moveValue)
    }

    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
        if (mSelfMoveSoph > 0) {
            --mSelfMoveSoph
            ++mMoveIndex
            return
        }

        if (!isSynced && dataSnapshot.key?.get(0) === 'M') {
            mUnsyncBuffer.add(dataSnapshot.getValue(String::class.java))
            ++mMoveIndex
            return
        }

        Log.d("FirebaseGameSync", "Real-sync")
        mMessageModulator!!.onReceiveMove(false, dataSnapshot.value as String)
        ++mMoveIndex
    }

    override fun onCancelled(p0: DatabaseError) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChildChanged(p0: DataSnapshot, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChildRemoved(p0: DataSnapshot) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    interface Modulator {
        fun onReceiveMove(isSyncingPast: Boolean, encodedMsg: String)
    }

    fun createUser(side : ChessPieceSide, uid : String) {
        mGameRecord.child("${side}User")
            .setValue(uid)
    }

    fun getUserUid(side : ChessPieceSide, userUidCallback: (String?) -> Unit) {
        val userListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val uid = dataSnapshot.getValue(String::class.java)
                userUidCallback(uid)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("getUserUid", "loadPost:onCancelled", databaseError.toException())
                // ...
                userUidCallback(null)
            }
        }
        mGameRecord.child("${side}User").addListenerForSingleValueEvent(userListener)
    }

    companion object {

        fun newInstance(
            mGameRecord: String,
            modulator: Modulator
        ): FirebaseGameSynchronizer {
            return FirebaseGameSynchronizer(
                FirebaseDatabase.getInstance()
                    .getReference(mGameRecord), modulator
            )
        }
    }
}