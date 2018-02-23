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

import com.google.gwt.core.client.GWT;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ui.uibinder.client.local.api.UiBinderStarter;
import org.jboss.errai.ui.uibinder.client.local.api.annotations.UiBinder;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.Decorable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.FactoryController;
import org.jboss.errai.ui.shared.TemplateUtil;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.errai.codegen.util.Stmt.declareFinalVariable;
import static org.jboss.errai.codegen.util.Stmt.invokeStatic;
import static org.jboss.errai.codegen.util.Stmt.loadLiteral;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 2/6/18.
 */
@CodeDecorator
public class UiBinderCodeDecorator extends IOCDecoratorExtension<UiBinder> {

    private static final Logger logger = LoggerFactory.getLogger(UiBinderCodeDecorator.class);
    public UiBinderCodeDecorator(Class<UiBinder> decoratesWith) {
        super(decoratesWith);
    }

    @Override
    public void generateDecorator(Decorable decorable, FactoryController controller) {
        final MetaClass declaringClass = decorable.getDecorableDeclaringType();
        final Templated templated = decorable.get().getAnnotation(Templated.class);

        if (templated == null) {
            logger.warn("UiBinder templates must be also annotated with @Template and has corresponding template. " + declaringClass.getFullyQualifiedName() + " will be skipped.");
            return;
        }

        generateTemplatedInitialization(decorable, controller);
    }

    private void generateTemplatedInitialization(final Decorable decorable,
                                                 final FactoryController controller) {
        final List<Statement> initStmts = new ArrayList<>();

        final String parentOfRootTemplateElementVarName = "parentElementForTemplateOf" + decorable.getDecorableDeclaringType().getName();
        final Statement rootTemplateElement = invokeStatic(TemplateUtil.class, "getRootTemplateElement",
                Stmt.loadVariable(parentOfRootTemplateElementVarName));


        initStmts.add(declareFinalVariable("starter", UiBinderStarter.class, invokeStatic(GWT.class, "create", loadLiteral(UiBinderStarter.class))));
        initStmts.add(Stmt.loadVariable("starter").invoke("process", loadLiteral(decorable.getDecorableDeclaringType().getCanonicalName()), rootTemplateElement));

        controller.addInitializationStatementsToEnd(initStmts);
    }

}
