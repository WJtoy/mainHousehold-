<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>配件分类</title>
    <meta name="decorator" content="default" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var this_index = top.layer.index

        $(document).ready(function() {
            $("#inputForm").validate({
                rules: {
                    name: {remote: "${ctx}/md/materialCategory/checkMaterialCategoryName?id=" + '${materialCategory.id}'}
                },
                messages: {
                    name: {remote: "配件分类名称已存在"}
                },
                submitHandler : function(form) {
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        return false;
                    }
                    var categoryId = $("#materialCategory\\.id").val();
                    if (categoryId == "") {
                        layerError("请选择配件分类！", "错误提示");
                        return false;
                    }
                    var name = $("#name").val();
                    if (name == "") {
                        layerError("配件分类不能为空！", "错误提示");
                        return false;
                    }

                    var remarks = $("#remarks").val();
                    var entity = {};
                    entity["id"] = $("#id").val();
                    entity["name"] = name;
                    entity["remarks"] = remarks;

                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    $btnSubmit.prop("disabled", true);
                    $.ajax({
                        url:"${ctx}/md/materialCategory/ajaxSave",
                        type:"POST",
                        data: entity,
                        dataType:"json",
                        success: function(data){
                            //提交后的回调函数
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg("保存成功");
                                setTimeout(function () {
                                    // cancel();
                                    var index = parent.layer.getFrameIndex(window.name);
                                    parent.layer.close(index);  //关闭当前页
                                }, 2000);
                                // 回调父窗口方法
                                if(parentIndex && parentIndex != undefined && parentIndex != ''){
                                    var layero = $("#layui-layer" + parentIndex,top.document);
                                    var iframeWin = top[layero.find('iframe')[0]['name']];
                                    iframeWin.refresh();
                                } else  {
                                    var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                    if(pframe){
                                        pframe.repage();
                                    }
                                    top.layer.close(this_index);//关闭本身
                                }
                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data)
                        {
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            //var msg = eval(data);
                        },
                        timeout: 30000               //限制请求的时间，当请求大于30秒后，跳出请求
                    });
                },
                errorContainer : "#messageBox",
                errorPlacement : function(error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox")
                        || element.is(":radio")
                        || element.parent().is(
                            ".input-append"))
                    {
                        error.appendTo(element.parent()
                            .parent());
                    } else
                    {
                        error.insertAfter(element);
                    }
                }});
        });

        function closeDialog() {
            top.layer.close(this_index);   //关闭本身
        }

    </script>
    <style type="text/css">
        .form-horizontal {margin-top: 35px;}
        .form-horizontal .control-label {  width: 80px;}
        .form-horizontal .controls { margin-left: 90px; }
        .form-horizontal .control-group {
            margin-left: 35px;
            margin-bottom: 10px;
        }
        .fromInput {
            border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
        }
    </style>
</head>

<body>
<form:form id="inputForm" modelAttribute="materialCategory" action="${ctx}/md/materialCategory/save" method="post" class="form-horizontal">
    <sys:message content="${message}" />
    <form:hidden path="id"></form:hidden>
    <div class="control-group">
        <label class="control-label">分类名称:</label>
        <div class="controls">
            <input class="fromInput required"  id="name" name="name" placeholder="" value="${materialCategory.name}" htmlEscape="false" maxlength="30" cssStyle="width: 225px;"/>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">描&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;述:</label>
        <div class="controls">
            <form:textarea path="remarks" htmlEscape="false" rows="4" maxlength="200" class="input-xlarge" cssStyle="width: 470px"/>
        </div>
    </div>
</form:form>
<shiro:hasPermission name="md:material:edit">
    <div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
        <hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
        <input id="btnSubmit" class="btn btn-primary" type="submit" style="margin-left: 530px;margin-top:12px" onclick="$('#inputForm').submit();" value="&nbsp;&nbsp;保 存&nbsp;&nbsp;"/>
        <input id="btnClose" class="btn" type="button" value="&nbsp;&nbsp;取消&nbsp;&nbsp;" style="margin-top:12px" onclick="javascript:closeDialog();"/>
    </div>
</shiro:hasPermission>
<script type="text/javascript">
    $(".control-group").css("border-bottom-style","none");
</script>
</body>
</html>
