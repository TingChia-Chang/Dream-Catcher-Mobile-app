package edu.vt.cs.cs5254.dreamcatcher

import android.content.Intent
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
import java.util.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntry
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntryKind
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries
import edu.vt.cs.cs5254.dreamcatcher.util.CameraUtil
import java.io.File

private const val TAG = "DreamDetailFragment"
private const val ARG_DREAM_ID = "dream_id"
private const val REQUEST_PHOTO = 2

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
    private lateinit var realizedCheckBox: CheckBox
    private lateinit var deferredCheckBox: CheckBox
    private lateinit var dreamWithEntries: DreamWithEntries
    private lateinit var photoView: ImageView
    private lateinit var iconView: ImageView
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private lateinit var dreamRecyclerView: RecyclerView
    private var adapter: DreamDetailFragment.DreamEntryAdapter?= DreamEntryAdapter(emptyList())

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


        dreamRecyclerView = view.findViewById(R.id.dream_entry_recycler_view) as RecyclerView
        dreamRecyclerView.layoutManager = LinearLayoutManager(context)
        dreamRecyclerView.adapter = adapter




        realizedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->

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
