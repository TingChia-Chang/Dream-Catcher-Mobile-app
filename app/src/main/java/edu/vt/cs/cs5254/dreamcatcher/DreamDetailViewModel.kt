package edu.vt.cs.cs5254.dreamcatcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.vt.cs.cs5254.dreamcatcher.database.Dream
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntry
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries
import java.io.File
import java.util.*

class DreamDetailViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val dreamRepository = DreamRepository.get()
    private val dreamIdLiveData = MutableLiveData<UUID>()

    var dreamLiveData: LiveData<DreamWithEntries?> =
        Transformations.switchMap(dreamIdLiveData){dreamId->
            dreamRepository.getDreamWithEntries(dreamId)
        }

    fun loadDream(dreamId: UUID){
        dreamIdLiveData.value = dreamId
    }

    fun saveDreams(dreamWithEntries: DreamWithEntries){
        dreamRepository.updateDreamWithEntries(dreamWithEntries)
    }

    fun getPhotoFile(dream: Dream): File {
        return dreamRepository.getPhotoFile(dream)
    }

}
