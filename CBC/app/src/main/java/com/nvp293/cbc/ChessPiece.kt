package com.nvp293.cbc

enum class ChessPieceType {
    PAWN, ROOK, KNIGHT, QUEEN, KING, BISHOP, EMPTY
}

enum class ChessPieceSide { WHITE, BLACK, EMPTY}

data class ChessPiece(var side: ChessPieceSide, var type: ChessPieceType, var xPosition: Int = -1, var yPosition: Int = -1, var highlight: Boolean = false) {
    override fun toString(): String {
        return "$side $type($xPosition, $yPosition)"
    }
}