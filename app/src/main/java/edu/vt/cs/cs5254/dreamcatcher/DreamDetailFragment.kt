package edu.vt.cs.cs5254.dreamcatcher

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import java.util.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntry
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntryKind
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries
import edu.vt.cs.cs5254.dreamcatcher.util.CameraUtil
import java.io.File
import java.text.DateFormat

private const val TAG = "DreamDetailFragment"
private const val ARG_DREAM_ID = "dream_id"
private const val REQUEST_PHOTO = 2

class DreamDetailFragment : Fragment(), AddDreamEntryFragment.Callbacks {
    override fun onCommentCreated(comment: String, createDate: Date) {
        Log.d("test", "detailfrag $comment, $createDate")

        dreamWithEntries.dreamEntries += DreamEntry(dreamId = dreamWithEntries.dream.id, comment = comment, dateCreated = createDate)
        viewModel.updateDreamEntries(dreamWithEntries.dreamEntries)

    }

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
    private lateinit var realizedCheckBox: CheckBox
    private lateinit var deferredCheckBox: CheckBox
    private lateinit var dreamWithEntries: DreamWithEntries
    private lateinit var photoView: ImageView
    private lateinit var iconView: ImageView
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var fab: FloatingActionButton
    private var df = DateFormat.getDateInstance(DateFormat.MEDIUM)

    private lateinit var dreamRecyclerView: RecyclerView
    private var adapter: DreamEntryAdapter?= DreamEntryAdapter(emptyList())

    private val dreamDetailViewModel: DreamDetailViewModel by lazy {
        ViewModelProviders.of(this).get(DreamDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val dreamID: UUID = arguments?.getSerializable(ARG_DREAM_ID) as UUID
        dreamDetailViewModel.loadDream(dreamID)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dream_detail_fragment, container, false)
        titleField = view.findViewById(R.id.dream_title) as EditText
        realizedCheckBox = view.findViewById(R.id.dream_realized) as CheckBox
        deferredCheckBox = view.findViewById(R.id.dream_deferred) as CheckBox
        photoView = view.findViewById(R.id.dream_photo) as ImageView
        iconView = view.findViewById(R.id.dream_icon) as ImageView
        fab = view.findViewById(R.id.add_comment_fab)


        dreamRecyclerView = view.findViewById(R.id.dream_entry_recycler_view) as RecyclerView
        dreamRecyclerView.layoutManager = LinearLayoutManager(context)
        dreamRecyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback())
        itemTouchHelper.attachToRecyclerView((dreamRecyclerView))





        realizedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->

//                iconView.setImageResource(R.drawable.dream_realized_icon)
//                iconView.tag = R.drawable.dream_realized_icon

                dreamWithEntries.dream.isRealized = isChecked
                deferredCheckBox.isEnabled = !dreamWithEntries.dream.isRealized
                val temp = dreamWithEntries.dreamEntries.filter { it.kind == DreamEntryKind.DEFERRED || it.kind == DreamEntryKind.REALIZED}

                dreamWithEntries.dreamEntries = dreamWithEntries.dreamEntries - temp
                if(isChecked){
                    dreamWithEntries.dreamEntries = dreamWithEntries.dreamEntries + DreamEntry(dreamId = dreamWithEntries.dream.id, kind = DreamEntryKind.REALIZED, comment = "DREAM REALIZED")
                }

                dreamDetailViewModel.saveDreams(dreamWithEntries)

            }
        }

        deferredCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->

