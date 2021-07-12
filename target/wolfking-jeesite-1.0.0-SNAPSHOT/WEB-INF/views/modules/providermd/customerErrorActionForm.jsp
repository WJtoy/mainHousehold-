<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>故障编辑页面</title>
    <meta name="decorator" content="default" />
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>

    <script type="text/javascript">
        var this_index = top.layer.index;
        $(document).ready(function() {
            $('a[data-toggle=tooltip]').darkTooltip();
            /*$(document).on('change',"#productId",function (e) {
                $("#name").rules("remove");
                var productId = $(this).val();
                if(productId==null || productId==''){
                    $("#contentTable tbody").empty();
                    return false;
                }
                //console.log(productId);
                setPage(0);
                var url = "${ctx}/provider/md/errorType/findList?productId="+productId;
                $("#searchForm").attr("action",url);
                $("#searchForm").submit();
                return false;
            });*/

            $("#btnSubmit").on("click",function(){
                var $btnSubmit = $("#btnSubmit");
                $btnSubmit.attr('disabled', 'disabled');

                var serviceTypeId = $("#serviceTypeId").val();
                if (serviceTypeId == null || serviceTypeId =='') {
                    layerAlert('请选择服务类型', '系统提示');
                    $btnSubmit.removeAttr("disabled");
                    return false;
                }

                var loadingIndex = layer.msg('正在提交，请稍等...', {
                    icon: 16,
                    time: 0,
                    shade: 0.3
                });
                console.log($("#analysis").val());

                var ajaxSuccess = 0;
                $.ajax({
                    cache: false,
                    type: "POST",
                    url: "${ctx}/provider/md/customerErrorAction/ajax/updateActionCodeNameAndAnalysis",
                    dataType: 'json',
                    data: { id : $("#actionCodeId").val(),
                            analysis: $("#analysis").val(),
                            name: $("#name").val(),
                            serviceTypeId: serviceTypeId
                    },
                    success: function (data) {
                        if (data.success) {
                            layer.close(loadingIndex);
                            top.layer.close(this_index);
                            //layerMsg(data.message);
                            var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                            if (iframe != undefined) {
                                iframe.repage();
                            }
                        }else {
                            layer.close(loadingIndex);
                            layerError(data.message,"错误提示");
                            $btnSubmit.removeAttr("disabled");
                        }
                    },
                    error: function (e) {
                        layer.close(loadingIndex);
                        layerError(data.message,"错误提示");
                        $btnSubmit.removeAttr("disabled");
                    }
                });
            });
        });
    </script>
    <style type="text/css">
        .form-horizontal .control-label {
            width: 80px;
            text-align: left;
        }
        .form-horizontal .controls{
            margin-left: 90px;
        }
    </style>
</head>
<body>
<form:form id="searchForm" modelAttribute="errorActionDto"  method="post" class="form-horizontal" cssStyle="margin-left: 80px;">
    <form:hidden path="actionCodeDto.id" id="actionCodeId"/>

    <div class="row-fluid">
        <div class="span6">
            <div class="control-group" style="padding-top:10px;">
                <label class="control-label">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
                <div class="controls">
                    <span>${errorActionDto.productName}</span>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group" style="padding-top:10px;">
                <label class="control-label" style="width: 100px">客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
                <div class="controls">
                    <span>${errorActionDto.customerName}</span>
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group" style="padding-top:10px;">
                <label class="control-label">故障分类：</label>
                <div class="controls">
                   <input type="text"  value="${errorActionDto.errorCodeDto.errorTypeName}" style="width:170px;" maxlength="50" disabled="true"/>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group" style="padding-top:10px;">
                <label class="control-label" style="width: 100px;">故障分类代码：</label>
                <div class="controls">
                        ${errorActionDto.errorCodeDto.errorTypeCode}
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span6">
            <div class="control-group" style="padding-top:10px;">
                <label class="control-label">故障现象：</label>
                <div class="controls">
                   <input type="text"  value="${errorActionDto.errorCodeDto.name}" style="width:170px;"  maxlength="50" disabled="true"/>
                </div>
            </div>
        </div>
        <div class="span6">
            <div class="control-group" style="padding-top:10px;">
                <label class="control-label" style="width: 100px;">故障现象代码：</label>
                <div class="controls">
                     ${errorActionDto.errorCodeDto.code}
                </div>
            </div>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">故障分析：</label>
        <div class="controls">
            <input id="analysis" name="actionCodeDto.analysis" type="text" htmlEscape="false" maxlength="50" style="width:350px" value="${errorActionDto.actionCodeDto.analysis}"/>
            <span class="red">*</span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">故障处理：</label>
        <div class="controls">
            <input id="name" name="actionCodeDto.name" type="text" htmlEscape="false" maxlength="50" style="width:350px" value="${errorActionDto.actionCodeDto.name}"/>
            <span class="red">*</span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">服务类型：</label>
        <div class="controls">
            <select id="serviceTypeId" name="actionCodeDto.serviceTypeId" class="input-small" style="width:250px;">
                <option value="" selected>请选择</option>
                <c:forEach items="${serviceTypeList}" var="dict">
                    <option value="${dict.id}" <c:out value="${(errorActionDto.actionCodeDto.serviceTypeId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                </c:forEach>
            </select>   &nbsp;
        </div>
    </div>
    <div class="control-group">
        <label class="control-label"></label>
        <div class="controls">
            <shiro:hasPermission name="md:customererroraction:edit">
                <input id="btnSubmit" class="btn btn-primary" type="submit" value="&nbsp;&nbsp;&nbsp;保存&nbsp;&nbsp;&nbsp;" />
            </shiro:hasPermission>
        </div>
    </div>
</form:form>

</body>
</html>


