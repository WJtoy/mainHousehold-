<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>我的订单-已完成(网点)</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/ServicePointOrderService.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <!-- date range picker -->
    <link href="${ctxStatic}/jquery-daterangepicker/daterangepicker.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/common/moment.min.js" type="text/javascript"></script>
    <script src="${ctxStatic}/jquery-daterangepicker/jquery.daterangepicker.min.js" type="text/javascript"></script>

    <script type="text/javascript">
        ServicePointOrderService.rootUrl = "${ctx}";

        function beforePage() {
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            $("#btnClearSearch").attr('disabled', 'disabled');
            layerLoading("查询中...", true);
        }

        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function () {
            if (clicktag == 0) {
                clicktag = 1;
                beforePage();
                setPage();
                this.form.submit();
            }
        });

        function openjBox(url, title, width, height) {
            top.$.jBox.open("iframe:" + url, title, width, height,
                {
                    top: '5%',
                    buttons: {},
                    loaded: function (h) {
                        $("#jbox-iframe", h).prop("height", "98%");
                    }
                });
        }

        $(document).on("click", "#btnClearSearch", function () {
            $("#searchForm")[0].reset();
            $("#orderNo").val("");
            $("#userName").val("");
            $("#engineerName").val("");
            $("#isEngineerInvoiced").attr('checked', false);
            var startDate = moment().subtract(1, 'M').format("YYYY-MM-DD");
            var endDate = moment().format("YYYY-MM-DD");
            $('#acceptDateRange').data('dateRangePicker').setDateRange(startDate, endDate, true);
            $('#completeDateRange').data('dateRangePicker').clear();
            $("#phone").val("");
            $("#areaIdsId").val("");
            $("#areaIdsName").val("");
            search();
        });

        $(document).on("click", "#btn-reset-completeDateRange", function () {
            $('#completeDateRange').data('dateRangePicker').clear();
        });

        $(document).ready(function () {
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipnorth]').darkTooltip(
                {
                    gravity: 'north'
                });
            $('a[data-toggle=tooltipeast]').darkTooltip(
                {
                    gravity: 'east'
                });
            $("#acceptDateRange").dateRangePicker({
                language: 'cn',
                autoClose: true,
                startOfWeek: 'monday',
                separator: ' ~ ',
                format: 'YYYY-MM-DD',
                time: {
                    enabled: false
                },
                minDays: 1,
                maxDays: 90,
                showWeekNumbers: true,
                selectForward: true,
                shortcuts: null,
                showShortcuts: true,
                customShortcuts:
                    [
                        {
                            name: '90天',
                            dates: function () {
                                // var start = moment().subtract(3,'M').toDate();
                                var start = moment().subtract(89, 'd').toDate();
                                var end = moment().toDate();
                                return [start, end];
                            }
                        },
                        {
                            name: '最近两个月',
                            dates: function () {
                                var start = moment().subtract(2, 'M').toDate();
                                var end = moment().toDate();
                                return [start, end];
                            }
                        },
                        {
                            name: '最近-个月',
                            dates: function () {
                                var start = moment().subtract(1, 'M').toDate();
                                var end = moment().toDate();
                                return [start, end];
                            }
                        }
                    ]
            });
            $("#completeDateRange").dateRangePicker({
                language: 'cn',
                autoClose: true,
                startOfWeek: 'monday',
                separator: ' ~ ',
                format: 'YYYY-MM-DD',
                time: {
                    enabled: false
                },
                minDays: 0,
                maxDays: 365,
                showWeekNumbers: true,
                selectForward: true,
                shortcuts: null,
                showShortcuts: true,
                customShortcuts:
                    [
                        {
                            name: '最近三个月',
                            dates: function () {
                                var start = moment().subtract(3, 'M').toDate();
                                var end = moment().toDate();
                                return [start, end];
                            }
                        },
                        {
                            name: '最近两个月',
                            dates: function () {
                                var start = moment().subtract(2, 'M').toDate();
                                var end = moment().toDate();
                                return [start, end];
                            }
                        },
                        {
                            name: '最近-个月',
                            dates: function () {
                                var start = moment().subtract(1, 'M').toDate();
                                var end = moment().toDate();
                                return [start, end];
                            }
                        }
                    ]
            });
        });
    </script>

    <style type="text/css">
        .dropdown-menu {
            min-width: 80px
        }

        .dropdown-menu > li > a {
            text-align: left;
            padding: 3px 10px
        }

        .pagination {
            margin: 10px 0
        }

        .label-search {
            width: 70px;
            text-align: right
        }

        .td {
            word-break: break-all
        }

        .table tbody td, .table thead th {
            text-align: center;
            vertical-align: middle;
            background-color: Transparent
        }
    </style>
