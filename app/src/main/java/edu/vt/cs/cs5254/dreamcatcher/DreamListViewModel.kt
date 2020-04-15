package edu.vt.cs.cs5254.dreamcatcher

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.dreamcatcher.database.Dream
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries


class DreamListViewModel : ViewModel() {
    // TODO: Implement the
    private val dreamRepository =  DreamRepository.get()
    val dreamListLiveData = dreamRepository.getDreams()


    fun addDreamWithEntries(dreamWithEntries: DreamWithEntries){
        dreamRepository.addDreamWithEntries(dreamWithEntries)
    }

    fun saveDream(dream: Dream){
        dreamRepository.updateDream(dream)
    }

    fun deleteAllDreams(){
        dreamRepository.deleteAllDreams()
    }



}
