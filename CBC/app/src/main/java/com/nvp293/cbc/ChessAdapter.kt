package com.nvp293.cbc

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Toast


class ChessAdapter(private val mContext: Context,
                   private val board: Array<ChessPiece>) : BaseAdapter() {

    private var lightBkgColor = Color.parseColor("#D1A1A1")
    private var darkBkgColor = Color.parseColor("#22 5522")

    override fun getCount(): Int {
        return board.size
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    // 4
    override fun getItem(position: Int): ChessPiece? {
        return board[position]
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var piece = board[position]

        if(convertView == null) {
            val layoutInflater = LayoutInflater.from(mContext)
            var view = layoutInflater.inflate(R.layout.board_square, null)
            view.setOnClickListener{
                Toast.makeText(mContext, "position $position", Toast.LENGTH_SHORT).show()
            }
            var bkgIV = view.findViewById<ImageView>(R.id.square_background)
            var chessIV = view.findViewById<ImageView>(R.id.piece)

            var row = position / 8
            var col = position % 8
            if((row + col) % 2 == 0) {
                bkgIV.setBackgroundColor(darkBkgColor)
            } else {
                bkgIV.setBackgroundColor(lightBkgColor)
            }

            if(piece != null) {
                when(piece.side){
                    ChessPieceSide.WHITE -> {
                        when(piece.type) {
                            ChessPieceType.BISHOP -> chessIV.setImageResource(R.drawable.ic_white_bishop)
                            ChessPieceType.KING -> chessIV.setImageResource(R.drawable.ic_white_king)
                            ChessPieceType.KNIGHT -> chessIV.setImageResource(R.drawable.ic_white_knight)
                            ChessPieceType.PAWN -> chessIV.setImageResource(R.drawable.ic_white_pawn)
                            ChessPieceType.QUEEN -> chessIV.setImageResource(R.drawable.ic_white_queen)
                            ChessPieceType.ROOK -> chessIV.setImageResource(R.drawable.ic_white_rook)
                        }
                    }
                    ChessPieceSide.BLACK -> {
                        when(piece.type) {
                            ChessPieceType.BISHOP -> chessIV.setImageResource(R.drawable.ic_black_bishop)
                            ChessPieceType.KING -> chessIV.setImageResource(R.drawable.ic_black_king)
                            ChessPieceType.KNIGHT -> chessIV.setImageResource(R.drawable.ic_black_knight)
                            ChessPieceType.PAWN -> chessIV.setImageResource(R.drawable.ic_black_pawn)
                            ChessPieceType.QUEEN -> chessIV.setImageResource(R.drawable.ic_black_queen)
                            ChessPieceType.ROOK -> chessIV.setImageResource(R.drawable.ic_black_rook)
                        }
                    }
                }
            }

            return view
        }

        return convertView
    }

}