<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>历史派单订单详细信息(客服)</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8"/>
    <meta name="generator" content="1.0"/>
    <%@include file="/WEB-INF/views/include/dialog.jsp" %>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/modules/sd/kefuOrderList/service/tpl/historyOrderDetailInfo.html" %>
    <style type="text/css">
        .table thead th,.table tbody td{text-align:center;vertical-align:middle}.tdlable{width:90px;text-align:right}.tdbody{width:300px}.table th,.table td{padding:4px}.table thead th{text-align:center;vertical-align:middle}.table .tdcenter{text-align:center;vertical-align:middle}.alert{padding:4px 5px 4px 4px;margin-right:5px}#toolbar{height:40px;line-height:40px}.form-horizontal .control-label{width:90px}.form-horizontal .controls{margin-left:120px}i[class^="icon-"]{font-size:18px}.red{color:red}
    </style>
    <script type="text/javascript">
        <c:set var="tabActiveName" value="${empty param.activeTab?'tabTracking':param.activeTab}" />
        Order.rootUrl = "${ctx}";
        var orderdetail_index = parent.layer.getFrameIndex(window.name);
    </script>
</head>
<body>
<!-- new -->
<form:form id="inputForm" action="#" method="post" class="form-horizontal">
    <input type="hidden" id="quarter" name="quarter" value="${order.quarter}"/>
    <sys:message content="${message}"/>
    <c:if test="${errorFlag == false}">
        <c:set var="status" value="${fns:stringToInteger(order.orderCondition.status.value)}"/>
        <c:set var="cuser" value="${fns:getUser()}"/>
        <c:set var="isCustomer" value="${!empty cuser && cuser.isCustomer()?true:false }"/>
        <input type="hidden" id="isCustomer" name="isCustomer" value="${isCustomer}"/>
        <div class="accordion-group" style="margin-top:2px;">
            <div class="accordion-heading">
                <a href="#divheader" class="accordion-toggle" data-toggle="collapse">基本信息<span class="arrow"></span></a>
            </div>
            <div id="divheader" class="accordion-body">
                <table class="table table-bordered table-hover" style="margin-bottom: 0px;">
                    <tbody>
                    <tr>
                        <td class="tdlable"><label class="control-label">订单编号:</label></td>
                        <td class="tdbody">
                            <span id="spOrderNo">${order.orderNo}</span>
                            <span class="label status_${status}">${order.orderCondition.status.label}</span>
                        </td>
                        <td class="tdlable">
                            <label class="control-label">客户名称:</label>
                        </td>
                        <td class="tdbody">${order.orderCondition.customer.name}</td>
                        <td class="tdlable"><label class="control-label">业务员:</label></td>
                        <td class="tdbody">${order.orderCondition.customer.sales.name}</td>
                    </tr>
                    <tr>
                        <td class="tdlable"><label class="control-label">联系人:</label></td>
                        <td class="tdbody">${order.orderCondition.userName}</td>
                        <td class="tdlable"><label class="control-label">联络电话:</label></td>
                        <td class="tdbody"><label id="lblservicePhone">${order.orderCondition.servicePhone}</label></td>
                        <td class="tdlable"><label class="control-label">客服:</label></td>
                        <td class="tdbody">${order.orderCondition.kefu.name}</td>
                    </tr>
                    <tr>
                        <td class="tdlable"><label class="control-label">上门地址:</label></td>
                        <td class="tdbody" colspan="3">${order.orderCondition.area.name}&nbsp;&nbsp;<label id="lblserviceAddress">${order.orderCondition.serviceAddress}</label></td>
