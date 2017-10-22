package com.peterstovka.apsbtcar

import com.erz.joysticklibrary.JoyStick

/**
 * @author [Peter Stovka](mailto:stovka.peter@gmail.com)
 */
open class JoyStickAdapter : JoyStick.JoyStickListener {

    override fun onTap() {}

    override fun onDoubleTap() {}

    override fun onMove(joyStick: JoyStick?, angle: Double, power: Double, direction: Int) {}
}