/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.uibinder.rebind;

import com.google.common.reflect.ClassPath;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import jsinterop.annotations.JsType;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.api.annotations.ClassNames;
import org.jboss.errai.common.client.api.annotations.Properties;
import org.jboss.errai.common.client.api.annotations.Property;
import org.jboss.errai.ui.uibinder.client.local.api.UiBinderStarter;
import org.jboss.errai.ui.uibinder.client.local.api.annotations.Namespace;
import org.jboss.errai.ui.uibinder.client.local.api.annotations.UiBinder;
import org.jboss.errai.ui.uibinder.client.local.utils.UiBinderUtil;
import org.jboss.errai.ui.uibinder.shared.ElementPropertiesHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jboss.errai.codegen.builder.impl.ObjectBuilder.newInstanceOf;
import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 2/7/18.
 */
public class UiBinderCodeGenerator extends Generator {

    // package -> Element -> ElementPropertiesHolder
    final private Map<String, Map<String, ElementPropertiesHolder>> packageHolder = new HashMap<>();
    // Template -> ns -> package
    final private Map<String, Map<String, String>> templateToNsMapper = new HashMap<>();

    private ClassStructureBuilder body;

    private static final Logger logger = LoggerFactory.getLogger(UiBinderCodeDecorator.class);


    private static final String IMPL_TYPE_NAME =
            UiBinderStarter.class.getSimpleName() + "Impl";
    private static final String IMPL_PACKAGE_NAME =
            UiBinderStarter.class.getPackage().getName();

    @Override
    public String generate(TreeLogger treeLogger, GeneratorContext context,
                           String requestedClass) {


        for (JClassType type : context.getTypeOracle().getTypes()) {
            UiBinder annotation = type.getAnnotation(UiBinder.class);
            if (annotation != null) {

                String path = type.getPackage().getName() + "." + type.getName();
                Map<String, String> templated = new HashMap<>();
                templateToNsMapper.put(path, templated);
                for (Namespace p : annotation.value()) {
                    templated.put(p.ns().toUpperCase(), p.name());
                    scanPackageForElements(p.name());
                }
            }
        }

        ClassStructureBuilder body = ClassBuilder.define(IMPL_PACKAGE_NAME + "." + IMPL_TYPE_NAME)
                .publicScope()
                .implementsInterface(UiBinderStarter.class)
                .body();

        body.publicMethod(void.class, "process", Parameter.of(String.class, "clazz"), Parameter.of(com.google.gwt.dom.client.Element.class, "template")).annotatedWith(OVERRIDE_ANNOTATION)
                .append(Stmt.newObject(UiBinderUtil.class).withParameters(Stmt.loadVariable("templateToNsMapper"), Stmt.loadVariable("packageHolder"), Stmt.loadVariable("clazz"), Stmt.loadVariable("template")))
                .finish();

        this.body = body;

        addMaps();
        processConstructor();


        final String cls = body.toJavaString();

        PrintWriter foozWriter = context.tryCreate(treeLogger, IMPL_PACKAGE_NAME, IMPL_TYPE_NAME);
        if (foozWriter != null) {
            foozWriter.println(cls);
            context.commit(treeLogger, foozWriter);
        }

        return IMPL_PACKAGE_NAME + "." + IMPL_TYPE_NAME;
    }

    private void processConstructor() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        ConstructorBlockBuilder constructor = body.packageConstructor();

        templateToNsMapper.forEach((template, v) -> {
            if (atomicBoolean.getAndSet(false)) {
                constructor.append(Stmt.declareVariable(parameterizedAs(Map.class, typeParametersOf(String.class, String.class))).named("map").initializeWith(Stmt.newObject(HashMap.class)));//.finish();
            } else {
                constructor.append(Stmt.loadVariable("map").assignValue(Stmt.newObject(HashMap.class)));//.finish();
            }

            v.forEach((ns, packageName) -> {
                constructor.append(Stmt.loadVariable("map").invoke("put", Stmt.loadLiteral(ns), Stmt.loadLiteral(packageName)));//.finish();
            });

            constructor.append(Stmt.loadVariable("templateToNsMapper").invoke("put", Stmt.loadLiteral(template), Stmt.loadVariable("map")));//.finish();

        });

