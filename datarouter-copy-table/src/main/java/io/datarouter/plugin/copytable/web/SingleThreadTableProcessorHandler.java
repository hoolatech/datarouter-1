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
package io.datarouter.plugin.copytable.web;

import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.p;

import java.util.List;

import javax.inject.Inject;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessor;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorRegistry;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorService;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorService.TableProcessorSpanResult;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;

public class SingleThreadTableProcessorHandler extends BaseHandler{

	private static final String
			P_sourceNodeName = "sourceNodeName",
			P_lastKeyString = "lastKeyString",
			P_processorName = "processorName",
			P_numThreads = "numThreads",
			P_putBatchSize = "putBatchSize",
			P_toEmail = "toEmail",
			P_submitAction = "submitAction";

	private static final int DEFAULT_NUM_THREADS = 4;
	private static final int DEFAULT_BATCH_SIZE = 1_000;

	@Inject
	private TableProcessorService service;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private TableProcessorRegistry processorRegistry;
	@Inject
	private DatarouterCopyTablePaths paths;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private CopyTableChangelogRecorderService changelogRecorderService;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	@Inject
	private DatarouterInjector injector;
	@Inject
	private TableSamplerService tableSamplerService;

	@Handler(defaultHandler = true)
	private <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	Mav defaultHandler(
			@Param(P_sourceNodeName) OptionalString sourceName,
			@Param(P_processorName) OptionalString processorName,
			@Param(P_lastKeyString) OptionalString lastKeyString,
			@Param(P_toEmail) OptionalString toEmail,
			@Param(P_numThreads) OptionalString numThreads,
			@Param(P_putBatchSize) OptionalString putBatchSize,
			@Param(P_submitAction) OptionalString submitAction){
		String errorNumThreads = null;
		String errorPutBatchSize = null;

		if(submitAction.isPresent()){
			try{
				if(numThreads.map(StringTool::nullIfEmpty).isPresent()){
					Integer.valueOf(numThreads.get());
				}
			}catch(Exception e){
				errorNumThreads = "Please specify an integer";
			}
			try{
				if(putBatchSize.map(StringTool::nullIfEmpty).isPresent()){
					Integer.valueOf(putBatchSize.get());
				}
			}catch(Exception e){
				errorPutBatchSize = "Please specify an integer";
			}
		}

		List<String> possibleNodes = tableSamplerService.scanAllSortedMapStorageNodes()
				.map(node -> node.getClientId().getName() + "." + node.getFieldInfo().getTableName())
				.append("")
				.sort()
				.list();
		List<String> possibleProcessors = Scanner.of(processorRegistry.getAll())
				.map(Class::getSimpleName)
				.sort()
				.list();
		var form = new HtmlForm()
				.withMethod("post");
		form.addSelectField()
				.withDisplay("Node Name")
				.withName(P_sourceNodeName)
				.withValues(possibleNodes);
		form.addSelectField()
				.withDisplay("Processor Name")
				.withName(P_processorName)
				.withValues(possibleProcessors);
		form.addTextField()
				.withDisplay("Last Key String")
				//add validation
				.withName(P_lastKeyString)
				.withValue(lastKeyString.orElse(null));
		form.addTextField()
				.withDisplay("Num Threads")
				.withError(errorNumThreads)
				.withName(P_numThreads)
				.withPlaceholder(DEFAULT_NUM_THREADS + "")
				.withValue(numThreads.orElse(null));
		form.addTextField()
				.withDisplay("Batch Size")
				.withError(errorPutBatchSize)
				.withName(P_putBatchSize)
				.withPlaceholder(DEFAULT_BATCH_SIZE + "")
				.withValue(putBatchSize.orElse(null));
		form.addTextField()
				.withDisplay("Email on Completion")
				//add validation
				.withName(P_toEmail)
				.withPlaceholder("you@email.com")
				.withValue(toEmail.orElse(null));
		form.addButton()
				.withDisplay("Copy")
				.withValue("anything");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Table Processor - Single Thread")
					.withContent(Html.makeContent(form))
					.buildMav();
		}

		int actualNumThreads = numThreads
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_NUM_THREADS);
		int actualPutBatchSize = putBatchSize
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_BATCH_SIZE);
		TableProcessor<?,?> processor = injector.getInstance(processorRegistry.find(processorName.get())
				.get());
		TableProcessorSpanResult result = service.runTableProcessor(
				sourceName.get(),
				lastKeyString.map(StringTool::nullIfEmpty).orElse(null),
				null,
				processor,
				actualNumThreads,
				actualPutBatchSize,
				1,
				1);
		if(!result.success){
			String message = String.format("The table processor was interrupted unexpectedly with %s."
					+ "  Please resume the processor with lastKey %s",
					result.exception.getMessage(),
					result.resumeFromKeyString);
			return pageFactory.message(request, message);
		}
		var header = standardDatarouterEmailHeaderService.makeStandardHeader();
		String message = String.format("Successfully processed %s records for %s - %s",
				NumberFormatter.addCommas(result.numCopied),
				sourceName.get(),
				processorName.get());
		var body = body(header, p(message));
		if(toEmail.filter(str -> !str.isEmpty()).isPresent()){
			String primaryHref = htmlEmailService.startLinkBuilder()
					.withLocalPath(paths.datarouter.copyTableSingleThread)
					.build();
			var emailBuilder = htmlEmailService.startEmailBuilder()
					.withTitle("Table Processor")
					.withTitleHref(primaryHref)
					.withContent(body)
					.fromAdmin()
					.to(toEmail.get());
			htmlEmailService.trySendJ2Html(emailBuilder);
		}
		changelogRecorderService.recordChangelogForTableProcessor(getSessionInfo(), "Single Thread", sourceName.get(),
				processorName.get());
		return pageFactory.message(request, message);
	}

	private static class Html{

		public static ContainerTag<?> makeContent(HtmlForm htmlForm){
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			return div(
					h2("Table Processor - Single Thread"),
					form,
					br())
					.withClass("container mt-3");
		}

	}

}
