package com.nvp293.cbc

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.lang.Exception

/**
 * Created by thunt on 9/21/16.
 */
open class ChessGridView : View {
    protected lateinit var grid: ChessGrid
    private var boarderWidth: Int = 0
    protected var cellSz: Float = 0.toFloat()
    protected var visibleRows: Int = 0
    protected var visibleColumns: Int = 0
    private var backgroundPaint = Paint()
    private var lightBkgColor = Color.parseColor("#D1A1A1")
    private var darkBkgColor = Color.parseColor("#225522")
    private var whiteDrawables = HashMap<ChessPieceType, Drawable>()
    private var blackDrawables = HashMap<ChessPieceType, Drawable>()
    private val visitor = DrawCellVisitor()

    private fun init(rows: Int = 5, cols: Int = 5) {
        visibleColumns = cols
        visibleRows = rows
        boarderWidth = 1

        var res = resources
        whiteDrawables[ChessPieceType.BISHOP] = res.getDrawable(R.drawable.ic_white_bishop, null)
        whiteDrawables[ChessPieceType.KING] = res.getDrawable(R.drawable.ic_white_king, null)
        whiteDrawables[ChessPieceType.KNIGHT] = res.getDrawable(R.drawable.ic_white_knight, null)
        whiteDrawables[ChessPieceType.PAWN] = res.getDrawable(R.drawable.ic_white_pawn, null)
        whiteDrawables[ChessPieceType.QUEEN] = res.getDrawable(R.drawable.ic_white_queen, null)
        whiteDrawables[ChessPieceType.ROOK] = res.getDrawable(R.drawable.ic_white_rook, null)

        blackDrawables[ChessPieceType.BISHOP] = res.getDrawable(R.drawable.ic_black_bishop, null)
        blackDrawables[ChessPieceType.KING] = res.getDrawable(R.drawable.ic_black_king, null)
        blackDrawables[ChessPieceType.KNIGHT] = res.getDrawable(R.drawable.ic_black_knight, null)
        blackDrawables[ChessPieceType.PAWN] = res.getDrawable(R.drawable.ic_black_pawn, null)
        blackDrawables[ChessPieceType.QUEEN] = res.getDrawable(R.drawable.ic_black_queen, null)
        blackDrawables[ChessPieceType.ROOK] = res.getDrawable(R.drawable.ic_black_rook, null)


    }

    private inner class DrawCellVisitor() : ChessGrid.CellVisitor {
        override fun visitCell(canvas: Canvas, paint: Paint, piece: ChessPiece?) {
            if(piece != null) {
                if((piece.xPosition + piece.yPosition) % 2 == 1 ) {
                    paint.color = lightBkgColor
                } else
                {
                    paint.color = darkBkgColor
                }
                val x = piece.xPosition * cellSz
                val y = piece.yPosition * cellSz
                canvas.drawRect(x, y, x + cellSz, y + cellSz, paint)

                var d : Drawable
                if(piece.side == ChessPieceSide.WHITE) {
                    d = whiteDrawables[piece.type] ?: throw Exception("${piece.side} ${piece.type} not found")
                } else {
                    d = blackDrawables[piece.type] ?: throw Exception("${piece.side} ${piece.type} not found")
                }
                d.setBounds(x.toInt(), y.toInt(), (x + cellSz).toInt(), (y + cellSz).toInt())
                d.draw(canvas)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // XXX: see write up
        cellSz = width/visibleColumns.toFloat()
        backgroundPaint.color = Color.WHITE
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), backgroundPaint)
        grid.visitCells(canvas, backgroundPaint, visitor)
    }

    fun setChessGrid(g : ChessGrid) {
        grid = g
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, rows: Int, columns: Int) : super(context) {
        init(rows, columns)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, rows: Int, columns: Int) : super(context, attrs) {
        init(rows, columns)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(
        context: Context, attrs: AttributeSet, defStyleAttr: Int,
        rows: Int, columns: Int
    ) : super(context, attrs, defStyleAttr) {
        init(rows, columns)
    }
}
