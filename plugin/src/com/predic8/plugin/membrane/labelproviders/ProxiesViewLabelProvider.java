/* Copyright 2009 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.predic8.plugin.membrane.labelproviders;

import java.text.NumberFormat;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.predic8.membrane.core.rules.Rule;
import com.predic8.membrane.core.rules.ServiceProxy;
import com.predic8.membrane.core.rules.StatisticCollector;
import com.predic8.plugin.membrane.MembraneUIPlugin;
import com.predic8.plugin.membrane.PlatformUtil;
import com.predic8.plugin.membrane.resources.ImageKeys;


public class ProxiesViewLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider  {
	
	private NumberFormat nf = NumberFormat.getInstance();
	
	private Image proxyImage;
	
	private Image serviceProxyImage;
	
	public ProxiesViewLabelProvider() {
		nf.setMaximumFractionDigits(3);
		proxyImage =  MembraneUIPlugin.getDefault().getImageRegistry().getDescriptor(ImageKeys.IMAGE_PROXY).createImage();
		serviceProxyImage =  MembraneUIPlugin.getDefault().getImageRegistry().getDescriptor(ImageKeys.IMAGE_SERVICE_PROXY).createImage();
	}
	
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex != 0)
			return null;
		if (element instanceof ServiceProxy) {
			return serviceProxyImage;
		}
		return proxyImage;
	}

	public String getColumnText(Object element, int columnIndex) {
		Rule rule = (Rule)element;
		
		StatisticCollector statistics = PlatformUtil.getRouter().getExchangeStore().getStatistics(rule.getKey());
		
		switch (columnIndex) {
		case 0:
			return rule.toString();
		case 1:
			return "" + statistics.getCount();
		default:
			throw new RuntimeException("Table in proxies view  must have only 3 columns");
		}
	}

	public Color getBackground(Object element, int columnIndex) {
		
		return null;
	}

	public Color getForeground(Object element, int columnIndex) {
		
		return null;
	}

}
