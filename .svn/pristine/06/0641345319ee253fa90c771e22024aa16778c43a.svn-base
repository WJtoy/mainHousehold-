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

        function page(n,s){
            var name = $("#name").val();
            var contactInfo1 = $("#contactInfo1").val();
            if(isEmpty(name) && isEmpty(contactInfo1)){
                var searchType = $('input[name="searchType"]:checked').val();
                if(searchType==1){
                    layerAlert("请选择网点编号，手机号中至少一项进行查询");
				}else{
                    layerAlert("请选择网点名称，手机号中至少一项进行查询");
				}
                return false;
            }
            $("#pageNo").val(n);
            $("#pageSize").val(s);
            $("#searchForm").submit();
            return false;
        }

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
					grade:${point.grade},
					appFlag:${point.primary.appFlag},
					paymentType:{label:"${point.finance.paymentType.label}",value:${point.finance.paymentType.value}}},
			</c:forEach>
			];

        function isEmpty(obj){
            if(typeof obj == "undefined" || obj == null || obj == ""){
                return true;
            }else{
                return false;
            }
        }

	</script>

</head>

<body>
	<c:if test="${searchTag==1}">
		<div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
			请选择网点编号，网点名称，手机号中至少一项进行查询
		</div>
	</c:if>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<div style="margin-left:3px;margin-right:3px;">
		<form:form id="searchForm" modelAttribute="servicePoint"  action="${ctx}/sd/common/selectServicePoint?showArea=${showArea}" method="post" class="breadcrumb form-search">
			<input type="hidden" name="searchTag" value="2">
			<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
			<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
			<input id="layerIndex" name="layerIndex" type="hidden" value="${servicePoint.layerIndex}"/>
			<div style="margin-bottom:5px;">
				<c:if test="${showArea eq 'true'}">
				<label>区域：</label>
						<sys:treeselectlayer id="area" name="area.id" value="${servicePoint.area.id}"
							labelName="area.name" labelValue="${servicePoint.area.name}" title="区域"
							url="/sys/area/treeData" nodesLevel="2" nameLevel="3" allowClear="true"
							cssStyle="width:140px;"  />
				&nbsp;</c:if>
				<label>
                    <form:radiobutton path="searchType" name="searchType" value="1" title="按网点编号查询"></form:radiobutton><label for="searchType1">编号</label>
                    <form:radiobutton path="searchType" name="searchType" value="0" title="按网点名称查询"></form:radiobutton><label for="searchType2">名称</label>
				</label>
				<form:input path="name" maxlength="100" class="input-mini" cssStyle="width: 300px;" placeholder="请输入完整编号或名称"/>
				&nbsp;<label>电话 ：</label>
				<form:input path="contactInfo1" maxlength="20" onkeyup="this.value=this.value.replace(/\D/g,'')" class="input-mini digits" cssStyle="width: 100px;"/>
				&nbsp; <label>结算方式 ：</label>
				<select id="finance.paymentType" name="finance.paymentType" class="input-small" style="width:100px;">
					<option value=""
						<c:out value="${(servicePoint.finance==null || servicePoint.finance.paymentType == null || !empty servicePoint.finance.paymentType.value)?'selected=selected':''}" />>所有</option>
					<c:forEach items="${fns:getDictListFromMS('PaymentType')}" var="dict"><%--切换为微服务--%>
						<option value="${dict.value}"
							<c:out value="${(servicePoint.finance.paymentType.value eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
					</c:forEach>
				</select> &nbsp;
					<input id="btnSubmit" class="btn btn-primary"  type="button" onclick="page();" value="查询" />

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
