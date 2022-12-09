package com.colin.docscan.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.fragment.app.Fragment
import com.colin.docscan.ScannerApplication
import com.colin.docscan.databinding.FragmentSettingsBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    companion object {
        val recognizerModeKey = intPreferencesKey("0")
        // fast - 0
        // accurate - 1
        // balance - 2
    }
    private val binding get() = _binding!!

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val notificationsViewModel =
//            ViewModelProvider(this)[SettingsViewModel::class.java]
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        suspend fun setRecognizerMode(mode: Int) {
            (context?.applicationContext as ScannerApplication).dataStore.edit { pref ->
                pref[recognizerModeKey] = mode
            }
        }

        fun getRecognizerMode(): Flow<Int> {
            return (context?.applicationContext as ScannerApplication).dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { pref ->
                    val recogMode = pref[recognizerModeKey] ?: 1
                    Log.d("RECOG SETTINGS", recogMode.toString())
                    recogMode
                }
        }

        GlobalScope.launch(Dispatchers.IO) {
            val pos = getRecognizerMode().first()
            requireActivity().runOnUiThread {
                binding.spinnerBoxRecognizerMode.setSelection(pos)
            }
        }


        binding.spinnerBoxRecognizerMode.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    itemSelected: View?,
                    selectedItemPosition: Int,
                    selectedId: Long
                ) {
                    GlobalScope.launch(Dispatchers.IO) {
                        setRecognizerMode(selectedItemPosition)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}