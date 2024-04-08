package com.bohdan2505.htmlziptoweb

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout

class TextInputDialog(context: Context) {

    private val context: Context = context
    private val editText = EditText(context)
    private val layout = LinearLayout(context)
    private var onTextEnteredListener: OnTextEnteredListener? = null

    init {
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(editText)
    }

    fun setOnTextEnteredListener(listener: OnTextEnteredListener) {
        onTextEnteredListener = listener
    }

    fun showDialog(title: String, positiveButtonLabel: String, negativeButtonLabel: String) {
        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setView(layout)
            .setPositiveButton(positiveButtonLabel) { _, _ ->
                val enteredText = editText.text.toString()
                onTextEnteredListener?.onTextEntered(enteredText)
            }
            .setNegativeButton(negativeButtonLabel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    interface OnTextEnteredListener {
        fun onTextEntered(enteredText: String)
    }
}
