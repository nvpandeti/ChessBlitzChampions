package com.nvp293.cbc

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.lang.Exception
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class ChessAdapter(private val mContext: Context)// : ArrayAdapter<ChessPiece>() {
    : ListAdapter<ChessPiece, ChessAdapter.VH>(PieceDiff()) {

    private lateinit var chessGrid : ChessGrid

    fun setChessGrid(cGrid: ChessGrid) {
        chessGrid = cGrid
    }

    class PieceDiff : DiffUtil.ItemCallback<ChessPiece>() {

        override fun areItemsTheSame(oldItem: ChessPiece, newItem: ChessPiece): Boolean {
            return oldItem.xPosition == newItem.xPosition
                    && oldItem.yPosition == newItem.yPosition
        }

        override fun areContentsTheSame(oldItem: ChessPiece, newItem: ChessPiece): Boolean {
            return oldItem.side == newItem.side
                    && oldItem.type == newItem.type
                    && oldItem.xPosition == newItem.xPosition
                    && oldItem.yPosition == newItem.yPosition
                    && oldItem.highlight == newItem.highlight
        }
    }

    inner class VH(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        private var bkgIV = itemView.findViewById<ImageView>(R.id.square_background)
        private var chessIV = itemView.findViewById<ImageView>(R.id.piece)
        private var row = -1
        private var col = -1

        init {
            bkgIV.setOnClickListener {
                if(row >= 0) {
                    chessGrid.userClicked(row, col)
                    notifyDataSetChanged()
                }
            }
        }

        fun bind(piece : ChessPiece) {
            row = piece?.yPosition
            col = piece?.xPosition
            Log.i("bind", "pos $col $row ${piece?.side} ${piece?.type}")

            if((row + col) % 2 == 0) {
                if(piece?.highlight == true) {
                    bkgIV.setBackgroundColor(mContext.getColor(R.color.darkSquareHighlight))
                }
                else {
                    bkgIV.setBackgroundColor(mContext.getColor(R.color.darkSquare))
                }
            } else {
                if(piece?.highlight == true) {
                    bkgIV.setBackgroundColor(mContext.getColor(R.color.lightSquareHighlight))
                }
                else {
                    bkgIV.setBackgroundColor(mContext.getColor(R.color.lightSquare))
                }
            }

            if(piece != null) {
                //Log.i("bind2", "pos $col $row ${piece?.side} ${piece?.type}")
                when(piece.side){
                    ChessPieceSide.WHITE -> {
                        when(piece.type) {
                            ChessPieceType.BISHOP -> chessIV.setImageResource(R.drawable.ic_white_bishop)
                            ChessPieceType.KING -> chessIV.setImageResource(R.drawable.ic_white_king)
                            ChessPieceType.KNIGHT -> chessIV.setImageResource(R.drawable.ic_white_knight)
                            ChessPieceType.PAWN -> chessIV.setImageResource(R.drawable.ic_white_pawn)
                            ChessPieceType.QUEEN -> chessIV.setImageResource(R.drawable.ic_white_queen)
                            ChessPieceType.ROOK -> chessIV.setImageResource(R.drawable.ic_white_rook)
                            else -> {}
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
                            else -> {}
                        }
                    }
                    else -> {}
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(mContext)
            .inflate(R.layout.board_square, parent, false)
        var holder = VH(itemView)
        holder.setIsRecyclable(false)
        return holder
        //return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position)?: throw Exception())
    }
}