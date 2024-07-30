package com.example.groupify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class AppData(val name: String, val iconFileName: String, val dominantColors: IntArray) :
    Serializable

class MainActivity : AppCompatActivity() {

    private val appDataList = mutableListOf<AppData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView: TextView = findViewById(R.id.textView)
        textView.text = "Hello~"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE), 1)
            } else {
                logInstalledApps()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            } else {
                logInstalledApps()
            }
        }

        val buttonNext = findViewById<Button>(R.id.button_next)
        buttonNext.setOnClickListener {
            val intent = Intent(this, DeployActivity::class.java)
            startActivity(intent)

            try {
                val appContainer: LinearLayout = findViewById(R.id.appContainer)
                val packageManager = packageManager
                val packages = packageManager.getInstalledPackages(0)

                for (packageInfo in packages) {
                    if (packageManager.getInstallerPackageName(packageInfo.packageName) == "com.android.vending") {
                        val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                        val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)

                        val bitmap = drawableToBitmap(appIcon)
                        val iconFileName = saveAppIcon(appName, bitmap)
                        val dominantColors = extractDominantColors(bitmap)

                        appDataList.add(AppData(appName, iconFileName, dominantColors))

                        val textView = TextView(this)
                        textView.text = appName

                        val imageView = ImageView(this)
                        imageView.setImageDrawable(appIcon)
                        imageView.layoutParams = LinearLayout.LayoutParams(100, 100)

                        val appLayout = LinearLayout(this)
                        appLayout.orientation = LinearLayout.HORIZONTAL
                        appLayout.addView(imageView)
                        appLayout.addView(textView)

                        appContainer.addView(appLayout)
                    }
                }

                saveAppDataToFile()
            } catch (e: Exception) {
                Log.e("AppInfo", "Error retrieving app information", e)
            }
        }
    }

    private fun saveAppIcon(appName: String, bitmap: Bitmap): String {
        val appDir = File(getExternalFilesDir(null), "AppIcons")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }

        val iconFileName = "$appName.png"
        val iconFile = File(appDir, iconFileName)
        try {
            FileOutputStream(iconFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
        } catch (e: IOException) {
            Log.e("SaveAppIcon", "Error saving app icon", e)
        }
        return iconFileName
    }

    private fun extractDominantColors(bitmap: Bitmap): IntArray {
        val palette = Palette.from(bitmap).generate()
        val dominantSwatch = palette.dominantSwatch
        return if (dominantSwatch != null) {
            intArrayOf(Color.red(dominantSwatch.rgb), Color.green(dominantSwatch.rgb), Color.blue(dominantSwatch.rgb))
        } else {
            intArrayOf(0, 0, 0)
        }
    }

    private fun saveAppDataToFile() {
        val dataFile = File(getExternalFilesDir(null), "AppData.dat")
        try {
            ObjectOutputStream(FileOutputStream(dataFile)).use { output ->
                output.writeObject(appDataList)
            }
            Log.d("SaveAppData", "AppData saved to ${dataFile.absolutePath}")
        } catch (e: IOException) {
            Log.e("SaveAppData", "Error saving AppData", e)
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                logInstalledApps()
            } else {
                Log.e("Permission", "WRITE_EXTERNAL_STORAGE permission denied")
            }
        }
    }

    private fun logInstalledApps() {
        val packageManager = packageManager
        val packages = packageManager.getInstalledPackages(0)
        val logStringBuilder = StringBuilder()

        for (packageInfo in packages) {
            val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
            val packageName = packageInfo.packageName
            val versionName = packageInfo.versionName
            val versionCode = packageInfo.longVersionCode

            logStringBuilder.append("App Name: $appName, Package Name: $packageName, Version Name: $versionName, Version Code: $versionCode\n")
            Log.d("AllAppInfo", "App Name: $appName, Package Name: $packageName, Version Name: $versionName, Version Code: $versionCode")
        }
        saveLogcat(logStringBuilder.toString())
    }

    private fun saveLogcat(log: String) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val logsDir = getExternalFilesDir(null)
        if (logsDir != null && !logsDir.exists()) {
            logsDir.mkdirs()
        }

        val logFile = File(logsDir, "logcat_$timestamp.txt")
        try {
            FileOutputStream(logFile, true).use { output ->
                output.write(log.toByteArray())
            }
            Log.d("saveLogcat", "Logcat saved to ${logFile.absolutePath}")
        } catch (e: IOException) {
            Log.e("saveLogcat", "Error saving logcat", e)
        }
    }
}
