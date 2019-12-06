package com.nvp293.cbc

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.MutableData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.auth.FirebaseAuth

class FirebaseMatchMaker private constructor(
    var mUserRoomRef: DatabaseReference,
    var mOnComplete : OnMatchMadeCallback){

    interface OnMatchMadeCallback {
        fun run(c: FirebaseMatchMaker)
    }



    private val mOwnChallengeRef: DatabaseReference? = null

    var mOpener: String? = null
    var mGamePath: String? = null
    var mLocalPlayerIndex: Int = 0
    private val mClosed = false

    fun isClosed(): Boolean {
        return mClosed
    }

    protected var mMatcher: Matcher? = null
    protected lateinit var mSelfChallengeManager: SelfChallengeManager
    protected var mSelfChallengeCanceller: SelfChallengeCanceller? = null

    fun findMatch() {
        Thread(Runnable {
            val onMatchNotFoundFallback = object : OnFailCallback {
                override fun onFail() {
                    mMatcher = null
                    mSelfChallengeManager = SelfChallengeManager()
                    mUserRoomRef.runTransaction(mSelfChallengeManager!!)
                }
            }
            Log.i("findMatch", "find")
            mMatcher = Matcher(onMatchNotFoundFallback)
            FirebaseDatabase.getInstance().getReference("$ROOM_ID").runTransaction(mMatcher!!)
        }).start()
    }

    fun stop() {
        if (mSelfChallengeManager == null || mSelfChallengeCanceller != null) {
            return
        }

        mSelfChallengeCanceller = SelfChallengeCanceller(mSelfChallengeManager)
        mUserRoomRef.runTransaction(mSelfChallengeCanceller!!)
    }

    companion object {
        val RANDOM_ROOM_ID = "/Globl"
        val ROOM_ID = "/GameRooms"
        val GAMES_RECORD = "/OpenGameMoves"
        fun newInstance(onComplete: OnMatchMadeCallback): FirebaseMatchMaker {
            var room = FirebaseDatabase.getInstance().getReference("$ROOM_ID").push()
            return FirebaseMatchMaker(
                FirebaseDatabase.getInstance().getReference("$ROOM_ID/${room.key}"), onComplete
            )
        }
    }




    protected var mIsThisWhite: Boolean = false

    protected fun onMatchFound(isWhite: Boolean) {
        Log.i("onMatchFound", "$isWhite")
        mIsThisWhite = isWhite
        mLocalPlayerIndex = if (isWhite) 1 else 0
        mOnComplete.run(this)
    }

    fun isThisWhite(): Boolean {
        return mIsThisWhite
    }

    interface OnFailCallback {
        fun onFail()
    }

    inner class Matcher(var onMatchNotFoundFallback : OnFailCallback) : Transaction.Handler {

        var mSelectedChallenge : Challenge? = null

        private fun isChallengeCompat(oppoChallenge: Challenge): Boolean {
            return true
        }

        override fun doTransaction(rooms: MutableData): Transaction.Result {
            Log.i("MatchMaker.Matcher do", "${rooms.key}")
            for (challenge in rooms.getChildren()) {
                if (challenge?.key?.equals("sad") != false) {
                    continue
                }
                for (challengeData in challenge.getChildren()) {
                    Log.i("MatchMaker.Matcher", "${challengeData.key}")

                    val postedChallenge = challengeData.getValue(Challenge::class.java)!!

                    if (isChallengeCompat(postedChallenge)) {
                        Log.i("MatchMaker", "Match Found")
                        mSelectedChallenge = postedChallenge
                        challengeData.setValue(null)
                        return Transaction.success(rooms)
                    }
                }
            }

            Log.i("MatchMaker.Matcher", "Didn't find any matching challenge")
            return Transaction.success(rooms)
        }

        override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
            if (mSelectedChallenge != null) {
                var mOpener = mSelectedChallenge?.opener
                mGamePath = mSelectedChallenge?.gameRef
                Log.i("MatchMaker on", "Found match, onComplete ${mSelectedChallenge?.gameRef}")
                onMatchFound(false)
            } else if (onMatchNotFoundFallback != null) {
                onMatchNotFoundFallback.onFail()
            }
        }
    }

    inner class SelfChallengeManager : Transaction.Handler, ValueEventListener {

        protected var mUploadedChallenge: Challenge = Challenge()
        lateinit var mChallengeRef: DatabaseReference
        lateinit var mGameRecordRef: DatabaseReference

        override fun doTransaction(p0: MutableData): Transaction.Result {
            mUploadedChallenge.opener = FirebaseAuth.getInstance().currentUser!!.uid
            mGameRecordRef = FirebaseDatabase.getInstance().getReference()
                .child(GAMES_RECORD)
                .push()
            mUploadedChallenge.gameRef =
                GAMES_RECORD + "/" + mGameRecordRef.getKey()
            mGamePath = mUploadedChallenge.gameRef

            mChallengeRef = mUserRoomRef.push()
            mChallengeRef.setValue(mUploadedChallenge)
            mChallengeRef.addValueEventListener(this)

            return Transaction.success(p0)
        }

        override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
            Log.i("SelfChallengeManager", "mUploadedChallenge")
        }
        override fun onDataChange(data: DataSnapshot) {
            if (data.getValue() == null) {
                onMatchFound(true)
            }
        }

        override fun onCancelled(error : DatabaseError) {
            Log.d("MatchMaker", "Cancelled: " + error.getMessage())
        }
    }

    inner class SelfChallengeCanceller(var mChallenger : SelfChallengeManager) : Transaction.Handler {
        override fun doTransaction(rooms: MutableData): Transaction.Result {
            mChallenger.mChallengeRef.removeEventListener(mChallenger)
            val challengeKey = mChallenger.mChallengeRef.getKey()!!

            for (challengeNode in rooms.getChildren()) {
                if (challengeNode.getKey()!!.contentEquals(challengeKey)) {
                    challengeNode.setValue(null)
                }
            }

            return Transaction.success(rooms)
        }

        override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
            val gameRecordRef = mChallenger.mGameRecordRef
            gameRecordRef.setValue(null)
        }
    }

    @IgnoreExtraProperties
    class Challenge() {
        var opener : String? = null
        var gameRef : String? = null
        @Exclude
        fun toMap() : Map<String, Any?> {
            return mapOf("opener" to opener, "gameRef" to gameRef)
        }
    }
}