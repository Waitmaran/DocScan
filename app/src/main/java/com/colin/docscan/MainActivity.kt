package com.colin.docscan

import DocStorage
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.colin.docscan.databinding.ActivityMainBinding
import com.colin.docscan.ui.notifications.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.*
import java.lang.reflect.Type


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var token: String

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(intent.extras?.get("token") == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        token = intent.extras?.get("token").toString()

        Log.d("TOKEN", token)
        DataBaseSync.userId = token

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_scan, R.id.navigation_documents, R.id.navigation_settings
            )
        )

        if (
            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
            checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED ||
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
            checkSelfPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
        ) {
            val permission = arrayOf<String>(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
            )
            requestPermissions(permission, 112)
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val datapath = cacheDir.absolutePath + File.separator.toString() + "tessdata" + File.separator
        val models = arrayOf(
            "rus",
            "fast_rus",
            "balance_rus",
            "eng",
            "fast_eng",
            "balance_eng")

        for(model in models) {
            if (!File("$datapath$model.traineddata").exists()) {
                try {
                    val assetManager = assets
                    val `in`: InputStream =
                        assetManager.open("$model.traineddata")
                    Log.d("RECOG", "Input stream opened")

                    File("$datapath$model.traineddata").parentFile?.mkdirs()

                    val outFile = File(datapath, "$model.traineddata")
                    val out: OutputStream = FileOutputStream(outFile)
                    Log.d("RECOG", "Output stream opened")

                    val buf = ByteArray(8024)
                    var len: Int
                    while (`in`.read(buf).also { len = it } > 0) {
                        out.write(buf, 0, len)
                    }
                    `in`.close()
                    out.close()
                } catch (e: IOException) {
                    Log.d("RECOG", "Was unable to copy rus traineddata $e")
                }
            } else {
                Log.d("RECOG", "TRAINED FILE FOUND!")
            }
        }

    }

}

