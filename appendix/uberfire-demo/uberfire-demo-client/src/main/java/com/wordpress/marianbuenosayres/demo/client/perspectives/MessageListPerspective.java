package com.wordpress.marianbuenosayres.demo.client.perspectives;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;

import org.uberfire.lifecycle.OnStartup;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PanelType;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;

/**
 * A Perspective to show Messages 
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "BookDemoMessages", isDefault = false)
public class MessageListPerspective {
    
    @Perspective
    public PerspectiveDefinition getPerspective() {
        final PerspectiveDefinition p = new PerspectiveDefinitionImpl( PanelType.ROOT_LIST );
        p.setName("Demo for book perspective");
        p.getRoot().addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "BookDemoMessageScreen" ) ) );
        p.setTransient( true );
        return p;
    }

    @OnStartup
    public void init() {
        
    }
}
