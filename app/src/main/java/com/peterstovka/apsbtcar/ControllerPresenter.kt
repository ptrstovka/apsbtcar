package com.peterstovka.apsbtcar

import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_CENTER
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_DOWN
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_LEFT
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_LEFT_DOWN
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_LEFT_UP
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_RIGHT
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_RIGHT_DOWN
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_RIGHT_UP
import com.peterstovka.apsbtcar.ControllerContract.Presenter.Companion.DIR_UP
import java.util.*

/**
 * @author [Peter Stovka](mailto:stovka.peter@gmail.com)
 */
class ControllerPresenter: ControllerContract.Presenter {

    private var view: ControllerContract.View? = null

    private var headlightsOn = false
    private var longDistanceHeadlightsOn = false
    private var warningLightsOn = false

    override fun bind(view: ControllerContract.View) {
        this.view = view

        if (isConnected) {
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
        if (!isConnected) {
            return
        }

        view?.sendCommand("fwd_${power.toInt()}")
    }

    private fun goBackward(power: Double) {
        if (!isConnected) {
            return
        }

        view?.sendCommand("bwd_${power.toInt()}")
    }

    private fun stop() {
        if (!isConnected) {
            return
        }

        view?.sendCommand("stop")
    }

    private fun turnRight(power: Double) {
        if (!isConnected) {
            return
        }

        if (power > 50 && !rightBlinkEnabled) {
            toggleRightBlink()
        }

        view?.sendCommand("rht_${power.toInt()}")
    }

    private fun turnLeft(power: Double) {
        if (!isConnected) {
            return
        }

        if (power > 50 && !leftBlinkEnabled) {
            toggleLeftBlink()
        }

        view?.sendCommand("lft_${power.toInt()}")
    }

    private fun center() {
        if (!isConnected) {
            return
        }

        if (leftBlinkEnabled) {
            toggleLeftBlink()
        }

        if (rightBlinkEnabled) {
            toggleRightBlink()
        }

        view?.sendCommand("ctr")
    }

    override fun toggleHeadlights() {
        if (!isConnected) {
            return
        }

        headlightsOn = !headlightsOn
        render()

        if (headlightsOn) {
            view?.sendCommand("h1")
        } else {
            view?.sendCommand("h0")
        }
    }

    override fun toggleLongDistanceHeadlights() {
        if (!isConnected) {
            return
        }

        longDistanceHeadlightsOn = !longDistanceHeadlightsOn
        render()

        if (longDistanceHeadlightsOn) {
            view?.sendCommand("ldh1")
        } else {
            view?.sendCommand("ldh0")
        }
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

        if (leftBlinkEnabled) {
            view?.setLeftBlinkOn()
        } else {
            view?.setLeftBlinkOff()
        }

        if (rightBlinkEnabled) {
            view?.setRightBlinkOn()
        } else {
            view?.setRightBlinkOff()
        }
    }

    private fun reset() {
        longDistanceHeadlightsOn = false
        headlightsOn = false
        warningLightsOn = false
        leftBlinkEnabled = false
        rightBlinkEnabled = false
        render()
    }

    override fun toggleWarningLights() {
        if (!isConnected) {
            return
        }

        if (leftBlinkEnabled || rightBlinkEnabled) {
            return
        }

        warningLightsOn = !warningLightsOn
        render()

        if (warningLightsOn) {
            view?.sendCommand("warn1")
        } else {
            view?.sendCommand("warn0")
        }
    }

    override fun hornOn() {
        if (!isConnected) {
            return
        }

        view?.sendCommand("horn1")
    }

    override fun hornOff() {
        if (!isConnected) {
            return
        }

        view?.sendCommand("horn0")
    }

    private var leftBlinkEnabled = false
    private var rightBlinkEnabled = false

    override fun toggleLeftBlink() {
        if (!isConnected) {
            return
        }

        if (warningLightsOn) {
            return
        }

        leftBlinkEnabled = !leftBlinkEnabled
        rightBlinkEnabled = false
        render()

        if (leftBlinkEnabled) {
            view?.sendCommand("lbl1")
        } else {
            view?.sendCommand("lbl0")
        }
    }

    override fun toggleRightBlink() {
        if (!isConnected) {
           return
        }

        if (warningLightsOn) {
            return
        }

        rightBlinkEnabled = !rightBlinkEnabled
        leftBlinkEnabled = false
        render()

        if (rightBlinkEnabled) {
            view?.sendCommand("rbl1")
        } else {
            view?.sendCommand("rbl0")
        }
    }

    override fun onConnectionToggle() {
        if (!isConnected) {
            view?.checkForBluetooth()
        } else {
            view?.disconnect()
        }
    }

    private var isConnected = false

    private var keepAlive: KeepAlive? = null

    override fun setConnected() {
        isConnected = true
        reset()
        view?.showButtonConnected()
        view?.showControls()
        keepAlive = KeepAlive()
        keepAlive?.startTimer()
    }

    override fun setDisconnected() {
        isConnected = false
        view?.showButtonDisconnected()
        view?.hideControls()
        reset()
        keepAlive?.stopTimer()
        keepAlive = null
    }

    inner class KeepAlive: TimerTask() {

        private var timer: Timer? = null

        fun startTimer() {
            stopTimer()

            timer = Timer("Heart")
            timer?.scheduleAtFixedRate(this, 50, 50)
        }

        fun stopTimer() {
            if (timer != null) {
                timer?.cancel()
                timer = null
            }
        }

        override fun run() {
            if (isConnected) {
                view?.sendCommand("~")
            }
        }
    }

}