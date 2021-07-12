<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户配件</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
            height: 30px;
        }
    </style>
    <script type="text/javascript">
        $(document).ready(function() {
            $('a[data-toggle=tooltip]').darkTooltip();
            $("#contentTable").treeTable({expandLevel : 5});

            $(document).on('change',"#customer\\.id",function (e) {
                var customerId = $(this).val();
                if (customerId !=null || customerId !='' || customerId != '0') {
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

                    $.ajax({
                        url: "${ctx}/md/customerMaterialNew/ajax/customerProductCategoryList",
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

        });

        //覆盖分页前方法
        function beforePage() {
            var val = $("#customer\\.id").val();
            if (val == undefined || val.length == 0) {
                layerInfo("请选择客户!", "信息提示");
                return false;
            }
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            $("#btnClearSearch").attr('disabled', 'disabled');
            layerLoading("查询中...", true);
            return true;
        }
        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function () {
            if (clicktag == 0) {
                clicktag = 1;
                var result = beforePage();
                if(!result){
                    clicktag = 0;
                    return false;
                }
                setPage();
                this.form.submit();
            }
        });

        $(document).on("change","#productCategoryId",function (e) {
            var productCategoryId = $(this).val();
            var customerId = $("#customer\\.id").val();
            if (customerId !='') {
                if (productCategoryId != "") {
                    $.ajax({
                        url: "${ctx}/md/customerMaterialNew/ajax/getProductCategoryProductList",
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
                }
            }else {
                layerMsg('请先选择客户！');
            }
        });

        // 产品发生变更,获取客户产品型号
        $(document).on("change","#product\\.id",function (e) {
            var productId = $(this).val();
            var customerId = $("#customer\\.id").val();
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
                            }else {
                                $("#customerProductModelId").html('<option value="" selected>请选择</option>');
                                // layerMsg('该客户还没有配置产品！');
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
        });

        function editCustomerMaterial(type,id) {
            var text = "添加配件";
            var url = "${ctx}/md/customerMaterialNew/formNew";
            var area = ['936px', '640px'];
            if(type == 20){
                text = "修改";
                url = "${ctx}/md/customerMaterialNew/formNew?id="+ id;
            }
            top.layer.open({
                type: 2,
                id:"completePic",
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
        }
        function showProductMaterial(id,productId,productName,customerId,mark) {
                var text = "关联配件";
                var url = "${ctx}/md/customerMaterialNew/showProductMaterial?id="+ id +"&product.id=" + productId + "&product.name=" + productName +"&customer.id=" + customerId +"&mark=" + mark;
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

        }
        function deleteCustomerMaterials(id,customerId,productId,materialId,materialName) {
            layer.confirm(
                '确认要删除配件' +'<label style="color:#63B9E6">'+ materialName +'</label>吗？',
                {
                    btn: ['确定','取消'], //按钮
                    title:'提示'
                }, function(index){
                    layer.close(index);//关闭本身
                    var loadingIndex = top.layer.msg('正在删除，请稍等...', {
                        icon: 16,
                        time: 0,//不定时关闭
                        shade: 0.3
                    });
                    $.ajax({
                        url: "${ctx}/md/customerMaterialNew/ajax/delete?id="+id+"&customer.id="+customerId+"&product.id="+productId+"&material.id="+materialId,
                        success:function (data) {
                            // 提交后的回调函数
                            if(loadingIndex) {
                                setTimeout(function () {
                                    layer.close(loadingIndex);
                                }, 2000);
                            }
                            if (data.success) {
                                layerMsg(data.message);
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                            } else {
                                layerError("删除失败:" + data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            ajaxLogout(data,null,"数据操作错误，请重试!");
                        },
                    });
                    return false;
                }, function(){
                    // 取消操作
                });
        }

        function updateCustomerMaterials(btn) {
            var clicktag = 0;
            if (clicktag == 0) {
                clicktag = 1;
                var customerId = $("#customer\\.id").val();
                if (customerId == undefined || customerId.length == 0) {
                    layerInfo("请选择客户!", "信息提示");
                    clicktag = 0;

                    return false;
                }

                var productId = $("#product\\.id").val();
                if (productId == undefined || productId.length == 0) {
                    layerInfo("请选择产品!", "信息提示");
                    clicktag = 0;
                    return false;
                }

                var customerProductModelId = $("#customerProductModelId").val();
                if (customerProductModelId == undefined || customerProductModelId.length == 0) {
                    layerInfo("请选择客户产品!", "信息提示");
                    clicktag = 0;
                    return false;
                }

                var $btnSubmit = $(btn);
                $btnSubmit.attr('disabled', 'disabled');
                layerLoading("查询中...", true);

                setPage();
                $("#searchForm").attr("action","${ctx}/md/customerMaterialNew/updateMaterialList");
                $("#searchForm").submit();
            }
        }
    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">配件</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<input type="hidden" id="idSale" value="${currentuser.isSaleman()}">
<form:form id="searchForm" modelAttribute="customerMaterial" action="${ctx}/md/customerMaterialNew/listNew" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <c:choose>
        <c:when test="${currentuser.isCustomer()}">
            <li>
                <label style="margin-left: 0px"><span class="red">*</span>客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户：</label>
                <form:hidden path="customer.id"/>
                <form:input path="customer.name" readonly="true" style="width:250px;"/>
            </li>
        </c:when>
        <c:otherwise>
            <li>
                <label style="margin-left: 0px"><span class="red">*</span>客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户：</label>
                <form:select path="customer.id" class="input-large" style="width:250px;">
                    <form:option value="" label="所有"/>
                    <form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false" />
                </form:select>
            </li>
        </c:otherwise>
    </c:choose>
    &nbsp;
    <label>产品品类：</label>
    <form:select path="productCategoryId" cssStyle="width: 250px;">
        <option value=""
                <c:out value="${(empty customerMaterial.productCategoryId)?'selected=selected':''}" />>请选择</option>
        <c:forEach items="${productCategoryList}" var="dict">
            <option value="${dict.id}" <c:out value="${(customerMaterial.productCategoryId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
        </c:forEach>
    </form:select>
    <label>产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
    <form:select path="product.id" cssStyle="width: 250px;">
        <option value=""
                <c:out value="${(empty customerMaterial.product.id)?'selected=selected':''}" />>请选择</option>
        <c:forEach items="${productList}" var="dict">
            <option value="${dict.id}" <c:out value="${(customerMaterial.product.id eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
        </c:forEach>
    </form:select>
    &nbsp;
    <div style="margin-top: 8px">
    <label style="margin-left: 5px">客户产品：</label>
    <form:select path="customerProductModelId" cssStyle="width: 250px;">
        <option value=""
                <c:out value="${(empty customerMaterial.customerProductModelId)?'selected=selected':''}" />>请选择</option>
        <c:forEach items="${customerProductModelList}" var="dict">
            <option value="${dict.id}" <c:out value="${(customerMaterial.customerProductModelId eq dict.id)?'selected=selected':''}" />>${dict.customerProductName}</option>
        </c:forEach>
    </form:select>
    <label style="margin-left: 18px">配&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：</label>
    <form:select path="material.id" cssStyle="width: 250px;">
        <option value=""
                <c:out value="${(empty customerMaterial.material.id)?'selected=selected':''}" />>请选择</option>
        <c:forEach items="${materialList}" var="dict">
            <option value="${dict.id}" <c:out value="${(customerMaterial.material.id eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
        </c:forEach>
    </form:select>
    &nbsp;
    <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
    <button  type="button" style="border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: #FF9502;width: 120px;height: 32px;color: #fff" onclick="updateCustomerMaterials(this)">
    <i class="icon-refresh"></i>&nbsp更新配件列表
</button>
    </div>
</form:form>
<shiro:hasPermission name="md:customermaterial:edit">
    <button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 100px;height: 32px" onclick="editCustomerMaterial()">
        <i class="icon-plus-sign"></i>&nbsp;添加配件
    </button></shiro:hasPermission>
<sys:message content="${message}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
    <tr>
        <th width="50">序号</th>
        <th width="250">产品</th>
        <th width="160">配件</th>
        <th width="200">客户配件名称</th>
        <th width="200">客户配件编码</th>
        <th width="80">质保天数(天)</th>
        <th width="80">返件</th>
        <th width="80">参考价格(元)</th>
        <th width="80">回收配件</th>
        <th width="80">回收价格(元)</th>
        <th>描述</th>
        <shiro:hasPermission name="md:customermaterial:edit"><th width="100">操作</th></shiro:hasPermission>
    </tr>
    </tr>
    </thead>
    <tbody>
    <c:set var="index" value="0"></c:set>
    <c:forEach items="${page.list}" var="entity">
        <tr>
            <c:set var="index" value="${index+1}"></c:set>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${entity.product.name}</td>
            <td>
                <c:choose>
                    <c:when test="${entity.material.id gt 0}">
                        ${entity.material.name}
                    </c:when>
                    <c:otherwise>
                        <shiro:hasPermission name="md:customermaterial:edit">
                        <a onclick="javascript:showProductMaterial('${entity.id}','${entity.product.id}','${entity.product.name}','${entity.customer.id}',1)">点击关联配件</a>
                        </shiro:hasPermission>
                    </c:otherwise>
                </c:choose>

            </td>
            <td>${entity.customerPartName}</td>
            <td>${entity.customerPartCode}</td>
            <td>${entity.warrantyDay}</td>
            <c:choose>
                <c:when test="${entity.isReturn==1}">
                    <td><span style="color: red">是</span></td>
                </c:when>
                <c:otherwise>
                    <td>否</td>
                </c:otherwise>
            </c:choose>
            <td><fmt:formatNumber value="${entity.price}" pattern="0.0"></fmt:formatNumber></td>
            <c:choose>
                <c:when test="${entity.recycleFlag==1}">
                    <td><span style="color: red">是</span></td>
                </c:when>
                <c:otherwise>
                    <td>否</td>
                </c:otherwise>
            </c:choose>
            <td><fmt:formatNumber value="${entity.recyclePrice}" pattern="0.0"></fmt:formatNumber></td>

            <td>
                <c:choose>
                    <c:when test="${fn:length(entity.remarks)>40}">
                        <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.remarks}">${fns:abbr(entity.remarks,80)}</a>
                    </c:when>
                    <c:otherwise>
                        ${entity.remarks}
                    </c:otherwise>
                </c:choose>
            </td>
            <shiro:hasPermission name="md:customermaterial:edit"><td>
                <a href="javascript:editCustomerMaterial(20,'${entity.id}')">修改</a>
                &nbsp;&nbsp;
                <a href="#" onclick="deleteCustomerMaterials('${entity.id}','${entity.customer.id}','${entity.product.id}','${entity.material.id}','${entity.material.name}')">删除</a><br>
            </td></shiro:hasPermission>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>
