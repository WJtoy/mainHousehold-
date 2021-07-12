<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户配件信息</title>
    <meta name="decorator" content="default" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <script src="${ctxStatic}/js/ajaxfileupload.js"></script>
    <c:set var="currentuser" value="${fns:getUser()}"/>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center !important;
            vertical-align: middle;
            BackColor: Transparent;
            height: 30px;
        }
        .form-horizontal .controls {
            margin-left: 130px;
        }
        .form-horizontal .control-label {
            width: 130px;
        }
    </style>
    <script type="text/javascript">
        var orderdetail_index = parent.layer.getFrameIndex(window.name);
        var this_index = top.layer.index;
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }
        var clickTag = 0;
        $(document).ready(function() {
            $("#inputForm").validate({
                rules: {
                    price:{min:0},
                    recyclePrice:{min:1}
                },
                messages: {
                    price:{min:"参考价格应大于或等于0"},
                    recyclePrice:{min:"回收价格应大于0"}
                },
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

                    var materialId = $("#material\\.id").val();
                    if(materialId == null || materialId == ''){
                        clickTag = 0;
                        layerMsg("请选择配件");
                        return false;
                    }


                    var entity = {};
                    var customerId = $("#customer\\.id").val();
                    var productId = $("#product\\.id").val();
                    var id = $("#id").val();
                    var port = true;
                    $.ajax({
                        async: false,
                        url:"${ctx}/md/customerMaterialNew/ajax/checkCustomerMaterial?id="+ id + "&customerId="+customerId+"&productId="+productId+"&materialId="+ materialId,
                        success:function (e) {
                            if(e.success){

                            }else {
                                port = false;
                                clickTag = 0;
                                layerMsg('该配件已存在！');
                            }
                        },
                        error:function (e) {
                            clickTag = 0;
                            layerError("验证配件名失败","错误提示");
                        }
                    });
                    if(!port){
                        clickTag = 0;
                        return false;
                    }
                    entity["id"] = id;
                    entity['customerId'] = customerId;
                    entity['productId'] = productId;
                    entity['materialId'] = materialId;
                    entity['customerPartName'] = $("#customerPartName").val();
                    entity['customerPartCode'] = $("#customerPartCode").val();
                    entity['warrantyDay'] = $("#warrantyDay").val();
                    entity['customerProductModelId'] = $("#customerProductModelId").val();
                    entity['isReturn'] = $('[name=isReturn]:checked').val();
                    entity['recycleFlag'] = $('[name=recycleFlag]:checked').val();
                    entity['price'] = $('#price').val();
                    entity['recyclePrice'] = $('#recyclePrice').val();
                    entity['remarks'] = $('#remarks').val();
                    $btnSubmit.prop("disabled", true);
                        $.ajax({
                            url:"${ctx}/md/customerMaterialNew/saveNew",
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
                                    top.layer.close(this_index);//关闭本身
                                    layerMsg("配件信息已保存");
                                    var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                    <%--if(pframe!=undefined){--%>
                                    <%--    pframe.document.location="${ctx}/md/customerMaterialNew/listNew?customer.id="+customerId;--%>
                                    <%--}--%>
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
                    }else if(element.parent().is(".recyclePrice")){
                        var aspan = $(element.parent()).find("span");
                        error.insertAfter(aspan);
                    } else {
                        error.insertAfter(element);
                    }
                }

            });


            $(document).on('change',"#customer\\.id",function (e) {
                var customerId = $(this).val();
                if (customerId !='') {
                    $.ajax({
                        url: "${ctx}/md/customerMaterialNew/ajax/customerProductList",
                        data: {customerId: customerId},
                        success:function (e) {
                            if(e.success){
                                $("#product\\.id").empty();
                                var programme_sel=[];
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
                                for(var i=0,len = e.data.length;i<len;i++){
                                    var programme = e.data[i];
                                    programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                                }
                                $("#product\\.id").append(programme_sel.join(' '));
                                $("#product\\.id").val("");
                                $("#product\\.id").change();
                            }else {
                                $("#product\\.id").html('<option value="" selected>请选择</option>');
                                layerMsg('该客户还没有配置产品！');
                            }
                        },
                        error:function (e) {
                            layerError("请求产品失败","错误提示");
                        }
                    });
                    whichCustomers();
                }
            });
            if($("#customer\\.id").val()!=null && $("#customer\\.id").val()!=''){
                whichCustomers();
            }

            $("input[name='recycleFlag']").on("change",function(){
                editRecycleFlag();
            });
        });
        // 产品发生变更,获取客户产品型号
        $(document).on("change","#product\\.id",function (e) {
            changeProduct();
        });


        function checkPrice(obj){
            var reg = $(obj).val().match(/\d+\.?\d{0,2}/);
            var txt = '';
            if (reg != null) {
                txt = reg[0];
            }
            $(obj).val(txt);
        }
        function changeProduct() {
            var productId = $("#product\\.id").val();
            var customerId = $("#customer\\.id").val();
            $("#customerProductModelId").val("");
            $("#customerProductModelId").change();
            if (customerId !='' ) {
                if (productId != "") {
                    $.ajax({
                        url: "${ctx}/md/customerproductmodel/ajax/findListByCustomerAndProduct",
                        data: {customerId : customerId,productId: productId},
                        success:function (e) {
                            if(e.success){
                                $("#customerProductModelId").empty();
                                var programme_sel=[];
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
                                for(var i=0, len = e.data.length; i<len; i++){
                                    var programme = e.data[i];
                                    programme_sel.push('<option value="'+programme.id+'" data-id="'+programme.id+'">'+programme.customerProductName+'</option>')
                                }
                                $("#customerProductModelId").append(programme_sel.join(' '));
                                $("#customerProductModelId").val("");
                                $("#customerProductModelId").change();

                                var customerProductModelId = $("[name=customerProductModel]").val();

                                if(customerProductModelId != ''){
                                    $("#customerProductModelId").val(customerProductModelId);
                                    $("#customerProductModelId").change();
                                }
                            }else {
                                $("#customerProductModelId").html('<option value="" selected>请选择</option>');
                                //layerMsg('该客户还没有配置产品！');
                            }
                        },
                        error:function (e) {
                            layerError("请求客户产品型号失败","错误提示");
                        }
                    });
                }
            }else {
                layerMsg('请先选择客户！');
            }
        }

        function editRecycleFlag() {
            var vipFlag = $("input[name='recycleFlag']:checked").val();
            if(vipFlag == 1){
                $("#recyclePrice").removeAttr("disabled");
            }else {
                $("#recyclePrice").attr("disabled", true);
            }
        }

        function refreshMaterial(materialId,materialName) {
            $("#material\\.name").val(materialName);
            $("#material\\.id").val(materialId);

        }
        function checkboxOnchange(obj) {
            var value = $(obj).val();
            if($(obj).is(':checked')){
                if($("#trId_" + value).hasClass("warning")){
                    $("#trId_" + value).attr("class", "success");
                }
            }else{
                if($("#trId_" + value).hasClass("success")){
                    $("#trId_" + value).attr("class", "warning");
                }
            }
        }


        function showProductMaterial() {
            var customerId = $("#customer\\.id").val();
            var productId = $("#product\\.id").val();
            var id = $("#id").val();
            var productName = $("#product\\.id").find("option:selected").text();
            if(productName == ''){
                productName = $("#product\\.name").val();
            }
            if(productId != ''){
                var text = "关联配件";
                var url = "${ctx}/md/customerMaterialNew/showProductMaterial?product.id=" + productId + "&product.name=" + productName +"&customer.id="+ customerId +"&id=" + id +"&mark="+ 0 +"&parentIndex=" + (orderdetail_index || '');
                var area = ['936px', '560px'];
                top.layer.open({
                    type: 2,
                    id:"productMaterial",
                    zIndex:19,
                    title:text,
                    content: url,
                    area: area,
                    shade: 0.3,
                    maxmin: false,
                    success: function(layero,index){
                    },
                    end:function(){
                    }
                });
            }else {
                layerMsg('请选择产品！');
            }


        }
    </script>
    <style>
        .line {
            border-bottom: 3.5px solid #0096DA;
            width: 65px;
            border-radius: 10px;
        }

        .row-fluid {
            margin-top: 16px;
        }

        .buttons {
            display: flex;
            justify-content: flex-end;
            position: absolute;
            bottom: 0px;
            height: 50px;
            width: 100%;
            margin-left:-20px;
            border: 0;
            border-top: 1px solid #e5e5e5;
            padding-top: 18px;
        }

        .search_button {
            width: 64px;
            height: 30px;
            background-color: #0096DA;
            color: #fff;
            border-radius: 3px;
            border: 1px solid rgba(255, 255, 255, 0);
        }
    </style>
