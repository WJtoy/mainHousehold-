<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>我的订单-所有(事业部主管)</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <%@ include file="/WEB-INF/views/include/WdateLimitPicker.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <script src="${ctxStatic}/area/AreaFourLevel.js" type="text/javascript"></script>
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
            $("#messageType").val("");
            $("#orderNo").val("");
            $("#customerId").val("");
            $("#customerName").val("");
            var startDate = moment().subtract(1,'M').format("YYYY-MM-DD");
            var endDate = moment().format("YYYY-MM-DD");
            $("#beginDate").val(startDate);
            $("#endDate").val(endDate);
            $("[id='status.value']").val("");
            $("[id='s2id_status.value']").find("span.select2-chosen").html('所有');
            $("#creator").val("");
            $("#phone1").val("");
            $("#customer\\.id").val(null);
            $("#s2id_customer\\.id").find("span.select2-chosen").html('所有');
            $("#userName").val("");
            $("#areaId").val("");
            $("#areaName").val("");
            $("#remarks").val("");
            $("#replyFlagCustomer").attr('checked', false);
            // $("#address").val("");
            $("#partsFlag").attr('checked', false);
            $("#appAbnormalyFlag").attr('checked', false);
            $("#productId").val("0");
            $("#s2id_productId").find("span.select2-chosen").html('所有');
            $("#serviceTypeId").val("0");
            $("#s2id_serviceTypeId").find("span.select2-chosen").html('所有');
            $("[id='urgentLevel.id']").val("0");
            $("[id='s2id_urgentLevel.id']").find("span.select2-chosen").html('');
            $("#dataSource").val("0");
            $("#s2id_dataSource").find("span.select2-chosen").html('所有');
            $("#shopId").val("");
            $("#s2id_shopId").find("span.select2-chosen").html('所有');
            $("#customerOrderNo").val("");
            search();
        });

        function getShopList(customerId, dataSource) {
            var ctl_shopId = $("#shopId");
            var sid = "shopId";
            ctl_shopId.empty();
            if (dataSource == '0' || dataSource == '1' || customerId == '0') {
                var s2text = "所有";
                var option = document.createElement("option");
                option.text = s2text;
                option.value = "";
                ctl_shopId[0].options.add(option);
                $("#s2id_" + sid).find("span.select2-chosen").html('所有');
                return false;
            }
            $("#s2id_" + sid).find("span.select2-chosen").html('');
            $.ajax({
                url: "${ctx}/b2bcenter/md/customer/getShopList?dataSource=" + dataSource + "&customerId=" + customerId,
                type: "GET",
                success: function (data) {
                    if (ajaxLogout(data)) {
                        return false;
                    }
                    if (data.success == false) {
                        layerError(data.message, "读取店铺列表错误");
                        return;
                    }
                    var s2text = "所有";
                    var option = document.createElement("option");
                    option.text = s2text;
                    option.value = "";
                    ctl_shopId[0].options.add(option);
                    $.each(data.data, function (i, item) {
                        option = document.createElement("option");
                        option.text = item.shopName;
                        option.value = item.shopId;
                        ctl_shopId[0].options.add(option);
                    });
                    $("#" + sid + " option:nth-child(1)").attr("selected", "selected");
                    $("#s2id_" + sid).find("span.select2-chosen").html(s2text);
                    return false;
                },
                error: function (e) {
                    ajaxLogout(e.responseText, null, "读取店铺列表错误，请重试!");
                    e.preventDefault();
                }
            });
            return false;
        }

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
            //customer
            $("[id='customer.id']").change(function (event) {
                var customerId = $(this).val();
                var dataSource = $("#dataSource").val() || '0';
                getShopList(customerId, dataSource);
                event.preventDefault();
                return false;
            });
            //datasource
            $("#dataSource").change(function () {
                var dataSource = $(this).val() || '0';
                var customerId = $("[id='customer.id']").val() || '0';
                getShopList(customerId, dataSource);
                return false;
            });
        });
    </script>

