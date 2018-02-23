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

package org.jboss.errai.ui.uibinder.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 2/20/18.
 */
public class ElementPropertiesHolder {

    public String tag;
    public String clazz;
    public Map<String, String> properties = new HashMap<>();

    public ElementPropertiesHolder(String tag, String clazz, Map<String, String> properties){
        this(tag, clazz);
        this.properties = properties;
    }

    public ElementPropertiesHolder(String tag, String clazz){
        this.tag = tag;
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ");
        properties.forEach((k,v) -> sj.add(k + " => " + v));
        return "properties: " + tag + " " + clazz + " " + sj.toString();
    }

    public ElementPropertiesHolder addPropertyToMap(String key, String value){
        this.properties.put(key, value);
        return this;
    }
}
