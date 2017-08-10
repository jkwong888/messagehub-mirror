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
package com.messagehub.mirror;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.messagehub.mirror.bluemix.MessageHubCredentials;
import com.messagehub.mirror.rest.RESTAdmin;

/**
 * Console-based sample interacting with Message Hub, authenticating with SASL/PLAIN over an SSL connection.
 *
 * @author IBM
 */
public class MessageHubMirror {
    private static final String APP_NAME = "messagehub-mirror";
    private static final String TOPIC_NAME = "orders";
    private static final String ENV_SRC_MESSAGE_HUB = "SRC_MESSAGE_HUB";
    private static final String ENV_DST_MESSAGE_HUB = "DST_MESSAGE_HUB";
    private static final Logger logger = Logger.getLogger(MessageHubMirror.class);

    private static Thread consumerThread = null;
    private static ConsumerRunnable consumerRunnable = null;

    //add shutdown hooks (intercept CTRL-C etc.)  
    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { 
                logger.log(Level.WARN, "Shutdown received.");
                shutdown();
            }
        });
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logger.log(Level.ERROR, "Uncaught Exception on " + t.getName() + " : " + e, e);
                shutdown();
            }
        });
    }
    
    private static Properties createKafkaProperties(MessageHubCredentials creds) {
    	final Properties properties = new Properties();
    	
		properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		
		properties.put("client.id", "messagehub-mirror");
		properties.put("group.id", "messagehub-mirror-group");
		properties.put("security.protocol", "SASL_SSL");
		properties.put("sasl.mechanism", "PLAIN");
		properties.put("ssl.protocol", "TLSv1.2");
		properties.put("ssl.enabled.protocols", "TLSv1.2");
		properties.put("ssl.endpoint.identification.algorithm", "HTTPS");
		
//auto.offset.reset=latest

    	properties.put("bootstrap.servers", StringUtils.join(creds.getKafkaBrokersSasl(), ",") );
		properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + creds.getUser() + "\" password=\"" + creds.getPassword() + "\";");
    	
    	return properties;
    	
    }
    
    public static void main(String args[])  {
        try {
            final Properties srcClientProperties;
            final Properties dstClientProperties;
            
            final String srcEnv = System.getenv(MessageHubMirror.ENV_SRC_MESSAGE_HUB);
            final String dstEnv = System.getenv(MessageHubMirror.ENV_DST_MESSAGE_HUB);

			
			if (srcEnv == null) {
				throw new IllegalArgumentException("Environment variable not found: " + MessageHubMirror.ENV_SRC_MESSAGE_HUB);
			}
			
			if (dstEnv == null) {
				throw new IllegalArgumentException("Environment variable not found: " + MessageHubMirror.ENV_DST_MESSAGE_HUB);
			}
			
			MessageHubCredentials srcHubCreds = MessageHubCredentials.getInstance(srcEnv);
			logger.log(Level.INFO, "Loading source MessageHub from environment in: " + ENV_SRC_MESSAGE_HUB);
			srcClientProperties = createKafkaProperties(srcHubCreds);
			
			MessageHubCredentials dstHubCreds = MessageHubCredentials.getInstance(dstEnv);
			logger.log(Level.INFO, "Loading destination MessageHub from environment in: " + ENV_DST_MESSAGE_HUB);
			dstClientProperties = createKafkaProperties(dstHubCreds);

		    //Using Message Hub Admin REST API to create and list topics
            //If the topic already exists, creation will be a no-op
            try {
                logger.log(Level.INFO, "Creating the topic " + TOPIC_NAME + " on source hub ...");
                String restResponse = RESTAdmin.createTopic(srcHubCreds.getKafkaRestUrl(), srcHubCreds.getApiKey(), TOPIC_NAME);
                logger.log(Level.INFO, "Admin REST response :" + restResponse);

                String topics = RESTAdmin.listTopics(srcHubCreds.getKafkaRestUrl(), srcHubCreds.getApiKey());
                logger.log(Level.INFO, "Admin REST Listing Topics on src hub: " + topics);
            } catch (Exception e) {
                logger.log(Level.WARN, "Error occurred accessing the Admin REST API on source hub " + e.getLocalizedMessage(), e);
                //The application will carry on regardless of Admin REST errors, as the topic may already exist
            }
             
            try {
                logger.log(Level.INFO, "Creating the topic " + TOPIC_NAME + " on dest hub ...");
                String restResponse = RESTAdmin.createTopic(dstHubCreds.getKafkaRestUrl(), dstHubCreds.getApiKey(), TOPIC_NAME);
                logger.log(Level.INFO, "Admin REST response :" + restResponse);

                String topics = RESTAdmin.listTopics(dstHubCreds.getKafkaRestUrl(), dstHubCreds.getApiKey());
                logger.log(Level.INFO, "Admin REST Listing Topics on dst hub: " + topics);
            } catch (Exception e) {
                logger.log(Level.WARN, "Error occurred accessing the Admin REST API on dest hub " + e.getLocalizedMessage(), e);
                //The application will carry on regardless of Admin REST errors, as the topic may already exist
            }

            //create the Kafka clients
			final Properties consumerProperties = getClientConfiguration(srcClientProperties, "consumer.properties");
			consumerRunnable = new ConsumerRunnable(consumerProperties, TOPIC_NAME);
			consumerThread = new Thread(consumerRunnable, "Consumer Thread");
			consumerThread.start();

			final Properties producerProperties = getClientConfiguration(dstClientProperties, "producer.properties");
			final Producer prd = Producer.getInstance();
			prd.init(producerProperties, TOPIC_NAME);
            
            logger.log(Level.INFO, "MessageHubConsoleSample will run until interrupted.");
        } catch (Exception e) {
            logger.log(Level.ERROR, "Exception occurred, application will terminate", e);
            System.exit(-1);
        }
    }

    /*
     * convenience method for cleanup on shutdown
     */
    private static void shutdown() {
    	final Producer producer = Producer.getInstance();
		producer.shutdown();
		
        if (consumerRunnable != null)
            consumerRunnable.shutdown();
        if (consumerThread != null)
            consumerThread.interrupt();
    }
    
    /*
     * Retrieve client configuration information, using a properties file, for
     * connecting to Message Hub Kafka.
     */
    static final Properties getClientConfiguration(Properties commonProps, String fileName) {
    	// TODO: put as a configmap in Kubernetes outside of jar
        final Properties result = new Properties();
        
        result.putAll(commonProps);

        try (final InputStream propsStream = MessageHubMirror.class.getResourceAsStream("/" + fileName)){
            result.load(propsStream);
        } catch (IOException e) {
            logger.log(Level.ERROR, "Could not load properties from file");
            return result;
        }

        return result;
    }
}
