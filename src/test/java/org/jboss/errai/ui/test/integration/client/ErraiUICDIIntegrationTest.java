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

package org.jboss.errai.ui.test.integration.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.test.integration.client.res.MyPage;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 *         Created by treblereel on 10/2/17.
 */
public class ErraiUICDIIntegrationTest extends AbstractErraiCDITest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.test.integration.Test";
  }


  public void testCreated() throws Exception {
    final MyPage bean = IOC.getBeanManager().lookupBean(MyPage.class).getInstance();
    assertNotNull("Text input was not injected.", bean);
  }

  public void testInputElementWithClassAndPropertyAnnotations() throws Exception {
    final MyPage bean = IOC.getBeanManager().lookupBean(MyPage.class).getInstance();

    assertEquals("Unable to bind InputElementWithClassAndPropertyAnnotations","<input class=\"my_class\" placeholder=\"fooblie\" type=\"text\">" , bean.getTest1().innerHTML.trim());
    assertEquals("Unable to set element properties to InputElementWithClassAndPropertyAnnotations","<input class=\"my_class any_one\" placeholder=\"fooblie\" type=\"text\" id=\"_z\">" , bean.getTest2().innerHTML.trim());
    assertEquals("Content of DivElement lost","<div class=\"my_div_class\" style=\"background-color: maroon\" placeholder=\"fooblie\"><h1>HELLO</h1></div>" , bean.getTest3().innerHTML.trim());
    assertEquals("data-field element is not found in uibindable element","<div class=\"my_div_class\" style=\"background-color: maroon\" placeholder=\"fooblie\"><h1>HELLO</h1><div data-field=\"test5\"></div></div>" , bean.getTest4().innerHTML.trim());
    assertEquals("Unable to bind two Elements with the same name from diff packages","<div class=\"my_div_class\" style=\"background-color: maroon\" placeholder=\"fooblie\"></div>\n" +
            "        <div class=\"my_extra_div_class\" style=\"background-color: black\" placeholder=\"fooblie\"></div>" , bean.getTest6().innerHTML.trim());



  }

}
