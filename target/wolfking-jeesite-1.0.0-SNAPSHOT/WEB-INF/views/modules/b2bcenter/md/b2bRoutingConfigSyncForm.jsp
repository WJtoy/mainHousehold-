<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>同步</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
</head>
<body>
<form:form id="inputForm" action="${ctx}/sd/order/pending" method="post" class="form-horizontal">
    <sys:message content="${message}"/>
    <div class="control-group" style="margin-top:30px;">
        <label class="control-label">数据源:</label>
        <div class="controls">
            <select class="required" name="dataSourceId" id="dataSourceId" style="width: 220px">
                <c:forEach items="${dataSources}" var="dataSource">
                    <option value="${dataSource.value}">${dataSource.label}</option>
                </c:forEach>
            </select>
        </div>
    </div>

    <div class="form-actions" style="text-align: center; padding: 20px 0px 20px 0px;">
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="确 定"/>&nbsp;
        <input id="btnCancel" class="btn" type="button" value="关 闭" onclick="closethisfancybox();"/>
    </div>
</form:form>
<script type="text/javascript">
    var index = parent.layer.getFrameIndex(window.name);
    var clickTag = 0;

    $(document).ready(function () {
        $("#inputForm").validate({
            submitHandler: function (form) {
                if (clickTag == 1) {
                    return false;
                }
                clickTag = 1;
                var $btnSubmit = $("#btnSubmit");
                $btnSubmit.attr("disabled", "disabled");
                var ajaxSuccess = 0;
                var params = {};
                var dataSourceId = $("#dataSourceId").val();
                var loadingIndex;
                $.ajax({
                    async: false,
                    cache: false,
                    type: "POST",
                    url: "${ctx}/b2bcenter/md/product/ajax/syncByDataSource?dataSourceId=" + (dataSourceId || 2),
                    data: params,
                    beforeSend: function () {
                        loadingIndex = top.layer.msg('正在提交，请稍等...', {
                            icon: 16,
                            time: 0,//不定时关闭
                            shade: 0.3
                        });
                    },
                    complete: function () {
                        if (loadingIndex) {
                            top.layer.close(loadingIndex);
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
                            var iframe = getActiveTabIframe();
                            if (iframe != undefined) {
                                iframe.repage();
                            }
                            parent.layer.close(index);
                            ajaxSuccess = 1;
                        }
                        else if (data && data.message) {
                            layerError(data.message, "错误提示");
                        }
                        else {
                            layerError("同步失败", "错误提示");
                        }
                        return false;
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText, null, "同步失败，请重试!");
                    }
                });

                return false;
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


    function closethisfancybox() {
        parent.layer.close(index);
    }
</script>
</body>
</html>