<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>云米异常重发</title>
    <meta name="description" content="云米异常重发">
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
        var infoArr = [];
        var resultArr = [];
        var i = 0;
        <c:forEach items="${completedRetryBean}" var="item">
        infoArr[i] = '${item.infoJson}';
        resultArr[i] = '${item.resultJson}';
        i++;
        </c:forEach>

        function showInfoJson(index) {
            var json = infoArr[index];
            top.layer.open({
                title: '发送内容',
                content: json
            });
        }

        function showResultJson(index) {
            var json = resultArr[index];
            top.layer.open({
                title: '返回结果',
                content: json
            });
        }
        var this_index = top.layer.index;
        function resend(apiLogId){
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
                    type: "GET",
                    url: "${ctx}/b2b/rpt/processlog/viomiRetry?id="+apiLogId,
                    success: function (data) {
                        if (data.success) {
                            layer.close(loadingIndex);
                            location.reload();
                            // top.layer.close(this_index);
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
<form:form id="inputForm" modelAttribute="completedRetryBean" action="${ctx}/b2b/rpt/processlog/viomiRetry" method="post" class="form-horizontal">

    <sys:message content="${message}"/>

    <div class="row-fluid">
        <div class="span4" style="margin-top: 20px">
            <div class="control-group">

                <label class="control-label">客户单号：</label>
                <label class="control-label" style="margin-left: 5px;min-height:30px;text-align: left">${orderInfo.orderNumber}</label>
            </div>
        </div>
        <div class="span4" style="margin-top: 20px">
            <div class="control-group">
                <label class="control-label">工单号：</label>
                <label class="control-label" ><a  class="input-block-level"  href="javascript:void(0);" onclick="Order.viewOrderDetail('${orderInfo.kklOrderId}','${orderInfo.quarter}');">${orderInfo.kklOrderNo}</a>
                </label>
            </div>
        </div>
    </div>

    <div id="divGrid" style="overflow-x:hidden; margin-top: 10px;">
        <table id="contentTable"
               class="table table-striped table-bordered table-condensed table-hover">
            <thead>
            <tr>
                <th>序号</th>
                <th>操作状态</th>
                <th>发送内容</th>
                <th>返回结果</th>
                <th>创建时间</th>
                <th>备注</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <c:set var="index" value="0"/>

            <c:forEach items="${completedRetryBean}" var="item">
                <c:set var="index" value="${index+1}"/>
                <tr>
                    <td>${index}</td>
                    <td>${item.interfaceName}</td>

                    <td>
                        <a style="line-height: 30px" class="input-block-level"
                           href="javascript:showInfoJson(${index-1});"
                           data-toggle="tooltip" data-tooltip='${item.infoJson}'>${fns:abbr(item.infoJson,40)}</a>
                    </td>
                    <td><a href="javascript:showResultJson(${index-1});" data-toggle="tooltip"
                           data-tooltip='${item.resultJson}'>${fns:abbr(item.resultJson,40)}</a></td>
                    <jsp:useBean id="timestamp" class="java.util.Date"/>
                    <jsp:setProperty name="timestamp" property="time" value="${item.createDt}"/>
                    <td><fmt:formatDate value="${timestamp}" pattern="yyyy/MM/dd HH:mm:ss"/></td>
                    <td><a href="javascript:void(0);" data-toggle="tooltip"
                           data-tooltip='${item.processComment}'>${fns:abbr(item.processComment,40)}</a></td>
                    <td>
                        <c:if test="${orderInfo.apiExceptionStatus eq 1 and item.firstExceptionId le item.id}">
                        <input id="btnSubmit"  class="btn btn-primary" type="button" value="重 发" onclick="resend('${item.id}')" />
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    </div>
 

</form:form>



</body>
</html>