package com.nvp293.cbc

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.FieldPosition

class ChessViewModel : ViewModel() {

    private var chessBoardList = MutableLiveData<List<ChessPiece>>().apply {
        value = mutableListOf<ChessPiece>()
    }

    private var selectedPiece = MutableLiveData<ChessPiece>().apply {
        value = null
    }

    fun observeChessBoardList() : LiveData<List<ChessPiece>> {
        return chessBoardList
    }

    fun observeSelectedPiece() : LiveData<ChessPiece> {
        return selectedPiece
    }

    fun squareSelected(position: Int) {

    }

    fun highlightSquare(position: Int) {
        Log.i("HighlightSquare", "$position")
        chessBoardList.value?.get(position)?.highlight = true
    }

    fun updateBoard(board : List<ChessPiece>) {
        Log.i("ChessViewModel", "Update Board")
        chessBoardList.postValue(board)
    }
}