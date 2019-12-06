package com.nvp293.cbc

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.GridLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_chess_board.*
import kotlinx.coroutines.delay
import java.lang.Exception

class ChessBoardActivity : AppCompatActivity() {

    private lateinit var viewModel : ChessViewModel
    private lateinit var chessAdapter : ChessAdapter
    private lateinit var chessGrid : ChessGrid
    private lateinit var gameSynchronizer: FirebaseGameSynchronizer
    private lateinit var TimerOppo : Timer
    private lateinit var TimerMe : Timer
    private var isWhite : Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chess_board)
        supportActionBar?.hide()

        var bundle = intent.extras

        isWhite = bundle?.getBoolean("isWhite")?: throw Exception("isWhite is null")
        var mGamePath = bundle?.getString("mGamePath")?: throw Exception("mGamePath is null")

        viewModel = ViewModelProviders.of(this)[ChessViewModel::class.java]

        gameSynchronizer = FirebaseGameSynchronizer.newInstance(mGamePath, ChessGrid(viewModel, ChessPieceSide.WHITE, ChessPieceSide.WHITE, null))

        if(isWhite) {
            chessGrid = ChessGrid(viewModel, ChessPieceSide.WHITE, ChessPieceSide.WHITE, gameSynchronizer)
        } else {
            chessGrid = ChessGrid(viewModel, ChessPieceSide.WHITE, ChessPieceSide.BLACK, gameSynchronizer)
        }

        gameSynchronizer.attachModulator(chessGrid)

        viewModel.observeChessBoardList().observe(this, Observer {
            if(isWhite) {
                chessAdapter.submitList(it)
            } else {
                chessAdapter.submitList(it.reversed())
            }
        })

        Log.i("CreateView", "ChessBoardActivity")

        chessAdapter = ChessAdapter(this)
        recyclerView.adapter = chessAdapter
        recyclerView.layoutManager = GridLayoutManager(this, 8, GridLayoutManager.VERTICAL, false)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.isMotionEventSplittingEnabled = false

        chessAdapter.setChessGrid(chessGrid)


        var user = FirebaseAuth.getInstance().currentUser!!
        if(isWhite) {
            gameSynchronizer.createUser(ChessPieceSide.WHITE, user.uid)
        } else {
            gameSynchronizer.createUser(ChessPieceSide.BLACK, user.uid)
        }

        gameSynchronizer.startSync()

        newGame()
    }

    fun newGame() {
        //var timerOppo = Timer(button)
        //var timer = Timer(button)
        gameSynchronizer.getUserUid(ChessPieceSide.WHITE) { uid ->
            if(uid != null) {
                var userManager = FirebaseUserManager.newInstance(uid)
                userManager.getUser {
                    if(it != null) {
                        if(isWhite) {
                            tvMe.text = "${it.blitzId} ELO: ${it.elo}"
                            TimerMe = Timer(timerMe)
                        } else {
                            tvOppo.text = "${it.blitzId} ELO: ${it.elo}"
                            TimerOppo = Timer(timerOppo)
                        }
                    }
                }
            }
        }
        gameSynchronizer.getUserUid(ChessPieceSide.BLACK) { uid ->
            if(uid != null) {
                var userManager = FirebaseUserManager.newInstance(uid)
                userManager.getUser {
                    if(it != null) {
                        if(!isWhite) {
                            tvMe.text = "${it.blitzId} ELO: ${it.elo}"
                            TimerMe = Timer(timerMe)
                        } else {
                            tvOppo.text = "${it.blitzId} ELO: ${it.elo}"
                            TimerOppo = Timer(timerOppo)
                        }
                    }
                }
            }
        }

/*
        launch {
            val timerJob = async {
                timer.timerCo(durationMillis)
            }
            words.playRound(numWords) {
                doScore(timer.millisLeft())
                launch{timerJob.cancelAndJoin()}
            }
        }*/
    }

    override fun onStop() {
        gameSynchronizer.stopSync()
        super.onStop()
    }

    override fun onResume() {
        gameSynchronizer.startSync()
        super.onResume()
    }

    override fun onDestroy() {
        gameSynchronizer.flush()
        gameSynchronizer.detachModulator()
        super.onDestroy()
    }
}
