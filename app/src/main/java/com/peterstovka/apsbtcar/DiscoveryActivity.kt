package com.peterstovka.apsbtcar

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_discovery.*


class DiscoveryActivity : AppCompatActivity() {

    private val pairedAdapter = DiscoveryResultAdapter()
    private val foundAdapter = DiscoveryResultAdapter()

    private val REQUEST_ENABLE_BT = 145

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discovery)

        pairedDevicesList.adapter = pairedAdapter
        pairedDevicesList.layoutManager = LinearLayoutManager(this)
        pairedDevicesList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        foundDevicesList.adapter = foundAdapter
        foundDevicesList.layoutManager = LinearLayoutManager(this)
        foundDevicesList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        pairedAdapter.itemClickListener = { connectToDevice(it) }
        foundAdapter.itemClickListener = { connectToDevice(it) }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(applicationContext, "The device has no bluetooth support.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (adapter.isEnabled) {
            startDiscovery()
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun connectToDevice(item: DiscoveryResultAdapter.Item) {
        intent.putExtra("device", item.device)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            startDiscovery()
        } else {
            Toast.makeText(applicationContext, "You must enable bluetooth on the device.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun startDiscovery() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(searchReceiver, filter)
        BluetoothAdapter.getDefaultAdapter().startDiscovery()
        getPairedDevices()
    }

    private fun getPairedDevices() {
        BluetoothAdapter.getDefaultAdapter().bondedDevices.forEach {
            pairedAdapter.addItem(DiscoveryResultAdapter.Item(
                    mac = it.address ?: "NULL",
                    name = it.name ?: "NULL",
                    device = it
            ))
        }
    }

    private val searchReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                foundAdapter.addItem(DiscoveryResultAdapter.Item(
                        mac = device.address ?: "NULL",
                        name = device.name ?: "NULL",
                        device = device
                ))
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (BluetoothAdapter.getDefaultAdapter().isDiscovering) {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
        }

        unregisterReceiver(searchReceiver)
    }
}
