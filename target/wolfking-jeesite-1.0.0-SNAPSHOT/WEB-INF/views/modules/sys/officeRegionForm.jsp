<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>添加部门客服</title>
    <meta name="decorator" content="default"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
            height: 30px;
        }
        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 0;
            width: 100%;
            height: 60px;
            background: #fff;
            z-index: 10;
            border-top: 1px solid #e5e5e5;
        }
        .area {
            width: 107px;
            height: 40px;
            float: left;
            /*text-overflow: ellipsis;*/
            overflow: hidden;
            white-space: nowrap;
        }
    </style>

    <script type="text/javascript">
        var this_index = top.layer.index;
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }

        var clickTag = 0;
        $(document).ready(function() {

            $("#inputForm").validate({
                submitHandler: function(form){
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");

                    if(clickTag == 1){
                        return false;
                    }
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }

                    clickTag = 1;
                    $btnSubmit.prop("disabled", true);

                    var keFuId = $("#keFuId").val();
                    var areaIds = [];
                    if(keFuId == ''){
                        layerError("请选择客服", "错误提示");
                        return false;
                    }
                    $("input[type='checkbox'][name='unauthorizedArea']:checkbox:checked").each(function(i,element){
                        var id = $(this).val();
                        areaIds.push(id);
                    });
                    var areaRegion = areaIds.join(",");
                    var cityId = $("#cityId").val();
                    var provinceId = $("#provinceId").val();
                    $.ajax({
                        url:"${ctx}/sys/officeRegion/save",
                        type:"POST",
                        data:{keFuId:keFuId,provinceId:provinceId,cityId:cityId,areaRegion:areaRegion},
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
                                top.layer.close(this_index);//关闭本身
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                top.layer.close(loadingIndex);
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            top.layer.close(loadingIndex);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            //var msg = eval(data);
                            top.layer.close(loadingIndex);
                        },
                        timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                    });
                },
                errorContainer : "#messageBox",
                errorPlacement : function(error, element)
                {
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
    </script>
</head>
<body>
<form:form id="inputForm" method="post" action="${ctx}/sys/officeRegion/save" class="form-horizontal">
    <sys:message content="${message}"/>
    <input type="hidden" id="cityId" value="${cityId}">
    <input type="hidden" id="provinceId" value="${provinceId}">
    <input type="hidden" id="officeId" value="${officeId}">
    <div class="control-group" style="margin-top: 62px">
        <label style="float: left;margin-left: 115px;margin-top: 5px">部&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp门：</label>
        <div class="controls" style="margin-left: 190px">
            <input id="officeName" style="width:237px;" readonly="readonly" type="text" value="${officeName}" class="valid" aria-invalid="false">

            <label style="margin-left: 100px;margin-top: 5px">产品品类：</label>
            <input id="productCategoryName" style="width:237px;" readonly="readonly" type="text" value="${productCategoryName}" class="valid" aria-invalid="false">
        </div>

    </div>


    <div class="control-group" style="margin-top: 15px">
        <label style="float: left;margin-left: 115px;margin-top: 5px">区&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp域：</label>
        <label style="margin-left: 7px;margin-top: 5px;font-weight: bold;width: 150px"> ${areaName}</label>
        <div id="userRegion" class="controls" style="margin-left: 190px;float: left;background-color:#F8F8F9;width: 680px;margin-top: 10px;overflow-y:auto;overflow-x:hidden;">
            <div style="width: 685px;float: left">
                <label style="margin: 10px 0px 10px 10px;color:#515A6E;float: left">已添加客服：</label>
                <div style="width: 585px;float: left">
                <c:forEach items="${beGrantedArea}" var="beGranted">
                    <div class="area">
                        <input id="${beGranted.id}" disabled="disabled" type="checkbox" name="beGrantedArea" checked="true" value="${beGranted.id}" style="zoom: 1.4;margin-left: 7px">
                        <label style="margin-top: 12px">${beGranted.name}</label>
                    </div>
                </c:forEach>
                </div>
            </div>
            <div style="width: 685px">
                <label style="margin: 10px 0px 10px 10px;color:#515A6E;float: left">未添加客服：</label>
                <div style="width: 585px;float: left">
                    <c:forEach items="${unauthorizedArea}" var="unauthorized">
                        <div class="area">
                            <input id="${unauthorized.id}" type="checkbox" name="unauthorizedArea"  value="${unauthorized.id}" style="zoom: 1.4;margin-left: 7px">
                            <label style="margin-top: 12px">${unauthorized.name}</label>
                        </div>
                    </c:forEach>
                </div>

            </div>
        </div>
    </div>
    <div class="control-group">
        <label style="float: left;margin-left: 115px;margin-top: 5px">客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp服：</label>
        <div class="controls" style="margin-left: 100px;">
            <select id="keFuId" name="keFuId" class="input-small required selectCustomer" style="width:250px;">
                <option value="">请选择</option>
                <c:forEach items="${userKeFuList}" var="kefu">
                    <option value="${kefu.id}">${kefu.name}</option>
                </c:forEach>
            </select>
        </div>

    </div>
    <div id="editBtn" class="line-row">
        <shiro:hasPermission name="sys:officeRegion:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="width: 96px;height: 40px;margin-left: 77%;margin-top: 10px;margin-bottom: 10px"/>
            &nbsp;</shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="width: 96px;height: 40px;margin-top: 10px;margin-left: 13px;margin-bottom: 10px"/>
    </div>
</form:form>

<script type="text/javascript">

    $(document).ready(function() {
        var userRegionHight = document.getElementById("userRegion").offsetHeight;
        if(userRegionHight > 215){
            document.getElementById("userRegion").style.height= "220px";
        }
    });

</script>
</body>
</html>
