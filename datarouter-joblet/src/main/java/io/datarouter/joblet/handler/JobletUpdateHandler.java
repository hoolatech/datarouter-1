/**
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
package io.datarouter.joblet.handler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.joblet.JobletPageFactory;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.queue.JobletRequestQueueManager;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.joblet.storage.jobletrequestqueue.DatarouterJobletQueueDao;
import io.datarouter.joblet.storage.jobletrequestqueue.JobletRequestQueueKey;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.model.databean.Databean;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalString;

public class JobletUpdateHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(JobletUpdateHandler.class);

	public static final String PARAM_jobletType = "jobletType";
	public static final String PARAM_executionOrder = "executionOrder";
	public static final String PARAM_status = "status";

	@Inject
	private JobletTypeFactory jobletTypeFactory;
	@Inject
	private DatarouterJobletQueueDao jobletQueueDao;
	@Inject
	private JobletService jobletService;
	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;
	@Inject
	private JobletRequestQueueManager jobletRequestQueueManager;
	@Inject
	private JobletPageFactory pageFactory;
	@Inject
	private ChangelogRecorder changelogRecorder;

	@Handler
	private Mav deleteGroup(
			@Param(PARAM_jobletType) String typeString,
			@Param(PARAM_executionOrder) Integer executionOrder,
			@Param(PARAM_status) String status){
		JobletType<?> jobletType = jobletTypeFactory.fromPersistentString(typeString);
		JobletStatus statusString = JobletStatus.fromPersistentStringStatic(status);
		JobletRequestKey prefix = JobletRequestKey.create(jobletType, executionOrder, null, null);
		jobletRequestDao.scanWithPrefix(prefix)
				.include(jobletRequest -> statusString == jobletRequest.getStatus())
				.map(Databean::getKey)
				.batch(100)
				.forEach(jobletRequestDao::deleteMulti);
		String message = String.format("Deleted joblets with type %s, status %s, executionOrder %s", typeString, status,
				executionOrder);
		changelogRecorder.record(
				"Joblet",
				typeString + " " + status + " " + executionOrder,
				"deleteGroup",
				getSessionInfo().getRequiredSession().getUsername());
		return pageFactory.message(request, message);
	}

	@Handler
	private Mav copyJobletRequestsToQueues(@Param(PARAM_jobletType) OptionalString jobletType){
		List<JobletType<?>> jobletTypes = jobletType.isPresent()
				? Arrays.asList(jobletTypeFactory.fromPersistentString(jobletType.get()))
				: jobletTypeFactory.getAllTypes();
		long numCopied = 0;
		for(JobletType<?> type : jobletTypes){
			Scanner<JobletRequest> requestsOfType = jobletRequestDao.scanType(type, false)
					.include(jobletRequest -> JobletStatus.CREATED == jobletRequest.getStatus());
			for(List<JobletRequest> requestBatch : requestsOfType.batch(100).iterable()){
				Map<JobletRequestQueueKey,List<JobletRequest>> requestsByQueueKey = Scanner.of(requestBatch)
						.groupBy(jobletRequestQueueManager::getQueueKey);
				for(JobletRequestQueueKey queueKey : requestsByQueueKey.keySet()){
					List<JobletRequest> jobletsForQueue = requestsByQueueKey.get(queueKey);
					jobletQueueDao.getQueue(queueKey).putMulti(jobletsForQueue);
					numCopied += jobletsForQueue.size();
				}
				logger.warn("copied {}", numCopied);
			}
		}
		changelogRecorder.record(
				"Joblet",
				jobletType.orElse("all"),
				"requeue",
				getSessionInfo().getRequiredSession().getUsername());
		return pageFactory.message(request, "copied " + numCopied);
	}

	@Handler
	private Mav restart(
			@Param(PARAM_jobletType) OptionalString type,
			@Param(PARAM_status) String status){
		JobletStatus jobletStatus = JobletStatus.fromPersistentStringStatic(status);
		long numRestarted = 0;
		if(type.isPresent()){
			JobletType<?> jobletType = jobletTypeFactory.fromPersistentString(type.get());
			numRestarted = jobletService.restartJoblets(jobletType, jobletStatus);
		}else{
			for(JobletType<?> jobletType : jobletTypeFactory.getAllTypes()){
				numRestarted += jobletService.restartJoblets(jobletType, jobletStatus);
			}
		}
		changelogRecorder.record(
				"Joblet",
				type.orElse("all") + " " + status,
				"restart",
				getSessionInfo().getRequiredSession().getUsername());
		return pageFactory.message(request, "restarted " + numRestarted);
	}

	//is this used?
	@Handler
	private Mav timeoutStuckRunning(String type){
		JobletType<?> jobletType = jobletTypeFactory.fromPersistentString(type);
		Scanner<JobletRequest> requests = jobletRequestDao.scanType(jobletType, false)
				.include(request -> request.getStatus() == JobletStatus.RUNNING)
				.include(request -> request.getReservedAgoMs().isPresent())
				.include(request -> request.getReservedAgoMs().get() > Duration.ofDays(2).toMillis());
		long numTimedOut = 0;
		for(List<JobletRequest> requestBatch : requests.batch(100).iterable()){
			List<JobletRequest> toSave = new ArrayList<>();
			for(JobletRequest request : requestBatch){
				request.setStatus(JobletStatus.CREATED);
				request.setNumFailures(0);
				++numTimedOut;
			}
			jobletRequestDao.putMulti(toSave);
			logger.warn("copied {}", numTimedOut);
		}
		changelogRecorder.record(
				"Joblet",
				type,
				"timeoutStuckRunning",
				getSessionInfo().getRequiredSession().getUsername());
		return pageFactory.message(request, "timedOut " + numTimedOut);
	}

}
