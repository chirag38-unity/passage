package com.tweener.passage.core

import com.tweener.passage.core.mapper.PassageUserMapper
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.DefaultEntrant
import com.tweener.passage.core.model.Entrant
import com.tweener.passage.core.model.PassageAuthResult
import com.tweener.passage.core.plugin.PassageAuthPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PassageCoreTest {

    private val testUser = mapOf(
        "id" to "test-uid",
        "email" to "test@example.com",
        "name" to "Test User",
    )

    private val testEntrant = DefaultEntrant(
        id = "test-uid",
        email = "test@example.com",
        displayName = "Test User",
    )

    private val testMapper = PassageUserMapper { backendUser ->
        @Suppress("UNCHECKED_CAST")
        val map = backendUser as Map<String, String>
        DefaultEntrant(
            id = map["id"]!!,
            email = map["email"],
            displayName = map["name"],
        )
    }

    private fun createPlugin(
        signInResult: Result<PassageAuthResult> = Result.success(PassageAuthResult(backendUser = testUser)),
        signUpResult: Result<PassageAuthResult> = Result.success(PassageAuthResult(backendUser = testUser)),
        currentUser: Any? = null,
        idTokenResult: Result<String> = Result.success("test-token"),
    ): FakeAuthPlugin = FakeAuthPlugin(
        signInResult = signInResult,
        signUpResult = signUpResult,
        initialUser = currentUser,
        idTokenResult = idTokenResult,
    )

    @Test
    fun signIn_success_returns_mapped_entrant() = runTest {
        val plugin = createPlugin()
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        val result = core.signIn(AuthCredential.EmailPassword("test@example.com", "password"))

        assertTrue(result.isSuccess)
        assertEquals(testEntrant, result.getOrNull())
    }

    @Test
    fun signIn_failure_returns_error() = runTest {
        val error = IllegalStateException("Auth failed")
        val plugin = createPlugin(signInResult = Result.failure(error))
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        val result = core.signIn(AuthCredential.EmailPassword("test@example.com", "wrong"))

        assertTrue(result.isFailure)
        assertEquals("Auth failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun signUp_success_returns_mapped_entrant() = runTest {
        val plugin = createPlugin()
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        val result = core.signUp(AuthCredential.EmailPassword("test@example.com", "password"))

        assertTrue(result.isSuccess)
        assertEquals(testEntrant, result.getOrNull())
    }

    @Test
    fun signUp_failure_returns_error() = runTest {
        val error = IllegalStateException("Email exists")
        val plugin = createPlugin(signUpResult = Result.failure(error))
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        val result = core.signUp(AuthCredential.EmailPassword("test@example.com", "password"))

        assertTrue(result.isFailure)
        assertEquals("Email exists", result.exceptionOrNull()?.message)
    }

    @Test
    fun signOut_delegates_to_plugin() = runTest {
        val plugin = createPlugin(currentUser = testUser)
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        core.signOut()

        assertTrue(plugin.signedOut)
    }

    @Test
    fun getCurrentUser_returns_mapped_entrant_when_logged_in() {
        val plugin = createPlugin(currentUser = testUser)
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        val entrant = core.getCurrentUser()

        assertNotNull(entrant)
        assertEquals("test-uid", entrant.id)
        assertEquals("test@example.com", entrant.email)
    }

    @Test
    fun getCurrentUser_returns_null_when_not_logged_in() {
        val plugin = createPlugin(currentUser = null)
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        assertNull(core.getCurrentUser())
    }

    @Test
    fun isUserLoggedIn_returns_true_when_user_exists() {
        val plugin = createPlugin(currentUser = testUser)
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        assertTrue(core.isUserLoggedIn())
    }

    @Test
    fun isUserLoggedIn_returns_false_when_no_user() {
        val plugin = createPlugin(currentUser = null)
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        assertFalse(core.isUserLoggedIn())
    }

    @Test
    fun observeAuthState_emits_mapped_entrant() = runTest {
        val plugin = createPlugin(currentUser = testUser)
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        val entrant = core.observeAuthState().first()

        assertNotNull(entrant)
        assertEquals("test-uid", entrant.id)
    }

    @Test
    fun isUserLoggedInAsFlow_emits_true_when_user_exists() = runTest {
        val plugin = createPlugin(currentUser = testUser)
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        val loggedIn = core.isUserLoggedInAsFlow().first()

        assertTrue(loggedIn)
    }

    @Test
    fun getIdToken_returns_token() = runTest {
        val plugin = createPlugin(idTokenResult = Result.success("my-id-token"))
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        val result = core.getIdToken()

        assertTrue(result.isSuccess)
        assertEquals("my-id-token", result.getOrNull())
    }

    @Test
    fun getIdToken_returns_error_when_no_user() = runTest {
        val plugin = createPlugin(idTokenResult = Result.failure(IllegalStateException("No user")))
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        val result = core.getIdToken()

        assertTrue(result.isFailure)
    }

    @Test
    fun deleteCurrentUser_delegates_to_plugin() = runTest {
        val plugin = createPlugin(currentUser = testUser)
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        core.deleteCurrentUser()

        assertTrue(plugin.userDeleted)
    }

    @Test
    fun reauthenticate_delegates_to_plugin() = runTest {
        val plugin = createPlugin()
        val core = PassageCore(authPlugin = plugin, userMapper = testMapper)

        val result = core.reauthenticate(AuthCredential.EmailPassword("test@example.com", "password"))

        assertTrue(result.isSuccess)
        assertTrue(plugin.reauthenticated)
    }
}

/**
 * Fake [PassageAuthPlugin] implementation for testing.
 */
private class FakeAuthPlugin(
    private val signInResult: Result<PassageAuthResult>,
    private val signUpResult: Result<PassageAuthResult>,
    private val initialUser: Any?,
    private val idTokenResult: Result<String>,
) : PassageAuthPlugin {

    var signedOut = false
        private set

    var userDeleted = false
        private set

    var reauthenticated = false
        private set

    private val _userFlow = MutableStateFlow(initialUser)

    override suspend fun signIn(credential: AuthCredential): Result<PassageAuthResult> = signInResult
    override suspend fun signUp(credential: AuthCredential): Result<PassageAuthResult> = signUpResult

    override suspend fun signOut() {
        signedOut = true
        _userFlow.value = null
    }

    override suspend fun reauthenticate(credential: AuthCredential): Result<Unit> {
        reauthenticated = true
        return Result.success(Unit)
    }

    override fun getCurrentUser(): Any? = _userFlow.value

    @Suppress("USELESS_CAST")
    override fun observeAuthState(): Flow<Any?> = _userFlow as Flow<Any?>

    override suspend fun getIdToken(forceRefresh: Boolean): Result<String> = idTokenResult

    override suspend fun deleteCurrentUser() {
        userDeleted = true
        _userFlow.value = null
    }
}
