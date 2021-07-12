<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE HTML>
<html>
<head>
    <title>网点应付汇总</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
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
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
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
            $("th").css({"text-align": "center", "vertical-align": "middle"});
            $("td").css({"vertical-align": "middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity: 'east'});

            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#pageNo").val(1);
                $("#searchForm").attr("action", "${ctx}/finance/rpt/servicePointCharge/servicePointPaySummaryReport");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/finance/rpt/servicePointCharge/checkPaySummaryExportTask?" + (new Date()).getTime(),
                    data: $(searchForm).serialize(),
                    success: function (data) {
                        if (ajaxLogout(data)) {
                            return false;
                        }
                        if (data && data.success == true) {
                            top.$.jBox.closeTip();
                            top.$.jBox.confirm("确认要导出数据吗？", "系统提示", function (v, h, f) {
                                if (v == "ok") {
                                    top.$.jBox.tip('请稍候...', 'loading');
                                    $.ajax({
                                        type: "POST",
                                        url: "${ctx}/finance/rpt/servicePointCharge/paySummaryExport?" + (new Date()).getTime(),
                                        data: $(searchForm).serialize(),
                                        success: function (data) {
                                            if (ajaxLogout(data)) {
                                                return false;
                                            }
                                            if (data && data.success == true) {
                                                top.$.jBox.closeTip();
                                                top.$.jBox.tip(data.message, "success");
                                                $('#btnExport').removeAttr('disabled');
                                                return false;
                                            }
                                            else if (data && data.message) {
                                                top.$.jBox.error(data.message, "导出错误");
                                            }
                                            else {
                                                top.$.jBox.error("导出错误", "错误提示");
                                            }
                                            $('#btnExport').removeAttr('disabled');
                                            top.$.jBox.closeTip();
                                            return false;
                                        },
                                        error: function (e) {
                                            $('#btnExport').removeAttr('disabled');
                                            ajaxLogout(e.responseText, null, "导出错误，请重试!");
                                            top.$.jBox.closeTip();
                                        }
                                    });
                                }
                            }, {buttonsFocus: 1});
                            $('#btnExport').removeAttr('disabled');
                            top.$.jBox.closeTip();
                            return false;
                        }
                        else if (data && data.message) {
                            top.$.jBox.error(data.message, "导出错误");
                        }
                        else {
                            top.$.jBox.error("导出错误", "错误提示");
                        }
                        $('#btnExport').removeAttr('disabled');
                        top.$.jBox.closeTip();
                        return false;
                    },
                    error: function (e) {
                        $('#btnExport').removeAttr('disabled');
                        ajaxLogout(e.responseText, null, "导出错误，请重试!");
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
    <li class="active"><a href="javascript:void(0);">网点应付汇总</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }" />
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/finance/rpt/servicePointCharge/servicePointPaySummaryReport" method="post" class="breadcrumb form-search">
    <div style="width: 90%">
        <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
        <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label class="control-label">服务网点：</label>
        <rpt:servicePointSelector id="servicePoint" name="servicePointId" value="${rptSearchCondition.servicePointId}"
                                  labelName="servicePointName" labelValue="${rptSearchCondition.servicePointName}"
                                  width="900" height="700" noblackList="true" callbackmethod="" cssClass="required"/>
        &nbsp;&nbsp;
        <label>服务品类：</label>
        <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
            <option value="0" <c:out value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${productCategoryList}" var="dict">
                <option value="${dict.id}" <c:out value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <label>结算方式：</label>
        <select id="paymentType" name="paymentType" class="input-small" style="width:125px;">
            <option value="" <c:out value="${(empty rptSearchCondition.paymentType)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${fns:getDictListFromMS('PaymentType')}" var="dict"><%--切换为微服务--%>
                <option value="${dict.value}" <c:out value="${(rptSearchCondition.paymentType eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
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
        <shiro:hasPermission name="rpt:finance:servicePointPaySummaryReport:view"><input id="btnSubmit" class="btn btn-primary"type="button" value="查询"/></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:finance:servicePointPaySummaryReport:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
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
                    【本月应付合计】本月完工单金额+时效奖励+厂商时效费+加急费+扣点+好评费+平台服务费(入账)+质保金额+本月互助基金+本月退补金额<br/>
                    【本月余额】上月余额+本月应付合计-本月实付-平台服务费(付款)
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}" />
<script type="text/javascript">
    $(document).ready(function() {
        var h = $(window).height();
        if($("#contentTable tbody>tr").length>0) {
            //无数据报错

            var w = $(window).width();
            $("#contentTable").toSuperTable({
                width: w-10,
                height: h - 208,
                fixedCols: 3,
                headerRows: 3,
                colWidths:
                    [70,
                        140, 200, 100, 100, 100, 200,  100,
                        100,
                        100, 100, 150, 100, 100, 100, 100, 100,160,100,100,100, 100,100,160,
                        100,100,100,100,100,160, 100, 160,
                        200],
                onStart: function () {
                },
                onFinish: function () {
                }
            });
        }
        else {
            $("#divGrid").css("height", h-208);
        }

    });
</script>
<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table table-bordered table-condensed table-hover" style="table-layout:fixed;margin-top: 0px;border-top-width: 0px;" cellspacing="0" width="100%">
        <thead>
        <tr style="height: 4px; border-width: 0px; padding: 0px; margin: 0px;visibility: hidden;">
            <th width="70"></th>

            <th width="140"></th>
            <th width="200"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="200"></th>
            <th width="100"></th>

            <th width="100"></th>

            <th width="100"></th>
            <th width="100"></th>
            <th width="150"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="160"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="160"></th>
            <th width="100"></th>
            <th width="100"></th>
            <th width="100"></th>

            <th width="100"></th>
            <th width="100"></th>
            <th width="160"></th>
            <th width="100"></th>
            <th width="160"></th>

            <th width="200"></th>
        </tr>
        <tr>
            <th rowspan="2">序号</th>
            <th colspan="7">服务网点信息</th>
            <th rowspan="2">本月完成单</th>
            <th colspan="18">费用情况</th>
            <th colspan="5">银行帐号信息</th>
            <th rowspan="2">备注</th>
        </tr>
        <tr>
            <th>网点编号</th>
            <th>网点店名</th>
            <th>网点负责人</th>
            <th>联系电话1</th>
            <th>联系电话2</th>
            <th>省市区</th>
            <th>结算方式</th>

            <th>上月余额</th>
            <th>本月预付</th>
            <th>本月完工单金额</th>
            <th>时效奖励</th>
            <th>厂商时效费</th>
            <th>加急费</th>
            <th>扣点</th>
            <th>好评费</th>
            <th>平台服务费(入账)</th>
            <th>质保金额</th>
            <th>本月互助基金</th>
            <th>本月退补金额</th>
            <th>本月应付合计</th>
            <th>本月实付</th>
            <th>平台服务费(付款)</th>
            <th>本月余额</th>
            <th>远程费用</th>
            <th>其他费用</th>

            <th>开票</th>
            <th>扣点</th>
            <th>所属银行及支行</th>
            <th>账户名</th>
            <th>账号</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="totalPreBalance" value="0"/>
        <c:set var="totalDeposit" value="0"/>
        <c:set var="totalCompletedOrderCharge" value="0"/>
        <c:set var="totalTimeLinessCharge" value="0"/>
        <c:set var="totalCustomerTimeLinessCharge" value="0"/>
        <c:set var="totalUrgentCharge" value="0"/>
        <c:set var="praiseFee" value="0"/>
        <c:set var="infoFee" value="0"/>
        <c:set var="taxFee" value="0"/>
        <c:set var="engineerDeposit" value="0"/>
        <c:set var="totalInsuranceCharge" value="0"/>
        <c:set var="totalDiffCharge" value="0"/>
        <c:set var="totalPayableAmount" value="0"/>
        <c:set var="totalPaidAmount" value="0"/>
        <c:set var="totalPlatformFee" value="0"/>
        <c:set var="totalTheBalance" value="0"/>
        <c:set var="totalTravelCharge" value="0"/>
        <c:set var="totalOtherCharge" value="0"/>
        <c:set var="totalFinishQty" value="0"/>

        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${page.list}" var="item">
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <tr>

                <td>${rowIndex}</td>

                <td>${item.servicePoint.servicePointNo}</td>
                <td>${item.servicePoint.name}</td>
                <td>${item.primaryName}</td>
                <td>${item.servicePoint.contactInfo1}</td>
                <td>${item.servicePoint.contactInfo2}</td>
                <td>${item.address}</td>
                <td>${item.servicePoint.paymentType.label}</td>

                <td>${item.completeQty}</td>

                <td>${item.lastMonthBalance}</td>
                <td>${item.preDeposit}</td>
                <td>${item.completedCharge}</td>
                <td>${item.timelinessCharge}</td>
                <td>${item.customerTimelinessCharge}</td>
                <td>${item.urgentCharge}</td>
                <td>${item.taxFee}</td>
                <td>${item.praiseFee}</td>
                <td>${item.infoFee}</td>
                <td>${item.engineerDeposit}</td>
                <td>${item.insuranceCharge}</td>
                <td>${item.returnCharge}</td>
                <td>${item.payableAmount}</td>
                <td><fmt:formatNumber pattern="0.00" value="${item.actualPaidAmount}"/></td>
                <td>${item.platformFee}</td>
                <td>${item.theBalance}</td>
                <td>${item.engineerTravelCharge}</td>
                <td>${item.engineerOtherCharge}</td>

                <td>${item.invoiceFlag==0?"否":"是"}</td>
                <td>${item.discountFlag==0?"否":"是"}</td>
                <td>${item.servicePoint.bank.label}</td>
                <td>${item.servicePoint.bankOwner}</td>
                <td>${item.servicePoint.bankNo}</td>

                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.servicePointRemarks}">${fns:abbr(item.servicePointRemarks,40)}</a></td>

            </tr>
            <c:set var="totalPreBalance" value="${totalPreBalance + item.lastMonthBalance}"/>
            <c:set var="totalDeposit" value="${totalDeposit + item.preDeposit}"/>
            <c:set var="totalCompletedOrderCharge" value="${totalCompletedOrderCharge + item.completedCharge}"/>
            <c:set var="totalTimeLinessCharge" value="${totalTimeLinessCharge + item.timelinessCharge}"/>
            <c:set var="totalCustomerTimeLinessCharge" value="${totalCustomerTimeLinessCharge + item.customerTimelinessCharge}"/>
            <c:set var="totalUrgentCharge" value="${totalUrgentCharge + item.urgentCharge}"/>
            <c:set var="praiseFee" value="${praiseFee + item.praiseFee}"/>
            <c:set var="infoFee" value="${infoFee + item.infoFee}"/>
            <c:set var="taxFee" value="${taxFee + item.taxFee}"/>
            <c:set var="engineerDeposit" value="${engineerDeposit + item.engineerDeposit}"/>
            <c:set var="totalInsuranceCharge" value="${totalInsuranceCharge + item.insuranceCharge}"/>
            <c:set var="totalDiffCharge" value="${totalDiffCharge + item.returnCharge}"/>
            <c:set var="totalPayableAmount" value="${totalPayableAmount + item.payableAmount}"/>
            <c:set var="totalPaidAmount" value="${totalPaidAmount + item.actualPaidAmount}"/>
            <c:set var="totalPlatformFee" value="${totalPlatformFee + item.platformFee}"/>
            <c:set var="totalTheBalance" value="${totalTheBalance + item.theBalance}"/>
            <c:set var="totalTravelCharge" value="${totalTravelCharge + item.engineerTravelCharge}"/>
            <c:set var="totalOtherCharge" value="${totalOtherCharge + item.engineerOtherCharge}"/>
            <c:set var="totalFinishQty" value="${totalFinishQty + item.completeQty}"/>
        </c:forEach>
        <c:if test="${page.list.size()> 0}">
            <tr style="color: red;">
                <td colspan="8">合计</td>
                <td>${totalFinishQty}</td>
                <td><fmt:formatNumber pattern="0.00" value="${totalPreBalance}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalDeposit}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalCompletedOrderCharge}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalTimeLinessCharge}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalCustomerTimeLinessCharge}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalUrgentCharge}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${taxFee}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${praiseFee}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${infoFee}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${engineerDeposit}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalInsuranceCharge}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalDiffCharge}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalPayableAmount}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalPaidAmount}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalPlatformFee}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalTheBalance}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalTravelCharge}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${totalOtherCharge}"/></td>
                <td colspan="6"></td>
            </tr>
        </c:if>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
</body>
</html>

