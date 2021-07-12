<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>订单详细信息(网点)</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8"/>
    <%@include file="/WEB-INF/views/include/dialog.jsp" %>
    <!-- clipboard -->
    <script src="${ctxStatic}/common/clipboard.min.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <script src="${ctxStatic}/sd/ServicePointOrderService.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <!-- image viewer -->
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
        }

        .tdlable {
            width: 160px;
            align: right;
        }

        .tdbody {
            width: 300px;
        }

        .table th, .table td {
            padding: 4px;
        }

        .table thead th {
            text-align: center;
            vertical-align: middle;
        }

        .table .tdcenter {
            text-align: center;
            vertical-align: middle;
        }

        .alert {
            padding: 4px 5px 4px 4px;
        }

        .gallery-thumb {
            position: relative;
            cursor: pointer;
            padding: 5px;
        }

        .gallery-thumb img {
            width: 30px;
            height: 30px;
            margin-right: 5px;
        }
    </style>

    <script type="text/javascript">
        <c:set var="tabActiveName" value="${empty param.activeTab?'tabTracking':param.activeTab}" />
        <c:set var="cuser" value="${fns:getUser()}" />
        ServicePointOrderService.rootUrl = "${ctx}";
        var orderdetail_index = parent.layer.getFrameIndex(window.name);
        $(document).ready(function () {
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipnorth]').darkTooltip({gravity: 'north'});
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity: 'east'});
            //图片预览
            $("div.viewer-container").remove();
            $.each($("#itemTable").find("div.gallery-thumb"), function (i, thumb) {
                $(thumb).viewer('destroy').viewer({url: "data-original"});
            });
            //clip board
            //engineer
            var html = $('#txengineertmsg').val();
            var engineerMsg = html.replace(/~n/g,'\n');
            $("#btn_engineer_copy").attr('data-clipboard-text',engineerMsg);
            var clip_engineer = new ClipboardJS('#btn_engineer_copy');
            clip_engineer.on('success', function(e) {
                layerMsg("安维短信复制成功");
            });
            clip_engineer.on('error', function(e) {
                layerError("安维短信复制失败： <br/>" + JSON.stringify(e.message));
            });

            //user
            var userMsg = $('#txtusermsg').val();
            $("#btn_user_copy").attr('data-clipboard-text',userMsg);
            var clip_user = new ClipboardJS('#btn_user_copy');
            clip_user.on('success', function(e) {
                layerMsg("用户短信复制成功");
            });
            clip_user.on('error', function(e) {
                layerError("用户短信复制失败： <br/>" + JSON.stringify(e.message));
            });

            //order
            var orderMsg = $('#txtordermsg').val();
            //换行处理
            orderMsg = orderMsg.replace(/~n/g,'\n');
            $("#btn_order_copy").attr('data-clipboard-text',orderMsg);
            var clip_order = new ClipboardJS('#btn_order_copy');
            clip_order.on('success', function(e) {
                layerMsg("订单信息复制成功");
            });
            clip_order.on('error', function(e) {
                layerError("订单信息复制失败： <br/>" + JSON.stringify(e.message));
            });

            if (!Utils.isEmpty(tabName)) {
                setTimeout('loadTabContent()', 100);
            }
        });

    </script>
