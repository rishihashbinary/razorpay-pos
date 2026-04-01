package com.routehub.pos.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.eze.api.EzeAPI
import com.routehub.pos.PrintCallback
import com.routehub.pos.R
import com.routehub.pos.analytics.MixpanelManager
import com.routehub.pos.models.ReceiptData
import org.json.JSONObject
import java.io.ByteArrayOutputStream

object ReceiptPrintHelper {

    private const val REQUEST_CODE_PRINT_BITMAP = 10029

    fun printReceipt(context: Context, receiptData: ReceiptData, callback: PrintCallback) {
        try {
            val bitmap = createBitmapFromLayout(context, receiptData)
            val encodedImage = getEncoded64ImageStringFromBitmap(bitmap)

            val jsonRequest = JSONObject()
            val jsonImageObj = JSONObject()

            jsonImageObj.put("imageData", encodedImage)
            jsonImageObj.put("imageType", "JPEG")

            jsonRequest.put("image", jsonImageObj)

            EzeAPI.printBitmap(context, REQUEST_CODE_PRINT_BITMAP, jsonRequest)
            Thread.sleep(2000)
            callback.onSuccess();

        } catch (e: Exception) {
            MixpanelManager.track("PrinterError", e)
            e.printStackTrace()
            callback.onError(e.message);
        }
    }

    private fun createBitmapFromLayout(
        context: Context,
        data: ReceiptData
    ): Bitmap {

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.layout_print_receipt, null)

        view.findViewById<TextView>(R.id.txtMerchant).text = data.merchantName
        view.findViewById<TextView>(R.id.TxnId).text = data.txnId
        view.findViewById<TextView>(R.id.PaymentMode).text = data.paymentMode
        view.findViewById<TextView>(R.id.txtRefrenceNo).text = data.reference1
        view.findViewById<TextView>(R.id.StatusId).text = data.status
        view.findViewById<TextView>(R.id.txtAmount).text = "₹"+data.amount.toString()
        view.findViewById<TextView>(R.id.Customer).text = data.customerName
        view.findViewById<TextView>(R.id.CustPhone).text = data.customerPhone
        view.findViewById<TextView>(R.id.ReceiptDate).text = data.receiptDate

        view.measure(
            View.MeasureSpec.makeMeasureSpec(384, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.RGB_565
        )

        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE) // important
        view.draw(canvas)

        return bitmap
    }

    private fun getEncoded64ImageStringFromBitmap(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
}