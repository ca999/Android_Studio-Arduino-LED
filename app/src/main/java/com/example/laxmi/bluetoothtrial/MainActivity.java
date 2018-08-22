package com.example.laxmi.bluetoothtrial;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
{
    Button bton;
    Button btoff;
    private boolean isBtConnected = false;

    private BluetoothAdapter mybluetooth=null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS="device_address";
    String HCname="";
    String HCaddress="";
    private ProgressDialog progress;
    BluetoothSocket btSocket=null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bton=(Button)findViewById(R.id.bton);
        btoff=(Button)findViewById(R.id.btoff);
        mybluetooth=BluetoothAdapter.getDefaultAdapter();//get the default bluetooth adapter in the device
        if(mybluetooth==null)//check if the device has a bluetooth service
        {
            Toast.makeText(getApplicationContext(),"Bluetooth device not available",Toast.LENGTH_LONG).show();
            finish();
        }
        else if(!mybluetooth.isEnabled())//if present then check if it is on
        {
            mybluetooth.enable();//switch on the bluetooth without user interaction
               Toast.makeText(this,"Bluetooth enabled",Toast.LENGTH_LONG).show();
              // pairedDevises();
        }
        for(long i=0;i<1000000000;i++)
        {
          //for some reason to get the bonded devices a delay is required,this loop is to provide that delay xD
        }
        pairedDevices=mybluetooth.getBondedDevices();


        if(pairedDevices.size()>0)
        {
            Toast.makeText(this,"Getting paired devices",Toast.LENGTH_LONG).show();
            for(BluetoothDevice bt:pairedDevices)
            {
                String btname=bt.getName();
                String btadd=bt.getAddress();
                if(btname.equals("HC-05"))//get the address of the bluetooth module used.I used HC-05 :)
                {
                    HCname=btname;
                    HCaddress=btadd;
                    onBluetooth();

                }
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(),"No paired devices",Toast.LENGTH_LONG).show();
        }

        bton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOn();
            }
        });
        btoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                turnOffLed();   //method to turn off
            }
        });


    }



    private void turnOn()
    {
       if(btSocket!=null)
       {
           try
           {
               btSocket.getOutputStream().write("TO".toString().getBytes());//sends intruction to arduino to switch on led via bluetooth
           }
           catch (IOException e)
           {
               Toast.makeText(MainActivity.this,"error",Toast.LENGTH_LONG).show();
           }
       }
    }
    private void turnOffLed()
    {
        if(btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("TF".toString().getBytes());//sends instruction to arduino via bluetooth to switch off
            }
            catch (IOException e)
            {
                Toast.makeText(MainActivity.this,"error",Toast.LENGTH_LONG).show();
            }


        }
    }


    private void onBluetooth()
    {
        Toast.makeText(this,HCaddress,Toast.LENGTH_LONG).show();
        new ConnectBT().execute();
    }

    private class ConnectBT extends AsyncTask<Void,Void,Void>//this class basically switches on the required bluetooth module
    {
        private boolean ConnectSuccess=true;

        @Override
        protected void onPreExecute()
        {
           // super.onPreExecute();
            progress=ProgressDialog.show(MainActivity.this,"Connecting","Please wait");


        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try
            {
                if(btSocket==null||!isBtConnected)
                {
                    mybluetooth=BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo=mybluetooth.getRemoteDevice(HCaddress);
                    btSocket=dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();

                }

            }
            catch (IOException e)
            {
                ConnectSuccess=false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!ConnectSuccess)
            {
                Toast.makeText(MainActivity.this,"Connection failed,sorry:(",Toast.LENGTH_LONG).show();
                finish();
            }
            else
            {
                Toast.makeText(MainActivity.this,"connected to"+HCname,Toast.LENGTH_LONG).show();
                isBtConnected=true;
            }
            progress.dismiss();
        }
    }
}
