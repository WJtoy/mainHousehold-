<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>配件跟踪进度</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
</head>
<body>
<fieldset style="margin:4px 4px 4px 4px;">
    <%--<legend>${orderNo} 配件跟踪进度</legend>--%>
    <table id="treeTable" width="100%" class="table table-hover table-bordered table-condensed"
           style="margin-bottom: 0px;table-layout: fixed;">
        <thead>
        <tr>
            <th width="80">序号</th>
            <th width="130">跟踪日期</th>
            <th width="*">跟踪内容</th>
            <th width="80">操作人</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${logs}" var="log" varStatus="i" begin="0">
            <tr>
                <td>${i.index+1}</td>
                <td><fmt:formatDate value="${log.createDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                <td>${log.content}</td>
                <td>${log.createBy}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</fieldset>
</body>
</html>