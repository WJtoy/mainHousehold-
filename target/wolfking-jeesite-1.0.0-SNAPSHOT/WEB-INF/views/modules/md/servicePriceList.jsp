<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>安维价格管理</title>
	<meta name="decorator" content="default"/>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<script src="${ctxStatic}/js/fixtable.js" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		$(document).ready(function() {
			var w = $(window).width();
			FixTable("treeTable", 2, w, "100%");//460

			var pagestyle = function() {
                var width = $(window).width() -0;
                FixTable("treeTable", 2, width, "100%");
                $("#treeTable_tableLayout").css("width",width);
			}
			//注册加载事件
//			$("#iframe",window).load(pagestyle);
			//注册窗体改变大小事件
			$(window).resize(pagestyle);
	        $("th").css({"text-align":"center","vertical-align":"middle"});
	        $("td").css({"vertical-align":"middle"});
		});

		// 查询
		var clicktag = 0;
		$(document).on("click", "#btnSubmit", function () {
            layerLoading('正在查询，请稍等...',true);

            var spid = $("#servicePointId").val();
			if(spid === "" || spid === "0"){
				layerAlert("请选择网点","系统提示");
				return false;
			}
			if (clicktag == 0) {
				clicktag = 1;
				beforePage();
				setPage();
				this.form.submit();
			}

		});

		// 查看安维价格
	    function selectPrice(servicePintId, servicePointNo, serviceName, servicePointPrimaryName, contactInfo1, customizePriceFlag,useDefaultPrice,degree,serviceRemotePriceFlag,remotePriceFlag,remotePriceType){
	    	window.location="${ctx}/md/serviceprice/selectPrice?id="+servicePintId + "&primaryName="+servicePointPrimaryName+"&contactInfo="+contactInfo1+"&customizePriceFlag="+customizePriceFlag+"&useDefaultPrice="+useDefaultPrice+"&servicePointNo="+servicePointNo+"&servicePointName="+serviceName+"&degree="+degree + "&serviceRemotePriceFlag=" + serviceRemotePriceFlag + "&remotePriceFlag=" + (remotePriceFlag || '0') + "&remotePriceType=" + (remotePriceType || '0');
	    }


	</script>
	<style type="text/css">
		.col_product {width: 260px;}
		.col_command {width: 80px;}
        .table tbody td.error {background-color: #f2dede!important;}
		tr>td{text-align: center;}
	</style>
</head>
<body>
	<ul class="nav nav-tabs">
	<li class="active"><a href="javascript:;">服务网点</a></li>
	</ul>
	<sys:message content="${message}" type="loading"/>
	<form:form id="searchForm" modelAttribute="servicePoint"  action="${ctx}/md/serviceprice/list" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
		<div style="margin-bottom:5px;">
			<label>网点编号 ：</label>
			<form:input path="servicePointNo" htmlEscape="false" maxlength="20"	class="input-small" cssStyle="width: 200px;"/>
			<%--<input value="${servicePoint.servicePointNo}" name="servicePointNo" maxlength="20" class="input-mini digits" cssStyle="width: 100px;"/>--%>
			&nbsp;<label>网点名称 ：</label>
			<form:input path="name" maxlength="20" class="input-mini digits" cssStyle="width: 200px;"/>
			<%--&nbsp;<label>主账号 ：</label>--%>
			<%--<form:input path="primary.name" maxlength="20" class="input-mini digits" cssStyle="width: 200px;"/>--%>
			&nbsp;<label>网点电话 ：</label>
			<form:input path="contactInfo1" maxlength="20" class="input-mini digits" cssStyle="width: 200px;"/>
			&nbsp;<label>价格属性 ：</label>
			<form:select path="customizePriceFlag" style="width:200px;">
				<c:choose>
					<c:when test="${getRequest != null && getRequest eq true}">
						<option value="" selected="selected">所有</option>
						<option value="0">标准价</option>
						<option value="1">自定义</option>
					</c:when>
					<c:otherwise>
						<option value="" selected="selected">所有</option>
						<option value="0" <c:out value="${(servicePoint.customizePriceFlag eq '0')?'selected=selected':''}" />>标准价</option>
						<option value="1" <c:out value="${(servicePoint.customizePriceFlag eq '1')?'selected=selected':''}" />>自定义</option>
					</c:otherwise>
				</c:choose>
			</form:select>
			&nbsp;<label>网点分类 ：</label>
			<form:select path="degree" class="input-small" style="width:200px;">
				<form:option value="0" label="所有" />
				<form:options items="${fns:getDictListFromMS('degreeType')}" itemLabel="label" itemValue="value" htmlEscape="false" />
			</form:select>
			<input id="btnSubmit" class="btn btn-primary"  type="submit" onclick="return setPage();" value="查询" style="margin-left: 30px;"/>
		</div>
	</form:form>


	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
		<tr>
			<th width="45">序号</th>
			<th width="180">网点编号</th>
			<th width="180">网点名称</th>
			<th width="120">网点电话</th>
			<th width="120">网点分类</th>
			<th width="80">服务价格属性</th>
			<th width="100">价格轮次</th>
			<th width="80">偏远价格属性</th>
		</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0" />

		<c:forEach items="${page.list}" var="servicepoint">
			<c:set var="i" value="${i+1}" />
			<tr id="info">
				<td>${i+(page.pageNo-1)*page.pageSize}</td>
				<td>${servicepoint.servicePointNo}</td>
				<td>${servicepoint.name}</td>
				<td>${servicepoint.contactInfo1}</td>
				<td>${fns:getDictLabelFromMS(servicepoint.degree,'degreeType','')}</td>
				<td><label onclick="selectPrice('${servicepoint.id}','${servicepoint.servicePointNo}','${servicepoint.name}', '${servicepoint.primary.name}', '${servicepoint.contactInfo1}', '${servicepoint.customizePriceFlag}', '${servicepoint.useDefaultPrice}','${servicepoint.degree}',0,'${servicepoint.remotePriceFlag}','${servicepoint.remotePriceType}')" style="color: #0096DA;">${servicepoint.customizePriceFlag == 0 ? '标准价' : "自定义"}</label></td>
				<td>${fns:getDictLabelFromMS(servicepoint.useDefaultPrice,'PriceType','')}</td>
				<c:choose>
					<c:when test="${servicepoint.remotePriceEnabledFlag == 0}">
							<td>无</td>
					</c:when>
					<c:otherwise>
						<td><label onclick="selectPrice('${servicepoint.id}','${servicepoint.servicePointNo}','${servicepoint.name}', '${servicepoint.primary.name}', '${servicepoint.contactInfo1}', '${servicepoint.customizePriceFlag}', '${servicepoint.useDefaultPrice}','${servicepoint.degree}',1,'${servicepoint.remotePriceFlag}','${servicepoint.remotePriceType}')" style="color: #0096DA;">${servicepoint.remotePriceFlag == 0 ? '标准价' : "自定义"}</label></td>
					</c:otherwise>
				</c:choose>

			</tr>
			<c:set var="index" value="${index+1}" />
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>

<script>
    $(document).ready(function() {
        $("th").css({"text-align":"center","vertical-align":"middle"});
        $("td").css({"text-align":"center","vertical-align":"middle"});
    });
</script>
</html>
