<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>关联产品</title>
    <meta name="decorator" content="default" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <%@include file="/WEB-INF/views/include/treeview.jsp" %>
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
            padding-left: 190px;
            border-top: 1px solid #e5e5e5;
        }
        .product{
            zoom: 1.2;
        }
        .span{
            width: 240px;
            font-size: 14px;
            margin: 10px 0px 10px 10px;
            overflow: hidden;
            text-overflow: ellipsis;
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
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }

                    var customerProductTypeId = $("#customerProductTypeId").val();

                    $btnSubmit.prop("disabled", true);
                    var loadingIndex = layerLoading('正在提交，请稍候...');

                    var productIds = [];
                    $("input[type='checkbox'][name='productName']:checkbox:checked").each(function(i,element){
                        var index = this.value;
                        productIds.push(index);
                    });
                    productIds = productIds.join(',');
                    var customerId = $("#customerId").val();
                    $.ajax({
                        url:"${ctx}/md/customerProductType/ajax/updateCustomerProductMapping",
                        type:"POST",
                        data: {customerProductTypeId:customerProductTypeId,productIds:productIds},
                        async: false,
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
                    } else {
                        error.insertAfter(element);
                    }
                }
            });

        });

        $(document).on("change","#productId",function (e) {
            checkCustomMaterial();
        });


        function checkCustomMaterial() {
            var num = [];
            $("input[type='checkbox'][name='productName']:checkbox:checked").each(function(i,element){
                var index = this.value;
                num.push(index);
            });
            $("#productNum").html("已选" + num.length +"个产品");

        }

        function materialCategory(productCategoryId) {
            var customerId = ${customerId};
            var customerProductTypeId = $("#customerProductTypeId").val();

            $("#content").html("");
            $.ajax({
                url: "${ctx}/md/customerProductType/ajax/getCustomerProduct",
                data: {customerId:customerId,productCategoryId: productCategoryId,customerProductTypeId:customerProductTypeId},
                success:function (e) {
                    if(e.success){
                        var programme_sel=[];
                        for(var i=0,len = e.data.productList.length;i<len;i++){
                            var flag = 0;
                            var programme = e.data.productList[i];
                            for(var j=0;j<e.data.customerProducts.length;j++){
                                var customerProduct = e.data.customerProducts[j];
                                if(programme.id==customerProduct){
                                    flag = 1;
                                    programme_sel.push('<span id="spProductId" class="span" title="'+programme.name+'"><input type="checkbox" class="product" checked="checked" value="'+programme.id+'" name="productName" id="productId" >'+programme.name+'</input></span>')
                                }
                            }

                            for(var k=0;k<e.data.customerProductOutList.length;k++){
                                var customerProductOut = e.data.customerProductOutList[k];
                                if(programme.id==customerProductOut.productId){
                                    flag = 1;
                                    programme_sel.push('<span id="spProductId" class="span" title="'+programme.name+'"><label><input type="checkbox" class="product" disabled="true" value="'+programme.id+'" name="productName" id="productId" />'+programme.name+'</label></span>')
                                }
                            }
                            if(flag != 1){
                                programme_sel.push('<span id="spProductId" class="span" title="'+programme.name+'"><label><input type="checkbox" class="product" value="'+programme.id+'" name="productName" id="productId" />'+programme.name+'</label></span>')
                            }

                        }
                        $("#content").append(programme_sel);
                    }
                },
                error:function (e) {
                    layerError("请求失败","错误提示");
                }
            });
        }
        function closeDialog() {
            top.layer.close(this_index);   //关闭本身
        }

    </script>
    <style>
        *{ margin:0; padding:0;list-style: none;}
        body {font:12px/1.5 Tahoma;}
        #outer {width:150px;float: left;}
        #tab {width:150px;float:left;overflow:hidden;zoom:1;background-color:#F8F8F9;}
        #tab li {cursor:pointer;width:150px;font-size: 14px;line-height: 35px}
        #tab li.current {color:black;background:#fff;}
        #content {width:763px;float:left;}
        #content div {line-height:25px;display:none;margin:0 30px;}
    </style>

</head>

<body>

<form:form id="inputForm" modelAttribute="customeraction" action="${ctx}/md/customerProductType/customerRelatedProductsForm" method="post" class="form-horizontal" cssStyle="margin-left: 0px;width: 100%">
    <sys:message content="${message}" />

    <ipunt type="hidden" value="${customerId}" id="customerId"></ipunt>
    <input type="hidden" value="${customerProductList}" id="customerProductList">
    <div style="margin-top:24px; height: 50px;">
        <div class="row-fluid" style="margin-left: 24px;width: 80%">
            <div class="span6" style="width: 355px;">
                <label class="control-label" style="width: 100px;">客户产品分类：</label>
                <div class="controls" style="margin-left: 80px;">
                    <input type="hidden" value="${customerProductTypeId}" id="customerProductTypeId">
                    <input id="customerProductTypeName" name="customerProductTypeName" style="width:237px;" readonly="readonly" type="text" value="${customerProductTypeName}" class="valid" aria-invalid="false">
                </div>
            </div>
            <div class="span6">
                <label style="margin-top: 5px" id="productNum"></label>
            </div>
        </div>
    </div>
    <legend style="margin-bottom: 0px;display: inline-block;"></legend>
    <div style="float: left">
        <div id="outer">
            <ul id="tab" class="nav nav-pills nav-stacked" style="text-align: center;">
                <c:forEach items="${productCategoryList}" var="entity">
                    <li id="${entity.id}">${entity.name}</li>
                </c:forEach>
            </ul>
        </div>
        <div id="content" style="background-color:white;">
        </div>
    </div>
    <div style="height: 58px;width: 98%;float: left"></div>
    <div id="editBtn"  class="line-row" style="width: 79%;">
        <shiro:hasPermission name="md:customeraction:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="确定" style="width: 96px;height: 40px;margin-top: 10px;margin-left: 500px"/>
            <input id="btnSubmit1" class="btn " type="button" onclick="javascript:closeDialog();" value="取消" style="margin-left: 20px;width: 96px;height: 40px;margin-top: 10px;"/>
        </shiro:hasPermission>
    </div>


</form:form>
<form:form id="submitForm" ></form:form>
<script>
    //获取变量==>存变量==>给变量绑定属性
    $(function(){
        var $li = $('#tab li');//获取每一个标题和内容用变量存起来

        $li.click(function(){
            var $this = $(this);//用变量把点击的每一个当前的li存起来
            $li.removeClass();//因为默认是第一个显示  所以先移除
            $this.addClass('current');//再添加当前的

            materialCategory($this.prop("id"));
        })
    });
    $(function () {
        $("li:first").addClass("current");
        var id = $("li:first").prop("id");
        materialCategory(id);
    })
</script>
</body>
</html>

