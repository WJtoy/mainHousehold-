<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>客户账号管理[业务]</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        top.layer.closeAll();
        $(document).ready(function() {
            $("#loginName").focus();
            $("#inputForm").validate({
                rules: {
                    loginName: {
                        remote: "${ctx}/md/customer/account/checkLoginName?oldLoginName=" + encodeURIComponent('${!empty user.id?user.loginName:''}')},
                    mobile: {
                        remote: "${ctx}/sys/user/checkMobile?expectId=${!empty user.id?user.id:''}&expectType=user&returnType=message"
                    }
                },
                messages: {
                    loginName: {remote: "客户账号人员登录名已存在"},
                    mobile: {remote: "该手机号用户已存在，请确认输入是否正确"},
                    confirmNewPassword: {equalTo: "输入与上面相同的密码"}
                },
                submitHandler: function(form){
                    var loadingIndex = layerLoading('正在提交，请稍等...');
                    form.submit();
                },
                errorContainer: "#messageBox",
                errorPlacement: function(error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });

            $("#userType").change(function(){
                if($("#userType").val()==3){
                    $("input[name='customerAccountProfile.orderApproveFlag'][value='0']").prop("checked",true);
                    $("input[name='customerAccountProfile.orderApproveFlag']").prop("disabled",true);
                }else{
                    $("input[name='customerAccountProfile.orderApproveFlag'][value='1']").prop("checked",true);
                    $("input[name='customerAccountProfile.orderApproveFlag']").removeAttr('disabled');
                }
            });
        });
	</script>
</head>
<body>
<ul class="nav nav-tabs">
	<li><a href="${ctx}/md/customer/user/list">客户账号列表</a></li>
	<li class="active"><a href="javascript:;"><shiro:hasPermission name="md:customer:edit">${not empty user.id?'修改':'添加'}</shiro:hasPermission>
		<shiro:lacksPermission name="md:customer:edit">查看</shiro:lacksPermission>子账号</a></li>
</ul><br/>
<form:form id="inputForm" modelAttribute="user" action="${ctx}/md/customer/user/save" method="post" class="form-horizontal">
	<form:hidden path="id"/>
	<form:hidden path="customerAccountProfile.id"/>
	<sys:message content="${message}"/>
	<legend>基本信息</legend>
	<div class="control-group">
		<label class="control-label">客户:</label>
		<div class="controls">
			<c:choose>
				<c:when test="${currentuser.userType eq 3}">
					<form:hidden path="customerAccountProfile.customer.id" />
					<input id="customerName" name="customerAccountProfile.customer.name" type="text" class="span4" readonly="true" value="${user.customerAccountProfile.customer.name}" />
				</c:when>
				<c:otherwise>
					<%--<sys:treeselect id="customer" name="customerAccountProfile.customer.id" value="${user.customerAccountProfile.customer.id}"
									labelName="customerAccountProfile.customer.name"
									labelValue="${user.customerAccountProfile.customer.name}" title="客户" url="/md/customer/treeData"
									cssClass="span4 required" allowClear="false" />--%>
					<form:select path="customerAccountProfile.customer.id" cssClass="required" style="width:338px;">
						<form:option value="" label="请选择"/>
						<form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false" />
					</form:select>
					<span class="red">*</span>
				</c:otherwise>
			</c:choose>
		</div>
	</div>
	<div class="control-group">
		<label class="control-label">账号类型:</label>
		<div class="controls">
			<c:choose>
				<c:when test="${currentuser.userType eq 3}">
					<form:hidden path="userType" />
					<input id="userTypeName" name="userTypeName" type="text" class="span4" readonly="true" value="${fns:getDictLabel(user.getUserType(), 'sys_user_type', '')}" />
				</c:when>
				<c:otherwise>
					<form:select path="userType" cssClass="required" style="width:338px;">
						<form:option value="" label="请选择"/>
						<form:options items="${fns:getDictInclueListFromMS('sys_user_type','3,4')}" itemLabel="label" itemValue="value" htmlEscape="false" /><%-- 切换为微服务 --%>
					</form:select>
					<span class="red">*</span>
				</c:otherwise>
			</c:choose>
		</div>
	</div>
	<div class="control-group">
		<label class="control-label">登录名:</label>
		<div class="controls">
			<input id="oldLoginName" name="oldLoginName" type="hidden" value="${user.loginName}">
			<input type="text" id="loginName" name="loginName" maxlength="30" class="required" value="${user.loginName }" autocomplete="off" />
			<span class="red">*</span>
		</div>
	</div>
	<div class="control-group">
		<label class="control-label">姓名:</label>
		<div class="controls">
			<form:input path="name" htmlEscape="false" maxlength="10" class="required" /><span class="red">*</span>&nbsp;&nbsp;<span class="help-inline">子帐号名称前缀必须为客户名</span>
		</div>
	</div>
	<div class="control-group">
		<label class="control-label">密码:</label>
		<div class="controls">
			<input id="newPassword" name="newPassword" type="password" value="" maxlength="20" minlength="6" class=" ${empty user.id?'required':''}"/>
			<c:if test="${not empty user.id}"><span class="red">*</span>&nbsp;&nbsp;<span class="help-inline">若不修改密码，请留空。</span></c:if>
		</div>
	</div>
	<div class="control-group">
		<label class="control-label">确认密码:</label>
		<div class="controls">
			<input id="confirmNewPassword" name="confirmNewPassword" type="password" value="" maxlength="20" minlength="6" equalTo="#newPassword"/>
		</div>
	</div>
	<div class="control-group">
		<label class="control-label">手机:</label>
		<div class="controls">
<%--			<form:input path="mobile" htmlEscape="false" maxlength="11" class="mobile required" />--%>
			<input id="mobile" name="mobile" type="tel" htmlEscape="false" value="" maxlength="11" class=" ${empty user.id?'required mobile':'mobile'}" style="width: 206px;"/>

			<c:if test="${empty user.id}"><span class="red">*</span></c:if>
		</div>
	</div>
	<div class="control-group">
		<label class="control-label">电话:</label>
		<div class="controls">
<%--			<form:input path="phone" htmlEscape="false" maxlength="16" cssClass="phone"/>--%>
			<input id="phone" name="phone" htmlEscape="false" type="tel" value="" maxlength="16" class="phone" style="width: 206px;"/>
		</div>
	</div>
	<div class="control-group">
		<label class="control-label">邮箱:</label>
		<div class="controls">
			<form:input path="email" htmlEscape="false" maxlength="100" cssClass=" email" style="width:338px;"/>
		</div>
	</div>
	<div class="control-group">
		<label class="control-label">备注:</label>
		<div class="controls">
			<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="250" style="width:338px;"/>
		</div>
	</div>
	<legend>订单相关</legend>
	<div class="control-group">
		<label class="control-label">订单审核标识:</label>
		<div class="controls">
			<form:radiobuttons path="customerAccountProfile.orderApproveFlag" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" class="required"/><%--切换为微服务--%>
			<span class="red">*</span><span class="help-inline">是：该账号所开订单需主账号审核才能生效或开单超过设定时限自动生效；否：自动审核生效。</span>
		</div>
	</div>
	<legend>权限设定</legend>
	<div class="control-group">
		<label class="control-label">角色:</label>
		<div class="controls">
			<form:checkboxes path="roleIdList" items="${allRoles}" itemLabel="name" itemValue="id" htmlEscape="false"
							 class="required"/>
			<span class="help-inline"><font color="red">*</font> </span>
		</div>
	</div>
	<c:if test="${not empty user.id}">
		<legend>其它</legend>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">创建时间:</label>
					<div class="controls">
						<label class="lbl"><fmt:formatDate value="${user.createDate}" type="both" dateStyle="full"/></label>
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label">最后登陆:</label>
					<div class="controls">
						<label class="lbl">IP: ${user.loginIp}&nbsp;&nbsp;&nbsp;&nbsp;时间：<fmt:formatDate value="${user.loginDate}" type="both" dateStyle="full"/></label>
					</div>
				</div>
			</div>
		</div>
	</c:if>
	<div class="form-actions">
		<shiro:hasPermission name="md:customer:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
		<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
	</div>
</form:form>
<script type="text/javascript">
    $(window).load(function() {
        //解决一些浏览器自动将登录帐号覆盖现有控件
        setTimeout(resetform, 2000);
    });
    function resetform(){
        $("#loginName").val("${user.loginName}");
        $("#loginName").text("${user.loginName}");
        $("#newPassword").val("");
        top.$.jBox.closeTip();
    }
</script>
</body>
</html>