</head>

<body>
<ul id="navtabs" class="nav nav-tabs">
    <li><a href="${ctx}/servicePoint/sd/orderList/reminderList" title="催单待回复">催单</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/noAppointmentList" title="未预约的订单">未预约</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/arriveAppointmentList" title="预约到期的订单">预约到期</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/passAppointmentList" title="预约超期的订单列表">预约超期</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/servicedList" title="待完成的订单列表">待完成</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/appCompletedList" title="待回访的订单列表">待回访</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/waitingAccessoryList" title="等配件的订单列表">等配件</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/pendingList" title="停滞的订单列表">停滞</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/uncompletedList" title="未完成的订单列表">未完成</a></li>
    <li class="active"><a href="javascript:void(0);">已完成</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/returnlist" title="退单列表">退单</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/allList" title="所有的订单列表">所有</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="order" action="${ctx}/servicePoint/sd/orderList/completedList" method="post"
           class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
        注:当用订单编号和电话进行搜索查询时，不受接单时间等其他条件的限制。
    </div>
    <div>
        <label>订单号：</label>&nbsp;<input type=text class="input-small" id="orderNo" name="orderNo"
                                        value="${order.orderNo}" maxlength="20"/>
        <label>联系人：</label><input type=text class="input-small" id="userName" name="userName" value="${order.userName}"
                                  maxlength="20"/>
        <label>电 话：</label><input type=text class="input-small" id="userPhone" name="userPhone"
                                  value="${order.userPhone}" maxlength="20"/>
        <label>安维：</label>
        <form:select path="searchEngineerId" class="input-small" style="width:180px;">
            <form:option value="0" label="所有"/>
            <form:options items="${order.engineerList}" itemLabel="name" itemValue="id" htmlEscape="false"/>
        </form:select>
    </div>
    <div style="margin-top: 8px;">
        <label>接单时间：</label>
        <input id="acceptDateRange" name="acceptDateRange" type="text" style="width:185px;margin-left:4px"
               maxlength="25" class="input-xlarge Wdate"
               value="${fns:formatDate(order.beginAcceptDate,'yyyy-MM-dd')} ~ ${fns:formatDate(order.endAcceptDate,'yyyy-MM-dd')}"/>
        &nbsp;&nbsp;
        <label>完成时间：</label>
        <input id="completeDateRange" name="completeDateRange" type="text" style="width:185px;margin-left:4px"
               maxlength="25" class="input-xlarge Wdate"
               value="${fns:formatDate(order.beginCompleteDate,'yyyy-MM-dd')} ~ ${fns:formatDate(order.endCompleteDate,'yyyy-MM-dd')}"/>
        <a href="javascript:;" id="btn-reset-completeDateRange" class="icon-remove"></a>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>
        &nbsp;&nbsp;
        <input id="btnClearSearch" class="btn btn-primary" type="button" value="清除条件"/>
    </div>
