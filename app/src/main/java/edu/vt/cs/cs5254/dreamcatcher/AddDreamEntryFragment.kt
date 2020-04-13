package edu.vt.cs.cs5254.dreamcatcher


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class AddDreamEntryFragment : DialogFragment() {
    lateinit var commentText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialoglayout = LayoutInflater.from(context).inflate(R.layout.dialog_add_comment, null)
        commentText = dialoglayout.findViewById(R.id.comment_text)

        return AlertDialog.Builder(activity)
            .setTitle("Add Comment")
            .setView(dialoglayout)
            .setPositiveButton(android.R.string.ok){ dialog, which ->  }
            .setNegativeButton(android.R.string.cancel){ dialog, which ->  }
            .create()
        
    }


    companion object {

        @JvmStatic
        fun newInstance() =
            AddDreamEntryFragment()
    }
}
