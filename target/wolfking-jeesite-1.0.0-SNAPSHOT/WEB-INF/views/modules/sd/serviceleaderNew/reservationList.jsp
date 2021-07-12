<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>异常工单-爽约列表</title>
    <meta name="description" content="预约时间两次及以上工单">
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            /*background: transparent;*/
        }
    </style>
    <script type="text/javascript">
        top.layer.closeAll();
        Order.rootUrl = "${ctx}";

        //覆盖分页前方法
        function beforePage() {
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            // $btnSubmit.val("...");
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
            $("#servicePointId").val("");
            $("#servicePointName").val("");
            var dateStr = DateFormat.format(new Date(), 'yyyy-MM-dd');
            $("#endDate").val(dateStr);
            $("#beginDate").val(DateFormat.format(DateFormat.addMonthStr(dateStr, -3), 'yyyy-MM-01'));
            $("#customerId").val("");
            $("#customerName").val("");
            $("#orderNo").val("");
            $("#phone1").val("");
            $("[id='engineer.name']").val("");
            $("[id='engineer.phone']").val("");
            $("#userName").val("");
            $("#productId").val("");
            $("#s2id_productId").find("span.select2-chosen").html('所有');
            $("#serviceTypeId").val("");
            $("#s2id_serviceTypeId").find("span.select2-chosen").html('所有');
            $("#dataSource").val("0");
            $("#s2id_dataSource").find("span.select2-chosen").html('所有');
            search();
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
    <%--<li><a href="${ctx}/sd/order/serviceLeaderNew/delaylist" title="下单超1小时未接单">延时</a></li>--%>
    <li class="active"><a href="javascript:void(0);" title="预约时间两次及以上">爽约</a></li>
    <%--<li><a href="${ctx}/sd/order/serviceLeaderNew/complainlist" title="被厂商或用户投诉的工单">投诉单</a></li>
    <li ><a href="${ctx}/sd/order/serviceLeaderNew/travellist" title="远程费超20的工单">远程单</a></li>--%>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="order" action="${ctx}/sd/order/serviceLeaderNew/reservationlist" method="post" class="form-inline">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="searchType" name="searchType" type="hidden" value="processing" />
    <input id="repageFlag" name="repageFlag" type="hidden" value="false" />
    <div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
        注:当用工单号和手机号码进行搜索查询时，不受当前时间、地区等其他条件的限制。
    </div>
    <div class="control-group">
        <label class="label-search">订单号：</label>&nbsp;
        <input type=text class="input-small" id="orderNo" name="orderNo" value="${order.orderNo}" maxlength="20" />
        <label>用户：</label>
        <input type=text class="input-mini" id="userName" name="userName" value="${order.userName}" maxlength="20" />
        <label>用户电话：</label>
        <input type=text class="input-small digits" id="phone1" name="phone1" value="${order.phone1}" placeholder="用户电话 或 实际联络电话" maxlength="20" />
        <label>安维姓名：</label>
        <input type=text class="input-mini" id="engineer.name" name="engineer.name" maxlength="20" <c:if test="${order!=null && order.engineer != null}"> value="${order.engineer.name}"</c:if> placeholder="安维姓名"/> &nbsp;&nbsp;
        <label>安维手机：</label>
        <input type="text" class="input-small" id="engineer.phone" name="engineer.phone" maxlength="20" <c:if test="${order!=null && order.engineer != null}"> value="${order.engineer.phone}"</c:if> placeholder="安维手机"/>&nbsp;&nbsp;
        <label>预约次数：</label>
        <input type="text" class="input-mini" id="reservationTime" name="reservationTime" maxlength="5" placeholder="预约次数" <c:if test="${order!=null}"> value="${order.reservationTime}"</c:if>
               onkeyup="this.value=this.value.replace(/[^0-9-]+/,'');"
               onafterpaste="this.value=this.value.replace(/[^0-9-]+/,'');">
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>&nbsp;&nbsp;
        <input id="btnClearSearch" class="btn btn-primary" type="button" value="清除条件"/>&nbsp;&nbsp;
        <a href="javascript:void(0);" id="btnAdvanceSearch" class="btn btn-info accordion-toggle collapsed">高级查询 <span class="arrow"></span> </a></a>
    </div>
    <div style="margin-top:0px; display:none;" id="div_search_customer">
        <div>
            <label class="label-search">客户：</label>
           <%-- <sys:treeselect id="customer" name="customer.id" value="${order.customer.id}" labelName="customer.name"
                            labelValue="${order.customer.name}" cssStyle="width:211px;"
                            title="客户" url="/md/customer/treeData?kefu=${currentuser.id}"
                            cssClass="input-small" allowClear="true"/>--%>
            <form:select path="customer.id" style="width:250px;">
                <form:option value="0" label="所有"/>
                <form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id"
                              htmlEscape="false"/>
            </form:select>
            <label>区域：</label>
            <%--
            <sys:treeselect id="area" name="area.id" value="${order.area.id}" levelValue="${order.areaLevel}" nodeLevel="true"
                            labelName="area.name" labelValue="${order.area.name }" title="区域"
                            url="/sys/area/treeData?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1" nameLevel="3"/>
                            --%>
            <sys:treeselectareanew id="area" name="area.id" value="${order.area.id}" levelValue="${order.areaLevel}"
                                   labelName="area.name" labelValue="${order.area.name}" title="区域" clearIdValue="0"
                                   url="/sys/area/treeDataNew?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1" nameLevel="3"/>
            <label>服务网点：</label>
            <sd:servicePointSelect id="servicePoint" name="servicePoint.id" value="${order.servicePoint.id}" labelName="servicePoint.name" labelValue="${order.servicePoint.name}"
                                 width="1200" height="780" title="选择服务网点" areaId=""
                                 showArea="false" allowClear="true" callbackmethod="" />
        </div>
        <div style="margin-top:5px;">
            <label class="label-search">订单来源：</label>
            <form:select path="dataSource" class="input-small" style="width:125px;">
                <form:option value="0" label="所有"/>
                <form:options items="${fns:getDictListFromMS('order_data_source')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
            </form:select>
            <label>产品：</label>
            <form:select path="productId" cssClass="input-small" cssStyle="width:263px;">
                <form:option value="" label="所有"/>
                <form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
            </form:select>
            <label>服务项目：</label>
            <form:select path="serviceTypeId" class="input-small" style="width:125px;">
                <form:option value="" label="所有"/>
                <form:options items="${fns:getServiceTypes()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
            </form:select>
            <label>下单日期：</label>
            <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:95px;"
                   maxlength="20" class="input-small Wdate" value="${fns:formatDate(order.beginDate,'yyyy-MM-dd')}"
                   onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
            <label>~</label>&nbsp;&nbsp;&nbsp;
            <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:95px" maxlength="20"
                   class="input-small Wdate" value="${fns:formatDate(order.endDate,'yyyy-MM-dd')}"
                   onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
            </div>&nbsp;
        </div>

</form:form>
<sys:message content="${message}"/>
<c:set var="rowNumber" value="0"/>
<c:set var="processTimeout" value="${fns:getDictSingleValueFromMS('order_process_timeout', '48')}"/><%-- 切换为微服务 --%>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="40">序号</th>
        <th width="145">单号</th>
        <th width="40">状态</th>
        <th width="60">来源</th>
        <th width="65">品牌</th>
        <th width="65">产品</th>
        <th width="65">服务类型</th>
        <th width="45">数量</th>
        <th width="65">安维</th>
        <th width="65">预约次数</th>
        <th width="45">客服</th>
        <th width="65">用户</th>
        <th width="90">手机</th>
        <th>安装地址</th>
        <th width="80">进度跟踪</th>
        <th width="80">问题反馈</th>
        <th width="45">照片</th>
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
                        <span title="订单处理超过已 ${ processTimeout} 小时"><span class="badge badge-important">${rowNumber}</span>
                        </span>
                    </td>
                </c:when>
                <c:otherwise>
                    <td rowspan="${rowspan}">${rowNumber}</td>
                </c:otherwise>
            </c:choose>

            <c:choose>
                <c:when test="${order.orderCondition.appAbnormalyFlag == 1}">
                        <td rowspan="${rowspan}">
                            <a href="javascript:void(0);" style="color: red;" onclick="Order.showKefuOrderDetail('${order.id}','${order.quarter}',${order.orderCondition.orderServiceType});">
                                <abbr title="手机标识异常">${order.orderNo}</abbr><c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}" >(重)</abbr></c:if>
                            </a>
                            <img src="${ctxStatic}/images/${order.orderCondition.operationAppFlag == 1?"phone":"people"}.png">
                            <%--<c:if test="${order.orderCondition.isComplained>0}">
                                <c:choose>
                                    <c:when test="${order.complainFormStatus == null}">
                                        <a class="label label-warning">投诉</a>
                                    </c:when>
                                    <c:when test="${order.complainFormStatus.value == '0'}">
                                        <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-important">投诉</a>
                                    </c:when>
                                    <c:when test="${order.complainFormStatus.value == '1'}">
                                        <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-success">投诉</a>
                                    </c:when>
                                    <c:when test="${order.complainFormStatus.value == '2'}">
                                        <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label">投诉</a>
                                    </c:when>
                                    <c:otherwise>
                                        <a class="label label-warning">投诉</a>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>--%>
                            <c:if test="${order.orderStatus.complainFlag>0}">
                                <c:choose>
                                    <c:when test="${order.complainFormStatus == null}">
                                        <a class="label label-warning">投诉</a>
                                    </c:when>
                                    <c:when test="${order.complainFormStatus.value == '0'}">
                                        <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-important">投诉</a>
                                    </c:when>
                                    <c:when test="${order.complainFormStatus.value == '1'}">
                                        <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-success">投诉</a>
                                    </c:when>
                                    <c:when test="${order.complainFormStatus.value == '2'}">
                                        <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label">投诉</a>
                                    </c:when>
                                    <c:otherwise>
                                        <a class="label label-warning">投诉</a>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </td>
                </c:when>
                <c:when test="${order.canBackApprove()}">
                    <td rowspan="${rowspan}">
                        <a href="javascript:void(0);" style="color: red;" onclick="Order.showKefuOrderDetail('${order.id}','${order.quarter}',${order.orderCondition.orderServiceType});">
                            <abbr title="订单派单2小时未预约">${order.orderNo}</abbr><c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}" >(重)</abbr></c:if>
                        </a>
                        <img src="${ctxStatic}/images/${order.orderCondition.operationAppFlag == 1?"phone":"people"}.png">
                        <%--<c:if test="${order.orderCondition.isComplained>0}">
                            <c:choose>
                                <c:when test="${order.complainFormStatus == null}">
                                    <a class="label label-warning">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '0'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-important">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '1'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-success">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '2'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label">投诉</a>
                                </c:when>
                                <c:otherwise>
                                    <a class="label label-warning">投诉</a>
                                </c:otherwise>
                            </c:choose>
                        </c:if>--%>
                        <c:if test="${order.orderStatus.complainFlag>0}">
                            <c:choose>
                                <c:when test="${order.complainFormStatus == null}">
                                    <a class="label label-warning">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '0'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-important">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '1'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-success">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '2'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label">投诉</a>
                                </c:when>
                                <c:otherwise>
                                    <a class="label label-warning">投诉</a>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </td>
                </c:when>
                <c:when test="${order.orderCondition.replyFlagCustomer == 1}">
                    <td rowspan="${rowspan}">
                        <a href="javascript:void(0);" style="color: red;" onclick="Order.showKefuOrderDetail('${order.id}','${order.quarter}',${order.orderCondition.orderServiceType});">
                            <abbr title="问题反馈或回复未处理">${order.orderNo}</abbr><c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}" >(重)</abbr></c:if>
                        </a>
                        <img src="${ctxStatic}/images/${order.orderCondition.operationAppFlag == 1?"phone":"people"}.png">
                        <%--<c:if test="${order.orderCondition.isComplained>0}">
                            <c:choose>
                                <c:when test="${order.complainFormStatus == null}">
                                    <a class="label label-warning">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '0'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-important">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '1'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-success">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '2'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label">投诉</a>
                                </c:when>
                                <c:otherwise>
                                    <a class="label label-warning">投诉</a>
                                </c:otherwise>
                            </c:choose>
                        </c:if>--%>
                        <c:if test="${order.orderStatus.complainFlag>0}">
                            <c:choose>
                                <c:when test="${order.complainFormStatus == null}">
                                    <a class="label label-warning">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '0'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-important">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '1'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-success">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '2'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label">投诉</a>
                                </c:when>
                                <c:otherwise>
                                    <a class="label label-warning">投诉</a>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </td>
                </c:when>
                <c:when test="${not empty order.repeateNo}">
                    <td rowspan="${rowspan}">
                        <a href="javascript:void(0);" onclick="Order.showKefuOrderDetail('${order.id}','${order.quarter}',${order.orderCondition.orderServiceType});">
                            <abbr title="疑似重复订单，相关单号:${order.repeateNo}" >${order.orderNo}(重)</abbr>
                        </a>
                        <img src="${ctxStatic}/images/${order.orderCondition.operationAppFlag == 1?"phone":"people"}.png">
                        <%--<c:if test="${order.orderCondition.isComplained>0}">
                            <c:choose>
                                <c:when test="${order.complainFormStatus == null}">
                                    <a class="label label-warning">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '0'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-important">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '1'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-success">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '2'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label">投诉</a>
                                </c:when>
                                <c:otherwise>
                                    <a class="label label-warning">投诉</a>
                                </c:otherwise>
                            </c:choose>
                        </c:if>--%>
                        <c:if test="${order.orderStatus.complainFlag>0}">
                            <c:choose>
                                <c:when test="${order.complainFormStatus == null}">
                                    <a class="label label-warning">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '0'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-important">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '1'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-success">投诉</a>
                                </c:when>
                                <c:when test="${order.complainFormStatus.value == '2'}">
                                    <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label">投诉</a>
                                </c:when>
                                <c:otherwise>
                                    <a class="label label-warning">投诉</a>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </td>
                </c:when>
                <c:otherwise>
                            <td rowspan="${rowspan}">
                                <a href="javascript:void(0);" onclick="Order.showKefuOrderDetail('${order.id}','${order.quarter}',${order.orderCondition.orderServiceType});">
                                    <abbr title="查看订单详情">${order.orderNo}</abbr><c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}" >(重)</abbr></c:if>
                                </a>
                                <img src="${ctxStatic}/images/${order.orderCondition.operationAppFlag == 1?"phone":"people"}.png">
                                <%--<c:if test="${order.orderCondition.isComplained>0}">
                                    <c:choose>
                                        <c:when test="${order.complainFormStatus == null}">
                                            <a class="label label-warning">投诉</a>
                                        </c:when>
                                        <c:when test="${order.complainFormStatus.value == '0'}">
                                            <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-important">投诉</a>
                                        </c:when>
                                        <c:when test="${order.complainFormStatus.value == '1'}">
                                            <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-success">投诉</a>
                                        </c:when>
                                        <c:when test="${order.complainFormStatus.value == '2'}">
                                            <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label">投诉</a>
                                        </c:when>
                                        <c:otherwise>
                                            <a class="label label-warning">投诉</a>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>--%>
                                <c:if test="${order.orderStatus.complainFlag>0}">
                                    <c:choose>
                                        <c:when test="${order.complainFormStatus == null}">
                                            <a class="label label-warning">投诉</a>
                                        </c:when>
                                        <c:when test="${order.complainFormStatus.value == '0'}">
                                            <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-important">投诉</a>
                                        </c:when>
                                        <c:when test="${order.complainFormStatus.value == '1'}">
                                            <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-success">投诉</a>
                                        </c:when>
                                        <c:when test="${order.complainFormStatus.value == '2'}">
                                            <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label">投诉</a>
                                        </c:when>
                                        <c:otherwise>
                                            <a class="label label-warning">投诉</a>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                            </td>
                </c:otherwise>
            </c:choose>
            <td rowspan="${rowspan}">
                <span class="label status_${order.orderCondition.status.value}">${order.orderCondition.status.label}</span>
                <c:if test="${order.orderCondition.subStatus == 70 || order.orderCondition.subStatus == 75}">
                    <i class="icon-thumbs-up" title="等待客评"></i>
                </c:if>
            </td>
            <td rowspan="${rowspan}">${order.dataSource.label}</td>
            <c:set var="item" value="${order.items[0]}"/>
            <td>${item.brand }</td>
            <td>
                <a href="javascript:" data-toggle="tooltip" data-tooltip="${item.productSpec}">${item.product.name } </a>
            </td>
            <td>${item.serviceType.name }</td>
            <td>${item.qty}</td>
            <td rowspan="${rowspan}">
                <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${order.orderCondition.servicePoint==null?'':order.orderCondition.servicePoint.servicePointNo}">${order.orderCondition.engineer==null?'':order.orderCondition.engineer.name}</a>
            </td>
            <td rowspan="${rowspan}">${order.orderCondition.reservationTimes}</td>
            <td rowspan="${rowspan}">${order.orderCondition.kefu.name}</td>
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
                        <a href="javascript:void(0);" data-toggle="tooltip"
                           data-tooltip="${fns:formatDate(order.orderCondition.trackingDate,'yyyy-MM-dd')} - ${order.orderCondition.trackingMessage}">${fns:abbr(order.orderCondition.trackingMessage,20)}</a>
                    </c:if>
                </c:if>
            </td>
            <td rowspan="${rowspan}">
                <c:choose>
                    <c:when test="${order.orderCondition.feedbackFlag eq 1}">
                        <c:if test="${order.orderCondition.replyFlag eq 2}">
                            <img id="complain_${order.orderCondition.feedbackId}" style="width:24px;height:24px;" src="${ctxStatic}/images/complain.gif"/>
                        </c:if>
                        <a href="javascript:void(0);" onclick="Order.replylist('${order.orderCondition.feedbackId}','${order.quarter}','${order.orderNo}');" title="${order.orderCondition.feedbackTitle}" style="display: block;">
                                ${fns:abbr(order.orderCondition.feedbackTitle,20)} </a>
                    </c:when>
                    <c:otherwise>
                    </c:otherwise>
                </c:choose>
            </td>
            <%--<td rowspan="${rowspan}">
                <c:if test="${order.orderCondition.finishPhotoQty>0 }">
                    <i class="icon-camera" title="已上传完成照片,点击查看" style="cursor: pointer;" onclick="Order.photolist('${order.id}','${order.quarter}');" ></i>
                </c:if>
            </td>--%>
            <td rowspan="${rowspan}">
                <c:if test="${order.orderCondition.finishPhotoQty>0 }">
                    <i class="icon-camera" title="已上传完成照片,点击查看" style="cursor: pointer;" onclick="Order.photolistNew('${order.id}','${order.quarter}',${fns:isNewOrder(order.orderNo)});" ></i>
                </c:if>
            </td>
        </tr>
        <c:forEach items="${order.items}" var="item" varStatus="i" begin="1">
            <tr class="item">
                <td>${item.brand }</td>
                <td>
                    <a href="javascript:" data-toggle="tooltip" data-tooltip="${item.productSpec}">${item.product.name } </a>
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
        $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
        $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
    });
</script>
<style type="text/css">
    .dropdown-menu {min-width: 80px;}
    .dropdown-menu > li > a {text-align: left;padding: 3px 10px;}
    .pagination {margin: 4px 0 0 4px;}
    .label-search {width: 70px;  text-align: right;}
    form {margin: 0 0 5px;}
</style>
</body>
</html>
