﻿
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <title>客户订单消费汇总表</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
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
    <script type="text/javascript">
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
            $("#btnSubmit").click(function() {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action","${ctx}/finance/rpt/customerReceivableSummary/customerReceivableSummaryRptNewer");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/finance/rpt/customerReceivableSummary/checkExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/finance/rpt/customerReceivableSummary/export?"+ (new Date()).getTime(),
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
        // function page(n,s){
        //     $("#pageNo").val(n);
        //     $("#pageSize").val(s);
        //     $("#searchForm").submit();
        //     return false;
        // }
        function check() {
            var isSalesMan = ${fns:getUser().isSaleman()};
            if(isSalesMan && $("#customerId").find("option:selected").text()=="所有")
            {
                top.$.jBox.error('请选择客户！', '错误');
                return false;
            }
            return true;
        }
    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">客户应收帐款汇总表</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/finance/rpt/customerReceivableSummary/customerReceivableSummaryRptNewer" method="post" class="breadcrumb form-search"  >
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label >客  户:</label>
        <select id="customerId" name="customerId" class="input-small"
                style="width:250px;">
            <option value=""
                    <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${fns:getMyCustomerListFromMS()}" var="customer">
                <option value="${customer.id}" <c:out value="${(rptSearchCondition.customerId eq customer.id)?'selected=selected':''}" />>${customer.name}</option>
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
                <option value="${i+1}" <c:out value="${(rptSearchCondition.selectedMonth eq i+1)?'selected=selected':''}" />>${i+1}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>结算方式 ：</label>
        <select id="paymentType" name="paymentType" class="input-small" style="width:125px;">
            <option value="" <c:out value="${(empty rptSearchCondition.paymentType)?'selected=selected':''}" />>所有</option><%--切换为微服务--%>
            <c:forEach items="${fns:getDictExceptListFromMS('PaymentType', '20')}" var="dict"><%--切换为微服务--%>
                <option value="${dict.value}" <c:out value="${(rptSearchCondition.paymentType eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:finance:customerReceivableSummary:view"><input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:finance:customerReceivableSummary:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
    </div>

    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 250px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：隔天数据 <br/>
                    统计方式：审单时间<br/>
                    栏位说明：<br/>
                    【本月消费余额】上月消费余额+本月已收款-本月应收款-对账差异单-时效费-加急费-好评费<br/>
                    【本月未完成单】上月未完成单+本月接单-本月完工单-本月退单-本月取消单
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
                height: h - 248,
                fixedCols: 1,
                headerRows: 3,
                colWidths:
                    [60,
                        80, 120, 100, 200, 100, 100,
                        100, 100, 100, 100, 100, 100, 100,
                        100, 100, 100, 100, 100, 100,100, 100, 100],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
        else {
            $("#divGrid").css("height", h-228);
        }

    });
