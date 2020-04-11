package edu.vt.cs.cs5254.dreamcatcher

import androidx.lifecycle.ViewModel
import edu.vt.cs.cs5254.dreamcatcher.database.Dream


class DreamListViewModel : ViewModel() {
    // TODO: Implement the
    private val dreamRepository =  DreamRepository.get()
    val dreamListLiveData = dreamRepository.getDreams()

    fun addDream(dream:Dream){
        dreamRepository.addDream(dream)
    }

}
