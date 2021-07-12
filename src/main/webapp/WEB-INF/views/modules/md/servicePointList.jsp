<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>服务网点管理</title>
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
			top.layer.closeAll();
			$(document).ready(function() {

				$("#contentTable").treeTable({expandLevel : 2});
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
			});

            //查看网店备注历史列表
            function viewRemarkList(servicePointId,servicePointNo,servicePointName) {
                var planIndex = top.layer.open({
                    type: 2,
                    id:'layer_planRemarkList_view',
                    zIndex:19891016,
                    title:'网点备注',
                    content: "${ctx}/md/servicepoint/viewRemarkList?servicePointId=" + (servicePointId || '')+"&servicePointNo="+ (servicePointNo || '')+"&servicePointName="+ (servicePointName || ''),
                    // area: ['980px', '640px'],
                    area: ['1255px', (screen.height/2)+'px'],
                    shade: 0.3,
                    shadeClose: true,
                    maxmin: false,
                    success: function(layero,index){
                    },
                    end:function(){
                    }
                });
            }

		</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:;">服务网点列表</a></li>
		<shiro:hasPermission name="md:servicepoint:edit">
			<li><a href="${ctx}/md/servicepoint/form">服务网点添加</a></li>
		</shiro:hasPermission>
		<li><a href="${ctx}/md/servicepoint/disableList">停用列表</a></li>
		<%--<shiro:hasPermission name="md:servicepoint:edit">--%>
			<%--<li><a href="${ctx}/md/servicepoint/approvelist">安维网点审核</a></li>--%>
		<%--</shiro:hasPermission>--%>
		<%--<li><a href="${ctx}/md/servicepoint/approveinvoiced">有付款待审核安维网点</a></li>--%>
	</ul>
	<sys:message content="${message}" />
