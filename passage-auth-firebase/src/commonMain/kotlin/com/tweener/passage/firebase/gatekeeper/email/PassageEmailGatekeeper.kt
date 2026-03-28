package com.tweener.passage.firebase.gatekeeper.email

import com.tweener.kmpkit.thread.suspendCatching
import com.tweener.passage.core.error.PassageEmailAddressAlreadyExistsException
import com.tweener.passage.core.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.core.error.PassageInvalidCredentialsException
import com.tweener.passage.core.error.PassageNoUserMatchingEmailException
import com.tweener.passage.core.error.PassageSignInLinkToEmailException
import com.tweener.passage.core.error.PassageTooManyRequestsException
import com.tweener.passage.core.error.PassageWeakPasswordException
import com.tweener.passage.core.gatekeeper.PassageGatekeeper
import com.tweener.passage.core.model.Entrant
import com.tweener.passage.firebase.gatekeeper.email.model.PassageEmailAuthParams
import com.tweener.passage.firebase.gatekeeper.email.model.PassageEmailVerificationParams
import com.tweener.passage.firebase.gatekeeper.email.model.PassageForgotPasswordParams
import com.tweener.passage.firebase.gatekeeper.email.model.PassageSignInLinkToEmailParams
import com.tweener.passage.firebase.mapper.FirebaseEntrantMapper
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseTooManyRequestsException
import dev.gitlive.firebase.auth.ActionCodeResult
import dev.gitlive.firebase.auth.ActionCodeSettings
import dev.gitlive.firebase.auth.AndroidPackageName
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseAuthWeakPasswordException
import dev.gitlive.firebase.auth.auth
import kotlin.jvm.JvmInline

@JvmInline
value class EmailAddress(val email: String)

