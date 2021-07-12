<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>故障现象添加</title>
    <meta name="decorator" content="default" />
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>

    <script type="text/javascript">
        $(document).ready(function() {
            $('a[data-toggle=tooltip]').darkTooltip();
            $(document).on('change',"#productId",function (e) {
                var customerId = $("#customerId").val();
                /*if(customerId ==null || customerId=='' || customerId<=0){
                    layerAlert('请选择客户！','系统提示');
                    return false;
                }*/
                var productId = $(this).val();
                if(productId ==null || productId==''){
                    $("#errorTypeId").html('<option value="" selected>请选择</option>');
                    $("#errorTypeId").val("");
                    $("#errorTypeId").change();
                    $("#contentTable tbody").empty();
                    return false;
                }

                setPage(0);
                $("#name").rules("remove");
                var url = "${ctx}/provider/md/customerErrorCode/findList?productId="+productId+"&customerId="+customerId;
                $("#searchForm").attr("action",url);
                $("#searchForm").submit();
                return false;
            });

            $("#btnSubmit").on("click",function(){
                var customerId = $("#customerId").val();
                if(customerId==null || customerId=='' || customerId<=0){
                    layerAlert('请选择客户！','系统提示');
                    return false;
                }
                var productId = $("#productId").val();
                if(productId ==null || productId==''){
                    layerAlert('请选择产品！','系统提示');
                    return false;
                }
                var errorTypeId = $("#errorTypeId").val();
                if (errorTypeId == null || errorTypeId =='') {
                    layerAlert('请选择故障分类！','系统提示');
                    return false;
                }
                $("#name").rules("add","required");
                if(!$("#searchForm").valid()){
                    return false;
                }
                $("#name").rules("remove");
                top.$.jBox.tip('正在保存,请稍候...', 'loading');
                var url = "${ctx}/provider/md/customerErrorCode/save";
                $("#searchForm").attr("action",url);
                $("#searchForm").submit();
                return false;
            });

            $("#searchForm").validate({
                rules: {
                    name: {remote: {
                        url: "${ctx}/provider/md/customerErrorCode/checkName",
                        type: "get",
                        data:{
                            productId: function() {
                                return $("#productId").val();
                            },
                            errorTypeId: function() {
                                return $("#errorTypeId").val();
                            },
                            customerId:function(){
                                return $("#customerId").val();
                            }
                        }
                    }}
                },
                messages: {
                    name: {remote: "故障现象已存在"}
                },
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

            $(document).on('change','#customerId',function(){
                var customerId =$(this).val();
                if (customerId == "")
                {
                    $("#productId").html('<option value="" selected>请选择</option>');
                    $("#productId").val("");
                    $("#productId").change();
                    //$("#contentTable tbody").empty();
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
            });

        });

        //跳到配置服务区域页面
        function showErrorCode() {
            var customerId = $("#customerId").val();
            var productId = $("#productId").val();
            top.layer.open({
                type: 2,
                id:'layer_errorCode',
                zIndex:19891015,
                title:'故障分类添加',
                content: "${ctx}/provider/md/customerErrorType/findList?productId=" + productId+"&customerId="+customerId,
                area: ['980px', screen.height-350+'px'],
                shade: 0.3,
                shadeClose:true,
                maxmin: false,
                success: function(layero,index) {
                },
                end:function(){
                    var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                    if(iframe != undefined){
                        if (customerId!=null && customerId>0 && productId !=null && productId >0) {
                            iframe.refresh(productId,customerId);
                        }
                    }
                }
            });
        }

        function refresh(productId,customerId) {
            $.ajax({
                url:"${ctx}/provider/md/customerErrorType/ajax/findListByCustomerAndProduct?productId=" + productId+"&customerId="+customerId,
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
        }

        function del(id, productId) {
            $.ajax({
                url:"${ctx}/provider/md/errorAction/ajax/getIdByProductAndErrorCode?productId=" + productId+"&errorCodeId="+id,
                success:function (e) {
                    if(e.success){
                        if (e.data>0) {
                            layerError("该故障现象下有故障处理，不能删除","错误提示");
                        } else {
                            var url = "${ctx}/provider/md/customerErrorCode/delete?id="+id+"&productId="+productId;
                            $("#searchForm").attr("action",url);
                            $("#searchForm").submit();
                            return false;
                        }
                    } else {
                        layerMsg('请求失败！');
                    }
                },
                error:function (e) {
                    layerError("请求失败","错误提示");
                }
            });
        }
        function beforePage(){
            $("#name").rules("remove");
        }
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
    <li><a href="${ctx}/provider/md/customerErrorAction/findList">故障列表</a></li>
    <li class="active"><a href="javascript:void(0);">故障现象添加</a></li>
    <li><a href="${ctx}/provider/md/customerErrorAction/findListForActionCode">分析处理添加</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="searchForm" modelAttribute="errorCode" action="${ctx}/provider/md/customerErrorCode/findList" method="post" class="form-horizontal">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <div class="control-group">
        <label class="control-label">客户：</label>
        <div class="controls">
            <select id="customerId" name="customerId" class="input-small" style="width:250px;">
                <option value="">请选择</option>
                <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                    <option value="${dict.id}"
                        <c:out value="${(errorCode.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                </c:forEach>
            </select>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">产品：</label>
        <div class="controls">
            <select id="productId" name="productId" class="input-small" style="width:250px;">
                <option value=""
                        <c:out value="${(empty errorCode.productId)?'selected=selected':''}" />>请选择</option>
                <c:forEach items="${productList}" var="dict">
                    <option value="${dict.id}" <c:out value="${(errorCode.productId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                </c:forEach>
            </select>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">故障分类：</label>
        <div class="controls">
            <select id="errorTypeId" name="errorTypeId" class="input-small" style="width:250px;">
                <option value="" selected>请选择</option>
                <c:forEach items="${errorTypeList}" var="dict">
                    <option value="${dict.id}" <c:out value="${(errorCode.errorTypeId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                </c:forEach>
            </select>   &nbsp;
            <input id="btnExpressAdd" class="btn btn-default" type="button" onclick="javascript:showErrorCode();" value="快速添加" />
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">故障现象：</label>
        <div class="controls">
            <input id="name" name="name" type="text" htmlEscape="false" maxlength="50" style="width:238px"/>
            <span class="red">*</span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">故障描述：</label>
        <div class="controls">
            <div style="float:left;">
                <textarea id="remarks" name="remarks" style="min-width: 280px;max-width: 560px;min-height: 70px;max-height: 210px;" maxlength="250" class="input-xlarge" rows="3"></textarea>
            </div>
            <div style="float:left;margin-left: 20px;line-height: 80px;">
                <shiro:hasPermission name="md:customererroraction:edit">
                    <input id="btnSubmit" class="btn btn-primary" style="vertical-align: bottom" type="submit" onclick="return setPage();" value="&nbsp;&nbsp;&nbsp;保存&nbsp;&nbsp;&nbsp;" />
                </shiro:hasPermission>
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}" />
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="30">序号</th>
        <th>故障分类</th>
        <th>故障分类代码</th>
        <th>描述</th>
        <th>故障现象</th>
        <th>故障现象代码</th>
       <shiro:hasPermission name="md:customererroraction:edit">
        <th style="text-align: center;">操作</th>
       </shiro:hasPermission>
    </tr>
    </thead>
    <tbody>
    <c:set var="index" value="0"></c:set>
    <c:forEach items="${page.list}" var="entity">
        <tr>
            <c:set var="index" value="${index+1}"></c:set>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${entity.errorTypeName}</td>
            <td>${entity.errorTypeCode}</td>
            <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${entity.errorTypeRemarks}">${fns:abbr(entity.errorTypeRemarks,30)}</a></td>
            <td>${entity.name}</td>
            <td>${entity.code}</td>
            <td style="text-align: center;">
                <shiro:hasPermission name="md:customererroraction:edit">
                    <%--<a style="margin-left: 6px;" href="${ctx}/provider/md/errorCode/delete?id=${entity.id}&productId=${entity.productId}"
                       onclick="return confirmx('确认要删除吗？', this.href)"><i class="icon-delete" style="margin-top: 0px;"></i></a>--%>

                    <a style="margin-left: 6px;" onclick="javascript:del(${entity.id},${entity.productId},'${entity.customerId}');"><i class="icon-delete" style="margin-top: 0px;"></i></a>
                </shiro:hasPermission>
            </td>

        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>