<%--                        <td class="tdlable"><label class="control-label"></label></td>--%>
<%--                        <td class="tdbody"></td>--%>
                        <td class="tdlable"><label class="control-label">购买时间:</label></td>
                        <td><label id="lblBuyDate">${order.orderAdditionalInfo.buyDateString}</label></td>
                    </tr>
                    <tr>
                        <td class="tdlable"><label class="control-label">服务描述:</label></td>
                        <td class="tdbody" colspan="3"><label id="lbldescription">${order.description}</label></td>
                        <td class="tdlable"><label class="control-label">实际到货:</label></td>
                        <td><label id="lblArrivalDate"><fmt:formatDate value="${order.orderCondition.arrivalDate}" pattern="yyyy-MM-dd HH:mm"/></label></td>
                    </tr>
                    </tbody>
                </table>
                <!-- order items -->
                <table id="productTable" class="table table-bordered table-condensed table-hover" style="margin-bottom: 0px;pmargin-top:3px;">
                    <thead>
                    <tr>
                        <th width=30px>序号</th>
                        <th>服务类型</th>
                        <th>产品</th>
                        <th>品牌</th>
                        <th>型号/规格</th>
                        <th>数量</th>
                    </thead>
                    <tbody>
                    <c:set var="ridx" value="0"/>
                    <c:set var="totalQty" value="0"/>
                    <c:forEach items="${order.items}" var="item">
                        <tr>
                            <td>${ridx+1}</td>
                            <td>${item.serviceType.name }</td>
                            <td>${item.product.name }</td>
                            <td>${item.brand }</td>
                            <td>${item.productSpec }</td>
                            <td>${item.qty }</td>
                        </tr>
                        <c:set var="ridx" value="${ridx+1}"/>
                        <c:set var="totalQty" value="${totalQty+item.qty}"/>
                    </c:forEach>
                    <!-- 客户备注 -->
                    <tr>
                        <td colspan="2">客户说明</td>
                        <td colspan="4">${order.orderCondition.customer.remarks}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <!-- engineer -->
        <shiro:hasPermission name="sd:order:showengineerinfo">
            <div class="accordion-group" style="margin-top:2px;">
                <div class="accordion-heading">
                    <a href="#divengineer" class="accordion-toggle" data-toggle="collapse">网点师傅 <span class="arrow"></span></a>
                </div>
                <div id="divengineer" class="accordion-body">
                    <table class="table table-bordered table-hover" style="margin-bottom: 0px;">
                        <tbody>
                        <tr>
                            <td class="tdlable"><label class="control-label">网点编号:</label></td>
                            <td class="tdbody">${order.orderCondition.servicePoint.servicePointNo}</td>
                            <td class="tdlable"><label class="control-label">姓名:</label></td>
                            <td class="tdbody">${order.orderCondition.engineer.name}</td>
                            <td class="tdlable"><label class="control-label">手机:</label></td>
                            <td class="tdbody">${order.orderCondition.engineer.mobile}</td>
                        </tr>
                        <tr>
                            <td class="tdlable"><label class="control-label">电话:</label></td>
                            <td class="tdbody">${order.orderCondition.engineer.mobile}</td>
                            <td class="tdlable"><label class="control-label">结算方式:</label></td>
                            <td class="tdbody">${order.orderFee.engineerPaymentType.label}</td>
                            <td class="tdlable"><label class="control-label">备注:</label></td>
                            <td class="tdbody">${order.orderCondition.servicePoint.remarks}</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </shiro:hasPermission>
    </c:if>
