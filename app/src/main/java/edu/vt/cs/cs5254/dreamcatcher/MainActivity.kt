package edu.vt.cs.cs5254.dreamcatcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.util.*


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(),
    DreamListFragment.Callbacks{
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            val dreamDetailFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

            if (dreamDetailFragment == null){
                val fragment = DreamListFragment.newInstance()
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
            }

        }
        override fun onDreamSelected(dreamId: UUID) {
            val fragment = DreamDetailFragment.newInstance(dreamId)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
}


