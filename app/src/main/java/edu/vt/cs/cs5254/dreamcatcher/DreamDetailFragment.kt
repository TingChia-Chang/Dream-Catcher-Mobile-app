package edu.vt.cs.cs5254.dreamcatcher

import android.graphics.Color
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntry
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntryKind
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries
import java.text.DateFormat

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
//    private lateinit var entry0_Button: Button
//    private lateinit var entry1_Button: Button
//    private lateinit var entry2_Button: Button
//    private lateinit var entry3_Button: Button
//    private lateinit var entry4_Button: Button
    private lateinit var realizedCheckBox: CheckBox
    private lateinit var deferredCheckBox: CheckBox
    private lateinit var dreamWithEntries: DreamWithEntries


    private lateinit var dreamRecyclerView: RecyclerView
    private var adapter: DreamDetailFragment.DreamEntryAdapter?= DreamEntryAdapter(emptyList())

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
//        entry0_Button = view.findViewById(R.id.dream_entry_0_button) as Button
//        entry1_Button = view.findViewById(R.id.dream_entry_1_button) as Button
//        entry2_Button = view.findViewById(R.id.dream_entry_2_button) as Button
//        entry3_Button = view.findViewById(R.id.dream_entry_3_button) as Button
//        entry4_Button = view.findViewById(R.id.dream_entry_4_button) as Button
        realizedCheckBox = view.findViewById(R.id.dream_realized) as CheckBox
        deferredCheckBox = view.findViewById(R.id.dream_deferred) as CheckBox


        dreamRecyclerView = view.findViewById(R.id.dream_entry_recycler_view) as RecyclerView
        dreamRecyclerView.layoutManager = LinearLayoutManager(context)
        dreamRecyclerView.adapter = adapter


//        entry1_Button.visibility = View.GONE
//        entry2_Button.visibility = View.GONE
//        entry3_Button.visibility = View.GONE



        realizedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
//                if (realizedCheckBox.isChecked){
//                    entry4_Button.visibility = View.VISIBLE
//                }

                dreamWithEntries.dream.isRealized = isChecked
                deferredCheckBox.isEnabled = !dreamWithEntries.dream.isRealized
                val temp = dreamWithEntries.dreamEntries.filter { it.kind == DreamEntryKind.DEFERRED }
                dreamWithEntries.dreamEntries = dreamWithEntries.dreamEntries - temp
                dreamWithEntries.dreamEntries = dreamWithEntries.dreamEntries + DreamEntry(dreamId = dreamWithEntries.dream.id, kind = DreamEntryKind.REALIZED, comment = "DREAM REALIZED")

//                if (realizedCheckBox.isChecked == false && deferredCheckBox.isChecked == false){
//                    entry4_Button.visibility = View.GONE
//                }
                dreamDetailViewModel.saveDreams(dreamWithEntries)

            }
        }

        deferredCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
//                if(deferredCheckBox.isChecked){
//                    entry4_Button.visibility = View.VISIBLE
//                }
                dreamWithEntries.dream.isDeferred = isChecked
                realizedCheckBox.isEnabled = !dreamWithEntries.dream.isDeferred
                val temp2 = dreamWithEntries.dreamEntries.filter { it.kind == DreamEntryKind.REALIZED }
                dreamWithEntries.dreamEntries = dreamWithEntries.dreamEntries - temp2
                dreamWithEntries.dreamEntries = dreamWithEntries.dreamEntries + DreamEntry(dreamId = dreamWithEntries.dream.id, kind = DreamEntryKind.DEFERRED, comment = "DREAM DEFERRED")
//                if (realizedCheckBox.isChecked == false && deferredCheckBox.isChecked == false){
//                    entry4_Button.visibility = View.GONE
//                }
                dreamDetailViewModel.saveDreams(dreamWithEntries)
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
//        var count = 0
//        for (entry in dreamWithEntries.dreamEntries){
//            if (entry.kind == DreamEntryKind.COMMENT){
//                when (count) {
//                    0 -> {
//                        entry1_Button.text = entry.comment + " ("+DateFormat.getDateInstance(android.icu.text.DateFormat.MEDIUM).format(this.dreamWithEntries.dream.dateRevealed)+")"
//                        entry1_Button.visibility = View.VISIBLE
//                    }
//                    1 -> {
//                        entry2_Button.text = entry.comment + " ("+DateFormat.getDateInstance(android.icu.text.DateFormat.MEDIUM).format(this.dreamWithEntries.dream.dateRevealed)+")"
//                        entry2_Button.visibility = View.VISIBLE
//                    }
//                    2 -> {
//                        entry3_Button.text = entry.comment + " ("+DateFormat.getDateInstance(android.icu.text.DateFormat.MEDIUM).format(this.dreamWithEntries.dream.dateRevealed)+")"
//                        entry3_Button.visibility = View.VISIBLE
//                    }
//                }
//                count += 1
//            }
//            else if (entry.kind == DreamEntryKind.REVEALED){
//                entry0_Button.text = "DREAM REVEALED"
//            }
//            else if (entry.kind == DreamEntryKind.REALIZED){
//                entry4_Button.text = entry.comment
//                entry4_Button.setBackgroundColor(Color.parseColor("#8DD33B"))
//            }
//            else if (entry.kind == DreamEntryKind.DEFERRED){
//                entry4_Button.text = entry.comment
//                entry4_Button.setBackgroundColor(Color.parseColor("#F06055"))
//            }
//
//
//        }



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

        adapter = DreamEntryAdapter(dreamWithEntries.dreamEntries)
        dreamRecyclerView.adapter = adapter
    }

    inner class DreamEntryHolder(view: View)
        :RecyclerView.ViewHolder(view) {


        private lateinit var dreamEntry: DreamEntry

        private val button: Button = itemView.findViewById(R.id.dream_entry_button)

        fun bind(dreamEntry: DreamEntry){
            this.dreamEntry = dreamEntry
            button.text = this.dreamEntry.comment
        }
    }

    private inner class DreamEntryAdapter(var dreamEntries: List<DreamEntry>)
        :RecyclerView.Adapter<DreamDetailFragment.DreamEntryHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : DreamEntryHolder {
            val view = layoutInflater.inflate(R.layout.list_item_dream_entry,parent,false)
            return DreamEntryHolder(view)
        }

        override fun getItemCount() = dreamEntries.size

        override fun onBindViewHolder(holder: DreamEntryHolder, position: Int) {
            val dreamEntry = dreamEntries[position]
            holder.bind(dreamEntry)
        }



    }

}
