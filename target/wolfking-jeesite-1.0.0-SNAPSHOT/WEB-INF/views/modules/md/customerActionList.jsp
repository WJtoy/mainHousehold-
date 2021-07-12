<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>故障列表</title>
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

            $(document).on('change','#productId',function(){
                getErrorTypeNameList();
            });

            $(document).on('change','#customerId',function(){
                editCustomerProduct();
            });
        });

        function editCustomerProduct() {
            var customerId =$("#customerId").val();
            if (customerId == "" || customerId == null) {
                return false;
            }
            $.ajax({
                    url:"${ctx}/md/product/ajax/customerProductList?customerId="+customerId,
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
                            $("#productId").change();
                        }
                    },
                    error:function (e) {
                        layerError("请求客户产品失败","错误提示");
                    }
                }
            );
        }

        function getErrorTypeNameList() {
            var customerId =$("#customerId").val();
            var productId =$("#productId").val();
            if (customerId == "" || productId =="")
            {
                return false;
            }
            $.ajax({
                    url:"${ctx}/md/customerProductType/ajax/findErrorTypeNameList?customerId="+customerId + "&productId=" + productId,
                    success:function (e) {
                        if(e.success){
                            $("#errorTypeName").empty();
                            var programme_sel=[];
                            programme_sel.push('<option value="" selected="selected">请选择</option>')
                            for(var i=0,len=e.data.length;i<len;i++){
                                var programme = e.data[i];
                                programme_sel.push('<option value="'+programme+'">'+programme+'</option>')
                            }
                            $("#errorTypeName").append(programme_sel.join(' '));
                            $("#errorTypeName").val("");
                            $("#errorTypeName").change();

                        }else {
                            $("#errorTypeName").html('<option value="" selected>请选择</option>');
                            $("#errorTypeName").change();
                        }
                    },
                    error:function (e) {
                        layerError("请求故障失败","错误提示");
                    }
                }
            );
        }
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

                $("#searchForm").attr("action", "${ctx}/md/customerProductType/updateCustomerProductTypeList");
                $("#searchForm").submit();
            }
        }
        function editCustomerAction(type,customerProductTypeId,customerProductTypeName,errorTypeName,errorTypeCode,errorAppearanceName,errorAppearanceCode) {
            var customerId = $("#customerId").val();
            var text = "添加故障处理";
            var url = "${ctx}/md/customerProductType/customerActionForm?newFlag=" + true;
            var area = ['1200px', '680px'];
            if(customerId != ''){
                url = "${ctx}/md/customerProductType/customerActionForm?customerId=" + customerId + "&newFlag=" + true;
            }
            if(type == 20){
                text = "修改故障处理";
                url = "${ctx}/md/customerProductType/customerActionForm?&customerId=" + customerId +"&customerProductTypeId=" + customerProductTypeId +"&customerProductTypeName=" + customerProductTypeName +
                    "&errorTypeName=" + errorTypeName + "&errorTypeCode=" + errorTypeCode + "&errorAppearanceName=" + errorAppearanceName + "&errorAppearanceCode=" + errorAppearanceCode + "&newFlag=" + false;
            }
            top.layer.open({
                type: 2,
                id:"completeAction",
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

        function deleteCustomerAction(productId,customerProductTypeId,errorTypeName,errorAppearanceName) {
            var customerId = $("#customerId").val();
            $.ajax({
                url: "${ctx}/md/customerProductType/delete",
                data: {customerId: customerId,customerProductTypeId:customerProductTypeId,errorTypeName:errorTypeName,errorAppearanceName:errorAppearanceName},
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
    <li class="active"><a href="javascript:void(0);">故障列表</a></li>
    <li><a href="${ctx}/md/customerProductType/customerProductTypeList">客户产品分类</a></li>
    <li><a href="${ctx}/md/customerProductType/customerRelatedProductsList">关联产品</a></li>



</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="searchForm"  modelAttribute="mdCustomerActionDto" action="${ctx}/md/customerProductType/customerActionList" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${entityPage.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${entityPage.pageSize}"/>
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

    <label>产&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp品：</label>
    <select id="productId" name="productId" class="input-small" style="width:200px;">
        <option value=""
                <c:out value="${(empty mdCustomerActionDto.productId)?'selected=selected':''}" />>请选择</option>
        <c:forEach items="${productList}" var="dict">
            <option value="${dict.id}" <c:out value="${(mdCustomerActionDto.productId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
        </c:forEach>
    </select>

    <label>故障分类：</label>
    <select id="errorTypeName" name="errorTypeName" class="input-small" style="width:200px;">
        <option value=""
                <c:out value="${(empty mdCustomerActionDto.errorTypeName)?'selected=selected':''}" />>请选择</option>
        <c:forEach items="${errorTypeNameList}" var="dict">
            <option value="${dict}" <c:out value="${(mdCustomerActionDto.errorTypeName eq dict)?'selected=selected':''}" />>${dict}</option>
        </c:forEach>
    </select>
    <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();"  style="margin-left:10px" value="查询" />

</form:form>
<shiro:hasPermission name="md:customeraction:edit">
    <button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 120px;height: 32px"
            onclick="editCustomerAction()">
        <i class="icon-plus-sign"></i>&nbsp添加故障处理
    </button>
</shiro:hasPermission>
<sys:message content="${message}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
    <tr>
        <th width="50">序号</th>
        <th width="230">产品</th>
        <th width="230">客户产品分类</th>
        <th width="230">故障分类</th>
        <th width="100">故障分类编码</th>
        <th width="230">故障现象</th>
        <th width="100">故障现象编码</th>
        <shiro:hasPermission name="md:customeraction:edit"><th width="100">操作</th></shiro:hasPermission>
    </tr>
    </tr>
    </thead>
    <tbody>
    <c:set var="index" value="0"></c:set>
    <c:forEach items="${entityPage.list}" var="entity">
        <tr>
            <c:set var="index" value="${index+1}"></c:set>
            <td>${index}</td>
            <td>${entity.productName}</td>
            <td>${entity.customerProductTypeName}</td>
            <td>${entity.errorTypeName}</td>
            <td>${entity.errorTypeCode}</td>
            <td>${entity.errorAppearanceName}</td>
            <td>${entity.errorAppearanceCode}</td>
            <shiro:hasPermission name="md:customeraction:edit"> <td>
                <a href="javascript:editCustomerAction(20,'${entity.customerProductTypeId}','${entity.customerProductTypeName}','${entity.errorTypeName}','${entity.errorTypeCode}','${entity.errorAppearanceName}','${entity.errorAppearanceCode}')">修改</a>
                &nbsp;&nbsp;
                <a href="javascript:deleteCustomerAction('${entity.productId}','${entity.customerProductTypeId}','${entity.errorTypeName}','${entity.errorAppearanceName}')" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
            </shiro:hasPermission>


        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${entityPage}</div>
</body>
</html>
