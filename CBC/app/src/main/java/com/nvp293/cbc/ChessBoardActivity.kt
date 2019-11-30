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
import kotlinx.android.synthetic.main.activity_chess_board.*
import java.lang.Exception

class ChessBoardActivity : AppCompatActivity() {

    private lateinit var viewModel : ChessViewModel
    private lateinit var chessAdapter : ChessAdapter
    private lateinit var chessGrid : ChessGrid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chess_board)

        /*
        var grid = ChessGrid(8, 8)
        var gridView = ChessGridView(applicationContext, 8, 8)
        gridView.setChessGrid(grid)
        boardFrame.addView(gridView)
        */

        viewModel = ViewModelProviders.of(this)[ChessViewModel::class.java]

        chessGrid = ChessGrid(viewModel)

        viewModel.observeChessBoardList().observe(this, Observer {
            var newList = ArrayList<ChessPiece>()
            for (p in it) {
                newList.add(ChessPiece(p.side, p.type, p.xPosition, p.yPosition, p.highlight, p.notYetMoved))
            }
            chessAdapter.submitList(newList)
        })

        Log.i("CreateView", "ChessBoardActivity")

        chessAdapter = ChessAdapter(this)
        recyclerView.adapter = chessAdapter
        recyclerView.layoutManager = GridLayoutManager(this, 8, GridLayoutManager.VERTICAL, false)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.isMotionEventSplittingEnabled = false

        chessAdapter.setChessGrid(chessGrid)

    }
}
