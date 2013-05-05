/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.rebind;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.pentaho.mantle.client.events.EventBusUtil;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPackage;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

public class EventBusUtilGenerator extends Generator {

  private String packageName;
  private String className;
  private TypeOracle typeOracle;
  private TreeLogger logger;

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    this.logger = logger;
    typeOracle = context.getTypeOracle();

    try {
      // get classType and save instance variables
      JClassType classType = typeOracle.getType(typeName);
      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      // Generate class source code
      generateClass(logger, context);

    } catch (Exception e) {
      // record to logger that Map generation threw an exception
      logger.log(TreeLogger.ERROR, "PropertyMap ERROR!!!", e);
    }

    // return the fully qualifed name of the class generated
    return packageName + "." + className;
  }

  private void generateClass(TreeLogger logger, GeneratorContext context) {

    // get print writer that receives the source code
    PrintWriter printWriter = null;
    printWriter = context.tryCreate(logger, packageName, className);
    // print writer if null, source code has ALREADY been generated, return
    if (printWriter == null)
      return;

    // init composer, set class properties, create source writer
    ClassSourceFileComposerFactory composer = null;
    composer = new ClassSourceFileComposerFactory(packageName, className);
    composer.addImplementedInterface(EventBusUtil.class.getName());
    composer.addImport(GwtEvent.class.getName());
    composer.addImport(JavaScriptObject.class.getName());
    composer.addImport(EventBus.class.getName());

    SourceWriter sourceWriter = null;
    sourceWriter = composer.createSourceWriter(context, printWriter);

    sourceWriter.indent();

    // generator constructor source code
    generateConstructor(sourceWriter);
    generateMethods(sourceWriter);

    // close generated class
    sourceWriter.outdent();
    sourceWriter.println("}");

    // commit generated class
    context.commit(logger, printWriter);
  }

  private void generateMethods(SourceWriter sourceWriter) {

    sourceWriter.println("public native void invokeEventBusJSO(final JavaScriptObject jso, final Object... params)");
    sourceWriter.println("/*-{");
    sourceWriter.indent();
    sourceWriter.println("jso.call(this, params)");
    sourceWriter.outdent();
    sourceWriter.println("}-*/;");

    sourceWriter.println();
    sourceWriter.println("public void fireEvent(final String eventType) { ");
    sourceWriter.indent();
    try {
      // find Command implementors
      ArrayList<JClassType> implementingTypes = new ArrayList<JClassType>();
      JPackage pack = typeOracle.getPackage(EventBusUtil.class.getPackage().getName());
      JClassType eventSourceType = typeOracle.getType(GwtEvent.class.getName());

      for (JClassType type : pack.getTypes()) {
        if (type.isAssignableTo(eventSourceType)) {
          implementingTypes.add(type);
        }
      }

      sourceWriter.println("if(false){}"); // placeholder
      for (JClassType implementingType : implementingTypes) {
        sourceWriter.println("else if(eventType.equals(\"" + implementingType.getSimpleSourceName() + "\")){");
        sourceWriter.indent();
        sourceWriter.println("EVENT_BUS.fireEvent(new " + implementingType.getName() + "());");
        sourceWriter.outdent();
        sourceWriter.println("}");
      }

    } catch (Exception e) {
      // record to logger that Map generation threw an exception
      logger.log(TreeLogger.ERROR, "Error generating BindingContext!!!", e);
    }
    sourceWriter.outdent();
    sourceWriter.println("}");

    sourceWriter.println();
    sourceWriter.println("public void addHandler(final String eventType, final JavaScriptObject handler) { ");
    sourceWriter.indent();
    try {
      // find Command implementors
      ArrayList<JClassType> implementingTypes = new ArrayList<JClassType>();
      JPackage pack = typeOracle.getPackage(EventBusUtil.class.getPackage().getName());
      JClassType eventSourceType = typeOracle.getType(GwtEvent.class.getName());

      for (JClassType type : pack.getTypes()) {
        if (type.isAssignableTo(eventSourceType)) {
          implementingTypes.add(type);
        }
      }

      sourceWriter.println("if(false){}"); // placeholder
      for (JClassType implementingType : implementingTypes) {
        sourceWriter.println("else if(eventType.equals(\"" + implementingType.getSimpleSourceName() + "\")){");
        sourceWriter.indent();

        JClassType handlerType = typeOracle.getType(EventBusUtil.class.getPackage().getName() + "." + implementingType.getName() + "Handler");
        sourceWriter.println("EVENT_BUS.addHandler(" + implementingType.getName() + ".TYPE, new " + implementingType.getName() + "Handler() {");
        sourceWriter.indent();
        for (JMethod handlerMethod : handlerType.getMethods()) {

          String parameterStr = "";
          for (JMethod eventMethod : implementingType.getMethods()) {
            if (eventMethod.isPublic() && !eventMethod.isStatic() && eventMethod.isConstructor() == null
                && !"void".equalsIgnoreCase(eventMethod.getReturnType().getSimpleSourceName()) && !eventMethod.getName().equals("getAssociatedType")) {
              parameterStr += ", event." + eventMethod.getName() + "()";
            }
          }

          sourceWriter.println("public void " + handlerMethod.getName() + "(" + implementingType.getName() + " event) {");
          sourceWriter.indent();
          sourceWriter.println("invokeEventBusJSO(handler" + parameterStr + ");");
          sourceWriter.outdent();
          sourceWriter.println("}");
        }
        sourceWriter.outdent();
        sourceWriter.println("});");

        sourceWriter.outdent();
        sourceWriter.println("}");
      }
    } catch (Exception e) {
      // record to logger that Map generation threw an exception
      logger.log(TreeLogger.ERROR, "Error generating BindingContext!!!", e);
    }

    sourceWriter.outdent();
    sourceWriter.println("}");
  }

  private void generateConstructor(SourceWriter sourceWriter) {
    // start constructor source generation
    sourceWriter.println("public " + className + "() { ");
    sourceWriter.indent();
    sourceWriter.println("super();");
    sourceWriter.outdent();
    sourceWriter.println("}");
  }

  @SuppressWarnings("unused")
  private String boxPrimative(JType type) {
    if (type.isPrimitive() != null) {
      JPrimitiveType primative = type.isPrimitive();
      return primative.getQualifiedBoxedSourceName();
    } else {
      return type.getQualifiedSourceName();
    }
  }
}