<shiro:hasPermission name="md:servicepoint:view">
	<form:form id="searchForm" modelAttribute="servicePoint" action="${ctx}/md/servicepoint" method="POST" class="breadcrumb form-search">
		<form:hidden path="firstSearch" />
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
		<input id="orderBy" name="orderBy" type="hidden" value="${servicePoint.orderBy}" />
		<ul class="ul-form">
			<li>
				<label>网点编号：</label>
				<form:input path="servicePointNo" htmlEscape="false" maxlength="20"	class="input-small" />
			</li>
			<li>
				<label>网点名称：</label>
				<form:input path="name" htmlEscape="false" maxlength="30"	class="input-small" />
			</li>
			<li>
				<label>网点电话：</label>
				<form:input path="contactInfo1" htmlEscape="false" maxlength="20" class="input-small" />
			</li>
			<li>
				<label>开发人员：</label>
				<form:input path="developer" htmlEscape="false" maxlength="20" class="input-small" />
			</li>
			<li>
				<label>签约：</label>
				<form:select path="signFlag" class="input-small">
					<form:option value="-1" label="所有" />
					<form:options items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" /><%--切换为微服务--%>
				</form:select>
			</li>
			<li>
				<label class="control-label">状态:</label>
				<form:select path="status.value" class="input-small">
					<form:option value="0" label="所有" />
					<form:options items="${fns:getDictListFromMS('service_point_status')}" itemLabel="label" itemValue="value" htmlEscape="false" />
				</form:select>
			</li>
			<%--<li>
				<label class="control-label">等级:</label>
				<form:select path="level.value" class="input-small">
					<form:option value="0" label="所有" />
					<form:options items="${fns:getDictExceptListFromMS('ServicePointLevel', '6,7,8')}" itemLabel="label" itemValue="value" htmlEscape="false" />&lt;%&ndash;切换为微服务&ndash;%&gt;
				</form:select>
			</li>--%>
			<li>
				<label class="control-label">分级:</label>
				<form:select path="degree" class="input-small">
					<form:option value="0" label="所有" />
					<form:options items="${fns:getDictListFromMS('degreeType')}" itemLabel="label" itemValue="value" htmlEscape="false" />
				</form:select>
			</li>
			<li>
				<label style="margin-left: 40px">区域：</label>
				<sys:treeselect id="area" name="area.id" value="${servicePoint.area.id}" labelName="area.name" labelValue="${servicePoint.area.name}" title="区域"
								 url="/sys/area/treeData" nodesLevel="2" nameLevel="3" cssStyle="width:140px;" cssClass="required" />
			</li>
			<li>
			<label class="control-label">开户行:</label>
			<form:select path="finance.bank.value" class="input-small">
				<form:option value="" label="所有" />
				<form:options items="${fns:getDictListFromMS('banktype')}" itemLabel="label" itemValue="value" htmlEscape="false" /><%--切换为微服务--%>
			</form:select>
			</li>
			<li>
				<label>账号：</label>
				<form:input path="finance.bankNo" htmlEscape="false" maxlength="20" class="input-small" />
			</li>
			<li>
				<label>支付失败原因：</label>
				<form:select path="finance.bankIssue.value" class="input-small">
					<form:option value="" label="所有" />
					<form:options items="${fns:getDictListFromMS('BankIssueType')}" itemLabel="label" itemValue="value" htmlEscape="false" /><%-- 切换为微服务 --%>
				</form:select>
			</li>
			<li>
				<label>服务品类：</label>
				<select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
					<option value="0" <c:out value="${(empty servicePoint.productCategory)?'selected=selected':''}" />>所有</option>
					<c:forEach items="${fns:getProductCategories()}" var="dict">
						<option value="${dict.id}" <c:out value="${(servicePoint.productCategory eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
					</c:forEach>
				</select>
			</li>
			<li>
				<label>手机接单：</label>
				<form:select path="primary.appFlag" class="input-small">
					<form:option value="-1">所有</form:option>
					<form:option value="1">是</form:option>
					<form:option value="0">否</form:option>
				</form:select>
			</li>
			<shiro:hasPermission name="md:servicepoint:bank">
			<li>
				<label>是否开票：</label>
				<form:select path="finance.invoiceFlag" class="input-small">
					<form:option value="-1">所有</form:option>
					<form:option value="1">是</form:option>
				</form:select>
			</li>
			<li>
				<label>是否扣点：</label>
				<form:select path="finance.discountFlag" class="input-small">
					<form:option value="-1">所有</form:option>
					<form:option value="1">是</form:option>
					<form:option value="0">否</form:option>
				</form:select>
			</li>
			</shiro:hasPermission>
			<shiro:hasPermission name="md:servicepoint:autocomplete">
			<li>
				<label>自动完工：</label>
				<form:select path="autoCompleteOrder" class="input-small">
					<form:option value="-1">所有</form:option>
					<form:option value="1">是</form:option>
				</form:select>
			</li>
			</shiro:hasPermission>
			<li>
				<label>完成单数：</label>
				<%--<input id="orderCount" name="orderCount" type="checkbox" value="${servicePoint.orderCount ge 1?1:-1}" <c:out value="${servicePoint.orderCount ge 1?'checked':''}" /> >--%>
				<input id="orderCount" name="orderCount" type="number" value="${servicePoint.orderCount}" class="required number" style="width: 100px;" onkeyup="this.value=this.value.replace(/\D/g,'')" onafterpaste="this.value=this.value.replace(/\D/g,'')"/>
			</li>
			<li class="btns"><input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" /></li>
			<li class="clearfix"></li>
		</ul>
	</form:form>
	<table id="contentTable"
		class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="30">序号</th>
				<th class="sort servicepoint_no" style="width:110px">网点编号</th>
				<th class="sort name" style="width:120px">名称</th>
<%--				<th width="100">网点电话</th>--%>
				<%--<th width="120">电话</th>--%>
				<th width="200">详细地址</th>
				<th width="45">接单量</th>
				<th width="70">状态</th>
				<th width="30">等级</th>
				<th width="60">手机接单</th>
				<shiro:hasPermission name="md:servicepoint:autocomplete">
					<th width="60">自动完工</th>
				</shiro:hasPermission>
				<shiro:hasPermission name="md:servicepoint:bank">
					<th width="30">扣点</th>
					<th width="30">开票</th>
					<th width="60">开户行</th>
					<th width="160">账号</th>
					<th width="80">开户人</th>
					<th width="80">开发人员</th>
				</shiro:hasPermission>
				<th width="105">覆盖区域</th>
				<th width="105">服务品类</th>
				<th width="100">分级</th>
				<th width="120">备注</th>
				<th width="50" style="border-left-style: none">&nbsp;&nbsp;&nbsp;&nbsp;</th>
				<shiro:hasPermission name="md:servicepoint:edit">
				<th width="80">操作</th>
				</shiro:hasPermission>
				<%--<shiro:hasPermission name="md:tmall:servicepoint">&lt;%&ndash; B2B &ndash;%&gt;--%>
					<%--<th width="60">B2B管理</th>--%>
				<%--</shiro:hasPermission>--%>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${page.list}" var="point">
				<c:set var="index" value="${index+1}" />
				<tr id="${point.id}">
					<td>${index+(page.pageNo-1)*page.pageSize}</td>
					<td>${point.servicePointNo}</td>
					<c:choose>
						<c:when test="${point.finance.bankIssue==null || point.finance.bankIssue.value == '0'}">
							<td><a href="javascript:void(0);" title="${point.name}">${fns:abbr(point.name,35)}</a></td>
						</c:when>
						<c:otherwise>
							<td><a href="javascript:" data-toggle="tooltip"
								data-tooltip="${point.finance.bankIssue.label}"><label id="lbltotalCharge" class="alert alert-error">${point.name}</label></a>
							</td>
						</c:otherwise>
					</c:choose>
