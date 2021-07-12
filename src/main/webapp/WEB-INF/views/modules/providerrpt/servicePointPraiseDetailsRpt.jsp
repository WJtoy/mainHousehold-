<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <title>网点好评明细</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
<%--    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>--%>
<%--    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>--%>
    <script src="${ctxStatic}/area/Area-1.3.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }

    </style>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#btnSubmit").click(function() {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action","${ctx}/rpt/provider/servicePointPraiseDetails/servicePointPraiseDetailsRptData");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/servicePointPraiseDetails/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/servicePointPraiseDetails/export?"+ (new Date()).getTime(),
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
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        .sBase{z-index: 2}
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">网点好评</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/servicePointPraiseDetails/servicePointPraiseDetailsRptData" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>创建时间：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:99px;margin-left:4px"
               maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.beginDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>
        &nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20"
               class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;&nbsp;
        <label>状态：</label>
        <select id="status" name="status" class="input-small" style="width:125px;">
            <option value="0" <c:out value="${(empty rptSearchCondition.status)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${praiseStatusEnumList}" var="dict">
                <option value="${dict.value}" <c:out
                        value="${(rptSearchCondition.status eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询" />
        &nbsp;&nbsp;
        <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
    </div>

</form:form>
<sys:message content="${message}"/>
<%--<script type="text/javascript">--%>
<%--    $(document).ready(function() {--%>
<%--        if($("#contentTable tbody>tr").length>0) {--%>
<%--            //无数据报错--%>
<%--            var h = $(window).height();--%>
<%--            var w = $(window).width();--%>
<%--            $("#contentTable").toSuperTable({--%>
<%--                width: w-10,--%>
<%--                height: h - 248,--%>
<%--                fixedCols: 2,--%>
<%--                headerRows: 1,--%>
<%--                colWidths:--%>
<%--                    [60,--%>
<%--                        140, 140, 140, 100, 140, 100,--%>
<%--                        100, 200, 100],--%>
<%--                onStart: function () {--%>

<%--                },--%>
<%--                onFinish: function () {--%>

<%--                }--%>
<%--            });--%>
<%--        }--%>
<%--        else {--%>
<%--            var h = $(window).height();--%>
<%--            $("#divGrid").height(h-248);--%>
<%--        }--%>
<%--    });--%>
<%--</script>--%>
<div id="divGrid" style="overflow-x:auto;">
    <table id="contentTable" class="table table-bordered table-condensed table-hover " style="table-layout:fixed " cellspacing="0">
        <thead>
        <tr>
            <th width="10">序号</th>

            <th width="140">工单单号</th>
            <th width="80">状态</th>
            <th width="100">创建时间</th>
            <th width="140">区域</th>
            <th width="40">用户姓名</th>

            <th width="100">用户电话</th>
            <th width="200">用户地址</th>
            <th width="40">师傅</th>

            <th width="30">好评费</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="totalPriaseFee" value="0"/>
        <c:set var="i" value="0"/>
        <c:forEach items="${page.list}" var="item">
            <c:set var="i" value="${i+1}"/>
            <tr>
                <td>${i}</td>
                <td>${item.orderNo}</td>
                <c:choose>
                    <c:when test="${item.status.value == '10' || item.status.value == '20'}">
                        <td><span class="badge badge-info" >${item.status.label}</span></td>
                    </c:when>
                    <c:when test="${item.status.value == '30'}">
                        <td ><span class="badge badge-important">${item.status.label}</span></td>
                    </c:when>
                    <c:when test="${item.status.value == '40'}">
                        <td><span  class="badge badge-success">${item.status.label}</span></td>
                    </c:when>
                    <c:when test="${item.status.value == '50' || item.status.value == '60'}">
                        <td><span class="badge">${item.status.label}</span></td>
                    </c:when>
                    <c:otherwise>
                        <td><span>${item.status.label}</span></td>
                    </c:otherwise>
                </c:choose>
                <td ><fmt:formatDate value="${item.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td>${item.areaName}</td>
                <td>${item.userName}</td>
                <td>${item.userPhone}</td>
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.userAddress}">${fns:abbr(item.userAddress,40)}</a></td>
                <td>${item.engineerName}</td>
                <c:choose>
                    <c:when test="${item.status.value >= '40'}">
                        <td>${item.praiseFee}</td>
                        <c:set var="totalPriaseFee" value="${totalPriaseFee+item.praiseFee}"/>
                    </c:when>
                    <c:otherwise>
                        <td>${item.applyPraiseFee}</td>
                        <c:set var="totalPriaseFee" value="${totalPriaseFee+item.applyPraiseFee}"/>
                    </c:otherwise>
                </c:choose>
            </tr>
        </c:forEach>
        <c:if test="${page.list.size()>0}">
            <tr style="font-weight: bold; color: red;">
                <td colspan="8"></td>
                <td>合计:</td>
                <td><fmt:formatNumber value="${totalPriaseFee}" pattern="0.00"/></td>
            </tr>
        </c:if>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>



