package org.pentaho.platform.engine.core.system.objfac.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Registers the custom tag handlers for our namespace.
 *
 * User: nbaker
 * Date: 1/16/13
 */
public class BeanAttributeNamespaceHandler extends NamespaceHandlerSupport {

  public void init() {
    registerBeanDefinitionDecorator(Const.SCHEMA_TAG_ATTRIBUTES, new BeanAttributeHandler());

    registerBeanDefinitionParser(Const.SCHEMA_TAG_LIST, new BeanListParser());
    registerBeanDefinitionParser(Const.SCHEMA_TAG_BEAN, new BeanParser());
    registerBeanDefinitionDecorator(Const.SCHEMA_PUBLISH, new BeanPublishParser());
  }
}