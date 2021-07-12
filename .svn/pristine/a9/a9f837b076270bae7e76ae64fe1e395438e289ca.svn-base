<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <title>省市区特殊费用分布表</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>

    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <script type="text/javascript" language="javascript">
        $(document).ready(function() {
            $("#contentTable").treeTable({expandLevel : 3});

            $("#btnSubmit").click(function(){
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action","${ctx}/rpt/provider/customerSpecialChargeArea/countyList");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/customerSpecialChargeArea/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/customerSpecialChargeArea/export?"+ (new Date()).getTime(),
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


        function details(year,month,day,provinceId,chargeFlag,provinceName,productCategoryId,customerId){
            if(chargeFlag == 1){
                type = "远程"
            }else if(chargeFlag == 2){
                type = "其他"
            }
            var h = $(top.window).height();
            var w = $(top.window).width();
            top.layer.open({
                type: 2,
                id:'layer_unitCode',
                zIndex:19891015,
                title: year +'年'+month +'月'+day +'日'+provinceName + type +'费用详情',
                content: "${ctx}/rpt/order/customerSpecialExpenses/specialFeesDetailsOfRptChargeDate?year="+ (year || '') + "&month=" + (month || '') + "&day=" + (day || '') + "&provinceId=" + (provinceId || '') + "&chargeFlag=" + (chargeFlag || '')+ "&productCategoryId=" + (productCategoryId || "") + "&customerId=" + (customerId || ""),
                area:[(w-600)+'px',(h-100)+'px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">省市特殊费用分布表</a></li>
</ul>
<form:form id="searchForm"  modelAttribute="rptSearchCondition"  action="${ctx}/rpt/provider/customerSpecialChargeArea/countyList" method="post" class="breadcrumb form-search">
    <div>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>区域：</label>
        <sys:treeselect id="areaId" name="areaId" value="${rptSearchCondition.areaId}"
                        labelName="areaName" labelValue="${rptSearchCondition.areaName}" nameLevel="3"
                        nodesLevel="-1" title="区域" url="/rpt/provider/commonalityRpt/treeData?fromType=3" cssClass="input-small" allowClear="true" />
        &nbsp;&nbsp;
        <c:choose>
            <c:when test="${currentuser.isCustomer()}">
                <input type="hidden" id="customer.name" name="customer.name" value="${currentuser.customerAccountProfile.customer.name}" />
                <input type="hidden" id="customer.id" name="customer.id" value="${currentuser.customerAccountProfile.customer.id}" />
            </c:when>
            <c:otherwise>
                <label>客户：</label>
                <select id="customerId" name="customerId" class="input-small" style="width:225px;">
                    <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}" />>所有</option>
                    <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                        <option value="${dict.id}" <c:out value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                    </c:forEach>
                </select>
            </c:otherwise>
        </c:choose>
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
        <shiro:hasPermission name="rpt:customerSpecialChargeArea:view"><input id="btnSubmit" class="btn btn-primary"type="button" value="查询" /></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:customerSpecialChargeArea:export"><input id="btnExport"class="btn btn-primary" type="button" value="导出" /></shiro:hasPermission>
    </div>
</form:form>
<sys:message content="${message}" />
<script type="text/javascript">
    $(document).ready(function() {
        if($("#contentTable tbody>tr").length>0) {
            //无数据报错
            var h = $(window).height();
            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w-10,
                height: h - 138,
                fixedCols: 1,
                headerRows: 3,
                colWidths: [200,
                    <c:forEach var="i" begin="1" end="${rptSearchCondition.days*2}" step="1">80,</c:forEach>
                    100, 100],
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
    <table id="contentTable" class="table table-bordered table-condensed table-hover" style="table-layout:fixed;">
        <thead>
        <tr>
            <th rowspan="3" width="200">省市</th>
            <th colspan="${rptSearchCondition.days*2}" width="${rptSearchCondition.days*2*70}">费用(元)</th>
            <th rowspan="3" width="100">远程合计</th>
            <th rowspan="3" width="100">其他合计</th>
        </tr>
        <tr>
            <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                <th colspan="2">${i}</th>
            </c:forEach>
        </tr>
        <tr>
            <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                <th>远程</th>
                <th>其他</th>
            </c:forEach>
        </tr>
        </thead>
        <tbody>

            <c:set var="rowIndex" value="0"/>
            <c:forEach items="${list}" var="provinceItem">
                <c:set var="rowIndex" value="${rowIndex+1}"/>
                <c:choose>
                    <c:when test="${rowIndex < list.size()}">
                        <tr>
                            <td>${provinceItem.provinceName}</td>
                            <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                                <c:set var="dColname" value="t${i}" />
                                <c:set var="aColname" value="o${i}" />
                                <td>
                                    <a href="javascript:void(0);" onclick="javascript:details('${rptSearchCondition.selectedYear}','${rptSearchCondition.selectedMonth}','${i}','${provinceItem.provinceId}','1','${provinceItem.provinceName}','${rptSearchCondition.productCategory}','${rptSearchCondition.customerId}')">
                                        <fmt:formatNumber pattern="0.00">${provinceItem[dColname] eq 0 ? '' : provinceItem[dColname]}</fmt:formatNumber>
                                    </a>
                                </td>
                                <td>
                                    <a href="javascript:void(0);" onclick="javascript:details('${rptSearchCondition.selectedYear}','${rptSearchCondition.selectedMonth}','${i}','${provinceItem.provinceId}','2','${provinceItem.provinceName}','${rptSearchCondition.productCategory}','${rptSearchCondition.customerId}')">
                                        <fmt:formatNumber pattern="0.00">${provinceItem[aColname] eq 0 ? '' : provinceItem[aColname]}</fmt:formatNumber>
                                    </a>
                                </td>
                            </c:forEach>
                            <td>
                                <fmt:formatNumber pattern="0.00">${provinceItem.totalTravelCharge}</fmt:formatNumber>
                            </td>
                            <td>
                                <fmt:formatNumber pattern="0.00">${provinceItem.totalOtherCharge}</fmt:formatNumber>
                            </td>
                        </tr>
                        <c:forEach items="${provinceItem.itemList}" var="cityItem">
                            <tr>
                            <td>
                                <a href="${ctx}/rpt/provider/customerSpecialChargeArea/countyList?cityItemId=${cityItem.cityId}&selectedYear=${rptSearchCondition.selectedYear}
                                &selectedMonth=${rptSearchCondition.selectedMonth}&areaName=${provinceItem.provinceName} ${cityItem.cityName}&productCategory=${rptSearchCondition.productCategory}&customerId=${rptSearchCondition.customerId}">
                                        ${provinceItem.provinceName}${cityItem.cityName}</a>
                            </td>
                            <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                                <c:set var="dColname" value="t${i}" />
                                <c:set var="aColname" value="o${i}" />
                                <td>
                                    <fmt:formatNumber pattern="0.00">${cityItem[dColname] eq 0 ? '' : cityItem[dColname]}</fmt:formatNumber>
                                </td>
                                <td>
                                    <fmt:formatNumber pattern="0.00">${cityItem[aColname] eq 0 ? '' : cityItem[aColname]}</fmt:formatNumber>
                                </td>
                            </c:forEach>
                            <td>
                                <fmt:formatNumber pattern="0.00">${cityItem.totalTravelCharge}</fmt:formatNumber>
                            </td>
                            <td>
                                <fmt:formatNumber pattern="0.00">${cityItem.totalOtherCharge}</fmt:formatNumber>
                            </td>
                        </c:forEach>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <tr style="color: red;">
                            <td colspan="1">合计</td>
                            <c:forEach var="i" begin="1" end="${rptSearchCondition.days}" step="1">
                                <c:set var="dColname" value="t${i}" />
                                <c:set var="aColname" value="o${i}" />
                                <td>
                                    <fmt:formatNumber pattern="0.00">${provinceItem[dColname] eq 0 ? '' : provinceItem[dColname]}</fmt:formatNumber>
                                </td>
                                <td>
                                    <fmt:formatNumber pattern="0.00">${provinceItem[aColname] eq 0 ? '' : provinceItem[aColname]}</fmt:formatNumber>
                                </td>
                            </c:forEach>
                            <td>
                                <fmt:formatNumber pattern="0.00">${provinceItem.totalTravelCharge}</fmt:formatNumber>
                            </td>
                            <td>
                                <fmt:formatNumber pattern="0.00">${provinceItem.totalOtherCharge}</fmt:formatNumber>
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </c:forEach>


        </tbody>
    </table>
</div>
</body>
</html>
