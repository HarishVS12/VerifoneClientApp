package com.harish.verifoneclientapp

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.harish.verifoneclientapp.ui.theme.VerifoneClientAppTheme
import com.harish.verifoneserverapp.CryptoAIDLInterface

class MainActivity : ComponentActivity() {

    private var cryptoAIDL: CryptoAIDLInterface? = null
    private var isServiceConnected = false


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            Log.d("AIDL Client", "Connected")
            cryptoAIDL = CryptoAIDLInterface.Stub.asInterface(service)
            isServiceConnected = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            cryptoAIDL = null
            isServiceConnected = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            val intent = Intent("com.harish.verifoneserverapp.CryptoAIDLInterface")
            intent.setPackage("com.harish.verifoneserverapp")
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        } catch (e: Exception) {
            Log.d("AIDL Client", "${e.printStackTrace()}")
        }

        setContent {
            VerifoneClientAppTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {
        var inputText by remember { mutableStateOf("") }
        var storedEncryptedText by remember { mutableStateOf("") }
        var decryptedText by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Enter Text") },
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //This text field is to show the Stored Encrypted Text
            Text(
                text = "Stored Encrypted Text: ${if (storedEncryptedText.isNotEmpty()) storedEncryptedText else "None"}",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            //This text field is to show the current decrypted Text
            Text(
                text = "Decrypted Text: ${if (decryptedText.isNotEmpty()) decryptedText else "None"}",
                modifier = Modifier.padding(bottom = 16.dp)
            )


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                //The button to encrypt the typed message.
                Button(
                    onClick = {
                        if (isServiceConnected) {
                            try {
                                val encryptedText = cryptoAIDL?.encrypt(inputText) ?: "Error"
                                storedEncryptedText = encryptedText
                                decryptedText = "" // Clear previous decrypted result
                            } catch (e: Exception) {
                                storedEncryptedText = "Encryption failed: ${e.message}"
                            }
                        } else {
                            storedEncryptedText = "Service not connected"
                        }
                    }
                ) {
                    Text("Encrypt")
                }

                // Decrypt Button
                Button(
                    onClick = {
                        if (isServiceConnected) {
                            try {
                                if (storedEncryptedText.isNotEmpty()) {
                                    val currDecrypted =
                                        cryptoAIDL?.decrypt(storedEncryptedText)
                                            ?: "Error"
                                    decryptedText = currDecrypted
                                } else {
                                    decryptedText = "No encrypted text stored"
                                }
                            } catch (e: Exception) {
                                decryptedText = "Decryption failed: ${e.message}"
                            }
                        } else {
                            decryptedText = "Service not connected"
                        }
                    }
                ) {
                    Text("Decrypt")
                }
            }
        }
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }
}