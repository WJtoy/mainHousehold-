<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE HTML>
<html>
<head>
    <title>未完工汇总</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
<%--    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>--%>
<%--    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>--%>
    <style type="text/css">
        a:LINK { /**连接文字本身的颜色**/
            color: #333333
        }

        a:VISITED { /**连接文字被点击后的颜色**/
            color: #333333;
        }

        a:HOVER { /**鼠标移到连接文字上，文字的颜色**/
            color: #0000ff;
            text-decoration: underline;
        }

        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $("th").css({"text-align": "center", "vertical-align": "middle"});
            $("td").css({"vertical-align": "middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity: 'east'});

            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action", "${ctx}/rpt/provider/uncompletedOrderNew/uncompletedOrderNewReport");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/uncompletedOrderNew/checkExportTask?"+ (new Date()).getTime(),
                    data:$(searchForm).serialize(),
                    success: function (data) {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if(data && data.success == true){
                            top.$.jBox.closeTip();
                            top.$.jBox.confirm("确认要导出数据吗？", "系统提示", function (v, h, f) {
                                if (v == "ok") {
                                    top.$.jBox.tip('请稍候...', 'loading');
                                    $.ajax({
                                        type: "POST",
                                        url: "${ctx}/rpt/provider/uncompletedOrderNew/export?"+ (new Date()).getTime(),
                                        data:$(searchForm).serialize(),
                                        success: function (data) {
                                            if(ajaxLogout(data)){
                                                return false;
                                            }
                                            if(data && data.success == true){
                                                top.$.jBox.closeTip();
                                                top.$.jBox.tip(data.message, "success");
                                                $('#btnExport').removeAttr('disabled');
                                                return false;
                                            }
                                            else if( data && data.message){
                                                top.$.jBox.error(data.message,"导出错误");
                                            }
                                            else{
                                                top.$.jBox.error("导出错误","错误提示");
                                            }
                                            $('#btnExport').removeAttr('disabled');
                                            top.$.jBox.closeTip();
                                            return false;
                                        },
                                        error: function (e) {
                                            $('#btnExport').removeAttr('disabled');
                                            ajaxLogout(e.responseText,null,"导出错误，请重试!");
                                            top.$.jBox.closeTip();
                                        }
                                    });
                                }
                            }, {buttonsFocus: 1});
                            $('#btnExport').removeAttr('disabled');
                            top.$.jBox.closeTip();
                            return false;
                        }
                        else if( data && data.message){
                            top.$.jBox.error(data.message,"导出错误");
                        }
                        else{
                            top.$.jBox.error("导出错误","错误提示");
                        }
                        $('#btnExport').removeAttr('disabled');
                        top.$.jBox.closeTip();
                        return false;
                    },
                    error: function (e) {
                        $('#btnExport').removeAttr('disabled');
                        ajaxLogout(e.responseText,null,"导出错误，请重试!");
                        top.$.jBox.closeTip();
                    }
                });
                top.$('.jbox-body .jbox-icon').css('top', '55px');
            });
        });
    </script>
</head>

<body>
<ul class="nav nav-tabs">

    <li class="active"><a href="javascript:void(0);">未完工汇总</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }" />
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/uncompletedOrderNew/uncompletedOrderNewReport" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>截止日期：</label>
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;&nbsp;
        <label>季度：</label>
        <select id="quarter" name="quarter" class="input-small" style="width:125px;">
            <option value="" <c:out value="${(empty rptSearchCondition.quarter)?'selected=selected':''}"/>>所有
            </option>
            <c:forEach items="${quarters}" var="item">
                <option value="${item}" <c:out
                        value="${(rptSearchCondition.quarter eq item)?'selected=selected':''}"/>>${item}</option>
            </c:forEach>
        </select>
        <shiro:hasPermission name="rpt:uncompletedOrderNewReport:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询" /></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:uncompletedOrderNewReport:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出" /></shiro:hasPermission>
    </div>

</form:form>
<sys:message content="${message}" />

<%--<script type="text/javascript">--%>
<%--    $(document).ready(function() {--%>
<%--        var h = $(window).height();--%>
<%--        if($("#contentTable tbody>tr").length>0) {--%>
<%--            //无数据报错--%>
<%--            var w = $(window).width();--%>
<%--            $("#contentTable").toSuperTable({--%>
<%--                width: w-10,--%>
<%--                height: h - 190,--%>
<%--                fixedCols: 1,--%>
<%--                headerRows: 3,--%>
<%--                colWidths:--%>
<%--                    [40,--%>
<%--                        140, 140, 120,--%>
<%--                        140, 140,80, 120, 100, 100, 80, 100, 200, 80, 110, 200,--%>
<%--                        100,--%>
<%--                        100, 100,--%>
<%--                        60, 200],--%>
<%--                onStart: function () {--%>
<%--                },--%>
<%--                onFinish: function () {--%>
<%--                }--%>
<%--            });--%>
<%--        }--%>
<%--        else {--%>
<%--            $("#divGrid").css("height", h-190);--%>
<%--        }--%>

<%--    });--%>
<%--</script>--%>

<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table table-bordered table-condensed table-hover" style="table-layout:fixed; margin-top: 0px;border-top-width: 0px;">
        <thead>
        <%-- 第一列用于固定表格列的宽度，并且该列隐藏不显示 --%>
        <tr style="height: 4px; border-width: 0px; padding: 0px; margin: 0px;visibility: hidden;">
            <th width="40"></th>

            <th width="140"></th>
            <th width="140"></th>
            <th width="80"></th>

            <th width="80"></th>
        </tr>
        <tr>

            <th width="40">序号</th>
            <th width="140">客户编号</th>
            <th width="140">客户名称</th>
            <th width="80">业务员</th>

            <th width="80">未完单量</th>
        </tr>
        </thead>
        <tbody>

        <c:set var="totalQty" value="0" />

        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${page.list}" var="orderMaster">
            <tr>
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <td >${rowIndex}</td>
            <td >${orderMaster.customer == null ? "" : orderMaster.customer.code}</td>
            <td >${orderMaster.customer == null ? "" : orderMaster.customer.name}</td>
            <td >${orderMaster.salesName}</td>

            <td >${orderMaster.uncompletedQty}</td>
                <c:set var="totalQty" value="${totalQty +orderMaster.uncompletedQty }" />
            </tr>
        </c:forEach>

        <c:if test="${page.list.size()>0}">
            <tr>
                <td colspan="3"></td>
                <td><B>合计</B></td>
                <td style="color:red;"><B>${totalQty}</B></td>
            </tr>
        </c:if>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>
