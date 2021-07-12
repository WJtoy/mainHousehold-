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
                layerMsg('请先选择客户！');
            }
        });

        function editCustomerMaterial(type,id) {
            var text = "添加配件";
            var url = "${ctx}/customer/md/customerMaterial/form";
            var area = ['1392px', '800px'];
            if(type == 20){
                text = "修改配件";
                url = "${ctx}/customer/md/customerMaterial/form?id="+ id;
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
                        url: "${ctx}/customer/md/customerMaterial/ajax/delete?id="+id+"&customer.id="+customerId+"&product.id="+productId+"&material.id="+materialId,
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
    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">配件</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<input type="hidden" id="idSale" value="${currentuser.isSaleman()}">
<sys:message content="${message}"/>
<form:form id="searchForm" modelAttribute="customerMaterial" action="${ctx}/customer/md/customerMaterial/list" method="post" class="breadcrumb form-search">
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
        <form:option value="" label="请选择"></form:option>
        <form:options items="${productCategoryList}" itemLabel="name" itemValue="id"></form:options>
    </form:select>
    <label>产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
    <form:select path="product.id" cssStyle="width: 250px;">
        <form:option value="" label="请选择"></form:option>
        <form:options items="${productList}" itemLabel="name" itemValue="id"></form:options>
    </form:select>
    &nbsp;
    <label>配&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：</label>
    <form:select path="material.id" cssStyle="width: 250px;">
        <form:option value="" label="请选择"></form:option>
        <form:options items="${materialList}" itemLabel="name" itemValue="id"></form:options>
    </form:select>
    &nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
</form:form>
<shiro:hasPermission name="customer:md:customermaterial:edit">
    <button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 100px;height: 32px" onclick="editCustomerMaterial()">
        <i class="icon-plus-sign"></i>&nbsp;添加配件
    </button></shiro:hasPermission>

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
        <shiro:hasPermission name="customer:md:customermaterial:edit"><th width="100">操作</th></shiro:hasPermission>
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
                    ${entity.material.name}
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
            <shiro:hasPermission name="customer:md:customermaterial:edit"><td>
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