        packageHolder.forEach((k, v) -> {
            if (!atomicBoolean.getAndSet(true)) {
                constructor.append(Stmt.declareVariable(parameterizedAs(Map.class, typeParametersOf(String.class, ElementPropertiesHolder.class))).named("map_").initializeWith(Stmt.newObject(HashMap.class)));//.finish();
            } else {
                constructor.append(Stmt.loadVariable("map_").assignValue(Stmt.newObject(HashMap.class)));
            }

            v.forEach((element, m) -> {
                ContextualStatementBuilder contextualStatementBuilder = Stmt.nestedCall(Stmt.newObject(ElementPropertiesHolder.class).withParameters(Stmt.loadLiteral(m.tag), Stmt.loadLiteral(m.clazz)));
                m.properties.forEach((key, value) -> {
                    contextualStatementBuilder.invoke("addPropertyToMap", Stmt.loadLiteral(key), Stmt.loadLiteral(value));
                });
                constructor.append(Stmt.loadVariable("map_").invoke("put", Stmt.loadLiteral(element), contextualStatementBuilder));//.finish();
            });

            constructor.append(Stmt.loadVariable("packageHolder").invoke("put", Stmt.loadLiteral(k), Stmt.loadVariable("map_")));//.finish();

        });
        constructor.finish();
    }

    private void scanPackageForElements(String packageName) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try {
            for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {

                if (info.getPackageName().equals(packageName)) {
                    final Class<?> clazz = info.load();
                    if (clazz.isAnnotationPresent(org.jboss.errai.common.client.api.annotations.Element.class) && clazz.isAnnotationPresent(JsType.class)) {
                        if (!packageHolder.containsKey(packageName)) {
                            packageHolder.put(packageName, new HashMap<>());
                        }

                        Map<String, ElementPropertiesHolder> map = packageHolder.get(packageName);
                        map.put(clazz.getSimpleName().toUpperCase(), processElementProperties(clazz));
                    }
                }
            }
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }

    private ElementPropertiesHolder processElementProperties(Class<?> clazz) {

        String tag = arrayToString(clazz.getAnnotation(org.jboss.errai.common.client.api.annotations.Element.class).value());
        String clazzNames = "";
        Map<String, String> properties = new HashMap<>();

        if (clazz.isAnnotationPresent(ClassNames.class)) {
            clazzNames = arrayToString(clazz.getAnnotation(org.jboss.errai.common.client.api.annotations.ClassNames.class).value());
        }

        if (clazz.isAnnotationPresent(Property.class)) {
            properties.put(clazz.getAnnotation(Property.class).name(), clazz.getAnnotation(Property.class).value());
        }

        if (clazz.isAnnotationPresent(Properties.class)) {
            for (Property property : clazz.getAnnotation(Properties.class).value()) {
                properties.put(property.name(), property.value());
            }
        }

        return new ElementPropertiesHolder(tag, clazzNames, properties);
    }

    private String arrayToString(String[] value) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String s : value) {
            joiner.add(s);
        }
        return joiner.toString();
    }

    private void addMaps() {
        body.publicField("templateToNsMapper", parameterizedAs(Map.class, typeParametersOf(String.class, parameterizedAs(Map.class, typeParametersOf(String.class, String.class))))).initializesWith(newInstanceOf(parameterizedAs(HashMap.class, typeParametersOf(String.class, parameterizedAs(Map.class, typeParametersOf(String.class, String.class)))))).finish();
        body.publicField("packageHolder", parameterizedAs(Map.class, typeParametersOf(String.class, parameterizedAs(Map.class, typeParametersOf(String.class, ElementPropertiesHolder.class))))).initializesWith(newInstanceOf(parameterizedAs(HashMap.class, typeParametersOf(String.class, parameterizedAs(Map.class, typeParametersOf(String.class, ElementPropertiesHolder.class)))))).finish();
    }


    private final static Override OVERRIDE_ANNOTATION = new Override() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Override.class;
        }
    };

}
