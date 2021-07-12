<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>关联配件</title>
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
        .material{
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
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
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
                    if($("#content input[name='materialName']:checked").length == 0){
                        layerMsg('请选择配件！');
                        return false;
                    }
                    var materialId = $("[name=materialName]:checked").val();
                    var materialName = $("[name=materialName]:checked").parent().text();
                    var customMaterialName = $("#customMaterialName").val();

                    var customerId = $("#customer\\.id").val();
                    var productId = $("#product\\.id").val();

                    if(materialId == ''){
                        if(customMaterialName == ''){
                            $("#customMaterialName").css('borderColor','red'); //添加css样式
                            return false;
                        }else {
                            var port = true;
                           $.ajax({
                                async: false,
                                url:"${ctx}/md/customerMaterialNew/ajax/checkMaterialName?name="+customMaterialName,
                                success:function (e) {
                                    if(e.success){

                                    }else {
                                        port = false;
                                        layerMsg('配件名已存在！');
                                    }
                                },
                                error:function (e) {
                                    layerError("验证配件名失败","错误提示");
                                }
                            });
                            if(!port){
                                return false;
                            }

                        }
                    }else {
                        var to = true;
                        $.ajax({
                            async: false,
                            url:"${ctx}/md/customerMaterialNew/ajax/checkCustomerMaterial?customerId="+customerId+"&productId="+productId+"&materialId="+ materialId,
                            success:function (e) {
                                if(e.success){

                                }else {
                                    to = false;
                                    layerMsg('配件已存在！');
                                }
                            },
                            error:function (e) {
                                layerError("验证配件失败","错误提示");
                            }
                        });
                        if(!to){
                            return false;
                        }
                    }

                    $btnSubmit.prop("disabled", true);
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var entity = {};
                    var materialCategoryId = $("li.current").prop("id");
                    entity["material.materialCategory.id"] = materialCategoryId;
                    entity["material.name"] = customMaterialName;
                    entity["material.id"] = materialId;
                    entity["customer.id"] = customerId;
                    entity["product.id"] = productId;
                    entity["id"] = $("#id").val();
                    $.ajax({
                        url:"${ctx}/md/customerMaterialNew/ajax/ajaxSaveMaterial",
                        type:"POST",
                        data: entity,
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
                                var mark = $("#mark").val();
                                if(mark == 1){
                                    var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                    if(pframe){
                                        pframe.repage();
                                    }
                                }
                                if(parentIndex && parentIndex != undefined && parentIndex != ''){
                                    var layero = $("#layui-layer" + parentIndex,top.document);
                                    var iframeWin = top[layero.find('iframe')[0]['name']];
                                    if(materialId == ''){
                                        materialName = $("#customMaterialName").val();
                                    }
                                    materialId = data.data;
                                    iframeWin.refreshMaterial(materialId,materialName);
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

        $(document).on("change","#materialId",function (e) {
            checkCustomMaterial();
        });
        $(document).on("change","#ctMaterialId",function (e) {
            checkCustomMaterial();
        });
        $(document).on("change","#customMaterialName",function (e) {
            if($("#ctMaterialId").attr("checked")){
                $("#customMaterialName").css('borderColor',''); //取消css样式
                checkCustomMaterial();
            }

        });

        function checkCustomMaterial() {
            var materialId = $("[name=materialName]:checked").val();
            var materialName = $("[name=materialName]:checked").parent().text();
            var materialCategoryName = $("li.current").html();
            if (materialId !='') {
                $("#materialName").html("已选“" + materialCategoryName +">"+materialName +"”");
            }else {
                var customMaterialName = $("#customMaterialName").val();
                $("#materialName").html("已选“自定义配件>"+ customMaterialName +"”");
            }
        }



        function materialCategory(materialCategoryId) {
            $("#content").html("");
            var customerMaterialList = $("#customerMaterialList").val();
            var items = ${fns:toJson(customerMaterialList)};
            $.ajax({
                url: "${ctx}/md/customerMaterialNew/ajax/getMaterialCategoryFromMaterial",
                data: {materialCategoryId: materialCategoryId},
                success:function (e) {
                    if(e.success){
                        var programme_sel=[];
                        for(var i=0,len = e.data.length;i<len;i++){
                            var flag = 0;
                            var programme = e.data[i];
                            for(var j=0;j<items.length;j++){
                                var customerMaterial = items[j];
                                if(programme.id==customerMaterial.material.id){
                                    flag = 1;
                                    programme_sel.push('<span style="color: #808695;" id="spMaterialId" class="span" title="'+programme.name+'(已关联)'+'"><input type="radio" class="material"  disabled="true" value="'+programme.id+'" name="materialName" id="materialId" >'+programme.name+'</input><span style="background:#F2FBFF;font-size: 12px;margin-left: 5px;padding: 5px 5px 5px;color:#2FA2DE">已关联</span></span>')

                                }
                            }
                            if(flag != 1){
                                programme_sel.push('<span id="spMaterialId" class="span" title="'+programme.name+'"><label><input type="radio" class="material" value="'+programme.id+'" name="materialName" id="materialId" />'+programme.name+'</label></span>')

                            }
                        }
                        programme_sel.push('<legend style="margin-bottom: 0px;display: inline-block;float: left;margin-top: 10px;margin-bottom: 10px"></legend>');
                        programme_sel.push('<span id="spMaterialId" class="span" style="width: 100px;"><label><input type="radio" class="material" name="materialName" value="" id="ctMaterialId">自定义配件</label></span>');
                        programme_sel.push('<input type="text" style="float: left;margin-top: 5px;width: 236px" class="input-small"  id="customMaterialName" name="customMaterialName">');
                        $("#content").append(programme_sel);
                    }
                },
                error:function (e) {
                    layerError("请求配件失败","错误提示");
                }
            });
        }
        function closeDialog() {
            top.layer.close(this_index);   //关闭本身
        }
        function editMaterialCategory() {
            var text = "添加配件分类";
            var url = "${ctx}/md/materialCategory/newForm";
            top.layer.open({
                type: 2,
                id:"materialCategory",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['720px', '353px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
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

<form:form id="inputForm" modelAttribute="customerMaterial" action="${ctx}/md/customerMaterialNew/showProductMaterial" method="post" class="form-horizontal" cssStyle="margin-left: 0px;width: 100%">
    <sys:message content="${message}" />
    <form:hidden path="id"></form:hidden>
    <form:hidden path="customer.id"></form:hidden>
    <input type="hidden" value="${mark}" id="mark">
    <input type="hidden" value="${customerMaterialList}" id="customerMaterialList">
<%--    <c:set var="customerId" value="${customerMaterial.customer.id}"></c:set>--%>
    <div style="margin-top:24px; height: 50px;">
        <div class="row-fluid" style="margin-left: 24px;width: 80%">
            <div class="span6" style="width: 350px;">
                <label class="control-label" style="width: 50px;">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
                <div class="controls" style="margin-left: 80px;">
                    <form:hidden path="product.id"/>
                    <form:input path="product.name" readonly="true" style="width:250px;"/>
                </div>
            </div>
            <div class="span6">
                <label style="color: #3ca9e9;margin-top: 5px" id="materialName"></label>
            </div>
        </div>
    </div>
    <legend style="margin-bottom: 0px;display: inline-block;"></legend>
    <div>
        <div id="outer">
            <ul id="tab" class="nav nav-pills nav-stacked" style="text-align: center;">
                <c:forEach items="${materialCategoryList}" var="entity">
                <li id="${entity.id}">${entity.name}</li>
                </c:forEach>
<%--                <a style="cursor: pointer;width: 150px;font-size: 14px;line-height: 35px;" href="javascript:void(0)" onclick="editMaterialCategory()">+添加分类</a>--%>

            </ul>
        </div>
        <div id="content" style="background-color:white;">
        </div>
    </div>
    <div style="height: 58px;width: 98%;float: left"></div>
    <div id="editBtn"  class="line-row" style="width: 79%;">
        <shiro:hasPermission name="md:customermaterial:edit">
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

