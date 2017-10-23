package com.peterstovka.apsbtcar

/**
 * @author [Peter Stovka](mailto:stovka.peter@gmail.com)
 */
interface ControllerContract {

    interface View {

        fun showButtonConnected()

        fun showButtonDisconnected()

        fun checkForBluetooth()

        fun showControls()

        fun hideControls()

        fun showHeadlightsOn()

        fun showHeadlightsOff()

        fun showLongDistanceHeadlightsOn()

        fun showLongDistanceHeadlightsOff()

        fun showWarningLightsOn()

        fun showWarningLightsOff()

        fun sendCommand(command: String)

        fun disconnect()

        fun setLeftBlinkOn()

        fun setLeftBlinkOff()

        fun setRightBlinkOn()

        fun setRightBlinkOff()

    }

    interface Presenter {

        companion object {

            const val DIR_LEFT = 1
            const val DIR_RIGHT = 2
            const val DIR_UP = 3
            const val DIR_DOWN = 4
            const val DIR_LEFT_UP = 5
            const val DIR_LEFT_DOWN = 6
            const val DIR_RIGHT_UP = 7
            const val DIR_RIGHT_DOWN = 8
            const val DIR_CENTER = 9

        }

        fun toggleLeftBlink()

        fun toggleRightBlink()

        fun bind(view: View)

        fun unbind()

        fun onLeftJoyStick(power: Double, direction: Int)

        fun onRightJoyStick(power: Double, direction: Int)

        fun onConnectionToggle()

        fun toggleHeadlights()

        fun toggleLongDistanceHeadlights()

        fun toggleWarningLights()

        fun hornOn()

        fun hornOff()

        fun setConnected()

        fun setDisconnected()

    }

}