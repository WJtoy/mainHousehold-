<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客戶管理</title>
    <meta name="decorator" content="default" />
    <script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
    <script src="${ctxStatic}/js/ajaxfileupload.js"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript">
        $(document).ready(function () {
            var w = $(window).width();

            var pagestyle = function() {
                var width = $(window).width() -0;
                $("#treeTable_tableLayout").css("width",width);
            }

            //注册窗体改变大小事件
            $(window).resize(pagestyle);
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
        });
        function editCustomer(type,id) {
            var text = "添加客户";
            var url = "${ctx}/md/customerNew/form?sort=" + type;
            var area = ['1000px', '888px'];
            if(type == 20){
                text = "修改客户";
                url = "${ctx}/md/customerNew/form?id=" + id;
            }
            top.layer.open({
                type: 2,
                id:"customer",
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


        function enableCustomer(customerId,name){
            var loadingIndex;
            $.ajax({
                url: "${ctx}/md/customerNew/selectCustomerOrder?id="+ customerId,
                success:function (data) {
                    // 提交后的回调函数
                    if (data.success) {
                        layer.confirm(
                            '确认要删除客户<label style="color:#63B9E6">'+name +'</label>吗？',
                            {
                                btn: ['确定','取消'], //按钮
                                title:'提示',
                            }, function(index){
                                layer.close(index);//关闭本身
                                loadingIndex = top.layer.msg('正在删除，请稍等...', {
                                    icon: 16,
                                    time: 0,//不定时关闭
                                    shade: 0.3
                                });
                                $.ajax({
                                    url: "${ctx}/md/customerNew/ajax/delete?id="+ customerId,
                                    success:function (data) {
                                        // 提交后的回调函数
                                        if(loadingIndex) {
                                            setTimeout(function () {
                                                // layer.close(loadingIndex);
                                            }, 2000);
                                        }
                                        if (data.success) {
                                            layerMsg(data.message);

                                            layerMsg("删除" + name + "成功");

                                            var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                            if(pframe){
                                                pframe.repage();
                                            }
                                        } else {
                                            layerError("删除"+name+"失败:" + data.message, "错误提示");
                                        }
                                        return false;
                                    },
                                    error: function (data) {
                                        layer.close(loadingIndex);
                                        ajaxLogout(data,null,"数据保存错误，请重试!");
                                    }
                                });
                                return false;
                            });
                    } else {
                        layerAlert("客户<label style=\"color:#63B9E6\">"+name +"</label>已有工单，不能删除", "提示");
                    }
                    return false;
                },
                error: function (data) {

                    ajaxLogout(data,null,"数据删除错误，请重试!");
                }
            });
        }
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:;">客戶</a></li>
</ul>
<sys:message content="${message}" type="loading"/>
<shiro:hasPermission name="md:customer:view">
<form:form id="searchForm" modelAttribute="mdCustomer" action="${ctx}/md/customerNew/list" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <%--
    <label>编号：</label>
    <input id="code" name="code" type="text" htmlEscape="false" maxlength="30" value="${mdCustomer.code}"/>
    --%>
    <label>客户：</label>
    <form:select path="id" class="input-large">
        <form:option value="" label="所有"/>
        <form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false" />
    </form:select>
    <label>联系电话：</label>
    <form:input path="phone" htmlEscape="false" type="number" maxlength="20" class="input-small" />
    <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
</form:form>
    <button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 90px;height: 30px" onclick="editCustomer(10,null)">
        <i class="icon-plus-sign"></i>&nbsp;添加客户
    </button>
<table id="contentTable"
       class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50">序号</th>
        <th width="60">编号</th>
        <th width="300">客户</th>
        <th width="80">负责人</th>
        <th width="80">业务</th>
        <th width="80">跟单</th>
        <th width="180">默认品牌</th>
        <th width="80">客户下单</th>
        <th width="80">短信发送</th>
        <th width="80">时效奖励</th>
        <th width="80">催单</th>
        <th width="80">加急</th>
        <th width="80">远程费用</th>
        <th width="80">VIP客户</th>
        <th style="width:15%">描述</th>
        <shiro:hasPermission name="md:customer:edit">
            <th width="100">操作</th>
        </shiro:hasPermission>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${page.list}" var="mdCustomer">
        <c:set var="index" value="${index+1}" />
        <tr>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${mdCustomer.code}</td>
            <td>${mdCustomer.name}</td>
            <td>${mdCustomer.master}</td>
            <td>${mdCustomer.salesName}</td>
            <td>${mdCustomer.merchandiserName}</td>
            <td>${mdCustomer.defaultBrand}</td>
            <td>
                <c:choose>
                    <c:when test="${mdCustomer.effectFlag == 1}">
                        开启
                    </c:when>
                    <c:otherwise>
                        <label style="color: red">关闭</label>
                    </c:otherwise>
                </c:choose></td>
            <td>
                <c:choose>
                    <c:when test="${mdCustomer.shortMessageFlag == 1}">
                        开启
                    </c:when>
                    <c:otherwise>
                        <label style="color: red">关闭</label>
                    </c:otherwise>
                </c:choose></td>
            <td>
                <c:choose>
                    <c:when test="${mdCustomer.timeLinessFlag == 1}">
                        开启
                    </c:when>
                    <c:otherwise>
                        <label style="color: red">关闭</label>
                    </c:otherwise>
                </c:choose></td>
            <td>
                <c:choose>
                    <c:when test="${mdCustomer.reminderFlag == 1}">
                        开启
                    </c:when>
                    <c:otherwise>
                        <label style="color: red">关闭</label>
                    </c:otherwise>
                </c:choose></td>
            <td>
                <c:choose>
                    <c:when test="${mdCustomer.urgentFlag == 1}">
                        开启
                    </c:when>
                    <c:otherwise>
                        <label style="color: red">关闭</label>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:choose>
                    <c:when test="${mdCustomer.remoteFeeFlag == 1}">
                        开启
                    </c:when>
                    <c:otherwise>
                        <label style="color: red">关闭</label>
                    </c:otherwise>
                </c:choose></td>
            <td>
                <c:choose>
                    <c:when test="${mdCustomer.vipFlag == 1}">
                        <label style="color: red">${mdCustomer.vipName}</label>
                    </c:when>
                    <c:otherwise>
                        否
                    </c:otherwise>
                </c:choose>
            </td>
            <td><a href="javascript:void(0);" title="${mdCustomer.remarks}">${fns:abbr(mdCustomer.remarks,30)}</a></td>
            <shiro:hasPermission name="md:customer:edit">
                <td>
                    <a href="javascript:editCustomer(20,'${mdCustomer.id}')">修改</a>
                    <shiro:hasPermission name="md:customer:detelete">
                        <a href="#" onclick="enableCustomer('${mdCustomer.id}','${mdCustomer.name}')">删除</a>
                    </shiro:hasPermission>
                </td>
            </shiro:hasPermission>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</shiro:hasPermission>
</body>
</html>
