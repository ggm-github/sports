package com.uway.sports.push.android;

import com.uway.sports.push.AndroidNotification;

public class AndroidListcast extends AndroidNotification {
	public AndroidListcast(String appkey, String appMasterSecret) throws Exception {
			setAppMasterSecret(appMasterSecret);
			setPredefinedKeyValue("appkey", appkey);
			this.setPredefinedKeyValue("type", "listcast");
	}
	
	public void setDeviceToken(String token) throws Exception {
    	setPredefinedKeyValue("device_tokens", token);
    }

}