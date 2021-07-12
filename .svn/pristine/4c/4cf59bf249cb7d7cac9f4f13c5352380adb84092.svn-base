<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>用户客服管理</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>

    <style>
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
            height: 30px;
        }
    </style>

    <script type="text/javascript">
        function search() {
            $("#pageNo").val(1);
            $("#searchForm").attr("action", "${ctx}/md/keFuRegion/keFuList");
            $("#searchForm").submit();
            return false;
        }
        function editUserKeFu(id) {
            var h = $(top.window).height();
            var text = "添加客服";
            var url = "${ctx}/sys/userKeFu/form";
            var height = (h-200);
            var area = ['1000px', (h-200)+'px'];
            if (id != null){
                text = "修改客服";
                url = "${ctx}/sys/userKeFu/form?userId=" + id;
            }
            top.layer.open({
                type: 2,
                id:"userKeFu",
                zIndex:150,
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

        function removeUser(userId,userName){

            layer.confirm(
                '确认要删除客服' +'<label style="color:#63B9E6">'+ userName +'</label>吗？',
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
                        url: "${ctx}/sys/userKeFu/delete?id=" + userId,
                        success:function (data) {
                            // 提交后的回调函数
                            if(loadingIndex) {
                                setTimeout(function () {
                                    layer.close(loadingIndex);
                                }, 2000);
                            }
                            if (data.success) {
                                layerMsg("删除客服成功");

                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                            } else {
                                layerError("删除客服失败:" + data.message, "错误提示");
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

        function enableUser(userId,statusFlag){
            var title;
            if(statusFlag == 0){
                title = "启用";
            }else {
                title = "停用";
            }

            layer.confirm(
                '确认要'+ title+'该用户吗？',
                {
                    btn: ['确定','取消'], //按钮
                    title:'提示'
                }, function(index){
                    layer.close(index);//关闭本身
                    var loadingIndex = top.layer.msg('正在'+ title +'，请稍等...', {
                        icon: 16,
                        time: 0,//不定时关闭
                        shade: 0.3
                    });
                    $.ajax({
                        url: "${ctx}/sys/user/userEnableDisable?userId="+ userId +"&statusFlag=" + statusFlag,
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
                                layerError(title + "失败:" + data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                        },
                    });
                    return false;
                }, function(){
                    // 取消操作
                });
        }
    </script>
</head>
<body>
    <ul class="nav nav-tabs">
        <li><a href="${ctx}/md/keFuRegion/list">客服区域</a></li>
        <li class="active"><a href="javascript:void(0);">客服账号</a></li>
    </ul>
    <form:form id="searchForm" modelAttribute="user" action="${ctx}/md/keFuRegion/keFuList" method="post"
               class="form-inline">
        <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
        <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
        <sys:tableSort id="orderBy" name="orderBy" value="${page.orderBy}" callback="page();"/>

        <div class="control-group" style="height: 45px;margin-top: 20px;border-bottom: 1px solid #EEEEEE;">

            <label class="margin_line" style="margin-left: 5px">用户名称：</label>
            <form:input path="name" htmlEscape="false" maxlength="50" class="input-medium"/>
            &nbsp;&nbsp;
            <label>登录账号：</label>
            <form:input path="loginName" htmlEscape="false" maxlength="50" class="input-medium"/>
            &nbsp;&nbsp;
            <label class="margin_line">状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态：</label>
            <select id="statusFlag" name="statusFlag" class="input-small" style="width:213px;">
                <option value="-1" <c:out value="${(empty user.statusFlag)?'selected=selected':''}" />>所有</option>
                <option value="0" <c:out value="${(user.statusFlag eq 0)?'selected=selected':''}" />>正常</option>
                <option value="1" <c:out value="${(user.statusFlag eq 1)?'selected=selected':''}" />>停用</option>
            </select>
            &nbsp;&nbsp;
            <label class="margin_line">部&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;门：</label>
                <form:select path="office.id" class="input-xlarge required" cssStyle="width: 250px">
                <form:option value="-1" label="请选择"/>
                <form:options items="${offices}" itemLabel="name" itemValue="id" htmlEscape="false"/>
                </form:select>
            &nbsp;&nbsp;
            <input id="btnSubmit" class="btn btn-primary" type="button" onclick="search();" value="查询"/>
        </div>
    </form:form>
    <shiro:hasPermission name="sys:user:edit">
        <button style="margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 100px;height: 32px" onclick="editUserKeFu()">
            <i class="icon-plus-sign"></i>&nbsp;添加客服
        </button></shiro:hasPermission>
    <sys:message content="${message}"/>
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
        <thead>
        <tr>
            <th>序号</th>
            <th class="sort-column login_name">用户名称</th>
            <th class="sort-column name">登录账号</th>
            <th>联系电话</th>
            <th>部门</th>
            <th>职位</th>
            <th>用户角色</th>
            <th>客服类型</th>
            <th>授权品类</th>
            <shiro:hasPermission name="sys:user:edit">
                <th>操作</th>
            </shiro:hasPermission>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${page.list}" var="user">
            <c:set var="index" value="${index+1}" />
            <tr>
                <td>${index+(page.pageNo-1)*page.pageSize}</td>
                <td>${user.name}</td>
                <td>${user.loginName}</td>
                <td>${user.mobile}</td>
                <td>${user.office.name}</td>
                <td>${user.managerFlag == 1 ? '主管' : "普通客服"}</td>
                <td>${user.roleNames}</td>
                <c:choose>
                    <c:when test="${user.subFlag == 0}">
                        <td>超级客服</td>
                    </c:when>
                    <c:when test="${user.subFlag == 1}">
                        <td>KA客服部</td>
                    </c:when>
                    <c:when test="${user.subFlag == 2}">
                        <td>普通客服部</td>
                    </c:when>
                    <c:when test="${user.subFlag == 3}">
                        <td>突击客服</td>
                    </c:when>
                    <c:when test="${user.subFlag == 4}">
                        <td>自动客服</td>
                    </c:when>
                    <c:when test="${user.subFlag == 5}">
                        <td>大客服部（含KA）</td>
                    </c:when>
                    <c:otherwise>
                        <td></td>
                    </c:otherwise>
                </c:choose>
                <td>${user.productCategoryNames}</td>
                <shiro:hasPermission name="sys:user:edit">
                    <td>
                        <a href="javascript:editUserKeFu('${user.id}')">修改</a>
                        &nbsp
                        <a href="javascript:removeUser('${user.id}','${user.name}')">删除</a>
                        &nbsp
                        <c:choose>
                            <c:when test="${user.statusFlag == 0}">
                                <a href="#" onclick="enableUser('${user.id}', 1)">停用</a>
                            </c:when>
                            <c:otherwise>
                                <a href="#" style="color:red;" onclick="enableUser('${user.id}', 0)">启用</a>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </shiro:hasPermission>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <div class="pagination">${page}</div>
</body>
</html>
