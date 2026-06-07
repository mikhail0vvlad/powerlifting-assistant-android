package com.powerlifting.assistant.presentation.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

private const val MIN_PASSWORD_LENGTH = 8

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    val context = LocalContext.current

    // R3: prevent screenshots / Recent-apps thumbnails from capturing the
    // password field. Restore on dispose so other screens can still be shared.
    DisposableEffect(context) {
        val window = (context as? Activity)?.window
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRegister) "Регистрация" else "Вход",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль (мин. $MIN_PASSWORD_LENGTH символов)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
            info?.let {
                Spacer(Modifier.height(12.dp))
                Text(text = it, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    error = null
                    info = null
                    loading = true
                    val trimmedEmail = email.trim()
                    if (isRegister) {
                        auth.createUserWithEmailAndPassword(trimmedEmail, password)
                            .addOnSuccessListener {
                                // R1: send verification email and sign the user
                                // out — they must confirm before getting an API
                                // session (server enforces email_verified too).
                                val user = auth.currentUser
                                user?.sendEmailVerification()
                                    ?.addOnCompleteListener {
                                        auth.signOut()
                                        loading = false
                                        info = "Аккаунт создан. Подтвердите почту и войдите снова."
                                    }
                                    ?: run {
                                        auth.signOut()
                                        loading = false
                                        info = "Аккаунт создан. Подтвердите почту и войдите снова."
                                    }
                            }
                            .addOnFailureListener { e ->
                                error = e.message
                                loading = false
                            }
                    } else {
                        auth.signInWithEmailAndPassword(trimmedEmail, password)
                            .addOnSuccessListener {
                                // R1: gate the entire app behind a verified email.
                                // Reload first so the cached User reflects any
                                // verification that happened on another device.
                                val user = auth.currentUser
                                user?.reload()
                                    ?.addOnCompleteListener {
                                        val verified = auth.currentUser?.isEmailVerified == true
                                        if (verified) {
                                            loading = false
                                            onAuthSuccess()
                                        } else {
                                            auth.currentUser?.sendEmailVerification()
                                            auth.signOut()
                                            loading = false
                                            info = "Подтвердите почту — мы выслали письмо повторно."
                                        }
                                    }
                                    ?: run {
                                        loading = false
                                        onAuthSuccess()
                                    }
                            }
                            .addOnFailureListener { e ->
                                error = e.message
                                loading = false
                            }
                    }
                },
                enabled = !loading && email.isNotBlank() && password.length >= MIN_PASSWORD_LENGTH,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                }
                Text(if (isRegister) "Создать аккаунт" else "Войти")
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = { isRegister = !isRegister; error = null; info = null }) {
                Text(if (isRegister) "Уже есть аккаунт? Войти" else "Нет аккаунта? Регистрация")
            }
        }
    }
}
