<%@ taglib prefix="C" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>重建中间表日志</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $("th").css({"text-align": "center", "vertical-align": "middle"});
            $("td").css({"vertical-align": "middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity: 'east'});

            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").submit();
            });
        });
    </script>
</head>

<body>
<form:form id="searchForm" action="${ctx}/rpt/provider/rebuildMiddleTableTask/logList"
           method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="middleTableId" name="middleTableId" type="hidden" value="${middleTableId}">
</form:form>
<div id="divGrid">
    <table id="contentTable"
           class="table table-striped table-bordered table-condensed table-hover">
        <thead>
        <tr>
            <th>序号</th>
            <th>中间表名</th>
            <th>中间表</th>
            <th>操作</th>
            <th>开始时间</th>
            <th>结束时间</th>
            <th>年份</th>
            <th>月份</th>
            <th>状态</th>
            <th>任务创建时间</th>
            <th>任务完成时间</th>

        </tr>
        </thead>
        <tbody>
        <c:set var="index" value="0"/>

        <c:forEach items="${page.list}" var="item">
            <c:set var="index" value="${index+1}"/>
            <tr>
                <td>${index+(page.pageNo-1)*page.pageSize}</td>
                <td>${item.middleTableEnum.label}</td>
                <td>${item.middleTableEnum.table}</td>
                <td>${item.operationTypeEnum.label}</td>
                <td>${fns:formatDateString(item.beginDate)}</td>
                <td>${fns:formatDateString(item.endDate)}</td>
                <td>${item.selectedYear}</td>
                <td>${item.selectedMonth}</td>
                <td>${item.statusEnum.name}</td>
                <td>${fns:formatDateString(item.taskCreateDate)}</td>
                <td>${fns:formatDateString(item.taskCompleteDate)}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>
