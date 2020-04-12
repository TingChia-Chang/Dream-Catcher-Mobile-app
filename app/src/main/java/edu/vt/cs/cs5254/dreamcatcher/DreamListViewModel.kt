package edu.vt.cs.cs5254.dreamcatcher

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.dreamcatcher.database.Dream


class DreamListViewModel : ViewModel() {
    // TODO: Implement the
    private val dreamRepository =  DreamRepository.get()
    val dreamListLiveData = dreamRepository.getDreams()


    fun addDream(dream:Dream){
        dreamRepository.addDream(dream)
    }

    fun saveDream(dream: Dream){
        dreamRepository.updateDream(dream)
    }



}
