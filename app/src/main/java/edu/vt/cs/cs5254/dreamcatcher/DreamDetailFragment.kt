package edu.vt.cs.cs5254.dreamcatcher

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import edu.vt.cs.cs5254.dreamcatcher.database.Dream
import kotlinx.android.synthetic.main.dream_detail_fragment.*


class DreamDetailFragment : Fragment() {

    companion object {
        fun newInstance() = DreamDetailFragment()
    }

    private lateinit var viewModel: DreamDetailViewModel
    private lateinit var dream: Dream
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var realizedCheckBox: CheckBox
    private lateinit var deferredCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dream = Dream()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dream_detail_fragment, container, false)
        titleField = view.findViewById(R.id.dream_title) as EditText
        dateButton = view.findViewById(R.id.dream_entry_1_button) as Button
        realizedCheckBox = view.findViewById(R.id.dream_realized) as CheckBox
        deferredCheckBox = view.findViewById(R.id.dream_deferred) as CheckBox

        dateButton.apply {
            text = dream.dateRevealed.toString()
            isEnabled = false
        }

        realizedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked -> dream.isRealized = isChecked }
        }

        deferredCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked -> dream.isDeferred = isChecked }
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DreamDetailViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                dream.description = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        titleField.addTextChangedListener(titleWatcher)
    }

}