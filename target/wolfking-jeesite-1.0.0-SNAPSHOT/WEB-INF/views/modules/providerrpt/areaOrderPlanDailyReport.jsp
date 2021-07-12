<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<html>
<head>
    <title>省市区每日下单报表</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@ include file="/WEB-INF/views/include/treetable.jsp" %>
    <link href="${ctxStatic}/jquery-fixedheadertable/defaultTheme.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery-fixedheadertable/jquery.fixedheadertable.min.js" type="text/javascript"></script>
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
            $("#contentTable").treeTable({expandLevel : 1});
            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action", "${ctx}/rpt/provider/areaOrderPlan/areaOrderPlanDailyReport");
                $("#searchForm").submit();
            });
            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/areaOrderPlan/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/areaOrderPlan/export?"+ (new Date()).getTime(),
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
    <li class="active"><a href="javascript:void(0);">省市区每日下单报表</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/areaOrderPlan/areaOrderPlanDailyReport"
           method="post" class="breadcrumb form-search">
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>区域：</label>
        <sys:treeselectareanew id="area" name="areaId" value="${rptSearchCondition.areaId}"
                        levelValue="${rptSearchCondition.areaLevel}" nodeLevel="true"
                        labelName="areaName" labelValue="${rptSearchCondition.areaName }" title="区域"
                        url="/sys/area/treeDataNew?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1"
                        nameLevel="3"/>
        &nbsp;&nbsp;
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
        <label>服务品类：</label>
        <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
            <option value="0" <c:out value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}"/>>所有
            </option>
            <c:forEach items="${productCategoryList}" var="dict">
                <option value="${dict.id}" <c:out
                        value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
            </c:forEach>
        </select>
        <c:choose>
            <c:when test="${currentuser.isCustomer()}">
                <input type="hidden" readonly="true" id="customerName" name="customerName"
                       value="${currentuser.customerAccountProfile.customer.name}"/>
                <input type="hidden" id="customerId" name="customerId"
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
        <label class="label-search">工单来源：</label>
        <form:select path="dataSource" class="input-small" style="width:125px;">
            <form:option value="0" label="所有"/>
            <form:options items="${fns:getDictListFromMS('order_data_source')}" itemLabel="label" itemValue="value"
                          htmlEscape="false"/>
        </form:select>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary"type="button" value="查询" />
        &nbsp;&nbsp;
        <input id="btnExport"class="btn btn-primary" type="button" value="导出" />
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
<sys:message content="${message}" />
<table id="contentTable" class="table table-bordered table-condensed table-hover" style="table-layout:fixed;">
    <thead>
    <tr>
        <th rowspan="2" width="100">省</th>
        <th rowspan="2" width="100">市</th>
        <th rowspan="2" width="100">区</th>
        <c:choose>
            <c:when test="${empty sumUp}">
                <th rowspan="2">每日下单(单)</th>
            </c:when>
            <c:otherwise>
                <th colspan="${rptSearchCondition.days}" width="${rptSearchCondition.days*50}">每日下单(单)</th>
            </c:otherwise>
        </c:choose>
        <th rowspan="2" width="80">合计(单)</th>
    </tr>
    <c:if test="${not empty sumUp}">
        <tr>
            <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                <th>${i}</th>
            </c:forEach>
        </tr>
    </c:if>
    </thead>
    <tbody>
    <c:if test="${not empty areaList}">
        <c:forEach items="${areaList}" var="item">
            <c:if test="${item.provinceId ne proviceId }">
                <c:set value="${item.provinceId}" var="proviceId"/>
                <tr id="${item.provinceId}" pId="" style="color: blue;">
                    <td>${item.provinceName}</td>
                    <td></td>
                    <td></td>
                    <c:forEach items="${provinceList}" var="pItem">
                        <c:if test="${pItem.provinceId eq item.provinceId }">
                            <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                                <c:set var="colname" value="d${i}" />
                                <td>
                                    <fmt:formatNumber maxFractionDigits="0">${pItem[colname]}</fmt:formatNumber>
                                </td>
                            </c:forEach>
                            <td>
                                <fmt:formatNumber maxFractionDigits="0">${pItem.total}</fmt:formatNumber>
                            </td>
                        </c:if>
                    </c:forEach>
                </tr>
            </c:if>
            <c:if test="${item.cityId ne cityId }">
                <c:set value="${item.cityId}" var="cityId"/>
                <tr id="${item.cityId}" pId="${item.provinceId}" style="color: green;">
                    <td></td>
                    <td>${item.cityName}</td>
                    <td></td>
                    <c:forEach items="${cityList}" var="cItem">
                        <c:if test="${cItem.cityId eq item.cityId }">
                            <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                                <c:set var="colname" value="d${i}" />
                                <td>
                                    <fmt:formatNumber maxFractionDigits="0">${cItem[colname]}</fmt:formatNumber>
                                </td>
                            </c:forEach>
                            <td>
                                <fmt:formatNumber maxFractionDigits="0">${cItem.total}</fmt:formatNumber>
                            </td>
                        </c:if>
                    </c:forEach>
                </tr>
            </c:if>
            <tr id="${item.areaId+"2"}" pId="${item.cityId}">
                <td></td>
                <td></td>
                <td>${item.areaName}</td>
                <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                    <c:set var="colname" value="d${i}" />
                    <td>
                        <fmt:formatNumber maxFractionDigits="0">${item[colname]}</fmt:formatNumber>
                    </td>
                </c:forEach>
                <td>
                    <fmt:formatNumber maxFractionDigits="0">${item.total}</fmt:formatNumber>
                </td>
            </tr>
        </c:forEach>
    </c:if>

    <c:if test="${not empty sumUp}">
        <c:set value="${sumUp}" var="sumUp"/>
        <tr style="color: blue;">
            <td colspan="3">${sumUp.provinceName }</td>
            <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                <c:set var="colname" value="d${i}" />
                <td>
                    <fmt:formatNumber maxFractionDigits="0">${sumUp[colname]}</fmt:formatNumber>
                </td>
            </c:forEach>
            <td>
                <fmt:formatNumber maxFractionDigits="0">${sumUp.total}</fmt:formatNumber>
            </td>
        </tr>
    </c:if>

    </tbody>
</table>
</body>
</html>
