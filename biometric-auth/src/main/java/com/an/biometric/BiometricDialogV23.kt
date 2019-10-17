package com.an.biometric

import android.app.Activity.RESULT_OK
import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.florent37.inlineactivityresult.kotlin.startForResult

import com.google.android.material.bottomsheet.BottomSheetDialog

class BiometricDialogV23 : BottomSheetDialog {

    private var btnCancel: Button? = null
    private var btnUsePassword: Button? = null
    private var itemTitle: TextView? = null
    private var itemSubtitle: TextView? = null
    private var activityContext: AppCompatActivity? = null

    private lateinit var biometricCallback: BiometricCallback

    constructor(context: Context, passwordViewTitle : String, passwordViewDescription : String) : super(context, R.style.BottomSheetDialogTheme) {
        //this.context = context.applicationContext
        setDialogView(passwordViewTitle, passwordViewDescription)
    }

    constructor(context: Context, biometricCallback: BiometricCallback, passwordViewTitle : String, passwordViewDescription : String) : super(context, R.style.BottomSheetDialogTheme) {
        //this.context = context.applicationContext
        this.biometricCallback = biometricCallback
        setDialogView(passwordViewTitle, passwordViewDescription)
    }

    constructor(context: Context, theme: Int) : super(context, theme) {}

    protected constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener) : super(context, cancelable, cancelListener) {}

    private fun setDialogView(passwordViewTitle: String, passwordViewDescription: String) {
        val bottomSheetView = layoutInflater.inflate(R.layout.view_bottom_sheet, null)
        setContentView(bottomSheetView)

        btnCancel = findViewById(R.id.btn_cancel)
        btnCancel!!.setOnClickListener {
            dismiss()
            biometricCallback.onAuthenticationCancelled()
        }

        btnUsePassword = findViewById(R.id.btn_usepassword)
        btnUsePassword!!.visibility = View.GONE
        btnUsePassword!!.setOnClickListener {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val credentialsIntent = keyguardManager.createConfirmDeviceCredentialIntent(
                    passwordViewTitle,
                    passwordViewDescription
            )
            if (credentialsIntent != null) {
                activityContext?.startForResult(credentialsIntent){
                    if(it.resultCode == RESULT_OK) {
                        dismiss()
                        biometricCallback.onAuthenticationSuccessful()
                    } else {
                        dismiss()
                        biometricCallback.onAuthenticationError(100, "")
                    }
                }
            } else {
                dismiss()
                biometricCallback.onAuthenticationHelp(100, "")
            }
        }

        itemTitle = findViewById(R.id.item_title)
        itemSubtitle = findViewById(R.id.item_subtitle)
    }

    fun setTitleText(title: String) {
        itemTitle!!.text = title
    }

    fun updateStatus(status: String) {
        itemTitle!!.text = status
    }

    fun showPasswordButton() {
        btnUsePassword!!.visibility = View.VISIBLE
    }

    fun setSubtitle(subtitle: String) {
        itemSubtitle!!.text = subtitle
    }

    fun setNegativeButtonText(negativeButtonText: String) {
        btnCancel!!.text = negativeButtonText
    }

    fun setPositiveButtonText(positiveButtonText: String) {
        btnUsePassword!!.text = positiveButtonText
    }

    fun setActivityContext(activityContext: AppCompatActivity) {
        this.activityContext = activityContext
    }

}
