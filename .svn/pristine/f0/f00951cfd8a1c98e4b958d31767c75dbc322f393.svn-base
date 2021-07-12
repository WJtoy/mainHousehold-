<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE HTML>
<html>
<head>
    <title>CustomerChageOrder</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
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
<%--    <script type="text/javascript" language="JavaScript">--%>
<%--        function validate(f) {--%>
<%--            if (f.customerId.value == "") {--%>
<%--                top.$.jBox.error("请选择客户！", "客户对帐单");--%>
<%--                top.$.jBox.closeTip();--%>
<%--                return false;--%>
<%--            } else {--%>
<%--                return true;--%>
<%--            }--%>
<%--        }--%>
<%--    </script>--%>
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
                $("#searchForm").attr("action", "${ctx}/customer/rpt/customerCharge/customerChargeSummaryRpt");
                $("#searchForm").submit();
            });
            $("#btnExport").click(function () {
                top.$.jBox.confirm("确认要导出数据吗？", "系统提示", function (v, h, f) {
                    if (v == "ok") {
                        top.$.jBox.tip('请稍候...', 'loading');
                        $.ajax({
                            type: "POST",
                            url: "${ctx}/customer/rpt/customerCharge/export?" + (new Date()).getTime(),
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
                top.$('.jbox-body .jbox-icon').css('top', '55px');
            });

        });

        function page(n, s) {
            $("#pageNo").val(n);
            $("#pageSize").val(s);
            $("#searchForm").submit();
            return false;
        }
    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">订单|消费|充值</a></li>
    <li>
        <a href="${ctx}/customer/rpt/customerCharge/customerChargeCompleteReport">完工单</a>
    </li>
    <li>
        <a href="${ctx}/customer/rpt/customerCharge/returnedOrderCancelledRpt">退单/取消单</a>
    </li>
    <li>
        <a href="${ctx}/customer/rpt/customerCharge/customerChargeWriteOffRpt">退补单</a>
    </li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<c:set var="mdcustomerID" value="${currentuser.getCustomerAccountProfile().getCustomer().getId()}"/>
<c:set var="mdcustomerName" value="${currentuser.getCustomerAccountProfile().getCustomer().getName()}"/>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/customer/rpt/customerCharge/customerChargeSummaryRpt" method="post" class="breadcrumb form-search">
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
<%--        <c:choose>--%>
<%--            <c:when test="${currentuser.isCustomer()}">--%>
<%--                <input type="hidden" id="customerId" name="customerId" value="${mdcustomerID}" maxlength="50"--%>
<%--                       style="width:345px;"/>--%>
<%--                <input type="hidden" id="customerName" name="customerName" value="${mdcustomerName}" maxlength="50"--%>
<%--                       style="width:105px;"/>--%>
<%--            </c:when>--%>
<%--            <c:otherwise>--%>
<%--                <label>客 户：</label>--%>
<%--                <select id="customerId" name="customerId" class="input-small" style="width:225px;">--%>
<%--                    <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}"/>>--%>
<%--                        所有--%>
<%--                    </option>--%>
<%--                    <c:forEach items="${fns:getMyCustomerList()}" var="dict">--%>
<%--                        <option value="${dict.id}" <c:out--%>
<%--                                value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>--%>
<%--                    </c:forEach>--%>
<%--                </select>--%>
<%--                <span class="add-on red">必选*</span>--%>
<%--            </c:otherwise>--%>
<%--        </c:choose>--%>
        <label>年份：</label>
        <select id="selectedYear" name="selectedYear" class="input-small" style="width:85px;">
            <c:forEach items="${fns:getReportQueryYears()}" var="year">
                <option value="${year}" <c:out
                        value="${(rptSearchCondition.selectedYear eq year)?'selected=selected':''}"/>>${year}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;

        <label>月份：</label>
        <select id="month" name="selectedMonth" class="input-mini" style="width:85px;">
            <c:forEach var="i" begin="0" end="11" step="1">
                <option value="${i+1}" <c:out
                        value="${(rptSearchCondition.selectedMonth eq i+1)?'selected=selected':''}"/>>${i+1}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:customer:customerChargeSummaryRpt:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:customer:customerCharge:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 250px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：隔天数据<br/>
                    统计方式：下单时间<br/>
                    栏位说明：<br/>
                    【本月未完成单】上月未完成单+本月下单-本月完工单-本月退单-本月取消单<br/>
                    【本月消费余额】上月余额+本月充值-本月完工金额-对账差异单-本月时效费-本月加急费-本月好评费
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>

</form:form>
<sys:message content="${message}"/>

<label>订单情况</label>
<br>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover" style="table-layout:fixed"
       cellspacing="0" width="100%">
    <thead>
    <tr>
        <th>上月未完成单</th>
        <th>本月下单</th>
        <th>本月完工单</th>
        <th>本月退单</th>
        <th>本月取消单</th>
        <th>本月未完成单</th>
    </tr>
    </thead>
    <tbody>
    <c:if test="${summary != null}">
        <tr>
            <td>${summary.lastMonthUncompletedQty}</td>
            <td>${summary.newQty}</td>
            <td>${summary.completedQty}</td>
            <td>${summary.returnedQty}</td>
            <td>${summary.cancelledQty}</td>
            <td>${summary.uncompletedQty}</td>
        </tr>
    </c:if>
    </tbody>
</table>

<label>消费情况</label>
<br>
<table id="contentTable" class="table table-striped table-bordered table-condensed" style="table-layout:fixed"
       cellspacing="0" width="100%">
    <thead>
    <tr>
        <th>上月消费余额</th>
        <th>本月充值</th>
        <th>本月完工单金额</th>
        <th>对账差异单（本期退补款）</th>
        <th>本月时效费</th>
        <th>本月加急费</th>
        <th>本月好评费</th>
        <th>本月消费余额</th>
        <th>未完工冻结金额</th>
    </tr>
    </thead>
    <tbody>
    <c:if test="${summary != null}">
        <tr>
            <td><fmt:formatNumber pattern="0.00" value="${summary.lastMonthBalance}"/></td>
            <td><fmt:formatNumber pattern="0.00" value="${summary.rechargeAmount}"/></td>
            <td><fmt:formatNumber pattern="0.00" value="${summary.completedOrderCharge}"/></td>
            <td><fmt:formatNumber pattern="0.00" value="${summary.writeOffCharge}"/></td>
            <td><fmt:formatNumber pattern="0.00" value="${summary.timelinessCharge}"/></td>
            <td><fmt:formatNumber pattern="0.00" value="${summary.urgentCharge}"/></td>
            <td><fmt:formatNumber pattern="0.00" value="${summary.praiseFee}"/></td>
            <td><fmt:formatNumber pattern="0.00" value="${summary.balance}"/></td>
            <td><fmt:formatNumber pattern="0.00" value="${summary.blockAmount}"/></td>
        </tr>
    </c:if>
    </tbody>
</table>
</body>
</html>
