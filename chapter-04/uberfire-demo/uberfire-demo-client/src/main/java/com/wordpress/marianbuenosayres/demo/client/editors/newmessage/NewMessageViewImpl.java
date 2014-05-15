package com.wordpress.marianbuenosayres.demo.client.editors.newmessage;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import com.wordpress.marianbuenosayres.demo.client.i18n.Constants;
import com.wordpress.marianbuenosayres.demo.client.editors.newmessage.NewMessagePresenter.NewMessageView;

@Dependent
@Templated(value = "NewMessageViewImpl.html")
public class NewMessageViewImpl extends Composite implements NewMessageView {
    private Constants constants = GWT.create(Constants.class);

    private NewMessagePresenter presenter;
    
    @Inject
    @DataField
    private TextBox message;

    @Inject
    @DataField
    private Button createButton;

    @Override
    public void init(NewMessagePresenter p) {
    	this.presenter = p;
        createButton.setText(constants.SendMessage());
    }

    @EventHandler("createButton")
    public void createButton( ClickEvent e ) {
        presenter.sendMessage(message.getText());
    }

    @Override
    public String getMessage() {
        return this.message.getText();
    }
}
