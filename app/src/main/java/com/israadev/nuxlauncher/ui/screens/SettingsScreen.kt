package com.israadev.nuxlauncher.ui.screens

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import com.israadev.nuxlauncher.core.lang.NuxLanguageManager
import com.israadev.nuxlauncher.core.lang.tr
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.israadev.nuxlauncher.R
import com.israadev.nuxlauncher.GameActivity
import java.io.File
import java.io.FileOutputStream
import com.israadev.nuxlauncher.core.account.AccountManager
import com.israadev.nuxlauncher.core.models.LauncherSettings
import com.israadev.nuxlauncher.core.renderer.NuxRendererRegistry
import com.israadev.nuxlauncher.core.renderer.NuxRendererInfo
import com.israadev.nuxlauncher.core.settings.SettingsManager
import com.israadev.nuxlauncher.core.utils.NuxVersionUtils
import com.israadev.nuxlauncher.ui.components.*
import com.israadev.nuxlauncher.ui.theme.NuxColors
import com.israadev.nuxlauncher.ui.theme.NuxSizes
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onOpenGuiEditor: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentSettings by SettingsManager.settings.collectAsState()

    val availableLanguages by NuxLanguageManager.availableLanguages.collectAsState()
    val activeLanguage by NuxLanguageManager.currentLanguage.collectAsState()
    var isTopLangMenuExpanded by remember { mutableStateOf(false) }
    var isCardLangMenuExpanded by remember { mutableStateOf(false) }


    // Local mutable state for instant responsive UI before saving
    var ramMb by remember(currentSettings.ramMb) { mutableIntStateOf(currentSettings.ramMb) }
    var initialHeapMb by remember(currentSettings.initialHeapMb) { mutableIntStateOf(currentSettings.initialHeapMb) }
    var customJvmArgs by remember(currentSettings.customJvmArgs) { mutableStateOf(currentSettings.customJvmArgs) }
    var defaultRuntime by remember(currentSettings.defaultJavaRuntime) { mutableStateOf(currentSettings.defaultJavaRuntime) }

    var mouseMode by remember(currentSettings.mouseControlMode) { mutableStateOf(currentSettings.mouseControlMode) }
    var cursorSensitivity by remember(currentSettings.cursorSensitivity) { mutableIntStateOf(currentSettings.cursorSensitivity) }
    var captureSensitivity by remember(currentSettings.captureSensitivity) { mutableIntStateOf(currentSettings.captureSensitivity) }
    var mouseSizeDp by remember(currentSettings.mouseSizeDp) { mutableIntStateOf(currentSettings.mouseSizeDp) }
    var physicalMouseMode by remember(currentSettings.physicalMouseMode) { mutableStateOf(currentSettings.physicalMouseMode) }

    var resolutionRatio by remember(currentSettings.resolutionRatio) { mutableIntStateOf(currentSettings.resolutionRatio) }
    var autoOptimizeMC by remember(currentSettings.autoOptimizeMinecraft) { mutableStateOf(currentSettings.autoOptimizeMinecraft) }
    var sustainedPerf by remember(currentSettings.sustainedPerformanceMode) { mutableStateOf(currentSettings.sustainedPerformanceMode) }
    var selectedRenderer by remember(currentSettings.selectedRenderer) { mutableStateOf(currentSettings.selectedRenderer) }
    var showRendererDialog by remember { mutableStateOf(false) }

    val totalRamMb = remember { SettingsManager.getTotalDeviceMemoryMb(context) }
    val maxAllocatableRam = remember(totalRamMb) { (totalRamMb * 0.65f).toInt().coerceAtLeast(1024) }
    val cpuCores = remember { SettingsManager.getCpuCoreCount() }

    fun commitSettings() {
        val updated = currentSettings.copy(
            ramMb = ramMb,
            initialHeapMb = initialHeapMb,
            customJvmArgs = customJvmArgs,
            defaultJavaRuntime = defaultRuntime,
            mouseControlMode = mouseMode,
            cursorSensitivity = cursorSensitivity,
            captureSensitivity = captureSensitivity,
            mouseSizeDp = mouseSizeDp,
            physicalMouseMode = physicalMouseMode,
            resolutionRatio = resolutionRatio,
            autoOptimizeMinecraft = autoOptimizeMC,
            sustainedPerformanceMode = sustainedPerf,
            selectedRenderer = selectedRenderer
        )
        SettingsManager.updateSettings(context, updated)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
    ) {
        // =========================================================================
        // 1. TOP HEADER BAR (Minimalist Cyber-Glass matching Home, Accounts & Mods)
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Back to Dashboard Button (Standard 26.dp)
                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NuxColors.SurfaceElevated, RoundedCornerShape(6.dp))
                        .border(1.dp, NuxColors.CardBorder, RoundedCornerShape(6.dp))
                        .clickable { onNavigateBack() }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = NuxColors.ForestGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tr("settings.dashboard_nav"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Text(
                    text = tr("settings.title"),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 0.8.sp
                )

            }

            // Top Right: Language Dropdown Capsule & Hardware Specs Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Language Selector Capsule
                Box {
                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(NuxColors.SurfaceElevated, RoundedCornerShape(6.dp))
                            .border(1.dp, NuxColors.MintGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { isTopLangMenuExpanded = true }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = activeLanguage.flag, fontSize = 12.sp)
                            Text(
                                text = activeLanguage.name,
                                color = NuxColors.MintGreen,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Language",
                                tint = Color(0xFFA1A1AA),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isTopLangMenuExpanded,
                        onDismissRequest = { isTopLangMenuExpanded = false },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = Color(0xF212141C),
                        shadowElevation = 16.dp,
                        border = BorderStroke(1.dp, Color(0x33FFFFFF))
                    ) {
                        availableLanguages.forEach { lang ->
                            val isSelected = lang.code.equals(activeLanguage.code, ignoreCase = true)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(text = lang.flag, fontSize = 15.sp)
                                            Column {
                                                Text(
                                                    text = lang.name,
                                                    color = if (isSelected) NuxColors.MintGreen else Color.White,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 11.sp
                                                )
                                                if (lang.author.isNotBlank()) {
                                                    Text(
                                                        text = "By ${lang.author}",
                                                        color = Color(0xFFA1A1AA),
                                                        fontSize = 8.5.sp
                                                    )
                                                }
                                            }
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = NuxColors.ForestGreen,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    NuxLanguageManager.setLanguage(context, lang.code)
                                    isTopLangMenuExpanded = false
                                    Toast.makeText(context, "${lang.flag} ${lang.name}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NuxColors.SurfaceElevated, RoundedCornerShape(6.dp))
                        .border(1.dp, NuxColors.CardBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${totalRamMb / 1024} GB RAM",
                        color = NuxColors.MintGreen,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NuxColors.SurfaceElevated, RoundedCornerShape(6.dp))
                        .border(1.dp, NuxColors.CardBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$cpuCores CORES",
                        color = NuxColors.GrayNeutral,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // =========================================================================
        // 2. MAIN CONTENT PANELS (Profile Tab vs Game Preferences Tab)
        // =========================================================================
        run {
            // =====================================================================
            // GAME PREFERENCES TAB (Memory/Java/Graphics + Controls/Mouse)
            // =====================================================================
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // LEFT COLUMN: Memory & Java + Graphics
                NuxCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    backgroundColor = NuxColors.SurfaceElevated,
                    borderColor = NuxColors.CardBorder,
                    borderWidth = 1.dp,
                    cornerRadius = 8.dp,
                    fillMaxHeight = true
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Section 1: Java Memory (RAM)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Memory,
                                contentDescription = null,
                                tint = NuxColors.ForestGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("settings.section_ram"),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        // RAM Slider & Value Display
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NuxColors.SurfaceInput, RoundedCornerShape(6.dp))
                                .border(1.dp, NuxColors.CardBorder, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tr("settings.ram_max_label"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (ramMb > (totalRamMb * 0.75f)) NuxColors.Coral.copy(alpha = 0.2f) else NuxColors.ForestGreen.copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (ramMb > (totalRamMb * 0.75f)) NuxColors.Coral.copy(alpha = 0.4f) else NuxColors.MintGreen.copy(alpha = 0.3f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$ramMb MB (${String.format("%.1f", ramMb / 1024f)} GB)",
                                        color = if (ramMb > (totalRamMb * 0.75f)) NuxColors.Coral else NuxColors.MintGreen,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Slider(
                                value = ramMb.toFloat(),
                                onValueChange = {
                                    ramMb = ((it / 128).roundToInt() * 128).coerceIn(512, maxAllocatableRam)
                                    commitSettings()
                                },
                                valueRange = 512f..maxAllocatableRam.toFloat(),
                                steps = ((maxAllocatableRam - 512) / 128) - 1,
                                colors = SliderDefaults.colors(
                                    thumbColor = NuxColors.MintGreen,
                                    activeTrackColor = NuxColors.ForestGreen,
                                    inactiveTrackColor = NuxColors.CardBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Quick RAM Presets
                            Text(
                                text = tr("settings.ram_quick_presets"),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NuxColors.GrayNeutral
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(1024, 1536, 2048, 3072, 4096).forEach { preset ->
                                    if (preset <= maxAllocatableRam) {
                                        val isSelected = ramMb == preset
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(24.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (isSelected) NuxColors.ForestGreen else NuxColors.SurfaceElevated,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) NuxColors.MintGreen else NuxColors.CardBorder,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .clickable {
                                                    ramMb = preset
                                                    commitSettings()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (preset >= 1024) "${preset / 1024}G" else "${preset}M",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else NuxColors.GrayNeutral
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Custom JVM Arguments
                        Column {
                            Text(
                                text = tr("settings.jvm_args_label"),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = NuxColors.GrayNeutral
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            NuxTextField(
                                value = customJvmArgs,
                                onValueChange = {
                                    customJvmArgs = it
                                    commitSettings()
                                },
                                placeholder = tr("settings.jvm_args_placeholder")
                            )
                        }

                        HorizontalDivider(color = NuxColors.CardBorder, thickness = 0.5.dp)

                        // Section 2: Graphics & Rendering
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Speed,
                                contentDescription = null,
                                tint = NuxColors.ForestGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("settings.section_graphics"),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        // Renderer Selection Card
                        val activeRendererInfo = remember(selectedRenderer) {
                            NuxRendererRegistry.findRendererById(selectedRenderer)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(NuxColors.SurfaceInput)
                                .border(1.dp, NuxColors.CardBorder, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            // Top Row: Section label & GANTI button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tr("settings.renderer_card_title"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NuxColors.SurfaceElevated)
                                            .border(1.dp, NuxColors.CardBorder, RoundedCornerShape(4.dp))
                                            .clickable {
                                                showRendererDialog = true
                                            }
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "CHANGE",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NuxColors.MintGreen
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Active Renderer Name & Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = activeRendererInfo.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NuxColors.MintGreen
                                )
                                Box(
                                    modifier = Modifier
                                        .background(NuxColors.ForestGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .border(1.dp, NuxColors.MintGreen.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = activeRendererInfo.badge,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NuxColors.MintGreen,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = activeRendererInfo.summary,
                                fontSize = 9.5.sp,
                                color = NuxColors.GrayNeutral,
                                lineHeight = 13.sp
                            )
                        }

                        // Auto Optimize options.txt Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NuxColors.SurfaceInput, RoundedCornerShape(6.dp))
                                .border(1.dp, NuxColors.CardBorder, RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Optimize options.txt",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Automatically optimize visual settings (Clouds, Shadows) for peak FPS",
                                    fontSize = 9.5.sp,
                                    color = NuxColors.GrayNeutral
                                )
                            }
                            Switch(
                                checked = autoOptimizeMC,
                                onCheckedChange = {
                                    autoOptimizeMC = it
                                    commitSettings()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NuxColors.ForestGreen,
                                    uncheckedTrackColor = NuxColors.SurfaceElevated
                                )
                            )
                        }

                        // Resolution Scaling
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NuxColors.SurfaceInput, RoundedCornerShape(6.dp))
                                .border(1.dp, NuxColors.CardBorder, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = tr("settings.resolution_ratio_label"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "$resolutionRatio%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NuxColors.MintGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(50, 60, 75, 85, 100).forEach { ratio ->
                                    val isSelected = resolutionRatio == ratio
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (isSelected) NuxColors.ForestGreen else NuxColors.SurfaceElevated,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) NuxColors.MintGreen else NuxColors.CardBorder,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable {
                                                resolutionRatio = ratio
                                                commitSettings()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$ratio%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else NuxColors.GrayNeutral
                                        )
                                    }
                                }
                            }
                        }

                    }
                }

                // RIGHT COLUMN: In-Game Mouse & Touch Controls
                NuxCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    backgroundColor = NuxColors.SurfaceElevated,
                    borderColor = NuxColors.CardBorder,
                    borderWidth = 1.dp,
                    cornerRadius = 8.dp,
                    fillMaxHeight = true
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header for Mouse settings
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Mouse,
                                contentDescription = null,
                                tint = NuxColors.ForestGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("settings.section_mouse"),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        Text(
                            text = tr("settings.mouse_desc"),
                            fontSize = 9.5.sp,
                            color = NuxColors.GrayNeutral
                        )

                        // CUSTOM CONTROLS / GUI EDITOR ACTION CARD
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NuxColors.SurfaceInput, RoundedCornerShape(6.dp))
                                .border(1.dp, NuxColors.CardBorder, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Tune,
                                        contentDescription = null,
                                        tint = NuxColors.ForestGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = tr("settings.virtual_controls_title"),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tr("settings.virtual_controls_desc"),
                                    fontSize = 9.5.sp,
                                    color = NuxColors.GrayNeutral,
                                    lineHeight = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                NuxButton(
                                    onClick = onOpenGuiEditor,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(34.dp),
                                    backgroundColor = NuxColors.ForestGreen,
                                    contentColor = Color.White,
                                    cornerRadius = 6.dp
                                ) {
                                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tr("settings.btn_open_gui_editor"), fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }

                        // Mouse Control Mode Selection (SLIDE vs CLICK)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Trackpad / Slide Card
                            val isSlide = mouseMode == "SLIDE"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSlide) NuxColors.ForestGreen.copy(alpha = 0.15f) else NuxColors.SurfaceInput,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSlide) NuxColors.MintGreen else NuxColors.CardBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        mouseMode = "SLIDE"
                                        commitSettings()
                                    }
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.TouchApp,
                                            contentDescription = null,
                                            tint = if (isSlide) NuxColors.MintGreen else Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = tr("settings.mouse_mode_slide_title"),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSlide) NuxColors.MintGreen else Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = tr("settings.mouse_mode_slide_desc"),
                                        fontSize = 9.sp,
                                        color = NuxColors.GrayNeutral,
                                        lineHeight = 11.sp
                                    )
                                }
                            }

                            // Direct Touch / Click Card
                            val isClick = mouseMode == "CLICK"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isClick) NuxColors.ForestGreen.copy(alpha = 0.15f) else NuxColors.SurfaceInput,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isClick) NuxColors.MintGreen else NuxColors.CardBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        mouseMode = "CLICK"
                                        commitSettings()
                                    }
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.NearMe,
                                            contentDescription = null,
                                            tint = if (isClick) NuxColors.MintGreen else Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = tr("settings.mouse_mode_click_title"),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isClick) NuxColors.MintGreen else Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = tr("settings.mouse_mode_click_desc"),
                                        fontSize = 9.sp,
                                        color = NuxColors.GrayNeutral,
                                        lineHeight = 11.sp
                                    )
                                }
                            }
                        }

                        // In-Game Camera Look Sensitivity
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NuxColors.SurfaceInput, RoundedCornerShape(6.dp))
                                .border(1.dp, NuxColors.CardBorder, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tr("settings.camera_sensitivity_label"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .background(NuxColors.ForestGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .border(1.dp, NuxColors.MintGreen.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$captureSensitivity%",
                                        color = NuxColors.MintGreen,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Slider(
                                value = captureSensitivity.toFloat(),
                                onValueChange = {
                                    captureSensitivity = it.roundToInt()
                                    commitSettings()
                                },
                                valueRange = 50f..300f,
                                steps = 25,
                                colors = SliderDefaults.colors(
                                    thumbColor = NuxColors.MintGreen,
                                    activeTrackColor = NuxColors.ForestGreen,
                                    inactiveTrackColor = NuxColors.CardBorder
                                )
                            )
                        }

                        // Virtual Cursor Sensitivity
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NuxColors.SurfaceInput, RoundedCornerShape(6.dp))
                                .border(1.dp, NuxColors.CardBorder, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tr("settings.mouse_sensitivity_label"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .background(NuxColors.ForestGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .border(1.dp, NuxColors.MintGreen.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$cursorSensitivity%",
                                        color = NuxColors.MintGreen,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Slider(
                                value = cursorSensitivity.toFloat(),
                                onValueChange = {
                                    cursorSensitivity = it.roundToInt()
                                    commitSettings()
                                },
                                valueRange = 50f..300f,
                                steps = 25,
                                colors = SliderDefaults.colors(
                                    thumbColor = NuxColors.MintGreen,
                                    activeTrackColor = NuxColors.ForestGreen,
                                    inactiveTrackColor = NuxColors.CardBorder
                                )
                            )
                        }

                        // Virtual Cursor Size with Preview
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NuxColors.SurfaceInput, RoundedCornerShape(6.dp))
                                .border(1.dp, NuxColors.CardBorder, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tr("settings.mouse_size_label"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${mouseSizeDp}dp",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NuxColors.MintGreen
                                )
                            }
                            Slider(
                                value = mouseSizeDp.toFloat(),
                                onValueChange = {
                                    mouseSizeDp = it.roundToInt()
                                    commitSettings()
                                },
                                valueRange = 16f..48f,
                                steps = 16,
                                colors = SliderDefaults.colors(
                                    thumbColor = NuxColors.MintGreen,
                                    activeTrackColor = NuxColors.ForestGreen,
                                    inactiveTrackColor = NuxColors.CardBorder
                                )
                            )
                        }

                        // Physical Mouse Mode Card (Zalith Style: Auto-Hide Virtual Cursor)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NuxColors.SurfaceInput, RoundedCornerShape(6.dp))
                                .border(1.dp, NuxColors.CardBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Physical Mouse Mode (Auto-Hide)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Automatically hide virtual controls when external mouse is connected",
                                    fontSize = 9.sp,
                                    color = NuxColors.GrayNeutral
                                )
                            }
                            Switch(
                                checked = physicalMouseMode,
                                onCheckedChange = {
                                    physicalMouseMode = it
                                    commitSettings()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NuxColors.MintGreen,
                                    checkedTrackColor = NuxColors.ForestGreen,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = NuxColors.CardBorder
                                )
                            )
                        }

                        // Device Info & Reset Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            NuxButton(
                                onClick = {
                                    SettingsManager.resetToDefaults(context)
                                    val reset = SettingsManager.settings.value
                                    ramMb = reset.ramMb
                                    initialHeapMb = reset.initialHeapMb
                                    customJvmArgs = reset.customJvmArgs
                                    defaultRuntime = reset.defaultJavaRuntime
                                    mouseMode = reset.mouseControlMode
                                    cursorSensitivity = reset.cursorSensitivity
                                    captureSensitivity = reset.captureSensitivity
                                    mouseSizeDp = reset.mouseSizeDp
                                    physicalMouseMode = reset.physicalMouseMode
                                    resolutionRatio = reset.resolutionRatio
                                    autoOptimizeMC = reset.autoOptimizeMinecraft
                                    sustainedPerf = reset.sustainedPerformanceMode
                                    Toast.makeText(context, "Settings reset to defaults!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                backgroundColor = NuxColors.Coral.copy(alpha = 0.15f),
                                contentColor = NuxColors.Coral,
                                borderColor = NuxColors.Coral.copy(alpha = 0.35f),
                                cornerRadius = 6.dp
                            ) {
                                Text(
                                    text = "RESET DEFAULTS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = NuxColors.Coral
                                )
                            }

                            NuxButton(
                                onClick = {
                                    commitSettings()
                                    Toast.makeText(context, "All settings saved!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                backgroundColor = NuxColors.ForestGreen,
                                contentColor = Color.White,
                                cornerRadius = 6.dp
                            ) {
                                Text(
                                    text = "SAVE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Renderer Selection Dialog (Landscape Optimized with NuxDialog)
        if (showRendererDialog) {
            NuxDialog(
                onDismissRequest = { showRendererDialog = false },
                modifier = Modifier.fillMaxWidth(0.72f),
                fillMaxHeight = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Layers,
                                contentDescription = null,
                                tint = NuxColors.ForestGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "SELECT GRAPHICS RENDERER",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Match graphics backend with Minecraft version & device GPU",
                                    fontSize = 9.5.sp,
                                    color = NuxColors.GrayNeutral
                                )
                            }
                        }
                        IconButton(
                            onClick = { showRendererDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Scrollable Renderer List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(NuxRendererRegistry.availableRenderers) { rendererItem ->
                            val isSelected = selectedRenderer.equals(rendererItem.id, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSelected) NuxColors.ForestGreen.copy(alpha = 0.15f) else NuxColors.SurfaceInput
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) NuxColors.MintGreen else NuxColors.CardBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        selectedRenderer = rendererItem.id
                                        commitSettings()
                                        showRendererDialog = false
                                        Toast.makeText(context, "Renderer changed to: ${rendererItem.displayName}", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    selectedRenderer = rendererItem.id
                                                    commitSettings()
                                                    showRendererDialog = false
                                                    Toast.makeText(context, "Renderer changed to: ${rendererItem.displayName}", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = NuxColors.ForestGreen,
                                                    unselectedColor = NuxColors.CardBorder
                                                ),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = rendererItem.displayName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (isSelected) NuxColors.MintGreen else Color.White
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (isSelected) NuxColors.ForestGreen else NuxColors.SurfaceElevated,
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .border(1.dp, NuxColors.CardBorder, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = rendererItem.badge,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else NuxColors.GrayNeutral
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = rendererItem.summary,
                                        fontSize = 9.5.sp,
                                        color = NuxColors.GrayNeutral,
                                        lineHeight = 12.sp,
                                        modifier = Modifier.padding(start = 24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Compatibility: ${rendererItem.compatibility}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NuxColors.MintGreen,
                                        modifier = Modifier.padding(start = 24.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        NuxButton(
                            onClick = { showRendererDialog = false },
                            modifier = Modifier.height(30.dp),
                            backgroundColor = NuxColors.SurfaceInput,
                            contentColor = Color.White,
                            cornerRadius = 6.dp
                        ) {
                            Text("CLOSE", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
