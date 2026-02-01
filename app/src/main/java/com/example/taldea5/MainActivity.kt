package com.example.taldea5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taldea5.ui.theme.SushiRed
import com.example.taldea5.ui.theme.Taldea5Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Taldea5Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot()
                }
            }
        }
    }
}

private enum class Screen { Login, Menu, Chat }

@Composable
private fun AppRoot() {
    var screen by remember { mutableStateOf(Screen.Login) }
    var workerName by remember { mutableStateOf<String?>(null) }
    
    // Saskia eta metatutako eskaerak hemen gorde, pantaila aldatzean ez galtzeko
    val sharedCart = remember { mutableStateMapOf<Int, Int>() }
    val sharedAccumulated = remember { mutableStateListOf<EskaeraLineRequest>() }

    when (screen) {
        Screen.Login -> LoginScreen(
            onLoginSuccess = { name ->
                workerName = name
                screen = Screen.Menu
            }
        )
        Screen.Menu -> MenuScreen(
            workerName = workerName,
            onLogout = {
                workerName = null
                sharedCart.clear() // Logout egitean saskia garbitu
                sharedAccumulated.clear()
                screen = Screen.Login
            },
            onOpenChat = {
                screen = Screen.Chat
            },
            sharedCart = sharedCart,
            sharedAccumulated = sharedAccumulated
        )
        Screen.Chat -> ChatScreen(
            host = "192.168.1.11", // "192.168.1.104"
            userName = workerName,
            onBack = {
                screen = Screen.Menu
            }
        )
    }
}

@Composable
private fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Row(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.shusinelli),
                contentDescription = "Logoa",
                modifier = Modifier.size(200.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(SushiRed)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text("Erabiltzailea:", fontSize = 18.sp, color = Color.White)
            OutlinedTextField(
                value = user,
                onValueChange = { user = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )

            Text("Pasahitza:", fontSize = 18.sp, color = Color.White)
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )

            Button(
                onClick = {
                    scope.launch {
                        loading = true
                        msg = ""
                        try {
                            val res = RetrofitClient.api.login(LoginRequest(user, pass))
                            if (res.isSuccessful && res.body() != null) {
                                onLoginSuccess(res.body()!!.izena)
                            } else if (res.code() == 401) {
                                msg = "Erabiltzailea edo pasahitza okerra da"
                            } else {
                                msg = "Errorea: ${res.code()}"
                            }
                        } catch (e: Exception) {
                            msg = "Errorea: ${e.message}"
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = SushiRed)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = SushiRed, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Text("Saioa hasi")
            }

            if (msg.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(msg, color = Color.White)
            }
        }
    }
}
