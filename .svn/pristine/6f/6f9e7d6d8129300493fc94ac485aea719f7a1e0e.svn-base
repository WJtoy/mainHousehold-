<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>安维网点审核</title>
	<meta name="decorator" content="default" />
	<%@include file="/WEB-INF/views/include/dialog.jsp"%>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<%@include file="/WEB-INF/views/include/treeview.jsp"%>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<style type="text/css">
	.sort {color: #0663A2;cursor: pointer;}
	.form-horizontal .control-label {width: 70px;}
	.form-horizontal .controls { margin-left: 80px;}
	.form-search .ul-form li label {width: auto;}
	</style>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
			$(document).ready(function() {

				//$("#contentTable").treeTable({expandLevel : 2});
				// 表格排序
				var orderBy = $("#orderBy").val().split(" ");
				$("#contentTable th.sort").each(function(){
					if ($(this).hasClass(orderBy[0])){
						orderBy[1] = orderBy[1]&&orderBy[1].toUpperCase()=="DESC"?"down":"up";
						$(this).html($(this).html()+" <i class=\"icon icon-arrow-"+orderBy[1]+"\"></i>");
					}
				});
				$("#contentTable th.sort").click(function(){
					var order = $(this).attr("class").split(" ");
					var sort = $("#orderBy").val().split(" ");
					for(var i=0; i<order.length; i++){
						if (order[i] == "sort"){order = order[i+1]; break;}
					}
					if (order == sort[0]){
						sort = (sort[1]&&sort[1].toUpperCase()=="DESC"?"ASC":"DESC");
						$("#orderBy").val(order+" DESC"!=order+" "+sort?"":order+" "+sort);
					}else{
						$("#orderBy").val(order+" ASC");
					}
					page();
				});

				 $('a[data-toggle=tooltip]').darkTooltip();
				 $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});

				//全选绑定
				$("#selectAll").change(function()
				{
					$("#contentTable input[name='chk_point']").prop('checked', $(this).prop('checked'));
				});

				$("#btnApprove").click(function(){
					var ids = [];
					$("#contentTable input[name='chk_point']:checked").each(function(){
						ids.push($(this).val());
					});
					if (ids.length == 0)
					{
						top.$.jBox.error('请选择要审核的安维网点', '安维网点审核');
						return;
					}
					top.$.jBox.confirm("确认要审核通过选择的安维网点吗？","系统确认",function(v,h,f){
						if(v=="ok"){
							top.$.jBox.tip("正在审核中...", "loading");
							$("#btnApprove").attr("disabled", "disabled");
							var data =
								{
									ids : ids.join(",")
								};
							$.ajax(
								{
									cache : false,
									type : "POST",
									url : "${ctx}/md/servicepoint/approve",
									data : data,
									success : function(data)
									{
										if (data.success)
										{
											top.$.jBox.tip("安维网点审核成功", "success");
											$("#btnApprove").removeAttr("disabled");
											top.$.jBox.closeTip();
											var pageNo = parseInt("${page.pageNo}");
											if(pageNo == 1) {
												$("#searchForm").submit();
											}else{
												var pageCount = parseInt("${page.totalPage}");
												if (pageCount == pageNo){
												   //last page
													var rowcnt = $("#contentTable input[name='chk_point']").length;
													if(ids.length==rowcnt){
														//all of the page confirmed,will redirect prev page
														var pageSize = parseInt("${page.pageSize}");
														return page(pageNo>1?pageNo-1:0,pageSize);
													}else{
//														$("#searchForm").submit();
														search();
													}
												}
											}
											return false;
										} else
										{
											top.$.jBox.error(data.message);
										}

									},
									error : function(xhr, ajaxOptions, thrownError)
									{
										$("#btnApprove").removeAttr("disabled");
										top.$.jBox.closeTip();
										top.$.jBox.error(thrownError.toString());
									}
								});
						}
					},{buttonsFocus:1});
					//top.$('.jbox-body .jbox-icon').css('top','55px');
				});

			});
		</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li><a href="${ctx}/md/servicepoint/list">安维网点列表</a></li>
		<shiro:hasPermission name="md:servicepoint:edit">
			<li><a href="${ctx}/md/servicepoint/form">安维网点添加</a></li>
		</shiro:hasPermission>
		<shiro:hasPermission name="md:servicepoint:edit">
			<li class="active"><a href="javascript:;">安维网点审核</a></li>
		</shiro:hasPermission>
		<%--<li><a href="${ctx}/md/servicepoint/approveinvoiced">有付款待审核安维网点</a></li>--%>
	</ul>
	<form:form id="searchForm" modelAttribute="servicePoint" action="${ctx}/md/servicepoint/approvelist" method="POST" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
		<input id="orderBy" name="orderBy" type="hidden" value="${servicePoint.orderBy}" />
		<ul class="ul-form">
			<li>
				<label style="margin-left: 40px">区域：</label>
				<sys:treeselect id="area" name="area.id" value="${area.id}" labelName="areaName" labelValue="${servicePoint.area.name}" title="区域"
								url="/sys/area/treeData" nodesLevel="2" nameLevel="3" cssStyle="width:140px;" cssClass="required" />
			</li>
			<li>
				<label>网点编号：</label>
				<form:input path="servicePointNo" htmlEscape="false" maxlength="20"	class="input-small" />
			</li>
			<li>
				<label>网点名称：</label>
				<form:input path="name" htmlEscape="false" maxlength="30"	class="input-small" />
			</li>
			<li>
				<label>手机：</label>
				<form:input path="contactInfo1" htmlEscape="false" maxlength="20" class="input-small" />
			</li>
			<li>
				<label title="有派过单的安维人员">派过单：</label>
				<input id="planCount" name="planCount" value="1"
					   <c:if test="${servicePoint.planCount >0}">checked="checked"</c:if>
					   type="checkbox" class="input-small" />
			</li>
			<li class="btns"><input id="btnSubmit" class="btn btn-primary" value="查询" type="submit" onclick="return setPage();" /></li>
			<li class="btns"><input id="btnApprove" class="btn btn-success" type="button" value="通过审核" /></li>
			<li class="clearfix"></li>
		</ul>
	</form:form>
	<sys:message content="${message}" />
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="30"><input type="checkbox" id="selectAll" name="selectAll" /></th>
				<th width="30">序号</th>
				<th class="sort servicepoint_no" width="130">网点编号</th>
				<th class="sort name">名称</th>
				<th>手机</th>
				<th width="200">详细地址</th>
				<th>接单量</th>
				<th>等级</th>
				<shiro:hasPermission name="md:servicepoint:bank">
					<th>开户行</th>
					<th width="160">账号</th>
					<th>开户人</th>
					<th>开发人员</th>
				</shiro:hasPermission>
				<th width="120">备注</th>
				<th width="80">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:set var="pidx" value="${(page.pageNo-1)*page.pageSize}"></c:set>
			<c:forEach items="${page.list}" var="point">
				<c:set var="pidx" value="${pidx+1}" />
				<tr id="${point.id}">
					<td><input type="checkbox" id="cbox_${pidx}" value="${point.id}" name="chk_point" /></td>
					<td>${pidx}</td>
					<td>${point.servicePointNo}</td>
					<td>${point.name}</td>
					<td>${point.contactInfo1}</td>
					<td>${point.address}</td>
					<td>${point.orderCount}</td>
					<td>${point.level.label}</td>
					<shiro:hasPermission name="md:servicepoint:bank">
						<td>${point.finance.bank.label}</td>
						<td>${point.finance.bankNo}</td>
						<td>${point.finance.bankOwner}</td>
						<td>${point.finance.developer}</td>
					</shiro:hasPermission>
					<td>${point.remarks}</td>
					<td><shiro:hasPermission name="md:servicepoint:edit">
							<a href="${ctx}/md/servicepoint/form?id=${point.id}">修改</a>
							<a href="${ctx}/md/servicepoint/delete?id=${point.id}&type=listDelete" onclick="return confirmx('确认要删除该安维网点吗？', this.href)">删除</a>
						</shiro:hasPermission>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>
