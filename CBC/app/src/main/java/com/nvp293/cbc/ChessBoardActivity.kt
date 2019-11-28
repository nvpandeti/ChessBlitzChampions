package com.nvp293.cbc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_chess_board.*
import java.lang.Exception

class ChessBoardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chess_board)

        /*
        var grid = ChessGrid(8, 8)
        var gridView = ChessGridView(applicationContext, 8, 8)
        gridView.setChessGrid(grid)
        boardFrame.addView(gridView)
        */

        var chessBoardArray = initChessBoard()
        var chessAdapter = ChessAdapter(this, chessBoardArray)
        gridview.adapter = chessAdapter

    }

    fun initChessBoard() : Array<ChessPiece> {

        var boardArray = Array<ChessPiece?>(64){null}
        var index = 0

        //Generate an empty grid
        boardArray[index++] = ChessPiece(ChessPieceSide.BLACK, ChessPieceType.ROOK, 0, 0)
        boardArray[index++] = ChessPiece(ChessPieceSide.BLACK, ChessPieceType.KNIGHT, 1, 0)
        boardArray[index++] = ChessPiece(ChessPieceSide.BLACK, ChessPieceType.BISHOP, 2, 0)
        boardArray[index++] = ChessPiece(ChessPieceSide.BLACK, ChessPieceType.QUEEN, 3, 0)
        boardArray[index++] = ChessPiece(ChessPieceSide.BLACK, ChessPieceType.KING, 4, 0)
        boardArray[index++] = ChessPiece(ChessPieceSide.BLACK, ChessPieceType.BISHOP, 5, 0)
        boardArray[index++] = ChessPiece(ChessPieceSide.BLACK, ChessPieceType.KNIGHT, 6, 0)
        boardArray[index++] = ChessPiece(ChessPieceSide.BLACK, ChessPieceType.ROOK, 7, 0)

        for (j in 0 until 8) {
            boardArray[index++] = ChessPiece(ChessPieceSide.BLACK, ChessPieceType.PAWN, j, 1)
        }

        for (i in 2 until 6) {
            val nextRow = ArrayList<ChessPiece?>()
            for (j in 0 until 8) {
                boardArray[index++] = ChessPiece(ChessPieceSide.EMPTY, ChessPieceType.EMPTY, j, i)
            }
        }

        for (j in 0 until 8) {
            boardArray[index++] = ChessPiece(ChessPieceSide.WHITE, ChessPieceType.PAWN, j, 6)
        }

        boardArray[index++] = ChessPiece(ChessPieceSide.WHITE, ChessPieceType.ROOK, 0, 7)
        boardArray[index++] = ChessPiece(ChessPieceSide.WHITE, ChessPieceType.KNIGHT, 1, 7)
        boardArray[index++] = ChessPiece(ChessPieceSide.WHITE, ChessPieceType.BISHOP, 2, 7)
        boardArray[index++] = ChessPiece(ChessPieceSide.WHITE, ChessPieceType.QUEEN, 3, 7)
        boardArray[index++] = ChessPiece(ChessPieceSide.WHITE, ChessPieceType.KING, 4, 7)
        boardArray[index++] = ChessPiece(ChessPieceSide.WHITE, ChessPieceType.BISHOP, 5, 7)
        boardArray[index++] = ChessPiece(ChessPieceSide.WHITE, ChessPieceType.KNIGHT, 6, 7)
        boardArray[index++] = ChessPiece(ChessPieceSide.WHITE, ChessPieceType.ROOK, 7, 7)

        return Array<ChessPiece>(64) {i -> boardArray[i]?:ChessPiece(ChessPieceSide.EMPTY, ChessPieceType.EMPTY, i % 8, i / 8)}
    }
}
