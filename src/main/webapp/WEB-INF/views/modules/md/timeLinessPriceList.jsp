<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品分类时效奖励列表</title>
    <meta name="decorator" content="default"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script src="${ctxStatic}/js/fixtable.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript">
        $(document).ready(function () {
            var w = $(window).width();

            var pagestyle = function() {
                var width = $(window).width() -0;
                $("#treeTable_tableLayout").css("width",width);
            }

            //注册窗体改变大小事件
            $(window).resize(pagestyle);
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
        });

    </script>
    <style type="text/css">
        .col_product {width: 250px;}
        .col_command {width: 78px;}
        .table tbody td.error {background-color: #f2dede!important;}
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">价格列表</a>
    <shiro:hasPermission name="md:timelinessprice:edit">
        <li><a href="${ctx}/md/timeLinessPrice/forms">添加</a>
        </li>
    </shiro:hasPermission>
</ul>

<form:form id="searchForm" modelAttribute="productPrice" action="${ctx}/md/timeLinessPrice/list" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="btnSubmit" class="btn btn-primary"  type="submit" onclick="return setPage();" value="查询"/>
</form:form>
<sys:message content="${message}"/>
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50">序号</th>
        <th width="100">产品类别</th>
        <c:forEach items="${levelList}" var="dict">
            <th width="100">${dict.description}</th>
        </c:forEach>
        <shiro:hasPermission name="md:timelinessprice:edit">
        <th width="100">操作</th>
        </shiro:hasPermission>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${listmap}" var="entity">
        <c:set var="index" value="${index+1}" />
        <tr>
            <td>${index}</td>
            <td>
                <shiro:hasPermission name="md:timelinessprice:edit">
                <a href="${ctx}/md/timeLinessPrice/forms?category.id=${entity.categoryId}">
                    ${entity.categoryName}
                </a>
                </shiro:hasPermission>
                <shiro:lacksPermission name="md:timelinessprice:edit">
                    ${entity.categoryName}
                </shiro:lacksPermission>
            </td>
            <c:forEach items="${entity.timeLinessPriceList}" var="timelevelPrice">
                <td>${timelevelPrice.amount}</td>
            </c:forEach>
            <shiro:hasPermission name="md:timelinessprice:edit">
                <td>
                    <a href="${ctx}/md/timeLinessPrice/delete?categoryId=${entity.categoryId}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
                </td>
            </shiro:hasPermission>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>
