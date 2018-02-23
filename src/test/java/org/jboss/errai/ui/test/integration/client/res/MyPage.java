package org.jboss.errai.ui.test.integration.client.res;

import com.google.gwt.user.client.ui.Composite;
import elemental2.dom.HTMLDivElement;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.errai.ui.uibinder.client.local.api.annotations.Namespace;
import org.jboss.errai.ui.uibinder.client.local.api.annotations.UiBinder;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 *         Created by treblereel on 10/2/17.
 */
@UiBinder({
        @Namespace(ns = "ns", name = "org.jboss.errai.ui.test.integration.client.res"),
        @Namespace(ns = "w", name = "org.jboss.errai.ui.test.integration.client.res.widgets")
})
@ApplicationScoped
@Templated(value = "my-page.html#contact")
public class MyPage extends Composite {

    @Inject
    Logger logger;

    @Inject
    @DataField
    HTMLDivElement contact;

    @Inject
    @DataField
    HTMLDivElement test1, test2, test3, test4, test5, test6;

    @PostConstruct
    public void init() {


    }

    public HTMLDivElement asElement() {
        return contact;
    }

    public HTMLDivElement getTest1(){
        return test1;
    }

    public HTMLDivElement getTest2(){
        return test2;
    }

    public HTMLDivElement getTest3(){
        return test3;
    }

    public HTMLDivElement getTest4(){
        return test4;
    }

    public HTMLDivElement getTest6(){
        return test6;
    }


}
