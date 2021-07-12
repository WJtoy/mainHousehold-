<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户地址</title>
    <meta name="decorator" content="default" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var this_index = top.layer.index;
        // 关闭页面
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }

        var clickTag=0;

        $(document).ready(function(){
                    $("#inputForm").validate({
                        submitHandler: function(form){
                            var loadingIndex = layerLoading('正在提交，请稍候...');
                            var $btnSubmit = $("#btnSubmit");
                            if ($btnSubmit.prop("disabled") == true) {
                                event.preventDefault();
                                return false;
                            }
                            var areaId = $("#customerAddressId").val();
                            if (Utils.isEmpty(areaId)) {
                                layerError("请选择完整的省、市、区县","错误提示");
                                $("#btnSubmit").prop("disabled", false);
                                top.layer.close(loadingIndex);
                                return false;
                            }

                            $btnSubmit.prop("disabled", true);

                            $.ajax({
                                url:"${ctx}/md/customerNew/saveCustomerAddress",
                                type:"POST",
                                data:$(form).serialize(),
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
                                        if(parentIndex && parentIndex != undefined && parentIndex != ''){
                                            var layero = $("#layui-layer" + parentIndex,top.document);
                                            var iframeWin = top[layero.find('iframe')[0]['name']];
                                            var addressId;
                                            if(data.data != null){
                                                addressId = data.data;
                                            }else {
                                                addressId = $("#id").val();
                                            }
                                            iframeWin.refresh($("#userName").val(),$("#contactInfo").val(),$("#customerAddressId").val(),$("#customerAddressName").val(),$("#address").val(),addressId,$("#isDefault").val(),$("#editType").val());
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
                                error: function (data)
                                {
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
                                },
                                timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                            });
                        },
                        errorContainer: "#messageBox",
                        errorPlacement: function(error, element) {
                            $("#messageBox").text("输入有误，请先更正。");
                            if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
                                error.appendTo(element.parent().parent());
                            } else {
                                error.insertAfter(element);
                            }
                        }
                    });


                    $("#isDefault").on("change", function(){
                        var checked = $("input[type='checkbox'][name='isDefault']").is(':checked');
                        if (checked) {
                            $("#isDefault").attr("value", 1);
                        } else {
                            $("#isDefault").attr("value", 0);
                        }

                    })
            });

    </script>

</head>

<body>
    <form:form id="inputForm" modelAttribute="customerAddress" action="${ctx}/md/customerNew/customerAddressForms" method="post" class="form-horizontal">
    <sys:message content="${message}" />
        <form:hidden id="customerId" path="customerId" />
        <form:hidden id="id" path="id" />
<%--        <form:hidden id="isDefaultVar" path="isDefault" />--%>
        <input type="hidden" value="${customerAddress.isDefault}" id="isDefaultVar">
        <input type="hidden" id="editType" value="${editType}" name ="editType" />
        <div class="control-group" style="margin-top: 48px;margin-left: 20px;">
            <label class="control-label" style="width: 108px">地址类型：</label>
            <div class="controls" style="margin-left: 0px">
                <form:input path="addressTypeName" readonly="true" style="width: 236px;"></form:input>
                <form:hidden id="addressType" path="addressType" />
            </div>
        </div>

            <div class="control-group" style="margin-top: 16px;margin-left: 20px;width: 49%;float: left">
                <label class="control-label" style="width: 108px">联系人：</label>
                <div class="controls" style="margin-left: 105px">
                    <form:input id="userName" path="userName" htmlEscape="false" maxlength="20" cssClass="required" style="width: 236px;"/>
                </div>

            </div>
    <div class="control-group" style="margin-top: 19px;margin-left: 20px">
        <label class="control-label" style="width: 108px">联系电话：</label>
        <div class="controls" style="margin-left: 521px">
<%--            <form:input id="contactInfo" path="contactInfo" htmlEscape="false" maxlength="11"  class="required number" style="width: 236px;"/>--%>
            <input id="contactInfo" name="contactInfo" htmlEscape="false" type="text" value="${empty customerAddress.customerId? customerAddress.contactInfo:''}" maxlength="16" class="${empty customerAddress.customerId?'required number':''}" style="width: 236px;"/>
        </div>
    </div>

    <div class="row-fluid">
        <div class="span4" style="width: auto;max-width:378px">
            <div class="control-group">
                <label class="control-label" style="width: 128px">详细地址：</label>
                <div class="controls" style="width: auto;max-width:440px;margin-left: 128px" >
                    <sys:areaselect name="areaId" id="customerAddress" value="${customerAddress.areaId}"
                                    labelValue="${customerAddress.areaName}" labelName="areaName" title=""
                                    mustSelectCounty="true" cssClass="required" cssStyle="width: 194px;">
                    </sys:areaselect>
                </div>
            </div>
        </div>
        <div class="span7" style="margin-left:5px;width:40%">
            <div class="control-group">
                <div class="controls" style="padding-left:0px;margin-left:0px;display:inherit;">
                    <form:input id="address" path="address" htmlEscape="false" maxlength="100" style="width:397px;" cssClass="required" placeholder="详细地址，如XX大厦1层101室"/>
                </div>
            </div>

        </div>
    </div>
        <div class="control-group" style="margin-left: 20px">
            <div class="controls" style="margin-left: 105px">
                <input type="checkbox" name="isDefault" value="${customerAddress.isDefault}" id="isDefault">设置为默认的返件地址
            </div>
        </div>

    <hr style="=border: 1px solid rgba(238, 238, 238, 1);margin: 0px;margin-top: 135px"/>


            <input id="btnSubmit" class="btn btn-primary" type="submit" style="width: 92px;height: 40px;margin-left: 76%;margin-top: 10px"
                   value="保存" />

    <input id="btnCancel" class="btn" type="button" value="取消" style="width: 92px;height: 40px;margin-top: 10px;margin-left: 10px"
           onclick="cancel()" />
    </form:form>

<script>
    $(document).ready(function(){
        var isDefault = $("#isDefaultVar").val();
        if (isDefault == 1){
            $("input[type='checkbox']").attr("checked", true);
        } else {
            $("input[type='checkbox']").attr("checked", false);
        }
    });
</script>
</body>
</html>
