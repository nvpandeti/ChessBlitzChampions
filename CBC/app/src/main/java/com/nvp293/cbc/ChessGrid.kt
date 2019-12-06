package com.nvp293.cbc

import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.lifecycle.Observer
import kotlinx.coroutines.processNextEventInCurrentThread
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.lang.Math.abs
import java.util.*

/**
 * Created by cody on 9/23/15.
 */
open class ChessGrid(var viewModel : ChessViewModel, private var currentTurn : ChessPieceSide, var mySide : ChessPieceSide, var gameSynchronizer: FirebaseGameSynchronizer?) : FirebaseGameSynchronizer.Modulator{

    protected var boardArray = Array<Array<ChessPiece>>(8){i ->  Array<ChessPiece>(8) {j ->
        ChessPiece(ChessPieceSide.EMPTY, ChessPieceType.EMPTY, j, i)
    } }

    private var currentlySelectedPiece : ChessPiece? = null

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

    override fun onReceiveMove(isSyncingPast : Boolean, moveString : String) {
        var list = moveString.split(",")
        movePiece(boardArray[list[0].toInt()][list[1].toInt()], boardArray[list[2].toInt()][list[3].toInt()])
        var millis = list[4].toLong()
        if (currentTurn == ChessPieceSide.WHITE) {
            currentTurn = ChessPieceSide.BLACK
        } else {
            currentTurn = ChessPieceSide.WHITE
        }
        currentlySelectedPiece = null
        clearPossibleMoves()
        if(!isSyncingPast) {
            //TODO
            updateViewModel()
        }
    }