</script>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover" style="table-layout:fixed; margin-top: 0px;border-top-width: 0px;">
        <thead>
        <%-- 第一列用于固定表格列的宽度，并且该列隐藏不显示 --%>
        <tr style="height: 4px; border-width: 0px; padding: 0px; margin: 0px;visibility: hidden;">
            <th width="60"></th>

            <th width="80"></th>
            <th width="120"></th>
            <th width="100"></th>
            <th width="200"></th>
            <th width="100"></th>

            <th width="90"></th>
            <th width="90"></th>
            <th width="90"></th>
            <th width="90"></th>
            <th width="90"></th>
            <th width="90"></th>

            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>

            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>


        </tr>
        <tr >
            <th rowspan="2">序号</th>

            <th rowspan="2">结算方式</th>
            <th rowspan="2">签约时间</th>
            <th rowspan="2">客户编号</th>
            <th rowspan="2">客户名称</th>
            <th rowspan="2">业务员</th>

            <th colspan="6">订单情况</th>
            <c:if test="${!result}">
                <th colspan="5">消费情况</th>
            </c:if>
            <c:if test="${result}">
                <th colspan="9">消费情况</th>
                <th rowspan="2">信用额度</th>
                <th rowspan="2">客户押金</th>
            </c:if>
        </tr>
        <tr >
            <th >上月未完成单</th>
            <th >本月接单</th>
            <th >本月完工单</th>
            <th >本月退单</th>
            <th >本月取消单</th>
            <th >本月未完成单</th>
            <c:if test="${result}">
                <th >上月消费余额</th>
                <th >本月已收款</th>
            </c:if>
            <th >本月应收款</th>
            <th >对帐差异单</th>
            <th >本月时效费</th>
            <th >本月加急费</th>
            <th >本月好评费</th>
            <c:if test="${result}">
                <th >本月消费余额</th>
                <th >本月冻结金额</th>
            </c:if>

        </tr>
        </thead>
        <tbody>
        <c:set value = "0" var= "totallastMonthNoClose"/>
        <c:set value = "0" var= "totalcurrentMonthAccept"/>
        <c:set value = "0" var= "totalcurrentMonthClose"/>
        <c:set value = "0" var= "totalcurrentMonthReturned"/>
        <c:set value = "0" var= "totalcurrentMonthCanceled"/>
        <c:set value = "0" var= "totalcurrentMonthNoClose"/>

        <c:set value = "0" var= "totallastMonthAalance"/>
        <c:set value = "0" var= "totalcurrentMonthInvoice"/>
        <c:set value = "0" var= "totalcurrentMonthCharge"/>
        <c:set value = "0" var= "totalcurrentMonthDiffCharge"/>
        <c:set value = "0" var= "totalCustomerTimeLinessCharge"/>
        <c:set value = "0" var= "totalCustomerUrgentCharge"/>
        <c:set value = "0" var= "totalPraiseFee"/>
        <c:set value = "0" var= "totalbalance"/>
        <c:set value = "0" var= "totalBlockAmount"/>

        <c:set value = "0" var= "totalCredit"/>
        <c:set value = "0" var= "totalDeposit"/>

        <c:set var="i" value="0"/>
        <c:forEach items="${page.list}" var="item">
            <c:set var="i" value="${i+1}"/>
            <c:set value = "${totallastMonthNoClose+item.preNoFinishQty}" var = "totallastMonthNoClose"/>
            <c:set value = "${totalcurrentMonthAccept+item.newQty}" var = "totalcurrentMonthAccept"/>
            <c:set value = "${totalcurrentMonthClose+item.finishQty}" var= "totalcurrentMonthClose"/>
            <c:set value = "${totalcurrentMonthReturned+item.returnQty}" var= "totalcurrentMonthReturned"/>
            <c:set value = "${totalcurrentMonthCanceled+item.cancelQty}" var= "totalcurrentMonthCanceled"/>
            <c:set value = "${totalcurrentMonthNoClose+item.noFinishQty}" var= "totalcurrentMonthNoClose"/>

            <c:set value = "${totallastMonthAalance+item.preBalance}" var= "totallastMonthAalance"/>
            <c:set value = "${totalcurrentMonthInvoice+item.rechargeAmount}" var= "totalcurrentMonthInvoice"/>
            <c:set value = "${totalcurrentMonthCharge+item.orderPaymentAmount}" var= "totalcurrentMonthCharge"/>
            <c:set value = "${totalcurrentMonthDiffCharge+item.diffCharge}" var= "totalcurrentMonthDiffCharge"/>
            <c:set value = "${totalCustomerTimeLinessCharge+item.customerTimeLinessCharge}" var= "totalCustomerTimeLinessCharge"/>
            <c:set value = "${totalCustomerUrgentCharge+item.customerUrgentCharge}" var= "totalCustomerUrgentCharge"/>
            <c:set value = "${totalPraiseFee+item.praiseFee}" var= "totalPraiseFee"/>
            <c:set value = "${totalbalance+item.balance}" var= "totalbalance"/>
            <c:set value = "${totalBlockAmount+item.blockAmount}" var= "totalBlockAmount"/>

            <c:set value = "${totalCredit+item.currentCredit}" var= "totalCredit"/>
            <c:set value = "${totalDeposit+item.currentDeposit}" var= "totalDeposit"/>

            <tr >
                <td>${i}</td>
                <td>${item.paymentType.label}</td>
                <td><fmt:formatDate value="${item.contractDate}" pattern="yyyy-MM-dd"/></td>
                <td>${item.customerCode}</td>
                <td>${item.customerName}</td>
                <td>${item.salesMan}</td>

                <td>${item.preNoFinishQty}</td>
                <td>${item.newQty}</td>
                <td>${item.finishQty}</td>
                <td>${item.returnQty}</td>
                <td>${item.cancelQty}</td>
                <td>${item.noFinishQty}</td>
                <c:if test="${result}">
                    <td><fmt:formatNumber value="${item.preBalance}" pattern="0.00"/></td>
                    <td><fmt:formatNumber value="${item.rechargeAmount}" pattern="0.00"/></td>
                </c:if>
                <td><fmt:formatNumber value="${item.orderPaymentAmount}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${item.diffCharge}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${item.customerTimeLinessCharge}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${item.customerUrgentCharge}" pattern="0.00"/></td>
                <td><fmt:formatNumber value="${item.praiseFee}" pattern="0.00"/></td>
                <c:if test="${result}">
                    <td><fmt:formatNumber value="${item.balance}" pattern="0.00"/></td>
                    <td><fmt:formatNumber value="${item.blockAmount}" pattern="0.00"/></td>

                    <td><fmt:formatNumber value="${item.currentCredit}" pattern="0.00"/></td>
                    <td><fmt:formatNumber value="${item.currentDeposit}" pattern="0.00"/></td>
                </c:if>
            </tr>

        </c:forEach>
        <c:if test="${page.list.size()>0}">
            <tr>
                <td colspan="6"><b>合计</b></td>
                <td style="color: red;"><c:out value="${totallastMonthNoClose}"/></td>
                <td style="color: red;"><c:out value="${totalcurrentMonthAccept}"/></td>
                <td style="color: red;"><c:out value="${totalcurrentMonthClose}"/></td>
                <td style="color: red;"><c:out value="${totalcurrentMonthReturned}"/></td>
                <td style="color: red;"><c:out value="${totalcurrentMonthCanceled}"/></td>
                <td style="color: red;"><c:out value="${totalcurrentMonthNoClose}"/></td>
                <c:if test="${result}">
                    <td style="color: red;"><fmt:formatNumber value="${totallastMonthAalance}" pattern="0.00"/></td>
                    <td style="color: red;"><fmt:formatNumber value="${totalcurrentMonthInvoice}" pattern="0.00"/></td>
                </c:if>
                <td style="color: red;"><fmt:formatNumber value="${totalcurrentMonthCharge}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${totalcurrentMonthDiffCharge}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${totalCustomerTimeLinessCharge}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${totalCustomerUrgentCharge}" pattern="0.00"/></td>
                <td style="color: red;"><fmt:formatNumber value="${totalPraiseFee}" pattern="0.00"/></td>
                <c:if test="${result}">
                    <td style="color: red;"><fmt:formatNumber value="${totalbalance}" pattern="0.00"/></td>
                    <td style="color: red;"><fmt:formatNumber value="${totalBlockAmount}" pattern="0.00"/></td>

                    <td style="color: red;"><fmt:formatNumber value="${totalCredit}" pattern="0.00"/></td>
                    <td style="color: red;"><fmt:formatNumber value="${totalDeposit}" pattern="0.00"/></td>
                </c:if>
            </tr>
        </c:if>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>

