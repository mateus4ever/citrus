/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.validation.xml;

import com.consol.citrus.validation.context.ValidationContext;
import org.springframework.core.io.Resource;

import java.util.*;

/**
 * XML validation context holding validation specific information needed for XML 
 * message validation.
 * 
 * @author Christoph Deppisch
 */
public class XmlMessageValidationContext implements ValidationContext {
    /** Map holding xpath expressions to identify the ignored message elements */
    private Set<String> ignoreExpressions = new HashSet<String>();

    /** Namespace definitions resolving namespaces in XML message validation */
    private Map<String, String> namespaces = new HashMap<String, String>();
    
    /** dtdResource for DTD validation */
    private Resource dtdResource;
    
    /** Map holding control namespaces for validation */
    private Map<String, String> controlNamespaces = new HashMap<String, String>();
    
    /** Should message be validated with its schema definition */
    private boolean schemaValidation = true;
    
    /** Explicit schema repository to use for this validation */
    private String schemaRepository;
    
    /** Explicit schema instance to use for this validation */
    private String schema;

    /**
     * Get ignored message elements.
     * @return the ignoreExpressions
     */
    public Set<String> getIgnoreExpressions() {
        return ignoreExpressions;
    }

    /**
     * Set ignored message elements.
     * @param ignoreExpressions the ignoreExpressions to set
     */
    public void setIgnoreExpressions(Set<String> ignoreExpressions) {
        this.ignoreExpressions = ignoreExpressions;
    }

    /**
     * Get the namespace definitions for this validator.
     * @return the namespaceContext
     */
    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Set the namespace definitions.
     * @param namespaces the namespaceContext to set
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Get the dtd resource.
     * @return the dtdResource
     */
    public Resource getDTDResource() {
        return dtdResource;
    }

    /**
     * Set dtd resource.
     * @param dtdResource the dtdResource to set
     */
    public void setDTDResource(Resource dtdResource) {
        this.dtdResource = dtdResource;
    }

    /**
     * Get control namespace elements.
     * @return the controlNamespaces
     */
    public Map<String, String> getControlNamespaces() {
        return controlNamespaces;
    }

    /**
     * Set the control namespace elements.
     * @param controlNamespaces the controlNamespaces to set
     */
    public void setControlNamespaces(Map<String, String> controlNamespaces) {
        this.controlNamespaces = controlNamespaces;
    }

    /**
     * Is schema validation enabled.
     * @return the schemaValidation
     */
    public boolean isSchemaValidationEnabled() {
        return schemaValidation;
    }

    /**
     * Enable/disable schema validation.
     * @param schemaValidation the schemaValidation to set
     */
    public void setSchemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
    }

    /**
     * Gets the schemaRepository.
     * @return the schemaRepository the schemaRepository to get.
     */
    public String getSchemaRepository() {
        return schemaRepository;
    }

    /**
     * Sets the schemaRepository.
     * @param schemaRepository the schemaRepository to set
     */
    public void setSchemaRepository(String schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    /**
     * Gets the schema.
     * @return the schema the schema to get.
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the schema.
     * @param schema the schema to set
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

}
