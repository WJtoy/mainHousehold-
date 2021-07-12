<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<html>
<head>
    <title>财务每日审单</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th,.table tbody td {
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
        $(document).ready(function() {
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
            $("#btnSubmit").click(function(){
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action","${ctx}/finance/rpt/abnormalFinancialReview/abnormalFinancialReviewRptData");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/finance/rpt/abnormalFinancialReview/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/finance/rpt/abnormalFinancialReview/export?"+ (new Date()).getTime(),
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
    <li class="active"><a href="javascript:void(0);">财务每日审单</a></li>
    <li>
        <a href="${ctx}/finance/rpt/financialReview/financialReviewDetailsRptData">财务审单明细</a>
    </li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/finance/rpt/abnormalFinancialReview/abnormalFinancialReviewRptData" method="post" class="breadcrumb form-search">
    <div>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>年份：</label>
        <select id="selectedYear" name="selectedYear" class="input-small" style="width:85px;">
            <c:forEach items="${fns:getReportQueryYears()}" var="year">
                <option value="${year}" <c:out value="${(rptSearchCondition.selectedYear eq year)?'selected=selected':''}" />>${year}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>月份：</label>
        <select id="selectedMonth" name="selectedMonth" class="input-mini" style="width:85px;">
            <c:forEach var="i" begin="0" end="11" step="1">
                <option value="${1+i}" <c:out value="${(rptSearchCondition.selectedMonth eq 1+i)?'selected=selected':''}" />>${1+i}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>审单员：</label>
        <select id="reviewerId" name="reviewerId" class="input-small" style="width:125px;">
            <option value="0" <c:out value="${(empty rptSearchCondition.reviewerId)?'selected=selected':''}"/>>所有
            </option>
            <c:forEach items="${reviewerList}" var="dict">
                <option value="${dict.id}" <c:out
                        value="${(rptSearchCondition.reviewerId eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
<%--        <label>服务品类：</label>--%>
<%--        <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">--%>
<%--            <option value="0" <c:out value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}"/>>所有--%>
<%--            </option>--%>
<%--            <c:forEach items="${productCategoryList}" var="dict">--%>
<%--                <option value="${dict.id}" <c:out--%>
<%--                        value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>--%>
<%--            </c:forEach>--%>
<%--        </select>--%>
        <shiro:hasPermission name="rpt:finance:abnormalFinancialReview:view"><input id="btnSubmit" class="btn btn-primary"type="button" value="查询" /></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:finance:abnormalFinancialReview:export"><input id="btnExport"class="btn btn-primary" type="button" value="导出" /></shiro:hasPermission>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 150px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：隔天数据<br/>
                    统计方式：审单时间<br/>
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>

</form:form>
<sys:message content="${message}" />
<div id="divGrid" style="overflow-x:hidden;">
    <table id="contentTable" class="table table-bordered table-condensed " style="table-layout:fixed;margin-top: 0px;border-top-width: 0px;">
        <thead>
        <tr>
            <th rowspan="2" width="60">序号</th>
            <th rowspan="2" width="180">统计名称</th>
            <c:choose>
                <c:when test="${rptSearchCondition.rowsCount == 0}">
                    <th rowspan="2">每日(单)</th>
                </c:when>
                <c:otherwise>
                    <th colspan="${rptSearchCondition.days}" width="${rptSearchCondition.days*80}">每日(单)</th>
                </c:otherwise>
            </c:choose>
            <th rowspan="2" width="120">合计(单)</th>
        </tr>
        <c:if test="${rptSearchCondition.rowsCount != 0}">
            <tr>
                <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                    <th>${i}</th>
                </c:forEach>
            </tr>
        </c:if>
        </thead>
        <tbody>
        <c:set var="rowIndex" value="0"></c:set>
        <c:forEach items="${rptSearchCondition.list}" var="item">
            <c:if test="${rowIndex == 0 }">
                <tr><td style="text-align:  left; " colspan="34" ><span style="margin-left: 20px ; font-size:18px">完工单</span></td></tr>
            </c:if>
            <c:if test="${rptSearchCondition.reviewerId == 0 and rowIndex == 3 }">
                <tr><td style="text-align:  left; " colspan="34" ><span style="margin-left: 20px ; font-size:18px">异常工单</span></td></tr>
            </c:if>
            <c:if test="${rptSearchCondition.reviewerId > 0 and rowIndex == 1 }">
                <tr><td style="text-align:  left; " colspan="34" ><span style="margin-left: 20px ; font-size:18px">异常工单</span></td></tr>
            </c:if>
            <c:set var="rowIndex" value="${rowIndex+1}"></c:set>
            <c:choose>
                <c:when test="${item.rowNumber < rptSearchCondition.sumRowNumber}">
                    <c:choose>
                        <c:when test="${item.createName eq ('手动对账') || item.createName eq ('手动对账（合计）')|| item.createName eq ('异常工单（合计）')}">
                            <tr style="background-color: rgba(204,204,204,0.26)">
                        </c:when>
                        <c:otherwise>
                            <tr>
                        </c:otherwise>
                    </c:choose>
                        <td>${rowIndex}</td>
                        <td>${item.createName}</td>
                        <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                            <c:set var="colname" value="d${i}" />
                            <td>
                                <c:if test="${item[colname]!=0}">
                                    <fmt:formatNumber maxFractionDigits="0">${item[colname]}</fmt:formatNumber>
                                </c:if>
                            </td>
                        </c:forEach>
                        <td>
                            <c:if test="${item.total!=0}">
                                <fmt:formatNumber maxFractionDigits="0">${item.total}</fmt:formatNumber>
                            </c:if>
                        </td>
                    </tr>
                </c:when>
                <c:when test="${item.rowNumber == rptSearchCondition.sumRowNumber}">
                    <tr>
                        <td></td>
                        <td>${item.createName}</td>
                        <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                            <c:set var="colname" value="d${i}" />
                            <td>
                                <c:if test="${item[colname]!=0}">
                                    <fmt:formatNumber maxFractionDigits="0">${item[colname]}</fmt:formatNumber>
                                </c:if>
                            </td>
                        </c:forEach>
                        <td>
                            <c:if test="${item.total!=0}">
                                <fmt:formatNumber maxFractionDigits="0">${item.total}</fmt:formatNumber>
                            </c:if>
                        </td>
                    </tr>
                </c:when>
            </c:choose>
        </c:forEach>
        </tbody>
    </table>
</div>

<script type="text/javascript">
    $(document).ready(function() {
        if($("#contentTable tbody>tr").length>0) {
            //无数据报错
            var h = $(window).height();
            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w-10,
                height: h - 138,
                fixedCols: 2,
                headerRows: 2,
                colWidths: [60, 200,
                    <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">80,</c:forEach>
                    120],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
    });
</script>

</body>
</html>
