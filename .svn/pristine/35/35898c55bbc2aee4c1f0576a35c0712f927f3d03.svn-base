<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>配件</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
            height: 30px;
        }
    </style>
    <script type="text/javascript">
        function editMaterial(type,id) {
            var text = "添加配件";
            var url = "${ctx}/md/material/form";
            if(type == 2){
                text = "修改"
                url = "${ctx}/md/material/form?id=" + id;
            }
            top.layer.open({
                type: 2,
                id:"material",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['700px', '480px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

        function deleteMaterials(materialId,materialName) {
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
                        url: "${ctx}/md/material/ajax/delete?id="+materialId,
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
    <%--<style type="text/css">
        th {text-align: center !important;}
        td {text-align: center !important;}
    </style>--%>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">&nbsp;&nbsp;&nbsp;&nbsp;配件&nbsp;&nbsp;&nbsp;&nbsp;</a></li>
    <li><a href="${ctx}/md/materialCategory/list">配件分类</a></li>
    <li><a href="${ctx}/md/material/requirement">照片要求</a></li>
</ul>
<form:form id="searchForm" modelAttribute="material" action="${ctx}/md/material/list" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden"
           value="${page.pageSize}" />
    <label>配件分类：</label>
    <form:select path="materialCategory.id" cssStyle="width: 200px;">
        <form:option value="" label="所有"></form:option>
        <form:options items="${materialCategoryList}" itemLabel="name" itemValue="id"></form:options>
    </form:select>
    &nbsp;
    <label>配件名称：</label>
    <form:input path="name" htmlEscape="false" maxlength="30" value="${name}" class="input-small" />
    &nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="top.$.jBox.tip('正在查询,请稍候...', 'loading');return setPage();" value="查询" />
</form:form>
<shiro:hasPermission name="md:material:edit">
    <button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 100px;height: 30px" onclick="editMaterial(1,null)"><i class="icon-plus-sign"></i>&nbsp;添加配件</button>
</shiro:hasPermission>
<sys:message content="${message}" />
<table id="contentTable"
       class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="40px">序号</th>
        <th width="150px">配件分类</th>
        <th width="200px">配件名称</th>
        <th width="60px">参考价格(元)</th>
        <th width="60px">返件</th>
        <th width="60px">回收配件</th>
        <th width="60px">回收价格(元)</th>
        <th>描述</th>
        <shiro:hasPermission name="md:material:edit">
            <th width="80px">操作</th>
        </shiro:hasPermission>
    </tr>
    </thead>
    <tbody>
    <c:set var="index" value="0"></c:set>
    <c:forEach items="${page.list}" var="material">
        <tr>
            <c:set var="index" value="${index+1}"></c:set>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${material.materialCategory.name}</td>
            <td>${material.name}</td>
            <td><fmt:formatNumber value="${material.price}" pattern="0.0"></fmt:formatNumber></td>
            <c:choose>
                <c:when test="${material.isReturn==0}">
                    <td>否</td>
                </c:when>
                <c:when test="${material.isReturn==1}">
                    <td><font color="red">是</font></td>
                </c:when>
                <c:otherwise>
                    <td>未知</td>
                </c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${material.recycleFlag==0}">
                    <td>否</td>
                </c:when>
                <c:when test="${material.recycleFlag==1}">
                    <td><font color="red">是</font></td>
                </c:when>
                <c:otherwise>
                    <td>未知</td>
                </c:otherwise>
            </c:choose>
            <td><fmt:formatNumber value="${material.recyclePrice}" pattern="0.0"></fmt:formatNumber></td>
            <td>${material.remarks}</td>
            <shiro:hasPermission name="md:material:edit">
                <td>
                    <a href="javascript:void(0)" onclick="editMaterial(2,${material.id})">修改</a>
    &nbsp;&nbsp;
                     <a href="#" onclick="deleteMaterials('${material.id}','${material.name}')">删除</a>
                </td>
            </shiro:hasPermission>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
<script>
    $("th").css({"text-align":"center","vertical-align":"middle"});
    $("td").css({"text-align":"center","vertical-align":"middle"});
    $("td").css({"vertical-align":"middle"});
</script>
</body>
</html>