</head>

<body>

<form:form id="inputForm" modelAttribute="customerMaterial" action="${ctx}/md/customerMaterialNew/saveNew" method="post" class="form-horizontal">
    <sys:message content="${message}" />
    <form:hidden path="id"></form:hidden>
    <c:set var="customerId" value="${customerMaterial.customer.id}"></c:set>

    <div style="margin-top:24px; height: 160px;">
        <legend>产品配件
            <div class="line"></div>
        </legend>
        <div class="row-fluid">
            <div class="span6">
                <label class="control-label"><span class=" red">*</span>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
                <div class="controls">
                    <c:choose>
                        <c:when test="${currentuser.isCustomer() || customerMaterial.id != null}">
                            <form:hidden path="customer.id"/>
                            <form:input path="customer.name" readonly="true" style="width:237px;"/>
                        </c:when>
                        <c:otherwise>
                            <form:select path="customer.id" class="input-large" style="width:250px;">
                                <form:option value="" label="所有"/>
                                <form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false" />
                            </form:select>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="span6">
                <label class="control-label"><span class=" red">*</span>产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
                <div class="controls">
                    <c:choose>
                        <c:when test="${customerMaterial.product.id != null}">
                            <form:hidden path="product.id"/>
                            <form:input path="product.name" readonly="true" style="width:237px;"/>
                        </c:when>
                        <c:otherwise>
                            <form:select path="product.id" cssStyle="width: 250px;">
                                <form:option value="" label="请选择"></form:option>
                                <form:options items="${productList}" itemLabel="name" itemValue="id"></form:options>
                            </form:select>
                        </c:otherwise>
                    </c:choose>

                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span6" style="width: 385px;">
                <label class="control-label"><span class=" red">*</span>配&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：</label>
                <div class="controls">
                    <input id="material.id" name="material.id" type="hidden" value="${customerMaterial.material.id}">
                    <input id="material.name" name="material.name" style="width: 237px" class="required" disabled="disabled" type="text" value="${customerMaterial.material.name}" maxlength="20">
                </div>
            </div>
            <shiro:hasPermission name="md:customermaterial:edit">
            <button class="search_button" onclick="showProductMaterial()" type="button" style="float: left">
                <i class="icon-search"></i>&nbsp;选择
            </button>
            </shiro:hasPermission>
            <div class="span4">
            </div>
        </div>
    </div>

    <legend>客户配件
        <div class="line"></div>
    </legend>
    <div class="row-fluid">
        <div class="span6">
            <label class="control-label"><span id="spproduct" class=" red"></span>产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
            <div class="controls">
                <form:select path="customerProductModelId" cssStyle="width: 250px;">
                    <form:option value="" label="请选择"></form:option>
                </form:select>
                <input type="hidden" name="customerProductModel" value="${customerMaterial.customerProductModel.id}">
            </div>
        </div>
        <div class="span6">
            <label class="control-label"><span id="sppartName" class=" red"></span>配件名称：</label>
            <div class="controls">
                <form:input path="customerPartName" htmlEscape="false" maxlength="50"  cssStyle="width: 237px"/>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span6">
            <label class="control-label"><span id="sppartCode" class=" red"></span>配件编码：</label>
            <div class="controls">
                <form:input path="customerPartCode" htmlEscape="false" maxlength="20"  cssStyle="width: 237px"/>
            </div>
        </div>

        <div class="span6">

            <label class="control-label"><span class=" red">*</span>质保天数：</label>
            <div class="controls">
                <div class="input-append">
                    <input class="span2 required" id="warrantyDay" style="width: 225px;" type="number" maxlength="4" min="0" max="3650" value="${customerMaterial.warrantyDay}">
                    <span class="add-on">天</span>
                </div>
            </div>

        </div>

    </div>
    <div class="row-fluid">

        <div class="span6">
            <label class="control-label">参考价格：</label>
            <div class="controls">
            <div class="input-append">
                <input class="span2 required" id="price" name="price" style="width: 225px;" type="text" maxlength="4" min="0" max="5000" value="${customerMaterial.price}">
                <span class="add-on">元</span>
            </div>
            </div>
        </div>

        <div class="span6">
            <label class="control-label">返&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：</label>
            <div class="controls">
                <form:radiobuttons path="isReturn"
                                   items="${fns:getDictListFromMS('yes_no')}" itemLabel="label"
                                   itemValue="value" htmlEscape="false" class="required" cssStyle="margin-left: 5px"/>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span6">
            <label class="control-label">回收配件：</label>
            <div class="controls" style="margin-left: 0px;float: left;margin-top: 3px">
                <form:radiobuttons path="recycleFlag"
                                   items="${fns:getDictListFromMS('yes_no')}" itemLabel="label"
                                   itemValue="value" htmlEscape="false" class="required" cssStyle="margin-left: 5px"/>
            </div>
            <div class="controls recyclePrice" style="margin-left: 200px">
                <form:input path="recyclePrice" htmlEscape="false" maxlength="20" class="required number" min="1" placeholder="输入价格需大于0"
                            cssStyle="width: 132px;margin-left: 10px"/>
                <span class="add-on" style="margin-left: -5px;border-radius: 0 4px 4px 0;">元</span>
            </div>
            <span class="help-inline" style="margin-left: 55px;margin-top: 5px">注：旧件给师傅回收，收取回收费用</span>
        </div>

    </div>
    <div class="row-fluid" style="margin-top: 13px">
        <div class="span6">
            <label class="control-label">描&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;述：</label>
            <div class="controls">
            <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge"  cssStyle="width: 695px;"/>
            </div>
        </div>
        <div class="span2">
        </div>
    </div>

    <div class="buttons">
        <shiro:hasPermission name="md:customermaterial:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="width: 104px;height: 40px;margin-right: 10px;"/>
