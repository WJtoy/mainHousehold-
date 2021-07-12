<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>订单处理-突击列表</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <%@ include file="/WEB-INF/views/include/WdateLimitPicker.jsp" %>
    <style type="text/css">
        .table tbody td,.table thead th{text-align:center;vertical-align:middle}
    </style>
    <script type="text/javascript">
        top.layer.closeAll();
        Order.rootUrl = "${ctx}";

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
        $(document).on("click", "#btnClearSearch", function () {
            $("#searchForm")[0].reset();
            $("#areaId").val("");
            $("#areaName").val("");
            var startDate = moment().subtract(3,'M').format("YYYY-MM-01");
            var endDate = moment().format("YYYY-MM-DD");
            $("#beginDate").val(startDate);
            $("#endDate").val(endDate);
            $("#customerId").val("");
            $("#customerName").val("");
            $("#orderNo").val("");
            $("#phone1").val("");
            $("[id='engineer.name']").val("");
            $("[id='engineer.phone']").val("");
            $("#serviceAddress").val("");
            $("#userName").val("");
            $("#dataSource").val("0");
            $("#s2id_dataSource").find("span.select2-chosen").html('所有');
            $("[id='urgentLevel.id']").val("0");
            $("[id='s2id_urgentLevel.id']").find("span.select2-chosen").html('');
            resetShop();
            $("#productId").val("");
            $("#s2id_productId").find("span.select2-chosen").html('所有');
            $("#serviceTypeId").val("");
            $("#s2id_serviceTypeId").find("span.select2-chosen").html('所有');
            // search();
        });

        $(document).on("click", "#btnAdvanceSearch", function () {
            if ($(this).hasClass("collapsed")) {
                $(this).removeClass("collapsed");
                $("#div_search_customer").show();
            } else {
                $(this).addClass("collapsed");
                $("#div_search_customer").hide();
            }
        });
    </script>

</head>

