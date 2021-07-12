<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<html>
<head>
    <title>客户每日下单</title>
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
            z-index: 4 !important;
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
            boder-top: 0px solid transparent
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
            $('a[data-toggle=tooltip]').darkTooltip({gravity:'north'});
            //$('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});
            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action", "${ctx}/finance/rpt/customerOrderPlan/customerOrderPlanDailyReport");
                $("#searchForm").submit();
            });
            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/finance/rpt/customerOrderPlan/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/finance/rpt/customerOrderPlan/export?"+ (new Date()).getTime(),
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
    <li class="active"><a href="javascript:void(0);">客户每日下单</a>
    </li>
    <li>
        <a href="${ctx}/finance/rpt/customerOrderPlan/customerOrderPlanDailyChart">客户每日下单图表</a>
    </li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}" />
<form:form id="searchForm" modelAttribute="rptSearchCondition" method="post" class="breadcrumb form-search">
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>年份：</label>
        <select id="selectedYear" name="selectedYear" class="input-small" style="width:85px;">
            <c:forEach items="${fns:getReportQueryYears()}" var="year">
                <option value="${year}" <c:out value="${(rptSearchCondition.selectedYear eq year)?'selected=selected':''}" />>${year}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>月份：</label>
        <select id="month" name="selectedMonth" class="input-mini" style="width:85px;">
            <c:forEach var="i" begin="0" end="11" step="1">
                <option value="${i+1}" <c:out value="${(rptSearchCondition.selectedMonth eq i+1)?'selected=selected':''}" />>${i+1}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>服务品类：</label>
        <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
            <option value="0" <c:out value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}"/>>所有
            </option>
            <c:forEach items="${productCategoryList}" var="dict">
                <option value="${dict.id}" <c:out
                        value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>客户：</label>
        <c:choose>
            <c:when test="${currentuser.isCustomer()}">
                <input type="text" readonly="true" id="customer.name" name="customer.name" value="${currentuser.customerAccountProfile.customer.name}" />
                <input type="hidden" readonly="true" id="customer.id" name="customer.id" value="${currentuser.customerAccountProfile.customer.id}" />
            </c:when>
            <c:otherwise>
                <select id="customerId" name="customerId" class="input-small" style="width:225px;">
                    <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}" />>所有</option>
                    <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                        <option value="${dict.id}" <c:out value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                    </c:forEach>
                </select>
            </c:otherwise>
        </c:choose>
        &nbsp;&nbsp;
        <c:if test="${!currentuser.isCustomer() && !currentuser.isSaleman()}">
            <label>业务员：</label>
            <form:select path="salesId" style="width:120px;">
                <form:option value="" label="所有"/>
                <form:options items="${fns:getSaleList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
            </form:select>
        </c:if>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:finance:customerOrderPlanDailyReport:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询" /></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:finance:customerOrderPlanDailyReport:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出" /></shiro:hasPermission>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 250px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：实时数据（5分钟延迟）<br/>
                    统计方式：下单时间 <br/>
                    栏位说明：<br/>
                    【每日下单环比】：当日下单量和上月平均日下单量比较 <br/>
                    环比计算公式 =（当月每日下单量 -上月平均日下单量)/上月平均日下单量 <br/>
                    【每日下单同比】：当日下单量和去年当前月的平均日下单量比较 <br/>
                    同比计算公式 = （当月每日下单 - 去年同月平均日下单量）/去年同月平均日下单量 <br/>
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>

<sys:message content="${message}" />
<div id="divGrid" style="overflow-x:hidden;">
    <table id="contentTable" class="table table-bordered table-condensed table-hover" style="table-layout:fixed;">
        <thead>
        <tr>
            <th rowspan="2" width="60">序号</th>
            <th rowspan="2" width="80">结算类型</th>
            <th rowspan="2" width="100">签约时间</th>
            <th rowspan="2" width="80">客户编码</th>
            <th rowspan="2" width="180">客户名称</th>
            <th rowspan="2" width="120">业务员</th>
            <c:choose>
                <c:when test="${rptSearchCondition.rowsCount == 0}">
                    <th rowspan="2">每日下单情况(单)</th>
                </c:when>
                <c:otherwise>
                    <th colspan="${rptSearchCondition.days}" width="${rptSearchCondition.days*80}">每日下单情况(单)</th>
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
            <c:set var="rowIndex" value="${rowIndex+1}"></c:set>
            <c:choose>
                <c:when test="${item.rowNumber < rptSearchCondition.sumRowNumber}">
                    <tr>
                        <td>${rowIndex}</td>
                        <td>${item.customer.paymentType.label}</td>
                        <td><fmt:formatDate value="${item.customer.contractDate}" pattern="yyyy-MM-dd"/></td>
                        <td>${item.customer.code}</td>
                        <td>${item.customer.name}</td>
                        <td>${item.customer.sales.name}</td>
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
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td>${item.customer.sales.name}</td>
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
                <c:when test="${item.rowNumber == rptSearchCondition.perRowNumber}">
                    <tr>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td>${item.customer.sales.name}</td>
                        <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                            <c:set var="colname" value="d${i}" />
                            <td>
                                <c:if test="${item[colname]!=0}">
                                    <fmt:formatNumber maxFractionDigits="2">${item[colname]}</fmt:formatNumber>%
                                </c:if>
                            </td>
                        </c:forEach>
                        <td>
                            <c:if test="${item.total!=0}">
                                <fmt:formatNumber maxFractionDigits="2">${item.total}</fmt:formatNumber>%
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
                fixedCols: 6,
                headerRows: 2,
                colWidths: [60, 80, 100, 80, 180, 120,
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
