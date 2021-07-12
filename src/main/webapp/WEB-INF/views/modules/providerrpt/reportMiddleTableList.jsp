<%@ taglib prefix="C" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>报表导出查询</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">报表中间表重建</a></li>
</ul>
<sys:message content="${message}"/>
<div id="divGrid" style="overflow-x:hidden;">
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
        <thead>
        <tr>
            <th rowspan="2">序号</th>
            <th rowspan="2">名称</th>
            <th rowspan="2">表名</th>
            <th colspan="4">操作</th>
            <th rowspan="2">日志</th>
        </tr>
        <tr>
            <th>新增</th>
            <th>补漏</th>
            <th>更新</th>
            <th>删除</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="index" value="0"/>

        <c:forEach items="${list}" var="item">
            <c:set var="index" value="${index+1}"/>
            <tr>
                <td>${index}</td>
                <td>${item.label}</td>
                <td>${item.table}</td>
                <td><a class="btn btn-primary" href="javascript:void(0);" onclick="rebuildMidTable(${item.value}, 10,'${item.label}', '新增');">新增</a></td>
                <td><a class="btn btn-info" href="javascript:void(0);" onclick="rebuildMidTable(${item.value}, 15,'${item.label}', '补漏');">补漏</a></td>
                <td><a class="btn btn-warning" href="javascript:void(0);" onclick="rebuildMidTable(${item.value}, 20,'${item.label}', '更新');">更新</a></td>
                <td><a class="btn btn-danger" href="javascript:void(0);" onclick="rebuildMidTable(${item.value}, 30,'${item.label}', '删除');">删除</a></td>
                <td><a class="btn btn-success" href="javascript:void(0);" onclick="showRebuildMidTableLogList(${item.value});">日志</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<script type="text/javascript">
    function rebuildMidTable(midTableId, operationType, midTableName, operationTypeName){
        var self = this;
        var pendingIndex = top.layer.open({
            type: 2,
            id:'layer_rebuildMidTableForm',
            zIndex: 19891015,
            title:'重建中间表 ['+ operationTypeName + ' - ' +midTableName+']',
            content: "${ctx}/rpt/provider/rebuildMiddleTableTask/rebuildForm?midTableId="+ midTableId + "&operationType=" + operationType,
            area: ['550px', '400px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    }

    function showRebuildMidTableLogList(midTableId){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        var pendingIndex = top.layer.open({
            type: 2,
            id:'layer_rebuildMidTableLogList',
            zIndex: 19891016,
            title:'重建中间表日志',
            content: "${ctx}/rpt/provider/rebuildMiddleTableTask/logList?middleTableId="+ midTableId,
            area: [(w-40)+'px',(h-40)+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    }
</script>
</body>
</html>
