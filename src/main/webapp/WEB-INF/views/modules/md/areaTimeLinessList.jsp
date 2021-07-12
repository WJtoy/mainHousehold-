<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>区域时效奖励开关</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="${ctx}/md/areaTimeLiness/list?area.parent.id=${areaTimeLiness.area.parent.id}">区域时效奖励开关</a></li>
</ul>
<form:form id="searchForm" modelAttribute="areaTimeLiness" action="${ctx}/md/areaTimeLiness/list" method="POST" class="breadcrumb form-search">
    <input name="area.parent.id" type="hidden" value="${areaTimeLiness.area.parent.id}" />
    <input class="btn btn-primary" type="button" value="启用" onclick="isEnable(1,this)"/>&nbsp;&nbsp;
    <input class="btn btn-warning" type="button" value="停用" onclick="isEnable(0,this)"/>&nbsp;&nbsp;
</form:form>
<sys:message content="${message}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover table-hover">
    <thead>
    <tr>
        <th width="25px"><input type="checkbox" id="selectAll" name="selectAll"></th>
        <th width="50px">序号</th>
        <th>区域名称</th>
        <th>是否开关</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${areaTimeLinessList}" var="entity">
        <c:set var="index" value="${index+1}" />
        <tr>
            <td>
                <input type="checkbox" name="checkedRecords" data-id="${entity.id}" value="${entity.area.id}">
            </td>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${entity.area.name}</td>
            <c:choose>
                <c:when test="${not empty entity.id and entity.isOpen==0}">
                    <td> <span class="label status_100">关</span></td>
                </c:when>
                <c:when test="${not empty entity.id and entity.isOpen==1}">
                    <td><span class="label status_80">开</span></td>
                </c:when>
                <c:otherwise>
                    <td><span class="label status_30">未配置</span></td>
                </c:otherwise>
            </c:choose>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script type="text/javascript">
    $(document).ready(function () {
        $("#selectAll").change(function () {
            var $check = $(this);
            $("input[type=checkbox][name='checkedRecords']:checkbox").each(function () {
                if ($(this).val() != "on") {
                    if ($check.attr("checked") == "checked") {
                        $(this).attr("checked", true);
                    }
                    else {
                        $(this).attr("checked", false);
                    }
                }
            });
        });
    });

    //启用或者停用
    function isEnable(isOpen,obj) {
       var $obj = $(obj);
       var entity =[];
       var id;
       var msg=isOpen==1?"启用":"停用";
       $("input[type=checkbox][name='checkedRecords']:checkbox:checked").each(function () {
            id = $(this).data("id");
           var area = {};
            area.id = $(this).val();
            if(id!=null && id>0){
                entity.push({id:id,area:area,isOpen:isOpen})
            }else{
                entity.push({area:area,isOpen:isOpen})
            }
       })
       if($obj.prop("disabled") == true){
           return false;
       }
       if(entity.length<=0){
           top.layerError( "至少勾选一条数据！", "错误");
           return false;
       }
        var confirmIndex = layer.confirm('您确定要'+msg+'区域时效奖励吗？', {
            btn: ['确定','取消'], //按钮
        }, function(){
            $obj.attr('disabled', 'disabled');
            layer.close(confirmIndex);
            var loadingIndex = layer.msg('正在提交，请稍等...', {
                icon: 16,
                time: 0,
                shade: 0.3
            });
            $.ajax({
                cache: false,
                type: "POST",
                url: "${ctx}/md/areaTimeLiness/enable",
                data: JSON.stringify(entity),
                dataType: 'json',
                contentType: "application/json;charset=utf-8",
                success:function (data) {
                    layer.close(loadingIndex);
                    if (ajaxLogout(data)) {
                        $obj.removeAttr('disabled');
                        return false;
                    }
                    if(data.success){
                        layerMsg("保存成功");
                        repage();
                    }else{
                        setTimeout(function () {
                            $obj.removeAttr('disabled');
                        }, 2000);
                        layerError("数据保存错误:" + data.message, "错误提示");
                    }
                },
                error:function (data) {
                    setTimeout(function () {
                        $obj.removeAttr('disabled');
                    }, 2000);
                    ajaxLogout(data,null,"数据保存错误，请重试!");
                }
            });
        },function(){
            $obj.removeAttr('disabled');
            layer.close(confirmIndex);
            return false;
        });
    }
</script>
</body>
</html>
