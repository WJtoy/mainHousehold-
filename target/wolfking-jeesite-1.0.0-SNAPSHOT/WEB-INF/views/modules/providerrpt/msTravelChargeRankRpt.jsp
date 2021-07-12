<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE HTML>
<html>
<head>
    <title>远程费用排名</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <script src="${ctxStatic}/area/AreaFourLevel.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        .provinceCityAll {
            z-index:1000 !important;
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
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});


            $("#btnSubmit").click(function(){
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action","${ctx}/rpt/provider/travelChargeRank/getList");
                $("#searchForm").submit();
            });
            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/travelChargeRank/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/travelChargeRank/export?"+ (new Date()).getTime(),
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
        function page(n,s){
            $("#pageNo").val(n);
            $("#pageSize").val(s);
            $("#searchForm").submit();
            return false;
        }
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">远程费用排名</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }" />
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/travelChargeRank/getList" method="post" class="breadcrumb form-search">

        <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
        <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <div style="width: 90%">
            <label>区域：</label>
            <sys:areaSelectFourLevel id="areaId" name="areaId" value="${rptSearchCondition.areaId}" levelValue=""
                                     labelValue="${rptSearchCondition.areaName}" labelName="areaName"
                                     title="区域" mustSelectCounty="true" cssClass="required" showMaxLevel="3"> </sys:areaSelectFourLevel>
            &nbsp;&nbsp;
            <label>网点编号：</label>
            <form:input path="servicePointNo" htmlEscape="false" maxlength="20"	class="input-small" />
            &nbsp;&nbsp;
            <label>网点名称：</label>
            <form:input path="servicePointName" htmlEscape="false" maxlength="30" class="input-small"/>
            &nbsp;&nbsp;
            <label>联系方式：</label>
            <form:input path="contactInfo" htmlEscape="false" maxlength="20" class="input-small" />
            &nbsp;&nbsp;
            <label>自行接单：</label>
            <select id="appFlag" name="appFlag" class="input-small" style="width:125px;">
                <option value="" <c:out value="${(empty rptSearchCondition.appFlag)?'selected=selected':''}" />>所有</option>
                <c:forEach items="${fns:getDictListFromMS('yes_no')}" var="dict"><%--切换为微服务--%>
                    <option value="${dict.value}" <c:out value="${(rptSearchCondition.appFlag eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
                </c:forEach>
            </select>
            &nbsp;&nbsp;
            <label>完成单数：</label>
            <form:input path="finishQty" htmlEscape="false" maxlength="6" class="input-small"  onkeyup="value=value.replace(/[^\d]/g,'')"/>
        </div>
        &nbsp;&nbsp;
        <div>
            <label>结算方式 ：</label>
            <select id="paymentType" name="paymentType" class="input-small" style="width:125px;">
                <option value="" <c:out value="${(empty rptSearchCondition.paymentType)?'selected=selected':''}" />>所有</option>
                <c:forEach items="${fns:getDictListFromMS('PaymentType')}" var="dict"><%--切换为微服务--%>
                    <option value="${dict.value}" <c:out value="${(rptSearchCondition.paymentType eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
                </c:forEach>
            </select>
            &nbsp;&nbsp;
            <label>服务品类：</label>
            <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
                <c:if test="${userFlag==1}">
                <option value="0" <c:out value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}" />>所有</option>
                </c:if>
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
            <select id="selectedMonth" name="selectedMonth" class="input-mini" style="width:85px;">
                <c:forEach var="i" begin="0" end="11" step="1">
                    <option value="${1+i}" <c:out value="${(rptSearchCondition.selectedMonth eq 1+i)?'selected=selected':''}" />>${1+i}</option>
                </c:forEach>
            </select>
            &nbsp;&nbsp;
            <input id="btnSubmit" class="btn btn-primary"type="button" value="查询"/>
            &nbsp;&nbsp;
            <shiro:hasPermission name="rpt:order:servicePointTravelChargePerOrderRptExport"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
        </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 250px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：隔天数据<br/>
                    统计方式：客评时间<br/>
                    栏位说明：<br/>
                    【平均每单费用】（远程费用+其他费用）/本月完成单
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
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
                height: h - 250,
                fixedCols: 4,
                headerRows: 3,
                colWidths:
                    [70,
                        120, 200, 80, 100, 200,  80, 80, 200,
                        80,
                        80, 80, 80, 80,
                        80, 200],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
        else {
            $("#divGrid").css("height", h-230);
        }

    });