<body>
<ul id="navtabs" class="nav nav-tabs">
    <li><a href="${ctx}/technology/sd/orderList/reminderlist" title="催单的工单">催单</a></li>
    <li><a href="${ctx}/technology/sd/orderList/planinglist" title="待接派的工单列表">待接派单</a></li>
    <li><a href="${ctx}/technology/sd/orderList/noAppointmentList" title="未预约的工单">未预约</a></li>
    <li><a href="${ctx}/technology/sd/orderList/arriveAppointmentList" title="预约到期的工单">预约到期</a></li>
    <li><a href="${ctx}/technology/sd/orderList/passAppointmentList" title="预约超期的工单">预约超期</a></li>
    <li><a href="${ctx}/technology/sd/orderList/servicedList" title="待回访的工单">待回访</a></li>
    <li><a href="${ctx}/technology/sd/orderList/followUpFailList" title="回访失败的工单">回访失败</a></li>
    <li><a href="${ctx}/technology/sd/orderList/tmallAnomalyList" title="天猫一键求助">求助</a></li>
    <li><a href="${ctx}/technology/sd/orderList/tmallServiceMonitorList" title="天猫预警">预警</a></li>
    <li class="active"><a href="javascript:void(0);" title="突击中的工单">突击单<span id="spn_order_count" class="badge badge-info">${page !=null ?page.count:''}</span></a></li>
    <li><a href="${ctx}/technology/sd/orderList/complainlist" title="投诉的工单">投诉</a></li>
    <li><a href="${ctx}/technology/sd/orderList/pendinglist" title="需要等待的工单">停滞</a></li>
    <li><a href="${ctx}/technology/sd/orderList/uncompletedList" title="未完成的工单">未完成</a></li>
    <li><a href="${ctx}/technology/sd/orderList/completedList" title="已完成的工单">已完成</a></li>
    <li><a href="${ctx}/technology/sd/orderList/alllist" title="所有工单">所有</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="order" method="post" class="form-inline">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="searchType" name="searchType" type="hidden" value="processing"/>
    <div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
        注:当用工单号和手机号码进行搜索查询时，不受当前时间、地区等其他条件的限制。
    </div>
    <div class="control-group">
        <label class="label-search">工单号：</label>&nbsp;
        <input type=text class="input-small" id="orderNo" name="orderNo" maxlength="20" value="${order.orderNo }"/>
        <label>用户：</label>
        <input type=text class="input-mini" id="userName" name="userName" maxlength="20" value="${order.userName}"/>
        <label>用户电话：</label>
        <input type=text class="input-small" id="phone1" name="phone1" value="${order.phone1}" maxlength="20"
               placeholder="用户电话 或 实际联络电话"/>
        <%--<label>安维姓名：</label>--%>
        <%--<input type=text class="input-mini" id="engineer.name" name="engineer.name" maxlength="20" <c:if--%>
                <%--test="${order!=null && order.engineer != null}"> value="${order.engineer.name}"</c:if>--%>
               <%--placeholder="安维姓名"/> &nbsp;&nbsp;--%>
        <%--<label>安维手机：</label>--%>
        <%--<input type=text class="input-small" id="engineer.phone" name="engineer.phone" maxlength="20" <c:if--%>
                <%--test="${order!=null && order.engineer != null}"> value="${order.engineer.phone}"</c:if>--%>
               <%--placeholder="安维手机"/>--%>
        <label>安维师傅：</label>
        <md:engselectforkefu id="engineer" name="engineer.id" value="${order.engineer.id}"
                             labelName="engineer.name" labelValue="${order.engineer.name}"
                             width="1200" height="780" allowClear="true" title="选择安维人员" />
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>&nbsp;&nbsp;
        <input id="btnClearSearch" class="btn btn-primary" type="button" value="清除条件"/>&nbsp;&nbsp;
        <a href="javascript:void(0);" id="btnAdvanceSearch" class="btn btn-info accordion-toggle collapsed">高级查询 <span
                class="arrow"></span> </a></a>
    </div>
    <div style="margin-top:0px; display:none;" id="div_search_customer">
        <div>
            <label class="label-search">客户：</label>
            <sys:treeselect id="customer" name="customer.id" value="${order.customer.id}" labelName="customer.name"
                            labelValue="${order.customer.name}" title="客户"
                            url="/md/customer/treeData?kefu=${currentuser.id}"
                            cssClass="input-small" allowClear="true" cssStyle="width:211px;"/>
            <label>区域：</label>
            <%--
            <sys:treeselect id="area" name="area.id" value="${order.area.id}" nodeLevel="true"
                            levelValue="${order.areaLevel}"
                            labelName="area.name" labelValue="${order.area.name }" title="区域"
                            url="/sys/area/treeData?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1"
                            nameLevel="3"/>
                            --%>
            <sys:treeselectareanew id="area" name="area.id" value="${order.area.id}" levelValue="${order.areaLevel}"
                                   labelName="area.name" labelValue="${order.area.name}" title="区域" clearIdValue="0"
                                   url="/sys/area/treeDataNew?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1" nameLevel="4"/>
            <label>加急：</label>
            <form:select path="urgentLevel.id" class="input-small" style="width:125px;">
                <form:option value="0" label=""/>
                <c:if test="${!empty order.urgentLevels}">
                    <form:option value="1" label="所有"/>
                    <form:options items="${order.urgentLevels}" itemLabel="remarks" itemValue="id" htmlEscape="false"/>
                </c:if>
            </form:select>
        </div>
        <div style="margin-top:5px;">
            <label class="label-search">客户单号：</label>&nbsp;
            <input type=text class="input-small" id="parentBizOrderId" name="parentBizOrderId" maxlength="30" value="${order.parentBizOrderId}"/>
            <label class="label-search">工单来源：</label>
            <form:select path="dataSource" class="input-small" style="width:125px;">
                <form:option value="0" label="所有"/>
                <form:options items="${fns:getDictListFromMS('order_data_source')}" itemLabel="label" itemValue="value"
                              htmlEscape="false"/>
            </form:select>
            <sd:b2BShop value="${order.shopId}" width="125" shopList="${order.shopList}"></sd:b2BShop>
            <%--<label>地址：</label>
            <input type=text class="input-small" id="serviceAddress" name="serviceAddress"
                   value="${order.serviceAddress}" maxlength="100"/>--%>
            <label>产品：</label>
            <form:select path="productId" cssClass="input-small" cssStyle="width:125px;">
                <form:option value="" label="所有"/>
                <form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
            </form:select>
            <label>服务项目：</label>
            <form:select path="serviceTypeId" class="input-small" style="width:125px;">
                <form:option value="" label="所有"/>
                <form:options items="${fns:getServiceTypes()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
            </form:select>&nbsp;&nbsp;
            <label>下单日期：</label>
            <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:95px;margin-left:4px"
                   maxlength="20" class="input-small Wdate" value="${fns:formatDate(order.beginDate,'yyyy-MM-dd')}"  />
            <label>~</label>&nbsp;&nbsp;&nbsp;
            <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:95px" maxlength="20"
                   class="input-small Wdate" value="${fns:formatDate(order.endDate,'yyyy-MM-dd')}" />

        </div>&nbsp;
    </div>

