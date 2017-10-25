package com.peterstovka.apsbtcar

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.erz.joysticklibrary.JoyStick
import com.erz.joysticklibrary.JoyStick.*
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_CENTER
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_DOWN
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_LEFT
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_LEFT_DOWN
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_LEFT_UP
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_RIGHT
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_RIGHT_DOWN
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_RIGHT_UP
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_UP
import kotlinx.android.synthetic.main.activity_controller.*


class ControllerActivity : AppCompatActivity(), ControllerContract.View {

    val TAG = "Controller"

    val REQUEST_ENABLE_BT = 142
    val REQUEST_DISCOVERY = 174

    // TODO: Should be injected.
    private val presenter: ControllerContract.Presenter = ControllerPresenter()

    override fun onStart() {
        super.onStart()
        presenter.bind(this)
    }

    override fun onStop() {
        presenter.unbind()
        super.onStop()
    }

    lateinit var bt: Bluetooth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        leftJoyStick.setListener(object: JoyStickListener {

            override fun onTap() {}

            override fun onDoubleTap() {
                presenter.toggleLeftBlink()
            }

            override fun onMove(joyStick: JoyStick?, angle: Double, power: Double, direction: Int) {
                presenter.onLeftJoyStick(power, getDirection(direction))
            }
        })

        rightJoyStick.setListener(object: JoyStickListener {

            override fun onTap() {}

            override fun onDoubleTap() {
                presenter.toggleRightBlink()
            }

            override fun onMove(joyStick: JoyStick?, angle: Double, power: Double, direction: Int) {
                presenter.onRightJoyStick(power, getDirection(direction))
            }
        })

        connectButton.setOnClickListener {
            presenter.onConnectionToggle()
        }

        headlightsButton.setOnClickListener {
            presenter.toggleHeadlights()
        }

        longDistanceHeadlightsButton.setOnClickListener {
            presenter.toggleLongDistanceHeadlights()
        }

        blinkButton.setOnClickListener {
            presenter.toggleWarningLights()
        }

        leftBlinkButton.setOnClickListener {
            presenter.toggleLeftBlink()
        }

        rightBlinkButton.setOnClickListener {
            presenter.toggleRightBlink()
        }

        hornButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                presenter.hornOn()
                return@setOnTouchListener true
            } else if (event.action == MotionEvent.ACTION_UP) {
                presenter.hornOff()
                return@setOnTouchListener true
            }

            false
        }

        bt = Bluetooth(this, mHandler)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getDirection(direction: Int): Int {
        return when (direction) {
            DIRECTION_CENTER -> DIR_CENTER
            DIRECTION_LEFT -> DIR_LEFT
            DIRECTION_RIGHT -> DIR_RIGHT
            DIRECTION_UP -> DIR_UP
            DIRECTION_DOWN -> DIR_DOWN
            DIRECTION_LEFT_UP -> DIR_LEFT_UP
            DIRECTION_UP_RIGHT -> DIR_RIGHT_UP
            DIRECTION_DOWN_LEFT -> DIR_LEFT_DOWN
            DIRECTION_RIGHT_DOWN -> DIR_RIGHT_DOWN
            else -> -1
        }
    }

    override fun showButtonConnected() {
        connectButton.text = "Connected"
        setGreen(connectButton)
    }

    override fun showButtonDisconnected() {
        connectButton.text = "Disconnected"
        setRed(connectButton)
    }

    override fun checkForBluetooth() {
        val adapter = BluetoothAdapter.getDefaultAdapter()

        if (adapter == null) {
            Toast.makeText(this, "This device does not have bluetooth support.", Toast.LENGTH_LONG).show()
            return
        }

        if (!adapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            searchForAvailableDevices()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            searchForAvailableDevices()
        }

        if (requestCode == REQUEST_DISCOVERY && resultCode == Activity.RESULT_OK && data != null) {

            // This is the device I should connect to.
            val device = data.getParcelableExtra<BluetoothDevice>("device")

            if (device == null) {
                log("Device is null.")
                return
            }

            log("Device name: " +  device.name)
            log("Device MAC: " + device.address)

            device.uuids.forEach {
                log("UUID: " + it.uuid.toString())
            }

//            presenter.connect(device)
            connect(device)
        }

    }

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Bluetooth.MESSAGE_STATE_CHANGE -> {
                    Log.d("READ", "MESSAGE_STATE_CHANGE: " + msg.arg1)

                    if (msg.arg1 == Bluetooth.STATE_CONNECTED) {
                        presenter.setConnected()
                    } else {
                        presenter.setDisconnected()
                    }

                }
//                Bluetooth.MESSAGE_WRITE -> Log.d("READ", "MESSAGE_WRITE ")
                Bluetooth.MESSAGE_READ -> Log.d("READ", "MESSAGE_READ ")
                Bluetooth.MESSAGE_DEVICE_NAME -> Log.d("READ", "MESSAGE_DEVICE_NAME " + msg)
                Bluetooth.MESSAGE_TOAST -> Log.d("READ", "MESSAGE_TOAST " + msg)
            }
        }
    }

    override fun sendCommand(command: String) {
        if (command != "~") {
            Log.d(TAG, "CMD: '$command'")
        }

        bt.sendMessage(command)
    }

    override fun disconnect() {
        bt.stop()
    }

    private fun connect(device: BluetoothDevice) {
        bt.connectDevice(device)
    }

    private fun searchForAvailableDevices() {
        val intent = Intent(this, DiscoveryActivity::class.java)
        startActivityForResult(intent, REQUEST_DISCOVERY)
    }

    private fun log(message: String) {
        Log.d(TAG, message)
    }

    private fun setGreen(button: AppCompatButton) {
        runOnUiThread {
            button.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.button_green))
        }
    }

    private fun setRed(button: AppCompatButton) {
        runOnUiThread {
            button.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.button_red))
        }
    }

    override fun showControls() {
        runOnUiThread {
            controlContainer.visibility = View.VISIBLE
            joyStickContainer.visibility = View.VISIBLE
        }
    }

    override fun hideControls() {
        runOnUiThread {
            controlContainer.visibility = View.GONE
            joyStickContainer.visibility = View.GONE
        }
    }

    override fun showHeadlightsOn() {
        setGreen(headlightsButton)
    }

    override fun showHeadlightsOff() {
        setRed(headlightsButton)
    }

    override fun showLongDistanceHeadlightsOn() {
        setGreen(longDistanceHeadlightsButton)
    }

    override fun showLongDistanceHeadlightsOff() {
        setRed(longDistanceHeadlightsButton)
    }

    override fun showWarningLightsOn() {
        setGreen(blinkButton)
    }

    override fun showWarningLightsOff() {
        setRed(blinkButton)
    }

    override fun setLeftBlinkOn() {
        setGreen(leftBlinkButton)
    }

    override fun setLeftBlinkOff() {
        setRed(leftBlinkButton)
    }

    override fun setRightBlinkOn() {
        setGreen(rightBlinkButton)
    }

    override fun setRightBlinkOff() {
        setRed(rightBlinkButton)
    }
}
