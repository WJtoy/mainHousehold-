<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>韩电发送内容</title>
    <meta name="description" content="韩电发送内容">
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <style>
        #editBtn{
            position: fixed;
            left: 0px;
            bottom: 5px;
            width: 100%;
            height: 50px;
            background: #fff;
            z-index: 10;
            border-top: 1px solid #e5e5e5;
            text-align: right;
        }
        #main{
            height: 80%;
            padding: 66px;
            margin-left: -110px;
            margin-top: -25px;
        }
        .lineBot{
            margin-bottom: 10px;
        }
        input[type=text]{
            width: 250px;
        }
    </style>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>

    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <!-- image viewer -->
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <script type="text/javascript">

    var this_index = top.layer.index;
    function resend(){

        var params = {id:$("#processLogId").val(),machineNo:$("#machineNo").val(),buyDate:$("#buyDate").val(),productId:$("#productId").val(),remark:$("#remark").val()};

        var confirmClickTag = 0;
        top.layer.confirm('确定重发吗？', {icon: 3, title:'系统确认'}, function(index){
            if(confirmClickTag == 1){
                return false;
            }
            confirmClickTag = 1;
            top.layer.close(index);//关闭本身
            var loadingIndex = layer.msg('正在提交，请稍等...', {
                icon: 16,
                time: 0,
                shade: 0.3
            });

            var ajaxSuccess = 0;
            $.ajax({
                cache: false,
                type: "POST",
                url: "${ctx}/b2b/rpt/processlog/kegCompletedRetry",
                dataType: 'json',
                contentType:"application/json",
                data: JSON.stringify(params),
                success: function (data) {
                    if (data.success) {
                        layer.close(loadingIndex);
                        top.layer.close(this_index);
                        layerMsg(data.message);
                        var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                        if (iframe != undefined) {
                            iframe.repage();
                        }
                    }else {
                        layer.close(loadingIndex);
                        layerError(data.message,"错误提示");
                    }
                },
                error: function (e) {
                    layer.close(loadingIndex);
                    layerError(data.message,"错误提示");
                }
            });
        });
    };
    function closeme(){
        var confirmClickTag = 0;
        top.layer.confirm('确定忽略本条信息吗？', {icon: 3, title:'系统确认'}, function(index){
            if(confirmClickTag == 1){
                return false;
            }
            confirmClickTag = 1;
            top.layer.close(index);//关闭本身
            var loadingIndex = layer.msg('正在提交，请稍等...', {
                icon: 16,
                time: 0,
                shade: 0.3
            });
            var ajaxSuccess = 0;
            $.ajax({
                cache: false,
                type: "GET",
                url: "${ctx}/b2b/rpt/processlog/kegCompletedCloseLog?id=${completedRetryBean.id}",
                success: function (data) {
                    if (data.success) {
                        layer.close(loadingIndex);
                        top.layer.close(this_index);
                        layerMsg(data.message);
                        var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                        if (iframe != undefined) {
                            iframe.repage();
                        }
                    }else {
                        layer.close(loadingIndex);
                        layerError(data.message,"错误提示");
                    }
                },
                error: function (e) {
                    layer.close(loadingIndex);
                    layerError(data.message,"错误提示");

                }
            });
        });
    };

    function closeForm(){
        top.layer.close(this_index);
    }

    $(document).ready(function() {
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


    var clickTag = 0;
    var viewer;
    function imageViewer(){
        viewer = $("#divMain").viewer('destroy').viewer(
            {
                url: "data-original",
                filter:function(image) {
                    if(image.src.lastIndexOf("/upload-photo.png")>0){
                        return false;
                    }
                    return true;
                },
                viewed: function(image) {
                },
                shown:function () {
                    // console.log(this.viewer);
                    if(this.viewer.index == -1){
                        this.viewer.hide();
                        //$(".viewer-container").removeClass("viewer-in").addClass("viewer-hide");
                    }
                }
            }
        );
    }

    $(document).ready(function () {
        imageViewer();
    });

    function clickFile(id){
        $(id).click();
    }
    </script>

    <style>
        .img_ho div{width: 200px;height:200px;float: left;margin: 10px}
        .img_ho div img{width:200px;height: 200px}
    </style>

    <style type="text/css">
        .form-horizontal .control-label{width:120px;}
        .div-inline{ display:inline}
        .form-horizontal .controls {
            margin-left:100px;*margin-left:0px;*display:block;*padding-left:20px;
        }


    </style>
</head>
<body>
<form:form id="inputForm" modelAttribute="completedRetryBean" action="${ctx}/b2b/rpt/processlog/canboResend" method="post" class="form-horizontal">

    <sys:message content="${message}"/>
    <input type="hidden" id="processLogId" name="processLogId" value="${completedRetryBean.id}">
    <div class="row-fluid">
        <div class="span6" style="margin-top: 20px">
            <div class="control-group">
                <label class="control-label">工单号：<input type="hidden" name="kklOrderNo" value="${completedRetryBean.appBillID}"></label>
                <label class="control-label">
                    <a  class="input-block-level"  href="javascript:void(0);" onclick="Order.viewOrderDetail('${completedRetryBean.orderId}','${completedRetryBean.quarter}');">${completedRetryBean.appBillID}</a>
                </label>
            </div>
        </div>
    </div>

<%--<div id="main">--%>
    <div class="row-fluid" style="margin-top: 10px;margin-bottom: 5px">
        <div class="span6">
            <div class="control-group">
                <label class="control-label">姓名：</label>
                <div class="controls">
                    <input type="text" readonly="readonly"  value="${completedRetryBean.name}"/><%--切换为微服务--%>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group">
                <label class="control-label">电话：</label>
                <div class="controls">
                    <input type="text"  value="${completedRetryBean.mobile}"
                           readonly="readonly"/>
                </div>
            </div>
        </div>
    </div>

    <div class="control-group" style="margin-top: 10px;margin-bottom: 5px">
        <label class="control-label">地址：</label>
        <div class="controls" >
            <input type="text" readonly="readonly"  value="${completedRetryBean.address}" style="width: 710px"/><%--切换为微服务--%>
        </div>
    </div>

<%--        <div class="row-fluid" style="margin-top:10px;margin-left:20px;clear: both">--%>
<%--            <div class="span4" >--%>
<%--                <div class="control-group">--%>
<%--                    <label class="control-label">完工条码:</label>--%>
<%--                    <div class="controls"  >--%>
<%--                        <input id="barcode${index}" name="barcodes" value="${completedRetryBean.barcode}" maxlength="20" style="margin-left: 2px"><span class="add-on red" style="margin-left: 2px">*</span>--%>
<%--                    </div>--%>
<%--                </div>--%>
<%--            </div>--%>
<%--        </div>--%>
    <div class="control-group" style="margin-top: 10px;margin-bottom: 5px">
        <label class="control-label">机号：</label>
        <div class="controls">
            <input id="machineNo" type="text"   value="${completedRetryBean.machineNo}"/><span class="add-on red" style="margin-left: 3px">*</span>
        </div>
    </div>

    <div class="control-group" style="margin-top: 10px;margin-bottom: 5px">
        <label class="control-label">型号：</label>
        <div class="controls">
            <form:select id="productId" path="productId" cssClass="required input-medium" cssStyle="width: 264px;">
                <option value="">请选择</option>
                <c:forEach items="${customerProductModelList}" var="model">
                    <option value="${model.customerModel}" ${model.id == completedRetryBean.modelId ? 'selected':''}>
                        ${model.customerProductName}
                    </option>
                </c:forEach>
            </form:select> <span class="add-on red">*</span>
        </div>
    </div>
    <div class="control-group" style="margin-top: 10px;margin-bottom: 5px">
        <label class="control-label">备注：</label>
        <div class="controls">
            <input id="remark" type="text"   value="${completedRetryBean.remark}"/><span class="add-on red" style="margin-left: 3px">*</span>
        </div>
    </div>
    <div class="control-group" style="margin-top: 10px;margin-bottom: 5px">
        <label class="control-label">购买时间：</label>
        <div class="controls">
            <input id="buyDate" name="buyDate" type="text" readonly="readonly"
                   maxlength="20" class="input-small Wdate"
                   value="${completedRetryBean.buyDate}"
                   onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',isShowClear:false});"/><span class="add-on red" style="margin-left: 3px">*</span>
        </div>
    </div>

    <div class="control-group" style="margin-top: 10px">
        <label class="control-label" style="color:red;">失败原因：</label>
        <div class="controls" style="margin-top: 3px">
                    <label style="color:red">
                            ${completedRetryBean.processComment}
                    </label>
        </div>
        </div>
<%--</div>--%>
<div id="divMain">

    <c:forEach items="${completedRetryBean.orderServiceItems}" var="item">

    <div class="img_ho" style="margin-left:70px;margin-top:10px;clear: both" >
        <c:set var="index" value="0"/>
        <c:forEach items="${item.pics}" var="pic">
            <c:set value="${index+1}" var="index"/>
        <div>
            <img src="${pic}"data-original="${pic}" width="200px" height="200px"/>
        </div>
        </c:forEach>
    </div>
    </c:forEach>
</div>
    <div class="control-group" style="margin-top:10px;clear: both">
        <div class="controls" style="margin-top: 30px">
                <input id="btnSubmit"  class="btn btn-primary" type="button" value="重 发" onclick="resend();"/>
                &nbsp;
                <input id="btnClose" class="btn" type="button" value="忽 略" onclick="closeme();"/>
                &nbsp;
                <input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closeForm();"/>
        </div>
    </div>

</form:form>



</body>
</html>