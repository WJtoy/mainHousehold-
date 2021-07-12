<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>天猫发送内容</title>
    <meta name="description" content="天猫发送内容">
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

        var params = {updater: $("#updater").val(),type: $("#type").val(),status: $("#status").val(),dataSource: $("#dataSource").val(), b2bInterfaceId: $("#b2bInterfaceId").val(),processLogId:$("#processLogId").val(),workcardId:$("#orderNo").val()};

        if ($("#status").val() == 3) {
            var serviceDate = $("#serviceDate").val();
            if (Utils.isEmpty(serviceDate)){
                layerMsg("预约时间不能为空！");
                return false;
            }else {
                params.serviceDateD = serviceDate;
            }
        }
        if ($("#status").val() == 5){
            var completeDate = $("#completeDate").val();
            if (Utils.isEmpty(completeDate)){
                layerMsg("完成时间不能为空！");
                return false;
            }else {
                params.completeDateD = completeDate;
            }
        }
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
                url: "${ctx}/tmall/rpt/tmallorder/tmallFailLogRetryData",
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
        var params = {processLogId:$("#processLogId").val()}
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
                url: "${ctx}/tmall/rpt/tmallorder/tmallFailLogClose",
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


    <style type="text/css">
        .form-horizontal .control-label{width:120px;}
        .div-inline{ display:inline}
        .form-horizontal .controls {
            margin-left:124px;*margin-left:0px;*display:block;*padding-left:20px;
        }
        .row-fluid .span4 {width: 400px;}
        .row-fluid .span6 {width: 600px;}

    </style>
</head>
<body>
<form:form id="inputForm" modelAttribute="b2BTmallJsonBean" action="${ctx}/tmall/rpt/tmallorder/tmallRetryForm" method="post" class="form-horizontal">

    <sys:message content="${message}"/>
    <input type="hidden" id="dataSource" name="dataSource" value="${b2BTmallJsonBean.dataSource}">
    <input type="hidden" id="b2bInterfaceId" name="b2bInterfaceId" value="${b2BTmallJsonBean.b2bInterfaceId}">
    <input type="hidden" id="processLogId" name="processLogId" value="${b2BTmallJsonBean.processLogId}">
    <input type="hidden" id="status" name="status" value="${b2BTmallJsonBean.status}">
    <input type="hidden" id="type" name="status" value="${b2BTmallJsonBean.type}">
    <div class="row-fluid">
        <div class="span4" style="margin-top: 20px">
            <div class="control-group">
                <input type="hidden"id="orderNo" name="orderNo" value="${b2BTmallJsonBean.workcardId}">
                <label class="control-label">客户单号:</label>
                <label class="control-label" style="margin-left: 5px;min-height:30px;text-align: left">${b2BTmallJsonBean.workcardId}</label>
            </div>
        </div>
        <div class="span4" style="margin-top: 20px">
            <div class="control-group">
                <label class="control-label">工单号:<input type="hidden" name="orderNo" value="${b2BTmallJsonBean.kklOrderNo}"></label>
                <label class="control-label" style="margin-left: 5px"><a  class="input-block-level"  href="javascript:void(0);" onclick="Order.viewOrderDetail('${b2BTmallJsonBean.kklOrderId}','${b2BTmallJsonBean.quarter}');">${b2BTmallJsonBean.kklOrderNo}</a>
                </label>
            </div>
        </div>
    </div>
    <div id="divMain">

        <div class="row-fluid" style="margin-top:10px;min-height:30px;">
            <div class="span4" >
                <div class="control-group">
                    <label class="control-label">操作类型:</label>
                    <label class="control-label" style="margin-left: 5px;text-align: left">${b2BTmallJsonBean.actionType == 10 ?"预约":
                            b2BTmallJsonBean.actionType == 30?"取消":
                                    b2BTmallJsonBean.actionType == 40?"退单":
                                                b2BTmallJsonBean.actionType == 50?"完成":""}</label>
                </div>
            </div>
        </div>
        <div class="row-fluid" style="margin-top:10px;min-height:30px;">
            <div class="span4" >
                <div class="control-group">
                    <label class="control-label">操作人:</label>
                    <label class="control-label" style="margin-left: 5px;text-align: left">${b2BTmallJsonBean.updater}</label>
                    <input type="hidden" id="updater" name="updater" value="${b2BTmallJsonBean.updater}">
                </div>
            </div>
        </div>

        <c:choose>
        <c:when test="${b2BTmallJsonBean.status eq 5}">
        <div class="row-fluid" style="margin-top:10px;min-height:30px;">
            <div class="span4" >
                <div class="control-group">
                    <label class="control-label">完工时间:</label>
                    <div class="controls" >
                        <input id="completeDate" name="completeDate" type="text" readonly="readonly" style="width:198px;margin-left:2px"  class="input-small Wdate"
                               value="<fmt:formatDate value='${b2BTmallJsonBean.completeDateD}' pattern='yyyy-MM-dd HH:mm:ss' type='date'/>"
                               onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',isShowClear:false});"/><span class="add-on red" style="margin-left: 2px">*</span>
                    </div>
                </div>
            </div>
        </div>
        </c:when>
        <c:when test="${b2BTmallJsonBean.status eq 3}">
            <div class="row-fluid" style="margin-top:10px;min-height:30px;">
                <div class="span4" >
                    <div class="control-group">
                        <label class="control-label">预约时间:</label>
                        <div class="controls" >
                            <input id="serviceDate" name="serviceDate" type="text" readonly="readonly" style="width:198px;margin-left:4px"  class="input-small Wdate"
                                   value="<fmt:formatDate value='${b2BTmallJsonBean.serviceDateD}' pattern='yyyy-MM-dd HH:mm:ss' type='date'/>"
                                   onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',isShowClear:false});"/><span class="add-on red" style="margin-left: 2px">*</span>
                        </div>
                    </div>
                </div>
            </div>
        </c:when>
            <c:otherwise>
            <div class="row-fluid" style="margin-top:10px;min-height:30px;">
                <div class="span4" >
                    <div class="control-group">
                        <label class="control-label">操作时间:</label>
                        <div class="controls" >
                            <label  class="control-label" style="margin-left: 5px;text-align: left;width: 300px">
                                <fmt:formatDate value="${b2BTmallJsonBean.processUpdateDateD}" pattern="yyyy-MM-dd HH:mm:ss" type="date"/>
                            </label>


                        </div>
                    </div>
                </div>
            </div>
        </c:otherwise>
        </c:choose>
        <div class="row-fluid" style="margin-top: 10px;min-height: 30px;">
            <div class="span6">
                <div class="control-group">
                    <label class="control-label" style="color:red">失败原因:</label>
                    <label class="control-label" style="margin-left: 5px;text-align: left;color: red">
                            ${b2BTmallJsonBean.processComment}
                    </label>
                </div>
            </div>
        </div>

    </div>
    <div class="control-group">
        <div class="controls" style="margin-top: 30px">

                <input id="btnSubmit"  class="btn btn-primary" type="button" value="重 发" onclick="resend();"/>
                &nbsp;
                <input id="btnCancel" class="btn" type="button" value="忽 略" onclick="closeme();"/>
                &nbsp;
                <input id="btnClose" class="btn" type="button" value="关 闭" onclick="closeForm();"/>

        </div>
    </div>


</form:form>



</body>
</html>