<%--					<td>${point.contactInfo1}</td>--%>
					<%--<td>${point.contactInfo2}</td>--%>
					<td>${point.address}</td>
					<td>${point.orderCount}</td>
					<td>${fns:getDictLabelFromMS(point.status.value, 'service_point_status', '')}</td>
					<td>${point.level.label}</td>
					<td>${point.primary.appFlag==1?'是':'否'}</td>
					<shiro:hasPermission name="md:servicepoint:autocomplete">
					<td>${point.autoCompleteOrder==1?'是':'否'}</td>
					</shiro:hasPermission>
					<shiro:hasPermission name="md:servicepoint:bank">
						<td>
							<c:if test="${point.finance.discountFlag == 1}"><span class="label status_Canceled">是</span></c:if>
							<c:if test="${point.finance.discountFlag == 0}">否</c:if>
						</td>
						<td>
							<c:if test="${point.finance.invoiceFlag == 1}"><span class="label status_Canceled">${fns:getDictLabelFromMS(point.finance.invoiceFlag, "yes_no","")}</span></c:if><%--切换为微服务--%>
							<c:if test="${point.finance.invoiceFlag == 0}">${fns:getDictLabelFromMS(point.finance.invoiceFlag, "yes_no","")}</c:if><%--切换为微服务--%>
						</td>
						<td>${point.finance.bank.label}</td>
						<td>${point.finance.bankNo}</td>
						<td>${point.finance.bankOwner}</td>
						<td>${point.developer}</td>
					</shiro:hasPermission>
					<td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${point.serviceAreas}">${fns:abbr(point.serviceAreas,30)}</a></td>
					<td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${point.productCategoryNames}">${fns:abbr(point.productCategoryNames,30)}</a></td>
					<td>${fns:getDictLabelFromMS(point.degree,'degreeType','')}</td>
					<td>
						<a href="javascript:" data-toggle="tooltip"  data-tooltip="${point.remarks}">${fns:abbr(point.remarks,30)}</a>
					</td>
					<td style="border-left-style: none;vertical-align: middle">
						<c:if test="${point.remarks ne ''}">
							<button id="btnShowPlanRemarkList" class="btn btn-small" type="button" onclick="viewRemarkList('${point.id}','${point.servicePointNo}','${point.name}');">
								历史
							</button>
						</c:if>
					</td>
					<shiro:hasAnyPermissions name="md:servicepoint:edit,md:servicepoint:delete">
						<td>
							<%--<a href="${ctx}/md/servicepoint/form?id=${point.id}">修改</a><br>--%>
							<a href="${ctx}/md/servicepoint/newForm?id=${point.id}">修改</a><br>
							<shiro:hasPermission name="md:servicepoint:delete">
								<a href="${ctx}/md/servicepoint/delete?id=${point.id}&type=listDelete" onclick="return confirmx('确认要删除该安维网点吗？', this.href)">删除</a><br>
							</shiro:hasPermission>
							<shiro:hasAnyPermissions name="md:engineer:edit,md:engineer:view">
							<a href="${ctx}/md/engineer/list?servicePoint.id=${point.id}" >人员管理</a>
							</shiro:hasAnyPermissions>
							<shiro:hasAnyPermissions name="md:engineer:edit">
								<a href="${ctx}/md/servicepoint/syncDataToEs?id=${point.id}" onclick="return confirmx('确认要同步网点数据到服务器吗？', this.href)">同步</a><br>
							</shiro:hasAnyPermissions>
						</td>
					</shiro:hasAnyPermissions>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</shiro:hasPermission>
</body>
</html>
