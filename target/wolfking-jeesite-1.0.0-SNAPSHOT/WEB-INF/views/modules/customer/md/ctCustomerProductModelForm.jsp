<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户产品型号</title>
    <meta about="客户产品型号(微服务md)" />
    <meta name="decorator" content="default" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="${ctxStatic}/jquery-ztree/3.5.12/css/zTreeStyle/zTreeStyle.min.css" type="text/css">
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <script type="text/javascript" src="${ctxStatic}/jquery-ztree/3.5.12/js/jquery.ztree.all-3.5.min.js"></script>
    <c:set var="currentuser" value="${fns:getUser()}"/>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#inputForm").validate({
                submitHandler : function(form)
                {
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);
                    //form.submit();
                    var options = {
                        url: "${ctx}/customer/md/customerProductModel/save",
                        type: 'post',
                        dataType: 'json',
                        data:$(form).serialize(),
                        beforeSubmit: function(formData, jqForm, options){
                            loadingIndex = layer.msg('正在提交，请稍等...', {
                                icon: 16,
                                time: 0,
                                shade: 0.3
                            });
                            return true;
                        },// 提交前的回调函数
                        success:function (data) {
                            // 提交后的回调函数
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg(data.message);
                                setTimeout(function () {
                                    cancel();
                                }, 1000);
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                            } else {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError("数据保存错误:" + data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                        },
                    };
                    $("#inputForm").ajaxSubmit(options);
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

            $(document).on('change','.selectCustomer',function(e){
                var customerId =$(this).val();
                getProductListByCustomerId(customerId);
            });

            $(document).on('change',"#productId",function (e) {
                var customerId = $("#customerId").val();
                var productId = $(this).val();
                if(customerId ==null || customerId == '' ){
                    layerError("请选择客户");
                    return false;
                }
                if(productId ==null || productId==''){
                    return false;
                }
                $.ajax({
                    url:"${ctx}/customer/md/customerBrandCategory/ajax/getListByCustomerId?customerId="+customerId + "&productId=" + productId,
                    success:function (e) {
                        if(e.success){
                            $("#brandId").empty();
                            var programme_sel=[];
                            programme_sel.push('<option value="" selected="selected">请选择</option>')
                            for(var i=0,len=e.data.length;i<len;i++){
                                var programme = e.data[i];
                                programme_sel.push('<option value="'+programme.brandId+'">'+programme.brandName+'</option>')
                            }
                            $("#brandId").append(programme_sel.join(' '));
                            $("#brandId").val("");
                            $("#brandId").change();
                        }else {
                            $("#brandId").html('<option value="" selected>请选择</option>');
                            layerMsg('该客户还没有关联品牌！');
                        }
                    },
                    error:function (e) {
                        layerError("请求客户品牌失败","错误提示");
                    }
                });
            });
            //用户是客户并且是添加
            if(${currentuser.isCustomer()==true && (customerProductModel.id ==null || customerProductModel.id<=0)}){
                var customerId =$("#customerId").val();
                getProductListByCustomerId(customerId);
            }

            if(${(customerProductModel.id!=null && customerProductModel.id>0) && (customerProductModel.brandId ==null || customerProductModel.brandId<=0)}){
                var customerId =$("#customerId").val();
                var productId = $("#productId").val();
                if (customerId == null || customerId == '')
                {
                    return false;
                }
                $.ajax({
                        url:"${ctx}/customer/md/customerProductModel/ajax/customerProductList?customerId="+customerId,
                        success:function (e) {
                            if(e.success){
                                $("#productId").empty();
                                var programme_sel=[];
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
                                for(var i=0,len=e.data.length;i<len;i++){
                                    var programme = e.data[i];
                                    if(programme.id == ${customerProductModel.productId}){
                                        programme_sel.push('<option value="'+programme.id+'" selected="selected">'+programme.name+'</option>')
                                    }else{
                                        programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                                    }
                                }
                                //salert(programme_sel)
                                $("#productId").append(programme_sel.join(' '));
                                $("#productId").change();
                            }else {
                                $("#productId").html('<option value="" selected>请选择</option>');
                                layerMsg('该客户还没有关联产品！');
                            }
                        },
                        error:function (e) {
                            layerError("请求客户产品失败","错误提示");
                        }
                    }
                );
                if(productId == null || productId == ''){
                    return false;
                }
                $.ajax({
                    url:"${ctx}/customer/md/customerBrandCategory/ajax/getListByCustomerId?customerId="+customerId + "&productId=" + productId,
                    success:function (e) {
                        if(e && e.success == true){
                            $("#brandId").empty();
                            var programme_sel=[];
                            programme_sel.push('<option value="" selected="selected">请选择</option>')
                            for(var i=0,len=e.data.length;i<len;i++){
                                var programme = e.data[i];
                                programme_sel.push('<option value="'+programme.brandId+'">'+programme.brandName+'</option>')
                            }
                            $("#brandId").append(programme_sel.join(' '));
                            $("#brandId").val("");
                            $("#brandId").change();
                        }else if(e.success == false){
                            $("#brandId").html('<option value="" selected>请选择</option>');
                            //layerMsg('该客户还没有关联品牌！');
                            layerError(e.message,"错误提示");
                        }
                    },
                    error:function (e) {
                        layerError("请求客户品牌失败","错误提示");
                    }
                });
            }

            $("th").css({"text-align":"left","vertical-align":"middle"});
            $("td").css({"text-align":"left","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
        });

        //根据客户ID获取产品
        function  getProductListByCustomerId(customerId) {
            if(customerId != null && customerId!=''){
                $.ajax({
                        url:"${ctx}/customer/md/customerProductModel/ajax/customerProductList?customerId="+customerId,
                        success:function (e) {
                            if(e.success){
                                $("#productId").empty();
                                var programme_sel=[];
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
                                for(var i=0,len=e.data.length;i<len;i++){
                                    var programme = e.data[i];
                                    programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                                }
                                $("#productId").append(programme_sel.join(' '));
                                $("#productId").val("");
                                $("#productId").change();
                            }else {
                                $("#productId").html('<option value="" selected>请选择</option>');
                                layerMsg('该客户还没有关联商品！');
                            }
                        },
                        error:function (e) {
                            layerError("请求客户产品失败","错误提示");
                        }
                    }
                );
            }
        }

    </script>
    <style type="text/css">
        .form-horizontal .control-label{
            width: 180px;
        }
        .form-horizontal .controls{
            margin-left: 200px;
        }
        .form-horizontal .control-group{
            margin-bottom: 15px;
        }
        .fromInput {
            border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
        }
        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 3px;
            width: 100%;
            height: 55px;
            background: #fff;
            z-index: 10;
            border-top: 1px solid #ccc;
            border-top: 1px solid #e5e5e5;
            text-align: right;
        }
        #main{
            margin-top: 45px;
            margin-left: -45px;
        }
        .tips{
            color: #808695;
            margin-left: 8px;
        }
    </style>
