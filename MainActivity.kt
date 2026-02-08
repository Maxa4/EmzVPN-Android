package com.freevpn.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNThread
import de.blinkt.openvpn.core.VpnStatus
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    
    private lateinit var btnConnect: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvIpAddress: TextView
    private lateinit var serverRecyclerView: RecyclerView
    private lateinit var tvSelectedServer: TextView
    
    private var isConnected = false
    private var selectedServer: VpnServer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupServerList()
        setupVpnStatusListener()
        checkCurrentIp()
    }
    
    private fun initViews() {
        btnConnect = findViewById(R.id.btnConnect)
        tvStatus = findViewById(R.id.tvStatus)
        tvIpAddress = findViewById(R.id.tvIpAddress)
        tvSelectedServer = findViewById(R.id.tvSelectedServer)
        serverRecyclerView = findViewById(R.id.serverRecyclerView)
        
        btnConnect.setOnClickListener {
            if (selectedServer == null) {
                Toast.makeText(this, "Выберите сервер", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (isConnected) {
                disconnectVpn()
            } else {
                connectToVpn(selectedServer!!)
            }
        }
        
        // Кнопка обновления IP
        findViewById<Button>(R.id.btnRefreshIp).setOnClickListener {
            checkCurrentIp()
        }
    }
    
    private fun setupServerList() {
        val servers = listOf(
            VpnServer(
                id = "de1",
                name = "Германия #1 (Берлин)",
                country = "DE",
                flag = "🇩🇪",
                config = """
                    client
                    dev tun
                    proto udp
                    remote de.vpnbook.com 53
                    resolv-retry infinite
                    nobind
                    persist-key
                    persist-tun
                    cipher AES-256-CBC
                    auth sha256
                    comp-lzo
                    verb 3
                    auth-user-pass
                """.trimIndent(),
                username = "vpnbook",
                password = "ruf8peb"
            ),
            VpnServer(
                id = "de2",
                name = "Германия #2 (Франкфурт)",
                country = "DE",
                flag = "🇩🇪",
                config = """
                    client
                    dev tun
                    proto udp
                    remote de217.vpnbook.com 53
                    resolv-retry infinite
                    nobind
                    persist-key
                    persist-tun
                    cipher AES-256-CBC
                    auth sha256
                    comp-lzo
                    verb 3
                    auth-user-pass
                """.trimIndent(),
                username = "vpnbook",
                password = "ruf8peb"
            ),
            VpnServer(
                id = "us1",
                name = "США #1 (Нью-Йорк)",
                country = "US",
                flag = "🇺🇸",
                config = """
                    client
                    dev tun
                    proto udp
                    remote us1.vpnbook.com 53
                    resolv-retry infinite
                    nobind
                    persist-key
                    persist-tun
                    cipher AES-256-CBC
                    auth sha256
                    verb 3
                    auth-user-pass
                """.trimIndent(),
                username = "vpnbook",
                password = "ruf8peb"
            )
        )
        
        val adapter = ServerAdapter(servers) { server ->
            selectedServer = server
            tvSelectedServer.text = "Выбран: ${server.name}"
        }
        
        serverRecyclerView.layoutManager = LinearLayoutManager(this)
        serverRecyclerView.adapter = adapter
    }
    
    private fun connectToVpn(server: VpnServer) {
        try {
            // Запрашиваем разрешение на VPN
            val intent = android.net.VpnService.prepare(this)
            if (intent != null) {
                startActivityForResult(intent, 1)
                return
            }
            
            // Запускаем VPN соединение
            OpenVpnApi.startVpn(
                this,
                server.config,
                server.country,
                server.username,
                server.password,
                "FreeVPN"
            )
            
            tvStatus.text = "Подключение..."
            btnConnect.text = "ОТКЛЮЧИТЬ"
            btnConnect.setBackgroundColor(getColor(android.R.color.holo_red_dark))
            
            Toast.makeText(this, "Подключаемся к ${server.name}...", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun disconnectVpn() {
        OpenVPNThread.stop()
        isConnected = false
        tvStatus.text = "Отключено"
        btnConnect.text = "ПОДКЛЮЧИТЬ"
        btnConnect.setBackgroundColor(getColor(android.R.color.holo_green_dark))
        checkCurrentIp()
    }
    
    private fun setupVpnStatusListener() {
        VpnStatus.addStateListener(object : VpnStatus.StateListener {
            override fun updateState(state: String?, logmessage: String?, localizedResId: Int) {
                runOnUiThread {
                    tvStatus.text = state ?: "Неизвестно"
                    
                    when (state) {
                        "CONNECTED" -> {
                            isConnected = true
                            Toast.makeText(this@MainActivity, "VPN подключен!", Toast.LENGTH_SHORT).show()
                            checkCurrentIp()
                        }
                        "DISCONNECTED" -> {
                            isConnected = false
                            btnConnect.text = "ПОДКЛЮЧИТЬ"
                            btnConnect.setBackgroundColor(getColor(android.R.color.holo_green_dark))
                        }
                        "AUTH" -> tvStatus.text = "Авторизация..."
                        "WAIT" -> tvStatus.text = "Ожидание..."
                        "RECONNECTING" -> tvStatus.text = "Переподключение..."
                    }
                }
            }
        })
    }
    
    private fun checkCurrentIp() {
        Thread {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()
                
                val request = Request.Builder()
                    .url("https://api.ipify.org?format=json")
                    .build()
                
                val response = client.newCall(request).execute()
                val json = response.body?.string()
                val ip = JSONObject(json).getString("ip")
                
                runOnUiThread {
                    tvIpAddress.text = "Ваш IP: $ip"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    tvIpAddress.text = "IP: Недоступен"
                }
            }
        }.start()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && selectedServer != null) {
            connectToVpn(selectedServer!!)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isConnected) {
            disconnectVpn()
        }
    }
}

data class VpnServer(
    val id: String,
    val name: String,
    val country: String,
    val flag: String,
    val config: String,
    val username: String,
    val password: String
)
