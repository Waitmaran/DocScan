package com.colin.docscan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.colin.docscan.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var token: String

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
                R.id.navigation_scan, R.id.navigation_documents, R.id.navigation_notifications
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
        if (!File( datapath + "rus" + ".traineddata").exists()) {
            try {
                val assetManager = assets
                val `in`: InputStream =
                    assetManager.open("rus" + ".traineddata")
                Log.d("RECOG", "Input stream opened")

                File(datapath + "rus.traineddata").parentFile?.mkdirs()

                val outFile = File(datapath, "rus.traineddata")
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