</script>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table table-bordered table-condensed table-hover" style="table-layout:fixed;margin-top: 0px;border-top-width: 0px;" cellspacing="0" width="100%">
        <thead>
        <tr style="height: 4px; border-width: 0px; padding: 0px; margin: 0px;visibility: hidden;">
            <th width="70"></th>

            <th width="120"></th>
            <th width="200"></th>
            <th width="80"></th>
            <th width="100"></th>

            <th width="200"></th>
            <th width="80"></th>
            <th width="80"></th>
            <th width="200"></th>

            <th width="80"></th>

            <th width="80"></th>
            <th width="80"></th>
            <th width="80"></th>
            <th width="80"></th>


            <th width="80"></th>
            <th width="200"></th>
        </tr>
        <tr>
            <th rowspan="2">序号</th>
            <th colspan="8">服务网点信息</th>
            <th rowspan="2">本月<br/>完成单</th>
            <th colspan="4">费用情况</th>
            <th rowspan="2">平均<br/>每单费用</th>
            <th rowspan="2">备注</th>
        </tr>
        <tr>
            <th>网点编号</th>
            <th>网点名称</th>
            <th>负责人</th>
            <th>手机</th>
            <th>省市区</th>
            <th>结算方式</th>
            <th>自行接单</th>
            <th>负责区域</th>


            <th>完工单金额</th>
            <th>退补金额</th>

            <th>远程费用</th>
            <th>其他费用</th>
        </tr>
        </thead>
        <tbody>

        <c:set var="totalCompletedOrderCharge" value="0"/>
        <c:set var="totalWriteOffCharge" value="0"/>

        <c:set var="totalTravelCharge" value="0"/>
        <c:set var="totalOtherCharge" value="0"/>
        <c:set var="totalCompletedQty" value="0"/>

        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${page.list}" var="item">
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <tr>

                <td>${rowIndex}</td>

                <td>${item.servicePointNo}</td>
                <td>${item.servicePointName}</td>
                <td>${item.primaryEngineerName}</td>
                <td>${item.contactInfo}</td>
                <td>${item.address}</td>
                <td>${item.paymentTypeLabel}</td>
                <td>${item.appFlag==1?"是":"否"}</td>
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.serviceAreaNames}">${fns:abbr(item.serviceAreaNames,30)}</a></td>

                <td>${item.completedQty}</td>


                <td>${item.completedOrderCharge}</td>
                <td>${item.writeOffCharge}</td>

                <td>${item.engineerTravelCharge}</td>
                <td>${item.engineerOtherCharge}</td>

                <td><fmt:formatNumber pattern="0.00" value="${item.averageCharge}"/></td>
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.remarks}">${fns:abbr(item.remarks,40)}</a></td>

            </tr>

            <c:set var="totalCompletedOrderCharge" value="${totalCompletedOrderCharge + item.completedOrderCharge}"/>
            <c:set var="totalWriteOffCharge" value="${totalWriteOffCharge + item.writeOffCharge}"/>
            <c:set var="totalTravelCharge" value="${totalTravelCharge + item.engineerTravelCharge}"/>
            <c:set var="totalOtherCharge" value="${totalOtherCharge + item.engineerOtherCharge}"/>
            <c:set var="totalCompletedQty" value="${totalCompletedQty + item.completedQty}"/>
        </c:forEach>
        <c:if test="${page.list.size()> 0}">
            <tr style="color: red;">
                <td colspan="9">合计</td>
                <td>${totalCompletedQty}</td>

                <td><fmt:formatNumber pattern="0.00" value="${totalCompletedOrderCharge}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalWriteOffCharge}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalTravelCharge}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalOtherCharge}"/></td>

                <td colspan="4"></td>
            </tr>
        </c:if>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>
