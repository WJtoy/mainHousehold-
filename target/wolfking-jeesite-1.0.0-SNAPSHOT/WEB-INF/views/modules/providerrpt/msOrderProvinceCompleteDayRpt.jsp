<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>省每日完成报表</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        .parent:after{
            content:"";
            height:0;
            line-height:0;
            display:block;
            visibility:hidden;
            clear:both;
        }

        .target{
            display:none;
            z-index: 4;
        }

        .triggle:hover + .target {
            display: block;
        }

        .border{
            display: none;
            opacity: 0.8;
            width: 0 !important;
            border-bottom:solid 12px #1B1E24;
            border-left:12px solid transparent;
            border-right: 6px solid transparent;
            boder-top: 0px solid transparent;
        }
    </style>
    <script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $(".triggle").on('hover', function(){
                $(".border").css({
                    display:"block"
                })
            })
            $(".triggle").on('mouseleave', function(){
                $(".border").css({
                    display:"none"
                })
            })
            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action", "${ctx}/rpt/provider/gradedOrder/provinceCompleteOrderRpt");
                $("#searchForm").submit();
            });
            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/gradedOrder/areaCompletedCheckExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/gradedOrder/provinceCompletedExport?"+ (new Date()).getTime(),
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
    <li class="active"><a href="javascript:void(0);">省每日完成统计</a></li>
    <li>
        <a href="${ctx}/rpt/provider/gradedOrder/cityCompleteOrderRpt">市每日完成统计</a>
    </li>
    <li>
        <a href="${ctx}/rpt/provider/gradedOrder/areaCompleteOrderRpt">区/县每日完成统计</a>
    </li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="searchForm" modelAttribute="rptSearchCondition"
           action="${ctx}/rpt/provider/gradedOrder/provinceCompleteOrderRpt" method="post" class="breadcrumb form-search">
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>区域：</label>
        <sys:treeselectareanew id="area" name="areaId" value="${rptSearchCondition.areaId}"
                        levelValue="${rptSearchCondition.areaLevel}" nodeLevel="true"
                        labelName="areaName" labelValue="${rptSearchCondition.areaName }" title="区域"
                        url="/sys/area/treeDataNew?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1"
                        nameLevel="3"/>
        &nbsp;&nbsp;
        <label>服务品类：</label>
        <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
            <option value="0" <c:out value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${productCategoryList}" var="dict">
                <option value="${dict.id}" <c:out value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>年份：</label>
        <select id="selectedYear" name="selectedYear" class="input-small" style="width:85px;">
            <c:forEach items="${fns:getReportQueryYears()}" var="year">
                <option value="${year}" <c:out
                        value="${(rptSearchCondition.selectedYear eq year)?'selected=selected':''}"/>>${year}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>月份：</label>
        <select id="selectedMonth" name="selectedMonth" class="input-mini" style="width:85px;">
            <c:forEach var="i" begin="0" end="11" step="1">
                <option value="${1+i}" <c:out
                        value="${(rptSearchCondition.selectedMonth eq 1+i)?'selected=selected':''}"/>>${1+i}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <c:choose>
            <c:when test="${currentuser.isCustomer()}">

                <input type="hidden" readonly="true" id="customerName" name="customerName"
                       value="${currentuser.customerAccountProfile.customer.name}"/>
                <input type="hidden" readonly="true" id="customerId" name="customerId"
                       value="${currentuser.customerAccountProfile.customer.id}"/>
            </c:when>
            <c:otherwise>
                <label>客户：</label>
                <select id="customerId" name="customerId" class="input-small" style="width:225px;">
                    <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}"/>>
                        所有
                    </option>
                    <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                        <option value="${dict.id}" <c:out
                                value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
                    </c:forEach>
                </select>
            </c:otherwise>
        </c:choose>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/>
        &nbsp;&nbsp;
        <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 150px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：实时数据(5分钟延迟)<br/>
                    统计方式：下单时间
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}"/>
<script type="text/javascript">
    $(document).ready(function () {
        if ($("#contentTable tbody>tr").length > 0) {
            //无数据报错
            var h = $(window).height();
            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w - 10,
                height: h - 138,
                fixedCols: 1,
                headerRows: 2,
                colWidths: [100,
                    <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">80, </c:forEach>
                    80],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
        else {
            $("#divGrid").css("height", h - 138);
        }
    });
</script>
<div id="divGrid" style="overflow-x:hidden;">
    <table id="contentTable" class="table  table-bordered table-condensed table-hover"
           style="table-layout:fixed; margin-top: 0px;border-top-width: 0px;">
        <thead>
        <tr>
            <th rowspan="2" width="100">省</th>

            <th colspan="${rptSearchCondition.days}" width="${rptSearchCondition.days*80}">每日完工单(单)</th>

            <th rowspan="2" width="80">合计(单)</th>
        </tr>
        <tr>
            <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                <th>${i}</th>
            </c:forEach>
        </tr>
        </thead>
        <tbody>
        <c:if test="${not empty list}">
            <c:set var="rowIndex" value="0"/>
            <c:forEach items="${list}" var="pItem">
                <c:set var="rowIndex" value="${rowIndex+1}"/>
             <c:choose>
            <c:when test="${rowIndex < list.size()}">
                <tr>
                    <td>
                        <a href="${ctx}/rpt/provider/gradedOrder/cityCompleteOrderRpt?provinceId=${pItem.provinceId}&selectedYear=${rptSearchCondition.selectedYear}&selectedMonth=${rptSearchCondition.selectedMonth}&areaName=${pItem.provinceName}">
                                ${pItem.provinceName}</a>
                    </td>
                    <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                        <c:set var="colname" value="d${i}"/>
                        <td>
                            <fmt:formatNumber maxFractionDigits="0">${pItem[colname]}</fmt:formatNumber>
                        </td>
                    </c:forEach>
                    <td>
                        <fmt:formatNumber maxFractionDigits="0"> ${pItem.total}</fmt:formatNumber>
                    </td>
                </tr>
            </c:when>
                <c:otherwise>
                    <tr style="color: blue;">
                        <td colspan="1">${pItem.provinceName }</td>
                        <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                            <c:set var="colname" value="d${i}"/>
                            <td>
                                <fmt:formatNumber maxFractionDigits="0">${pItem[colname]}</fmt:formatNumber>
                            </td>
                        </c:forEach>
                        <td>
                            <fmt:formatNumber maxFractionDigits="0">${pItem.total}</fmt:formatNumber>
                        </td>
                    </tr>
                </c:otherwise>
            </c:choose>
            </c:forEach>
        </c:if>

        </tbody>
    </table>
</div>
</ul>
</body>
</html>
