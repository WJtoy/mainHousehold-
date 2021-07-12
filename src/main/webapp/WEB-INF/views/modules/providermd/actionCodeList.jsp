<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>分析处理添加</title>
    <meta name="decorator" content="default" />
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>

    <script type="text/javascript">
        $(document).ready(function() {
            $('a[data-toggle=tooltip]').darkTooltip();
            $(document).on('change',"#productId",function (e) {
                var productId = $(this).val();
                if(productId ==null || productId==''){
                    $("#errorTypeId").html('<option value="" selected>请选择</option>');
                    $("#errorTypeId").val("");
                    $("#errorTypeId").change();
                    $("#errorCodeId").html('<option value="" selected>请选择</option>');
                    $("#errorCodeId").val("");
                    $("#errorCodeId").change();
                    return false;
                }
                $.ajax({
                    url:"${ctx}/provider/md/customerErrorType/ajax/findListByCustomerAndProduct?productId=" + productId+"&customerId=0",
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

            $(document).on('change',"#errorTypeId",function (e) {
                var productId = $("#productId").val();
                var errorTypeId = $(this).val();
                if (productId == null || productId =='') {
                    return false;
                }
                if(errorTypeId ==null || errorTypeId==''){
                    $("#errorCodeId").html('<option value="" selected>请选择</option>');
                    $("#errorCodeId").val("");
                    $("#errorCodeId").change();
                    return false;
                }
                $.ajax({
                    url:"${ctx}/provider/md/customerErrorCode/ajax/findListByProductIdAndErrorTypeId?productId=" + productId+"&errorTypeId="+errorTypeId+"&customerId=0",
                    success:function (e) {
                        if(e.success){
                            $("#errorCodeId").empty();
                            var programme_sel=[];
                            programme_sel.push('<option value="" selected="selected">请选择</option>')
                            for(var i=0,len=e.data.length;i<len;i++){
                                var programme = e.data[i];
                                programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                            }
                            $("#errorCodeId").append(programme_sel.join(' '));
                            $("#errorCodeId").val("");
                            $("#errorCodeId").change();
                        }else {
                            $("#errorCodeId").html('<option value="" selected>请选择</option>');
                            layerMsg('该产品及故障分类下还没有故障现象！');
                        }
                    },
                    error:function (e) {
                        layerError("请求故障现象失败","错误提示");
                    }
                });
            });

            $(document).on('change',"#serviceTypeId",function (e) {
                var productId = $("#productId").val();
                var serviceTypeId = $(this).val();
                if (productId == null || productId =='') {
                    return false;
                }
                if(serviceTypeId ==null || serviceTypeId==''){
                    return false;
                }
                $.ajax({
                    url:"${ctx}/md/productprice/ajax/getPrice?productId=" + productId+"&serviceTypeId="+serviceTypeId+"&priceType=10",
                    success:function (e) {
                        if(e.success){
                            $("#price").val(e.data);
                        }else {
                            layerMsg('该产品没有参考价格！');
                        }
                    },
                    error:function (e) {
                        layerError("请求参考价格失败","错误提示");
                    }
                });
            });

            $("#btnAdd").on("click",function(){
                $("#name").rules("add",'required');
                var $btnAdd = $("#btnAdd");
                $btnAdd.attr('disabled', 'disabled');

                var productId = $("#productId").val();
                if (productId == null || productId =='') {
                    layerAlert('请选择产品', '系统提示');
                    $btnAdd.removeAttr("disabled");
                    return false;
                }

                var errorTypeId = $("#errorTypeId").val();
                if (errorTypeId == null || errorTypeId =='') {
                    layerAlert('请选择故障分类', '系统提示');
                    $btnAdd.removeAttr("disabled");
                    return false;
                }

                var errorCodeId = $("#errorCodeId").val();
                if (errorCodeId == null || errorCodeId =='') {
                    layerAlert('请选择故障现象', '系统提示');
                    $btnAdd.removeAttr("disabled");
                    return false;
                }

                var serviceTypeId = $("#serviceTypeId").val();
                if (serviceTypeId == null || serviceTypeId =='') {
                    layerAlert('请选择服务类型', '系统提示');
                    $btnAdd.removeAttr("disabled");
                    return false;
                }

                var url = "${ctx}/provider/md/errorAction/save";
                $("#searchForm").attr("action",url);
                $("#searchForm").submit();

                $btnAdd.removeAttr("disabled");
                return false;
            });

            $("#btnQuery").on("click",function(){
                var productId = $("#productId").val();
                if (productId == null || productId =='') {
                    layerAlert('请选择产品', '系统提示');
                    $btnAdd.removeAttr("disabled");
                    return false;
                }
                $("#name").rules("remove");
                var url = "${ctx}/provider/md/errorAction/findListForActionCode";
                $("#searchForm").attr("action",url);
                $("#searchForm").submit();
                return false;
            });

            $("#searchForm").validate({
                onfocusout: function(element){
                    $(element).valid();//失去焦点时再验证
                },
                errorPlacement: function (error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });
        });
    </script>
    <style type="text/css">
        .form-horizontal{
            margin-left: 0px;
        }
        .form-horizontal .control-label {
            width: 80px;
        }
        .form-horizontal .controls{
            margin-left: 90px;
        }
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <li><a href="${ctx}/provider/md/errorAction/findList">故障列表</a></li>
    <li><a href="${ctx}/provider/md/errorCode/findList">故障现象添加</a></li>
    <li class="active"><a href="javascript:void(0);">分析处理添加</a></li>
</ul>
<form:form id="searchForm" modelAttribute="errorActionDto" method="post" class="form-horizontal">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <div class="control-group">
        <label class="control-label">产品：</label>
        <div class="controls">
            <select id="productId" name="productId" class="input-small" style="width:250px;">
                <option value=""
                        <c:out value="${(empty errorActionDto.productId)?'selected=selected':''}" />>请选择</option>
                <c:forEach items="${fns:getSingleProductListFromMS()}" var="dict">
                    <option value="${dict.id}" <c:out value="${(errorActionDto.productId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                </c:forEach>
            </select>
            <span class="red">*</span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">故障分类：</label>
        <div class="controls">
            <select id="errorTypeId" name="errorCodeDto.errorTypeId" class="input-small" style="width:250px;">
                <option value="" selected>请选择</option>
                <c:forEach items="${errorTypeList}" var="dict">
                    <option value="${dict.id}" <c:out value="${(errorActionDto.errorCodeDto.errorTypeId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                </c:forEach>
            </select>   &nbsp;
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">故障现象：</label>
        <div class="controls">
            <select id="errorCodeId" name="errorCodeDto.id" class="input-small" style="width:250px;">
                <option value="" selected>请选择</option>
                <c:forEach items="${errorCodeList}" var="dict">
                    <option value="${dict.id}" <c:out value="${(errorActionDto.errorCodeDto.id eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                </c:forEach>
            </select>   &nbsp;
            <input id="btnQuery" class="btn btn-primary" type="button" value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" />
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">故障处理：</label>
        <div class="controls">
            <input id="name" name="actionCodeDto.name" type="text" htmlEscape="false" maxlength="50" style="width:238px"/>
            <span class="red">*</span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">故障分析：</label>
        <div class="controls">
            <input id="analysis" name="actionCodeDto.analysis" type="text" htmlEscape="false" maxlength="50" style="width:238px"/>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">服务类型：</label>
        <div class="controls">
            <select id="serviceTypeId" name="actionCodeDto.serviceTypeId" class="input-small" style="width:250px;">
                <option value="" selected>请选择</option>
                <c:forEach items="${serviceTypeList}" var="dict">
                    <option value="${dict.id}" <c:out value="${(errorActionDto.actionCodeDto.serviceTypeId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                </c:forEach>
            </select>   &nbsp;
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">价格(元)：</label>
        <div class="controls">
            <input id="price" name="price" type="text" htmlEscape="false" maxlength="50" readonly="readonly" style="width:238px"/>
            <shiro:hasPermission name="md:erroraction:edit">
            <input id="btnAdd" class="btn btn-primary" type="button" style="margin-left: 8px;" value="&nbsp;&nbsp;&nbsp;保存&nbsp;&nbsp;&nbsp;" />
            </shiro:hasPermission>
        </div>
    </div>
</form:form>
<sys:message content="${message}" />
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="30">序号</th>
        <th>故障分类</th>
        <th>故障现象</th>
        <th>故障分析</th>
        <th>故障处理</th>
        <th>服务类型</th>
        <th>价格</th>
        <th style="text-align: center;">操作</th>
    </tr>
    </thead>
    <tbody>
    <c:set var="index" value="0"></c:set>
    <c:forEach items="${page.list}" var="entity">
        <tr>
            <c:set var="index" value="${index+1}"></c:set>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${entity.errorCodeDto.errorTypeName}</td>
            <td>${entity.errorCodeDto.name}</td>
            <td>${entity.actionCodeDto.analysis}</td>
            <td>${entity.actionCodeDto.name}</td>
            <td>${entity.actionCodeDto.serviceTypeName}</td>
            <td>${entity.actionCodeDto.price}</td>

            <td style="text-align: center;">
                <shiro:hasPermission name="md:erroraction:edit">
                <a style="margin-left: 6px;" href="${ctx}/provider/md/errorAction/deleteForActionCode?id=${entity.id}&productId=${entity.productId}&actionCodeId=${entity.actionCodeDto.id}&customerId=${customerId}"
                   onclick="return confirmx('将同时删除该故障分析对应的故障处理，确认要删除吗？', this.href)"><i class="icon-delete" style="margin-top: 0px;"></i></a>
                </shiro:hasPermission>
            </td>

        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>

