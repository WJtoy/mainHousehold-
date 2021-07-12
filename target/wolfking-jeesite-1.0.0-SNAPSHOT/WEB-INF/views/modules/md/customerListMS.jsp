<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客戶管理</title>
    <meta name="decorator" content="default" />
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
        <script type="text/javascript">
            function enableCustomer(customerId,name){
                var loadingIndex;
                $.ajax({
                    url: "${ctx}/md/customerNew/selectCustomerOrder?id="+ customerId,
                    success:function (data) {
                        // 提交后的回调函数
                        if (data.success) {
                            layer.confirm(
                                '确认要删除客户<label style="color:#63B9E6">'+name +'</label>吗？',
                                {
                                    btn: ['确定','取消'], //按钮
                                    title:'提示',
                                }, function(index){
                                    layer.close(index);//关闭本身
                                    loadingIndex = top.layer.msg('正在删除，请稍等...', {
                                        icon: 16,
                                        time: 0,//不定时关闭
                                        shade: 0.3
                                    });
                                    $.ajax({
                                        url: "${ctx}/md/customerNew/ajax/delete?id="+ customerId,
                                        success:function (data) {
                                            // 提交后的回调函数
                                            if(loadingIndex) {
                                                setTimeout(function () {
                                                    // layer.close(loadingIndex);
                                                }, 2000);
                                            }
                                            if (data.success) {
                                                layerMsg(data.message);

                                                layerMsg("删除" + name + "成功");

                                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                                if(pframe){
                                                    pframe.repage();
                                                }
                                            } else {
                                                layerError("删除"+name+"失败:" + data.message, "错误提示");
                                            }
                                            return false;
                                        },
                                        error: function (data) {
                                            layer.close(loadingIndex);
                                            ajaxLogout(data,null,"数据保存错误，请重试!");
                                        }
                                    });
                                    return false;
                                });
                        } else {
                            layerAlert("客户<label style=\"color:#63B9E6\">"+name +"</label>已有工单，不能删除", "提示");
                        }
                        return false;
                    },
                    error: function (data) {

                        ajaxLogout(data,null,"数据删除错误，请重试!");
                    }
                });
            }
        </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:;">客戶列表</a></li>
    <shiro:hasPermission name="md:customer:edit">
        <li><a href="${ctx}/md/customer/form?sort=10">客戶添加</a></li>
    </shiro:hasPermission>
</ul>
<sys:message content="${message}" />
<shiro:hasPermission name="md:customer:view">
<form:form id="searchForm" modelAttribute="mdCustomer" action="${ctx}/md/customer" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
<%--    <label>代码：</label>--%>
<%--    <input id="code" name="code" type="text" htmlEscape="false" maxlength="30" value="${mdCustomer.code}"/>--%>
    <label>名称：</label>
    <input id="name" name="name" type="text" htmlEscape="false" maxlength="60" class="required" value="${mdCustomer.name}"/>
    <label>负责人电话：</label>
    <form:input path="phone" htmlEscape="false" type="number" maxlength="20" class="input-small" />
    <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
</form:form>
<table id="contentTable"
       class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th>序号</th>
        <th>代码</th>
        <th>名称</th>
        <th>负责人</th>
<%--        <th>手机</th>--%>
        <th>邮件</th>
        <th>技术人员</th>
        <th>技术人员电话</th>
        <th>默认品牌</th>
        <th>可下单</th>
        <th>短信发送</th>
        <th style="width:20%">描述</th>
        <shiro:hasPermission name="md:customer:edit">
            <th>操作</th>
        </shiro:hasPermission>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${page.list}" var="mdCustomer">
        <c:set var="index" value="${index+1}" />
        <tr>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${mdCustomer.code}</td>
            <td>${mdCustomer.name}</td>
            <td>${mdCustomer.master}</td>
<%--            <td>${mdCustomer.phone}</td>--%>
            <td>${mdCustomer.email}</td>
            <td>${mdCustomer.technologyOwner}</td>
            <td>${mdCustomer.technologyOwnerPhone}</td>
            <td>${mdCustomer.defaultBrand}</td>
            <td>${mdCustomer.effectFlag==0?'否':'是'}</td>
            <td>${mdCustomer.shortMessageFlag==1?'发送':'不发送'}</td>
            <td><a href="javascript:void(0);" title="${mdCustomer.remarks}">${fns:abbr(mdCustomer.remarks,40)}</a></td>
            <shiro:hasPermission name="md:customer:edit">
                <td><a href="${ctx}/md/customer/form?id=${mdCustomer.id}">修改</a>
                    <shiro:hasPermission name="md:customer:detelete">
                        <a href="#" onclick="enableCustomer('${mdCustomer.id}','${mdCustomer.name}')">删除</a>
                    </shiro:hasPermission>
                </td>
            </shiro:hasPermission>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</shiro:hasPermission>
</body>
</html>
