package com.example.groupify

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.provider.Settings.Secure
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SelectionActivity : AppCompatActivity() {
    private lateinit var storageRef: StorageReference
    private lateinit var deviceId: String
    private lateinit var auth: FirebaseAuth
    private lateinit var selectedOption: String // Track selected option
    private lateinit var syncIcon: ImageView
    private lateinit var syncText: TextView
    private lateinit var syncContainer: LinearLayout
    private var isSynced = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)

        syncContainer = findViewById(R.id.button_sync_container)
        syncIcon = findViewById<ImageView>(R.id.sync_check_icon)
        syncText = findViewById<TextView>(R.id.sync_text)

        // FirebaseAuth instance initialization
        auth = FirebaseAuth.getInstance()
        signInAnonymously()

        // Firebase Storage initialization
        val storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        // Retrieve ANDROID_ID
        deviceId = Secure.getString(contentResolver, Secure.ANDROID_ID)


        syncContainer.setOnClickListener {
            if (!isSynced) {
                showSyncDialog() // 업데이트 전에는 AlertDialog를 띄우기
            }
        }



        // Selection buttons and their highlighting
        val buttonFunction = findViewById<LinearLayout>(R.id.button_function)
        val buttonColorRange = findViewById<LinearLayout>(R.id.button_color_range)
        val buttonProceed = findViewById<Button>(R.id.button_proceed)
        val checkIconFunction = findViewById<ImageView>(R.id.check_icon_function)
        val checkIconColor = findViewById<ImageView>(R.id.check_icon_color)

        buttonFunction.setOnClickListener {
            selectOption("function", buttonFunction, checkIconFunction)
            buttonProceed.visibility = View.VISIBLE
        }

        buttonColorRange.setOnClickListener {
            selectOption("color", buttonColorRange, checkIconColor)
            buttonProceed.visibility = View.VISIBLE
        }

        buttonProceed.setOnClickListener {
            val intent = when (selectedOption) {
                "function" -> Intent(this, FunctionActivity::class.java)
                "color" -> Intent(this, ColorClassify::class.java)
                else -> null
            }
            intent?.let { startActivity(it) }
        }
    }

    // Highlight selected option with border color and check icon
    private fun highlightSelectedOption(selectedLayout: LinearLayout, checkIcon: ImageView) {
        val buttonFunction = findViewById<LinearLayout>(R.id.button_function)
        val buttonColorRange = findViewById<LinearLayout>(R.id.button_color_range)

        buttonFunction.background = ContextCompat.getDrawable(this, R.drawable.border_radius)
        buttonColorRange.background = ContextCompat.getDrawable(this, R.drawable.border_radius)

        findViewById<ImageView>(R.id.check_icon_function).visibility = View.GONE
        findViewById<ImageView>(R.id.check_icon_color).visibility = View.GONE

        selectedLayout.setBackgroundResource(R.drawable.selection_rounded_button)
        checkIcon.visibility = View.VISIBLE
    }

    private fun selectOption(option: String, selectedLayout: LinearLayout, checkIcon: ImageView) {
        selectedOption = option

        // Reset background and hide all check icons
        findViewById<LinearLayout>(R.id.button_function).setBackgroundResource(R.drawable.selection_rounded_button)
        findViewById<LinearLayout>(R.id.button_color_range).setBackgroundResource(R.drawable.selection_rounded_button)
        findViewById<ImageView>(R.id.check_icon_function).visibility = View.GONE
        findViewById<ImageView>(R.id.check_icon_color).visibility = View.GONE

        // Apply selected background and show the check icon
        selectedLayout.setBackgroundResource(R.drawable.selection_rounded_button_selected)
        checkIcon.visibility = View.VISIBLE
    }


    private fun signInAnonymously() {
        auth.signInAnonymously().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d("FirebaseAuth", "signInAnonymously:success")
            } else {
                Log.w("FirebaseAuth", "signInAnonymously:failure", task.exception)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                startActivityForResult(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION), 1)
            } else {
                logInstalledApps()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), 1
                )
            } else {
                logInstalledApps()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            logInstalledApps()
        }
    }

    private fun logInstalledApps() {
        val packageManager = packageManager
        val installedPackages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appDataList = mutableListOf<Map<String, Any>>()

        for (appInfo in installedPackages) {
            val packageName = appInfo.packageName
            if (packageManager.getLaunchIntentForPackage(packageName) != null) {
                val appName = appInfo.loadLabel(packageManager).toString()
                val versionName = packageManager.getPackageInfo(packageName, 0).versionName
                val versionCode = packageManager.getPackageInfo(packageName, 0).longVersionCode
                val appIcon = appInfo.loadIcon(packageManager)
                val dominantColorHex = try {
                    extractDominantColorHex(appIcon)
                } catch (e: Exception) {
                    Log.e("AppLog", "Error extracting dominant color for $appName", e)
                    "#000000"
                }

                val appData = mapOf(
                    "appName" to appName,
                    "packageName" to packageName,
                    "versionName" to versionName,
                    "versionCode" to versionCode,
                    "dominantColor" to dominantColorHex
                )
                appDataList.add(appData)
                updateSyncStatus()

                Log.d("suacheck", "App Name: $appName, Package Name: $packageName, Dominant Color: $dominantColorHex")
            }
        }

        saveToFirebaseAsJson(appDataList)
    }

    private fun showSyncDialog() {
        val dialogView = layoutInflater.inflate(R.layout.custom_alert_dialog, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // 버튼 설정
        dialogView.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.confirm_button).setOnClickListener {
            checkAndRequestPermissions() // 기능 연결
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun updateSyncStatus() {
        syncIcon.setImageResource(R.drawable.check_icon_selected) // 업데이트 성공 시 아이콘 변경
        syncText.text = "업데이트 되었어요" // 업데이트 성공 시 텍스트 변경
        syncText.setTextColor(Color.parseColor("#4b5ae4")) // 텍스트 색상 변경
        isSynced = true // 업데이트 성공 상태로 변경
    }

    private fun extractDominantColorHex(drawable: Drawable): String {
        val bitmap = when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            is AdaptiveIconDrawable -> {
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
            else -> throw IllegalArgumentException("Unsupported drawable type")
        }

        return extractDominantColorFromBitmap(bitmap)
    }

    private fun extractDominantColorFromBitmap(bitmap: Bitmap): String {
        val palette = Palette.from(bitmap).generate()
        val dominantColor = palette.getDominantColor(Color.GRAY)
        return String.format("#%06X", 0xFFFFFF and dominantColor)
    }

    private fun saveToFirebaseAsJson(appDataList: List<Map<String, Any>>) {
        val gson = com.google.gson.Gson()
        val jsonString = gson.toJson(appDataList)

        val logFile = File(getExternalFilesDir(null), "app_data_$deviceId.json")
        Log.d("suacheck", "Saving file to: ${logFile.absolutePath}")

        try {
            FileOutputStream(logFile, false).use { output ->
                output.write(jsonString.toByteArray())
            }
            uploadLogToFirebase(logFile)
        } catch (e: IOException) {
            Log.e("saveToFirebaseAsJson", "Error saving JSON", e)
        }
    }

    private fun uploadLogToFirebase(logFile: File) {
        val fileUri = Uri.fromFile(logFile)
        val storageRef = storageRef.child("logs/${logFile.name}")

        storageRef.putFile(fileUri)
            .addOnSuccessListener { Log.d("Firebase", "JSON data uploaded successfully: ${logFile.name}") }
            .addOnFailureListener { exception -> Log.e("Firebase", "Failed to upload JSON data", exception) }
    }
}