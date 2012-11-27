/******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.extender.internal.activator;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;

import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.eclipse.gemini.blueprint.mock.MockServiceRegistration;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextListener;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.extender.event.BootstrappingDependenciesEvent;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ListListenerAdapterTest extends TestCase {

    private static final String SERVICE_PROPERTY = "service";
    
    private boolean fired;

    public void testX() throws Exception {
        
        fired = false;

        final OsgiBundleApplicationContextListener<OsgiBundleApplicationContextEvent> listener = new OsgiBundleApplicationContextListener<OsgiBundleApplicationContextEvent>() {

            public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
                fired = true;
            }
        };

        final ServiceReference ref = null;

        MockBundleContext ctx = new MockBundleContext() {

            public ServiceReference getServiceReference(String clazz) {
                return ref;
            }

            public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
                return new ServiceReference[] { ref };
            }

            public Object getService(ServiceReference reference) {
                if (reference != null) {
                    Object service = reference.getProperty(SERVICE_PROPERTY);
                    if (service != null) {
                        return service;
                    }
                }

                return (reference == ref ? listener : super.getService(reference));
            }

            @SuppressWarnings("rawtypes")
            public ServiceRegistration registerService(String[] clazzes, final Object service, Dictionary properties) {
                MockServiceRegistration reg = new MockServiceRegistration(properties);

                MockServiceReference ref = new MockServiceReference(getBundle(), properties, reg, clazzes) {

                    @Override
                    public Object getProperty(String key) {
                        if (SERVICE_PROPERTY.equals(key)) {
                            return service;
                        } else {
                            return super.getProperty(key);
                        }
                    }

                };
                ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, ref);

                for (Iterator iter = serviceListeners.iterator(); iter.hasNext();) {
                    ServiceListener listener = (ServiceListener) iter.next();
                    listener.serviceChanged(event);
                }

                return reg;
            }

        };

        ListListenerAdapter adapter = new ListListenerAdapter(ctx);
        adapter.afterPropertiesSet();
        ctx.registerService(OsgiBundleApplicationContextListener.class.getName(), listener, null);
        adapter.onOsgiApplicationEvent(new BootstrappingDependenciesEvent(new OsgiBundleXmlApplicationContext(), new MockBundle(),
            new ArrayList<OsgiServiceDependencyEvent>(), null, 0));
        Assert.assertTrue(fired);
        adapter.destroy();
    }
}