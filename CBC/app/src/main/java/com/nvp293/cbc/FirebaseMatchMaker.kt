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

    val RANDOM_ROOM_ID = "/Globl"
    val ROOM_ID = "/GameRooms"
    val GAMES_RECORD = "/OpenGameMoves"

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

            mMatcher = Matcher(onMatchNotFoundFallback)
            mUserRoomRef.runTransaction(mMatcher!!)
        }).start()
    }

    fun stop() {
        if (mSelfChallengeManager == null || mSelfChallengeCanceller != null) {
            return
        }

        mSelfChallengeCanceller = SelfChallengeCanceller(mSelfChallengeManager)
        mUserRoomRef.runTransaction(mSelfChallengeCanceller!!)
    }

    fun newInstance(userRoom: String, onComplete: OnMatchMadeCallback): FirebaseMatchMaker {
        return FirebaseMatchMaker(
            FirebaseDatabase.getInstance().getReference("$ROOM_ID/$userRoom"), onComplete
        )
    }



    protected var mIsThisOpener: Boolean = false

    protected fun onMatchFound(isOpener: Boolean) {
        mIsThisOpener = isOpener
        mLocalPlayerIndex = if (isOpener) 1 else 0
        mOnComplete.run(this)
    }

    fun isThisOpener(): Boolean {
        return mIsThisOpener
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
            for (challengeData in rooms.getChildren()) {
                val postedChallenge = challengeData.getValue(Challenge::class.java)!!

                if (isChallengeCompat(postedChallenge)) {
                    mSelectedChallenge = postedChallenge
                    challengeData.setValue(null)
                    return Transaction.success(rooms)
                }
            }

            Log.d("MatchMaker.Matcher", "Didn't find any matching challenge")
            return Transaction.success(rooms)
        }

        override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
            if (mSelectedChallenge != null) {
                var mOpener = mSelectedChallenge?.opener;
                var mGamePath = mSelectedChallenge?.gameRef;
                Log.d("MatchMaker.Matcher", "Found match, onComplete");
                onMatchFound(false)
            } else if (onMatchNotFoundFallback != null) {
                onMatchNotFoundFallback.onFail()
            }
        }
    }

    inner class SelfChallengeManager : Transaction.Handler, ValueEventListener {

        protected var mUploadedChallenge: Challenge = Challenge(
            FirebaseAuth.getInstance().currentUser!!.uid, null
        )
        lateinit var mChallengeRef: DatabaseReference
        lateinit var mGameRecordRef: DatabaseReference

        override fun doTransaction(p0: MutableData): Transaction.Result {
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
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
    class Challenge(var opener : String?, var gameRef : String?)
}