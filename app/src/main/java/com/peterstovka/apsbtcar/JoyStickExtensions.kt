package com.peterstovka.apsbtcar

import com.erz.joysticklibrary.JoyStick

/**
 * @author [Peter Stovka](mailto:stovka.peter@gmail.com)
 */

fun JoyStick.setListener(listener: (power: Double, direction: Int) -> Unit) {
    setListener(object: JoyStickAdapter() {
        override fun onMove(joyStick: JoyStick?, angle: Double, power: Double, direction: Int) {
            listener.invoke(power, direction)
        }
    })
}