</form:form>
<sys:message content="${message}"/>
<c:set var="rowNumber" value="0"/>
<table id="contentTable" class="table table-bordered table-condensed table-striped" style="table-layout:fixed;"
       cellspacing="0" width="100%">
    <thead>
    <tr>
        <th width="30">序号</th>
        <th width="110">单号</th>
        <th width="80">接单时间</th>
        <th width="80">完成时间</th>
        <th width="100">联系信息</th>
        <th width="200">地址</th>
        <th width="140">服务描述</th>
        <th width="70">安维姓名</th>
        <th width="100">费用明细</th>
        <th width="80">转入余额时间</th>
        <th width="60">完成照片</th>
        <th width="120">客服信息</th>
        <th width="160">实际服务明细</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${page.list}" var="order">
        <c:set var="rowNumber" value="${rowNumber+1}"/>
        <tr>
            <td>${rowNumber}</td>
            <td><a href="javascript:void(0);">
                <abbr>${order.orderNo}</abbr> </a><br>
                <c:choose>
                    <c:when test="${order.orderCondition.status.value eq 50}"><label
                            title="安维手机APP上完成，待客服回访，客评">待回访</label> </c:when>
                    <c:when test="${order.orderCondition.status.value eq 80 && order.orderCondition.chargeFlag ==0}">
                        <a href="javascript:" data-toggle="tooltip" data-tooltip="待财务审核" style="cursor: pointer;">
                            <span class="label status_60">${order.orderCondition.status.label}</span>
                        </a>
                    </c:when>
                    <c:otherwise><span
                            class="label status_${order.orderCondition.status.value}">${order.orderCondition.status.label}</span></c:otherwise>
                </c:choose>
            </td>
            <td><fmt:formatDate value="${order.orderStatus.planDate}" pattern="yyyy-MM-dd"/>
            </td>
            <td><fmt:formatDate value="${order.orderCondition.closeDate}" pattern="yyyy-MM-dd"/>
            </td>
            <td>${order.orderCondition.userName}<br>
                    ${order.orderCondition.servicePhone}
            </td>
            <td>
                <a href="javascript:" data-toggle="tooltip"
                   data-tooltip="${order.orderCondition.area.name}&nbsp;${order.orderCondition.serviceAddress}">${order.orderCondition.area.name}</a>
            </td>
            <td>
                <a href="javascript:" data-toggle="tooltip"
                   data-tooltip="${order.description}">${fns:abbr(order.description,20)}</a>
            </td>
            <td>${order.orderCondition.engineer.name}</td>
            <td>
                <a style="color: green;"><b>${order.orderFee.engineerTotalCharge}</b></a>元
                <br>
                <c:if test="${order.orderFee.engineerServiceCharge ne 0}">服务费：${order.orderFee.engineerServiceCharge}
                    <br></c:if>
                <c:if test="${order.orderFee.engineerMaterialCharge ne 0}">配件费：${order.orderFee.engineerMaterialCharge}
                    <br></c:if>
                <c:if test="${order.orderFee.engineerTravelCharge ne 0}">远程费：${order.orderFee.engineerTravelCharge}
                    <br></c:if>
                <c:if test="${order.orderFee.engineerOtherCharge ne 0}">其他：${order.orderFee.engineerOtherCharge}
                    <br></c:if>

            </td>
            <td><fmt:formatDate value="${order.orderStatus.chargeDate}" pattern="yyyy-MM-dd"/></td>
            <td>
                <a href="javascript:void(0);"
                   onclick="ServicePointOrderService.browsePhotoList('${order.id}','${order.quarter}',${fns:isNewOrder(order.orderNo)});"
                   class="btn btn-mini btn-primary">查看</a>
            </td>
            <td>
                    ${order.orderCondition.kefu.name }
                <a style="padding-left: 20px;" target="_blank"
                   href="http://wpa.qq.com/msgrd?v=3&uin=${order.orderCondition.kefu.qq}&site=qq&menu=yes"><img
                        border="0" src="http://wpa.qq.com/pa?p=2:572202493:52" alt="点这发消息"
                        title="联系客服QQ ${order.orderCondition.kefu.qq}"/></a>
                <br>${order.orderCondition.kefu.phone }
            </td>
            <td>
                <c:forEach items="${order.detailList}" var="detail" varStatus="i" begin="0">
                    ${detail.brand}&nbsp;
                    ${detail.product.name }&nbsp;${detail.serviceType.name }&nbsp;&nbsp;数量:${detail.qty}<br/>
                </c:forEach>
            </td>
        </tr>
    </c:forEach>

    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>
