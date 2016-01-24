/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.consol.citrus.http.validation;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.context.DefaultValidationContext;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class FormUrlEncodedMessageValidatorTest {

    /** Class under test */
    private FormUrlEncodedMessageValidator validator = new FormUrlEncodedMessageValidator();

    private String expectedFormData = "<form-data xmlns=\"http://www.citrusframework.org/schema/http/message\">\n" +
                "<content-type>application/x-www-form-urlencoded</content-type>\n" +
                "<action>/form-test</action>\n" +
                "<controls>\n" +
                    "<control name=\"password\">\n" +
                        "<value>s!cr!t</value>\n" +
                    "</control>\n" +
                    "<control name=\"username\">\n" +
                        "<value>test</value>\n" +
                    "</control>\n" +
                "</controls>\n" +
            "</form-data>";

    @Test
    public void testValidateMessagePayload() throws Exception {
        DefaultValidationContext validationContext = new DefaultValidationContext();
        Message controlMessage = new DefaultMessage(expectedFormData);

        Message receivedMessage = new DefaultMessage("password=s%21cr%21t&username=test")
                                        .setHeader("Content-Type", "application/x-www-form-urlencoded")
                                        .setHeader(HttpMessageHeaders.HTTP_REQUEST_URI, "/form-test");

        validator.validateMessage(receivedMessage, controlMessage, new TestContext(), validationContext);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidationError() throws Exception {
        DefaultValidationContext validationContext = new DefaultValidationContext();
        Message controlMessage = new DefaultMessage(expectedFormData);

        Message receivedMessage = new DefaultMessage("password=s%21cr%21t&username=other")
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader(HttpMessageHeaders.HTTP_REQUEST_URI, "/form-test");

        validator.validateMessage(receivedMessage, controlMessage, new TestContext(), validationContext);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidationErrorMissingFormAction() throws Exception {
        DefaultValidationContext validationContext = new DefaultValidationContext();
        Message controlMessage = new DefaultMessage(expectedFormData);

        Message receivedMessage = new DefaultMessage("password=s%21cr%21t&username=test");

        validator.validateMessage(receivedMessage, controlMessage, new TestContext(), validationContext);
    }
}