<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>配件</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treeview.jsp" %>
    <script type="text/javascript">
        var this_index = top.layer.index;
        var parent_index = parent.layer.getFrameIndex(window.name);
        var clickTag = 0;

        $(document).ready(function() {
            $("#inputForm").validate({
                rules: {
                    name: {remote: "${ctx}/md/material/checkMaterialName?id=" + '${material.id}'},
                    recyclePrice:{min:1}
                },
                messages: {
                    name: {remote: "配件名称已存在"},
                    recyclePrice:{min:"回收价格应大于0"}
                },
                submitHandler: function(form){
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
                        layerError("配件不能为空！", "错误提示");
                        return false;
                    }

                    var price = $("#price").val();
                    var isReturn = $(':checked[name=isReturn]').val();
                    var recycleFlag = $(':checked[name=recycleFlag]').val();
                    var remarks = $("#remarks").val();
                    var entity = {};
                    entity["id"] = $("#id").val();
                    entity["materialCategory.id"] = categoryId;
                    entity["name"] = name;
                    entity["price"] = price;
                    entity["isReturn"] = isReturn;
                    entity["recycleFlag"] = recycleFlag;
                    entity["recyclePrice"] = $("#recyclePrice").val();
                    entity["remarks"] = remarks;

                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    $btnSubmit.prop("disabled", true);
                    $.ajax({
                        url:"${ctx}/md/material/ajaxSave",
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
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                                top.layer.close(this_index);//关闭本身
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
                errorContainer: "#messageBox",
                errorPlacement: function(error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
                        error.appendTo(element.parent().parent());
                    }else if(element.parent().is(".recyclePrice")){
                        var aspan = $(element.parent()).find("span");
                        error.insertAfter(aspan);
                    } else if(element.parent().is(".price")){
                        var aspan = $(element.parent()).find("span");
                        error.insertAfter(aspan);
                    }else {
                        error.insertAfter(element);
                    }
                }
            });

            $("input[name='recycleFlag']").on("change",function(){
                editRecycleFlag();
            });
        });

        function editRecycleFlag() {
            var vipFlag = $("input[name='recycleFlag']:checked").val();
            if(vipFlag == 1){
                $("#recyclePrice").removeAttr("disabled");
            }else {
                $("#recyclePrice").attr("disabled", true);
            }
        }

        function changeCategory(obj){
            var value= $(obj).find("option:selected").text();
            $("#materialCategoryName").val(value);
        }

        function closeDialog() {
            var pframe = getActiveTabIframe();//定义在jeesite.min.js中
            if (pframe) {
                pframe.repage();
            }
            top.layer.close(this_index);   //关闭本身
        }

        function refresh() {
            //console.log("refresh.");
            // 重新加载配件分类
            $.ajax({
                url:"${ctx}/md/materialCategory/ajax/findAllList",
                success:function (e) {
                    if(e.success){
                        $("#materialCategory\\.id").empty();
                        var programme_sel=[];
                        programme_sel.push('<option value="" selected="selected">请选择</option>')
                        for(var i=0,len=e.data.length;i<len;i++){
                            var programme = e.data[i];
                            programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                        }
                        $("#materialCategory\\.id").append(programme_sel.join(' '));
                        $("#materialCategory\\.id").val("");
                        $("#materialCategory\\.id").change();
                    }else {
                        $("#materialCategory\\.id").html('<option value="" selected>请选择</option>');
                        layerMsg('没有配件分类！');
                    }
                },
                error:function (e) {
                    layerError("请求配件分类失败","错误提示");
                }
            });
        }

        function addMaterialCategory() {
            var text = "添加配件分类";
            var url = "${ctx}/md/materialCategory/newForm?parentIndex=" + (parent_index || '');
            top.layer.open({
                type: 2,
                id:"materialCategory",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['700px', '340px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }
    </script>
    <style type="text/css">
        .form-horizontal {margin-top: 50px;}
        .form-horizontal .control-group {margin-left: 35px;margin-bottom: 10px;}
        .form-horizontal .control-label {width: 80px;}
        .form-horizontal .controls {margin-left: 90px;}
    </style>
</head>

<body>
<sys:message content="${message}"/>
<form:form id="inputForm" modelAttribute="material" action="${ctx}/md/material/save" method="post" class="form-horizontal">
    <form:hidden path="id"/>
    <div class="control-group">
        <label class="control-label"><span style="color:red">*</span>配件分类:</label>
        <div class="controls">
            <select name="materialCategory.id" id="materialCategory.id" class="input-small required " style="width:238px;" onchange="changeCategory(this)">
                <option value=""
                        <c:out value="${(empty material.materialCategory.id)?'selected=selected':''}" />>请选择</option>
                <c:forEach items="${materialCategoryList}" var="materialCategory">
                    <option value="${materialCategory.id}"
                            <c:out value="${(material.materialCategory.id eq materialCategory.id)?'selected=selected':''}" />>${materialCategory.name}</option>
                </c:forEach>
            </select>
            <input type="hidden" id="materialCategoryName" name="materialCategory.name" value="${material.materialCategory.name}">
            <a href="javascript:void(0);" onclick="javascript:addMaterialCategory();" style="margin-left: 10px;">+添加配件分类</a>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label"><span style="color:red">*</span>配件名称:</label>
        <div class="controls">
            <input id="oldName" name="oldName" type="hidden" value="${material.name}">
            <form:input path="name" htmlEscape="false" minLength="2" maxlength="30" class="required" cssStyle="width: 225px;"/>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">参考价格:</label>
        <div class="controls price">
            <form:input path="price" htmlEscape="false" maxlength="100" class="required number" cssStyle="width: 200px;"/>
            <span class="add-on" style="margin-left: -5px;border-radius: 0 4px 4px 0;">元</span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">返&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件:</label>
        <div class="controls">
            <c:choose>
                <c:when test="${material.id>0 && material.isReturn==0}">
                    <form:radiobutton path="isReturn" value="1"  label="是"/>
                    <form:radiobutton path="isReturn" value="0" checked="checked" label="否" cssStyle="margin-left: 10px;"/>
                </c:when>
                <c:otherwise>
                    <form:radiobutton path="isReturn" value="1" checked="checked" label="是"/>
                    <form:radiobutton path="isReturn" value="0" label="否" cssStyle="margin-left: 10px;"/>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="control-group">
        <label class="control-label">回收配件:</label>
        <div class="controls recyclePrice">
            <c:choose>
                <c:when test="${material.id>0 && material.recycleFlag==0}">
                    <form:radiobutton path="recycleFlag" value="1"  label="是"/>
                    <form:radiobutton path="recycleFlag" value="0" checked="checked" label="否" cssStyle="margin-left: 10px;"/>
                </c:when>
                <c:otherwise>
                    <form:radiobutton path="recycleFlag" value="1" checked="checked" label="是"/>
                    <form:radiobutton path="recycleFlag" value="0" label="否" cssStyle="margin-left: 10px;"/>
                </c:otherwise>
            </c:choose>

            <form:input path="recyclePrice" htmlEscape="false" maxlength="20" class="required number" min="1"
                        cssStyle="width: 109px;margin-left: 10px" placeholder="输入价格需大于0"/>
            <span class="add-on" style="margin-left: -5px;border-radius: 0 4px 4px 0;">元</span>
            <span class="help-inline" style="">旧件给师傅回收，收取回收费用</span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">描&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;述:</label>
        <div class="controls">
            <form:textarea path="remarks" htmlEscape="false" rows="4" maxlength="200" class="input-xlarge" cssStyle="width: 470px"/>
        </div>
    </div>
    <%--
    <div class="form-actions">
        <shiro:hasPermission name="md:material:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="返 回" onclick=""/>
    </div>
    --%>
</form:form>
<shiro:hasPermission name="md:material:edit">
    <div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
        <hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
        <input id="btnSubmit" class="btn btn-primary" type="submit" style="margin-left: 460px;margin-top:12px;height: 40px;width: 104px" onclick="$('#inputForm').submit();" value="&nbsp;&nbsp;保 存&nbsp;&nbsp;"/>
        <input id="btnClose" class="btn" type="button" value="&nbsp;&nbsp;取消&nbsp;&nbsp;" style="margin-top:12px;height: 40px;width: 104px;margin-left: 10px" onclick="javascript:closeDialog();"/>
    </div>
</shiro:hasPermission>
<script type="text/javascript">
    $(".control-group").css("border-bottom-style", "none");
    editRecycleFlag();
</script>
</body>
</html>
