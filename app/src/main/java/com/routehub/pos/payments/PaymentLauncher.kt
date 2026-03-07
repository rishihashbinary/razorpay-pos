package com.routehub.pos.payments

import android.app.Activity
import android.content.Intent

object PaymentLauncher {

    const val REQUEST_CODE_PAYMENT = 1001

    fun startPayment(activity: Activity, amount: Int) {

        val intent = Intent()

        intent.action = "com.razorpay.pos.PAYMENT"

        intent.putExtra("amount", amount)

        intent.putExtra("currency", "INR")

        intent.putExtra("description", "Property Fee")

        activity.startActivityForResult(intent, REQUEST_CODE_PAYMENT)

    }

}