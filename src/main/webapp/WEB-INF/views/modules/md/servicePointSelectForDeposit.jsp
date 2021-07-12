<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>选择安维网点（含质保金信息）</title>
	<meta name="decorator" content="default" />
	<style type="text/css">
		.pagination {  margin: 10px 0;  }
		.td {  word-break: break-all;  }
		.label {padding: 4px 4px !important;text-shadow:none !important;font-weight:400;}
		.label-fullPay{background-color: #0096DA;}
		.label-notFullPay{background-color: #FF9500;}
		.label-notPay{background-color: #F64344;}
	</style>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		var data = null;
		var index = top.layer.index;

		function setData(i){
            data = items[i];
            var layerIndex = $("#layerIndex").val() || index;
            var parentIndex = $("#parentLayerIndex").val();
            if(parentIndex && parentIndex != undefined && parentIndex != '' && parentIndex != 'undefined'){
                var layero = $("#layui-layer" + parentIndex,top.document);
                var iframeWin = top[layero.find('iframe')[0]['name']];
                iframeWin.setServicePoint(data);
                top.layer.close(layerIndex)
            }else{//触发窗口非弹窗页面
				top.$("#layui-layer"+index).find(".layui-layer-btn0").trigger("click");
			}
		}
		var items =[
				<c:forEach items="${page.list}" var="point">{id:"${point.id}",servicePointNo:"${point.servicePointNo}",name:"${point.name}",primary:{id:"${point.primary.id}",name:"${point.primary.name}"}, phone:"${point.contactInfo1 }",mdDepositLevel:{id:"${point.mdDepositLevel.id}",name:"${point.mdDepositLevel.name}"},deposit:"${point.deposit}",finance:{deposit:"${point.finance.deposit}",depositRecharge:${point.finance.depositRecharge}} }, </c:forEach>
			];
	</script>
	<script type="text/javascript">

        function cancel() {
            var layerIndex = $("#layerIndex").val() || index;
            top.layer.close(layerIndex)
        }

	</script>
</head>

<body>
	<div style="margin-left:3px;margin-right:3px;">
		<form:form id="searchForm" modelAttribute="servicePoint"  action="${ctx}/md/servicepoint/selectForDeposit" method="post" class="breadcrumb form-inline">
			<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
			<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
			<input id="layerIndex" name="layerIndex" type="hidden" value="${servicePoint.layerIndex}"/>
			<input id="parentLayerIndex" name="parentLayerIndex" type="hidden" value="${servicePoint.parentLayerIndex}"/>
			<div style="margin-top: 8px">

				&nbsp;&nbsp;<label>网点名称：</label>
				<form:input path="name" maxlength="50" class="input-mini" cssStyle="width: 200px;"/>
				<label>网点编号：</label>
				<form:input path="servicePointNo" maxlength="50" class="input-mini" cssStyle="width: 200px;"/>
				&nbsp;&nbsp;<label>网点电话：</label>
				<form:input path="contactInfo1" maxlength="20" class="input-mini digits" cssStyle="width: 100px;"/>
				<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
			</div>
		</form:form>
		<sys:message content="${message}" />
		<table id="contentTable" class="datatable table table-bordered table-condensed table-hover" >
			<thead>
				<tr>
					<th width="45">序号</th>
					<th width="280">网点</th>
					<th width="100">主帐号</th>
					<th width="100">网点电话</th>
					<th width="80">质保等级</th>
					<th>应缴金额(元)</th>
					<th>已缴金额(元)</th>
					<th>每单扣除(元)</th>
				</tr>
			</thead>
			<tbody>
				<c:set var="index" value="0" />
				<c:forEach items="${page.list}" var="entity">
					<c:set var="i" value="${i+1}" />
					<tr>
						<td>${i+(page.pageNo-1)*page.pageSize}</td>
						<td>
							<a href="javascript:void(0);" onclick="javascript:setData(${index});">
								${entity.servicePointNo}<br/>${entity.name}
							</a>
						</td>
						<td>${entity.primary.name}</td>
						<td>${entity.contactInfo1}</td>
						<td>${entity.mdDepositLevel.name}</td>
						<td><fmt:formatNumber pattern="0.0">${entity.deposit}</fmt:formatNumber> </td>
						<td>
							<fmt:formatNumber pattern="0.0">${entity.finance.deposit}</fmt:formatNumber><br/>
							<c:choose>
								<c:when test="${entity.finance.deposit >= entity.deposit}"><span class="label label-fullPay">已缴满</span></c:when>
								<c:when test="${entity.finance.deposit > 0}"><span class="label label-notFullPay">未缴满</span></c:when>
								<c:otherwise><span class="label label-notPay">未缴费</span></c:otherwise>
							</c:choose>
						</td>
						<td><fmt:formatNumber pattern="0.0">${entity.mdDepositLevel.deductPerOrder}</fmt:formatNumber></td>

					</tr>
					<c:set var="index" value="${index+1}" />
				</c:forEach>
			</tbody>
		</table>
		<div class="pagination">${page}</div>
	</div>
	<div style="height: 60px;width: 100%"></div>
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
