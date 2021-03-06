/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
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

package org.eclipse.gemini.blueprint.blueprint.config;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.blueprint.CustomType;
import org.eclipse.gemini.blueprint.blueprint.TestComponent;
import org.eclipse.gemini.blueprint.blueprint.container.SpringBlueprintConverter;
import org.eclipse.gemini.blueprint.blueprint.container.support.BlueprintEditorRegistrar;
import org.eclipse.gemini.blueprint.service.importer.support.ServiceReferenceEditor;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.ReifiedType;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ObjectUtils;

/**
 * @author Costin Leau
 * 
 */
public class TypeConverterTest extends TestCase {

	private static final String CONFIG = "type-converters.xml";

	private GenericApplicationContext context;
	private XmlBeanDefinitionReader reader;

	protected void setUp() throws Exception {
		context = new GenericApplicationContext();
		context.setClassLoader(getClass().getClassLoader());
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

		beanFactory.registerCustomEditor(ServiceReference.class, ServiceReferenceEditor.class);
		beanFactory.addPropertyEditorRegistrar(new BlueprintEditorRegistrar());

		reader = new XmlBeanDefinitionReader(context);
		reader.loadBeanDefinitions(new ClassPathResource(CONFIG, getClass()));
		context.refresh();
	}

	protected void tearDown() throws Exception {
		context.close();
		context = null;
	}

	public void testNumberOfBeans() throws Exception {
		System.out.println("The beans declared are: " + ObjectUtils.nullSafeToString(context.getBeanDefinitionNames()));
		assertTrue("not enough beans found", context.getBeanDefinitionCount() >= 3);
	}

	public void testReferenceToConverter() throws Exception {
		TestComponent component = (TestComponent) context.getBean("conversion");
		Object prop = component.getPropB();
		assertTrue(prop instanceof ComponentHolder);
		assertEquals("rachmaninoff", ((ComponentHolder) prop).getProperty());
	}

	public void testNestedConverter() throws Exception {
		TestComponent component = (TestComponent) context.getBean("conversion");
		Object prop = component.getPropA();
		assertTrue(prop instanceof TestComponent);
		assertEquals("sergey", ((TestComponent) prop).getPropA());
	}

	public void testConversionService() throws Exception {
		SpringBlueprintConverter cs = new SpringBlueprintConverter(context.getBeanFactory());

		Object converted = cs.convert("1", new ReifiedType(Long.class));
		assertNotNull(converted);
		assertEquals(Long.valueOf("1"), converted);

		assertEquals(Boolean.TRUE, cs.convert("T", new ReifiedType(Boolean.class)));
	}

	public void testBooleanConversion() throws Exception {
		TestComponent comp = (TestComponent) context.getBean("booleanConversion");
		assertEquals(Boolean.TRUE, comp.getPropA());
	}

	public void testArrayConversion() throws Exception {
		TestComponent comp = (TestComponent) context.getBean("arrayConversion");
		assertTrue(comp.getPropA() instanceof CustomType[]);
	}

	public void testReferenceDelegate() throws Exception {
		TestComponent comp = (TestComponent) context.getBean("serviceReference");
		assertNotNull(comp.getServiceReference());
	}

	public void testInvalidInjection1() throws Exception {
		try {
			TestComponent comp = (TestComponent) context.getBean("invalidInjection1");
			fail("expected exception");
		} catch (Exception ex) {
		}
	}

	public void testInvalidInjection2() throws Exception {
		try {
			TestComponent comp = (TestComponent) context.getBean("invalidInjection2");
			fail("expected exception");
		} catch (Exception ex) {
		}
	}
}