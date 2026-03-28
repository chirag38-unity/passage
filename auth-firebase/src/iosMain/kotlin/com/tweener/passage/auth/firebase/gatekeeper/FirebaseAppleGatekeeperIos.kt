package com.tweener.passage.auth.firebase.gatekeeper

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIROAuthProvider
import com.tweener.kmpkit.contract.requireNotNullOrThrow
import com.tweener.kmpkit.thread.resumeIfActive
import com.tweener.kmpkit.thread.resumeWithExceptionIfActive
import com.tweener.passage.auth.firebase.error.PassageAppleGatekeeperException
import com.tweener.passage.auth.firebase.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.core.model.PassageAuthResult
import dev.gitlive.firebase.auth.FirebaseAuth
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import kotlin.coroutines.cancellation.CancellationException

/**
 * iOS implementation of [FirebaseAppleGatekeeper].
 *
 * Uses the ASAuthorizationAppleIDProvider to perform Apple Sign-In on iOS,
 * generates a secure nonce, and authenticates with Firebase using the extracted tokens.
 *
 * @param firebaseAuth The Firebase Authentication instance.
 */
class FirebaseAppleGatekeeperIos(
    private val firebaseAuth: FirebaseAuth,
) : FirebaseAppleGatekeeper() {

    private lateinit var delegate: AuthorizationControllerDelegate
    private val presentationContextProvider = PresentationContextProvider()

    override suspend fun signIn(): Result<PassageAuthResult> = suspendCancellableCoroutine { continuation ->
        try {
            val rawNonce = AppleNonceFactory.createRandomNonceString()
            delegate = AuthorizationControllerDelegate(firebaseAuth = firebaseAuth, nonce = rawNonce) { result ->
                continuation.resumeIfActive(result)
            }

            continuation.invokeOnCancellation {
                println("Canceled Apple Sign In on iOS")
                delegate.onResponse = {}
                continuation.resumeWithExceptionIfActive(CancellationException())
            }

            val request = ASAuthorizationAppleIDProvider().createRequest().apply {
                requestedScopes = listOf(ASAuthorizationScopeEmail, ASAuthorizationScopeFullName)
                nonce = AppleNonceFactory.sha256(rawNonce)
            }

            ASAuthorizationController(authorizationRequests = listOf(request)).apply {
                delegate = this@FirebaseAppleGatekeeperIos.delegate
                presentationContextProvider = this@FirebaseAppleGatekeeperIos.presentationContextProvider
                performRequests()
            }
        } catch (throwable: Throwable) {
            println("An error occurred while signing in with Apple provider: $throwable")
            continuation.resumeWithExceptionIfActive(throwable)
        }
    }

    override suspend fun signOut() {
        // Nothing to do here for iOS Apple Sign-In
    }
}

private class AuthorizationControllerDelegate(
    private val firebaseAuth: FirebaseAuth,
    private val nonce: String,
    var onResponse: (Result<PassageAuthResult>) -> Unit,
) : ASAuthorizationControllerDelegateProtocol, NSObject() {

    @OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
    override fun authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization: ASAuthorization) {
        try {
            val appleIDCredential = didCompleteWithAuthorization.credential as ASAuthorizationAppleIDCredential

            val appleIDToken = appleIDCredential.identityToken
            requireNotNullOrThrow(appleIDToken) { PassageAppleGatekeeperException(message = "Unable to fetch identity token") }

            val idTokenString = NSString.create(data = appleIDToken, encoding = NSUTF8StringEncoding)?.toString()
            requireNotNullOrThrow(idTokenString) { PassageAppleGatekeeperException(message = "Unable to serialize token string from data: ${appleIDToken.debugDescription}") }

            val credential = FIROAuthProvider.appleCredentialWithIDToken(idToken = idTokenString, rawNonce = nonce, fullName = appleIDCredential.fullName)

            FIRAuth.auth().signInWithCredential(credential) { authResult, error ->
                error?.let { println("Couldn't sign in with Apple on iOS! $error") }

                when {
                    error != null || authResult == null -> onResponse(Result.failure(PassageAppleGatekeeperException(message = "FIRAuthDataResult is null")))

                    else -> {
                        val user = firebaseAuth.currentUser
                        if (user != null) {
                            onResponse(Result.success(PassageAuthResult(backendUser = user)))
                        } else {
                            onResponse(Result.failure(PassageGatekeeperUnknownEntrantException()))
                        }
                    }
                }
            }
        } catch (throwable: Throwable) {
            onResponse(Result.failure(throwable))
        }
    }

    override fun authorizationController(controller: ASAuthorizationController, didCompleteWithError: NSError) {
        println("Didn't get authorization to sign in with Apple: $didCompleteWithError")
        onResponse(Result.failure(PassageAppleGatekeeperException(message = didCompleteWithError.localizedFailureReason)))
    }
}

private class PresentationContextProvider : ASAuthorizationControllerPresentationContextProvidingProtocol, NSObject() {

    override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): ASPresentationAnchor =
        UIApplication.sharedApplication.keyWindow?.rootViewController?.view?.window
}
