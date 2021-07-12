<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>我的订单-待回访(网点)</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <script src="${ctxStatic}/sd/ServicePointOrderService.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <meta name="decorator" content="default"/>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <!-- date range picker -->
    <link href="${ctxStatic}/jquery-daterangepicker/daterangepicker.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/common/moment.min.js" type="text/javascript"></script>
    <script src="${ctxStatic}/jquery-daterangepicker/jquery.daterangepicker.min.js" type="text/javascript"></script>

    <script type="text/javascript">
        ServicePointOrderService.rootUrl = "${ctx}";

        //覆盖分页前方法
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
            var startDate = moment().subtract(1, 'M').format("YYYY-MM-DD");
            var endDate = moment().format("YYYY-MM-DD");
            $('#acceptDateRange').data('dateRangePicker').setDateRange(startDate, endDate, true);
            $("#servicePhone").val("");
            $("#userPhone").val("");
            $("#engineerName").val("");
            search();
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
        });
    </script>
    <style type="text/css">
        .dropdown-menu {
            min-width: 80px;
        }

        .dropdown-menu > li > a {
            text-align: left;
            padding: 3px 10px;
        }

        .pagination {
            margin: 10px 0;
        }

        .label-search {
            width: 70px;
            text-align: right;
        }

        .td {
            word-break: break-all;
        }

        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            background-color: Transparent;
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
    <li class="active"><a href="javascript:void(0);">待回访</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/waitingAccessoryList" title="等配件的订单列表">等配件</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/pendingList" title="停滞的订单列表">停滞</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/uncompletedList" title="未完成的订单列表">未完成</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/completedList" title="已完成的订单列表">已完成</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/returnlist" title="退单列表">退单</a></li>
    <li><a href="${ctx}/servicePoint/sd/orderList/allList" title="所有的订单列表">所有</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="order" action="${ctx}/servicePoint/sd/orderList/appCompletedList"
           method="post" class="breadcrumb form-search">
    <input id="searchType" name="searchType" type="hidden" value="accepted"/>
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
        注:当用订单编号和电话进行搜索查询时，不受接单时间等其他条件的限制。
    </div>
    <div>
        <label>订单编号：</label>&nbsp;
        <input type=text class="input-small" id="orderNo" name="orderNo" value="${order.orderNo}" maxlength="20"/>
        <label>联系人：</label>
        <input type=text class="input-mini" id="userName" name="userName" value="${order.userName}" maxlength="20"/>
        <label>电 话：</label>
        <input type=text class="input-small" id="userPhone" name="userPhone" value="${order.userPhone}" maxlength="20"/>
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
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/> &nbsp;&nbsp;
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
        <th width="100">联系人</th>
        <th width="180">地址</th>
        <th width="120">服务描述</th>
        <th width="80">安维信息</th>
        <shiro:hasPermission name="sd:order:engineeraccept">
            <th width="60">操作</th>
        </shiro:hasPermission>
        <th width="80">跟踪进度</th>
        <th width="60">完成照片</th>
        <th width="120">客服信息</th>
        <th>服务明细</th>
    </tr>
    </thead>
    <tbody>
    <c:set var="rowcnt" value="${page.list.size()}"/>
    <c:forEach items="${page.list}" var="order">
        <c:set var="rowNumber" value="${rowNumber+1}"/>
        <tr>
            <td>${rowNumber}</td>
            <td>
                <c:if test="${order.orderCondition.replyFlagKefu == 1 }">
                    <a href="javascript:void(0);" style="color: red;"><abbr title="问题反馈/回复未处理">${order.orderNo}</abbr>
                    </a>
                </c:if>
                <c:if test="${order.orderCondition.replyFlagKefu != 1}">
                    <a href="javascript:void(0);">
                        <abbr>${order.orderNo}</abbr> </a>
                </c:if><br>
                <span class="label status_${order.orderCondition.status.value}">${order.orderCondition.status.label} </span>
                <c:if test="${order.orderCondition.pendingType != null || !order.isClosed()}">
                    <br>
                    <c:if test="${order.orderCondition.pendingType.value ne '6' }">
                        <label class="">${fns:abbr(order.orderCondition.pendingType.label,20)}</label>
                    </c:if>
                </c:if>
                <c:if test="${order.orderCondition.urgentLevel.id >0}">
                    <a data-toggle="tooltip" data-tooltip="${order.orderCondition.urgentLevel.remarks}"
                       class='label label-important'>加急</a>
                </c:if>
                <c:if test="${order.orderStatus.reminderStatus >0}">
                    <c:set var="reminderStatus" value="${order.orderStatus.reminderStatus}"/>
                    <c:set var="reminderLabel" value=""/>
                    <c:choose>
                        <c:when test="${reminderStatus == 1}">
                            <c:set var="reminderLabel" value="待处理"/>
                        </c:when>
                        <c:when test="${reminderStatus == 2}">
                            <c:set var="reminderLabel" value="处理中"/>
                        </c:when>
                        <c:when test="${reminderStatus == 3}">
                            <c:set var="reminderLabel" value="已完成"/>
                        </c:when>
                        <c:when test="${reminderStatus == 4}">
                            <c:set var="reminderLabel" value="已关闭"/>
                        </c:when>
                    </c:choose>
                    <a data-toggle="tooltip" data-tooltip="${reminderLabel}" class="label label-warning">催单</a>
                </c:if>
            </td>
            <td><fmt:formatDate value="${order.orderStatus.planDate}" pattern="yyyy-MM-dd"/>
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
            <td>
                <a href="javascript:" data-toggle="tooltip"
                   data-tooltip="${order.orderCondition.servicePoint.contactInfo1} ${order.orderCondition.servicePoint.contactInfo2}">${order.orderCondition.engineer.name}</a>
            </td>
            <td>
                <shiro:hasPermission name="sd:order:engineeraccept">
                <div class="btn-group">
                    <a class="btn btn-primary dropdown-toggle"
                       data-toggle="dropdown" href="#">操作 <span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <c:if test="${ order.canPlanOrder()}">
                            <li>
                                <a href="javascript:void(0);"
                                   onclick="ServicePointOrderService.servicePointPlan('${order.id}','${order.orderNo}','${order.quarter}');">
                                    <i class="icon-truck"></i>&nbsp;派单</a>
                            </li>
                        </c:if>
                        <c:if test="${order.isAllowedForSetAppointment() == 1}">
                            <li>
                                <a href="javascript:void(0);"
                                   onclick="ServicePointOrderService.servicePointPending('${order.id}','${order.orderNo}','${order.quarter}',${order.orderCondition.reservationTimes});"><i
                                        class="icon-calendar"></i>&nbsp;预约时间</a>
                            </li>
                        </c:if>
                        <c:if test="${order.orderCondition.status.value eq '40'}">
                            <li>
                                <a href="javascript:void(0);"
                                   onclick="ServicePointOrderService.servicePointConfirmDoorAuto('${order.id}','${order.quarter}',undefined);"><i
                                        class="icon-home"></i>&nbsp;确认上门</a>
                            </li>
                        </c:if>
                        <c:if test="${order.orderCondition.status.value eq '50' and order.orderCondition.subStatus ne 70 }">
                            <li>
                                <a href="javascript:void(0);"
                                   onclick="ServicePointOrderService.servicePointComplete('${order.id}','${order.orderNo}','${order.quarter}');"><i
                                        class="icon-ok"></i>&nbsp;完成服务</a>
                            </li>
                        </c:if>
                    </ul>
                </div>
                </shiro:hasPermission>
            <td>
                <c:if test="${fns:hasServicePoint(order.orderCondition.trackingFlag)}">
                    <a href="javascript:void(0);"
                       title="<fmt:formatDate value="${order.orderCondition.trackingDate}" pattern="MM-dd" />${order.orderCondition.trackingMessage}">${fns:abbr(order.orderCondition.trackingMessage,10)}</a>
                </c:if>
                <c:if test="${ order.canTracking()}">
                    <a href="javascript:void(0);"
                       onclick="ServicePointOrderService.servicePointTracking('${order.id}','${order.quarter}');"
                       class="btn btn-mini btn-primary">进度</a>
                </c:if>
            </td>
            <td>
                <a href="javascript:void(0);"
                   onclick="ServicePointOrderService.browsePhotoList('${order.id}','${order.quarter}',${fns:isNewOrder(order.orderNo)});"
                   class="btn btn-mini btn-primary">查看</a>
            </td>
            <td>
                    ${order.orderCondition.kefu.name }
                <a style="padding-left: 20px;" target="_blank"
                   href="http://wpa.qq.com/msgrd?v=3&uin=${order.orderCondition.kefu.qq}&site=qq&menu=yes">
                    <img border="0" src="http://wpa.qq.com/pa?p=2:572202493:52" alt="点这发消息"
                         title="联系客服QQ${order.orderCondition.kefu.qq}"/>
                </a>
                <br>${order.orderCondition.kefu.phone }
            </td>
            <td>
                <c:forEach items="${order.items}" var="item"
                           varStatus="i" begin="0">
                    ${item.brand }&nbsp;${item.product.name }&nbsp;${item.serviceType.name }&nbsp;&nbsp;数量:${item.qty }
                    <br/>
                </c:forEach>
            </td>
        </tr>
    </c:forEach>

    </tbody>
</table>
<c:if test="${rowcnt > 0}">
    <div class="pagination">${page}</div>
</c:if>
</body>
</html>
