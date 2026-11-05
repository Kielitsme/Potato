package com.israadev.nuxlauncher.core.renderer

import com.israadev.nuxlauncher.core.utils.NuxVersionUtils
import java.io.File

/**
 * Daftar renderer sengaja dibatasi ke dua wrapper GLES yang ringan:
 *  - Krypton (NG-GL4ES, OpenGL ES 3)  -> Minecraft 1.17+
 *  - GL4ES 1.1.4 (OpenGL ES 2)        -> Minecraft 1.7.x - 1.16.5
 *
 * Renderer berat berbasis Mesa/Vulkan (Zink, Freedreno, VirGL, Panfrost) dan MobileGlues
 * (OpenGL 4.x untuk shader) sudah dihapus: butuh GPU kuat dan memakan RAM/CPU besar di HP low-end.
 */
object NuxRendererRegistry {

    val RENDERER_AUTO = NuxRendererInfo(
        id = "auto",
        displayName = "Auto (Rekomendasi)",
        badge = "Otomatis Cerdas",
        summary = "Automatically selects the most optimal & stable renderer based on the Minecraft version launched.",
        compatibility = "All Minecraft Versions",
        rendererId = "auto",
        libraryName = "auto"
    )

    val RENDERER_KRYPTON = NuxRendererInfo(
        id = "krypton",
        displayName = "Krypton (Next-Gen GL4ES)",
        badge = "Rekomendasi Utama (Mali & Adreno)",
        summary = "Modern OpenGL ES 3.x wrapper directly atop official system drivers. Maximum stability and peak FPS for Mali & Adreno GPUs on modern MC (1.17 – 1.21+).",
        compatibility = "Minecraft 1.17 – 26.3-snapshot-4",
        rendererId = "opengles3",
        libraryName = "libng_gl4es.so",
        isMaliRecommended = true,
        isAdrenoRecommended = true,
        minMCVersion = "1.17",
        maxMCVersion = "26.3-snapshot-4",
        envVariables = mapOf(
            "LIBGL_USE_MC_COLOR" to "1",
            "LIBGL_GL" to "31",
            "LIBGL_ES" to "3",
            "LIBGL_NORMALIZE" to "1",
            "LIBGL_NOERROR" to "1"
        )
    )

    val RENDERER_GL4ES = NuxRendererInfo(
        id = "gl4es",
        displayName = "GL4ES 1.1.4",
        badge = "Most Stable for Legacy MC",
        summary = "Classic OpenGL ES 2.0 wrapper. Highly compatible with Minecraft 1.7.10 – 1.16.5 and low-end devices.",
        compatibility = "Minecraft 1.7.10 – 1.16.5",
        rendererId = "opengles2",
        libraryName = "libgl4es_114.so",
        minMCVersion = "1.7.2",
        maxMCVersion = "1.16.5",
        envVariables = mapOf(
            "LIBGL_ES" to "2",
            "LIBGL_MIPMAP" to "3",
            "LIBGL_NOERROR" to "1",
            "LIBGL_NOINTOVLHACK" to "1",
            "LIBGL_NORMALIZE" to "1"
        )
    )

    val availableRenderers: List<NuxRendererInfo> = listOf(RENDERER_AUTO, RENDERER_KRYPTON, RENDERER_GL4ES)

    /** Menemukan renderer berdasarkan ID; ID tak dikenal (mis. "zink" dari pengaturan lama) jatuh ke Auto. */
    fun findRendererById(id: String): NuxRendererInfo {
        return availableRenderers.find { it.id.equals(id, ignoreCase = true) } ?: RENDERER_AUTO
    }

    /** Memeriksa apakah renderer yang dipilih kompatibel dengan versi Minecraft yang akan dimainkan. */
    fun isSupportedForVersion(renderer: NuxRendererInfo, mcVersion: String): Boolean {
        return NuxVersionUtils.isRendererSupported(renderer, mcVersion)
    }

    /**
     * Menyelesaikan renderer aktual. "auto" (atau ID lama yang sudah dihapus) memilih Krypton untuk
     * MC 1.17+ dan GL4ES untuk versi di bawahnya; jika library pilihan tidak ada, jatuh ke yang tersedia.
     */
    @Suppress("UNUSED_PARAMETER")
    fun resolveRenderer(
        selectedId: String,
        mcVersion: String,
        nativeLibDir: File,
        context: android.content.Context? = null
    ): NuxRendererInfo {
        val hasKrypton = File(nativeLibDir, RENDERER_KRYPTON.libraryName).exists()
        val hasGl4es = File(nativeLibDir, RENDERER_GL4ES.libraryName).exists()

        val requested = availableRenderers.find { it.id.equals(selectedId, ignoreCase = true) }
        if (requested != null && requested.id != RENDERER_AUTO.id && File(nativeLibDir, requested.libraryName).exists()) {
            return requested
        }

        val modern = isModernMinecraft(mcVersion)
        return when {
            modern && hasKrypton -> RENDERER_KRYPTON
            hasGl4es -> RENDERER_GL4ES
            hasKrypton -> RENDERER_KRYPTON
            else -> RENDERER_GL4ES
        }
    }

    /** Deteksi apakah versi Minecraft adalah 1.17 ke atas. */
    fun isModernMinecraft(version: String): Boolean {
        if (version.isBlank()) return true
        val clean = version.trim().lowercase()
        // Snapshot modern (24w..., 25w..., 26w...)
        if (clean.matches(Regex("""^\d{2}w\d{2}.*"""))) return true

        val match = Regex("""^1\.(\d+)""").find(clean)
        if (match != null) {
            val minor = match.groupValues[1].toIntOrNull() ?: 17
            return minor >= 17
        }
        return true
    }
}
