<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>天猫预警</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	  <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
  </head>
  <style type="text/css">
	  .table thead th, .table tbody td {
		  text-align: center;
		  vertical-align: middle;
		  /*background: transparent;*/
	  }
  </style>
  <script type="text/javascript">
      $(document).ready(function () {
          $('a[data-toggle=tooltip]').darkTooltip();
          $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
          $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
      });

	  function monitorFeedback(orderNo,monitorId,id) {
          top.layer.open({
              type: 2,
              id:'layer_serviceMonitor',
              zIndex:19891015,
              title:'天猫预警反馈['+orderNo+']',
              content: "${ctx}/sd/order/serviceMonitor/feedbackFrom?monitorId=" +monitorId + "&id=" + id,
              area: ['830px', '380px'],
              shade: 0.3,
              maxmin: false,
              success: function(layero,index){
              },
              end:function(){
                  var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                  if(iframe != undefined){
                      var repageFlag = $("#repageFlag",iframe.document).val();
                      if(repageFlag == "true"){
                          iframe.repage();
                      }
                  }
              }
          });
      }
  </script>

  <body>
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/sd/order/kefu/processlist" title="处理中工单列表">处理中</a></li>
		<li><a href="${ctx}/sd/order/anomaly/list" title="天猫一键求助">求助</a></li>
		<li class="active"><a href="javascript:void(0);" title="天猫预警">预警</a></li>
		<li><a href="${ctx}/sd/order/kefu/pendinglist" title="需要等待的工单">停滞</a></li>
		<li><a href="${ctx}/sd/order/kefu/appointedlist" title="未预约的工单">未预约</a></li>
		<li><a href="${ctx}/sd/order/kefu/rushinglist" title="突击中的工单">突击单</a></li>
		<li><a href="${ctx}/sd/order/kefu/complainlist" title="投诉的工单">投诉</a></li>
		<li><a href="${ctx}/sd/order/kefu/finishlist" title="已完成工单列表">已完成</a></li>
		<li><a href="${ctx}/sd/order/kefu/cancellist" title="取消的工单，未接单">取消单</a></li>
		<li><a href="${ctx}/sd/order/kefu/returnlist" title="退单">退单</a></li>
		<li><a href="${ctx}/sd/order/kefu/alllist" title="所有工单">所有</a></li>
	</ul>
	<form:form id="searchForm" modelAttribute="entity" action="${ctx}/sd/order/serviceMonitor/list" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<input id="repageFlag" type="hidden" value="false">
		<label class="label-search">订单号：</label>&nbsp;
		<input type=text class="input-small" id="orderNo" name="orderNo" value="${entity.orderNo }" maxlength="20" />
		<label class="label-search">预警等级：</label>
		<form:select path="level" class="input-small" style="width:125px;">
			<form:option value="0" label="所有"/>
			<form:options items="${fns:getDictListFromMS('OrderMonitorLevel')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
		</form:select>
		<label class="label-search">状态：</label>
		<form:select path="status" class="input-small" style="width:125px;">
			<form:option value="0" label="所有"/>
			<form:option value="1" label="未反馈"/>
			<form:option value="2" label="已反馈"/>
		</form:select>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="100">订单号</th>
				<th width="70">状态</th>
				<th width="100">预警等级</th>
				<th width="200">预警内容</th>
				<th width="100">预警时间</th>
				<th width="100">反馈内容</th>
				<th width="100">反馈时间</th>
				<th width="100">客服</th>
				<shiro:hasPermission name="sd:servicemonitor:feedback"><th width="40">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="vmodel">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>
					<a href="javascript:void(0);" onclick="Order.showOrderDetail('${vmodel.orderId}','${vmodel.quarter}');">
					  <abbr title="查看订单详情">${vmodel.orderNo}</abbr>
					</a>
				</td>
				<c:choose>
					<c:when test="${vmodel.status==1}">
						<td><span class="label status_60">未反馈</span></td>
					</c:when>
					<c:otherwise>
						<td><span class="label status_50">已反馈</span></td>
					</c:otherwise>
				</c:choose>
				<td>${fns:getDictLabelFromMS(vmodel.level,'OrderMonitorLevel','OrderMonitorLevel')}</td>
				<td>
					<c:choose>
						<c:when test="${fn:length(vmodel.content)>100}">
							<a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${vmodel.content}">${fns:abbr(vmodel.content,150)}</a>
						</c:when>
						<c:otherwise>
							${vmodel.content}
						</c:otherwise>
					</c:choose>
				</td>
				<td>
					<fmt:formatDate value="${vmodel.gmtDate}" pattern="yyyy-MM-dd HH:mm"/>
				</td>
				<td>${vmodel.replyContent}</td>
				<td>
					<c:if test="${vmodel.status==2}">
						<fmt:formatDate value="${vmodel.replyDate}" pattern="yyyy-MM-dd HH:mm"/>
					</c:if>
				</td>
				<td>${vmodel.replierName}</td>
				<shiro:hasPermission name="sd:servicemonitor:feedback">
				<td>
					<c:if test="${vmodel.status==1}">
						<a class="btn btn-mini btn-primary" href="javascript:void(0);"
						   onclick="monitorFeedback('${vmodel.orderNo}','${vmodel.monitorId}','${vmodel.id}')">反馈</a>
					</c:if>
				</td>
				</shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>
