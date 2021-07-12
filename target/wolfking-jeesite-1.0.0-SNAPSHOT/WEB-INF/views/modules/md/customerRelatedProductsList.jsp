<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>关联产品</title>
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
        function updateCustomerProductType(btn) {
            var clicktag = 0;
            if (clicktag == 0) {
                clicktag = 1;
                var val = $("#customerId").val();
                if (val == undefined || val.length == 0) {
                    layerInfo("请选择客户!", "信息提示");
                    clicktag = 0;
                    return false;
                }

                var $btnSubmit = $(btn);
                $btnSubmit.attr('disabled', 'disabled');
                layerLoading("更新中...", true);

                $("#searchForm").attr("action", "${ctx}/md/customerProductType/updateCustomerRelatedProductsList");
                $("#searchForm").submit();
            }
        }

        function showCustomerProductTypeMapping(customerProductTypeId,customerProductTypeName) {
            var customerId = $("#customerId").val();
            var text = "关联产品";
            var url = "${ctx}/md/customerProductType/customerRelatedProductsForm?customerId="+ customerId +"&customerProductTypeId=" + customerProductTypeId + "&customerProductTypeName=" + customerProductTypeName;
            var area = ['936px', '560px'];
            top.layer.open({
                type: 2,
                id:"customerProductTypeMapping",
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

        function showCustomerAction(customerProductTypeId,customerProductTypeName) {
            var customerId = $("#customerId").val();
            var text = "故障详情";
            var url = "${ctx}/md/customerProductType/customerAction?customerId="+ customerId +"&customerProductTypeId=" + customerProductTypeId+"&customerProductTypeName=" + customerProductTypeName;
            var area = ['1000px', '720px'];
            top.layer.open({
                type: 2,
                id:"customerAction",
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
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li><a href="${ctx}/md/customerProductType/customerActionList">故障列表</a></li>
    <li><a href="${ctx}/md/customerProductType/customerProductTypeList">客户产品分类</a></li>
    <li class="active"><a href="javascript:void(0);">关联产品</a></li>

</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="searchForm"  modelAttribute="mdCustomerAction" action="${ctx}/md/customerProductType/customerRelatedProductsList" method="post" class="breadcrumb form-search">
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
    <shiro:hasPermission name="md:customeraction:edit">
    <button style="margin-top: 15px;margin-bottom: 15px;margin-left:10px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: #FF9502;width: 150px;height: 32px;color: #fff"
            onclick="updateCustomerProductType(this)">
        <i class="icon-refresh"></i>&nbsp更新客户产品分类
    </button>
    </shiro:hasPermission>
</form:form>
<sys:message content="${message}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
    <tr>
        <th width="50">序号</th>
        <th width="230">客户产品分类(关联产品数量)</th>
        <th width="800">产品</th>
        <shiro:hasPermission name="md:customeraction:edit"><th width="100">操作</th></shiro:hasPermission>
    </tr>
    </tr>
    </thead>
    <tbody>
    <c:set var="index" value="0"></c:set>
    <c:forEach items="${entityList}" var="entity">
        <tr>
            <c:set var="index" value="${index+1}"></c:set>
            <td>${index}</td>
            <td>${entity.customerProductTypeName}</td>
            <td>${entity.productName}</td>

            <shiro:hasPermission name="md:customeraction:edit"> <td>
                <a href="javascript:showCustomerProductTypeMapping('${entity.customerProductTypeId}','${entity.customerProductTypeName}')">关联产品</a>
                &nbsp;&nbsp;
                <a href="javascript:showCustomerAction('${entity.customerProductTypeId}','${entity.customerProductTypeName}')">故障详情</a></td>
            </shiro:hasPermission>


        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>
