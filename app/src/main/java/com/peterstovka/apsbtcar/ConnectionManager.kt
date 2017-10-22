package com.peterstovka.apsbtcar

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

/**
 * @author [Peter Stovka](mailto:stovka.peter@gmail.com)
 */
class ConnectionManager(
        private val handler: Handler,
        private val callback: Callback
) {

    private var service: ConnectionService? = null

    interface Callback {

        fun onConnected()

        fun onDisconnected()

    }

    fun isConnected(): Boolean {
        if (service == null) {
            return false
        }

        if (service!!.socket == null) {
            return false
        }

        return service!!.socket!!.isConnected
    }

    fun connect(device: BluetoothDevice) {
        if (service != null) {
            service?.close()
        }

        service = ConnectionService(handler, device, callback, callbackListener)
    }

    private val callbackListener = object: Callback {
        override fun onConnected() {
            onSocketConnected()
        }

        override fun onDisconnected() {
            onSocketDisconnected()
        }
    }

    fun close() {
        service?.close()
        service = null
    }

    fun write(string: String) {
        if (isConnected()) {
            service?.write(string.toByteArray())
        }
    }

    private var heart: KeepAliveService? = null

    private fun onSocketConnected() {
        if (heart != null) {
            heart?.stopService()
            heart = null
        }

        service?.let {
            heart = KeepAliveService(it)
            heart?.startService()
        }
    }

    private fun onSocketDisconnected() {
        heart?.let {
            it.stopService()
            heart = null
        }
    }

    class ConnectionService(
            private val handler: Handler,
            private val device: BluetoothDevice,
            vararg private val callback: Callback
    ) {

        val TAG = "ConnectionService"

        companion object {

            const val MESSAGE_READ = 1
            const val MESSAGE_WRITE_OK = 2
            const val MESSAGE_WRITE_FAILED = 3

        }

        var socket: BluetoothSocket? = null
        private var connectedThread: ConnectedThread? = null

        init {
            val connectThread = ConnectThread()
            connectThread.execute()
        }

        fun close() {
            connectedThread?.cancel()
        }

        fun write(message: ByteArray) {
            connectedThread?.write(message)
        }

        private inner class ConnectThread : AsyncTask<Void, Void, Unit>() {

            override fun doInBackground(vararg params: Void?) {
                socket = device.createRfcommSocketToServiceRecord(device.uuids.first()!!.uuid)
                try {
                    socket?.connect()
                } catch (e: Exception) {
                    socket?.close()
                    Log.e(TAG,  "Connection failed.")
                }
            }

            override fun onPostExecute(result: Unit?) {
                socket?.let {
                    if (it.isConnected) {
                        connectedThread = ConnectedThread(it)
                        connectedThread?.start()
                        callback.forEach { it.onConnected() }
                    }
                }
            }
        }

        private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {

            val TAG = "ConnectionService"

            private val inputStream: InputStream
            private val outputStream: OutputStream

            init {
                var input: InputStream? = null
                var output: OutputStream? = null

                try {
                    input = socket.inputStream
                } catch (e: IOException) {
                    Log.e(TAG, "Could not get input stream.", e)
                }

                try {
                    output = socket.outputStream
                } catch (e: IOException) {
                    Log.e(TAG, "Could not get output stream.", e)
                }

                inputStream = input ?: throw NullPointerException("Hey, the input stream cannot be null.")
                outputStream = output ?: throw NullPointerException("Hey, the output stream cannot be null.")
            }

            private var buffer = ByteArray(1024)

            override fun run() {
                var numBytes: Int

                while (true) {
                    try {
                        numBytes = inputStream.read(buffer)

                        val message = handler.obtainMessage(
                                ConnectionService.MESSAGE_READ,
                                numBytes,
                                -1,
                                buffer
                        )

                        message.sendToTarget()
                    } catch (e: IOException) {
                        Log.e(TAG, "The input stream has been disconnected.")
//                        close()
                        break
                    }
                }
            }

            fun write(bytes: ByteArray) {
                try {
                    outputStream.write(bytes)

                    handler.obtainMessage(
                            MESSAGE_WRITE_OK,
                            -1,
                            -1,
                            buffer
                    ).sendToTarget()
                } catch (e: IOException) {
                    Log.e(TAG, "Could not write.")

                    handler.obtainMessage(
                            MESSAGE_WRITE_FAILED
                    ).sendToTarget()
                }
            }

            fun cancel() {
                try {
                    socket.close()
                    callback.forEach { it.onConnected() }
                } catch (e: IOException) {
                    Log.e(TAG, "Could not close the socket.", e)
                }
            }

        }

    }

    class KeepAliveService(private val connectionService: ConnectionService) : TimerTask() {

        var timer: Timer? = null

        override fun run() {
            connectionService.write("beep".toByteArray())
        }

        fun startService() {
            timer = Timer("heart")
            timer?.scheduleAtFixedRate(this, 0, 500)
        }

        fun stopService() {
            timer?.cancel()
        }
    }

}