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
package io.datarouter.nodewatch.web;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.nodewatch.config.DatarouterNodewatchFiles;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.storage.alertthreshold.DatarouterTableSizeAlertThresholdDao;
import io.datarouter.nodewatch.storage.alertthreshold.TableSizeAlertThreshold;
import io.datarouter.nodewatch.storage.alertthreshold.TableSizeAlertThresholdKey;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;

public class TableSizeAlertThresholdHandler extends BaseHandler{

	public static final String PARAM_clientName = "clientName";
	public static final String PARAM_tableName = "tableName";
	public static final String PARAM_threshold = "threshold";

	@Inject
	private DatarouterTableSizeAlertThresholdDao tableSizeAlertThresholdDao;
	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private DatarouterNodewatchFiles files;

	@Handler(defaultHandler = true)
	public Mav displayThreshold(){
		Mav mav = new Mav(files.jsp.datarouter.nodewatch.thresholdSettingsJsp);
		List<TableSizeAlertThreshold> thresholdSettings = new ArrayList<>();
		for(PhysicalNode<?,?,?> node : datarouter.getWritableNodes()){
			String clientName = node.getClientId().getName();
			TableSizeAlertThresholdKey key = new TableSizeAlertThresholdKey(clientName, node.getFieldInfo()
					.getTableName());
			TableSizeAlertThreshold row = tableSizeAlertThresholdDao.get(key);
			if(row == null){
				row = new TableSizeAlertThreshold(key, 0L);
			}
			thresholdSettings.add(row);
		}
		mav.put("thresholdSettings", thresholdSettings);
		mav.put("thresholdPath", paths.datarouter.nodewatch.threshold.toSlashedString());
		return mav;
	}

	@Handler
	public Mav updateThreshold(String tableName, String clientName, Long threshold){
		TableSizeAlertThreshold tableSizeAlertThreshold = new TableSizeAlertThreshold(clientName, tableName, threshold);
		tableSizeAlertThresholdDao.put(tableSizeAlertThreshold);
		return getRedirectMav();
	}

	@Handler
	public Mav saveThresholds(){
		String[] daos = request.getParameterValues(PARAM_clientName);
		String[] nodes = request.getParameterValues(PARAM_tableName);
		String[] thresholds = request.getParameterValues(PARAM_threshold);
		int listSize = daos.length;
		List<TableSizeAlertThreshold> thresholdSettingList = new ArrayList<>();
		for(int i = 0; i < listSize; i++){
			TableSizeAlertThreshold tableSizeAlertThreshold = new TableSizeAlertThreshold(
					daos[i],
					nodes[i],
					Long.parseLong(thresholds[i]));
			thresholdSettingList.add(tableSizeAlertThreshold);
		}
		tableSizeAlertThresholdDao.putMulti(thresholdSettingList);
		return getRedirectMav();
	}

	private Mav getRedirectMav(){
		return new InContextRedirectMav(request, paths.datarouter.nodewatch.threshold.toSlashedString());
	}

}
