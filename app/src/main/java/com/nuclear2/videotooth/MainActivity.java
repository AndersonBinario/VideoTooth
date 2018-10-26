package com.nuclear2.videotooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;

    private ListView lstDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        lstDevices = (ListView) findViewById(R.id.lstDevices);
        lstDevices.setAdapter(lstAdapter);
        lstDevices.setOnItemClickListener(onItemClickListener);

        startBluetooth();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregister();
    }

    private BluetoothAdapter mBluetoothAdapter;

    public void startBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else
            findPared();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (requestCode == RESULT_OK) {
                // success
                findPared();
            } else {
                // cué, cué, cué
                // user don't turn the bluetooth on
            }
        }
    }

    private ArrayList<Device> mArrayAdapter = new ArrayList<>();

    public void findPared() {
        mArrayAdapter.clear();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(new Device(device.getName(), device.getAddress(), device.getUuids()));
            }
        }

        // TODO
        register();
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mArrayAdapter.add(new Device(device.getName(), device.getAddress(), device.getUuids()));
            }
        }
    };

    public void register() {
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
    }

    public void unregister() {
        // Register the BroadcastReceiver
        unregisterReceiver(mReceiver); // Don't forget to unregister during onDestroy
    }

    public void makeVisible() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    BluetoothSocket socket;
    public void connect(Device device) {
        if (device.getUuids() != null)
            for (int i = 0; i < device.getUuids().length; i++) {
                try {
                    BluetoothServerSocket serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("VideoTooth", UUID.fromString(device.getUuids()[i].toString()));

                    socket = serverSocket.accept();
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    private ListAdapter lstAdapter = new ListAdapter() {
        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int i) {
            return true;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {
            // do nothing
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
            // do nothing
        }

        @Override
        public int getCount() {
            return mArrayAdapter == null || mArrayAdapter.size() == 0 ? 1 : mArrayAdapter.size();
        }

        @Override
        public Object getItem(int i) {
            if (mArrayAdapter == null || mArrayAdapter.size() == 0)
                return null;
            else
                return mArrayAdapter.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View viewDevice;
            if (view == null)
                viewDevice = View.inflate(getApplicationContext(), R.layout.listitem_device, null);
            else
                viewDevice = view;

            TextView txtTitle = (TextView) viewDevice.findViewById(R.id.txtTitle);
            if (i == 0 && (mArrayAdapter == null || mArrayAdapter.size() == 0)) {
                if (mArrayAdapter == null)
                    txtTitle.setText(R.string.main_searching);
                else
                    txtTitle.setText(R.string.main_no_devices_found);
            } else
                txtTitle.setText(((Device) getItem(i)).getName());

            return viewDevice;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            connect(mArrayAdapter.get(i));
        }
    };

    public static class Device {
        private String name;
        private String address;
        private ParcelUuid[] uuids;

        public Device(String name, String address, ParcelUuid[] uuids) {
            setName(name);
            setAddress(address);
            setUuids(uuids);
        }

        public String getName() {
            return name;
        }
        private void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }
        private void setAddress(String address) {
            this.address = address;
        }

        public ParcelUuid[] getUuids() {
            return uuids;
        }
        private void setUuids(ParcelUuid[] uuids) {
            this.uuids = uuids;
        }
    }
}
