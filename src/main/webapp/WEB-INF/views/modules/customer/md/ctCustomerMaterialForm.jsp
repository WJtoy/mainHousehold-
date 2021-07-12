<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户产品型号</title>
    <meta name="decorator" content="default" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <c:set var="currentuser" value="${fns:getUser()}"/>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center !important;
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
    </style>
    <script type="text/javascript">

        var this_index = top.layer.index;
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }
        var clickTag = 0;
        $(document).ready(function() {
            if($("#id").val()!=null && $("#id").val()!=''){
                changeProduct();
            }
            $("th").css({"text-align":"left","vertical-align":"middle"});
            $("td").css({"text-align":"left","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});

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

                    var entity = {};
                    var customerId = $("#customer\\.id").val();
                    entity['customer.id'] = customerId;
                    entity['product.id'] = $("#product\\.id").val();
                    $("input[type='checkbox'][name='checkedRecords']:checkbox:checked").each(function(i,element){
                        var index = this.value;
                        var materialId = $("input[name=materialId_"+index +"]").val();
                        entity['itemList['+i+'].materialId'] = materialId;
                        var customerPartName = $("input[name=customerPartName_"+index +"]").val();
                        entity['itemList['+i+'].customerPartName'] = customerPartName;
                        var customerPartCode = $("input[name=customerPartCode_"+index +"]").val();
                        entity['itemList['+i+'].customerPartCode'] = customerPartCode;
                        var warrantyDay = $("input[name=warrantyDay_"+index +"]").val();
                        entity['itemList['+i+'].warrantyDay'] = warrantyDay;
                        var isReturn = $('input[name=isReturn_'+ index+ ']').is(":checked");
                        entity['itemList['+i+'].isReturn'] = isReturn?1:0;
                        var recycleFlag = $('input[name=recycleFlag_'+ index+ ']').is(":checked");
                        entity['itemList['+i+'].recycleFlag'] = recycleFlag?1:0;
                        var price = $('input[name=price_'+ index+ ']').val();
                        entity['itemList['+i+'].price'] = price;
                        var recyclePrice = $('input[name=recyclePrice_'+ index+ ']').val();
                        entity['itemList['+i+'].recyclePrice'] = recyclePrice;
                        var remarks = $('input[name=remarks_'+ index+ ']').val();
                        entity['itemList['+i+'].remarks'] = remarks;
                    });
                    var flag = true;
                    if($("#treeTable input[type='checkbox'][name='checkedRecords']:checkbox:checked").length == 0){
                        flag = false;
                        top.layer.close(loadingIndex);
                        var confirmIndex = layer.confirm('您没有选择配件确定要继续提交吗', {
                            btn: ['确定','取消'],//按钮
                            title:'提示',
                        }, function(){
                            layer.close(confirmIndex);
                            $.ajax({
                                url:"${ctx}/customer/md/customerMaterial/save",
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
                                        layerMsg("配件信息已保存");
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
                        },function(){
                            clickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                            layer.close(confirmIndex);
                            return false;
                        });
                    }
                    if(flag){
                        $.ajax({
                            url:"${ctx}/customer/md/customerMaterial/save",
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
                                    layerMsg("配件信息已保存");
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
                    }


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
                }

            });


            $(document).on('change',"#customer\\.id",function (e) {
                var customerId = $(this).val();
                if (customerId !='') {
                    $.ajax({
                        url: "${ctx}/customer/md/customerMaterial/ajax/customerProductList",
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

                    $.ajax({
                        url: "${ctx}/customer/md/customerMaterial/ajax/customerProductCategoryList",
                        data: {customerId: customerId},
                        success:function (e) {
                            if(e.success){
                                $("#productCategoryId").empty();
                                var programme_sel=[];
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
                                for(var i=0,len = e.data.length;i<len;i++){
                                    var programme = e.data[i];
                                    programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                                }
                                $("#productCategoryId").append(programme_sel.join(' '));
                                $("#productCategoryId").val("");
                                $("#productCategoryId").change();
                            }else {
                                $("#productCategoryId").html('<option value="" selected>请选择</option>');
                                layerMsg('该客户还没有配置产品品类！');
                            }
                        },
                        error:function (e) {
                            layerError("请求产品品类失败","错误提示");
                        }
                    });
                }
            });

            $("#selectAll").change(function() {
                var $check = $(this);
                $("input:checkbox[name='checkedRecords']").each(function(){
                    if ($(this).val() != "on"){
                        if ($check.prop("checked") == "checked" || $check.prop("checked") == true) {
                            $(this).prop("checked", true);
                        }
                        else{
                            $(this).prop("checked", false);
                        }
                    }
                });
            });
        });

        $(document).on("change","#productCategoryId",function (e) {
            var productCategoryId = $(this).val();
            var customerId = $("#customer\\.id").val();
            if (customerId !='') {
                if (productCategoryId != "") {
                    $.ajax({
                        url: "${ctx}/customer/md/customerMaterial/ajax/getProductCategoryProductList",
                        data: {customerId : customerId,productCategoryId: productCategoryId},
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
                }else {
                    $.ajax({
                        url: "${ctx}/customer/md/customerMaterial/ajax/customerProductList",
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
                }
            }else {
                layerMsg("请先选择客户");
            }
        });
        function checkPrice(obj){
            var reg = $(obj).val().match(/\d+\.?\d{0,2}/);
            var txt = '';
            if (reg != null) {
                txt = reg[0];
            }
            $(obj).val(txt);
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

        function changeProduct(){
            var customerId = $("#customer\\.id").val();
            var productId = $("#product\\.id").val();
            if(customerId ==null || customerId==''){
                layerMsg("请先选择客户")
                return false;
            }
            if(productId ==null || productId==''){
                return false;
            }
            $.ajax({
                    url:"${ctx}/customer/md/customerMaterial/ajax/getMaterialListByProductId?productId="+productId +"&customerId=" + customerId,
                    success:function (e) {
                        if(e.success == true){
                            var material_sel=[];
                            for(var i=0,len=e.data.materialList.length;i<len;i++){
                                var flag = 0;
                                var programme = e.data.materialList[i];
                                for(var j=0;j<e.data.customerMaterialList.length;j++){
                                    var customerMaterial = e.data.customerMaterialList[j];
                                    if(programme.id==customerMaterial.material.id){
                                        flag = 1;
                                        if(customerMaterial.isReturn==1){
                                            if(customerMaterial.recycleFlag == 1){
                                                material_sel.push('<tr class="success" id="trId_'+i+'"><td><input type="checkbox" name="checkedRecords" value="'+i+'" checked="checked" onchange="checkboxOnchange(this)"></td><td>'+programme.name+'<input type="hidden" name="materialId_'+i+'" value="'+programme.id+'"></td><td><input type="text" name="customerPartName_'+i+'" value="'+customerMaterial.customerPartName+'"></td><td><input type="text" name="customerPartCode_'+i+'" value="'+customerMaterial.customerPartCode+'"></td><td><input type="text" name="warrantyDay_'+i+'" style="width: 80px" value="'+customerMaterial.warrantyDay+'"></td><td><input type="checkbox" name ="isReturn_'+i+'" value="1" checked="checked" /></td><td><input type="text" name ="price_'+i+'" value="'+customerMaterial.price +'"  maxlength="6"  style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="checkbox" name ="recycleFlag_'+i+'" value="1" checked="checked" /></td><td><input type="text" name ="recyclePrice_'+i+'" value="'+customerMaterial.recyclePrice +'"  maxlength="6"  style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="text" name="remarks_'+i+'" value="'+customerMaterial.remarks+'"  maxlength="230"></td></tr>');
                                            }else {
                                                material_sel.push('<tr class="success" id="trId_'+i+'"><td><input type="checkbox" name="checkedRecords" value="'+i+'" checked="checked" onchange="checkboxOnchange(this)"></td><td>'+programme.name+'<input type="hidden" name="materialId_'+i+'" value="'+programme.id+'"></td><td><input type="text" name="customerPartName_'+i+'" value="'+customerMaterial.customerPartName+'"></td><td><input type="text" name="customerPartCode_'+i+'" value="'+customerMaterial.customerPartCode+'"></td><td><input type="text" name="warrantyDay_'+i+'" style="width: 80px" value="'+customerMaterial.warrantyDay+'"></td><td><input type="checkbox" name ="isReturn_'+i+'" value="1" checked="checked" /></td><td><input type="text" name ="price_'+i+'" value="'+customerMaterial.price +'"  maxlength="6"  style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="checkbox" name ="recycleFlag_'+i+'" value="0" /></td><td><input type="text" name ="recyclePrice_'+i+'" value="'+customerMaterial.recyclePrice +'"  maxlength="6"  style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="text" name="remarks_'+i+'" value="'+customerMaterial.remarks+'"  maxlength="230"></td></tr>');
                                            }
                                        }else{
                                            if(customerMaterial.recycleFlag == 1){
                                                material_sel.push('<tr class="success" id="trId_'+i+'"><td><input type="checkbox" name="checkedRecords" value="'+i+'" checked="checked" onchange="checkboxOnchange(this)"></td><td>'+programme.name+'<input type="hidden" name="materialId_'+i+'" value="'+programme.id+'"></td><td><input type="text" name="customerPartName_'+i+'" value="'+customerMaterial.customerPartName+'"></td><td><input type="text" name="customerPartCode_'+i+'" value="'+customerMaterial.customerPartCode+'"></td><td><input type="text" name="warrantyDay_'+i+'" style="width: 80px" value="'+customerMaterial.warrantyDay+'"></td><td><input type="checkbox" name ="isReturn_'+i+'" value="0" /></td><td><input type="text" name ="price_'+i+'" value="'+customerMaterial.price +'"  maxlength="6"  style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="checkbox" name ="recycleFlag_'+i+'" value="1" checked="checked"/></td><td><input type="text" name ="recyclePrice_'+i+'" value="'+customerMaterial.recyclePrice +'"  maxlength="6"  style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="text" name="remarks_'+i+'" value="'+customerMaterial.remarks+'"  maxlength="230"></td></tr>');
                                            }else {
                                                material_sel.push('<tr class="success" id="trId_'+i+'"><td><input type="checkbox" name="checkedRecords" value="'+i+'" checked="checked" onchange="checkboxOnchange(this)"></td><td>'+programme.name+'<input type="hidden" name="materialId_'+i+'" value="'+programme.id+'"></td><td><input type="text" name="customerPartName_'+i+'" value="'+customerMaterial.customerPartName+'"></td><td><input type="text" name="customerPartCode_'+i+'" value="'+customerMaterial.customerPartCode+'"></td><td><input type="text" name="warrantyDay_'+i+'" style="width: 80px" value="'+customerMaterial.warrantyDay+'"></td><td><input type="checkbox" name ="isReturn_'+i+'" value="0" /></td><td><input type="text" name ="price_'+i+'" value="'+customerMaterial.price +'"  maxlength="6"  style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="checkbox" name ="recycleFlag_'+i+'" value="0"/></td><td><input type="text" name ="recyclePrice_'+i+'" value="'+customerMaterial.recyclePrice +'"  maxlength="6"  style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="text" name="remarks_'+i+'" value="'+customerMaterial.remarks+'"  maxlength="230"></td></tr>');
                                            }
                                        }
                                    }
                                }
                                if(flag!=1){
                                    if(programme.isReturn==1){
                                        if(programme.recycleFlag == 1){
                                            material_sel.push('<tr id="trId_'+i+'"><td><input type="checkbox" id="checkbox_'+i+'" name="checkedRecords" value="'+i+'"></td><td>'+programme.name+'<input type="hidden" name="materialId_'+i+'" value="'+programme.id+'"></td><td><input type="text" name="customerPartName_'+i+'"></td><td><input type="text" name="customerPartCode_'+i+'"></td><td><input type="text" style="width: 80px" name="warrantyDay_'+i+'"></td><td><input type="checkbox" name ="isReturn_'+i+'" value="1" checked="checked" /></td><td><input type="text" name ="price_'+i+'" value="'+programme.price +'"  maxlength="6" style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="checkbox" name ="recycleFlag_'+i+'" value="1" checked="checked" /></td><td><input type="text" name ="recyclePrice_'+i+'" value="'+programme.recyclePrice +'"  maxlength="6"  style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="text" name="remarks_'+i+'"  maxlength="230"></td></tr>');
                                        }else {
                                            material_sel.push('<tr id="trId_'+i+'"><td><input type="checkbox" id="checkbox_'+i+'" name="checkedRecords" value="'+i+'"></td><td>'+programme.name+'<input type="hidden" name="materialId_'+i+'" value="'+programme.id+'"></td><td><input type="text" name="customerPartName_'+i+'"></td><td><input type="text" name="customerPartCode_'+i+'"></td><td><input type="text" style="width: 80px" name="warrantyDay_'+i+'"></td><td><input type="checkbox" name ="isReturn_'+i+'" value="1" checked="checked" /></td><td><input type="text" name ="price_'+i+'" value="'+programme.price +'"  maxlength="6" style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="checkbox" name ="recycleFlag_'+i+'" value="0"  /></td><td><input type="text" name ="recyclePrice_'+i+'" value="'+programme.recyclePrice +'"  maxlength="6"  style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="text" name="remarks_'+i+'"  maxlength="230"></td></tr>');
                                        }
                                    }else{
                                        if(programme.recycleFlag == 1){
                                            material_sel.push('<tr id="trId_'+i+'"><td><input type="checkbox" id="checkbox_'+i+'" name="checkedRecords" value="'+i+'"></td><td>'+programme.name+'<input type="hidden" name="materialId_'+i+'" value="'+programme.id+'"></td><td><input type="text" name="customerPartName_'+i+'"></td><td><input type="text" name="customerPartCode_'+i+'"></td><td><input type="text" style="width: 80px" name="warrantyDay_'+i+'"></td><td><input type="checkbox" name ="isReturn_'+i+'" value="0" /></td><td><input type="text" name ="price_'+i+'" value="'+programme.price +'"  maxlength="6" style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="checkbox" name ="recycleFlag_'+i+'" value="1" checked="checked" /></td><td><input type="text" name ="recyclePrice_'+i+'" value="'+programme.recyclePrice +'"  maxlength="6"  style="width: 80px" onkeyup="checkPrice(this)"><td><input type="text" name="remarks_'+i+'"  maxlength="230"></td></tr>');
                                        }else {
                                            material_sel.push('<tr id="trId_'+i+'"><td><input type="checkbox" id="checkbox_'+i+'" name="checkedRecords" value="'+i+'"></td><td>'+programme.name+'<input type="hidden" name="materialId_'+i+'" value="'+programme.id+'"></td><td><input type="text" name="customerPartName_'+i+'"></td><td><input type="text" name="customerPartCode_'+i+'"></td><td><input type="text" style="width: 80px" name="warrantyDay_'+i+'"></td><td><input type="checkbox" name ="isReturn_'+i+'" value="0" /></td><td><input type="text" name ="price_'+i+'" value="'+programme.price +'"  maxlength="6" style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="checkbox" name ="recycleFlag_'+i+'" value="0"  /></td><td><input type="text" name ="recyclePrice_'+i+'" value="'+programme.recyclePrice +'"  maxlength="6"  style="width: 80px" onkeyup="checkPrice(this)"></td><td><input type="text" name="remarks_'+i+'"  maxlength="230"></td></tr>');

                                        }
                                    }
                                }
                            }
                            $("#materialInfo").empty();
                            $("#materialInfo").append(material_sel.join(' '));
                        }else if(e.success == false){
                            layerAlert(e.message,"提示");
                        }
                    },
                    error:function (e) {
                        ajaxLogout(e.responseText,null,"请求产品配件失败","错误提示！");
                    }
                }
            );
        }
    </script>
</head>

<body>

<form:form id="inputForm" modelAttribute="customerMaterial" action="${ctx}/customer/md/customerMaterial/save" method="post" class="form-horizontal">
    <sys:message content="${message}" />
    <form:hidden path="id"></form:hidden>
    <c:set var="customerId" value="${customerMaterial.customer.id}"></c:set>
    <div class="control-group" style="margin-top: 22px">
        <div class="controls" style="margin-left: 5px">
            <c:choose>
                <c:when test="${customerId > 0}">

                    <label style="margin-left: 0px"><span class="red">*</span>客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户：</label>
                    <form:hidden path="customer.id"/>
                    <form:input path="customer.name" readonly="true" style="width:250px;"/>

                </c:when>
                <c:otherwise>

                    <label style="margin-left: 0px"><span class="red">*</span>客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户：</label>
                    <form:select path="customer.id" class="input-large" style="width:250px;">
                        <form:option value="" label="所有"/>
                        <form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false" />
                    </form:select>

                </c:otherwise>
            </c:choose>


            <c:if test="${customerMaterial.id == null}">
                &nbsp;
                <label style="margin-left: 24px">产品品类：</label>
                <form:select path="productCategoryId" cssStyle="width: 250px;">
                    <form:option value="" label="请选择"></form:option>
                    <form:options items="${productCategoryList}" itemLabel="name" itemValue="id"></form:options>
                </form:select>
            </c:if>

            <label style="margin-left: 24px"><span class="red">*</span>产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
            <form:select path="product.id" cssStyle="width: 250px;" onchange="changeProduct()">
                <form:option value="" label="请选择"></form:option>
                <form:options items="${productList}" itemLabel="name" itemValue="id"></form:options>
            </form:select>
            <div style="margin-top:10px;color: #808695">
                注：回收配件：旧件给师傅回收，收取回收费用，回收价格需大于0。
            </div>
        </div>
    </div>
    <div class="control-group">
        <div class="controls" style="margin-left: 5px">
            <table id="treeTable"
                   class="table table-striped table-bordered table-condensed">
                <thead>
                <tr>
                    <th width="69px"><input type="checkbox" id="selectAll" name="selectAll"/></th>
                    <th width="200px">配件</th>
                    <th width="200px">客户配件名称</th>
                    <th width="200px">客户配件编码</th>
                    <th width="80px">质保天数(天)</th>
                    <th width="80px">返件</th>
                    <th width="80px">参考价格(元)</th>
                    <th width="80px">回收配件</th>
                    <th width="80px">回收价格(元)</th>
                    <th>描述</th>
                </tr>
                </thead>
                <tbody id="materialInfo">

                </tbody>
            </table>
        </div>
    </div>
    <div id="editBtn" class="line-row">
        <shiro:hasPermission name="customer:md:customermaterial:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="width: 104px;height: 40px;margin-left: 83%;margin-top: 10px;margin-bottom: 10px"/>
        </shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="width: 104px;height: 40px;margin-top: 10px;margin-left: 13px;margin-bottom: 10px"/>
    </div>
</form:form>
<form:form id="submitForm" ></form:form>
</body>
</html>