</head>

<body>
<ul id="navtabs" class="nav nav-tabs">
    <li><a href="${ctx}/sd/order/deptleader/materialList" data-toggle="tooltipnorth" data-tooltip="要发配件的订单">待发配件</a></li>
    <li><a href="${ctx}/sd/order/deptleader/list" data-toggle="tooltipnorth" data-tooltip="&nbsp;&nbsp;处理中订单列表">处理中</a>
    </li>
    <li><a href="${ctx}/sd/order/deptleader/finishlist" data-toggle="tooltipnorth" data-tooltip="已完成订单列表">已完成</a></li>
    <li><a href="${ctx}/sd/order/deptleader/cancellist" data-toggle="tooltipnorth" data-tooltip="取消单列表">取消单</a></li>
    <li><a href="${ctx}/sd/order/deptleader/returnlist" data-toggle="tooltipnorth" data-tooltip="退单列表">退单</a></li>
    <li class="active"><a href="javascript:;" data-toggle="tooltipnorth" data-tooltip="所有订单列表">所有</a></li>
    <li><a href="${ctx}/sd/order/deptleader/complainlist" data-toggle="tooltipnorth" data-tooltip="投诉列表">投诉</a></li>
    <li><a href="${ctx}/sd/order/deptleader/reminderlist" data-toggle="tooltipnorth" data-tooltip="催单列表">催单</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="order" action="${ctx}/sd/order/deptleader/alllist" method="post"
           class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="messageType" name="messageType" type="hidden" value="${order.messageType}"/>
    <div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
        注:当用工单号和手机号码进行搜索查询时，不受当前时间、地区等其他条件的限制。
    </div>
    <div>
        <label>工单号：</label><input type=text class="input-small" id="orderNo" name="orderNo" value="${order.orderNo }"
                                 maxlength="20"/>
        <label>客户:</label>
        <form:select path="customer.id" style="width:300px;">
            <form:option value="0" label="所有"/>
            <form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id"
                          htmlEscape="false"/>
        </form:select>
        <label>区 域：</label>
        <%--
        <sys:treeSelectAnyArea id="area" name="area.id" value="${order.area.id}" levelValue="${order.areaLevel}"
                               labelName="area.name" labelValue="${order.area.name}" title="区域" cssStyle="width:245px;"
                               parentValue="${order.area.parent.id}" clearIdValue="0"
                               url="/sys/area/treeData" allowClear="true" nodesLevel="-1" nameLevel="4"/>
                               --%>
        <sys:areaSelectFourLevel id="area" name="area.id"  value="${order.area.id}" levelValue="${order.areaLevel}"
                                 labelValue="${order.area.fullName}" labelName="area.fullName" title=""
                                 mustSelectCounty="true" cssClass="required" showMaxLevel="4"></sys:areaSelectFourLevel>
        <label>产品：</label>
        <form:select path="productId" cssClass="input-small" cssStyle="width:125px;">
            <form:option value="" label="所有"/>
            <form:options items="${productList}" itemLabel="name" itemValue="id" htmlEscape="false"/>
        </form:select>
        <label>加急：</label>
        <form:select path="urgentLevel.id" class="input-small" style="width:125px;">
            <form:option value="0" label=""/>
            <c:if test="${!empty order.urgentLevels}">
                <form:option value="1" label="所有"/>
                <form:options items="${order.urgentLevels}" itemLabel="remarks" itemValue="id" htmlEscape="false"/>
            </c:if>
        </form:select>
    </div>

    <div style="margin-top:5px">
        <label>用户：</label><input type="text" class="input-small"
                                 id="userName" name="userName" value="${order.userName }" maxlength="20"/>
        <label>电话：</label>
        <input type="text" style="width: 120px;" id="phone1" name="phone1" value="${order.phone1}" maxlength="20"
               style="width: 90px;"/>
        <label>状态：</label>
        <c:set var="statusList" value="${fns:getDictListFromMS('order_status')}"/><%--切换为微服务--%>
        <form:select path="status.value" cssClass="input-small" cssStyle="width:125px;">
            <form:option value="" label="所有"/>
            <form:options items="${statusList}" itemLabel="label" itemValue="value" htmlEscape="false"/>
        </form:select>
        <label>服务项目：</label>
        <form:select path="serviceTypeId" class="input-small" style="width:125px;">
            <form:option value="" label="所有"/>
            <form:options items="${fns:getServiceTypes()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
        </form:select>
        <label>工单来源：</label>
        <form:select path="dataSource" class="input-small" style="width:125px;">
            <form:option value="0" label="所有"/>
            <form:options items="${fns:getDictListFromMS('order_data_source')}" itemLabel="label" itemValue="value"
                          htmlEscape="false"/>
        </form:select>
        <label>店铺：</label>
        <form:select path="shopId" class="input-small" style="width:125px;">
            <form:option value="" label="所有"/>
            <c:if test="${order != null && order.shopList != null && order.shopList.size() >0}">
                <form:options items="${order.shopList}" itemLabel="shopName" itemValue="shopId" htmlEscape="false"/>
            </c:if>
        </form:select>
    </div>
    </div>
    <div style="margin-top:5px">
        <label>客户单号:</label>
        <input type=text class="input-small" id="customerOrderNo" name="customerOrderNo" value="${order.customerOrderNo}" maxlength="30"/>&nbsp;&nbsp;
        <label>下单人：</label>
        <input type=text class="input-mini" id="creator" name="creator" value="${order.creator }" maxlength="20"
               style="width: 102px;"/>
        <label>下单日期：</label>
        <input
                id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:95px;margin-left:4px"
                maxlength="20" class="input-small Wdate" value="${fns:formatDate(order.beginDate,'yyyy-MM-dd')}"/>
        <label>~</label>&nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:95px" maxlength="20"
               class="input-small Wdate" value="${fns:formatDate(order.endDate,'yyyy-MM-dd')}" />&nbsp;&nbsp;
        <label for="replyFlagKefu">未回复反馈：</label>
        <input id="replyFlagKefu" name="replyFlagKefu"
               <c:if test="${order.replyFlagKefu == 1 ||  order.replyFlag == 1}">checked="checked"</c:if>
               type="checkbox" value="1" class="input-small"/>
        <label title="有配件申请" for="partsFlag">配件：</label>
        <input id="partsFlag" name="partsFlag"
               <c:if test="${order.partsFlag == 1}">checked="checked"</c:if> type="checkbox" value="1"
               class="input-small"/>
        <label title="手机标识异常" for="appAbnormalyFlag">异常：</label>
        <input id="appAbnormalyFlag" name="appAbnormalyFlag"
               <c:if test="${order.appAbnormalyFlag==1}">checked="checked"</c:if> type="checkbox" value="1"
               class="input-small"/>
        &nbsp;&nbsp;<label>反馈内容：</label>&nbsp;
        <input type=text class="input-small" style="width: 100px;" id="remarks" name="remarks" value="${remarks }"
               maxlength="30"/>&nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>&nbsp;&nbsp;
        <input id="btnClearSearch" class="btn btn-primary" type="button" value="清除条件"/>
    </div>
