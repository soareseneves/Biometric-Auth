package com.an.biometric

import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.appcompat.app.AppCompatActivity

class BiometricManager protected constructor(biometricBuilder: BiometricBuilder, activity: AppCompatActivity) : BiometricManagerV23() {


    protected var mCancellationSignal = CancellationSignal()

    init {
        this.context = biometricBuilder.context
        this.activity = activity
        this.title = biometricBuilder.title
        this.subtitle = biometricBuilder.subtitle
        this.negativeButtonText = biometricBuilder.negativeButtonText
        this.positiveButtonText = biometricBuilder.positiveButtonText
        this.passwordViewTitle = biometricBuilder.passwordViewTitle
        this.passwordViewDescription = biometricBuilder.passwordViewDescription
    }


    fun authenticate(biometricCallback: BiometricCallback) {

        if (title == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog title cannot be null")
            return
        }


        if (subtitle == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog subtitle cannot be null")
            return
        }

        if (negativeButtonText == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog negative button text cannot be null")
            return
        }

        if (!BiometricUtils.isSdkVersionSupported) {
            biometricCallback.onSdkVersionNotSupported()
            return
        }

        if (!BiometricUtils.isPermissionGranted(context!!)) {
            biometricCallback.onBiometricAuthenticationPermissionNotGranted()
            return
        }

        if (!BiometricUtils.isHardwareSupported(context!!)) {
            biometricCallback.onBiometricAuthenticationNotSupported()
            return
        }

        if (!BiometricUtils.isFingerprintAvailable(context!!)) {
            biometricCallback.onBiometricAuthenticationNotAvailable()
            return
        }

        displayBiometricDialog(biometricCallback)
    }

    fun cancelAuthentication() {
        if (BiometricUtils.isBiometricPromptEnabled) {
            if (!mCancellationSignal.isCanceled)
                mCancellationSignal.cancel()
        } else {
            if (!mCancellationSignalV23.isCanceled)
                mCancellationSignalV23.cancel()
        }
    }


    private fun displayBiometricDialog(biometricCallback: BiometricCallback) {
        displayBiometricPromptV23(biometricCallback)
    }

    class BiometricBuilder(val context: Context, val activity: AppCompatActivity) {

        var title: String? = null
        var subtitle: String? = null
        var negativeButtonText: String? = null
        var positiveButtonText: String? = null
        var passwordViewTitle: String? = null
        var passwordViewDescription: String? = null

        fun setTitle(title: String): BiometricBuilder {
            this.title = title
            return this
        }

        fun setSubtitle(subtitle: String): BiometricBuilder {
            this.subtitle = subtitle
            return this
        }

        fun setNegativeButtonText(negativeButtonText: String): BiometricBuilder {
            this.negativeButtonText = negativeButtonText
            return this
        }

        fun setPositiveButtonText(positiveButtonText: String): BiometricBuilder {
            this.positiveButtonText = positiveButtonText
            return this
        }

        fun setPasswordViewTitle(passwordViewTitle: String): BiometricBuilder {
            this.passwordViewTitle = passwordViewTitle
            return this
        }

        fun setPasswordViewDescription(passwordViewDescription: String): BiometricBuilder {
            this.passwordViewDescription = passwordViewDescription
            return this
        }

        fun build(): BiometricManager {
            return BiometricManager(this, activity)
        }
    }
}
