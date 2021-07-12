<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>用户管理</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript">
        $(document).ready(function () {
            <%--$("#btnExport").click(function () {--%>
                <%--top.$.jBox.confirm("确认要导出用户数据吗？", "系统提示", function (v, h, f) {--%>
                    <%--if (v == "ok") {--%>
                        <%--$("#searchForm").attr("action", "${ctx}/sys/user/export");--%>
                        <%--$("#searchForm").submit();--%>
                    <%--}--%>
                <%--}, {buttonsFocus: 1});--%>
                <%--top.$('.jbox-body .jbox-icon').css('top', '55px');--%>
            <%--});--%>
            $("#btnImport").click(function () {
                $.jBox($("#importBox").html(), {
                    title: "导入数据", buttons: {"关闭": true},
                    bottomText: "导入文件不能超过5M，仅允许导入“xls”或“xlsx”格式文件！"
                });
            });
        });
        function search() {
            $("#pageNo").val(1);
            $("#searchForm").attr("action", "${ctx}/sys/user/list");
            $("#searchForm").submit();
            return false;
        }

        function exportUser(pageNo){
            pageNo = pageNo || 'all';
            top.$.jBox.confirm("确认要导出用户数据吗？", "系统提示", function (v, h, f) {
                if (v == "ok") {
                    $("#searchForm").attr("action", "${ctx}/sys/user/export?pageNo=" + (pageNo || ''));
                    $("#searchForm").submit();
                    $("#searchForm").attr("action", "${ctx}/sys/user/list");
                }
            }, {buttonsFocus: 1});
            top.$('.jbox-body .jbox-icon').css('top', '55px');
        }
    </script>
</head>
<body>
<div id="importBox" class="hide">
    <form id="importForm" action="${ctx}/sys/user/import" method="post" enctype="multipart/form-data"
          class="form-search" style="padding-left:20px;text-align:center;" onsubmit="loading('正在导入，请稍等...');"><br/>
        <input id="uploadFile" name="file" type="file" style="width:330px"/><br/><br/>　　
        <input id="btnImportSubmit" class="btn btn-primary" type="submit" value="   导    入   "/>
        <a href="${ctx}/sys/user/import/template">下载模板</a>
    </form>
</div>
<ul class="nav nav-tabs">
    <li class="active"><a href="${ctx}/sys/user/list">用户列表</a></li>
    <shiro:hasPermission name="sys:user:edit">
        <li><a href="${ctx}/sys/user/form">用户添加</a></li>
    </shiro:hasPermission>
</ul>
<form:form id="searchForm" modelAttribute="user" action="${ctx}/sys/user/list" method="post"
           class="form-inline">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <sys:tableSort id="orderBy" name="orderBy" value="${page.orderBy}" callback="page();"/>
    <div class="control-group">
        <label class="label-search">归属公司：</label>&nbsp;
        <sys:treeselect id="company" name="company.id" value="${user.company.id}"
                        labelName="company.name" labelValue="${user.company.name}"
                        title="公司" url="/sys/office/treeData?type=1" cssClass="input-small"
                        allowClear="true"/>
        <label>登录名：</label>
        <form:input path="loginName" htmlEscape="false" maxlength="50" class="input-medium"/>
        <label>归属部门：</label>
        <sys:treeselect id="office" name="office.id" value="${user.office.id}"
                        labelName="office.name" labelValue="${user.office.name}"
                        title="部门" url="/sys/office/treeData?type=2" cssClass="input-small"
                        allowClear="true" notAllowSelectParent="true" nodesLevel="3"/>
        <label>姓&nbsp;&nbsp;&nbsp;名：</label>
        <form:input path="name" htmlEscape="false" maxlength="50" class="input-medium"/>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="button" onclick="search();" value="查询"/>
            <%--<input id="btnExport" class="btn btn-primary" type="button" value="导出"/>--%>
        <div class="btn-group">
            <button class="btn btn-primary" onclick="javascript:return false;">导出</button>
            <button class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
                <span class="caret"></span>
            </button>
            <ul class="dropdown-menu" style="min-width: 100px;width: 100px;">
                <li><a href="#" onclick="exportUser('${page.pageNo}');">当前页</a></li>
                <li><a href="#" onclick="exportUser('all');">所有</a></li>
            </ul>
        </div>
        <input id="btnImport" class="btn btn-primary" type="button" value="导入"/>
    </div>
</form:form>
<sys:message content="${message}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th>序号</th>
        <th>归属公司</th>
        <th>归属部门</th>
        <th class="sort-column login_name">登录名</th>
        <th class="sort-column name">姓名</th>
        <th>电话</th>
        <th>QQ</th>
        <th>手机</th>
        <th>角色</th>
        <th>最近登录IP</th>
        <th>最近登录日期</th>
        <shiro:hasPermission name="sys:user:edit">
        <th>操作</th>
        </shiro:hasPermission>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${page.list}" var="user">
        <c:set var="index" value="${index+1}" />
        <tr>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${user.company.name}</td>
            <td>${user.office.name}</td>
            <td><a href="${ctx}/sys/user/form?id=${user.id}">${user.loginName}</a></td>
            <td>${user.name}</td>
            <td>${user.phone}</td>
            <td>${user.qq}</td>
            <td>${user.mobile}</td>
            <td>${user.roleNames}</td>
            <td>${user.loginIp}</td>
            <td><fmt:formatDate value="${user.loginDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            <shiro:hasPermission name="sys:user:edit">
                <td>
                    <a href="${ctx}/sys/user/form?id=${user.id}">修改</a>
                    <a href="${ctx}/sys/user/delete?id=${user.id}"
                       onclick="return confirmx('确认要删除该用户吗？', this.href)">删除</a>
                </td>
            </shiro:hasPermission>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>