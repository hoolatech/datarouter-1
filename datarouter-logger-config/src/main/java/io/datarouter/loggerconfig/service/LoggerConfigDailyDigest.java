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
package io.datarouter.loggerconfig.service;

import static j2html.TagCreator.div;
import static j2html.TagCreator.small;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.loggerconfig.config.DatarouterLoggingConfigPaths;
import io.datarouter.loggerconfig.storage.loggerconfig.DatarouterLoggerConfigDao;
import io.datarouter.loggerconfig.storage.loggerconfig.LoggerConfig;
import io.datarouter.util.DateTool;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.html.email.J2HtmlEmailTable;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.ContainerTag;

@Singleton
public class LoggerConfigDailyDigest implements DailyDigest{

	@Inject
	private DatarouterLoggerConfigDao dao;
	@Inject
	private DailyDigestService digestService;
	@Inject
	private DatarouterLoggingConfigPaths paths;
	@Inject
	private DatarouterService datarouterService;

	@Override
	public Optional<ContainerTag> getPageContent(ZoneId zoneId){
		List<LoggerConfig> loggers = getTodaysLoggers();
		if(loggers.size() == 0){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Logger Configs", paths.datarouter.logging, getType());
		var description = small("Updated Today");
		var table = new J2HtmlTable<LoggerConfig>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Name", row -> row.getKey().getName())
				.withColumn("Level", row -> row.getLevel().getPersistentString())
				.withColumn("User", row -> row.getEmail())
				.withColumn("Updated", row -> DateTool.formatDateWithZone(row.getLastUpdated(), zoneId))
				.build(loggers);
		return Optional.of(div(header, description, table));
	}

	@Override
	public Optional<ContainerTag> getEmailContent(){
		List<LoggerConfig> loggers = getTodaysLoggers();
		if(loggers.size() == 0){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Logger Configs", paths.datarouter.logging, getType());
		var description = small("Updated Today");
		ZoneId zoneId = datarouterService.getZoneId();
		var table = new J2HtmlEmailTable<LoggerConfig>()
				.withColumn("Name", row -> row.getKey().getName())
				.withColumn("Level", row -> row.getLevel().getPersistentString())
				.withColumn("User", row -> row.getEmail())
				.withColumn("Updated", row -> DateTool.formatDateWithZone(row.getLastUpdated(), zoneId))
				.build(loggers);
		return Optional.of(div(header, description, table));
	}

	@Override
	public String getTitle(){
		return "Logger Configs";
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.SUMMARY;
	}

	private List<LoggerConfig> getTodaysLoggers(){
		return dao.scan()
				.exclude(config -> config.getLastUpdated() == null)
				.exclude(config -> config.getLastUpdated().getTime() < DateTool.atStartOfDayMs())
				.list();
	}

}
