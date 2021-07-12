<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>照片要求</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <script>
        $(document).ready(function() {
            $("#contentTable").viewer();
        });

        function saveRequirement() {
            var $btnSubmit = $("#btnSubmit");
            if ($btnSubmit.prop("disabled") == true) {
                return false;
            }

            var materialRequire = {};
            var i = 0;
            $("input[name='code']").each(function(){
                var code = $(this).val();
                materialRequire['requirementList['+i+'].code'] = code;
                materialRequire['requirementList['+i+'].visibleFlag'] = $("#visibleFlag-"+code).is(":checked")?1:0;
                materialRequire['requirementList['+i+'].mustFlag'] = $("#mustFlag-"+code).is(":checked")?1:0;
                materialRequire['requirementList['+i+'].remarks'] = $("#remarks-"+code).val();
                materialRequire['requirementList['+i+'].url'] = $("#url-"+code).val();
                i++;
            });

            var loadingIndex = layerLoading('正在提交，请稍候...');
            $btnSubmit.prop("disabled", true);
            $.ajax({
                url:"${ctx}/md/material/ajaxSaveRequirement",
                type:"POST",
                data: materialRequire,
                dataType: "json",
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
                        setTimeout(function () {
                            // cancel();
                        }, 2000);
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
        }
    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li><a href="${ctx}/md/material/list">&nbsp;&nbsp;&nbsp;&nbsp;配件&nbsp;&nbsp;&nbsp;&nbsp;</a></li>
    <li><a href="${ctx}/md/materialCategory/list">配件分类</a></li>
    <li class="active"><a href="javascript:void(0);">照片要求</a></li>
</ul>
<form:form id="inputForm" action="" method="post" class="form-horizontal">
    <sys:message content="${message}" />
</form:form>
<table id="contentTable" class="table table-bordered table-hover table-hover">
    <thead>
        <tr>
            <th width="40px">序号</th>
            <th width="150px">照片名称</th>
            <th>示例图</th>
            <th width="60px">显示</th>
            <th width="60px">必选</th>
            <th>描述</th>
        </tr>
    </thead>
    <tbody>
    <c:set var="index" value="0"></c:set>
    <c:forEach items="${list}" var="material">
        <tr>
            <c:set var="index" value="${index+1}"></c:set>
            <td>
                ${index+(page.pageNo-1)*page.pageSize}
                <input name="code" value="${material.code}" type="hidden" />
                <input id="url-${material.code}" value="${material.url}" type="hidden" />
            </td>
            <td>${material.name}</td>
            <td>
                <img title='点击放大' src='${ctxStatic}/${material.url}'  data-original='${ctxStatic}/${material.url}' style="width: 50px;height: 40px"/>
            </td>
            <td><input id ="visibleFlag-${material.code}" name="visibleFlag" type="checkbox" <c:out value="${material.visibleFlag eq 1?'checked=checked':''}"/> </td>
            <td><input id ="mustFlag-${material.code}" name="mustFlag" type="checkbox" <c:out value="${material.mustFlag eq 1?'checked=checked':''}"/> </td>
            <td><input id="remarks-${material.code}" name="remarks" type="text" value="${material.remarks}" maxlength="100" style="margin-bottom: 0px;width: 97%"></td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<button type="button" class="btn btn-primary" onclick="saveRequirement();">&nbsp;&nbsp;保存&nbsp;&nbsp;</button>
<script>
    $("th").css({"text-align":"center","vertical-align":"middle"});
    $("tbody tr").each(function(){
        $(this).find("td:lt(5)").css({"text-align":"center","vertical-align":"middle"});
    });
</script>
</body>
</html>

