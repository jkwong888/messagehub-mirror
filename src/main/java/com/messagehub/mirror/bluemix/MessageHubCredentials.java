/**
 * Copyright 2015-2016 IBM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corp. 2015-2016
 */
package com.messagehub.mirror.bluemix;

import java.io.IOException;
import java.util.Base64;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown=true)
public class MessageHubCredentials {

	private static final Logger logger = Logger.getLogger(MessageHubCredentials.class);
    private String apiKey, kafkaRestUrl, user, password;
    private String[] kafkaBrokersSasl;
    
    
    public static MessageHubCredentials getInstance(String jsonBinding) throws JsonParseException, JsonMappingException, IOException {
    	final ObjectMapper objectMapper = new ObjectMapper();
    	final String decodedJson = jsonBinding.toString();
    	
    	return objectMapper.readValue(decodedJson, MessageHubCredentials.class);
    }

    @JsonProperty("api_key")
    public String getApiKey() {
        return apiKey;
    }

    @JsonProperty("api_key")
    public void setLabel(String apiKey) {
        this.apiKey = apiKey;
    }
    
    @JsonProperty("kafka_rest_url")
    public String getKafkaRestUrl() {
        return kafkaRestUrl;
    }

    @JsonProperty("kafka_rest_url")
    public void setKafkaRestUrl(String kafkaRestUrl) {
        this.kafkaRestUrl = kafkaRestUrl;
    }
    
    @JsonProperty
    public String getUser() {
        return user;
    }

    @JsonProperty
    public void setUser(String user) {
        this.user = user;
    }
    
    @JsonProperty
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }
    
    @JsonProperty("kafka_brokers_sasl")
    public String[] getKafkaBrokersSasl() {
        return kafkaBrokersSasl;
    }

    @JsonProperty("kafka_brokers_sasl")
    public void setKafkaBrokersSasl(String[] kafkaBrokersSasl) {
        this.kafkaBrokersSasl = kafkaBrokersSasl;
    }
}
