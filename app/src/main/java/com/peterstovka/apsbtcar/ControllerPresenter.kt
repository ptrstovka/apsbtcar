package com.peterstovka.apsbtcar

import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.util.Log
import com.peterstovka.apsbtcar.ConnectionManager.ConnectionService.Companion.MESSAGE_READ
import com.peterstovka.apsbtcar.ConnectionManager.ConnectionService.Companion.MESSAGE_WRITE_FAILED
import com.peterstovka.apsbtcar.ConnectionManager.ConnectionService.Companion.MESSAGE_WRITE_OK
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_CENTER
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_DOWN
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_LEFT
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_LEFT_DOWN
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_LEFT_UP
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_RIGHT
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_RIGHT_DOWN
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_RIGHT_UP
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_UP

/**
 * @author [Peter Stovka](mailto:stovka.peter@gmail.com)
 */
class ControllerPresenter: ControllerContract.Presenter {

    val TAG = "ControllerPresenter"

    private var view: ControllerContract.View? = null

    private var receivedString = ""

    private val handler = Handler { message ->
        when (message.what) {

            MESSAGE_READ -> {
//                val msg = String((message.obj as ByteArray), 0, message.arg1)
//                receivedString += msg

//                val startIndex = msg.indexOf("#")
//                val endIndex = msg.indexOf('~')
//
////                Log.d("GOT", "Start: $startIndex End: $endIndex")
//
//                if (startIndex >= 0 && endIndex > 0 && startIndex < endIndex) {
//                    val received = receivedString.substring(startIndex, endIndex)
//                    Log.d("READ", received)
//                    receivedString.removeRange(startIndex, endIndex)
//                }

                true
            }

            MESSAGE_WRITE_OK -> {

                true
            }

            MESSAGE_WRITE_FAILED -> {


                true
            }

            else -> false
        }
    }

    private var headlightsOn = false
    private var longDistanceHeadlightsOn = false

    private var warningLightsOn = false

    private val connectionManager =  ConnectionManager(handler, object: ConnectionManager.Callback {
        override fun onConnected() {
            reset()
            view?.showButtonConnected()
            view?.showControls()
        }

        override fun onDisconnected() {
            view?.showButtonDisconnected()
            view?.hideControls()
            reset()
        }
    })

    override fun connect(device: BluetoothDevice) {
        connectionManager.connect(device)
    }

    override fun bind(view: ControllerContract.View) {
        this.view = view

        if (connectionManager.isConnected()) {
            view.showButtonConnected()
            view.showControls()
        } else {
            view.showButtonDisconnected()
            view.hideControls()
        }

        render()
    }

    override fun unbind() {
        this.view = null
    }

    override fun onLeftJoyStick(power: Double, direction: Int) {
        if (direction == DIR_UP || direction == DIR_LEFT_UP || direction == DIR_RIGHT_UP) {
            goForward(power)
        } else if (direction == DIR_DOWN || direction == DIR_LEFT_DOWN || direction == DIR_RIGHT_DOWN) {
            goBackward(power)
        } else if (direction == DIR_CENTER) {
            stop()
        }
    }

    override fun onRightJoyStick(power: Double, direction: Int) {
        if (direction == DIR_LEFT || direction == DIR_LEFT_DOWN || direction == DIR_LEFT_DOWN) {
            turnLeft(power)
        } else if (direction == DIR_RIGHT || direction == DIR_RIGHT_UP || direction == DIR_RIGHT_DOWN) {
            turnRight(power)
        } else if (direction == DIR_CENTER) {
            center()
        }
    }

    private fun goForward(power: Double) {
        if (!connectionManager.isConnected()) {
            return
        }

        connectionManager.write("F")
//        Log.d(TAG, "FORWARD: " + power)
    }

    private fun goBackward(power: Double) {
        if (!connectionManager.isConnected()) {
            return
        }

        connectionManager.write("B")
//        Log.d(TAG, "BACKWARD: " + power)
    }

    private fun stop() {
        if (!connectionManager.isConnected()) {
            return
        }

        connectionManager.write("S")
//        Log.d(TAG, "STOP ACCELERATE")
    }

    private fun turnRight(power: Double) {
        if (!connectionManager.isConnected()) {
            return
        }

        Log.d(TAG, "RIGHT: " + power)
    }

    private fun turnLeft(power: Double) {
        if (!connectionManager.isConnected()) {
            return
        }

        Log.d(TAG, "LEFT: " + power)
    }

    private fun center() {
        if (!connectionManager.isConnected()) {
            return
        }

        Log.d(TAG, "CENTER")
    }

    override fun toggleHeadlights() {
        if (!connectionManager.isConnected()) {
            return
        }

        headlightsOn = !headlightsOn
        render()

        // TODO: here send headlights
        Log.d(TAG, "Headlights " + headlightsOn)
    }

    override fun toggleLongDistanceHeadlights() {
        if (!connectionManager.isConnected()) {
            return
        }

        longDistanceHeadlightsOn = !longDistanceHeadlightsOn
        render()

        // TODO: Here sent long distance cmd
        Log.d(TAG, "Long distance " + longDistanceHeadlightsOn)
    }

    private fun render() {
        if (headlightsOn) {
            view?.showHeadlightsOn()
        } else {
            view?.showHeadlightsOff()
        }

        if (longDistanceHeadlightsOn) {
            view?.showLongDistanceHeadlightsOn()
        } else {
            view?.showLongDistanceHeadlightsOff()
        }

        if (warningLightsOn) {
            view?.showWarningLightsOn()
        } else {
            view?.showWarningLightsOff()
        }
    }

    private fun reset() {
        longDistanceHeadlightsOn = false
        headlightsOn = false
        warningLightsOn = false
        render()
    }

    override fun toggleWarningLights() {
        if (!connectionManager.isConnected()) {
            return
        }

        warningLightsOn = !warningLightsOn
        render()

        // TODO: here send warning lights

        Log.d(TAG, "warning " + warningLightsOn)
    }

    override fun horn() {
        if (!connectionManager.isConnected()) {
            return
        }

        // TODO: Here send horn command.
        Log.d(TAG, "HORN")
    }

    override fun onConnectionToggle() {
        if (!connectionManager.isConnected()) {
            view?.checkForBluetooth()
        } else {
            connectionManager.close()
        }
    }
}