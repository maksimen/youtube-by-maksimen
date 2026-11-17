package com.maksimen.youtubehub

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val VPN_PACKAGE = "io.nimbusnet.app"
private const val YOUTUBE_PACKAGE = "com.google.android.youtube"

class MainActivity : ComponentActivity() {
    private var launchAfterVpn by mutableStateOf(false)
    private var statusText by mutableStateOf("Готово к запуску")
    private var vpnConnected by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshVpn()
        setContent {
            HubScreen(
                vpnConnected = vpnConnected,
                statusText = statusText,
                onOpen = { startFlow() },
                onCheck = { refreshVpn() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshVpn()
        if (launchAfterVpn) {
            if (vpnConnected) {
                launchAfterVpn = false
                openYouTube()
            } else {
                statusText = "VPN пока не обнаружен. Вернись в VPNUS и подключись."
            }
        }
    }

    private fun startFlow() {
        refreshVpn()
        if (vpnConnected) {
            openYouTube()
            return
        }
        launchAfterVpn = true
        val vpnIntent = packageManager.getLaunchIntentForPackage(VPN_PACKAGE)
        if (vpnIntent == null) {
            launchAfterVpn = false
            statusText = "VPNUS не найден. Проверь, установлен ли io.nimbusnet.app."
            return
        }
        statusText = "Открываю VPNUS. После подключения вернись сюда."
        startActivity(vpnIntent)
    }

    private fun refreshVpn() {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val caps = network?.let { cm.getNetworkCapabilities(it) }
        vpnConnected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        if (vpnConnected) statusText = "VPN-подключение обнаружено"
    }

    private fun openYouTube() {
        val intent = packageManager.getLaunchIntentForPackage(YOUTUBE_PACKAGE)
        if (intent == null) {
            statusText = "YouTube ReVanced не найден."
            return
        }
        statusText = "Запускаю YouTube"
        startActivity(intent)
    }
}

@Composable
private fun HubScreen(
    vpnConnected: Boolean,
    statusText: String,
    onOpen: () -> Unit,
    onCheck: () -> Unit
) {
    val bg = Color(0xFF0B1413)
    val panel = Color(0xFF142522)
    val mint = Color(0xFF43D9B2)
    MaterialTheme(colorScheme = darkColorScheme(
        background = bg, surface = panel, primary = mint,
        onPrimary = Color(0xFF06231B), onBackground = Color.White,
        onSurface = Color.White
    )) {
        Surface(Modifier.fillMaxSize(), color = bg) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("▶", color = Color(0xFFFF3535), fontSize = 48.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("YouTube by maksimen", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("ТВОЙ YOUTUBE. ТВОЙ VPN.", color = Color(0xFF9BB8B1), fontSize = 12.sp)
                Spacer(Modifier.height(36.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = panel),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = mint)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("VPNUS", fontWeight = FontWeight.Bold)
                            Text(if (vpnConnected) "Подключён" else "Ожидает подключения",
                                color = if (vpnConnected) mint else Color(0xFFB8C7C3),
                                fontSize = 13.sp)
                        }
                        Button(onClick = onCheck) { Text("Проверить") }
                    }
                }
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = onOpen,
                    modifier = Modifier.fillMaxWidth().height(66.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE52D35))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ОТКРЫТЬ YOUTUBE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(18.dp))
                Text(statusText, color = Color(0xFF9BB8B1), fontSize = 13.sp)
                Spacer(Modifier.height(28.dp))
                Text("VPNUS → проверка VPN → YouTube ReVanced",
                    color = Color(0xFF668078), fontSize = 11.sp)
            }
        }
    }
}