</form:form>
<br/>
<!-- tabs -->
<c:if test="${errorFlag == false}">
    <c:set var="tabCount" value="0"/>

    <div class="tabbable" style="margin:0px 20px">
        <ul class="nav nav-tabs">
            <c:set var="tabCount" value="${tabCount+1}"/>
            <c:if test="${tabCount==1 && empty tabActiveName}"><c:set var="tabActiveName" value="tabTracking"/></c:if>
            <li id="litabTracking" class="${tabCount==1?'active':''}">
                <a href="#tabTracking" data-toggle="tab" id="lnktabTracking" onclick="Order.orderDetail_Tracking('${order.id}','${order.quarter}',${order.orderCondition.status.value});">跟踪进度</a>
            </li>
            <c:if test="${order.orderCondition.serviceTimes>0 || (status >= 40 && status<60)}">
                <c:set var="tabCount" value="${tabCount+1}"/>
                <c:if test="${tabCount==1 && empty tabActiveName}"><c:set var="tabActiveName" value="tabService"/></c:if>
                <li id="litabService" class="${tabCount==1?'active':''}">
                    <a href="#tabService" id="lnktabService" data-toggle="tab">上门服务</a>
                </li>
            </c:if>
        </ul>
        <!-- tab content -->
        <div class="tab-content">
            <!-- service -->
            <c:if test="${order.orderCondition.serviceTimes>0 || (status >= 40 && status<60)}">
                <c:set var="servicepoint" value="${order.orderCondition.servicePoint}" />
                <div class="tab-pane ${tabActiveName=="tabService"?"active":""}" id="tabService" title="实际上门清单">
                    <div id="divserviceTable">
                        <table id="serviceTable" class="table table-striped table-bordered table-condensed table-hover" style="margin-bottom: 0px;">
                            <thead>
                            <tr>
                                <th rowspan="2" width="30px">序号</th>
                                <th rowspan="2" width="100px">日期</th>
                                <th rowspan="2" width="60px">上门次数</th>
                                <th rowspan="2" width="60px">服务类型</th>
                                <th rowspan="2">产品</th>
                                <th rowspan="2">品牌</th>
                                <th rowspan="2">型号/规格</th>
                                <th rowspan="2">数量</th>
                                <c:choose>
                                    <c:when test="${isCustomer eq true}"><th colspan="7">应付款</th></c:when>
                                    <c:otherwise><th colspan="6">应收款</th></c:otherwise>
                                </c:choose>
                                <shiro:hasPermission name="sd:order:showpayment">
                                    <th colspan="7">应付款</th>
                                    <th rowspan="2">备注</th>
                                </shiro:hasPermission>
                            </tr>
                            <tr>
                                <th>服务费</th>
                                <th>配件费</th>
                                <th>快递费</th>
                                <th>远程费</th>
                                <th>其他</th>
                                <th>小计</th>
                                <shiro:hasPermission name="sd:order:showpayment">
                                    <th>服务费</th>
                                    <th>配件费</th>
                                    <th>快递费</th>
                                    <th>远程费</th>
                                    <th>其他</th>
                                    <th>小计</th>
                                    <th>安维</th>
                                </shiro:hasPermission>
                            </tr>
                            </thead>
                            <tbody>
                            <c:set var="totalQty" value="0"/>
                            <c:set var="ridx" value="0"/>
                            <c:set var="charge1" value="0"/>
                            <c:set var="charge2" value="0"/>
                            <c:set var="materialCharge1" value="0.00"/>
                            <c:set var="materialCharge2" value="0.00"/>
                            <c:set var="expressCharge1" value="0.00"/>
                            <c:set var="expressCharge2" value="0.00"/>
                            <c:set var="travelCharge1" value="0.00"/>
                            <c:set var="travelCharge2" value="0.00"/>
                            <c:set var="otherCharge1" value="0.00"/>
                            <c:set var="otherCharge2" value="0.00"/>
                            <c:set var="totalCharge1" value="0.00"/>
                            <c:set var="totalCharge2" value="0.00"/>
                            <c:forEach items="${order.detailList}" var="item">
                                <tr>
                                    <td class="tdcenter">${ridx+1}</td>
                                    <td class="tdcenter">${fns:formatDate(item.createDate,'yyyy-MM-dd HH:mm')}</td>
                                    <td class="tdcenter">${item.serviceTimes}</td>
                                    <td>${item.serviceType.name}</td>
                                    <td>${item.product.name}</td>
                                    <td>${item.brand}</td>
                                    <td>${item.productSpec}</td>
                                    <td class="tdcenter">${item.qty}</td>
                                    <c:set var="totalQty" value="${totalQty+item.qty}"/>
                                    <shiro:hasPermission name="sd:order:showreceive">
                                        <td class="tdcenter">${item.charge}</td>
                                    </shiro:hasPermission>
                                    <shiro:lacksPermission name="sd:order:showreceive">
                                        <td class="tdcenter">*</td>
                                    </shiro:lacksPermission>
                                    <td class="tdcenter">${item.materialCharge}</td>
                                    <td class="tdcenter">${item.expressCharge}</td>
                                    <td class="tdcenter">${item.travelCharge}</td>
                                    <td class="tdcenter">${item.otherCharge}</td>
                                    <shiro:hasPermission name="sd:order:showreceive">
                                        <td class="tdcenter"><b>${item.customerCharge}</b></td>
                                    </shiro:hasPermission>
                                    <shiro:lacksPermission name="sd:order:showreceive">
                                        <td class="tdcenter">*</td>
                                    </shiro:lacksPermission>
                                    <c:set var="charge1" value="${charge1 + item.charge}"/>
                                    <c:set var="materialCharge1" value="${materialCharge1 + item.materialCharge}"/>
                                    <c:set var="expressCharge1" value="${expressCharge1 + item.expressCharge}"/>
                                    <c:set var="travelCharge1" value="${travelCharge1 + item.travelCharge}"/>
                                    <c:set var="otherCharge1" value="${otherCharge1 + item.otherCharge}"/>
                                    <c:set var="totalCharge1" value="${totalCharge1 + item.customerCharge}"/>

                                    <shiro:hasPermission name="sd:order:showpayment">
                                        <c:set var="engineerClass" value='${servicepoint.id == item.servicePoint.id?"":"red"}' />
                                        <td class="tdcenter">${item.engineerServiceCharge}</td>
                                        <td class="tdcenter">${item.engineerMaterialCharge}</td>
                                        <td class="tdcenter">${item.engineerExpressCharge}</td>
                                        <td class="tdcenter">${item.engineerTravelCharge}
                                            <c:if test="${!empty item.travelNo}">签核单号:${item.travelNo}</c:if>
                                        </td>
                                        <td class="tdcenter">${item.engineerOtherCharge}</td>
                                        <td class="tdcenter"><b>${item.engineerChage}</b></td>
                                        <td class="tdcenter ${engineerClass}">${item.engineer.name}</td>
                                        <td class="tdcenter">${item.remarks}</td>
                                        <c:set var="charge2" value="${charge2 + item.engineerServiceCharge}"/>
                                        <c:set var="materialCharge2" value="${materialCharge2 + item.engineerMaterialCharge}"/>
                                        <c:set var="expressCharge2" value="${expressCharge2 + item.engineerExpressCharge}"/>
                                        <c:set var="travelCharge2" value="${travelCharge2 + item.engineerTravelCharge}"/>
                                        <c:set var="otherCharge2" value="${otherCharge2 + item.engineerOtherCharge}"/>
                                        <c:set var="totalCharge2" value="${totalCharge2 + item.engineerChage}"/>
                                    </shiro:hasPermission>
                                </tr>
                                <c:set var="ridx" value="${ridx+1}"/>
                            </c:forEach>
                            <tr>
                                <td style="text-align:right;" colspan="7"><span class="alert alert-success">总计</span></td>
                                <td class="tdcenter"><span class="alert alert-success"><strong>${totalQty}</strong></span></td>
                                <shiro:hasPermission name="sd:order:showreceive">
                                    <td class="tdcenter"><span class="alert alert-success">${fns:formatNum(charge1)}</span></td>
                                </shiro:hasPermission>
                                <shiro:lacksPermission name="sd:order:showreceive">
                                    <td class="tdcenter"><span class="alert alert-success">*</span></td>
                                </shiro:lacksPermission>
                                <td class="tdcenter"><span class="alert alert-success">${fns:formatNum(materialCharge1)}</span></td>
                                <td class="tdcenter"><span class="alert alert-success">${fns:formatNum(expressCharge1)}</span></td>
                                <td class="tdcenter"><span class="alert alert-success">${fns:formatNum(travelCharge1)}</span></td>
                                <td class="tdcenter"><span class="alert alert-success">${fns:formatNum(otherCharge1)}</span></td>
                                <shiro:hasPermission name="sd:order:showreceive">
                                    <td class="tdcenter"><span class="alert alert-info"><strong>${fns:formatNum(totalCharge1)}</strong></span></td>
                                </shiro:hasPermission>
                                <shiro:lacksPermission name="sd:order:showreceive">
                                    <td class="tdcenter"><span class="alert alert-success">*</span></td>
                                </shiro:lacksPermission>

                                <shiro:hasPermission name="sd:order:showpayment">
                                    <td class="tdcenter"><span class="alert alert-success">${fns:formatNum(charge2)}</span></td>
                                    <td class="tdcenter"><span class="alert alert-success">${fns:formatNum(materialCharge2)}</span></td>
                                    <td class="tdcenter"><span class="alert alert-success">${fns:formatNum(expressCharge2)}</span></td>
                                    <td class="tdcenter"><span class="alert alert-success">${fns:formatNum(travelCharge2)}</span></td>
                                    <td class="tdcenter"><span class="alert alert-success">${fns:formatNum(otherCharge2)}</span></td>
                                    <td class="tdcenter"><span class="alert alert-info"><strong>${fns:formatNum(totalCharge2)}</strong></span></td>
                                    <td class="tdcenter"></td>
                                    <td></td>
                                </shiro:hasPermission>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:if>
            <div class="tab-pane ${tabActiveName=="tabTracking"?"active":""}" id="tabTracking"></div>
        </div>
    </div>
</c:if>
<c:if test="${errorFlag == false}">
    <script type="text/javascript">
        var tabName = '${tabActiveName}';
        function loadTabContent() {
            $("#lnk" + tabName).trigger("click");
        }

        $(document).ready(function () {
            if (!Utils.isEmpty(tabName)) {
                setTimeout('loadTabContent()', 100);
            }
        });
    </script>
</c:if>
</body>
</html>