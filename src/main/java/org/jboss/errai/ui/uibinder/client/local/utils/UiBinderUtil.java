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

package org.jboss.errai.ui.uibinder.client.local.utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import elemental2.dom.Attr;
import elemental2.dom.DomGlobal;
import elemental2.dom.NamedNodeMap;
import jsinterop.base.Js;
import org.jboss.errai.ui.uibinder.shared.ElementPropertiesHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 2/21/18.
 */
public class UiBinderUtil {

    private static final Logger logger = LoggerFactory.getLogger(UiBinderUtil.class.getSimpleName());

    private Map<String, String> nsMapper;
    private Map<String, Map<String, ElementPropertiesHolder>> packageHolder;

    public UiBinderUtil(Map<String, Map<String, String>> templateToNsMapper, Map<String, Map<String, ElementPropertiesHolder>> pkgHolder, String clazz, com.google.gwt.dom.client.Element template){
        this.nsMapper = templateToNsMapper.get(clazz);
        this.packageHolder = pkgHolder;
        process(template);
    }

    private elemental2.dom.Element createElement(ElementPropertiesHolder holder) {
        final elemental2.dom.Element element = DomGlobal.document.createElement(holder.tag);
        element.className = holder.clazz;
        holder.properties.forEach((k, v) -> element.setAttribute(k, v));
        return element;
    }

    private void process(Element parent) {
        if (parent != null && parent.getTagName() != null) {
            if (parent.getTagName().contains(":")) {
                String[] couple = parent.getTagName().split(":");
                String ns = couple[0];
                String tag = couple[1];
                if (nsMapper.entrySet().stream().anyMatch(str -> str.getKey().toLowerCase().equals(ns.toLowerCase()))) {
                    getNodeChildren(parent).forEach(child -> process((Element) child));
                    String pkg = nsMapper.get(ns);

                    if(packageHolder.get(pkg).get(tag) == null){
                        throw new IllegalArgumentException("There is no such Element tagged as " + tag + " in package " + pkg + ". Check your template file or uiBinder settings. ");
                    }

                    elemental2.dom.Element elm =  createElement(packageHolder.get(pkg).get(tag));
                    copyPropertiesFromOriginalToNewElement(parent, elm);

                    getNodeChildren(parent).forEach(c -> elm.appendChild((elemental2.dom.Node) Js.cast(c)));

                    parent.getParentElement().replaceChild(Js.cast(elm), parent);
                }

            }else{
                getNodeChildren(parent).forEach(child -> process((Element) child));
            }
        }
    }

    private void copyPropertiesFromOriginalToNewElement(Element parent, elemental2.dom.Element elm) {
        NamedNodeMap<Attr> attributes = ((elemental2.dom.Element) Js.cast(parent)).attributes;
        for (int i = 0; i < attributes.length; i++) {
            Attr attr = attributes.getAt(i);
            if(elm.hasAttribute(attr.name)){ // merge attrs
                elm.setAttribute(attr.name, elm.getAttribute(attr.name) + " " +attr.value);
            }else{
                elm.setAttribute(attr.name, attr.value);
            }
        }

    }

    public List<Node> getNodeChildren(Node node) {
        return getNodeChildren(node, Node.TEXT_NODE);
    }

    public List<Node> getNodeChildren(Node node, int mode) {
        List<Node> result = new LinkedList<>();
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node child = node.getChildNodes().getItem(i);
            if (child.getNodeType() <= mode) {
                result.add(child);
            }
        }
        return result;
    }

}
