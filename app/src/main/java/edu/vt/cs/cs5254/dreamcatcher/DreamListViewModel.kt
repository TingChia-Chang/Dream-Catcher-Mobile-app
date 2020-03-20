package edu.vt.cs.cs5254.dreamcatcher

import androidx.lifecycle.ViewModel



class DreamListViewModel : ViewModel() {
    // TODO: Implement the
    private val dreamRepository =  DreamRepository.get()
    val dreamListLiveData = dreamRepository.getDreams()

}
