package com.wordpress.marianbuenosayres.omstrategy;

import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;

/**
 * This is an extension of an already existing ObjectMarshallingStrategy object,
 * the {@link JPAPlaceholderResolverStrategy}. The original one only reads an existing
 * entity from the database. This extension also persist those objects at serializing 
 * time, in order to keep all changes made to the model in the database.
 *  
 * @author marianbuenosayres
 *
 */
public class JPAReadAndWriteStrategy extends JPAPlaceholderResolverStrategy {

	private Environment env = null;
	private EntityManagerFactory emf = null;
	
	public JPAReadAndWriteStrategy(EntityManagerFactory emf) {
		super(emf);
		this.emf = emf;
	}

	public JPAReadAndWriteStrategy(Environment env) {
		super(env);
		this.env = env;
	}

	/**
	 * Decorates the super-call to add database persistence before storing the ID 
	 */
	@Override
	public void write(ObjectOutputStream os, Object object) throws IOException {
		EntityManagerFactory emf = getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        if (getClassIdValue(object) == null) {
        	em.persist(object);
        } else {
        	em.merge(object);
        }
		super.write(os, object);
	}

	/**
	 * Decorates the super-call to add database persistence before storing the ID 
	 */
	@Override
	public byte[] marshal(Context context, ObjectOutputStream os, Object object)
			throws IOException {
		EntityManagerFactory emf = getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        if (getClassIdValue(object) == null) {
        	em.persist(object);
        } else {
        	em.merge(object);
        }
		return super.marshal(context, os, object);
	}

	private EntityManagerFactory getEntityManagerFactory() {
		if (emf == null) {
			emf = (EntityManagerFactory) env.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
		}
		return emf;
	}


}
