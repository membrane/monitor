/* Copyright 2013 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */
package com.predic8.plugin.membrane;

import java.util.List;

import com.predic8.membrane.annot.bean.MCUtil;
import com.predic8.membrane.core.Router;
import com.predic8.membrane.core.interceptor.HTTPClientInterceptor;
import com.predic8.membrane.core.interceptor.Interceptor;

public class PlatformUtil {
	public static Router getRouter() {
		// TODO: ask com.predic8.membrane.osgi plugin for router instance 
		return null;
	}

	public static void loadConfiguration(String selected) {
		// TODO Auto-generated method stub
		
	}

	public static void saveConfiguration() {
		// TODO: save spring config to file it was loaded from
	}

	public static void saveConfiguration(String selected) {
		// TODO Auto-generated method stub
	}

	// TODO: persist these fields in config?
	private static boolean trackExchange;
	private static boolean indentMessage;

	public static boolean isTrackExchange() {
		return trackExchange;
	}
	
	public static void setTrackExchange(boolean trackExchange) {
		PlatformUtil.trackExchange = trackExchange;
	}
	
	public static boolean isIndentMessage() {
		return indentMessage;
	}
	
	public static void setIndentMessage(boolean indentMessage) {
		PlatformUtil.indentMessage = indentMessage;
	}
	
	public static boolean isAdjustHostHeader() {
		List<Interceptor> interceptors = getRouter().getTransport().getInterceptors();
		for (int i = 0; i < interceptors.size(); i++) {
			Interceptor interceptor = interceptors.get(i);
			if (interceptor instanceof HTTPClientInterceptor) {
				HTTPClientInterceptor hci = (HTTPClientInterceptor) interceptor;
				return hci.isAdjustHostHeader();
			}
		}
		return false;
	}
	
	public static void setAdjustHostHeader(boolean adjustHostHeader) {
		// exchange whole interceptor to avoid synchronization issues
		List<Interceptor> interceptors = getRouter().getTransport().getInterceptors();
		int httpClientInterceptorIndex = -1;
		HTTPClientInterceptor hci = null;
		for (int i = 0; i < interceptors.size(); i++)
			if (interceptors.get(i) instanceof HTTPClientInterceptor) {
				hci = MCUtil.clone((HTTPClientInterceptor) interceptors.get(i), false);
				httpClientInterceptorIndex = i;
				break;
			}

		if (hci == null)
			return;
		
		hci.setAdjustHostHeader(adjustHostHeader);
		interceptors.set(httpClientInterceptorIndex, hci);
	}

}
