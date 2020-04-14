package edu.vt.cs.cs5254.dreamcatcher

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import edu.vt.cs.cs5254.dreamcatcher.database.*
import java.io.File
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "dream_database"

class DreamRepository private constructor(context: Context) {

    private val repopulateRoomDatabaseCallback: RoomDatabase.Callback =
        object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.d(TAG, "repopulateRoomDatabaseCallback.onOpen")
                executor.execute {
//                    dreamDao.apply {
//                        reconstructSampleDatabase()
//                    }
                }
            }
        }

    private val database : DreamDatabase = Room.databaseBuilder(
        context.applicationContext,
        DreamDatabase::class.java,
        DATABASE_NAME
    ).addCallback(repopulateRoomDatabaseCallback).build()

    private val dreamDao = database.dreamDao()

    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir

    fun getDreams(): LiveData<List<Dream>> = dreamDao.getDreams()

    fun getDreamWithEntries(dreamId: UUID): LiveData<DreamWithEntries> =
        dreamDao.getDreamWithEntries(dreamId)

    fun updateDreamWithEntries(dreamWithEntries: DreamWithEntries) {
        executor.execute {
            dreamDao.updateDreamWithEntries(dreamWithEntries)
        }
    }

    fun updateDream(dream: Dream){
        executor.execute{
            dreamDao.updateDream(dream)
        }
    }
    fun addDreamWithEntries(dreamWithEntries: DreamWithEntries){
        executor.execute {
            dreamDao.addDreamWithEntries(dreamWithEntries)
        }
    }

    fun getPhotoFile(dream: Dream): File =File(filesDir, dream.photoFileName)

    //fun reconstructSampleDatabase() = dreamDao.reconstructSampleDatabase()

    companion object {
        private const val TAG = "DreamRepository"

        private var INSTANCE: DreamRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = DreamRepository(context)
            }
        }

        fun get(): DreamRepository {
            return INSTANCE ?:
            throw IllegalStateException("DreamRepository must be initialized")
        }
    }
}