</form:form>
<sys:message content="${message}"/>
<c:set var="rowNumber" value="0"/>
<c:set var="processTimeout" value="${fns:getDictSingleValueFromMS('order_process_timeout', '48')}"/><%-- 切换为微服务 --%>
<table id="contentTable" class="table table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="40">序号</th>
        <th width="145">单号</th>
        <th width="40">状态</th>
        <th width="40">突击进度</th>
        <th width="100">来源</th>
        <th>品牌</th>
        <th>产品</th>
        <th width="65">服务类型</th>
        <th width="50">数量</th>
        <th width="65">安维</th>
        <th width="65">用户</th>
        <th width="100">手机</th>
        <th>安装地址</th>
        <th width="80">进度跟踪</th>
        <th width="80">问题反馈</th>
        <th width="80">停滞原因</th>
        <th width="60">配件</th>
    </tr>
    </thead>
    <tbody>
    <c:set var="rowcnt" value="${page.list.size()}"/>
    <c:forEach items="${page.list}" var="order">
        <c:set var="isProcessTimeout" value="${ order.isProcessTimeout(processTimeout)}"/>
        <c:set var="rowNumber" value="${rowNumber+1}"/>
        <c:set var="rowspan" value="${order.items.size() eq 0?1:order.items.size() }"/>
        <tr>
            <c:choose>
                <c:when test="${ isProcessTimeout eq true}">
                    <td rowspan="${rowspan}">
                        <span title="订单处理超过已 ${ processTimeout} 小时"><span
                                class="badge badge-important">${rowNumber}</span>
                    </span>
                    </td>
                </c:when>
                <c:otherwise>
                    <td rowspan="${rowspan}">${rowNumber}</td>
                </c:otherwise>
            </c:choose>
           <%-- <c:set var="isComplained" value="${order.orderCondition.isComplained>0?true:false}"/>--%>
            <c:set var="isComplained" value="${order.orderStatus.complainFlag>0?true:false}"/>
            <c:set var="complainLabel" value=""/>
            <c:set var="complainClass" value="warning"/>
            <c:if test="${isComplained}">
                <c:set var="complainFormStatus" value="${order.complainFormStatus}"/>
                <c:set var="complainLabel" value="${complainFormStatus ==null?'':complainFormStatus.label}"/>
                <c:choose>
                    <c:when test="${complainFormStatus == null}">
                        <c:set var="complainClass" value="warning"/>
                    </c:when>
                    <c:when test="${complainFormStatus.value == '0'}">
                        <c:set var="complainClass" value="warning"/>
                    </c:when>
                    <c:when test="${complainFormStatus.value == '1'}">
                        <c:set var="complainClass" value="info"/>
                    </c:when>
                    <c:when test="${complainFormStatus.value == '2'}">
                        <c:set var="complainClass" value="success"/>
                    </c:when>
                    <c:when test="${complainFormStatus.value == '3'}">
                        <c:set var="complainClass" value="important"/>
                    </c:when>
                    <c:when test="${complainFormStatus.value == '4'}">
                        <c:set var="complainClass" value=""/>
                    </c:when>
                </c:choose>
            </c:if>
            <td rowspan="${rowspan}">
                <c:choose>
                    <c:when test="${order.isNotAccept(30)}">
                        <a href="javascript:void(0);" style="color: red;" onclick="Order.showKefuOrderDetail('${order.id}','${order.quarter}',${order.orderCondition.orderServiceType});">
                            <abbr title="30分钟无人接单">${order.orderNo}</abbr>
                            <c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}">(重)</abbr></c:if>
                        </a>
                    </c:when>
                    <c:when test="${order.orderCondition.appAbnormalyFlag == 1}">
                        <a href="javascript:void(0);" style="color: red;" onclick="Order.showKefuOrderDetail('${order.id}','${order.quarter}',${order.orderCondition.orderServiceType});">
                            <abbr title="手机标识异常">${order.orderNo}</abbr>
                            <c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}">(重)</abbr></c:if>
                        </a>
                    </c:when>
                    <c:when test="${order.canBackApprove()}">
                        <a href="javascript:void(0);" style="color: red;" onclick="Order.showKefuOrderDetail('${order.id}','${order.quarter}',${order.orderCondition.orderServiceType});">
                            <abbr title="订单派单2小时未预约">${order.orderNo}</abbr>
                            <c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}">(重)</abbr></c:if>
                        </a>
                    </c:when>
                    <c:when test="${order.orderCondition.replyFlagCustomer == 1}">
                        <a href="javascript:void(0);" style="color: red;" onclick="Order.showKefuOrderDetail('${order.id}','${order.quarter}',${order.orderCondition.orderServiceType});">
                            <abbr title="问题反馈或回复未处理">${order.orderNo}</abbr>
                            <c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}">(重)</abbr></c:if>
                        </a>
                    </c:when>
                    <c:when test="${not empty order.repeateNo}">
                        <a href="javascript:void(0);" onclick="Order.showKefuOrderDetail('${order.id}','${order.quarter}',${order.orderCondition.orderServiceType});">
                            <abbr title="疑似重复订单，相关单号:${order.repeateNo}">${order.orderNo}(重)</abbr>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="javascript:void(0);" onclick="Order.showKefuOrderDetail('${order.id}','${order.quarter}',${order.orderCondition.orderServiceType});">
                            <abbr title="查看订单详情">${order.orderNo}</abbr>
                            <c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}">(重)</abbr></c:if>
                        </a>
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${not empty order.orderCondition.engineer and order.orderCondition.engineer.appLoged==1}">
                        <img src="${ctxStatic}/images/phone.png" title="登录过APP">
                    </c:when>
                    <c:otherwise>
                        <img src="${ctxStatic}/images/people.png" title="未登录过APP">
                    </c:otherwise>
                </c:choose>
                <c:if test="${isComplained}">
                    <a data-toggle="tooltip" data-tooltip="${complainLabel}" class="label label-${complainClass}">投诉</a>
                </c:if>
                <c:if test="${order.orderCondition.urgentLevel.id >0}">
                    <a data-toggle="tooltip" data-tooltip="${order.orderCondition.urgentLevel.remarks}" class='label label-important' style="background-color: ${order.orderCondition.urgentLevel.markBgcolor};">加急</a>
                </c:if>
                <c:if test="${order.orderStatus.reminderStatus >0}">
                    <c:set var="reminderStatus" value="${order.orderStatus.reminderStatus}"/>
                    <c:set var="reminderLabel" value=""/>
                    <c:choose>
                        <c:when test="${reminderStatus == 1}">
                            <c:set var="reminderLabel" value="待回复" />
                        </c:when>
                        <c:when test="${reminderStatus == 2}">
                            <c:set var="reminderLabel" value="已回复" />
                        </c:when>
                        <c:when test="${reminderStatus == 3}">
                            <c:set var="reminderLabel" value="已处理" />
                        </c:when>
                        <c:when test="${reminderStatus == 4}">
                            <c:set var="reminderLabel" value="完成" />
                        </c:when>
                    </c:choose>
                    <a data-toggle="tooltip" data-tooltip="${reminderLabel}" class="label label-warning">催单</a>
                </c:if>
                <c:if test="${order.orderStatusFlag!=null && order.orderStatusFlag.praiseStatus>0}">
                    <a data-toggle="tooltip" data-tooltip="好评" style="text-decoration: none;"><img src="${ctxStatic}/images/praise.png" style="width: 15px;height: 15px"></a>
                </c:if>
            </td>
            <td rowspan="${rowspan}">
                <span class="label status_${order.orderCondition.status.value}">${order.orderCondition.status.label}</span>
            </td>
            <td rowspan="${rowspan}">${order.orderCondition.rushOrderFlag eq 2?"已完成":"未完成"}</td>
            <td rowspan="${rowspan}">${order.dataSource.label}<br>${order.b2bShop==null?"":order.b2bShop.shopName}</td>
            <c:set var="item" value="${order.items[0]}"/>
            <td>${item.brand }</td>
            <td>
                <a href="javascript:" data-toggle="tooltip"
                   data-tooltip="${item.productSpec}">${item.product.name } </a>
            </td>
            <td>${item.serviceType.name }</td>
            <td>${item.qty}</td>
            <td rowspan="${rowspan}">
                <a href="javascript:void(0);" data-toggle="tooltip"
                   data-tooltip="${order.orderCondition.servicePoint==null?'':order.orderCondition.servicePoint.servicePointNo} ">${order.orderCondition.engineer==null?'':order.orderCondition.engineer.name}</a>
            </td>
            <td rowspan="${rowspan}">${order.orderCondition.userName}</td>
            <td rowspan="${rowspan}">
                    ${order.orderCondition.servicePhone}
                <c:if test="${!empty order.orderCondition.phone2}">
                    <br/>${order.orderCondition.phone2}
                </c:if>
            </td>
            <td rowspan="${rowspan}">
                <a href="javascript:void(0);" data-toggle="tooltip"
                   data-tooltip="${order.orderCondition.area.name}&nbsp;${order.orderCondition.serviceAddress}">${fns:abbr(order.orderCondition.area.name,40)}</a>
            </td>
            <td rowspan="${rowspan}">
                <c:if test="${order.orderCondition.trackingFlag ge 1}">
                    <c:if test="${!empty order.orderCondition.trackingMessage }">
                        <a href="javascript:void(0);"
                           data-toggle="tooltip"
                           data-tooltip="${fns:formatDate(order.orderCondition.trackingDate,'yyyy-MM-dd')} - ${order.orderCondition.trackingMessage}">${fns:abbr(order.orderCondition.trackingMessage,20)}</a>
                    </c:if>
                </c:if>
            </td>
            <td rowspan="${rowspan}">
                <c:choose>
                    <c:when test="${order.orderCondition.feedbackFlag eq 1}">
                        <c:if test="${order.orderCondition.replyFlag eq 2}">
                            <img id="complain_${order.orderCondition.feedbackId}" style="width:24px;height:24px;"
                                 src="${ctxStatic}/images/complain.gif"/>
                        </c:if>
                        <a href="javascript:void(0);"
                           onclick="Order.replylist('${order.orderCondition.feedbackId}','${order.quarter}','${order.orderNo}');"
                           data-toggle="tooltip" data-tooltip="${order.orderCondition.feedbackTitle}"
                           style="display: block;">
                                ${fns:abbr(order.orderCondition.feedbackTitle,20)} </a>
                    </c:when>
                    <c:otherwise>
                    </c:otherwise>
                </c:choose>
            </td>
            <td rowspan="${rowspan}">
                <c:set var="pendingTypeStr" value=""/>
                <c:if test="${!empty order.orderCondition.pendingType && !order.isClosed()}">
                    <c:set var="isGreaterNow" value="true"/>
                    <c:if test="${!empty order.orderCondition.appointmentDate }">
                        <c:set var="pendingTypeStr" value="${pendingTypeStr.concat(' 预约日期:')}"/>
                        <c:set var="isGreaterNow" value="${fns:isGreaterNow(order.orderCondition.appointmentDate) }"/>
                        <c:set var="pendingTypeStr"
                               value="${pendingTypeStr.concat(fns:formatDate(order.orderCondition.appointmentDate,'yyyy-MM-dd HH:mm'))}"/>
                    </c:if>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${pendingTypeStr}">
                        <span class="${!isGreaterNow?'badge badge-important':'' }">${fns:abbr(order.orderCondition.pendingType.label,20)}</span>
                    </a>
                    <br/>
                </c:if>
            </td>
            <td rowspan="${rowspan}">
                <c:if test="${order.orderCondition.partsFlag == 1}">
                    <i class="icon-cogs" title="已申请配件,点击查看" style="cursor: pointer;"
                       onclick="Order.attachlist('${order.id}','${order.orderNo}','${order.quarter}');"></i>
                </c:if>
            </td>
        </tr>
        <c:forEach items="${order.items}" var="item" varStatus="i" begin="1">
            <tr class="item">
                <td>${item.brand }</td>
                <td>
                    <a href="javascript:" data-toggle="tooltip"
                       data-tooltip="${item.productSpec}">${item.product.name } </a>
                </td>
                <td>${item.serviceType.name }</td>
                <td>${item.qty}</td>
            </tr>
        </c:forEach>

    </c:forEach>
    </tbody>
</table>
<c:if test="${rowcnt > 0}">
    <div id="pagination" class="pagination">${page}</div>
</c:if>
<script type="text/javascript" language="javascript">
    $(document).ready(function () {

        $('a[data-toggle=tooltip]').darkTooltip();
        $('a[data-toggle=tooltipnorth]').darkTooltip({gravity: 'north'});
        $('a[data-toggle=tooltipeast]').darkTooltip({gravity: 'east'});
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
        margin: 4px 0 0 4px;
    }

    .label-search {
        width: 70px;
        text-align: right;
    }

    form {
        margin: 0 0 5px;
    }
</style>
<script type="text/javascript">
    $(document).ready(function () {
        <c:if test="${order != null}">
        <c:if test="${currentuser.userType <3}">
        $("#customerIdName").val('${order.customer.name}');
        </c:if>
        </c:if>
        oneYearDatePicker('beginDate','endDate');
    });
</script>
</body>
</html>
