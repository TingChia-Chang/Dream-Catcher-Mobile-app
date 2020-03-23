package edu.vt.cs.cs5254.dreamcatcher

import android.content.Context
import android.nfc.Tag
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.dreamcatcher.database.Dream
import java.text.DateFormat
import java.util.*

private const val TAG = "DreamListFragment"
class DreamListFragment : Fragment() {

    interface Callbacks{
        fun onDreamSelected(dreamId: UUID)
    }

    private var callbacks: Callbacks? = null

    private lateinit var dreamRecyclerView: RecyclerView
    private var adapter: DreamAdapter?= DreamAdapter(emptyList())


    private val dreamListViewModel: DreamListViewModel by lazy {
        ViewModelProviders.of(this).get(DreamListViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }



    companion object {
        fun newInstance(): DreamListFragment{
            return DreamListFragment()
        }
    }

    private lateinit var viewModel: DreamListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dream_list_fragment, container, false)
        dreamRecyclerView = view.findViewById(R.id.dream_recycler_view) as RecyclerView
        dreamRecyclerView.layoutManager = LinearLayoutManager(context)
        dreamRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dreamListViewModel.dreamListLiveData.observe(
            viewLifecycleOwner,
            Observer { dreams ->
                dreams?.let {
                    Log.i(TAG, "Got dreams ${dreams.size}")
                    updateUI(dreams)
                }

            }
        )
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun updateUI(dreams: List<Dream>){
        adapter = DreamAdapter(dreams)
        dreamRecyclerView.adapter = adapter
    }

    inner class DreamHolder(view: View)
        :RecyclerView.ViewHolder(view), View.OnClickListener{

        private lateinit var dream: Dream

        private val titleTextView: TextView = itemView.findViewById(R.id.dream_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.dream_date)
        private val dreamImageView: ImageView = itemView.findViewById(R.id.ic_dream)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(dream: Dream){
            this.dream = dream
            titleTextView.text = this.dream.description
            dateTextView.text = DateFormat.getDateInstance(android.icu.text.DateFormat.MEDIUM).format(this.dream.dateRevealed)

            when {
                dream.isRealized -> {
                    dreamImageView.setImageResource(R.drawable.dream_realized_icon)
                    dreamImageView.tag = R.drawable.dream_realized_icon
                }
                dream.isDeferred -> {
                    dreamImageView.setImageResource(R.drawable.dream_deferred_icon)
                    dreamImageView.tag = R.drawable.dream_deferred_icon
                }
                else -> {
                    dreamImageView.setImageResource(0)
                    dreamImageView.tag = 0
                }
            }

        }

        override fun onClick(v: View?){
            callbacks?.onDreamSelected(dream.id)
        }

    }
    private inner class DreamAdapter(var dreams: List<Dream>)
        :RecyclerView.Adapter<DreamHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : DreamHolder {
            val view = layoutInflater.inflate(R.layout.list_item_dream, parent,false)
            return DreamHolder(view)
        }

        override fun getItemCount()= dreams.size
        override fun onBindViewHolder(holder: DreamHolder, position: Int) {
            val dream = dreams[position]
            holder.bind(dream)

        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DreamListViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
