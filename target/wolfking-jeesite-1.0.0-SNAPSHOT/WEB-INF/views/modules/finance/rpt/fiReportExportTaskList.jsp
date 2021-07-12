<%@ taglib prefix="C" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>报表导出查询</title>
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
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">报表导出查询</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/finance/rpt/exportTask/list"
           method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
<%--        <label>报表类型：</label>--%>
<%--        <select id="reportType" name="reportType" class="input-small" style="width:160px;">--%>
<%--            <option value="0">所有</option>--%>
<%--            <c:forEach items="${allReportTypeList}" var="dict">--%>
<%--                <option value="${dict.value}" <c:out--%>
<%--                        value="${(dict.intValue eq rptSearchCondition.reportType)?'selected=selected':''}"/> >${dict.label}</option>--%>
<%--            </c:forEach>--%>
<%--        </select>--%>
        <label>报表名称：</label>
        <select id="reportId" name="reportId" class="input-small" style="width:160px;">
            <option value="0">所有</option>
            <c:forEach items="${allReportList}" var="dict">
                <option value="${dict.value}" <c:out
                        value="${(dict.intValue eq rptSearchCondition.reportId)?'selected=selected':''}"/> >${dict.label}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>导出时间：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.beginDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>

        <label>~</label>
        &nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:finance:download:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/></shiro:hasPermission>
        &nbsp;&nbsp;
    </div>
</form:form>
<sys:message content="${message}"/>
<div id="divGrid" style="overflow-x:hidden;">
    <table id="contentTable"
           class="table table-striped table-bordered table-condensed table-hover">
        <thead>
        <tr>
            <th>序号</th>
            <th>报表类型</th>
            <th>报表名称</th>
            <th>报表标题</th>
            <th>状态</th>
            <th>创建人</th>
            <th>创建时间</th>
            <th>下载次数</th>
            <th>最后下载人</th>
            <th>最后下载时间</th>
            <th>操作</th>

        </tr>
        </thead>
        <tbody>
        <c:set var="index" value="0"/>

        <c:forEach items="${page.list}" var="item">
            <c:set var="index" value="${index+1}"/>
            <tr>
                <td>${index+(page.pageNo-1)*page.pageSize}</td>
                <td>${item.reportTypeName}</td>
                <td>${item.reportName}</td>
                <td>${item.reportTitle}</td>
                <td>${item.statusLabel}</td>
                <td>${item.taskCreateByName}</td>
                <td>${fns:formatDateString(item.taskCreateDate)}</td>
                <td>
                    <c:if test="${item.downloadQty > 0}">
                        ${item.downloadQty}
                    </c:if>
                </td>
                <td>${item.lastDownloadByName}</td>
                <td>${fns:formatDateString(item.lastDownloadDate)}</td>
                <td>
                    <shiro:hasPermission name="rpt:finance:download:export">
                    <c:if test="${item.status >= 40}">
                        <a class="btn btn-success" href="${ctx}/finance/rpt/exportTask/downloadReportExcel?taskId=${item.id}&reportId=${item.reportId}">下载</a>
                    </c:if>
                    </shiro:hasPermission>
                </td>

            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>
