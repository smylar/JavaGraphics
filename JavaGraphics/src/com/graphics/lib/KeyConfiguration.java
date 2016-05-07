package com.graphics.lib;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class KeyConfiguration {
	
	private List<KeyConfigurationItem> keyList = new ArrayList<KeyConfigurationItem>();
	
	public KeyConfiguration(InputStream inputStream){
		
		try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))){
			String line;

			while((line=br.readLine()) != null) {
				KeyConfigurationItem item = new KeyConfigurationItem();
				
			     String[] data = line.split("<>"); //TODO change to JSON
			     
			     if (data[0].length() == 1){
			    	 item.setKeyCode(KeyEvent.getExtendedKeyCodeForChar((int)data[0].charAt(0)));
			     }else{
			    	 item.setKeyCode(Integer.parseInt(data[0]));
			     }
			     item.setEventType(KeyConfigurationItem.EVENT_TYPE.PRESS);
			     item.setMethodName(data[1]);
			     keyList.add(item);
			     
			     if (data.length > 2){
			    	 KeyConfigurationItem release = new KeyConfigurationItem();
			    	 release.setKeyCode(item.getKeyCode());
			    	 release.setEventType(KeyConfigurationItem.EVENT_TYPE.RELEASE);
			    	 release.setMethodName(data[2]);
			    	 keyList.add(release);
			     }
			     
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<KeyConfigurationItem> getKeyList() {
		return keyList;
	}
	
}
