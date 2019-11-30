package com.nvp293.cbc

import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.lifecycle.Observer
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.util.*

/**
 * Created by cody on 9/23/15.
 */
open class ChessGrid(var viewModel : ChessViewModel) {

    protected var boardArray = Array<Array<ChessPiece>>(8){i ->  Array<ChessPiece>(8) {j ->
        ChessPiece(ChessPieceSide.EMPTY, ChessPieceType.EMPTY, j, i)
    } }

    private var currentlySelectedPiece : ChessPiece? = null

    private var currentTurn : ChessPieceSide = ChessPieceSide.WHITE

    val width get() = 8
    val height get() = 8

    init{
        initChessBoard()
    }

    fun initChessBoard() {

        //Generate an empty grid
        for (i in 0..1) {
            for (j in 0..7) {
                boardArray[i][j].side = ChessPieceSide.BLACK
                boardArray[i][j].notYetMoved = true
            }
        }

        for (i in 6..7) {
            for (j in 0..7) {
                boardArray[i][j].side = ChessPieceSide.WHITE
                boardArray[i][j].notYetMoved = true
            }
        }

        for (i in listOf<Int>(1 , 6))
            for (j in 0..7)
                boardArray[i][j].type = ChessPieceType.PAWN

        for (i in listOf<Int>(0, 7)) {
            boardArray[i][0].type = ChessPieceType.ROOK
            boardArray[i][1].type = ChessPieceType.KNIGHT
            boardArray[i][2].type = ChessPieceType.BISHOP
            boardArray[i][3].type = ChessPieceType.QUEEN
            boardArray[i][4].type = ChessPieceType.KING
            boardArray[i][5].type = ChessPieceType.BISHOP
            boardArray[i][6].type = ChessPieceType.KNIGHT
            boardArray[i][7].type = ChessPieceType.ROOK
        }

        Log.i("init", "init chess board")

        updateViewModel()
    }

    fun updateViewModel() {
        viewModel.updateBoard(boardArray.flatten())
    }

    fun userClicked(position: Int) {
        var piece = getPieceAtIndex(position)
        Log.i("UserClick", "$position ${piece.side} ${piece.type}")

        if(currentlySelectedPiece == null) {
            if(piece.type != ChessPieceType.EMPTY && piece.side == currentTurn) {
                highlightPossibleMoves(piece)
                currentlySelectedPiece = piece
                updateViewModel()
            }
        } else {

            if(piece.highlight) {
                clearPossibleMoves()
                movePiece(currentlySelectedPiece?:throw Exception("currentlySelectedPiece == null"), piece)
                if (currentTurn == ChessPieceSide.WHITE) {
                    currentTurn = ChessPieceSide.BLACK
                } else {
                    currentTurn = ChessPieceSide.WHITE
                }
                currentlySelectedPiece = null
                updateViewModel()
            } else {

                if(piece.type != ChessPieceType.EMPTY && piece.side == currentTurn) {
                    clearPossibleMoves()
                    highlightPossibleMoves(piece)
                    currentlySelectedPiece = piece
                    updateViewModel()
                } else {
                    clearPossibleMoves()
                    currentlySelectedPiece = null
                    updateViewModel()
                }
            }
        }
    }

    fun clearPossibleMoves() {
        for (i in 0..7) {
            for(j in 0..7) {
                boardArray[i][j].highlight = false
            }
        }
    }

    fun highlightPossibleMoves(piece : ChessPiece) {
        var squaresToHighlight = mutableListOf<Int>()
        var checkPos = -1
        when(piece.type) {
            ChessPieceType.PAWN -> {
                if(piece.side == ChessPieceSide.WHITE) {
                    checkPos = checkIfEmpty(piece.yPosition - 1, piece.xPosition)
                    if(checkPos != -1) {
                        squaresToHighlight.add(checkPos)
                        if(piece.notYetMoved) {
                            checkPos = checkIfEmpty(piece.yPosition - 2, piece.xPosition)
                            if(checkPos != -1) {
                                squaresToHighlight.add(checkPos)
                            }
                        }
                    }
                    checkPos = checkIfEnemy(piece.yPosition - 1, piece.xPosition - 1, ChessPieceSide.BLACK)
                    if(checkPos != -1) {
                        squaresToHighlight.add(checkPos)
                    }

                    checkPos = checkIfEnemy(piece.yPosition - 1, piece.xPosition + 1, ChessPieceSide.BLACK)
                    if(checkPos != -1) {
                        squaresToHighlight.add(checkPos)
                    }

                } else if (piece.side == ChessPieceSide.BLACK) {
                    checkPos = checkIfEmpty(piece.yPosition + 1, piece.xPosition)
                    if(checkPos != -1) {
                        squaresToHighlight.add(checkPos)
                        if(piece.notYetMoved) {
                            checkPos = checkIfEmpty(piece.yPosition + 2, piece.xPosition)
                            if(checkPos != -1) {
                                squaresToHighlight.add(checkPos)
                            }
                        }
                    }
                    checkPos = checkIfEnemy(piece.yPosition + 1, piece.xPosition - 1, ChessPieceSide.WHITE)
                    if(checkPos != -1) {
                        squaresToHighlight.add(checkPos)
                    }

                    checkPos = checkIfEnemy(piece.yPosition + 1, piece.xPosition + 1, ChessPieceSide.WHITE)
                    if(checkPos != -1) {
                        squaresToHighlight.add(checkPos)
                    }
                }
            }
            ChessPieceType.ROOK -> {

            }
            ChessPieceType.KNIGHT -> {

            }
            ChessPieceType.BISHOP -> {

            }
            ChessPieceType.QUEEN -> {

            }
            ChessPieceType.KING -> {

            }
            ChessPieceType.EMPTY -> {

            }
        }

        for(i in squaresToHighlight) {
            getPieceAtIndex(i).highlight = true
        }
    }

    fun movePiece(oldSquare : ChessPiece, newSquare : ChessPiece) {
        newSquare.side = oldSquare.side
        newSquare.type = oldSquare.type
        oldSquare.empty()
    }

    fun getPieceAtIndex(i : Int) : ChessPiece {
        if(i < 0 || i >= 64) {
            throw IndexOutOfBoundsException("$i out of chess board bounds")
        }

        return boardArray[i / 8][i % 8]
    }

    fun checkBounds(yPos: Int, xPos: Int) : Boolean{
        return yPos in 0..7 && xPos in 0..7
    }

    fun checkIfEmpty(yPos: Int, xPos: Int) : Int{
        if(checkBounds(yPos, xPos)
            && boardArray[yPos][xPos].side.equals(ChessPieceSide.EMPTY)) {
            return yPos * 8 + xPos
        } else {
            return -1
        }
    }

    fun checkIfEnemy(yPos: Int, xPos: Int, enemySide: ChessPieceSide) : Int{
        if(checkBounds(yPos, xPos)
            && boardArray[yPos][xPos].side.equals(enemySide)) {
            return yPos * 8 + xPos
        } else {
            return -1
        }
    }
}
