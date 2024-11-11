// Top-level build file where you can add configuration options common to all sub-projects/modules.
import java.io.IOException
import java.util.Locale
import java.util.Properties

plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false

}

val localProperties = Properties()
file("local.properties").inputStream().use { localProperties.load(it) }
val sdkDir = localProperties["sdk.dir"] as String

tasks.register("saveLogcat") {
    doLast {
        val adbPath = "$sdkDir/platform-tools/adb"
        val projectDir = projectDir.absolutePath
        val os = System.getProperty("os.name").toLowerCase(Locale.ROOT)

        val command = if (os.contains("win")) {
            arrayOf("powershell", "-ExecutionPolicy", "Bypass", "-File", "$projectDir/save_logcat.ps1", adbPath, projectDir)
        } else {
            arrayOf("bash", "$projectDir/save_logcat.sh", adbPath, projectDir)
        }

        try {
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            println("Logcat saved successfully.")
        } catch (e: IOException) {
            e.printStackTrace()
            println("Error saving logcat: ${e.message}")
        } catch (e: InterruptedException) {
            e.printStackTrace()
            println("Logcat save interrupted: ${e.message}")
        }
    }
}