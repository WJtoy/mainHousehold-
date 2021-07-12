<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户产品分类</title>
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


        function showCustomerProductType(type,id) {
            var customerId = $("#customerId").val();
            var text = "添加客户产品分类";
            var url = "${ctx}/md/customerProductType/customerProductTypeForm?customerId=" + customerId;
            if(type == 20){
                text = "修改";
                url = "${ctx}/md/customerProductType/customerProductTypeForm?id=" + id;
            }
            var area = ['640px', '400px'];
            top.layer.open({
                type: 2,
                id:"customerProductType",
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

        function deleteCustomerProductType(customerId,id) {

            $.ajax({
                url: "${ctx}/md/customerProductType/ajax/customerProductTypeDelete",
                data: {customerId: customerId,id:id},
                success:function (e) {
                    if(e.success){
                        layerMsg(e.message);
                        var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                        if(pframe){
                            pframe.repage();
                        }
                    }else {
                        layerMsg(e.message);
                    }
                },
                error:function (e) {
                    layerError("请求失败","错误提示");
                }
            });
        }

    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li><a href="${ctx}/md/customerProductType/customerActionList">故障列表</a></li>
    <li class="active"><a href="javascript:void(0);">客户产品分类</a></li>
    <li><a href="${ctx}/md/customerProductType/customerRelatedProductsList">关联产品</a></li>


</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="searchForm"  modelAttribute="mdCustomerProductType" action="${ctx}/md/customerProductType/customerProductTypeList" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <c:choose>
        <c:when test="${currentuser.isCustomer()}">
            <li>
                <label style="margin-left: 0px"><span class="red">*</span>客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户：</label>
                <input type="hidden" value="${currentuser.id}" id="customerId">
                <input id="customerName" style="width:237px;" readonly="readonly" type="text" value="${currentuser.name}" class="valid" aria-invalid="false">
            </li>
        </c:when>
        <c:otherwise>
            <li>
                <label style="margin-left: 0px"><span class="red">*</span>客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户：</label>
                <form:select path="customerId" class="input-large" style="width:250px;">
                    <form:option value="" label="所有"/>
                    <form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id"
                                  htmlEscape="false"/>
                </form:select>
            </li>
        </c:otherwise>
    </c:choose>
    <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();"  style="margin-left:10px" value="查询" />
</form:form>
<shiro:hasPermission name="md:customeraction:edit">
    <button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 150px;height: 32px"
            onclick="showCustomerProductType()">
        <i class="icon-plus-sign"></i>&nbsp添加客户产品分类
    </button>
</shiro:hasPermission>
<sys:message content="${message}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
    <tr>
        <th width="50">序号</th>
        <th width="700">客户产品分类</th>
        <th width="700">别名</th>
        <shiro:hasPermission name="md:customeraction:edit"><th width="100">操作</th></shiro:hasPermission>
    </tr>
    </tr>
    </thead>
    <tbody>
    <c:set var="index" value="0"></c:set>
    <c:forEach items="${page.list}" var="entity">
        <tr>
            <c:set var="index" value="${index+1}"></c:set>
            <td>${index}</td>
            <td>${entity.name}</td>
            <td>${entity.alias}</td>

            <shiro:hasPermission name="md:customeraction:edit"> <td>
                <a href="javascript:showCustomerProductType(20,'${entity.id}')">修改</a>
                &nbsp;&nbsp;
                <a href="javascript:deleteCustomerProductType('${entity.customerId}','${entity.id}')" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
            </shiro:hasPermission>


        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>
