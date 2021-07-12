<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE HTML>
<html>
<head>
    <title>O</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <style>
        .demo {
            display: inline-block;
            *display: inline;
            *zoom: 1;
            width: 140px;
            height: 20px;
            line-height: 20px;
            font-size: 12px;
            overflow: hidden;
            -ms-text-overflow: ellipsis;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .demo:hover {
            height: auto;
            white-space: normal;
        }
    </style>
    <script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $("th").css({"text-align": "center", "vertical-align": "middle"});
            $("td").css({"vertical-align": "middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity: 'east'});
        });

        <%--var infoArr = [];--%>
        <%--var resultArr = [];--%>
        <%--var i = 0;--%>
        <%--<c:forEach items="${page.list}" var="item">--%>
        <%--infoArr[i] = '${item.infoJson}';--%>
        <%--resultArr[i] = '${item.resultJson}';--%>
        <%--i++;--%>
        <%--</c:forEach>--%>

        // function showInfoJson(index) {
        //
        //     var json = infoArr[index];
        //
        //     top.layer.open({
        //         title: '发送内容',
        //         content: json
        //     });
        // }
        //
        // function showResultJson(index) {
        //     var json = resultArr[index];
        //     top.layer.open({
        //         title: '返回结果',
        //         content: json
        //     });
        // }

        function kegCompletedRetry(id) {
            var self = this;
            top.layer.open({
                type: 2,
                id: 'layer_keg_comlete_retry',
                zIndex: 19891015,
                title: '韩电完工重发',
                content: "${ctx}/b2b/rpt/processlog/kegFailLogRetryForm?id=" + id,
                area: ['940px', screen.height - 200 + 'px'],
                shade: 0.3,
                shadeClose: true,
                maxmin: false,
                success: function (layero, index) {
                }
            });
        }

    </script>

</head>
<body>
<ul class="nav nav-tabs">


    <li class="active"><a href="javascript:void(0);">韩电</a></li>

</ul>
<form:form id="searchForm" modelAttribute="searchModel" action="${ctx}/b2b/rpt/processlog/kegFailLog"
           method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div>
        <label class="label-search">工单号：</label>&nbsp;
        <input type=text class="input-small" id="orderNo" name="orderNo" value="${searchModel.orderNo}"
               maxlength="20"/>
        &nbsp;&nbsp;
        <label>传送时间：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:99px;margin-left:4px"
               maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${searchModel.beginDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>

        <label>~</label>
        &nbsp;&nbsp;&nbsp;

        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20"
               class="input-small Wdate"
               value="<fmt:formatDate value='${searchModel.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;&nbsp;

        <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>
        &nbsp;&nbsp;
            <%--<input id="btnExport" class="btn btn-primary" type="button" value="导出" />--%>
    </div>

</form:form>
<sys:message content="${message}"/>

<div id="divGrid" style="overflow-x:hidden;">
    <table id="contentTable"
           class="table table-striped table-bordered table-condensed table-hover">
        <thead>
        <tr>
            <th>序号</th>
            <th>工单号</th>
            <th>姓名</th>
            <th>电话</th>
            <th>地址</th>
            <th>时间</th>
            <th>备注</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="index" value="0"/>

        <c:forEach items="${page.list}" var="item">
            <c:set var="index" value="${index+1}"/>
            <tr>
                <td>${index+(page.pageNo-1)*page.pageSize}</td>
                <td><a style="line-height: 30px" class="input-block-level" href="javascript:void(0);"
                                                       onclick="kegCompletedRetry('${item.id}')"
                                                       data-toggle="tooltip">${fns:abbr(item.appBillID,40)}</a>
                </td>
                <td>${item.name}</td>
                <td>${item.mobile}</td>
                    <%--<td><a href="javascript:" data-toggle="tooltip"  data-tooltip='${item.infoJson}' >${fns:abbr(item.infoJson,40)}</a></td>--%>

<%--                <td>--%>
<%--                    <c:choose>--%>
<%--                        <c:when test="${searchModel.b2bInterfaceId eq 5007}">--%>
<%--                            <a style="line-height: 30px" class="input-block-level" href="javascript:void(0);"--%>
<%--                               onclick="joyoungCompletedRetry('${item.id}','${searchModel.b2bInterfaceId}')"--%>
<%--                               data-toggle="tooltip">${fns:abbr(item.infoJson,40)}</a>--%>
<%--                        </c:when>--%>
<%--                        <c:when test="${searchModel.b2bInterfaceId eq 5006}">--%>
<%--                            <a style="line-height: 30px" class="input-block-level" href="javascript:void(0);"--%>
<%--                               onclick="joyoungCancelRetry('${item.id}','${searchModel.b2bInterfaceId}')"--%>
<%--                               data-toggle="tooltip">${fns:abbr(item.infoJson,40)}</a>--%>
<%--                        </c:when>--%>
<%--                    </c:choose>--%>

<%--                </td>--%>
                <td><a href="javascript:" data-toggle="tooltip"
                       data-tooltip='${item.address}'>${fns:abbr(item.address,20)}</a></td>
                <jsp:useBean id="timestamp" class="java.util.Date"/>
                <jsp:setProperty name="timestamp" property="time" value="${item.createDate}"/>
                <td><fmt:formatDate value="${timestamp}" pattern="yyyy/MM/dd HH:mm:ss"/></td>
                <td><a href="javascript:" data-toggle="tooltip"
                       data-tooltip='${item.processComment}'>${fns:abbr(item.processComment,40)}</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>
