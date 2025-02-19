/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
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
package io.datarouter.aws.sqs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;

import io.datarouter.storage.client.ClientId;
import io.datarouter.web.config.AwsSupport;

@Singleton
public class AmazonSqsHolder{

	private final Map<ClientId,AmazonSQS> amazonSqsByClient = new ConcurrentHashMap<>();

	@Inject
	private SqsOptions sqsOptions;
	@Inject
	private AwsSupport awsSupport;

	public void registerClient(ClientId clientId){
		if(amazonSqsByClient.containsKey(clientId)){
			throw new RuntimeException(clientId + " already registered an sqs client");
		}
		var conf = new ClientConfiguration()
				.withMaxConnections(200);
		var credentials = new BasicAWSCredentials(sqsOptions.getAccessKey(clientId.getName()),
				sqsOptions.getSecretKey(clientId.getName()));
		var credentialsProvider = new AWSStaticCredentialsProvider(credentials);
		AmazonSQS amazonSqs = AmazonSQSClient.builder()
				.withClientConfiguration(conf)
				.withCredentials(credentialsProvider)
				.withRegion(sqsOptions.getRegion(clientId.getName()))
				.build();
		awsSupport.registerConnectionManager("sqs " + clientId.getName(), amazonSqs);
		amazonSqsByClient.put(clientId, amazonSqs);
	}

	public AmazonSQS get(ClientId clientId){
		return amazonSqsByClient.get(clientId);
	}

}
