package com.tweener.passage.firebase.model

import com.tweener.passage.core.model.PassageGatekeeperConfiguration

// region Google

data class GoogleGatekeeperConfiguration(
    val serverClientId: String,
    val android: GoogleGatekeeperAndroidConfiguration,
    val ios: GoogleGatekeeperIosConfiguration = GoogleGatekeeperIosConfiguration,
) : PassageGatekeeperConfiguration

data class GoogleGatekeeperAndroidConfiguration(
    val useGoogleButtonFlow: Boolean = true,
    val filterByAuthorizedAccounts: Boolean = false,
    val autoSelectEnabled: Boolean = true,
    val maxRetries: Int = 3,
)

data object GoogleGatekeeperIosConfiguration

// endregion Google

// region Apple

data class AppleGatekeeperConfiguration(
    val android: AppleGatekeeperAndroidConfiguration = AppleGatekeeperAndroidConfiguration,
    val ios: AppleGatekeeperIosConfiguration = AppleGatekeeperIosConfiguration,
) : PassageGatekeeperConfiguration

data object AppleGatekeeperAndroidConfiguration

data object AppleGatekeeperIosConfiguration

// endregion Apple

// region Email/password

data object EmailPasswordGatekeeperConfiguration : PassageGatekeeperConfiguration

// endregion Email/password
