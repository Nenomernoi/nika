package com.example.nika

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.honeywell.aidc.*
import com.honeywell.aidc.BarcodeReader.BarcodeListener
import com.honeywell.aidc.BarcodeReader.TriggerListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), BarcodeListener, TriggerListener {

    private var barcodeReader: BarcodeReader? = null
    private var manager: AidcManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AidcManager.create(this) { aidcManager ->
            manager = aidcManager
            try {
                barcodeReader = manager?.createBarcodeReader()
                barcodeReader?.addBarcodeListener(this)
                barcodeReader?.setProperty(
                    BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                    BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL
                )
                barcodeReader?.addTriggerListener(this)

                val properties: MutableMap<String, Any> = HashMap()

                properties[BarcodeReader.PROPERTY_CODE_128_ENABLED] = true
                properties[BarcodeReader.PROPERTY_GS1_128_ENABLED] = true
                properties[BarcodeReader.PROPERTY_QR_CODE_ENABLED] = true
                properties[BarcodeReader.PROPERTY_CODE_39_ENABLED] = true
                properties[BarcodeReader.PROPERTY_DATAMATRIX_ENABLED] = true
                properties[BarcodeReader.PROPERTY_UPC_A_ENABLE] = true
                properties[BarcodeReader.PROPERTY_EAN_13_ENABLED] = false
                properties[BarcodeReader.PROPERTY_AZTEC_ENABLED] = false
                properties[BarcodeReader.PROPERTY_CODABAR_ENABLED] = false
                properties[BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED] = false
                properties[BarcodeReader.PROPERTY_PDF_417_ENABLED] = false
                properties[BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH] = 10
                properties[BarcodeReader.PROPERTY_CENTER_DECODE] = true
                properties[BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED] = true
                barcodeReader?.setProperties(properties)

                barcodeReader?.claim()

            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Invalid Scanner Name Exception: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Exception: " + e.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        barcodeReader?.removeBarcodeListener(this)
        barcodeReader?.removeTriggerListener(this)

        barcodeReader?.close()
        barcodeReader = null
        manager?.close()
    }

    override fun onResume() {
        super.onResume()
        barcodeReader?.claim()
    }

    override fun onPause() {
        super.onPause()
        barcodeReader?.release()
    }

    //////////////////////////////////////////////////////////////////

    override fun onFailureEvent(event: BarcodeFailureEvent?) {
        //
    }

    override fun onBarcodeEvent(event: BarcodeReadEvent?) {
        runOnUiThread {
            val list: MutableList<String> = ArrayList()
            list.add("Barcode data: " + event?.barcodeData)
            list.add("Character Set: " + event?.charset)
            list.add("Code ID: " + event?.codeId)
            list.add("AIM ID: " + event?.aimId)
            list.add("Timestamp: " + event?.timestamp)

            txtTitle?.text = list.joinToString(separator = ";    ")
        }
    }

    override fun onTriggerEvent(event: TriggerStateChangeEvent?) {
        //
    }

}
