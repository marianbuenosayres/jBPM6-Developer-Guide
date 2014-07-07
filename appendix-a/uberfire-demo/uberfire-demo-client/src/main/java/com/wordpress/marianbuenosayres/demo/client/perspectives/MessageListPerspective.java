package com.wordpress.marianbuenosayres.demo.client.perspectives;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.mvp.PlaceManager;
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
    
    @Inject
    private PlaceManager placeManager;

    @Perspective
    public PerspectiveDefinition getPerspective() {
        final PerspectiveDefinition p = new PerspectiveDefinitionImpl( PanelType.ROOT_LIST );
        p.setName("Demo for book perspective");
        p.getRoot().addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "BookDemoMessageScreen" ) ) );
        p.setTransient(true);
        return p;
    }

}
