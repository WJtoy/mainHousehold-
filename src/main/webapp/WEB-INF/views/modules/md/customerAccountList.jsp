<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>客户账号管理</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<style type="text/css">.sort{color:#0663A2;cursor:pointer;}</style>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		top.layer.closeAll();
		$(document).ready(function() {
			$('a[data-toggle=tooltip]').darkTooltip();

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

            $("#btnSubmit").on("click", function(){
                $("#pageNo").val(1);
                var loginName = $("#loginName").val();
                var name = $("#name").val();
                var userType = $("#userType").val();
                var selCustomer = true;
				<c:choose>
					<c:when test="${fns:getUser().isCustomer()}">
                		selCustomer = false;
					</c:when>
					<c:when test="${fns:getUser().isSaleman()}">
						if (name.length >0 || loginName.length >0 || (name.length >0 && userType != 0) || (loginName.length >0 && userType >0)) {
							selCustomer = false;
						}
					</c:when>
                	<c:otherwise>
                		selCustomer = false;
					</c:otherwise>
				</c:choose>

                var val = $("#customerAccountProfile\\.customer\\.id").val();
                if ((val == undefined || val.length == 0) && selCustomer ==true) {
                    layerInfo("请选择客户!", "信息提示");
                    return false;
                }
                var url = "${ctx}/md/customer/account/list";
                $("#searchForm").attr("action",url);
                $("#searchForm").submit();
                return false;
            });

		});

		function resetPassword(id){
            if (id == undefined || id=='' || id == '0') {
                layerError('参数错误', '错误提示',true);
                return false;
            }
            top.layer.confirm("重置密码后，需要重新登录，<br/>确定要<font color='blue'>重置</font>密码吗?", {icon: 3, title:'系统确认'}, function(index){
                top.layer.close(index);//关闭本身
                // do something
                var data = {id:id};
                $.ajax({
                    cache: false,
                    type: "POST",
                    url: "${ctx}/md/customer/resetPassword",
                    data: data,
                    success: function (data) {
                        if (data.success){
                            layerMsg("密码已重置成功。<br/>新的密码就是您的手机号<font color='#ff4500'>后六位</font>。",true)
                        }
                        else{
                            layerError(data.message,"错误提示",true);
                        }
                    },
                    error: function (xhr, ajaxOptions, thrownError) {
                        layerError(thrownError,"错误提示",true);
                    }
                });
            },function(index){
                //cancel
            });
            return false;
        }
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li class="active"><br></li><li class="active"><a href="javascript:;">客户账号列表</a></li>
		<shiro:hasPermission name="md:customer:edit"><li><a href="${ctx}/md/customer/account/form">添加子帐号</a></li></shiro:hasPermission>
	</ul>

	<c:set var="currentuser" value="${fns:getUser() }" />
	<c:set var="mdcustomerID" value="${currentuser.isCustomer()?currentuser.getCustomerAccountProfile().getCustomer().getId():user.getCustomerAccountProfile().getCustomer().getId()}"></c:set>
	<form:form id="searchForm" modelAttribute="user" action="${ctx}/md/customer/account/list" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="20"/>
		<input id="orderBy" name="orderBy" type="hidden" value="${page.orderBy}"/>
		<ul class="ul-form">
			<c:choose>
				<c:when test="${currentuser.isCustomer()}">
					<li>
						<label>客户：</label>
						<form:hidden path="customerAccountProfile.customer.id"/>
						<form:input path="customerAccountProfile.customer.name" readonly="true"/>
					</li>
				</c:when>
				<c:otherwise>
					<li>
						<label>客户：</label>
						<form:select path="customerAccountProfile.customer.id" class="input-large">
							<form:option value="" label="所有"/>
							<form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false" />
						</form:select>
					</li>
				</c:otherwise>
			</c:choose>
			<li>
				<label>登录名：</label><form:input path="loginName" htmlEscape="false" maxlength="30" class="input-small"/>
			</li>
			<li>
				<label>姓名：</label><form:input path="name" htmlEscape="false" maxlength="30" class="input-small"/>
			</li>
			<li>
				<label>联系电话：</label><input id="phone" name="phone" class="input-small" type="text" value="${user.phone}" maxlength="30">
			</li>
			<li>
				<label>帐户类型：</label>
				<form:select path="userType" style="width:80px;">
					<form:option value="0" label="所有"/>
					<form:option value="3" label="主帐号"/>
					<form:option value="4" label="子帐号"/>
					<form:option value="9" label="查询帐号"/>
				</form:select>
			</li>
			<%--<li>
				<label>订单审核：</label>
				<form:select path="customerAccountProfile.orderApproveFlag" style="width:80px;">
					<form:option value="-1" label="所有"/>
					<form:option value="1" label="是"/>
					<form:option value="0" label="否"/>
				</form:select>
			</li>--%>
			<li class="btns">
				<%--<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />--%>
				<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询" />
			</li>
			<li class="clearfix"></li>
		</ul>
		</div>
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th>序号</th>
				<th >客户</th>
				<th class="sort loginName">登录名</th>
				<th class="sort name">姓名</th>
				<th >账号类型</th>
				<th >电话</th>
				<th >手机</th>
				<th >订单审核标识</th>
				<shiro:hasPermission name="md:customer:edit">
					<th >操作</th>
				</shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${page.list}" var="user">
			<c:set var="index" value="${index+1}" />
			<tr>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${user.customerAccountProfile.customer.name}</td>
				<td>
					<c:choose>
						<c:when test="${user.delFlag == 1}">
							<a data-toggle="tooltip" data-tooltip="帐号已停用" class="label label-important">${user.loginName}</a>
						</c:when>
						<c:otherwise>${user.loginName}</c:otherwise>
					</c:choose>
				</td>
				<td>${user.name}</td>
				<%--<td>${fns:getDictLabel(user.userType,'sys_user_type','账号类型错误')}</td>--%>
				<td>${not empty user.userTypeName?user.userTypeName:'账号类型错误'}</td><%-- 切换为微服务 --%>
				<td>${user.phone}</td>
				<td>${user.mobile}</td>
				<td>${user.customerAccountProfile.orderApproveFlag eq '1'? '是':'否'}</td>
				<shiro:hasPermission name="md:customer:edit">
				<td>
    				<a href="${ctx}/md/customer/account/form?id=${user.id}">修改</a>
					<a href="javascript:;" onclick="resetPassword('${user.id}');">重置密码</a>
					<c:choose>
						<c:when test="${user.delFlag == 0}">
							<a href="${ctx}/md/customer/account/delete?id=${user.id}&loginName=${fns:urlEncode(user.loginName)}&customerAccountProfile.customer.id=${mdcustomerID}" onclick="return layerConfirmx('确认要<font color=\'red\'>停用</font>该账号吗？', this.href)">停用</a>
						</c:when>
						<c:when test="${user.delFlag == 1}">
							<a href="${ctx}/md/customer/account/enable?id=${user.id}&loginName=${fns:urlEncode(user.loginName)}&customerAccountProfile.customer.id=${mdcustomerID}" onclick="return layerConfirmx('确认要<font color=\'blue\'>启用</font>该账号吗？', this.href)">启用</a>
						</c:when>
					</c:choose>

				</td>
				</shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>
