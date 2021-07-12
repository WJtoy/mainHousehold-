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

        var infoArr = [];
        var resultArr = [];
        var i = 0;
        <c:forEach items="${page.list}" var="item">
        infoArr[i] = '${item.infoJson}';
        resultArr[i] = '${item.resultJson}';
        i++;
        </c:forEach>

        function showInfoJson(index) {
            var json = infoArr[index];
            top.layer.open({
                title:'发送内容',
                content: json
            });
        }

        function showResultJson(index) {
            var json = resultArr[index];
            top.layer.open({
                title:'返回结果',
                content: json
            });
        }
    </script>

</head>
<body>
    <ul class="nav nav-tabs">
        <li>
            <a href="${ctx}/tmall/rpt/tmallorder/tmallOrderSUMReport">天猫</a>
        </li>
        <li><a href="${ctx}/b2b/rpt/processlog/canboorder">康宝</a></li>

        <li><a href="${ctx}/b2b/rpt/processlog/jdorder">京东</a></li>

        <li class="active"><a href="javascript:void(0);">康佳</a></li>

        <li><a href="${ctx}/b2b/rpt/processlog/joyoungorder">九阳</a></li>

        <li><a href="${ctx}/b2b/rpt/processlog/inseorder">樱雪</a></li>
</ul>
<form:form id="searchForm" modelAttribute="konkaSearchModel" action="${ctx}/b2b/rpt/processlog/konkaorder" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <div>
        <label>接口：</label>

        <select id="b2bInterfaceId" name="b2bInterfaceId" class="input-small" style="width:240px;">
            <option value="">所有</option>
            <c:forEach items="${b2bInterfaceId}" var="dict">
                <option value="${dict.value}" <c:out value="${(dict.value == konkaSearchModel.b2bInterfaceId)?'selected=selected':''}" /> >${dict.label}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>状 态：</label>
        <select id="processFlag" name="processFlag" class="input-small" style="width:225px;">
            <c:set var="processFlags" value="<%= com.wolfking.jeesite.ms.tmall.rpt.feign.B2BProcessFlag.values() %>"/>
            <option value="" <c:out value="${(empty konkaSearchModel.processFlag)?'selected=selected':''}" />>所有</option>
            <c:forEach var="type" items="${processFlags}">
                <option value="${type.value}" <c:out value="${(type.value == konkaSearchModel.processFlag)?'selected=selected':''}" /> >${type.label}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;

        <label>传送时间：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${konkaSearchModel.beginDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>

        <label>~</label>
        &nbsp;&nbsp;&nbsp;

        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${konkaSearchModel.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;&nbsp;


        <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
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
                <th>接口</th>
                <th>发送内容</th>
                <th>返回结果</th>
                <th>创建时间</th>
                <th>状态</th>
                <th>备注</th>
            </tr>
            </thead>
            <tbody>
            <c:set var="index" value="0"/>

            <c:forEach items="${page.list}" var="item">
                <c:set var="index" value="${index+1}"/>
                <tr>
                    <td>${index+(page.pageNo-1)*page.pageSize}</td>
                    <td>${item.interfaceName}</td>
                    <%--<td><a href="javascript:" data-toggle="tooltip"  data-tooltip='${item.infoJson}' >${fns:abbr(item.infoJson,40)}</a></td>--%>
                    <td><a href="javascript:showInfoJson(${index-1});" data-toggle="tooltip"  data-tooltip='${item.infoJson}' >${fns:abbr(item.infoJson,40)}</a></td>
                    <%--<td><a href="javascript:" data-toggle="tooltip"  data-tooltip='${item.resultJson}' >${fns:abbr(item.resultJson,40)}</a></td>--%>
                    <td><a href="javascript:showResultJson(${index-1});" data-toggle="tooltip"  data-tooltip='${item.resultJson}' >${fns:abbr(item.resultJson,40)}</a></td>
                    <jsp:useBean id="timestamp" class="java.util.Date"/>
                    <jsp:setProperty name="timestamp" property="time" value="${item.createDt}"/>
                    <td><fmt:formatDate value="${timestamp}" pattern="yyyy/MM/dd HH:mm:ss"/></td>
                    <td>${item.processFlag==0?'受理':item.processFlag==1?'执行':item.processFlag==2?'拒绝':item.processFlag==3?'失败':'成功'}</td>
                    <td><a href="javascript:" data-toggle="tooltip"  data-tooltip='${item.processComment}' >${fns:abbr(item.processComment,40)}</a></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
    <div class="pagination">${page}</div>
</body>
</html>