</form:form>

<sys:message content="${message}"/>
<c:set var="rowNumber" value="0"/>
<div class="alert alert-info">
    <strong>当前余额：${order.balance}</strong>
</div>
<div style="height: 100%;min-height:450px; margin: 0;overflow-X:scroll;padding: 0;">
    <table id="contentTable" class="table table-bordered table-condensed table-striped"
           style="table-layout:fixed;" cellspacing="0" width="100%">
        <thead>
        <tr>
            <th width="30">序号</th>
            <th width="120">工单号</th>
            <th width="150">下单人</th>
            <th width="70">状态</th>
            <th width="100">来源</th>
            <th width="100">用户</th>
            <th width="150">安维详细地址</th>
            <th width="100">服务描述</th>
            <th width="60">派单价</th>
            <th width="60">参考价</th>
            <th width="60">配件</th>
            <shiro:hasPermission name="sd:order:edit">
                <th width="70">操作</th>
            </shiro:hasPermission>
            <th width="80">反馈</th>
            <shiro:hasPermission name="sd:complain:create">
                <th width="70">投诉</th>
            </shiro:hasPermission>
            <shiro:hasPermission name="sd:reminder:create">
                <th width="70">催单</th>
            </shiro:hasPermission>
            <th width="200">服务明细</th>
            <th width="60">完成照片</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${page.list}" var="o">
            <c:set var="rowNumber" value="${rowNumber+1}"/>
            <c:set var="statusValue" value="${fns:stringToInteger(o.orderCondition.status.value)}"/>
            <tr>
                <td>${rowNumber}</td>
                <td>
                    <c:if test="${o.orderCondition.replyFlagKefu == 1 }">
                        <a href="javascript:void(0);" style="color: red;"
                           onclick="Order.viewOrderDetail('${o.id}','${o.quarter}');"><abbr
                                title="问题反馈/回复未处理">${o.orderNo}</abbr> </a>
                    </c:if>
                    <c:if test="${o.orderCondition.replyFlagKefu != 1}">
                        <a href="javascript:void(0);"
                           onclick="Order.viewOrderDetail('${o.id}','${o.quarter}');"><abbr
                                title="点击查看订单详情">${o.orderNo}</abbr> </a>
                    </c:if>
                    <!-- 投诉 -->
                   <%-- <c:set var="isComplained" value="${o.orderCondition.isComplained>0?true:false}"/>--%>
                    <c:set var="isComplained" value="${o.orderStatus.complainFlag>0?true:false}"/>
                    <c:set var="complainLabel" value=""/>
                    <c:set var="complainClass" value="warning"/>
                    <c:if test="${isComplained}">
                        <c:set var="complainFormStatus" value="${o.complainFormStatus}"/>
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
                    <c:if test="${isComplained}">
                        <a data-toggle="tooltip" data-tooltip="${complainLabel}"
                           class="label label-${complainClass}">投诉</a>
                    </c:if>
                    <c:if test="${o.orderCondition.urgentLevel.id >0}">
                        <a data-toggle="tooltip" data-tooltip="${o.orderCondition.urgentLevel.remarks}"
                           class='label label-important' style="background-color: ${o.orderCondition.urgentLevel.markBgcolor};">加急</a>
                    </c:if>
                    <c:if test="${o.orderStatus.reminderStatus >0}">
                        <c:set var="reminderStatus" value="${o.orderStatus.reminderStatus}"/>
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
                </td>
                <td>${o.orderCondition.createBy.name}${not empty o.parentBizOrderId?'<br>'.concat(o.parentBizOrderId):''}
                </td>
                <td><span class="label status_${o.orderCondition.status.value}">${o.orderCondition.status.label} </span>
                    <c:if test="${o.orderCondition.pendingType != null || !o.isClosed()}">
                        <br>
                        <c:if test="${o.orderCondition.pendingType.value ne '6' }">
                            <label class="">${fns:abbr(o.orderCondition.pendingType.label,20)}</label>
                        </c:if>
                    </c:if>
                </td>
                <td>${o.dataSource.label}<br>${o.b2bShop==null?"":o.b2bShop.shopName}</td>
                <td>${o.orderCondition.userName}
                    <br>${o.orderCondition.servicePhone}
                    <c:if test="${!empty o.orderCondition.phone2 }">
                        <br>${o.orderCondition.phone2}
                    </c:if>
                </td>
                <td><a href="javascript:" data-toggle="tooltip"
                       data-tooltip="${o.orderCondition.area.name}&nbsp;${o.orderCondition.serviceAddress}">${o.orderCondition.area.name}</a>
                </td>
                <td><a href="javascript:" data-toggle="tooltip"
                       data-tooltip="${o.description}">${fns:abbr(o.description,20)}</a>
                </td>
                <td>${o.orderFee.expectCharge}</td>
                <td>
                    <c:choose>
                        <c:when test="${statusValue<60 || statusValue == 80 || statusValue == 85}">${o.orderFee.orderCharge}</c:when>
                        <c:otherwise>0.0</c:otherwise>
                    </c:choose>
                    <c:if test="${(statusValue == 80 || statusValue == 85) && (o.writeOff==1 || o.writeOff==3)}"><br/><span
                            class="label status_60">退补</span></c:if>
                </td>
                <td>
                    <c:if test="${o.orderCondition.partsFlag eq 1}">
                        <a href="javascript:void(0);" class="btn btn-mini btn-primary"
                           onclick="Order.attachlist('${o.id}','${o.orderNo}','${o.quarter}');"><abbr
                                title="点击查看配件申请列表">配件</abbr> </a>
                    </c:if>
                </td>
                <shiro:hasPermission name="sd:order:edit">
                    <td>
