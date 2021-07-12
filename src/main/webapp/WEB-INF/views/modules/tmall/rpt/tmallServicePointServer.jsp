<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<html>
<head>
    <title>O</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <style>
        .demo {display: inline-block;*display: inline;*zoom: 1;width: 140px;height: 20px;line-height: 20px;font-size: 12px;overflow: hidden;-ms-text-overflow: ellipsis;text-overflow: ellipsis;white-space: nowrap;}
        .demo:hover {height: auto;white-space: normal;}
    </style>
    <script type="text/javascript" language="javascript">
        $(document).ready(function() {
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});

        });
    </script>

</head>
<body>
<ul class="nav nav-tabs">
    <li>
        <a href="${ctx}/tmall/rpt/tmallorder/tmallOrderSUMReport">工单</a>
    </li>
    <li>
        <a href="${ctx}/tmall/rpt/tmallorder/tmallServicePointInfo">网点信息</a>
    </li>
    <li class="active"><a href="javascript:void(0);">网点服务</a></li>
    <li>
        <a href="${ctx}/tmall/rpt/tmallorder/tmallCapacity">容量</a>
    </li>
    <li>
        <a href="${ctx}/tmall/rpt/tmallorder/tmallWorker">工人</a>
    </li>
</ul>
<form:form id="searchForm" modelAttribute="processlogSearchModel" action="${ctx}/tmall/rpt/tmallorder/tamllServicePointServer" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <div>

        <label>状 态：</label>
        <select id="processFlag" name="processFlag" class="input-small" style="width:225px;">
            <c:set var="processFlags" value="<%= com.wolfking.jeesite.ms.tmall.rpt.feign.B2BProcessFlag.values() %>"/>
            <option value="" <c:out value="${(empty processlogSearchModel.processFlag)?'selected=selected':''}" />>所有</option>
            <c:forEach var="type" items="${processFlags}">
                <option value="${type.value}" <c:out value="${(type.value == processlogSearchModel.processFlag)?'selected=selected':''}" /> >${type.label}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>操作类型：</label>
        <select id="actionType" name="actionType" class="input-small" style="width:225px;">
            <c:set var="actionTypes" value="<%= com.wolfking.jeesite.ms.tmall.rpt.feign.B2BActionType.values() %>"/>
            <option value="" <c:out value="${(empty processlogSearchModel.actionType)?'selected=selected':''}" />>所有</option>
            <c:forEach var="type" items="${actionTypes}">
                <option value="${type.value}" <c:out value="${(type.value == processlogSearchModel.actionType)?'selected=selected':''}" /> >${type.label}</option>
            </c:forEach>

        </select>
        &nbsp;&nbsp;
        <label>传送时间：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${processlogSearchModel.createDateStart}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>

        <label>~</label>
        &nbsp;&nbsp;&nbsp;

        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${processlogSearchModel.createDateEnd}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;&nbsp;

        <label>处理时间：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${processlogSearchModel.updateDateStart}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>
        &nbsp;&nbsp;&nbsp;

        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${processlogSearchModel.updateDateEnd}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;&nbsp;

        <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
        &nbsp;&nbsp;
            <%--<input id="btnExport" class="btn btn-primary" type="button" value="导出" />--%>
    </div>

</form:form>
<sys:message content="${message}"/>
<script type="text/javascript">
    $(document).ready(function() {
        var h = $(window).height();
        if($("#contentTable tbody>tr").length>0) {
            //无数据报错

            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w-10,
                height: h - 138,
                fixedCols: 1,
                headerRows: 3,
                colWidths:
                    [70,
                        300, 150, 150, 150,
                        150, 300,100
                    ],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
        else {
            $("#divGrid").css("height", h-138);
        }

    });
</script>
<div id="divGrid" style="overflow-x:hidden;">
    <table id="contentTable" class="table table-bordered table-condensed " style="table-layout:fixed;">
        <thead>
        <tr>
            <th width="70">序号</th>
            <th width="300">内容</th>
            <th width="150">操作类型</th>
            <th width="150">传送时间</th>
            <th width="150">处理时间</th>
            <th width="150">状态</th>
            <th width="300">备注</th>
            <th width="100">次数</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="index" value="0"/>
        <c:forEach items="${page.list}" var="item">
            <c:set var="index" value="${index+1}"/>
            <tr>
                <td>${index+(page.pageNo-1)*page.pageSize}</td>
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip='${item.content}' >${fns:abbr(item.content,40)}</a></td>

                <td>${item.actionType==0?'新增':item.actionType==1?'更新':'删除'}</td>
                <td><fmt:formatDate value="${item.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td><fmt:formatDate value="${item.updateDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td>${item.processFlag==0?'受理':item.processFlag==1?'执行':item.processFlag==2?'拒绝':item.processFlag==3?'失败':'成功'}</td>
                <td>${item.processComment}</td>
                <td><fmt:formatDate value="${item.processTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>
