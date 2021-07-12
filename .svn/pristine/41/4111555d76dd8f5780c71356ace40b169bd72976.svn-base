<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>选择安维网点</title>
	<meta name="decorator" content="default" />
		<script src="${ctxStatic}/js/fixtable.js" type="text/javascript"></script>
	<style type="text/css">
	.pagination {  margin: 10px 0;  }
	.td {  word-break: break-all;  }
	</style>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
			var data = null;
			function setData(i){
				data = items[i];
				var index = top.layer.index;
				top.$.jBox.getBox().find("button[value='ok']").trigger("click");
			}

			var items =[
						<c:forEach items="${page.list}" var="point">{id:'${point.id}',servicePointNo:'${point.servicePointNo}',name:'${point.name}', phone:'${point.contactInfo1 }',grade:${point.grade},paymentType:{label:'${point.finance.paymentType.label}',value:${point.finance.paymentType.value}}},
						</c:forEach>
					   ];
		</script>
	<script type="text/javascript">
        function plugin0()
        {
            return document.getElementById('plugin0');
        }
        plugin = plugin0;
        function addEvent(obj, name, func)
        {
            if (window.addEventListener) {
                obj.addEventListener(name, func, false);
            } else {
                obj.attachEvent("on"+name, func);
            }
        }

        function load()
        {
			addEvent(plugin(), 'OnDeviceConnect', function(number){
				alert('设备连接事件：'+number);
			});
			addEvent(plugin(), 'OnDeviceDisconnect', function(number){
				alert('设备断开事件：'+number);
			});
			addEvent(plugin(), 'OnCallOut', function(teleNo){
				alert('呼叫事件：'+teleNo);
			});
			addEvent(plugin(), 'OnCallIn', function(teleNo){
				alert('来电事件：'+teleNo);
			});
			addEvent(plugin(), 'OnHangUp', function(teleNo){
				alert('挂起事件：'+teleNo);
			});
			addEvent(plugin(), 'OnAnswer', function(teleNo){
				alert('应答事件：'+teleNo);
			});
        }
        function pluginLoaded() {
            //alert("Plugin loaded!");
        }

		//摘机
		function OffHookCtrl()
		{
             if( !plugin().OffHookCtrl() )
				alert("OffHookCtrl Fail");
		}

		//拔号
        function StartDial(teleNo, bRecord)
        {
        	teleNo="00"+teleNo;
			alert(teleNo);
             if( !plugin().StartDial(teleNo, bRecord) )
				alert("StartDial Fail");
		}

		//挂机或挂断
		function HangUpCtrl()
		{
             if( !plugin().HangUpCtrl() )
				alert("HangUpCtrl Fail");
		}
		//上传录音
		function HangUpCtrl()
		{
			alert(plugin().UploadRecord());
		}
        function testEvent()
        {
            plugin().testEvent();
        }

        function pluginValid()
        {
            if(plugin().valid){
                alert(plugin().echo("This plugin seems to be working!"));
            } else {
                alert("Plugin is not working :(");
            }
        }
    </script>
