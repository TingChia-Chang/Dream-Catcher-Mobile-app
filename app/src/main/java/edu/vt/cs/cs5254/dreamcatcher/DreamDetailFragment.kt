package edu.vt.cs.cs5254.dreamcatcher

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import edu.vt.cs.cs5254.dreamcatcher.database.Dream
import java.util.*
import androidx.lifecycle.Observer
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries

private const val TAG = "DreamDetailFragment"
private const val ARG_DREAM_ID = "dream_id"

class DreamDetailFragment : Fragment() {

    companion object {
        fun newInstance(dreamID: UUID): DreamDetailFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DREAM_ID, dreamID)
            }
            return DreamDetailFragment().apply {
                arguments = args
            }
        }
    }

    private lateinit var viewModel: DreamDetailViewModel

    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var realizedCheckBox: CheckBox
    private lateinit var deferredCheckBox: CheckBox
    private lateinit var dreamWithEntries: DreamWithEntries
    private val dreamDetailViewModel: DreamDetailViewModel by lazy {
        ViewModelProviders.of(this).get(DreamDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dreamID: UUID = arguments?.getSerializable(ARG_DREAM_ID) as UUID
        dreamDetailViewModel.loadDream(dreamID)

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



        realizedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                dreamWithEntries.dream.isRealized = isChecked
                deferredCheckBox.isEnabled = !dreamWithEntries.dream.isRealized
            }
        }

        deferredCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                dreamWithEntries.dream.isDeferred = isChecked
                realizedCheckBox.isEnabled = !dreamWithEntries.dream.isDeferred
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dreamDetailViewModel.dreamLiveData.observe(
            viewLifecycleOwner,
            Observer { dreamEntries ->
                dreamEntries?.let {
                    this.dreamWithEntries = it
                    updateUI()
                }

            }
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DreamDetailViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                dreamWithEntries.dream.description = s.toString()
                Log.d("Tina", s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        titleField.addTextChangedListener(titleWatcher)
    }

    override fun onStop() {
        super.onStop()
        dreamDetailViewModel.saveDreams(dreamWithEntries)
    }

    private fun updateUI() {
        titleField.setText(dreamWithEntries.dream.description)
        dateButton.text = dreamWithEntries.dream.dateRevealed.toString()
        dateButton.isEnabled = false


        realizedCheckBox.apply {
            isChecked = dreamWithEntries.dream.isRealized
            isEnabled = !dreamWithEntries.dream.isDeferred
            jumpDrawablesToCurrentState()
        }
        deferredCheckBox.apply {
            isChecked = dreamWithEntries.dream.isDeferred
            isEnabled = !dreamWithEntries.dream.isRealized
            jumpDrawablesToCurrentState()
        }
    }

}
