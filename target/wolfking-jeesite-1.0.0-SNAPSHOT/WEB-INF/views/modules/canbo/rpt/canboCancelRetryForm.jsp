<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>同望发送内容</title>
    <meta name="description" content="同望发送内容">
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>

    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <!-- image viewer -->
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <script type="text/javascript">
    $(document).ready(function() {

        //var data = $("#inputForm").serialize();

    });
    var this_index = top.layer.index;
    function resend(){

        var params = {dataSource: $("#dataSource").val(), b2bInterfaceId: $("#b2bInterfaceId").val(),processLogId:$("#processLogId").val(),orderNo:"${completedRetryBean.orderNo}"};

        var cancelMan =  $("#cancelMan").val();
        if (Utils.isEmpty(cancelMan)) {
            layerMsg("取消人不能为空！");
            return false;
        }
        var cancelDate = $("#cancelDate").val();
        if (Utils.isEmpty(cancelDate)){
            layerMsg("取消时间不能为空！");
            return false;
        }
        var cancelRemark = $("#cancelRemark").val();
        if (Utils.isEmpty(cancelRemark)){
            layerMsg("取消原因不能为空！");
            return false;
        }
        params.cancelDate = cancelDate;
        params.cancelMan = cancelMan;
        params.cancelRemark = cancelRemark;

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
                url: "${ctx}/b2b/rpt/processlog/canboCancelRetryData",
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
                type: "POST",
                url: "${ctx}/b2b/rpt/processlog/canboCancelCloseLog",
                data:  $("#inputForm").serialize(),
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

    </script>

    <style>
        .img_ho div{width: 200px;height:200px;float: left;margin: 10px}
        .img_ho div img{width:200px;height: 200px}
    </style>

    <style type="text/css">
        .form-horizontal .control-label{width:120px;}
        .div-inline{ display:inline}
        .form-horizontal .controls {
            margin-left:120px;*margin-left:0px;*display:block;*padding-left:20px;
        }
        .row-fluid .span4 {width: 400px;}
        .row-fluid .span6 {width: 600px;}

    </style>
</head>
<body>
<form:form id="inputForm" modelAttribute="completedRetryBean" action="${ctx}/b2b/rpt/processlog/canboCancel" method="post" class="form-horizontal">

    <sys:message content="${message}"/>
    <input type="hidden" id="dataSource" name="dataSource" value="${completedRetryBean.dataSource}">
    <input type="hidden" id="b2bInterfaceId" name="b2bInterfaceId" value="${completedRetryBean.b2bInterfaceId}">
    <input type="hidden" id="processLogId" name="processLogId" value="${completedRetryBean.processLogId}">
    <div class="row-fluid">
        <div class="span4" style="margin-top: 20px;min-height:30px;">
            <div class="control-group">
                <label class="control-label">客户单号:<input type="hidden" name="orderNo" value="${completedRetryBean.orderNo}"></label>
                <label class="control-label" style="margin-left: 2px">
                        ${completedRetryBean.orderNo}
                </label>
            </div>
        </div>
        <div class="span4" style="margin-top: 20px">
            <div class="control-group">
                <label class="control-label">工单号:<input type="hidden" name="kklOrderNo" value="${completedRetryBean.kklOrderNo}"></label>
                <label class="control-label">
                    <a  class="input-block-level"  href="javascript:void(0);" onclick="Order.viewOrderDetail('${completedRetryBean.orderId}','${completedRetryBean.quarter}');">${completedRetryBean.kklOrderNo}</a>
                </label>
            </div>
        </div>
    </div>
    <div id="divMain">




        <div class="row-fluid" style="margin-top:10px;min-height:30px;">
            <div class="span4" >
                <div class="control-group">
                    <label class="control-label">取消人:</label>
                    <div class="controls"  >
                        <input id="cancelMan" name="cancelMan" value="${completedRetryBean.cancelMan}" maxlength="20" style="margin-left: 4px"><span class="add-on red" style="margin-left: 2px">*</span>
                    </div>
                </div>
            </div>
        </div>
        <div class="row-fluid" style="margin-top:10px;min-height:30px;">
            <div class="span4" >
                <div class="control-group">
                    <label class="control-label">取消时间:</label>
                    <div class="controls" >
                        <input id="cancelDate" name="cancelDate" type="text" readonly="readonly" style="width:198px;margin-left:4px"  class="input-small Wdate"
                               value="<fmt:formatDate value='${completedRetryBean.cancelDate}' pattern='yyyy-MM-dd HH:mm:ss' type='date'/>"
                               onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',isShowClear:false});"/><span class="add-on red" style="margin-left: 2px">*</span>
                    </div>
                </div>
            </div>
        </div>
        <div class="row-fluid" style="margin-top:10px;min-height:30px;">
            <div class="span6">
                <div class="control-group">
                    <label class="control-label">取消原因:</label>
                    <div class="controls">
                        <form:textarea cssStyle="margin-left: 4px;width: 400px" path="cancelRemark" htmlEscape="false" rows="4" maxlength="300" class="input-block-level required" id="cancelRemark" />
                        <span class="add-on red" style="margin-left: 2px">*</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="row-fluid" style="margin-top: 10px;min-height: 30px;">
            <div class="span6">
                <div class="control-group">
                    <label class="control-label"  style="color:red">失败原因:</label>
                    <label class="control-label" style="margin-left: 5px;text-align: left;color:red">
                            ${completedRetryBean.processComment}
                    </label>
                </div>
            </div>
        </div>

    </div>
    <div class="control-group">
        <div class="controls" style="margin-top: 30px">
            <shiro:hasPermission name="b2b:order:canboResend">
                <input id="btnSubmit"  class="btn btn-primary" type="button" value="重 发" onclick="resend();"/>
                &nbsp;
                <input id="btnCancel" class="btn" type="button" value="忽 略" onclick="closeme();"/>
                &nbsp;
                <input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closeForm();"/>
            </shiro:hasPermission>

        </div>
    </div>


</form:form>



</body>
</html>