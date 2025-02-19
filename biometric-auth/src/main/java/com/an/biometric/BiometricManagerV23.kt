package com.an.biometric

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.appcompat.app.AppCompatActivity

import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import java.util.UUID

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal


@TargetApi(Build.VERSION_CODES.M)
open class BiometricManagerV23 {

    private var cipher: Cipher? = null
    private var keyStore: KeyStore? = null
    private var keyGenerator: KeyGenerator? = null
    private var cryptoObject: FingerprintManagerCompat.CryptoObject? = null

    protected var context: Context? = null
    protected var activity: AppCompatActivity? = null

    protected var title: String? = null
    protected var subtitle: String? = null
    protected var negativeButtonText: String? = null
    protected var positiveButtonText: String? = null
    protected var passwordViewTitle: String? = null
    protected var passwordViewDescription: String? = null
    private var biometricDialogV23: BiometricDialogV23? = null
    protected var mCancellationSignalV23 = CancellationSignal()


    fun displayBiometricPromptV23(biometricCallback: BiometricCallback) :  BiometricDialogV23?{
        generateKey()

        if (initCipher()) {

            cryptoObject = FingerprintManagerCompat.CryptoObject(cipher!!)
            val fingerprintManagerCompat = FingerprintManagerCompat.from(context!!)

            fingerprintManagerCompat.authenticate(cryptoObject, 0, mCancellationSignalV23,
                    object : FingerprintManagerCompat.AuthenticationCallback() {
                        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
                            super.onAuthenticationError(errMsgId, errString)
                            updateStatus(context!!.getString(R.string.biometric_failed))
                            biometricCallback.onAuthenticationError(errMsgId, errString ?: "")
                        }

                        override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
                            super.onAuthenticationHelp(helpMsgId, helpString)
                            updateStatus(context!!.getString(R.string.biometric_failed), true)
                            biometricCallback.onAuthenticationHelp(helpMsgId, helpString ?: "")
                            showPasswordButton()
                        }

                        override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            dismissDialog()
                            biometricCallback.onAuthenticationSuccessful()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            updateStatus(context!!.getString(R.string.biometric_failed), true)
                            biometricCallback.onAuthenticationFailed()
                            showPasswordButton()
                        }
                    },
                    null)

            return displayBiometricDialog(biometricCallback)
        }

        return null
    }


    private fun displayBiometricDialog(biometricCallback: BiometricCallback) : BiometricDialogV23 {
        biometricDialogV23 = BiometricDialogV23(context!!, biometricCallback, passwordViewTitle ?: "", passwordViewDescription ?: "")
        biometricDialogV23!!.setTitleText(title ?: "")
        biometricDialogV23!!.setSubtitle(subtitle ?: "")
        biometricDialogV23!!.setNegativeButtonText(negativeButtonText ?: "")
        biometricDialogV23!!.setPositiveButtonText(positiveButtonText ?: "")
        biometricDialogV23!!.setActivityContext(activity!!)
        biometricDialogV23!!.show()
        return biometricDialogV23!!
    }


    private fun dismissDialog() {
        if (biometricDialogV23 != null) {
            biometricDialogV23!!.dismiss()
        }
    }

    private fun updateStatus(status: String, animation: Boolean = false) {
        if (biometricDialogV23 != null) {
            biometricDialogV23!!.updateStatus(status, animation)
        }
    }

    private fun showPasswordButton() {
        if (biometricDialogV23 != null) {
            biometricDialogV23!!.showPasswordButton()
        }
    }

    private fun hidePasswordButton() {
        if (biometricDialogV23 != null) {
            biometricDialogV23!!.hidePasswordButton()
        }
    }

    private fun generateKey() {
        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore!!.load(null)

            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator!!.init(KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build())

            keyGenerator!!.generateKey()

        } catch (exc: KeyStoreException) {
            exc.printStackTrace()
        } catch (exc: NoSuchAlgorithmException) {
            exc.printStackTrace()
        } catch (exc: NoSuchProviderException) {
            exc.printStackTrace()
        } catch (exc: InvalidAlgorithmParameterException) {
            exc.printStackTrace()
        } catch (exc: CertificateException) {
            exc.printStackTrace()
        } catch (exc: IOException) {
            exc.printStackTrace()
        }

    }


    private fun initCipher(): Boolean {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7)

        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        try {
            keyStore!!.load(null)
            val key = keyStore!!.getKey(KEY_NAME, null) as SecretKey
            cipher!!.init(Cipher.ENCRYPT_MODE, key)
            return true


        } catch (e: KeyPermanentlyInvalidatedException) {
            return false

        } catch (e: KeyStoreException) {

            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }

    }

    companion object {

        private val KEY_NAME = UUID.randomUUID().toString()
    }
}
