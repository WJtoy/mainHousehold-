<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>订单处理-未预约列表</title>
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
        function beforePage(){
            var $btnSubmit  = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            $("#btnClearSearch").attr('disabled', 'disabled');
            layerLoading("查询中...",true);
        }
        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function(){
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
            var dateStr = DateFormat.format(new Date(), 'yyyy-MM-dd');
            $("#endDate").val(dateStr);
            $("#beginDate").val(DateFormat.format(DateFormat.addMonthStr(dateStr, -3), 'yyyy-MM-01'));
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
            $("#servicePointId").val("");
            $("#servicePointName").val("");
            $("[id='engineer.name']").val("");
            $("[id='engineer.phone']").val("");
            search();
            //page(1, 10);
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
    <li><a href="${ctx}/sd/order/kefu/processlist" title="处理中工单列表">处理中</a></li>
    <li><a href="${ctx}/sd/order/anomaly/list" title="天猫一键求助">求助</a></li>
    <li><a href="${ctx}/sd/order/serviceMonitor/list" title="天猫预警">预警</a></li>
    <li><a href="${ctx}/sd/order/kefu/pendinglist" title="需要等待的工单">停滞</a></li>
    <li class="active"><a href="javascript:void(0);" title="已预约的工单">未预约</a></li>
    <li><a href="${ctx}/sd/order/kefu/rushinglist" title="突击中的工单">突击单</a></li>
    <li><a href="${ctx}/sd/order/kefu/complainlist" title="投诉的工单">投诉</a></li>
    <li><a href="${ctx}/sd/order/kefu/finishlist" title="已完成工单列表">已完成</a></li>
    <li><a href="${ctx}/sd/order/kefu/cancellist" title="取消的工单，未接单">取消单</a></li>
    <li><a href="${ctx}/sd/order/kefu/returnlist" title="退单">退单</a></li>
    <li><a href="${ctx}/sd/order/kefu/alllist" title="所有工单">所有</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="order" method="post" class="form-inline">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="searchType" name="searchType" type="hidden" value="processing" />
    <div class="control-group">
        <label class="label-search">订单号：</label>&nbsp;
        <input type=text class="input-small" id="orderNo" name="orderNo" maxlength="20" value="${order.orderNo }"/>
        <label>用户：</label>
        <input type=text class="input-mini" id="userName" name="userName" maxlength="20" value="${order.userName}"/>
        <label>用户电话：</label>
        <input type=text class="input-small" id="phone1" name="phone1" value="${order.phone1}" maxlength="20" placeholder="用户电话 或 实际联络电话"/>
        <label>安维姓名：</label>
        <input type=text class="input-mini" id="engineer.name" name="engineer.name" maxlength="20" <c:if test="${order!=null && order.engineer != null}"> value="${order.engineer.name}"</c:if> placeholder="安维姓名"/> &nbsp;&nbsp;
        <label>安维手机：</label>
        <input type=text class="input-small" id="engineer.phone" name="engineer.phone" maxlength="20" <c:if test="${order!=null && order.engineer != null}"> value="${order.engineer.phone}"</c:if> placeholder="安维手机"/>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>&nbsp;&nbsp;
        <input id="btnClearSearch" class="btn btn-primary" type="button" value="清除条件"/>&nbsp;&nbsp;
        <a href="javascript:void(0);" id="btnAdvanceSearch" class="btn btn-info accordion-toggle collapsed">高级查询 <span class="arrow"></span> </a></a>
    </div>
    <div style="margin-top:0px; display:none;" id="div_search_customer">
        <div>
            <label class="label-search">客户：</label>
            <sys:treeselect id="customer" name="customer.id" value="${order.customer.id}" labelName="customer.name"
                            labelValue="${order.customer.name}" title="客户" url="/md/customer/treeData?kefu=${currentuser.id}"
                            cssClass="input-small" allowClear="true" cssStyle="width:211px;"/>
            <label>区域：</label>
            <sys:treeselect id="area" name="area.id" value="${order.area.id}" nodeLevel="true" levelValue="${order.areaLevel}"
                            labelName="area.name" labelValue="${order.area.name }" title="区域"
                            url="/sys/area/treeData?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1"
                            nameLevel="3"/>
            <label>服务网点：</label>
            <sd:servicePointSelect id="servicePoint" name="servicePoint.id" value="${order.servicePoint.id}" labelName="servicePoint.name" labelValue="${order.servicePoint.name}"
                                 width="1200" height="780" title="选择服务网点" areaId=""
                                 showArea="false" allowClear="true" callbackmethod="" />
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
            <label class="label-search">订单来源：</label>
            <form:select path="dataSource" class="input-small" style="width:125px;">
                <form:option value="0" label="所有"/>
                <form:options items="${fns:getDictListFromMS('order_data_source')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
            </form:select>
            <label>地址：</label>
            <input type=text class="input-small" id="serviceAddress" name="serviceAddress" value="${order.serviceAddress}" maxlength="100"/>
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
<table id="contentTable" class="table table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="40">序号</th>
        <th width="145">单号</th>
        <th width="40">状态</th>
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
                        <span title="订单处理超过已 ${ processTimeout} 小时"><span class="badge badge-important">${rowNumber}</span>
                    </span>
                    </td>
                </c:when>
                <c:otherwise>
                    <td rowspan="${rowspan}">${rowNumber}</td>
                </c:otherwise>
            </c:choose>
            <!-- 投诉 -->
            <c:set var="isComplained" value="${order.orderCondition.isComplained>0?true:false}"/>
            <c:set var="complainLabel" value="" />
            <c:set var="complainClass" value="warning" />
            <c:if test="${isComplained}">
                <c:set var="complainFormStatus" value="${order.complainFormStatus}"/>
                <c:set var="complainLabel" value="${complainFormStatus ==null?'':complainFormStatus.label}" />
                <c:choose>
                    <c:when test="${complainFormStatus == null}">
                        <c:set var="complainClass" value="warning" />
                    </c:when>
                    <c:when test="${complainFormStatus.value == '0'}">
                        <c:set var="complainClass" value="warning" />
                    </c:when>
                    <c:when test="${complainFormStatus.value == '1'}">
                        <c:set var="complainClass" value="info" />
                    </c:when>
                    <c:when test="${complainFormStatus.value == '2'}">
                        <c:set var="complainClass" value="success" />
                    </c:when>
                    <c:when test="${complainFormStatus.value == '3'}">
                        <c:set var="complainClass" value="important" />
                    </c:when>
                    <c:when test="${complainFormStatus.value == '4'}">
                        <c:set var="complainClass" value="" />
                    </c:when>
                </c:choose>
            </c:if>
            <c:choose>
                <c:when test="${order.isNotAccept(30)}">
                    <td rowspan="${rowspan}">
                        <a href="javascript:void(0);" style="color: red;" onclick="Order.showOrderDetail('${order.id}','${order.quarter}');">
                            <abbr title="30分钟无人接单">${order.orderNo}</abbr><c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}" >(重)</abbr></c:if>
                        </a>
                        <img src="${ctxStatic}/images/${order.orderCondition.operationAppFlag == 1?"phone":"people"}.png">
                        <c:if test="${isComplained}">
                            <a data-toggle="tooltip" data-tooltip="${complainLabel}" class="label label-${complainClass}">投诉</a>
                        </c:if>
                        <c:if test="${order.orderCondition.urgentLevel.id >0}">
                            <a data-toggle="tooltip" data-tooltip="${order.orderCondition.urgentLevel.remarks}" class='label label-important'>加急</a>
                        </c:if>
                    </td>
                </c:when>
                <c:when test="${order.orderCondition.appAbnormalyFlag == 1}">
                    <td rowspan="${rowspan}">
                        <a href="javascript:void(0);" style="color: red;" onclick="Order.showOrderDetail('${order.id}','${order.quarter}');">
                            <abbr title="手机标识异常" >${order.orderNo}</abbr><c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}" >(重)</abbr></c:if>
                        </a>
                        <img src="${ctxStatic}/images/${order.orderCondition.operationAppFlag == 1?"phone":"people"}.png">
                        <c:if test="${isComplained}">
                            <a data-toggle="tooltip" data-tooltip="${complainLabel}" class="label label-${complainClass}">投诉</a>
                        </c:if>
                        <c:if test="${order.orderCondition.urgentLevel.id >0}">
                            <a data-toggle="tooltip" data-tooltip="${order.orderCondition.urgentLevel.remarks}" class='label label-important'>加急</a>
                        </c:if>
                    </td>
                </c:when>
                <c:when test="${order.canBackApprove()}">
                    <td rowspan="${rowspan}">
                        <a href="javascript:void(0);" style="color: red;" onclick="Order.showOrderDetail('${order.id}','${order.quarter}');">
                            <abbr title="订单派单2小时未预约" >${order.orderNo}</abbr><c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}" >(重)</abbr></c:if>
                        </a>
                        <img src="${ctxStatic}/images/${order.orderCondition.operationAppFlag == 1?"phone":"people"}.png">
                        <c:if test="${isComplained}">
                            <a data-toggle="tooltip" data-tooltip="${complainLabel}" class="label label-${complainClass}">投诉</a>
                        </c:if>
                        <c:if test="${order.orderCondition.urgentLevel.id >0}">
                            <a data-toggle="tooltip" data-tooltip="${order.orderCondition.urgentLevel.remarks}" class='label label-important'>加急</a>
                        </c:if>
                    </td>
                </c:when>
                <c:when test="${order.orderCondition.replyFlagCustomer == 1}">
                    <td rowspan="${rowspan}">
                        <a href="javascript:void(0);" style="color: red;" onclick="Order.showOrderDetail('${order.id}','${order.quarter}');">
                            <abbr title="问题反馈或回复未处理" >${order.orderNo}</abbr><c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}" >(重)</abbr></c:if>
                        </a>
                        <img src="${ctxStatic}/images/${order.orderCondition.operationAppFlag == 1?"phone":"people"}.png">
                        <c:if test="${isComplained}">
                            <a data-toggle="tooltip" data-tooltip="${complainLabel}" class="label label-${complainClass}">投诉</a>
                        </c:if>
                        <c:if test="${order.orderCondition.urgentLevel.id >0}">
                            <a data-toggle="tooltip" data-tooltip="${order.orderCondition.urgentLevel.remarks}" class='label label-important'>加急</a>
                        </c:if>
                    </td>
                </c:when>
                <c:when test="${not empty order.repeateNo}">
                    <td rowspan="${rowspan}">
                        <a href="javascript:void(0);" onclick="Order.showOrderDetail('${order.id}','${order.quarter}');">
                            <abbr title="疑似重复订单，相关单号:${order.repeateNo}" >${order.orderNo}(重)</abbr>
                        </a>
                        <img src="${ctxStatic}/images/${order.orderCondition.operationAppFlag == 1?"phone":"people"}.png">
                        <c:if test="${isComplained}">
                            <a data-toggle="tooltip" data-tooltip="${complainLabel}" class="label label-${complainClass}">投诉</a>
                        </c:if>
                        <c:if test="${order.orderCondition.urgentLevel.id >0}">
                            <a data-toggle="tooltip" data-tooltip="${order.orderCondition.urgentLevel.remarks}" class='label label-important'>加急</a>
                        </c:if>
                    </td>
                </c:when>
                <c:otherwise>
                    <td rowspan="${rowspan}">
                        <a href="javascript:void(0);" onclick="Order.showOrderDetail('${order.id}','${order.quarter}');">
                            <abbr title="查看订单详情">${order.orderNo}</abbr><c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}" >(重)</abbr></c:if>
                        </a>
                        <img src="${ctxStatic}/images/${order.orderCondition.operationAppFlag == 1?"phone":"people"}.png">
                        <c:if test="${isComplained}">
                            <a data-toggle="tooltip" data-tooltip="${complainLabel}" class="label label-${complainClass}">投诉</a>
                        </c:if>
                        <c:if test="${order.orderCondition.urgentLevel.id >0}">
                            <a data-toggle="tooltip" data-tooltip="${order.orderCondition.urgentLevel.remarks}" class='label label-important'>加急</a>
                        </c:if>
                    </td>
                </c:otherwise>
            </c:choose>
            <td rowspan="${rowspan}">
                <span class="label status_${order.orderCondition.status.value}">${order.orderCondition.status.label}</span>
            </td>
            <td rowspan="${rowspan}">${order.dataSource.label}<br>${order.b2bShop==null?"":order.b2bShop.shopName}</td>
            <c:set var="item" value="${order.items[0]}"/>
            <td>${item.brand }</td>
            <td>
                <a href="javascript:" data-toggle="tooltip" data-tooltip="${item.productSpec}">${item.product.name } </a>
            </td>
            <td>${item.serviceType.name }</td>
            <td>${item.qty}</td>
            <td rowspan="${rowspan}">
                <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${order.orderCondition.servicePoint==null?'':order.orderCondition.servicePoint.servicePointNo} ">${order.orderCondition.engineer==null?'':order.orderCondition.engineer.name}</a>
            </td>
            <td rowspan="${rowspan}">${order.orderCondition.userName}</td>
            <td rowspan="${rowspan}">
                ${order.orderCondition.servicePhone}
                <c:if test="${!empty order.orderCondition.phone2}">
                    <br/>${order.orderCondition.phone2}
                </c:if>
            </td>
            <td rowspan="${rowspan}">
                <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${order.orderCondition.area.name}&nbsp;${order.orderCondition.serviceAddress}">${fns:abbr(order.orderCondition.area.name,40)}</a>
            </td>
            <td rowspan="${rowspan}">
                <c:if test="${order.orderCondition.trackingFlag ge 1}">
                    <c:if test="${!empty order.orderCondition.trackingMessage }">
                        <a href="javascript:void(0);"
                           data-toggle="tooltip" data-tooltip="${fns:formatDate(order.orderCondition.trackingDate,'yyyy-MM-dd')} - ${order.orderCondition.trackingMessage}">${fns:abbr(order.orderCondition.trackingMessage,20)}</a>
                    </c:if>
                </c:if>
            </td>
            <td rowspan="${rowspan}">
                <c:choose>
                    <c:when test="${order.orderCondition.feedbackFlag eq 1}">
                        <c:if test="${order.orderCondition.replyFlag eq 2}">
                            <img id="complain_${order.orderCondition.feedbackId}" style="width:24px;height:24px;" src="${ctxStatic}/images/complain.gif"/>
                        </c:if>
                        <a href="javascript:void(0);" onclick="Order.replylist('${order.orderCondition.feedbackId}','${order.quarter}','${order.orderNo}');" data-toggle="tooltip" data-tooltip="${order.orderCondition.feedbackTitle}" style="display: block;">
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
                        <c:set var="pendingTypeStr" value="${pendingTypeStr.concat(fns:formatDate(order.orderCondition.appointmentDate,'yyyy-MM-dd HH:mm'))}"/>
                    </c:if>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${pendingTypeStr}">
                        <span class="${!isGreaterNow?'badge badge-important':'' }">${fns:abbr(order.orderCondition.pendingType.label,20)}</span>
                    </a>
                    <br/>
                </c:if>
            </td>
            <td rowspan="${rowspan}">
                <c:if test="${order.orderCondition.partsFlag == 1}">
                    <i class="icon-cogs" title="已申请配件,点击查看" style="cursor: pointer;" onclick="Order.attachlist('${order.id}','${order.orderNo}','${order.quarter}');"></i>
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
<script type="text/javascript">
    $(document).ready(function()
    {
        <c:if test="${order != null}">
            <c:if test="${currentuser.userType <3}">
            $("#customerIdName").val('${order.customer.name}');
            </c:if>
        </c:if>
    });
</script>
</body>
</html>
