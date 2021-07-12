<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户签约</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	  <%@ include file="/WEB-INF/views/include/WdateLimitPicker.jsp" %>
	  <style type="text/css">
		  .table thead th, .table tbody td {
			  text-align: center;
			  vertical-align: middle;
			  BackColor: Transparent;
		  }

	  </style>
	  <script type="text/javascript">

		  $(document).ready(function() {
			  oneYearDatePicker('beginDate','endDate',false);
			  $("#btnSubmit").click(function() {
				  top.$.jBox.tip('请稍候...', 'loading');
				  $("#searchForm").attr("action","${ctx}/rpt/provider/customerSign/customerSignRpt");
				  $("#searchForm").submit();
			  });

			  $("#btnExport").click(function () {
				  top.$.jBox.tip('请稍候...', 'loading');
				  $("#btnExport").prop("disabled", true);
				  $.ajax({
					  type: "POST",
					  url: "${ctx}/rpt/provider/customerSign/checkExportTask?"+ (new Date()).getTime(),
					  data:$(searchForm).serialize(),
					  success: function (data) {
						  if(ajaxLogout(data)){
							  return false;
						  }
						  if(data && data.success == true){
							  top.$.jBox.closeTip();
							  top.$.jBox.confirm("确认要导出数据吗？", "系统提示", function (v, h, f) {
								  if (v == "ok") {
									  top.$.jBox.tip('请稍候...', 'loading');
									  $.ajax({
										  type: "POST",
										  url: "${ctx}/rpt/provider/customerSign/export?"+ (new Date()).getTime(),
										  data:$(searchForm).serialize(),
										  success: function (data) {
											  if(ajaxLogout(data)){
												  return false;
											  }
											  if(data && data.success == true){
												  top.$.jBox.closeTip();
												  top.$.jBox.tip(data.message, "success");
												  $('#btnExport').removeAttr('disabled');
												  return false;
											  }
											  else if( data && data.message){
												  top.$.jBox.error(data.message,"导出错误");
											  }
											  else{
												  top.$.jBox.error("导出错误","错误提示");
											  }
											  $('#btnExport').removeAttr('disabled');
											  top.$.jBox.closeTip();
											  return false;
										  },
										  error: function (e) {
											  $('#btnExport').removeAttr('disabled');
											  ajaxLogout(e.responseText,null,"导出错误，请重试!");
											  top.$.jBox.closeTip();
										  }
									  });
								  }
							  }, {buttonsFocus: 1});
							  $('#btnExport').removeAttr('disabled');
							  top.$.jBox.closeTip();
							  return false;
						  }
						  else if( data && data.message){
							  top.$.jBox.error(data.message,"导出错误");
						  }
						  else{
							  top.$.jBox.error("导出错误","错误提示");
						  }
						  $('#btnExport').removeAttr('disabled');
						  top.$.jBox.closeTip();
						  return false;
					  },
					  error: function (e) {
						  $('#btnExport').removeAttr('disabled');
						  ajaxLogout(e.responseText,null,"导出错误，请重试!");
						  top.$.jBox.closeTip();
					  }
				  });
				  top.$('.jbox-body .jbox-icon').css('top', '55px');
			  });
		  });
	  </script>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">客户签约</a></li>
	</ul>
	<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/customerSign/customerSignRpt" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
		<label>店铺ID：</label>
		<input id="mallId"  maxlength="50" class="input-small" type="text" name="mallId" value="${rptSearchCondition.mallId}" style="width: 160px"/>
		&nbsp&nbsp
		<label>店铺名称：</label>
		<input id="mallName"  maxlength="50" class="input-small" type="text" name="mallName" value="${rptSearchCondition.mallName}" style="width: 160px"/>
		&nbsp&nbsp
		<label>联系电话：</label>
		<input id="mobile"  maxlength="20" class="input-small" type="text" name="mobile" value="${rptSearchCondition.mobile}" style="width: 160px"/>
		&nbsp&nbsp
		<label>状&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp态：</label>
		<select id="status" name="status" class="input-small" style="width:125px;">
			<option value="" <c:out value="${(empty rptSearchCondition.status)?'selected=selected':''}" />>所有</option>
			<c:forEach items="${customerSignEnumList}" var="dict">
				<option value="${dict.value}" <c:out
						value="${(rptSearchCondition.status eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
			</c:forEach>
		</select>
		&nbsp&nbsp
		<label>申请时间：</label>
<%--		<input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:120px;margin-left:4px" maxlength="20" class="input-small Wdate"--%>
<%--			   value="<fmt:formatDate value='${rptSearchCondition.beginDate}' pattern='yyyy-MM-dd' type='date'/>" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>--%>
<%--		<label>~</label>--%>
<%--		<input id="endDate" name="endDate" type="text" readonly="readonly" style="width:125px" maxlength="20" class="input-small Wdate"--%>
<%--			   value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>--%>
		<input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:95px;margin-left:4px" maxlength="20" class="input-small Wdate"
		value="${fns:formatDate(rptSearchCondition.beginDate,'yyyy-MM-dd')}" />
		<label>~</label><input id="endDate" name="endDate" type="text" readonly="readonly" style="width:95px" maxlength="20" class="input-small Wdate"
		value="${fns:formatDate(rptSearchCondition.endDate,'yyyy-MM-dd')}" />
		&nbsp&nbsp
		<shiro:hasPermission name="rpt:customerSignRpt:view"><input id="btnSubmit" class="btn btn-primary" type="submit" value="查询" /></shiro:hasPermission>
		&nbsp;&nbsp;
		<shiro:hasPermission name="rpt:customerSignRpt:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
	</form:form>

	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="100">数据源</th>
				<th width="100">签约单号</th>
				<th width="60">状态</th>
				<th width="100">店铺ID</th>
				<th width="200">店铺名称</th>
				<th width="100">服务类型</th>
				<th width="200">服务名称</th>
				<th width="150">申请时间</th>
				<th width="100">联系人</th>
				<th width="100">联系电话</th>
				<th width="100">座机</th>
				<th>备注</th>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${fns:getDictLabelFromMS(entity.dataSource,'order_data_source','Unknow')}</td>
				<td>${entity.signOrderSn}</td>
				<td>${entity.attributes}</td>
				<td>${entity.mallId}</td>
				<td>${entity.mallName}</td>

                <td>${entity.servType}</td>
				<td>${entity.servName}</td>
				<td><fmt:formatDate value="${entity.applyDate}" pattern="yyyy-MM-dd HH:mm:ss "/></td>
				<td>${entity.contactName}</td>
				<td>${entity.mobile}</td>
				<td>${entity.telephone}</td>
				<td>
					<c:choose>
						<c:when test="${fn:length(entity.remarks)>40}">
							<a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.remarks}">${fns:abbr(entity.remarks,80)}</a>
						</c:when>
						<c:otherwise>
							${entity.remarks}
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
	<script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
        });
	</script>
  </body>
</html>
