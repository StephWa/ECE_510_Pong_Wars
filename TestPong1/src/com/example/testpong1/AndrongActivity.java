package com.example.testpong1;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class AndrongActivity extends Activity
{
   private AndrongSurfaceView pongSurfaceView;
   private BluetoothAdapter BtAdapter;
   private ArrayList<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>();

   /**
    * Called when the activity is first created.
    */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.main);
      pongSurfaceView = (AndrongSurfaceView) findViewById(R.id.androng);
      pongSurfaceView.setTextView((TextView) findViewById(R.id.text));
      
   // Register the BroadcastReceiver
      IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
      //filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
      //filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
      registerReceiver(mReceiver, filter);
   }

   // Create a BroadcastReceiver for ACTION_FOUND
   private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	
	   @Override
	   public void onReceive(Context context, Intent intent) {
		   String action = intent.getAction();
		   // When discovery finds a device
		   if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			   showToast("found device");
			   // Get the BluetoothDevice object from the Intent
			   BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			   // Add the name and address to an array adapter to show in a ListView
			   btDeviceList.add(device);
		   }
	   }
   };
   
   private static final int MENU_PAUSE = 4;
   private static final int MENU_RESUME = 5;
   private static final int MENU_START_1P = 6;
   private static final int MENU_START_2P = 7;
   private static final int MENU_START_0P = 8;
   private static final int MENU_SHOWINFO = 10;
   private static final int MENU_SOUND_ON = 11;

   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
      super.onCreateOptionsMenu(menu);
      menu.add(0, MENU_START_1P, 0, R.string.menu_start_1p);
      menu.add(0, MENU_START_2P, 0, R.string.menu_start_2p);
      menu.add(0, MENU_START_0P, 0, R.string.menu_start_0p);
      menu.add(0, MENU_PAUSE, 0, R.string.menu_pause);
      menu.add(0, MENU_RESUME, 0, R.string.menu_resume);
      menu.add(0, MENU_SOUND_ON, 0, R.string.menu_sound);
      menu.add(0, MENU_SHOWINFO, 0, R.string.menu_info);
      return true;
   }

   @Override
   public boolean onMenuOpened(int featureId, Menu menu)
   {
      AndrongThread androidPongThread = pongSurfaceView.getAndroidPongThread();
      super.onMenuOpened(featureId, menu);
      androidPongThread.pause();
      return true;
   }
   
   public static final int REQUEST_ENABLE_BT = 1;		// request code parameter
   
   private void checkForBluetooth() {
	   //check to see if device supports Bluetooth
  	   BtAdapter = BluetoothAdapter.getDefaultAdapter(); 
  	   
       if(BtAdapter == null)
      	 showToast(getString(R.string.noBtMessage));
       else {
      	 //enable Bluetooth if not already enabled
      	 if(!BtAdapter.isEnabled()){
      		 // create intent that issues a request to system settings to enable Bluetooth
      		 Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			 startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);           		 
      	 }
       	 Set<BluetoothDevice> pairedDevices = BtAdapter.getBondedDevices();
       	 // If there are paired devices
       	 if (pairedDevices.size() > 0) {
       		 // Loop through paired devices
       		 for (BluetoothDevice device : pairedDevices) {
       			 // Add the name and address to an array adapter to show in a ListView
       			 btDeviceList.add(device);
       		 }
       	 }
    }
       
      	 //look for other Bluetooth devices
      	 BtAdapter.startDiscovery();
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      AndrongThread androidPongThread = pongSurfaceView.getAndroidPongThread();
      switch (item.getItemId())
      {
         case MENU_START_1P:
            androidPongThread.doStart1p();
            return true;
         case MENU_START_2P:     	 
            checkForBluetooth();
            	           	 
            androidPongThread.doStart2p();
            return true;
         case MENU_START_0P:
            androidPongThread.doStart0p();
            return true;
         case MENU_PAUSE:
            androidPongThread.pause();
            return true;
         case MENU_RESUME:
            androidPongThread.unpause();
            return true;
         case MENU_SHOWINFO:
            androidPongThread.toggleDiagnosticInformation();
            return true;
         case MENU_SOUND_ON:
            androidPongThread.toggleSound();
            return true;
      }

      return false;
   }
   
   /**
    * displays toast
    */
   public void showToast(final String toast)
   {
       runOnUiThread(new Runnable() {
           public void run()
           {
               Toast.makeText(AndrongActivity.this, toast, Toast.LENGTH_SHORT).show();
           }
       });
   }
   /**
    * Invoked when the Activity loses user focus.
    */
   @Override
   protected void onPause()
   {
      super.onPause();
      AndrongThread androidPongThread = pongSurfaceView.getAndroidPongThread();
      androidPongThread.pause(); // pause game when Activity pauses
   }

   @Override
   protected void onResume()
   {
      super.onResume();
      AndrongThread androidPongThread = pongSurfaceView.getAndroidPongThread();
      androidPongThread.resumeGame();
   }

   protected void onDestroy()
   {
      super.onDestroy();
      SoundManager.cleanup();
      unregisterReceiver(mReceiver);
   }
}
