<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>下游网点退补处理</title>
	<meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#treeTable").treeTable({expandLevel : 2});
			$("a#aSave").fancybox({
                fitToView : false,
                width  : 780,
                height  : 500,
                autoSize : false,
                closeClick : false,
                type  : 'iframe',
                openEffect : 'none',
                closeEffect : 'none'
            });
		});
		function setConfirm(id, no, itemid){

	        var submit = function (v, h, f) {
	            if (v == 'ok') {
	    	 		$(this).attr("disabled", "disabled");
	    	 		$("#aSave").attr("href", "${ctx}/fi/engineerchargewriteoff/writeoffform?id="+id);
	    	 		$("#aSave").val(id);
	    	 		$("#aSave").click();
	            }
	            else if (v == 'cancel') {
	                // 取消
	            }

	            return true; //close
	        };

	        top.$.jBox.confirm('您确定要开立退补单吗？', '下游网点退补处理', submit);

	    }

		function openjBox(url,title,width,height){
			top.$.jBox.open("iframe:" + url , title, width, height,{top:'5%',buttons:{}	});
		}
	</script>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">下游网点对帐列表</a></li>
	</ul>
	<form:form id="searchForm" modelAttribute="engineerCharge" action="${ctx}/fi/engineerchargewriteoff/list" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<a id="aSave" type="hidden"></a>
		<div>
			<label>产品类别：</label>
            <sys:treeselect id="category" name="productCategoryId" value="${productCategoryId}" labelName="productCategoryName" labelValue="${productCategoryName}"
			title="公司" url="/md/productcategory/treeData" allowClear="true"/>
			<label>产品名称：</label><input type = "text" id="productName" name="productName" value="${productName}" maxlength="30" class="input-small"/>
			<label>网点：</label>
			<sd:servicePointSelect id="servicePoint" name="servicePointId" value="${servicePointId}" labelName="servicePointName" labelValue="${servicePointName}"
								 width="1200" height="780" title="选择服务网点" areaId="" cssClass="required"
								 showArea="false" allowClear="true" callbackmethod="" />
		</div>
		<div style="margin-top:8px">
			<label>完成日期：</label><input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
				value="${beginDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			<label>~</label>&nbsp;&nbsp;&nbsp;<input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
				value="${endDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			<label>订单编号：</label><input type = "text" id="orderNo" name="orderNo" value="${orderNo}" maxlength="30" class="input-small"/>
			<c:set var="statusList" value="${fns:getDictListFromMS('engineer_charge_status')}" /><%-- 切换为微服务 --%>
			<label>状态：</label>
			<select id="status" name="status" style="width:154px;">
			<option value="" <c:out value="${(empty status)?'selected=selected':''}" />>所有</option>
			<c:forEach items="${statusList}" var="dict">
				<option value="${dict.value}" <c:out value="${(status eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
			</c:forEach>
		</select>
			&nbsp;&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询" onclick="top.$.jBox.tip('请稍候...', 'loading');return setPage();"/>
		</div>
	</form:form>
	<sys:message content="${message}"/>
	<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th>序号</th>
				<th>订单编号</th>
				<th>上门次序</th>
				<th>服务类型</th>
				<th>产品</th>
				<th>数量</th>
				<th>网点</th>
				<th>状态</th>
				<th>对帐时间</th>
				<th>服务费</th>
				<th>快递费</th>
				<th>远程费</th>
				<th>配件费</th>
				<th>其他</th>
				<th>合计</th>
				<th>操作</th>
			</tr>
		</thead>
		<tbody>
		<c:set var="totalCharge" value="0" />
		<c:forEach items="${page.list}" var="engineerCharge">
			<c:set var="index" value="${index+1}" />
			<tr id="${engineerCharge.id}" pId="0">
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td><a href="javascript:void(0);" onclick="Order.viewOrderDetail('${engineerCharge.orderId}');"  ><abbr title="点击查看订单详情">${engineerCharge.orderNo}</abbr></a></td>
				<td>第<span style="color:blue">${engineerCharge.serviceTimes}</span>次</td>
				<td>${engineerCharge.serviceType.name}</td>
				<td>${engineerCharge.product.name}</td>
				<td>${engineerCharge.qty}</td>
				<td>${engineerCharge.servicePoint.servicePointNo},${engineerCharge.servicePoint.name}</td>
				<td>
					<c:choose>
						<c:when test="${engineerCharge.status eq 10}">
							<span class="label status_Completed">
						</c:when>
						<c:when test="${engineerCharge.status eq 20}">
							<span class="label status_Returned">
						</c:when>
					<%--</c:choose>${fns:getDictLabel(engineerCharge.status, 'engineer_charge_status', '')}</span></td>--%>
					</c:choose>${engineerCharge.statusName}</span></td><%-- 切换为微服务 --%>
				<td><fmt:formatDate value="${engineerCharge.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
				<td><c:if test="${engineerCharge.serviceCharge ne 0}">${engineerCharge.serviceCharge}</c:if></td>
				<td><c:if test="${engineerCharge.expressCharge ne 0}">${engineerCharge.expressCharge}</c:if></td>
				<td><c:if test="${engineerCharge.travelCharge ne 0}">${engineerCharge.travelCharge}</c:if></td>
				<td><c:if test="${engineerCharge.materialCharge ne 0}">${engineerCharge.materialCharge}</c:if></td>
				<td><c:if test="${engineerCharge.otherCharge ne 0}">${engineerCharge.otherCharge}</c:if></td>
				<td style="color:red"><B>${engineerCharge.serviceCharge+engineerCharge.expressCharge+engineerCharge.travelCharge+engineerCharge.materialCharge+engineerCharge.otherCharge}</B></td>
				<c:set var="totalCharge" value="${totalCharge+engineerCharge.serviceCharge+engineerCharge.expressCharge+engineerCharge.travelCharge+engineerCharge.materialCharge+engineerCharge.otherCharge}" />
				<td>
					<c:if test="${engineerCharge.chargeOrderType eq 0}">
						<input type="Button" id="${engineerCharge.id}" class="btn btn-mini btn-danger" value="退补" onclick="setConfirm('${engineerCharge.id}','${engineerCharge.orderNo}')"/>
					</c:if>
				</td>
			</tr>
			<c:if test="${engineerCharge.chargeOrderType eq 1}">
			<tr>
				<td></td>
				<td style="background-color: #f2dede;">
					退补描述
				</td>
				<td style="background-color: #f2dede;" colspan="13">${engineerCharge.remarks}</td>
				<td></td>
			</tr>
			</c:if>
		</c:forEach>
		<tr>
			<td style="text-align:right;" colspan="9" ><B>合计</B></td>
			<td style="text-align:right;color:red;padding-right:15px;" colspan="6"><B>${fns:formatNum(totalCharge)}</B></td>
			<td></td>
		</tr>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>
