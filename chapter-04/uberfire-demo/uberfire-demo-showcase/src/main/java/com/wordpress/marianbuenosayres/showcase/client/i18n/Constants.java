package com.wordpress.marianbuenosayres.showcase.client.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

/**
 * This uses GWT to provide client side compile time resolving of locales. See:
 * http://code.google.com/docreader/#p=google-web-toolkit-doc-1-5&s=google-web- toolkit-doc-1-5&t=DevGuideInternationalization
 * (for more information).
 * <p/>
 * Each method name matches up with a key in Constants.properties (the properties file can still be used on the server). To use
 * this, use <code>GWT.create(Constants.class)</code>.
 */
public interface Constants extends Messages {

    Constants INSTANCE = GWT.create(Constants.class);

    String Tasks_List();

    String Dashboards();

    String Process_Dashboard();

    String Business_Dashboard();

    String Process_Authoring();

    String Authoring();

    String Process_Management();

    String Work();

    String LogOut();

    String Home();

    String Process_Definitions();

    String Process_Instances();

    String Deploy();

    String Deployments();

    String newItem();

    String Role();

    String User();
    
    String Settings();
    
    String Users();

    String Jobs();


}
