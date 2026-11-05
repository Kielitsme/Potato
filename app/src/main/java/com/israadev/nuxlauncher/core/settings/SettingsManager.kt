package com.israadev.nuxlauncher.core.settings

import android.app.ActivityManager
import android.content.Context
import com.google.gson.Gson
import com.israadev.nuxlauncher.core.models.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

object SettingsManager {
    private val gson = Gson()
    private val _settings = MutableStateFlow(LauncherSettings())
    val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

    private fun getSettingsFile(context: Context): File {
        return File(context.filesDir, "settings.json")
    }

    fun init(context: Context) {
        val file = getSettingsFile(context)
        if (file.exists()) {
            try {
                val json = file.readText()
                val loaded = gson.fromJson(json, LauncherSettings::class.java)
                if (loaded != null) {
                    _settings.value = loaded
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val initialSettings = defaultsFor(getTotalDeviceMemoryMb(context))
        _settings.value = initialSettings
        save(context)
    }

    /**
     * Default per tingkat RAM perangkat. Perangkat low-end mendapat heap lebih kecil (menyisakan RAM untuk
     * Android + driver GPU, yang berbagi memori dengan sistem) dan resolusi render lebih rendah
     * (fill-rate GPU adalah bottleneck utama di GPU Mali/Adreno entry-level).
     */
    private fun defaultsFor(totalRamMb: Int): LauncherSettings {
        val ramMb = when {
            totalRamMb >= 12000 -> 4096
            totalRamMb >= 8000 -> 3072
            totalRamMb >= 6000 -> 2560
            totalRamMb >= 4000 -> 2048
            totalRamMb >= 3000 -> 1536
            else -> 1024
        }
        val resolution = when {
            totalRamMb >= 8000 -> 100
            totalRamMb >= 6000 -> 85
            totalRamMb >= 4000 -> 75
            else -> 60
        }
        return LauncherSettings(ramMb = ramMb, resolutionRatio = resolution)
    }

    fun updateSettings(context: Context, newSettings: LauncherSettings) {
        _settings.value = newSettings
        save(context)
    }

    fun resetToDefaults(context: Context) {
        _settings.value = defaultsFor(getTotalDeviceMemoryMb(context))
        save(context)
    }

    private fun save(context: Context) {
        try {
            val file = getSettingsFile(context)
            file.writeText(gson.toJson(_settings.value))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTotalDeviceMemoryMb(context: Context): Int {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return 4096
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            (memInfo.totalMem / (1024 * 1024)).toInt()
        } catch (_: Throwable) {
            4096
        }
    }

    fun getAvailableDeviceMemoryMb(context: Context): Int {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return 2048
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            (memInfo.availMem / (1024 * 1024)).toInt()
        } catch (_: Throwable) {
            2048
        }
    }

    fun getCpuCoreCount(): Int {
        return Runtime.getRuntime().availableProcessors()
    }
}