</head>
<body>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<div style="margin-left:3px;margin-right:3px;">
		<form:form id="searchForm" modelAttribute="servicePoint"  action="${ctx}/md/servicepoint/select" method="post" class="breadcrumb form-search">
			<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
			<input id="pageSize" name="pageSize" type="hidden"
				value="${page.pageSize}" />
			<div style="margin-bottom:5px;">
				<label>区域：</label>
				<c:choose>
					<c:when test="${servicePoint.area != null && servicePoint.area.id>0 }">
						<form:hidden path="area.id" />
						<form:input path="area.name" Style="width:140px;" readonly="true" />
					</c:when>
					<c:otherwise>
						<sys:treeselect id="area" name="area.id" value="${servicePoint.area.id}"
							labelName="areaName" labelValue="${servicePoint.area.name}" title="区域"
							url="/sys/area/treeData" nodesLevel="2" nameLevel="3"
							cssStyle="width:140px;" cssClass="required" />
					</c:otherwise>
				</c:choose>
				&nbsp;
				<label>
					<%--<input type="radio" id="byCode" name="searchType" value="1" ${servicePoint.searchType ==1?'checked':''} title="按网点编号查询" />编号--%>
					<%--<input type="radio" id="byName" name="searchType" value="0" ${servicePoint.searchType ==0?'checked':''} title="按网点名称查询" />名称--%>
					<input type="radio" id="byCode" name="searchType" value="1" checked title="按网点编号查询" />编号
					<input type="radio" id="byName" name="searchType" value="0" title="按网点名称查询" />名称
				</label>
				<form:input path="name" maxlength="50" class="input-mini" />
				&nbsp;<label>电话 ：</label>
				<form:input path="contactInfo1" maxlength="20" class="input-mini digits" cssStyle="width: 100px;"/>
				&nbsp; <label>结算方式 ：</label>
				<select id="finance.paymentType" name="finance.paymentType" class="input-small" style="width:100px;">
					<option value=""
						<c:out value="${(servicePoint.finance==null || servicePoint.finance.paymentType == null || !empty servicePoint.finance.paymentType.value)?'selected=selected':''}" />>所有</option>
					<c:forEach items="${fns:getDictListFromMS('PaymentType')}" var="dict"><%--切换为微服务--%>
						<option value="${dict.value}"
							<c:out value="${(servicePoint.finance.paymentType.value eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
					</c:forEach>
				</select> &nbsp;
					<input id="btnSubmit" class="btn btn-primary"  type="submit" onclick="return setPage();" value="查询" />

			</div>
		</form:form>
		<tags:message content="${message}" />
		<table id="contentTable" class="datatable table table-bordered table-condensed"
			style="table-layout:fixed;width: 98%;" cellspacing="0">
			<thead>
				<tr>
					<th width="130">网点编号</th>
					<th width="160">网点名称</th>
					<c:if test="${hidePhone != null && hidePhone != '1'}">
						<th width="90">手机</th>
					</c:if>
					<th width="130">详细地址</th>
					<th width="60">派单数</th>
					<th width="60">完成单数</th>
					<th width="60">违约单数</th>
					<th width="60">评价分数</th>
					<th width="60">结算方式</th>
					<c:if test="${!currentuser.isEngineer()}">
					<th width="120">备注</th>
					</c:if>
				</tr>
			</thead>
			<tbody>
				<c:set var="index" value="0" />
				<c:forEach items="${page.list}" var="servicepoint">
					<tr>
						<td><a href="javascript:void(0);" onclick="setData(${index});"> ${servicepoint.servicePointNo}</a></td>
						<td>${servicepoint.name}</td>
						<c:if test="${hidePhone != null && hidePhone != '1'}">
						<td>${servicepoint.contactInfo1}<input id="btnCall" class="btn" type="button" value="拔号" onclick="javascript:StartDial(${servicepoint.contactInfo1},false)" /></td>
						</c:if>
						<td>${servicepoint.address}</td>
						<td><span class="label label-info">${servicepoint.planCount}</span>
						</td>
						<td><span class="label label-success">${servicepoint.orderCount}</span>
						</td>
						<td><span class="label label-important">${servicepoint.breakCount}</span>
						</td>
						<td>${servicepoint.grade}</td>
						<td>${servicepoint.finance.paymentType.label}<c:if test="${servicepoint.finance.invoiceFlag == 1}"><br/><span class="label status_Canceled">开票</span></c:if></td>
						<c:if test="${!currentuser.isEngineer()}">
						<td>${servicepoint.remarks}</td>
						</c:if>
					</tr>
					<c:set var="index" value="${index+1}" />
				</c:forEach>
			</tbody>
		</table>
		<div class="pagination">${page}</div>
	</div>
	<object id="plugin0" type="application/x-nyteleactivex" width="300" height="300">
			    <param name="onload" value="pluginLoaded" />
				<param name="install-url" value="${ctxPlugin}/npNYTeleActiveX.dll" />
			</object>
	<script type="text/javascript" language="javascript" class="init">
	$(document).ready(function() {
		$("td,th").css({"text-align":"center","vertical-align":"middle"});
        var w = $(window).width();
        FixTable("contentTable", 2, "99%", 460);
	});
	</script>
</body>
</html>
