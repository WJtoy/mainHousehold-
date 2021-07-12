<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>选择安维网点</title>
	<meta name="decorator" content="default" />
	<style type="text/css">
		.pagination {  margin: 10px 0;  }
		.td {  word-break: break-all;  }
	</style>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		var data = null;
		var index = top.layer.index;

		function setData(i){
			data = items[i];
			var layerIndex = $("#layerIndex").val() || index;
            top.$("#layui-layer"+layerIndex).find(".layui-layer-btn0").trigger("click");
		}

		var items =[
			<c:forEach items="${page.list}" var="point">
				{id:"${point.id}",
					servicePointNo:"${point.servicePointNo}",
					name:"${point.name}",
					primary:{
					    id:"${point.primary.id}",
						name:"${point.primary.name}",
                        address:"${point.primary.address}",
						area:"${point.primary.area.id}"
					},
					area:{
					    id:"${point.area.id}",
						fullName:"${point.area.fullName}"
                    },
					address:"${point.address}",
					subAddress : "${point.subAddress}",
					phone:"${point.contactInfo1 }",
					grade:${point.grade},
					appFlag:${point.primary.appFlag},
					paymentType:{label:"${point.finance.paymentType.label}",value:${point.finance.paymentType.value}}},
			</c:forEach>
			];
	</script>

</head>

<body>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<div style="margin-left:3px;margin-right:3px;">
		<form:form id="searchForm" modelAttribute="servicePoint"  action="${ctx}/md/servicepoint/select?dialogType=layer" method="post" class="breadcrumb form-search">
			<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
			<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
			<input id="layerIndex" name="layerIndex" type="hidden" value="${servicePoint.layerIndex}"/>
			<div style="margin-bottom:5px;">
				<label>区域：</label>
				<%--<c:choose>--%>
					<%--<c:when test="${servicePoint.area != null && servicePoint.area.id>0 }">--%>
						<%--<form:hidden path="area.id" />--%>
						<%--<form:input path="area.name" Style="width:140px;" readonly="true" />--%>
					<%--</c:when>--%>
					<%--<c:otherwise>--%>
						<sys:treeselectlayer id="area" name="area.id" value="${servicePoint.area.id}"
							labelName="area.name" labelValue="${servicePoint.area.name}" title="区域"
							url="/sys/area/treeData" nodesLevel="2" nameLevel="3" allowClear="true"
							cssStyle="width:140px;" cssClass="required" />
					<%--</c:otherwise>--%>
				<%--</c:choose>--%>
				&nbsp;
				<label>
                    <form:radiobutton path="searchType" name="searchType" value="1" title="按网点编号查询"></form:radiobutton><label for="searchType1">编号</label>
                    <form:radiobutton path="searchType" name="searchType" value="0" title="按网点名称查询"></form:radiobutton><label for="searchType2">名称</label>
					<%--<input type="radio" id="byCode" name="searchType" value="1" checked title="按网点编号查询" />编号--%>
					<%--<input type="radio" id="byName" name="searchType" value="0" title="按网点名称查询" />名称--%>
				</label>
				<form:input path="name" maxlength="50" class="input-mini" cssStyle="width: 250px;"/>
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
		<sys:message content="${message}" />
		<c:set var="isEngineer" value="${currentuser.isEngineer()}"/>
		<table id="contentTable" class="datatable table table-bordered table-condensed table-hover" >
			<thead>
				<tr>
					<th width="45">序号</th>
					<th width="180">网点</th>
					<th width="100">主帐号</th>
					<th width="80">手机接单</th>
					<c:if test="${hidePhone != null && hidePhone != '1'}">
					<th width="120">手机</th>
					</c:if>
					<th width="250">详细地址</th>
					<th width="80">派单数</th>
					<th width="80">完成单数</th>
					<th width="80">违约单数</th>
					<th width="55">评价分数</th>
					<th width="55">结算方式</th>
					<th width="180">备注</th>
				</tr>
			</thead>
			<tbody>
				<c:set var="index" value="0" />
				<c:forEach items="${page.list}" var="servicepoint">
					<c:set var="i" value="${i+1}" />
					<tr>
						<td>${i+(page.pageNo-1)*page.pageSize}</td>
						<td>
							<a href="javascript:void(0);" onclick="javascript:setData(${index});">
								${servicepoint.servicePointNo}<br/>${servicepoint.name}
									<c:if test="${servicepoint.primary.appLoged eq 1}">&nbsp;<i class="icon-mobile-phone" style="font-size: 17px;" title="该用户有手机登陆过APP" ></i></c:if>
						</a>
						</td>
						<td>${servicepoint.primary.name}</td>
						<td>${servicepoint.primary.appFlag eq 1?'是':'否'}</td>
						<c:if test="${hidePhone != null && hidePhone != '1'}">
						<td>${servicepoint.contactInfo1}
							<c:if test="${!empty servicepoint.contactInfo1}">
							<button id="btnCall" class="btn btn-success" type="button" onclick="javascript:StartDial(${servicepoint.contactInfo1},false)"><i class="icon-phone-sign icon-white"></i>  拨号</button>
							</c:if>
						</td>
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
						<td>
							<c:if test="${isEngineer == false}">
								<a href="javascript:void(0);" title="${servicepoint.remarks}">${fns:abbr(servicepoint.remarks,35)}</a>
							</c:if>
						</td>
					</tr>
					<c:set var="index" value="${index+1}" />
				</c:forEach>
			</tbody>
		</table>
		<div class="pagination">${page}</div>
	</div>
	<object id="plugin0" type="application/x-nyteleactivex" width="0" height="0">
			    <param name="onload" value="pluginLoaded" />
				<param name="install-url" value="${ctxPlugin}/npNYTeleActiveX.dll" />
			</object>
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
	<script type="text/javascript" language="javascript" class="init">
	$(document).ready(function() {
		$("td,th").css({"text-align":"center","vertical-align":"middle"});
        if(Utils.isEmpty($("#layerIndex").val())){
            $("#layerIndex").val(index);
        }
	});
	</script>
</body>
</html>