</head>
<body>
<!-- new -->
<form:form id="inputForm" action="#" method="post" class="form-horizontal">
    <sys:message content="${message}"/>
    <c:if test="${errorFlag == false}">
        <c:set var="cancopy" value="${order.canService()}"/>
        <!-- order head -->
        <div class="accordion-group" style="margin-top:2px;">
            <div class="accordion-heading">
                <c:set var="msg" value=""/>
                <c:forEach items="${order.items}" var="item">
                    <c:set var="msg"
                           value="${msg} ${item.brand} ${item.product.name}${item.qty }${item.serviceType.name}"/>
                </c:forEach>
                <c:set var="ordermsg" value="单号:${order.orderNo}  ${order.orderCondition.customer.name}"/>
                <c:if test="${order.dataSource.value != '0'}">
                    <c:set var="ordermsg"
                           value="${ordermsg}(${order.dataSource.label}${order.b2bShop==null || order.b2bShop.shopId == ''?'':'-'.concat(order.b2bShop.shopName)}${not empty order.parentBizOrderId?'-'.concat(order.parentBizOrderId):''})"/>
                </c:if>
                <c:set var="ordermsg"
                       value="${ordermsg} ~n联系人:${order.orderCondition.userName}  ${order.orderCondition.servicePhone}  ${order.orderCondition.area.name} ${order.orderCondition.serviceAddress} ~n服务明细:${msg} ~n---(反馈):"/>
                <input type="hidden" id="txtordermsg" name="txtordermsg" value="${ordermsg}"/>
                <c:if test="${cancopy eq true }">
                    <c:set var="usermsg"
                           value="您的${msg }，${fn:substring(order.orderCondition.engineer.name,0,1)}师傅${order.orderCondition.engineer.mobile}已接单，客服${fn:substring(order.orderCondition.kefu.name,0,1)}小姐${order.orderCondition.kefu.phone}/${fourServicePhone}"/>
                    <c:set var="msg"
                           value="${order.orderCondition.userName}${order.orderCondition.servicePhone }${' '}${order.orderCondition.area.name} ${order.orderCondition.serviceAddress} ${msg}请2小时内联系用户确认环境并预约，48小时内上门，严禁对产品作任何评价，有问题请联系客服"/>
                    <c:set var="msg"
                           value="${msg}${fn:substring(order.orderCondition.kefu.name,0,1)}${'小姐'}${order.orderCondition.kefu.phone}/${fourServicePhone}。"/>
                    <input type="hidden" id="txengineertmsg" name="txengineertmsg" value="${msg }"/>
                    <input type="hidden" id="txtusermsg" name="txtusermsg" value="${usermsg}"/>
                    <c:if test="${cancopy eq true }">
                        <shiro:hasPermission name="sd_order_message_btn">
                            <a style="margin-left: 200px;" href="javascript:void(0)"
                               id="btn_engineer_copy" class="btn btn-success btn-mini">复制安维短信</a>
                            <a style="margin-left: 10px;" href="javascript:void(0)"
                               id="btn_user_copy" class="btn btn-success btn-mini">复制用户短信</a>
                        </shiro:hasPermission>
                    </c:if>
                </c:if>
                <c:if test="${cancopy eq true }">
                    <a style="margin-left: 10px;" href="javascript:void(0)"
                       id="btn_order_copy" class="btn btn-success btn-mini">复制订单信息</a>
                </c:if>
                <c:if test="${cancopy ne true }">
                    <a style="margin-left: 320px;" href="javascript:void(0)"
                       id="btn_order_copy" class="btn btn-success btn-mini">复制订单信息</a>
                </c:if>
                <!-- 完成照片-->
                <c:if test="${order.orderCondition.finishPhotoQty > 0}">
                    <a class="btn btn-primary btn-mini" id="btnFinishPhoto" href="javascript:;"
                       onclick="ServicePointOrderService.browsePhotoList('${order.id}','${order.quarter}');"
                       title="点击浏览完成照片">完成照片</a>
                </c:if>
                <a href="#divheader" class="accordion-toggle" data-toggle="collapse">基本信息
                    <span class="arrow"></span>
                </a>
            </div>
            <div id="divheader" class="accordion-body">
                <table class="table table-bordered table-striped table-hover" style="margin-bottom: 0px;">
                    <tbody>
                    <tr>
                        <td class="tdlable">
                            <label class="control-label">订单编号:</label>
                        </td>
                        <td class="tdbody">
                            <span id="spOrderNo">${order.orderNo}</span><br>
                            <span class="alert alert-info">${order.orderCondition.status.label} </span>
                        </td>
                        <td class="tdlable">
                            <label class="control-label">客户名称:</label>
                        </td>
                        <td class="tdbody">
                            <a href="javascript:" data-toggle="tooltipeast"
                               data-tooltip="${order.orderCondition.customer.remarks}">
                                    ${order.orderCondition.customer.name}
                            </a>
                            <c:if test="${order.dataSource.value != '0'}">
                                <br>${order.dataSource.label}${order.b2bShop==null || order.b2bShop.shopId == ""?"":"-".concat(order.b2bShop.shopName)}
                            </c:if>
                        </td>
                    </tr>
                    <tr>
                        <td class="tdlable"><label class="control-label">联系人:</label></td>
                        <td class="tdbody">${order.orderCondition.userName}</td>
                        <td class="tdlable"><label class="control-label">手机:</label></td>
                        <td class="tdbody">${order.orderCondition.phone1}</td>
                    </tr>
                    <tr>
                        <td class="tdlable"><label class="control-label">座机:</label></td>
                        <td class="tdbody">${order.orderCondition.phone2}</td>
                        <td class="tdlable"><label class="control-label">实际联络电话:</label></td>
                        <td class="tdbody">${order.orderCondition.servicePhone}</td>
                    </tr>
                    <tr>
                        <td class="tdlable"><label class="control-label">用户地址:</label></td>
                        <td class="tdbody">${order.orderCondition.area.name} ${order.orderCondition.address}</td>
                        <td class="tdlable"><label class="control-label">实际上门地址:</label></td>
                        <td class="tdbody">${order.orderCondition.area.name}${order.orderCondition.serviceAddress}</td>
                    </tr>
                    <tr>
                        <td class="tdlable"><label class="control-label">服务描述:</label></td>
                        <td class="tdbody" colspan="3">${order.description}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <c:set var="cuser" value="${fns:getUser()}"/>
        <shiro:hasPermission name="sd:order:showserviceinfo">
            <div class="accordion-group" style="margin-top:2px;">
                <div class="accordion-heading">
                    <a href="#divservice" class="accordion-toggle collapsed"
                       data-toggle="collapse">客服信息 <span class="arrow"></span>
                    </a>
                </div>
                <c:set var="service" value="${order.orderCondition.kefu}"/>
                <div id="divservice" class="accordion-body collapse">
                    <table class="table table-bordered table-striped table-hover"
                           style="margin-bottom: 0px;">
                        <tbody>
                        <tr>
                            <td class="tdlable"><label class="control-label">客服:</label>
                            </td>
                            <td class="tdbody">${empty service?'':order.orderCondition.kefu.name}
                                <c:if test="${!empty order.orderCondition.kefu.qq}">
                                    <a style="padding-left: 20px;" target="_blank"
                                       href="http://wpa.qq.com/msgrd?v=3&uin=${order.orderCondition.kefu.qq}&site=qq&menu=yes">
                                        <img border="0" src="http://wpa.qq.com/pa?p=2:572202493:52"
                                             alt="点击这里给我发消息" title="联系客服QQ：${order.orderCondition.kefu.qq}"/>
                                    </a>
                                </c:if>
                            </td>
                            <td class="tdlable"><label class="control-label">手机号:</label>
                            </td>
                            <td class="tdbody">${empty service?'':order.orderCondition.kefu.mobile}</td>
                        </tr>
                        <tr>
                            <td class="tdlable"><label class="control-label">电话:</label>
                            </td>
                            <td class="tdbody">${empty service?'':order.orderCondition.kefu.phone}
                            </td>
                            <td class="tdlable"><label class="control-label">业务员:</label>
                            </td>
                            <td class="tdbody">${order.orderCondition.customer.sales.name}
                                <c:if test="${!empty order.orderCondition.customer.sales.qq}">
                                    <a style="padding-left: 20px;" target="_blank"
                                       href="http://wpa.qq.com/msgrd?v=3&uin=${order.orderCondition.customer.sales.qq}&site=qq&menu=yes"><img
                                            border="0" src="http://wpa.qq.com/pa?p=2:572202493:52" alt="点击这里给我发消息"
                                            title="联系业务员QQ：${order.orderCondition.customer.sales.qq}"/>
                                    </a>
                                </c:if>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </shiro:hasPermission>
        <c:if test="${order.orderCondition.servicePoint != null && order.orderCondition.servicePoint.id != null}">
            <shiro:hasPermission name="sd:order:showengineerinfo">
                <div class="accordion-group" style="margin-top:2px;">
                    <div class="accordion-heading">
                        <a href="#divengineer" class="accordion-toggle"
                           data-toggle="collapse">安维人员 <span class="arrow"></span>
                        </a>
                    </div>
                    <div id="divengineer" class="accordion-body">
                        <table class="table table-bordered table-striped table-hover"
                               style="margin-bottom: 0px;">
                            <tbody>
                            <tr>
                                <td class="tdlable"><label class="control-label">网点编号:</label>
                                </td>
                                <td class="tdbody">${order.orderCondition.servicePoint.servicePointNo}
                                    <c:if test="${!empty order.orderCondition.servicePoint.finance.bankIssue && order.orderCondition.servicePoint.finance.bankIssue.label ne '无' && !empty order.orderCondition.servicePoint.finance.bankIssue.label}">
                                        <label id="lbltotalCharge"
                                               class="alert alert-error">${order.orderCondition.servicePoint.finance.bankIssue.label }</label>
                                    </c:if>
                                </td>
                                <td class="tdlable"><label class="control-label">姓名:</label>
                                </td>
                                <td class="tdbody">
                                        ${order.orderCondition.servicePoint.primary.name}(主)
                                </td>
                            </tr>
                            <tr>
                                <td class="tdlable"><label class="control-label">手机号:</label>
                                </td>
                                <td class="tdbody">${order.orderCondition.servicePoint.primary.contactInfo}</td>
                                <td class="tdlable"><label class="control-label">电话:</label>
                                </td>
                                <td class="tdbody"></td>
                            </tr>
                            <tr>
                                <td class="tdlable"><label class="control-label">联络方式1:</label>
                                </td>
                                <td class="tdbody">${order.orderCondition.servicePoint.contactInfo1}</td>
                                <td class="tdlable"><label class="control-label">联络方式2:</label>
                                </td>
                                <td class="tdbody">${order.orderCondition.servicePoint.contactInfo2}</td>
                            </tr>
                            <tr>
                                <td class="tdlable"><label class="control-label">安维结算方式:</label>
                                </td>
                                <td class="tdbody">${order.orderFee.engineerPaymentType.label}</td>
                                <td class="tdlable"><label class="control-label">订单结算方式:</label>
                                </td>
                                <td class="tdbody">${order.orderFee.orderPaymentType.label}</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </shiro:hasPermission>
        </c:if>
    </c:if>
