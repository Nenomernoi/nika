package com.example.nika

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import com.honeywell.aidc.*
import java.nio.charset.Charset

class BarcodeManager(private var listener: ActionListener) : BarcodeReader.BarcodeListener,
    BarcodeReader.TriggerListener {

    companion object {
        private val TAG = "BarcodeManager"
    }

    private var barcodeReader: BarcodeReader? = null
    private var manager: AidcManager? = null

    fun initScanner(context: Context) {
        AidcManager.create(context) { aidcManager ->
            manager = aidcManager
            barcodeReader = manager?.createBarcodeReader()
            setupBarcodeReader()
        }
    }

    private fun setupBarcodeReader() {
        barcodeReader?.let {
            it.addBarcodeListener(this)
            try {
                it.setProperty(
                    BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                    BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL
                )
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
                listener.onBarcodeManagerError(R.string.scanner_failed_apply_properties)
            }
            it.addTriggerListener(this)
            val properties = hashMapOf<String, Any>()

            properties[BarcodeReader.PROPERTY_CODE_128_ENABLED] = true
            properties[BarcodeReader.PROPERTY_GS1_128_ENABLED] = true
            properties[BarcodeReader.PROPERTY_QR_CODE_ENABLED] = true
            properties[BarcodeReader.PROPERTY_CODE_39_ENABLED] = true
            properties[BarcodeReader.PROPERTY_DATAMATRIX_ENABLED] = true
            properties[BarcodeReader.PROPERTY_UPC_A_ENABLE] = true
            properties[BarcodeReader.PROPERTY_EAN_13_ENABLED] = true
            properties[BarcodeReader.PROPERTY_AZTEC_ENABLED] = false
            properties[BarcodeReader.PROPERTY_CODABAR_ENABLED] = false
            properties[BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED] = true
            properties[BarcodeReader.PROPERTY_PDF_417_ENABLED] = true
            // Set Max Code 39 barcode length
            properties[BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH] = 10
            // Turn on center decoding
            properties[BarcodeReader.PROPERTY_CENTER_DECODE] = true
            // Enable bad read response
            properties[BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED] = true

            it.setProperties(properties)
        }
        onScreenResume()
    }

    override fun onFailureEvent(p0: BarcodeFailureEvent?) {
        // TODO Auto-generated method stub
    }

    override fun onBarcodeEvent(p0: BarcodeReadEvent?) {
        if (p0 == null) {
            return
        }
        listener.onBarcodeScan(
            BarcodeInfo(
                barcodeData = p0.barcodeData,
                charset = p0.charset ?: Charset.defaultCharset(),
                codeId = p0.codeId,
                aimId = p0.aimId,
                timestamp = p0.timestamp
            )
        )
    }

    override fun onTriggerEvent(p0: TriggerStateChangeEvent?) {
        // TODO Auto-generated method stub
    }

    fun onScreenResume() {
        barcodeReader?.let {
            try {
                it.claim()
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
                listener.onBarcodeManagerError(R.string.scanner_unavailable)
            }
        }
    }

    fun onScreenPause() {
        barcodeReader?.release()
    }

    fun onScreenDestroy() {
        barcodeReader?.let {
            it.removeBarcodeListener(this)
            it.removeTriggerListener(this)
            it.close()
        }
        barcodeReader = null
        manager?.close()
    }

}

interface ActionListener {
    fun onBarcodeManagerError(@StringRes errorResId: Int)
    fun onBarcodeScan(barcode: BarcodeInfo)
}

data class BarcodeInfo(
    val barcodeData: String?,
    val charset: Charset,
    val codeId: String?,
    val aimId: String?,
    val timestamp: String?
)