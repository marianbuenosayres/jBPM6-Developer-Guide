package com.wordpress.marianbuenosayres.proccep;

import java.util.LinkedList;
import java.util.List;

import com.wordpress.marianbuenosayres.service.NotifierService;

public class MockNotifierService implements NotifierService {

	private List<String> warnings = new LinkedList<String>();
	
	@Override
	public void sendWarning(String warning) {
		System.out.println(warning);
		warnings.add(warning);
	}
	
	public List<String> getWarnings() {
		return warnings;
	}

}
