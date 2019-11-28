package com.nvp293.cbc

import android.graphics.Canvas
import android.graphics.Paint
import java.util.ArrayList

/**
 * Created by cody on 9/23/15.
 */
open class ChessGrid(val columns: Int, val rows: Int) {

    protected var grid: ArrayList<ArrayList<ChessPiece?>> = ArrayList()

    val width get() = columns
    val height get() = rows

    //look for the first row that is full
    //returns -1 if not found
    val firstFullRow: Int
        get() {
            for (j in 0 until this.height) {
                var fullRow = true
                val r = this.grid[j]
                for (i in 0 until this.width) {
                    if (r[i] == null) {
                        fullRow = false
                        break
                    }
                }
                if (fullRow) {
                    return j
                }
            }
            return -1
        }

    interface CellVisitor {
        fun visitCell(canvas: Canvas, paint: Paint, cell: ChessPiece?)
    }

    init {
        //Generate an empty grid
        var row = ArrayList<ChessPiece?>()
        row.add(ChessPiece(ChessPieceSide.BLACK, ChessPieceType.ROOK, 0, 0))
        row.add(ChessPiece(ChessPieceSide.BLACK, ChessPieceType.KNIGHT, 1, 0))
        row.add(ChessPiece(ChessPieceSide.BLACK, ChessPieceType.BISHOP, 2, 0))
        row.add(ChessPiece(ChessPieceSide.BLACK, ChessPieceType.QUEEN, 3, 0))
        row.add(ChessPiece(ChessPieceSide.BLACK, ChessPieceType.KING, 4, 0))
        row.add(ChessPiece(ChessPieceSide.BLACK, ChessPieceType.BISHOP, 5, 0))
        row.add(ChessPiece(ChessPieceSide.BLACK, ChessPieceType.KNIGHT, 6, 0))
        row.add(ChessPiece(ChessPieceSide.BLACK, ChessPieceType.ROOK, 7, 0))
        this.grid.add(row)
        row = ArrayList<ChessPiece?>()
        for (j in 0 until this.width) {
            row.add(ChessPiece(ChessPieceSide.BLACK, ChessPieceType.PAWN, j, 1))
        }
        this.grid.add(row)
        for (i in 2 until this.height - 2) {
            val nextRow = ArrayList<ChessPiece?>()
            for (j in 0 until this.width) {
                nextRow.add(null)
            }
            this.grid.add(nextRow)
        }
        row = ArrayList<ChessPiece?>()
        for (j in 0 until this.width) {
            row.add(ChessPiece(ChessPieceSide.WHITE, ChessPieceType.PAWN, j, 6))
        }
        this.grid.add(row)
        row = ArrayList<ChessPiece?>()
        row.add(ChessPiece(ChessPieceSide.WHITE, ChessPieceType.ROOK, 0, 7))
        row.add(ChessPiece(ChessPieceSide.WHITE, ChessPieceType.KNIGHT, 1, 7))
        row.add(ChessPiece(ChessPieceSide.WHITE, ChessPieceType.BISHOP, 2, 7))
        row.add(ChessPiece(ChessPieceSide.WHITE, ChessPieceType.QUEEN, 3, 7))
        row.add(ChessPiece(ChessPieceSide.WHITE, ChessPieceType.KING, 4, 7))
        row.add(ChessPiece(ChessPieceSide.WHITE, ChessPieceType.BISHOP, 5, 7))
        row.add(ChessPiece(ChessPieceSide.WHITE, ChessPieceType.KNIGHT, 6, 7))
        row.add(ChessPiece(ChessPieceSide.WHITE, ChessPieceType.ROOK, 7, 7))
        this.grid.add(row)
    }

    fun visitCells(canvas: Canvas, paint: Paint, visitor: CellVisitor) {
        for (i in 0 until this.height) {
            for (j in 0 until this.width)
                visitor.visitCell(canvas, paint, grid[i][j])
        }
    }

    //return a pointer to the cell at a position (X,Y)
    fun getChessPieceAt(X: Int, Y: Int): ChessPiece? {
        return if (X < 0 || X >= this.width || Y < 0 || Y >= this.height) {
            null
        } else this.grid[Y][X]
    }

    //same as getCellAt(X,Y), except this function also removes it from the grid
    private fun extractChessPieceAt(X: Int, Y: Int): ChessPiece? {
        if (X < 0 || X >= this.width || Y < 0 || Y >= this.height) {
            return null
        }
        val result = this.grid[Y][X]
        this.grid[Y][X] = null
        return result
    }

    //insert a cell into the grid
    open fun putChessPiece(X: Int, Y: Int, cell: ChessPiece): Boolean {
        if (X < 0 || X >= this.width || Y < 0 || Y >= this.height) {
            return false
        }
        cell.xPosition = X
        cell.yPosition = Y
        this.grid[Y][X] = cell
        return true
    }

    //remove a cell from the grid
    fun removeChessPiece(X: Int, Y: Int): Boolean {
        if (X < 0 || X >= this.width || Y < 0 || Y >= this.height) {
            return false
        }
        val theCell = this.grid[Y][X]
        if (theCell != null) {
            this.grid[Y][X] = null
            theCell.yPosition = -1
            theCell.xPosition = -1
        }
        return true
    }


    //delete all cells
    fun clear() {
        for (i in 0 until this.height) {
            val r = this.grid[i]
            for (j in 0 until this.width) {
                r[j] = null
            }
        }
    }
}
