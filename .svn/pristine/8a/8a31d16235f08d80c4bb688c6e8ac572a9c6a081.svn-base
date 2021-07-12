<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<title>区域汇总</title>
	<meta name="decorator" content="default" />
</head>
<style type="text/css">
	.table thead th, .table tbody td {
		text-align: center;
		vertical-align: middle;
		BackColor: Transparent;
	}
</style>

<script type="text/javascript">
	function editProductCategory() {
		var text = "普通区域VIP等级";
		var url = "${ctx}/provider/md/regionPermissionNew/form";
		var area = ['500px', '309px'];
		top.layer.open({
			type: 2,
			id:"regionPermissionNew",
			zIndex:19,
			title:text,
			content: url,
			area: area,
			shade: 0.3,
			maxmin: false,
			success: function(layero,index){
			},
			end:function(){
			}
		});
	}
</script>
<body>
	<ul class="nav nav-tabs">
		<li><a href="${ctx}/provider/md/regionPermissionNew/findRegionPermissionList">客服区域</a></li>
		<li class="active"><a href="javascript:void(0);">区域汇总</a></li>
	</ul>
	<form:form id="searchForm" action="${ctx}/provider/md/regionPermissionNew/findAreaList" method="post" class="breadcrumb form-search">
		<label>全国区/县</label>
		<label style="font-size: 16px;font-weight:bold">${areaModel.areaSum}</label>
		&nbsp;&nbsp;&nbsp;&nbsp;
		<label>全国街道</label>
		<label style="font-size: 16px;font-weight:bold">${areaModel.streetSum}</label>
	</form:form>
	<sys:message content="${message}" />
	<table id="contentTable"
		class="table table-striped table-bordered table-condensed table-hover" >
		<thead>
			<tr>
				<th rowspan="2"  width="200">产品品类</th>
				<th colspan="2" width="200">突击区域</th>
				<th colspan="2" width="200">自动区域</th>
				<th colspan="2" width="200">
					普通区域(VIP等级:&nbsp;${mdCustomerVipLevels ==null ? "":mdCustomerVipLevels.name})
					<a href="javascript:void(0);" onclick="javascript:editProductCategory()">
						<img style="width: 18px" src="${ctxStatic}/images/price05.png">
					</a>
				</th>
			</tr>

			<tr >
				<th width="100">街道</th>
				<th width="100">占全国街道比重</th>
				<th width="100">区/县</th>
				<th width="100">占全国区/县比重</th>
				<th width="100">区/县</th>
				<th width="100">占全国区/县比重</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${list}" var="item">
			<c:set var="areaSum" value="${areaModel.areaSum}"/>
			<c:set var="streetSum" value="${areaModel.streetSum}"/>
			<tr>

				<td>${item.productcategoryName}</td>

				<c:choose>
					<c:when test="${item.crushStreetSum>0}">
						<td style="color:red;">${item.crushStreetSum}</td>
						<td style="color:red;"><fmt:formatNumber value="${item.crushStreetSum/streetSum * 100}" pattern="0.00"/>%</td>
					</c:when>
					<c:otherwise>
						<td>${item.crushStreetSum}</td>
						<td><fmt:formatNumber value="${streetSum == 0 ? 0 : item.crushStreetSum/streetSum * 100}" pattern="0.00"/>%</td>
					</c:otherwise>
				</c:choose>

				<td>${item.automaticAreaSum}</td>
				<td><fmt:formatNumber value="${areaSum == 0 ? 0 : item.automaticAreaSum/areaSum * 100}" pattern="0.00"/>%</td>

				<td>${areaSum-item.automaticAreaSum-item.crushAreaSum}</td>
				<c:set var="kefuAreaSum" value="${areaSum-item.automaticAreaSum-item.crushAreaSum}"/>
				<td><fmt:formatNumber value="${areaSum == 0 ? 0: kefuAreaSum/areaSum * 100}" pattern="0.00"/>%</td>

			</tr>
		</c:forEach>
		</tbody>
	</table>
</body>
</html>