internal class PassageEmailGatekeeper(
    private val firebaseAuth: FirebaseAuth,
) : PassageGatekeeper<PassageEmailAuthParams>() {

    private val entrantMapper = FirebaseEntrantMapper()

    override suspend fun signIn(params: PassageEmailAuthParams): Result<Entrant> = suspendCatching {
        firebaseAuth.signInWithEmailAndPassword(email = params.email, password = params.password).user?.let { entrantMapper.map(it) }
            ?: throw PassageGatekeeperUnknownEntrantException()
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't sign in the user: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

    override suspend fun signOut() {
        // Nothing to do here
    }

    suspend fun signUp(params: PassageEmailAuthParams): Result<Entrant> = suspendCatching {
        firebaseAuth.createUserWithEmailAndPassword(email = params.email, password = params.password).user?.let { entrantMapper.map(it) }
            ?: throw PassageGatekeeperUnknownEntrantException()
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't sign up the user: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

    suspend fun reauthenticate(params: PassageEmailAuthParams): Result<Unit> = suspendCatching {
        val firebaseCredential = EmailAuthProvider.credential(email = params.email, password = params.password)
        firebaseAuth.currentUser?.reauthenticate(credential = firebaseCredential)
            ?: throw PassageGatekeeperUnknownEntrantException()
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't reauthenticate the user: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

    suspend fun sendPasswordResetEmail(params: PassageForgotPasswordParams): Result<Unit> = suspendCatching {
        val actionCodeSettings = buildActionCodeSettings(
            url = params.url,
            linkDomain = params.hostingDomain,
            iOSBundleId = params.iosParams?.bundleId,
            androidPackageName = params.androidParams?.packageName,
            installIfNotAvailable = params.androidParams?.installIfNotAvailable ?: true,
            minimumVersion = params.androidParams?.minimumVersion,
            canHandleCodeInApp = params.canHandleCodeInApp,
        )

        firebaseAuth.sendPasswordResetEmail(email = params.email, actionCodeSettings = actionCodeSettings)
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't send reset password email: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

    suspend fun handlePasswordResetCode(oobCode: String): Result<EmailAddress> = suspendCatching {
        val email = firebaseAuth.verifyPasswordResetCode(code = oobCode)
        EmailAddress(email = email)
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't verify the oobCode ($oobCode) from the password reset email: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

    suspend fun confirmResetPassword(oobCode: String, newPassword: String): Result<Unit> = suspendCatching {
        firebaseAuth.confirmPasswordReset(code = oobCode, newPassword = newPassword)
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't confirm the password reset: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

    suspend fun sendEmailVerification(params: PassageEmailVerificationParams): Result<Unit> = suspendCatching {
        val actionCodeSettings = buildActionCodeSettings(
            url = params.url,
            linkDomain = params.hostingDomain,
            iOSBundleId = params.iosParams?.bundleId,
            androidPackageName = params.androidParams?.packageName,
            installIfNotAvailable = params.androidParams?.installIfNotAvailable ?: true,
            minimumVersion = params.androidParams?.minimumVersion,
            canHandleCodeInApp = params.canHandleCodeInApp,
        )

        firebaseAuth.currentUser?.sendEmailVerification(actionCodeSettings = actionCodeSettings)
            ?: throw PassageGatekeeperUnknownEntrantException()
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't send email address verification email: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

    suspend fun handleEmailVerificationCode(oobCode: String) =
        handleOobCode<ActionCodeResult.VerifyEmail>(oobCode = oobCode).fold(
            onSuccess = { Result.success(it) },
            onFailure = { throwable ->
                println("Couldn't verify the oobCode ($oobCode) from the email verification email: $throwable")
                Result.failure(mapFirebaseAuthError(throwable))
            }
        )

    suspend fun sendSignInLinkToEmail(params: PassageSignInLinkToEmailParams): Result<Unit> = suspendCatching {
        val actionCodeSettings = buildActionCodeSettings(
            url = params.url,
            linkDomain = params.hostingDomain,
            iOSBundleId = params.iosParams?.bundleId,
            androidPackageName = params.androidParams?.packageName,
            installIfNotAvailable = params.androidParams?.installIfNotAvailable ?: true,
            minimumVersion = params.androidParams?.minimumVersion,
            canHandleCodeInApp = params.canHandleCodeInApp,
        )

        firebaseAuth.sendSignInLinkToEmail(email = params.email, actionCodeSettings = actionCodeSettings)
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't send sign-in link to email: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

    suspend fun handleSignInLinkToEmail(email: String, emailLink: String): Result<Entrant> = suspendCatching {
        if (firebaseAuth.isSignInWithEmailLink(link = emailLink).not()) throw PassageSignInLinkToEmailException()

        firebaseAuth.signInWithEmailLink(email = email, link = emailLink).user?.let { entrantMapper.map(it) }
            ?: throw PassageGatekeeperUnknownEntrantException()
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't sign in the user with the email link: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

    private fun buildActionCodeSettings(
        url: String,
        linkDomain: String,
        iOSBundleId: String?,
        androidPackageName: String?,
        installIfNotAvailable: Boolean,
        minimumVersion: String?,
        canHandleCodeInApp: Boolean,
    ): ActionCodeSettings =
        ActionCodeSettings(
            url = url,
            linkDomain = linkDomain,
            androidPackageName = androidPackageName?.let { AndroidPackageName(packageName = it, installIfNotAvailable = installIfNotAvailable, minimumVersion = minimumVersion) },
            iOSBundleId = iOSBundleId,
            canHandleCodeInApp = canHandleCodeInApp,
        )

    private suspend fun <T : ActionCodeResult> handleOobCode(oobCode: String) = suspendCatching {
        Firebase.auth.checkActionCode<T>(code = oobCode)
        Firebase.auth.applyActionCode(code = oobCode)
        Firebase.auth.currentUser?.reload() ?: Unit
    }

    private fun mapFirebaseAuthError(throwable: Throwable): Throwable =
        when (throwable) {
            is FirebaseAuthInvalidUserException -> PassageNoUserMatchingEmailException()
            is FirebaseAuthInvalidCredentialsException -> PassageInvalidCredentialsException()
            is FirebaseAuthUserCollisionException -> PassageEmailAddressAlreadyExistsException()
            is FirebaseAuthWeakPasswordException -> PassageWeakPasswordException()
            is FirebaseTooManyRequestsException -> PassageTooManyRequestsException()
            else -> throwable
        }
}
