package com.graphics.lib;

import java.util.ArrayList;
import java.util.List;

public class KeyConfiguration {
	private List<KeyConfigurationItem> keyList = new ArrayList<KeyConfigurationItem>();

	public List<KeyConfigurationItem> getKeyList() {
		return keyList;
	}

	public void setKeyList(List<KeyConfigurationItem> keyList) {
		this.keyList = keyList;
	}
}
