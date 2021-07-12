<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>故障分类添加</title>
    <meta name="decorator" content="default" />
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>

    <script type="text/javascript">
        $(document).ready(function() {
            $('a[data-toggle=tooltip]').darkTooltip();
            $(document).on('change',"#productId",function (e) {
                $("#name").rules("remove");
                var productId = $(this).val();
                if(productId==null || productId==''){
                    $("#contentTable tbody").empty();
                    return false;
                }
                //console.log(productId);
                setPage(0);
                var url = "${ctx}/provider/md/errorType/findList?productId="+productId;
                $("#searchForm").attr("action",url);
                $("#searchForm").submit();
                return false;
            });

            $("#btnSubmit").on("click",function(){
                var productId = $("#productId").val();
                if(productId==null || productId==''){
                    layerAlert('请选择产品！','系统提示');
                    return false;
                }
                $("#name").rules("add","required");
                if(!$("#searchForm").valid()){
                    return false;
                }
                top.$.jBox.tip('正在保存,请稍候...', 'loading');
                var url = "${ctx}/provider/md/errorType/save";
                $("#searchForm").attr("action",url);
                $("#searchForm").submit();
                return false;
            });

            $("#searchForm").validate({
                rules: {
                    name: {remote: "${ctx}/provider/md/errorType/checkName?productId="+$("#productId").val()}
                },
                messages: {
                    name: {remote: "故障分类名称已存在"}
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
        });

        function del(id,productId,customerId) {
            $.ajax({
                url:"${ctx}/provider/md/errorCode/ajax/getIdByProductAndErrorType?productId=" + productId+"&errorTypeId="+id,
                success:function (e) {
                    if(e.success){
                        if (e.data>0) {
                            layerError("该故障分类下有故障现象，不能删除","错误提示");
                        } else {
                            var url = "${ctx}/provider/md/errorType/delete?id="+id+"&productId="+productId +"&customerId="+customerId;
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
    </script>
    <style type="text/css">
        .form-horizontal .control-label {
            width: 80px;
            text-align: left;
        }
        .form-horizontal .controls{
            margin-left: 90px;
        }
    </style>
</head>
<body>
<form:form id="searchForm" modelAttribute="errorType"  method="post" class="form-horizontal">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <div class="control-group" style="padding-top:10px;">
        <label class="control-label">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
        <div class="controls">
            <select id="productId" name="productId" class="input-small" style="width:250px;">
                <option value=""
                        <c:out value="${(empty errorType.productId)?'selected=selected':''}" />>请选择</option>
                <c:forEach items="${fns:getSingleProductListFromMS()}" var="dict">
                    <option value="${dict.id}" <c:out value="${(errorType.productId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
                </c:forEach>
            </select>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">故障分类：</label>
        <div class="controls">
            <input id="name" name="name" type="text" htmlEscape="false" maxlength="50" style="width:238px"/>
            <span class="red">*</span>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">故障分类<br/>描述：</label>
        <div class="controls">
            <textarea id="remarks" name="remarks" style="min-width: 280px;max-width: 560px;min-height: 70px;max-height: 210px;" maxlength="200" class="input-xlarge" rows="3"></textarea>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label"></label>
        <div class="controls">
            <shiro:hasPermission name="md:errortype:edit">
                <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="&nbsp;&nbsp;&nbsp;保存&nbsp;&nbsp;&nbsp;" />
            </shiro:hasPermission>
        </div>
    </div>
    <sys:message content="${message}" />
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
        <thead>
        <tr>
            <th width="30">序号</th>
            <th>故障分类</th>
            <th>故障分类代码</th>
            <th>故障分类描述</th>
            <th style="text-align: center;">操作</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="index" value="0"></c:set>
        <c:forEach items="${page.list}" var="entity">
            <tr>
                <c:set var="index" value="${index+1}"></c:set>
                <td>${index+(page.pageNo-1)*page.pageSize}</td>
                <td>${entity.name}</td>
                <td>${entity.code}</td>
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${entity.remarks}">${fns:abbr(entity.remarks,30)}</a></td>
                <td style="text-align:center;">
                    <shiro:hasPermission name="md:errortype:edit">
                        <%--<a style="margin-left: 6px;" href="${ctx}/provider/md/errorType/delete?id=${entity.id}&productId=${entity.productId}"><i class="icon-delete" style="margin-top: 0px;"></i></a>--%>
                        <a style="margin-left: 6px;" onclick="javascript:del(${entity.id},${entity.productId},'${entity.customerId}')"><i class="icon-delete" style="margin-top: 0px;"></i></a>
                    </shiro:hasPermission>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <div class="pagination">${page}</div>
</form:form>

</body>
</html>