<%--            <input id="btnSubmit1" class="btn btn-primary" type="button" value="保存继续添加" style="width: 104px;height: 32px;margin-right: 25px;text-align: center"/>--%>
            <input id="btnSubmit1" class="btn " type="button" onclick="javascript:cancel();" value="取消" style="width: 104px;height: 40px;margin-right: 25px;"/>
        </shiro:hasPermission>
    </div>
</form:form>
<%--<form:form id="submitForm" ></form:form>--%>
</body>
<script type="text/javascript">
    if($("#product\\.id").val()!=null && $("#product\\.id").val()!=''){
        changeProduct();
    }
    editRecycleFlag();
    function whichCustomers() {
        var customerId = $("#customer\\.id").val();
        if(customerId == 5266){
            $("#inputForm").validate().element($("#customerPartCode"));
            $("#inputForm").validate().element($("#customerPartName"));
            $("#customerPartCode").rules("add","required");
            $("#customerPartName").rules("add","required");
            $("#spproduct").html("*");
            $("#sppartName").html("*");
            $("#sppartCode").html("*");
        } else {
            $("#inputForm").validate().element($("#customerPartCode"));
            $("#inputForm").validate().element($("#customerPartName"));
            $("#customerPartCode").rules("remove");
            $("#customerPartName").rules("remove");
            $("#spproduct").html("");
            $("#sppartName").html("");
            $("#sppartCode").html("");

        }
    }
</script>
</html>