</form:form>
<br/>
<c:if test="${errorFlag == false}">
    <div class="tabbable" style="margin:0px 20px">
        <ul class="nav nav-tabs">
            <c:set var="tabCount" value="${tabCount+1}"/>
            <c:if test="${tabCount==1 && empty tabActiveName}"><c:set var="tabActiveName" value="tabTracking"/></c:if>
            <li id="liTabOrderProducts" class="${tabCount==1?'active':''}">
                <a href="#tab1" data-toggle="tab">产品详细清单</a>
            </li>
        </ul>
        <div class="tab-contenTt">
            <div class="tab-pane active" id="tab1" title="产品详细清单">
                <div class="control-group">
                    <table id="itemTable"
                           class="table table-striped table-bordered table-condensed table-hover"
                           style="margin-bottom: 0px;">
                        <thead>
                        <tr>
                            <th width=30px>序号</th>
                            <th>服务类型</th>
                            <th>产品</th>
                            <th>品牌</th>
                            <th>型号/规格</th>
                            <th>产品图片</th>
                            <th>数量</th>
                            <th>备注</th>
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
                                <td>
                                    <c:if test="${not empty item.pics}">
                                        <div class="gallery-thumb">
                                            <c:forEach items="${item.pics}" var="pic">
                                                <a href="javascript:;"><img src="${pic}" data-original="${pic}"></a>
                                            </c:forEach>
                                        </div>
                                    </c:if>
                                </td>
                                <td>${item.qty }</td>
                                <td><c:if test="${!empty item.expressCompany }">
                                    <a href="http://www.kuaidi100.com/chaxun?com=${item.expressCompany.value}&nu=${item.expressNo }"
                                       target="_blank" title="点击进入快递100">
                                            ${item.expressCompany.label}&nbsp;&nbsp;${item.expressNo}
                                    </a>
                                </c:if>
                                </td>
                            </tr>
                            <c:set var="ridx" value="${ridx+1}"/>
                            <c:set var="totalQty" value="${totalQty+item.qty}"/>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
    <script type="text/javascript">
        var tabName = '${tabActiveName}';

        function loadTabContent() {
            $("#lnk" + tabName).trigger("click");
        }
    </script>
</c:if>
</body>
</html>