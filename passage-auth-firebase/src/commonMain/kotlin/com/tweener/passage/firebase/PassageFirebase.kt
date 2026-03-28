package com.tweener.passage.firebase

import androidx.compose.runtime.Composable
import com.tweener.passage.core.Passage
import com.tweener.passage.core.error.PassageGatekeeperNotConfiguredException
import com.tweener.passage.core.model.Entrant
import com.tweener.passage.core.model.GatekeeperType
import com.tweener.passage.core.model.PassageUniversalLink
import com.tweener.passage.firebase.gatekeeper.apple.PassageAppleGatekeeper
import com.tweener.passage.firebase.gatekeeper.email.EmailAddress
import com.tweener.passage.firebase.gatekeeper.email.PassageEmailGatekeeper
import com.tweener.passage.firebase.gatekeeper.email.model.PassageEmailAuthParams
import com.tweener.passage.firebase.gatekeeper.email.model.PassageEmailVerificationParams
import com.tweener.passage.firebase.gatekeeper.email.model.PassageForgotPasswordParams
import com.tweener.passage.firebase.gatekeeper.email.model.PassageResetPasswordParams
import com.tweener.passage.firebase.gatekeeper.email.model.PassageSignInLinkToEmailParams
import com.tweener.passage.firebase.gatekeeper.google.PassageGoogleGatekeeper
import com.tweener.passage.firebase.mapper.FirebaseEntrantMapper
import com.tweener.passage.firebase.model.AppleGatekeeperConfiguration
import com.tweener.passage.firebase.model.EmailPasswordGatekeeperConfiguration
import com.tweener.passage.firebase.model.GoogleGatekeeperConfiguration
import com.tweener.passage.core.model.PassageGatekeeperConfiguration
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Firebase-specific implementation of [Passage] that handles Firebase authentication operations.
 *
 * @author Vivien Mahe
 * @since 30/11/2024
 */
abstract class PassageFirebase : Passage() {

    protected lateinit var firebaseAuth: FirebaseAuth

    private val entrantMapper = FirebaseEntrantMapper()

    private var googleGatekeeper: PassageGoogleGatekeeper? = null
    private var appleGatekeeper: PassageAppleGatekeeper? = null
    private var emailGatekeeper: PassageEmailGatekeeper? = null

    /**
     * Initializes the Passage library with the specified list of Gatekeeper configurations.
     */
    fun initialize(gatekeeperConfigurations: List<PassageGatekeeperConfiguration>) {
        initializeFirebase()
        initialize(gatekeeperConfigurations = gatekeeperConfigurations, firebase = Firebase)
    }

    /**
     * Initializes the Passage library with the specified list of Gatekeeper configurations and Firebase instance.
     */
    fun initialize(gatekeeperConfigurations: List<PassageGatekeeperConfiguration>, firebase: Firebase) {
        firebaseAuth = firebase.auth

        gatekeeperConfigurations.forEach { configuration ->
            if (configuration is GoogleGatekeeperConfiguration) googleGatekeeper = createGoogleGatekeeper(configuration = configuration, firebaseAuth = firebaseAuth)
            if (configuration is AppleGatekeeperConfiguration) appleGatekeeper = createAppleGatekeeper(configuration = configuration)
            if (configuration is EmailPasswordGatekeeperConfiguration) emailGatekeeper = createEmailGatekeeper(configuration = configuration)
        }

        println("Passage is initialized.")
    }

    @Composable
    abstract fun bindToView()

    override fun getCurrentUser(): Entrant? =
        firebaseAuth.currentUser?.let { entrantMapper.map(it) }

    override fun getCurrentUserAsFlow(): Flow<Entrant?> =
        firebaseAuth.authStateChanged.map { it?.let { user -> entrantMapper.map(user) } }

    override fun isUserLoggedInAsFlow(): Flow<Boolean> =
        getCurrentUserAsFlow().map { it != null }

    override suspend fun signOut() {
        googleGatekeeper?.signOut()
        appleGatekeeper?.signOut()
        emailGatekeeper?.signOut()
        firebaseAuth.signOut()
    }

    override suspend fun deleteCurrentUser() {
        firebaseAuth.currentUser?.delete()
    }

    override suspend fun getIdToken(forceRefresh: Boolean): Result<String> =
        firebaseAuth.currentUser
            ?.getIdToken(forceRefresh = forceRefresh)
            ?.let { token -> Result.success(token) }
            ?: Result.failure(IllegalStateException("No authenticated user found to retrieve ID token."))

    protected abstract fun initializeFirebase()

    // region Google gatekeeper

    suspend fun authenticateWithGoogle(): Result<Entrant> =
        googleGatekeeper
            ?.signIn(Unit)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.GOOGLE))

    suspend fun reauthenticateWithGoogle(): Result<Unit> =
        googleGatekeeper
            ?.reauthenticate()
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.GOOGLE))

    internal abstract fun createGoogleGatekeeper(configuration: GoogleGatekeeperConfiguration, firebaseAuth: FirebaseAuth): PassageGoogleGatekeeper

    // endregion Google gatekeeper

    // region Apple gatekeeper

    suspend fun authenticateWithApple(): Result<Entrant> =
        appleGatekeeper
            ?.signIn(Unit)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.APPLE))

    internal abstract fun createAppleGatekeeper(configuration: AppleGatekeeperConfiguration): PassageAppleGatekeeper

    // endregion Apple gatekeeper

    // region Email & Password gatekeeper

    private fun createEmailGatekeeper(configuration: EmailPasswordGatekeeperConfiguration): PassageEmailGatekeeper = PassageEmailGatekeeper(firebaseAuth = firebaseAuth)

    suspend fun authenticateWithEmailAndPassword(params: PassageEmailAuthParams): Result<Entrant> =
        emailGatekeeper
            ?.signIn(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    suspend fun createUserWithEmailAndPassword(params: PassageEmailAuthParams): Result<Entrant> =
        emailGatekeeper
            ?.signUp(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    suspend fun reauthenticateWithEmailAndPassword(params: PassageEmailAuthParams): Result<Unit> =
        emailGatekeeper
            ?.reauthenticate(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    suspend fun sendPasswordResetEmail(params: PassageForgotPasswordParams): Result<Unit> =
        emailGatekeeper
            ?.sendPasswordResetEmail(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    suspend fun handlePasswordResetCode(oobCode: String): Result<EmailAddress> =
        emailGatekeeper
            ?.handlePasswordResetCode(oobCode = oobCode)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    suspend fun confirmResetPassword(params: PassageResetPasswordParams): Result<Unit> =
        emailGatekeeper
            ?.confirmResetPassword(oobCode = params.oobCode, newPassword = params.newPassword)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    suspend fun sendEmailVerification(params: PassageEmailVerificationParams): Result<Unit> =
        emailGatekeeper
            ?.sendEmailVerification(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    suspend fun handleEmailVerificationCode(oobCode: String): Result<Unit> =
        emailGatekeeper
            ?.handleEmailVerificationCode(oobCode = oobCode)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    suspend fun sendSignInLinkToEmail(params: PassageSignInLinkToEmailParams): Result<Unit> =
        emailGatekeeper
            ?.sendSignInLinkToEmail(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    suspend fun handleSignInLinkToEmail(email: String, emailLink: String): Result<Entrant> =
        emailGatekeeper
            ?.handleSignInLinkToEmail(email = email, emailLink = emailLink)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    // endregion Email & Password gatekeeper
}
