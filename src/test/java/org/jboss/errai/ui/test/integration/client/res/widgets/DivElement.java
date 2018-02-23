package org.jboss.errai.ui.test.integration.client.res.widgets;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.jboss.errai.common.client.api.annotations.ClassNames;
import org.jboss.errai.common.client.api.annotations.Element;
import org.jboss.errai.common.client.api.annotations.Properties;
import org.jboss.errai.common.client.api.annotations.Property;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 2/21/18.
 */

@Element("div")
@ClassNames({"my_div_class"})
@Property(name = "style", value = "background-color: maroon")
@Properties({
        @Property(name = "placeholder", value = "fooblie")
})
@JsType(isNative = true, name = "HTMLDivElement", namespace = JsPackage.GLOBAL)
public interface DivElement extends org.jboss.errai.ui.test.integration.client.res.Element{


}