//                iconView.setImageResource(R.drawable.dream_deferred_icon)
//                iconView.tag = R.drawable.dream_deferred_icon

                dreamWithEntries.dream.isDeferred = isChecked
                realizedCheckBox.isEnabled = !dreamWithEntries.dream.isDeferred
                val temp2 = dreamWithEntries.dreamEntries.filter { it.kind == DreamEntryKind.REALIZED || it.kind == DreamEntryKind.DEFERRED}
                dreamWithEntries.dreamEntries = dreamWithEntries.dreamEntries - temp2
                if (isChecked) {
                    dreamWithEntries.dreamEntries = dreamWithEntries.dreamEntries + DreamEntry(dreamId = dreamWithEntries.dream.id, kind = DreamEntryKind.DEFERRED, comment = "DREAM DEFERRED")
                }

                dreamDetailViewModel.saveDreams(dreamWithEntries)
            }
        }

        fab.setOnClickListener{view->
            val fragment: DialogFragment = AddDreamEntryFragment.newInstance()
            fragment.setTargetFragment(this, 1)
            fragment.show(parentFragmentManager, "dialog")

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
                    photoFile = dreamDetailViewModel.getPhotoFile(dreamEntries.dream)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        photoFile)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_dream_detail,menu)
        val cameraAvailable = CameraUtil.isCameraAvailable(requireActivity())
        val menuPhoto = menu.findItem(R.id.take_dream_photo)
        val menuShare = menu.findItem(R.id.share_dream)
        menuPhoto.apply {
            Log.d(TAG, "Camera available: $cameraAvailable")
            isEnabled = cameraAvailable
            isVisible = cameraAvailable
        }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.take_dream_photo -> {
                val captureImageIntent = CameraUtil.createCaptureImageIntent(requireActivity(), photoUri)
                startActivity(captureImageIntent)
                true
            }
            R.id.share_dream ->{
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getDreamReport())
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        dreamWithEntries.dream.description
                    ).also {
                        intent ->
                        val chooserIntent =
                            Intent.createChooser(intent, getString(R.string.send_report))
                        startActivity(chooserIntent)
                    }
                }

                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
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

        when{
            dreamWithEntries.dream.isRealized -> {
                iconView.setImageResource(R.drawable.dream_realized_icon)
                iconView.tag = R.drawable.dream_realized_icon
            }
            dreamWithEntries.dream.isDeferred -> {
                iconView.setImageResource(R.drawable.dream_deferred_icon)
                iconView.tag = R.drawable.dream_deferred_icon
            }
            else -> {
                iconView.setImageResource(0)
                iconView.tag = 0
            }
        }


        adapter = DreamEntryAdapter(dreamWithEntries.dreamEntries)
        Log.d("test", dreamWithEntries.dreamEntries.size.toString())
        dreamRecyclerView.adapter = adapter

        updatePhotoView()
    }

    private fun updatePhotoView(){
        if(photoFile.exists()){
            val bitmap = CameraUtil.getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        }else{
            photoView.setImageDrawable(null)
        }
    }

    private fun getDreamReport(): String{
        var result = "# " + dreamWithEntries.dream.description + "\n"
        dreamWithEntries.dreamEntries.forEach {
            result += it.comment + " (" + df.format(it.dateCreated) + ")\n"
        }
        return result
    }

    inner class SwipeToDeleteCallback() :
        ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT ){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
           return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            adapter?.deleteItem(position)

        }
    }


    inner class DreamEntryHolder(view: View)
        :RecyclerView.ViewHolder(view) {


        private lateinit var dreamEntry: DreamEntry

        private val button: Button = itemView.findViewById(R.id.dream_entry_button)

        fun bind(dreamEntry: DreamEntry){
            this.dreamEntry = dreamEntry
            when(dreamEntry.kind){
                DreamEntryKind.REVEALED -> {
                    button.apply {
                        setText(dreamEntry.comment)
                        setBackgroundColor(Color.YELLOW)
                        setTextColor(Color.BLACK)
                    }
                }
                DreamEntryKind.COMMENT -> {
                    button.apply {
                        setText(dreamEntry.comment + " (" + df.format(dreamEntry.dateCreated) + ')')
                        setBackgroundColor(Color.LTGRAY)
                    }
                }
                DreamEntryKind.REALIZED -> {
                    button.apply {
                        setText(dreamEntry.comment)
                        setBackgroundColor(Color.GREEN)
                    }
                }
                DreamEntryKind.DEFERRED -> {
                    button.apply {
                        setText(dreamEntry.comment)
                        setBackgroundColor(Color.RED)
                        setTextColor(Color.WHITE)
                    }
                }
            }

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

        fun deleteItem(position: Int) {
            val entryToDelete = dreamEntries[position]
            Log.d("test", "${dreamEntries.size}")
            if (entryToDelete.kind == DreamEntryKind.COMMENT){
                dreamEntries = dreamEntries - entryToDelete
                dreamDetailViewModel.updateDreamEntries(dreamEntries)
                notifyItemRemoved(position)
            }
            else{
                notifyItemChanged(position)
            }

        }

    }


}
