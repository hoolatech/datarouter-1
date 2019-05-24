<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Datarouter</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<style>
		td{
			white-space:nowrap;
		}
	</style>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<div class="container-fluid">
		<ol class="breadcrumb">
			<li><a href="${contextPath}/datarouter">Datarouter Home</a></li>
			<li>node: <b>${node.name}</b></li>
		</ol>
		<a href="${contextPath}/datarouter/nodes/deleteData?nodeName=${node.name}">Delete databean</a>
		<a href="${contextPath}/datarouter/nodes/getData?nodeName=${node.name}">Get databean</a>
		<h3>Browse databeans</h3>
		<form method="get" action="?">
			<div class="form-group">
				<label>Node</label>
				<input name="nodeName" value="${node.name}" type="text" class="form-control" />
			</div>
			<div class="form-group">
				<label>DatabeanType</label>
				${node.fieldInfo.sampleDatabean['class']}
			</div>
			<div class="form-group">
				<label>NodeType</label>
				${node['class'].simpleName}
			</div>
		<c:if test="${browseSortedData == true}">
			<div class="form-group">
				<label>StartAfterKey (changes each page):</label>
				<input name="startAfterKey" value="${startAfterKey}" type="text" class="form-control" />
			</div>
			<div class="form-group">
				<label>outputBatchSize:</label>
				<input name="outputBatchSize" value="${outputBatchSize}" type="text" class="form-control" />
			</div>
			<div class="form-group">
				<label>limit:</label>
				<input name="limit" value="${limit}" type="text" class="form-control" />
			</div>
			<input type="submit" class="btn btn-success"/>
		</form>
		<c:set var="accesDatabeans" value=""/>
		<c:if test="${fn:length(databeans) >= limit}">
			<c:set var="accesDatabeans" value="&startAfterKey=${nextKey}&limit=${limit}"/>
		</c:if>
		<nav> 
			<ul class="pager">
				<c:if test="${not empty startAfterKey}">
					<li>
						<a href="?nodeName=${param.nodeName}&startAfterKey=&limit=${limit}">
							Start
						</a>
					</li>
				</c:if>
				<c:if test="${not empty accesDatabeans}">
					<li>
						<a href="?nodeName=${param.nodeName}${accesDatabeans}">
							Next
						</a>
					</li>
				</c:if>
			</ul>
		</nav>
		<c:forEach items="${fields}" var="field" varStatus="loop">
			${field.key.name}<c:if test="${!loop.last}">,</c:if>
		</c:forEach>
		<table class="viewNodeDataTable data sortable table table-condensed table-bordered table-hover">
			<thead>
				<tr>
					<c:forEach items="${fields}" var="field">
						<th id="fieldAbbreviation.${field.key.name}">${abbreviatedFieldNameByFieldName[field.key.name]}</th>
					</c:forEach>
					<th style="width:26px"/>
				</tr>
			</thead>
			<tbody>
				<c:if test="${empty rowsOfFields}">
					<c:forEach items="${databeans}" var="databean" varStatus="status">
						<tr <c:if test="${status.index % 5 == 0}"> class="highlighted"</c:if>>
							<c:forEach items="${databean.fields}" var="field">
								<c:if test="${! field.key.collection}">
									<td>${field.valueString}</td>
								</c:if>
								<c:if test="${field.key.collection}">
									<td>
										<c:forEach items="${field.values}" var="v">
											${valueString}${v}
											<br />
										</c:forEach>
									</td>
								</c:if>
							</c:forEach>
						</tr>
						<c:set var="valueString" value="" />
					</c:forEach>
				</c:if>
				<c:if test="${not empty rowsOfFields}">
					<c:forEach items="${rowsOfFields}" var="rowOfFields" varStatus="status">
						<tr <c:if test="${status.index % 5 ==0}"> class="highlighted"</c:if>>
							<c:forEach items="${rowOfFields}" var="field">
								<c:if test="${! field.key.collection}">
									<td>${field.valueString}</td>
								</c:if>
								<c:if test="${field.key.collection}">
									<td>
										<c:forEach items="${field.values}" var="v">
											${valueString}${v}
											<br />
										</c:forEach>
									</td>
								</c:if>
							</c:forEach>
							<td>
								<a href="${contextPath}/datarouter/nodes/deleteData?nodeName=${node.name}${fieldKeys[status.index]}">
									<span class="glyphicon glyphicon-trash"/>
								</a>
							</td>
						</tr>
						<c:set var="valueString" value="" />
					</c:forEach>
				</c:if>
			</tbody>
		</table>
		<nav> 
			<ul class="pager">
				<c:if test="${not empty startAfterKey}">
					<li>
						<a href="?nodeName=${param.nodeName}&startAfterKey=&limit=${limit}">
							Start
						</a>
					</li>
				</c:if>
				<c:if test="${not empty accesDatabeans}">
					<li>
						<a href="?nodeName=${param.nodeName}${accesDatabeans}">
							Next
						</a>
					</li>
				</c:if>
			</ul>
		</nav>
		</c:if>
	</div>
</body>
</html>