    fun userClicked(row: Int, col: Int) {
        var piece = boardArray[row][col]
        Log.i("UserClick", "$col $row ${piece.side} ${piece.type}")

        if(currentlySelectedPiece == null) {
            if(piece.type != ChessPieceType.EMPTY && piece.side == currentTurn && currentTurn == mySide) {
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
                gameSynchronizer?.sendMoveMsg("${currentlySelectedPiece?.xPosition},${currentlySelectedPiece?.yPosition},${piece.xPosition}, ${piece.yPosition}, 345")
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
        var squaresToHighlight = mutableListOf<ChessPiece>()
        var checkPiece : ChessPiece? = null
        when(piece.type) {
            ChessPieceType.PAWN -> {
                if(piece.side == ChessPieceSide.WHITE) {
                    checkPiece = checkIfSide(piece.yPosition - 1, piece.xPosition, ChessPieceSide.EMPTY)
                    if(checkPiece != null) {
                        squaresToHighlight.add(checkPiece)
                        if(piece.notYetMoved) {
                            checkPiece = checkIfSide(piece.yPosition - 2, piece.xPosition, ChessPieceSide.EMPTY)
                            if(checkPiece != null) {
                                squaresToHighlight.add(checkPiece)
                            }
                        }
                    }
                    checkPiece = checkIfSide(piece.yPosition - 1, piece.xPosition - 1, ChessPieceSide.BLACK)
                    if(checkPiece != null) {
                        squaresToHighlight.add(checkPiece)
                    }

                    checkPiece = checkIfSide(piece.yPosition - 1, piece.xPosition + 1, ChessPieceSide.BLACK)
                    if(checkPiece != null) {
                        squaresToHighlight.add(checkPiece)
                    }

                } else if (piece.side == ChessPieceSide.BLACK) {
                    checkPiece = checkIfSide(piece.yPosition + 1, piece.xPosition, ChessPieceSide.EMPTY)
                    if(checkPiece != null) {
                        squaresToHighlight.add(checkPiece)
                        if(piece.notYetMoved) {
                            checkPiece = checkIfSide(piece.yPosition + 2, piece.xPosition, ChessPieceSide.EMPTY)
                            if(checkPiece != null) {
                                squaresToHighlight.add(checkPiece)
                            }
                        }
                    }
                    checkPiece = checkIfSide(piece.yPosition + 1, piece.xPosition - 1, ChessPieceSide.WHITE)
                    if(checkPiece != null) {
                        squaresToHighlight.add(checkPiece)
                    }

                    checkPiece = checkIfSide(piece.yPosition + 1, piece.xPosition + 1, ChessPieceSide.WHITE)
                    if(checkPiece != null) {
                        squaresToHighlight.add(checkPiece)
                    }
                }
            }
            ChessPieceType.ROOK -> {

                var dirs = listOf<Pair<Int, Int>>(Pair(0,1),Pair(0,-1),Pair(1,0),Pair(-1,0))

                var enemySide = if(piece.side == ChessPieceSide.WHITE) ChessPieceSide.BLACK else ChessPieceSide.WHITE

                for(dir in dirs) {
                    var dist = 1
                    loop@ while (true) {
                        var y = piece.yPosition + dist * dir.first
                        var x = piece.xPosition + dist * dir.second

                        if(checkBounds(y, x)) {
                            checkPiece = boardArray[y][x]
                            when( checkPiece.side) {
                                enemySide -> {
                                    squaresToHighlight.add(checkPiece)
                                    break@loop
                                }
                                piece.side -> {
                                    break@loop
                                }
                                ChessPieceSide.EMPTY -> {
                                    squaresToHighlight.add(checkPiece)
                                }
                            }
                        } else {
                            break
                        }
                        dist += 1
                    }
                }
            }
            ChessPieceType.KNIGHT -> {
                var dirs = listOf<Pair<Int, Int>>(Pair(2,1),Pair(2,-1),Pair(1,2),Pair(-1,2), Pair(-2,1),Pair(-2,-1),Pair(1,-2),Pair(-1,-2))

                var enemySide = if(piece.side == ChessPieceSide.WHITE) ChessPieceSide.BLACK else ChessPieceSide.WHITE

                for(dir in dirs) {
                    var dist = 1
                    loop@ while (dist == 1) {
                        var y = piece.yPosition + dist * dir.first
                        var x = piece.xPosition + dist * dir.second

                        if(checkBounds(y, x)) {
                            checkPiece = boardArray[y][x]
                            when( checkPiece.side) {
                                enemySide -> {
                                    squaresToHighlight.add(checkPiece)
                                    break@loop
                                }
                                piece.side -> {
                                    break@loop
                                }
                                ChessPieceSide.EMPTY -> {
                                    squaresToHighlight.add(checkPiece)
                                }
                            }
                        } else {
                            break
                        }
                        dist += 1
                    }
                }
            }
            ChessPieceType.BISHOP -> {

                var dirs = listOf<Pair<Int, Int>>(Pair(1,1),Pair(-1,-1),Pair(1,-1),Pair(-1,1))

                var enemySide = if(piece.side == ChessPieceSide.WHITE) ChessPieceSide.BLACK else ChessPieceSide.WHITE

                for(dir in dirs) {
                    var dist = 1
                    loop@ while (true) {
                        var y = piece.yPosition + dist * dir.first
                        var x = piece.xPosition + dist * dir.second
                        if(checkBounds(y, x)) {
                            checkPiece = boardArray[y][x]
                            when( checkPiece.side) {
                                enemySide -> {
                                    squaresToHighlight.add(checkPiece)
                                    break@loop
                                }
                                piece.side -> {
                                    break@loop
                                }
                                ChessPieceSide.EMPTY -> {
                                    squaresToHighlight.add(checkPiece)
                                }
                            }
                        } else {
                            break
                        }
                        dist += 1
                    }
                }

            }
            ChessPieceType.QUEEN -> {

                var dirs = listOf<Pair<Int, Int>>(Pair(0,1),Pair(0,-1),Pair(1,0),Pair(-1,0), Pair(1,1),Pair(-1,-1),Pair(1,-1),Pair(-1,1))

                var enemySide = if(piece.side == ChessPieceSide.WHITE) ChessPieceSide.BLACK else ChessPieceSide.WHITE

                for(dir in dirs) {
                    var dist = 1
                    loop@ while (true) {
                        var y = piece.yPosition + dist * dir.first
                        var x = piece.xPosition + dist * dir.second

                        if(checkBounds(y, x)) {
                            checkPiece = boardArray[y][x]
                            when( checkPiece.side) {
                                enemySide -> {
                                    squaresToHighlight.add(checkPiece)
                                    break@loop
                                }
                                piece.side -> {
                                    break@loop
                                }
                                ChessPieceSide.EMPTY -> {
                                    squaresToHighlight.add(checkPiece)
                                }
                            }
                        } else {
                            break
                        }
                        dist += 1
                    }
                }

            }
            ChessPieceType.KING -> {

                var dirs = listOf<Pair<Int, Int>>(Pair(0,1),Pair(0,-1),Pair(1,0),Pair(-1,0), Pair(1,1),Pair(-1,-1),Pair(1,-1),Pair(-1,1))

                var enemySide = if(piece.side == ChessPieceSide.WHITE) ChessPieceSide.BLACK else ChessPieceSide.WHITE

                for(dir in dirs) {
                    var dist = 1
                    loop@ while (dist == 1) {
                        //Log.i("BISHOP", squaresToHighlight.joinToString(prefix = "[", postfix = "]") { it.toString() })
                        var y = piece.yPosition + dist * dir.first
                        var x = piece.xPosition + dist * dir.second

                        if(checkBounds(y, x)) {
                            checkPiece = boardArray[y][x]
                            when( checkPiece.side) {
                                enemySide -> {
                                    squaresToHighlight.add(checkPiece)
                                    break@loop
                                }
                                piece.side -> {
                                    break@loop
                                }
                                ChessPieceSide.EMPTY -> {
                                    squaresToHighlight.add(checkPiece)
                                }
                            }
                        } else {
                            break
                        }
                        dist += 1
                    }
                }

                if(piece.notYetMoved && boardArray[piece.yPosition][0].notYetMoved) {
                    var spacesOpen = true
                    for(i in 1 until piece.xPosition) {
                        if(boardArray[piece.yPosition][i].side != ChessPieceSide.EMPTY)
                            spacesOpen = false
                    }
                    checkPiece = boardArray[piece.yPosition][piece.xPosition - 2]
                    if(spacesOpen)
                        squaresToHighlight.add(checkPiece)
                }
                if(piece.notYetMoved && boardArray[piece.yPosition][7].notYetMoved) {
                    var spacesOpen = true
                    for(i in (piece.xPosition + 1) until 7) {
                        if(boardArray[piece.yPosition][i].side != ChessPieceSide.EMPTY)
                            spacesOpen = false
                    }
                    checkPiece = boardArray[piece.yPosition][piece.xPosition + 2]
                    if(spacesOpen)
                        squaresToHighlight.add(checkPiece)
                }

            }
            ChessPieceType.EMPTY -> {

            }
        }

        for(i in squaresToHighlight) {
            i.highlight = true
        }
    }

    fun movePiece(oldSquare : ChessPiece, newSquare : ChessPiece) {
        if(newSquare.type == ChessPieceType.KING) {
            newSquare.side = oldSquare.side
            newSquare.type = oldSquare.type
            oldSquare.empty()
            oldSquare.notYetMoved = false
            //gameEnd()
        }

        if(oldSquare.type == ChessPieceType.KING && abs(oldSquare.xPosition - newSquare.xPosition) == 2) {
            if(newSquare.xPosition == 6)
                movePiece(boardArray[oldSquare.yPosition][7], boardArray[oldSquare.yPosition][5])
            else {
                movePiece(boardArray[oldSquare.yPosition][0], boardArray[oldSquare.yPosition][3])
            }
        }
        newSquare.side = oldSquare.side
        newSquare.type = oldSquare.type
        oldSquare.empty()
        oldSquare.notYetMoved = false

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


    fun checkIfSide(yPos: Int, xPos: Int, enemySide: ChessPieceSide) : ChessPiece?{
        if(checkBounds(yPos, xPos)
            && boardArray[yPos][xPos].side.equals(enemySide)) {
            return boardArray[yPos][xPos]
        } else {
            return null
        }
    }
}
