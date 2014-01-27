package com.wordpress.marianbuenosayres.showcase.backend;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.uberfire.backend.server.IOWatchServiceNonDotImpl;
import org.uberfire.backend.server.io.IOSecurityAuth;
import org.uberfire.backend.server.io.IOSecurityAuthz;
import org.uberfire.commons.cluster.ClusterServiceFactory;
import org.uberfire.io.IOService;
import org.uberfire.io.impl.IOServiceDotFileImpl;
import org.uberfire.io.impl.cluster.IOServiceClusterImpl;
import org.uberfire.security.auth.AuthenticationManager;
import org.uberfire.security.authz.AuthorizationManager;

@ApplicationScoped
public class ApplicationScopedProducer {

    @Inject
    private IOWatchServiceNonDotImpl watchService;

    @Inject
    @IOSecurityAuth
    private AuthenticationManager authenticationManager;

    @Inject
    @IOSecurityAuthz
    private AuthorizationManager authorizationManager;

    @Inject
    @Named("clusterServiceFactory")
    private ClusterServiceFactory clusterServiceFactory;

    private IOService ioService;

    @PostConstruct
    public void setup() {
        if ( clusterServiceFactory == null ) {
            ioService = new IOServiceDotFileImpl( watchService );
        } else {
            ioService = new IOServiceClusterImpl( new IOServiceDotFileImpl( watchService ), clusterServiceFactory );
        }
        ioService.setAuthenticationManager( authenticationManager );
        ioService.setAuthorizationManager( authorizationManager );
    }

    @PreDestroy
    public void onShutdown() {
        ioService.dispose();
    }

    @Produces
    @Named("ioStrategy")
    public IOService ioService() {
        return ioService;
    }
}