<%--                        <c:set var="canEdit"--%>
<%--                               value="${o.orderCondition.partsFlag == 0 && (fn:containsIgnoreCase('10,20,30,40',o.orderCondition.status.value) || (o.orderCondition.status.value == '50' && o.orderCondition.serviceTimes == 0))}"/>--%>
                        <c:set var="canEdit"
                               value="${o.orderCondition.partsFlag == 0 && (fn:containsIgnoreCase('10,20,30,40',o.orderCondition.status.value)
                               || ((o.orderCondition.status.value == '50' || o.orderCondition.status.value == '55') && o.orderCondition.serviceTimes == 0))}"/>
                        <c:if test="${canEdit || o.canCanceled()}">
                            <div class="btn-group">
                                <a class="btn btn-primary dropdown-toggle"
                                   data-toggle="dropdown" href="#">操作 <span class="caret"></span>
                                </a>
                                <ul class="dropdown-menu">
                                    <c:if test="${canEdit}">
                                        <shiro:hasPermission name="sd:order:edit">
                                            <li><a href="${ctx}/sd/order/edit?id=${o.id}&actiontype=sales_all"><i class="icon-pencil"></i>修改</a>
                                            </li>
                                        </shiro:hasPermission>
                                    </c:if>
                                    <c:if test="${ o.canCanceled() }">
                                        <shiro:hasPermission name="sd:order:cancel">
                                            <li><a href="javascript:void(0);"
                                                   onclick="Order.cancelOrder('${o.id}','${o.orderNo}')"><i
                                                    class="icon-ban-circle"></i>取消</a></li>
                                        </shiro:hasPermission>
                                    </c:if>
                                </ul>
                            </div>
                        </c:if>
                    </td>
                </shiro:hasPermission>
                <td><c:choose>
                    <c:when test="${o.orderCondition.feedbackFlag eq 1 }">
                        <c:if test="${o.orderCondition.replyFlag eq 1 }">
                            <img id="complain_${o.orderCondition.feedbackId}" style="width:24px;height:24px;"
                                 src="${ctxStatic}/images/complain.gif"/>
                        </c:if>
                        <a href="javascript:void(0);" class="btn-mini"
                           data-toggle="tooltip"
                           data-tooltip="${o.orderCondition.feedbackTitle}。<br/>点击查看/回复反馈详细内容"
                           onclick="Order.replylist('${o.orderCondition.feedbackId}','${o.quarter}','${o.orderNo}','${o.id}');">
                                ${fns:abbr(o.orderCondition.feedbackTitle,13)} </a>
                    </c:when>
                    <c:otherwise>
                        <shiro:hasPermission name="sd:feedback:add">
                            <a href="javascript:void(0);" class="btn btn-mini"
                               onclick="Order.feedback('${o.id}','${o.quarter}');">反馈</a>
                        </shiro:hasPermission>
                    </c:otherwise>
                </c:choose>
                </td>
                <shiro:hasPermission name="sd:complain:create">
                    <td>
                        <c:if test="${not isComplained or (isComplained and (complainFormStatus.value eq '2' or complainFormStatus.value eq '4') ) }">
                            <a class="btn btn-mini btn-warning" href="javascript:void(0);"
                               onclick="Order.complain_form('','${o.id}','${o.quarter}');">投诉</a>
                        </c:if>
                    </td>
                </shiro:hasPermission>
                <!-- 催单 -->
                <shiro:hasPermission name="sd:reminder:create">
                    <td>
