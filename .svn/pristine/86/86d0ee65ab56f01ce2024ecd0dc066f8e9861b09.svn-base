<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>派单</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        $(document).ready(function () {
            $("#inputForm").validate({
                submitHandler: function (form) {
                    if (clickTag == 1) {
                        return false;
                    }
                    clickTag = 1;
                    var $btnSubmit = $("#btnSubmit");
                    $btnSubmit.attr('disabled', 'disabled');
                    var loadingIndex;
                    var ajaxSuccess = 0;
                    $.ajax({
                        async: false,
                        cache: false,
                        type: "POST",
                        url: "${ctx}/servicePoint/sd/orderOperation/servicePointPlan?" + (new Date()).getTime(),
                        data: $(form).serialize(),
                        beforeSend: function () {
                            loadingIndex = layer.msg('正在提交，请稍等...', {
                                icon: 16,
                                time: 0,
                                shade: 0.3
                            });
                        },
                        complete: function () {
                            //console.log("" + new Date().getTime() + " [complete] clickTag:" + clickTag + " ,ajaxSuccess:" + ajaxSuccess);
                            if (loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            if (ajaxSuccess == 0) {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                            }
                        },
                        success: function (data) {
                            if (ajaxLogout(data)) {
                                return false;
                            }
                            if (data && data.success == true) {
                                ajaxSuccess = 1;
                                top.layer.close(this_index);
                                layerMsg('提交成功');
                                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if (iframe != undefined) {
                                    iframe.repage();
                                }
                            }
                            else if (data && data.message) {
                                layerError(data.message, "错误提示");
                            }
                            else {
                                layerError("派单错误", "错误提示");
                            }
                            return false;
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText, null, "派单错误，请重试!");
                        }
                    });
                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });

        });

        function selEngineerChange(data) {
            if (data) {
//				$("#sendUserMessageFlag").prop('checked', true);
            }
        }

        function closethisfancybox() {
            top.layer.close(this_index);
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
        });
    </script>
    <style type="text/css">
        .form-horizontal {
            margin-top: 5px;
        }
    </style>
</head>
<body>
<form:form id="inputForm" modelAttribute="order" action="${ctx}/servicePoint/sd/orderOperation/servicePointPlan" method="post"
           class="form-horizontal">
    <form:hidden path="id"/>
    <form:hidden path="quarter"/>
    <form:hidden path="orderCondition.servicePoint.id"></form:hidden>
    <sys:message content="${message}"/>
    <div class="row-fluid">
        <div class="span10">
            <div class="control-group">
                <label class="control-label">安维人员:</label>
                <div class="controls">
                    <servicePoint:engineerSelectorForPlan id="engineer" name="orderCondition.engineer.id" value=""
                                       labelName="orderCondition.engineer.name"
                                       labelValue="${order.orderCondition.engineer.name}"
                                       width="1200" height="700" exceptId="${order.orderCondition.engineer.id}"
                                       delFlag="0"
                                       callbackmethod="selEngineerChange" cssStyle="width:320px;"
                                       areaId="${order.orderCondition.area.id}"
                                       title="选择安维人员" servicePointId="${order.orderCondition.servicePoint.id}"
                                       cssClass="required"/>
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span10">
            <div class="control-group">
                <label class="control-label">派单说明:</label>
                <div class="controls">
                    <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xxlarge"
                                   cssStyle="width: 358px;"/>
                </div>
            </div>
        </div>
    </div>

    <div class="form-actions">
        <c:if test="${empty canSave || canSave ne false }">
            <shiro:hasPermission name="sd:order:engineeraccept"><input id="btnSubmit" class="btn btn-primary"
                                                                       type="submit"
                                                                       value="保 存"/>&nbsp;</shiro:hasPermission>
        </c:if>
        <input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethisfancybox();"/>
    </div>

    <div class="accordion-group" style="margin-top:2px;">
        <div class="accordion-heading">
            <a href="#divheader" class="accordion-toggle" data-toggle="collapse">基本信息 <span class="arrow"></span></a>
        </div>
        <div id="divheader" class="accordion-body">
            <table class="table table-bordered table-striped" style="margin-bottom: 0px;">
                <tbody>
                <tr>
                    <td class="tdlable"><label class="control-label">联系人:</label></td>
                    <td class="tdbody">${order.orderCondition.userName}</td>
                    <td class="tdlable"><label class="control-label">实际联系电话:</label></td>
                    <td class="tdbody">${order.orderCondition.servicePhone}</td>
                </tr>
                <tr>
                    <td class="tdlable"><label class="control-label">实际上门地址:</label></td>
                    <td class="tdbody">${order.orderCondition.area.name}${order.orderCondition.serviceAddress}</td>
                    <td class="tdlable"><label class="control-label">座机:</label></td>
                    <td class="tdbody">${order.orderCondition.phone2}</td>
                </tr>
                <tr>
                    <td class="tdlable"><label class="control-label">服务描述:</label></td>
                    <td class="tdbody" colspan="3">${order.description}</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
    <div class="control-group">
        <table id="productTable" class="table table-striped table-bordered table-condensed" style="margin-bottom: 0px;">
            <thead>
            <tr>
                <th width=30px>序号</th>
                <th>服务类型</th>
                <th>产品</th>
                <th>品牌</th>
                <th>型号/规格</th>
                <th>数量</th>
                <th>快递</th>
            </thead>
            <tbody>
            <c:set var="rownum" value="0"/>
            <c:forEach items="${order.items}" var="item">
                <tr>
                    <td>${rownum+1}</td>
                    <td>${item.serviceType.name }</td>
                    <td>${item.product.name }</td>
                    <td>${item.brand }</td>
                    <td>${item.productSpec }</td>
                    <td>${item.qty }</td>
                    <td><a href="http://www.kuaidi100.com/chaxun?com=${item.expressCompany.value}&nu=${item.expressNo }"
                           target="_blank" title="点击进入快递100">
                            ${item.expressCompany.label} ${item.expressNo }
                    </a></td>
                </tr>
                <c:set var="rownum" value="${rownum+1}"/>
            </c:forEach>

            <tr>
                <td colspan="2">客户说明</td>
                <td colspan="5">${order.orderCondition.customer.remarks}</td>
            </tr>
            </tbody>
        </table>
    </div>

</form:form>
</body>
</html>