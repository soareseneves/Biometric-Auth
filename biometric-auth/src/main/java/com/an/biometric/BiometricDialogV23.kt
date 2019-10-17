package com.an.biometric

import android.app.Activity.RESULT_OK
import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.florent37.inlineactivityresult.kotlin.startForResult

import com.google.android.material.bottomsheet.BottomSheetDialog

class BiometricDialogV23 : BottomSheetDialog {

    private var btnCancel: Button? = null
    private var btnUsePassword: Button? = null
    private var imgLogo: ImageView? = null
    private var itemTitle: TextView? = null
    private var itemDescription: TextView? = null
    private var itemSubtitle: TextView? = null
    private var itemStatus: TextView? = null
    private var activityContext: AppCompatActivity? = null

    private lateinit var biometricCallback: BiometricCallback

    constructor(context: Context) : super(context, R.style.BottomSheetDialogTheme) {
        //this.context = context.applicationContext
        setDialogView()
    }

    constructor(context: Context, biometricCallback: BiometricCallback) : super(context, R.style.BottomSheetDialogTheme) {
        //this.context = context.applicationContext
        this.biometricCallback = biometricCallback
        setDialogView()
    }

    constructor(context: Context, theme: Int) : super(context, theme) {}

    protected constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener) : super(context, cancelable, cancelListener) {}

    private fun setDialogView() {
        val bottomSheetView = layoutInflater.inflate(R.layout.view_bottom_sheet, null)
        setContentView(bottomSheetView)

        btnCancel = findViewById(R.id.btn_cancel)
        btnCancel!!.setOnClickListener {
            dismiss()
            biometricCallback.onAuthenticationCancelled()
        }

        btnUsePassword = findViewById(R.id.btn_usepassword)
        btnUsePassword!!.setOnClickListener {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val credentialsIntent = keyguardManager.createConfirmDeviceCredentialIntent(
                    "Password required",
                    "please enter your pattern to receive your token"
            )
            if (credentialsIntent != null) {
                activityContext?.startForResult(credentialsIntent){
                    if(it.resultCode == RESULT_OK) {
                        dismiss()
                        biometricCallback.onAuthenticationSuccessful()
                    } else {
                        biometricCallback.onAuthenticationError(100, "Invalid Password")
                    }
                }
            } else {
                biometricCallback.onAuthenticationHelp(100, "Senha não necessária")
            }
        }

        imgLogo = findViewById(R.id.img_logo)
        itemTitle = findViewById(R.id.item_title)
        itemStatus = findViewById(R.id.item_status)
        itemSubtitle = findViewById(R.id.item_subtitle)
        itemDescription = findViewById(R.id.item_description)

        updateLogo()
    }

    fun setTitle(title: String) {
        itemTitle!!.text = title
    }

    fun updateStatus(status: String) {
        itemStatus!!.text = status
    }

    fun setSubtitle(subtitle: String) {
        itemSubtitle!!.text = subtitle
    }

    fun setDescription(description: String) {
        itemDescription!!.text = description
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

    private fun updateLogo() {
        try {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            imgLogo!!.setImageDrawable(drawable)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}