</head>

<body>
<form:form id="inputForm" modelAttribute="customerProductModel"  method="post" class="form-horizontal">
    <sys:message content="${message}" />
    <c:if test="${canAction == true}">
        <form:hidden path="id"></form:hidden>
        <c:choose>
            <c:when test="${currentuser.isCustomer()==true}">
                <div class="control-group">
                    <form:hidden path="customerId"></form:hidden>
                </div>
            </c:when>
            <c:otherwise>
                <div class="control-group">
                    <label class="control-label"><span class="red">*</span>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户:</label>
                    <div class="controls">
                        <c:choose>
                            <c:when test="${customerProductModel.customerId > 0}">
                                <form:hidden path="customerId"></form:hidden>
                                <form:input path="customerName" readonly="true"></form:input>
                            </c:when>
                            <c:otherwise>
                                <select id="customerId" name="customerId" class="input-small required selectCustomer" style="width:225px;">
                                    <option value=""
                                            <c:out value="${(empty customerProductModel.customerId)?'selected=selected':''}" />>请选择</option>
                                    <c:forEach items="${fns:getMyCustomerList()}" var="customer">
                                        <option value="${customer.id}"
                                                <c:out value="${(customerProductModel.customerId eq customer.id)?'selected=selected':''}" />>${customer.name}</option>
                                    </c:forEach>
                                </select>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

        <div class="control-group">
            <label class="control-label"><span class="red">*</span>产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品:</label>
            <div class="controls">
                <c:choose>
                    <c:when test="${customerProductModel.customerId > 0 && customerProductModel.brandId > 0}">
                        <form:hidden path="productId"></form:hidden>
                        <form:input path="productName" readonly="true"></form:input>
                        <span class="add-on red">*</span>
                    </c:when>
                    <c:otherwise>
                        <select id="productId" name ="productId" style="width:225px;" class="required">
                            <option value="" selected>请选择</option>
                        </select>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label"><span class="red">*</span>品&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;牌:</label>
            <div class="controls">
                <c:choose>
                    <c:when test="${customerProductModel.brandId > 0}">
                        <form:hidden path="brandId"></form:hidden>
                        <form:input path="brandName" readonly="true"></form:input>
                        <span class="add-on red">*</span>
                    </c:when>
                    <c:otherwise>
                        <select id="brandId" name ="brandId" style="width:225px;" class="required">
                            <option value="" selected>请选择</option>
                        </select>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div id="productDiv">
            <div class="row-fluid">
                <div class="span12">
                    <div class="control-group">
                        <label class="control-label"><span class="red">*</span>型&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;号:</label>
                        <div class="controls">
                            <input class="fromInput required" id="customerModel" name="customerModel" value="${customerProductModel.customerModel}" htmlEscape="false" maxlength="30" style="width: 236px;"/>
                            <span class="tips">客户产品型号</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div id="productDiv">
            <div class="row-fluid">
                <div class="span12">
                    <div class="control-group">
                        <label class="control-label">型号ID:</label>
                        <div class="controls">
                            <input class="fromInput" name="customerModelId" value="${customerProductModel.customerModelId}" htmlEscape="false" maxlength="30" style="width: 236px;"/>
                            <span class="tips">客户产品型号ID</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div id="productDiv">
            <div class="row-fluid">
                <div class="span12">
                    <div class="control-group">
                        <label class="control-label">名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称:</label>
                        <div class="controls">
                            <input class="fromInput" name="customerProductName" value="${customerProductModel.customerProductName}" htmlEscape="false" maxlength="30" style="width: 236px"/>
                            <span class="tips">客户产品名称</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">描&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;述:</label>
            <div class="controls">
                <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge" cssStyle="min-width: 280px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
            </div>
        </div>
    </c:if>
    <div id="formActions" class="form-actions">
        <c:if test="${canAction == true}">
            <shiro:hasPermission name="customer:md:customerproductmodel:edit">
                <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"
                       style="margin-top: 10px;width: 85px;height: 35px;" onclick="$('#inputForm').submit()"/>&nbsp;
           &nbsp;</shiro:hasPermission>
        </c:if>
        <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()"
               style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
    </div>
</form:form>
</body>
<script class="removedscript" type="text/javascript">
    function cancel() {
        var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
        parent.layer.close(index);
    }
</script>
</html>