<%--                        <c:if test="${o.orderCondition.status.value <= 50}">--%>
                        <c:if test="${o.orderCondition.status.value <= 55}">
                            <c:choose>
                                <c:when test="${o.orderCondition.reminderCheckResult == 0}">
                                    <a class="label label-error" data-toggle="tooltip" data-tooltip="催单检查无结果，请重新刷新列表" href="javascript:void(0);">催单</a>
                                </c:when>
                                <c:when test="${o.orderCondition.reminderCheckResult == 1}">
                                    <c:choose>
                                        <c:when test="${o.orderStatus.reminderStatus == 0}">
                                            <a class="btn btn-mini btn-warning" href="javascript:void(0);" data-toggle="tooltip" data-tooltip="首次催单"
                                               onclick="Order.reminder('${o.id}','${o.quarter}','${o.orderNo}');">催单</a>
                                        </c:when>
                                        <c:when test="${o.orderStatus.reminderStatus == 2 or o.orderStatus.reminderStatus == 3}">
                                            <a class="btn btn-mini btn-warning" href="javascript:void(0);" data-toggle="tooltip" data-tooltip="再次催单"
                                               onclick="Order.reReminder('','${o.id}','${o.quarter}','${o.orderNo}');">催单</a>
                                        </c:when>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <a class="label label-error" data-toggle="tooltip" data-tooltip="${o.orderCondition.reminderCheckTitle}" href="javascript:void(0);">催单</a>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </td>
                </shiro:hasPermission>
                <td><c:forEach items="${o.items}" var="item"
                               varStatus="i" begin="0">
                    ${item.brand }&nbsp;&nbsp;${item.product.name }&nbsp;&nbsp;${item.serviceType.name }&nbsp;&nbsp;&nbsp;&nbsp;数量:${item.qty }
                    <br/>
                </c:forEach>
                </td>
                <td>
                    <c:if test="${o.orderCondition.finishPhotoQty > 0 }">
                        <a href="javascript:void(0);" onclick="Order.photolistNew('${o.id}','${o.quarter}',${fns:isNewOrder(o.orderNo)});"
                           class="btn btn-mini btn-primary">完成照片</a>
                    </c:if>
                </td>

            </tr>
        </c:forEach>

        </tbody>
    </table>
</div>
<div class="pagination">${page}</div>
<style type="text/css">
    .dropdown-menu {min-width: 80px;}
    .dropdown-menu > li > a {text-align: left;padding: 3px 10px;}
    .pagination {margin: 10px 0;}
</style>
<script type="text/javascript">
    $(document).ready(function () {
        oneYearDatePicker('beginDate','endDate',false);
        <c:if test="${order != null && currentuser.userType <3 }">
        $("#customerIdName").val('${order.customer.name}');
        </c:if>
        $("td,th").css(
            {
                "text-align": "center",
                "vertical-align": "middle"
            });
    });
</script>
</body>
</html>
