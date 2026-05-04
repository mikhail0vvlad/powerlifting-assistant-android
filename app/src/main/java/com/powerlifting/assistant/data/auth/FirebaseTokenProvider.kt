package com.powerlifting.assistant.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class FirebaseTokenProvider @Inject constructor() {
    suspend fun bearerToken(forceRefresh: Boolean = false): String {
        val user = FirebaseAuth.getInstance().currentUser
            ?: error("User is not authenticated")

        val token = withTimeout(TOKEN_TIMEOUT) {
            user.getIdToken(forceRefresh).await().token
        } ?: error("Firebase token is null")

        return "Bearer $token"
    }

    private companion object {
        val TOKEN_TIMEOUT = 15.seconds
    }
}
