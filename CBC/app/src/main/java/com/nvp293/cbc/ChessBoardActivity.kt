package com.nvp293.cbc

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chess_board)

        /*
        var grid = ChessGrid(8, 8)
        var gridView = ChessGridView(applicationContext, 8, 8)
        gridView.setChessGrid(grid)
        boardFrame.addView(gridView)
        */

        chessAdapter = ChessAdapter(this)
        recyclerView.adapter = chessAdapter
        recyclerView.layoutManager = GridLayoutManager(this, 8, GridLayoutManager.VERTICAL, false)
        recyclerView.isNestedScrollingEnabled = false



    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        viewModel = ViewModelProviders.of(this)[ChessViewModel::class.java]

        viewModel.observeChessBoardList().observe(this, Observer {
            chessAdapter.submitList(it)
        })

        return super.onCreateView(name, context, attrs)
    }
}
