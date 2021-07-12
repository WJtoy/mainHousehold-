<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>故障列表</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>

    <script type="text/javascript">
        $(document).ready(function() {
            $(document).on('change',"#productCategoryId", function(e){
                var productCategoryId = $(this).val();
                if(productCategoryId ==null || productCategoryId==''){
                    $("#productId").html('<option value="" selected>请选择</option>');
                    $("#productId").val("");
                    $("#productId").change();
                    return false;
                }
                $.ajax({
                    url:"${ctx}/md/product/ajax/singleProductList?productCategoryId=" + productCategoryId,
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
                            layerMsg('该产品分类还没有产品！');
                        }
                    },
                    error:function (e) {
                        layerError("请求产品失败","错误提示");
                    }
                });
            });

            $(document).on('change',"#productId",function (e) {
                var productId = $(this).val();
                if(productId ==null || productId==''){
                    $("#errorTypeId").html('<option value="" selected>请选择</option>');
                    $("#errorTypeId").val("");
                    $("#errorTypeId").change();
                    return false;
                }
                $.ajax({
                    url:"${ctx}/provider/md/customerErrorType/ajax/findListByCustomerAndProduct?productId=" + productId +"&customerId=0",
                    success:function (e) {
                        if(e.success){
                            $("#errorTypeId").empty();
                            var programme_sel=[];
                            programme_sel.push('<option value="" selected="selected">请选择</option>')
                            for(var i=0,len=e.data.length;i<len;i++){
                                var programme = e.data[i];
                                programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                            }
                            $("#errorTypeId").append(programme_sel.join(' '));
                            $("#errorTypeId").val("");
                            $("#errorTypeId").change();
                        }else {
                            $("#errorTypeId").html('<option value="" selected>请选择</option>');
                            layerMsg('该产品还没有故障分类！');
                        }
                    },
                    error:function (e) {
                        layerError("请求产品故障分类失败","错误提示");
                    }
                });
            });
        });

        //跳到配置服务区域页面
        function showErrorActionForm(id) {
            top.layer.open({
                type: 2,
                id:'layer_errorActionForm',
                zIndex:19891015,
                title:'修改',
                content: "${ctx}/provider/md/errorAction/form?id=" + id,
                area: ['680px', screen.height-700+'px'],
                shade: 0.3,
                shadeClose:true,
                maxmin: false,
                success: function(layero,index) {},
                end: function(){}
            });
        }

        function repage() {
            var url = "${ctx}/provider/md/errorAction/findList";
            $("#searchForm").attr("action",url);
            $("#searchForm").submit();
        }
    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">故障列表</a></li>
    <li><a href="${ctx}/provider/md/errorCode/findList">故障现象添加</a></li>
    <li><a href="${ctx}/provider/md/errorAction/findListForActionCode">分析处理添加</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="searchForm" modelAttribute="errorActionDto" action="${ctx}/provider/md/errorAction/findList" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <label>品类：</label>
    <select id="productCategoryId" name="productCategoryId" class="input-small" style="width:200px;">
        <option value=""
                <c:out value="${(empty errorActionDto.productCategoryId)?'selected=selected':''}" />>请选择</option>
        <c:forEach items="${fns:getProductCategories()}" var="dict">
            <option value="${dict.id}" <c:out value="${(errorActionDto.productCategoryId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
        </c:forEach>
    </select>

    <label>产品：</label>
    <select id="productId" name="productId" class="input-small" style="width:200px;">
        <option value=""
                <c:out value="${(empty errorActionDto.productId)?'selected=selected':''}" />>请选择</option>
        <c:forEach items="${productList}" var="dict">
            <option value="${dict.id}" <c:out value="${(errorActionDto.productId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
        </c:forEach>
    </select>

    <label>服务类型：</label>
    <select id="serviceTypeId" name="actionCodeDto.serviceTypeId" class="input-small" style="width:200px;">
        <option value=""
                <c:out value="${(empty errorActionDto.actionCodeDto.serviceTypeId)?'selected=selected':''}" />>请选择</option>
        <c:forEach items="${serviceTypeList}" var="dict">
            <option value="${dict.id}" <c:out value="${(errorActionDto.actionCodeDto.serviceTypeId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
        </c:forEach>
    </select>

    <label>故障分类：</label>
    <select id="errorTypeId" name="errorCodeDto.errorTypeId" class="input-small" style="width:200px;">
        <option value="" selected>请选择</option>
        <c:forEach items="${errorTypeList}" var="dict">
            <option value="${dict.id}" <c:out value="${(errorActionDto.errorCodeDto.errorTypeId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
        </c:forEach>
    </select>   &nbsp;

    <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="top.$.jBox.tip('正在查询,请稍候...', 'loading');return setPage();" value="查询" />
</form:form>
<sys:message content="${message}" />
<table id="contentTable"
       class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="30">序号</th>
        <th width="150">产品</th>
        <th>故障分类名称</th>
        <th>故障分类代码</th>
        <th>故障现象</th>
        <th>故障现象代码</th>
        <th>故障分析</th>
        <th>故障处理</th>
        <th>服务类型</th>
        <th style="text-align: center;">操作</th>
    </tr>
    </thead>
    <tbody>
    <c:set var="index" value="0"></c:set>
    <c:forEach items="${page.list}" var="entity">
        <tr>
            <c:set var="index" value="${index+1}"></c:set>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${entity.productName}</td>
            <td>${entity.errorCodeDto.errorTypeName}</td>
            <td>${entity.errorCodeDto.errorTypeCode}</td>
            <td>${entity.errorCodeDto.name}</td>
            <td>${entity.errorCodeDto.code}</td>
            <td>${entity.actionCodeDto.analysis}</td>
            <td>${entity.actionCodeDto.name}</td>
            <td>${entity.actionCodeDto.serviceTypeName}</td>
            <td style="text-align: center;">
                <shiro:hasPermission name="md:erroraction:edit">
                    <a style="margin-left: 6px;" href="javascript:void(0);"
                       onclick="javascript:showErrorActionForm(${entity.id});">修改</a>
                    <a style="margin-left: 6px;" href="${ctx}/provider/md/errorAction/delete?id=${entity.id}&productId=${entity.productId}&actionCodeId=${entity.actionCodeDto.id}&customerId=${entity.customerId}"
                       onclick="return confirmx('将同时删除该故障处理对应的故障分析，确认要删除吗？', this.href)">删除</a>
                </shiro:hasPermission>
            </td>

        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>
