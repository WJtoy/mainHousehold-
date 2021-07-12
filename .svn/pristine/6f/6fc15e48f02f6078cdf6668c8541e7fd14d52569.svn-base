<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
    <title>客戶管理</title>
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
                // $("#pageNo").val(1);
                $("#searchForm").attr("action", "${ctx}/rpt/provider/customerFinance/customerFinanceReport");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/customerFinance/checkExportTask?" + (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/customerFinance/export?" + (new Date()).getTime(),
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
    <li class="active"><a href="javascript:void(0);">客户账户余额</a>
    </li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}" />
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/customerFinance/customerFinanceReport" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
    <div style="width: 90%;">
    <label>代码：</label>
    <input id="remarks" name="remarks" value="${rptSearchCondition.remarks}" maxlength="20">
    &nbsp;&nbsp;
    <label>名称：</label>
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
    <label>结算方式：</label>
    <select id="paymentType" name="paymentType" class="input-small" style="width:125px;">
        <option value="" <c:out value="${(empty rptSearchCondition.paymentType)?'selected=selected':''}" />>所有</option>
        <c:forEach items="${fns:getDictListFromMS('PaymentType')}" var="dict"><%--切换为微服务--%>
            <option value="${dict.value}" <c:out value="${(rptSearchCondition.paymentType eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
        </c:forEach>
    </select>
    &nbsp;&nbsp;
    <input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/>
    &nbsp;&nbsp;
    <input id="btnExport" class="btn btn-primary" type="button" value="导出" />
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 150px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：实时数据
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}" />
<c:set var="monetarylimit" value="${fns:getDictSingleValueFromMS('monetary_limit', '500')}" /><%-- 切换为微服务 --%>
<div id="divGrid" style="overflow-x:hidden;">
    <table id="contentTable" class="table table-bordered table-condensed table-hover" style="table-layout:fixed;">
        <thead>
        <tr>
            <th width="100">代码</th>
            <th width="300">名称</th>
            <th width="150">账户余额</th>
            <th width="150">冻结金额</th>
            <th width="150">可下单金额</th>
            <th width="150">信用额度</th>
            <th width="100">单位</th>
            <th width="100">业务员</th>
            <th>描述</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${page.list}" var="item">
            <tr>
                <td>${item.customer.code}</td>
                <td>${item.customer.name}</td>
                <c:set var="isMonetarylimit" value="${monetarylimit gt item.balance}" />
                <c:choose>
                    <c:when test="${isMonetarylimit eq true}">
                        <td>
                            <abbr title="余额低于 ${monetarylimit} 元，请及时充值">
                                <span class="badge badge-important"><fmt:formatNumber pattern="0.00" value="${item.balance}"/></span>
                            </abbr>
                        </td>
                    </c:when>
                    <c:otherwise>
                        <td><fmt:formatNumber pattern="0.00" value="${item.balance}"/></td>
                    </c:otherwise>
                </c:choose>
                <td><fmt:formatNumber pattern="0.00" value="${item.blockAmount}"/></td>
                <td><fmt:formatNumber pattern="0.00" value="${item.balance - item.blockAmount}"/></td>
                <td>${item.credit}</td>
                <td>RMB</td>
                <td>${item.customer.sales.name}</td>
                <td style="text-align: left">${item.remarks}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
<script type="text/javascript">
    $(document).ready(function() {
        if($("#contentTable tbody>tr").length>0) {
            //无数据报错
            var h = $(window).height();
            var w = $(window).width();
            var lastColumnWidth = w - 10 - (100+300+150+150+150+100+100+150);
            lastColumnWidth = (lastColumnWidth < 400 ? 400 : lastColumnWidth);
            $("#contentTable").toSuperTable({
                width: w-10,
                height: h - 188,
                fixedCols: 2,
                headerRows: 1,
                colWidths: [100, 300, 150, 150, 150, 150, 100, 100, lastColumnWidth],
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