<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点服务点管理</title>
    <meta name="decorator" content="default" />
    <%@include file="/WEB-INF/views/include/dialog.jsp"%>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <style type="text/css">
        .sort {color: #0663A2;cursor: pointer;}
        .form-horizontal .control-label {width: 70px;}
        .form-horizontal .controls { margin-left: 80px;}
        .form-search .ul-form li label {width: auto;}
    </style>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript">
        top.layer.closeAll();
        $(document).ready(function() {
            $("#treeTable").treeTable({expandLevel : 5});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});
            $("#servicePointId").on("change",function(e){
                if (e.target.value == ""){
                    $("[id^='servicePointNo']").val("");
                }
            });

            var $btnSyncAutoPlan = $("#btnSyncAutoPlan");
            $("#btnSyncAutoPlan").click(function(){
                top.$.jBox.tip('正在提交,请稍候...', 'loading');
                clickTag = 1;
                $btnSyncAutoPlan.attr('disabled', 'disabled');
                $.ajax({
                    cache: false,
                    type: "POST",
                    url: "${ctx}/md/servicePointAutoPlan/syncAutoPlan",
                    success: function (data) {
                        top.$.jBox.closeTip();
                        if(ajaxLogout(data)){
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSyncAutoPlan.removeAttr('disabled');
                            }, 2000);
                            return false;
                        }
                        if (data.success) {
                            layerMsg("同步成功");
                            $btnSyncAutoPlan.removeAttr('disabled');
                        } else {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSyncAutoPlan.removeAttr('disabled');
                            }, 2000);
                            layerError("同步数据错误:" + data.message, "错误提示");
                        }
                    },
                    error: function (data) {
                        setTimeout(function () {
                            $(this).removeAttr('disabled');
                        }, 2000);
                        ajaxLogout(data, null, "同步数据错误，请重试!");
                    }
                });
            })
        });

        function pointSelect_callback(data){
            $("[id^='servicePointNo']").val(data.servicePointNo);
        }

        function editServicePointAutoPlan(id) {
            var text = "服务区域";
            var url = "${ctx}/md/servicePointAutoPlan/batchForm?id="+id;
            top.layer.open({
                type: 2,
                id:"servicePointAutoPlanBatchForm",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['1200px', '888px'],
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
    <li class="active"><a href="javascript:;">服务区域列表</a></li>
    <%--
    <shiro:hasPermission name="md:servicepointstation:edit">
        <li><a href="${ctx}/md/servicepointstation/form?servicePoint.id=${servicePointStation.servicePoint.id}">网点服务点添加</a></li>
    </shiro:hasPermission>
    --%>
    <li><a href="${ctx}/md/servicepointstation/areaStationList">区域服务点列表</a></li>
    <li><a href="${ctx}/md/servicepointstation/amap">地图</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }" />
<c:set var="isSystemUser" value="${currentuser.isSystemUser()}" />
<form:form id="searchForm" modelAttribute="servicePoint" action="${ctx}/md/servicepoint/selectForStation" method="POST" class="breadcrumb form-search">
    <form:hidden path="firstSearch" />
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <input id="orderBy" name="orderBy" type="hidden" value="${orderBy}" />
    <ul class="ul-form">
        <li>
            <label>网点编号：</label>
            <form:input path="servicePointNo" htmlEscape="false" class="input-small" maxlength="20" />
        </li>
        <li>
            <label>网点名称：</label>
            <c:choose>
                <c:when test="${isSystemUser}">
                    <%--<md:pointselectlayer id="servicePoint" name="id" value="${servicePoint.id}" labelName="name" labelValue="${servicePoint.name}"--%>
                                         <%--width="1200" height="780" callbackmethod="pointSelect_callback" title="选择网点" areaId="" cssClass="required" allowClear="true"/>--%>

                    <%--<md:mdServicePointSelector id="servicePoint" name="id" value="${servicePoint.id}"--%>
                                               <%--labelName="name" labelValue="${servicePoint.name}"--%>
                                               <%--width="1200" height="780" noblackList="true" callbackmethod="" cssClass="required"/>--%>
                    <form:input path="name" htmlEscape="false" maxlength="30" class="input-small"/>
                </c:when>
                <c:otherwise>
                    <form:input path="name" readonly="true" htmlEscape="false" class="input-small" />
                    <form:hidden path="id" />
                </c:otherwise>
            </c:choose>
        </li>
        <li>
            <label>网点电话：</label>
            <form:input path="contactInfo1" htmlEscape="false" class="input-small" maxlength="20" />
        </li>

        <%--<li>--%>
            <%--<label style="margin-left: 40px">区域：</label>--%>
                <%--&lt;%&ndash;--%>
                <%--<sys:treeselectarea id="area" name="area.id" value="${servicePoint.area.id}" labelName="area.name" labelValue="${servicePoint.area.name}" title="区域"--%>
                                <%--url="/sys/area/treeData" nodesLevel="2" nameLevel="3" cssStyle="width:140px;" cssClass="required" allowClear="true"/>--%>
                <%--&ndash;%&gt;--%>
            <%--<sys:treeselect id="area" name="area.id" value="${servicePoint.area.id}" labelName="area.name" labelValue="${servicePoint.area.name}" title="区域"--%>
                            <%--url="/sys/area/treeData" nodesLevel="2" nameLevel="3" cssStyle="width:140px;" cssClass="required" allowClear="true"/>--%>

        <%--</li>--%>
        <%--
        <li>
            <label style="margin-left: 40px">自动派单：</label>
            <form:select path="autoPlanFlag" class="input-small">
                <form:option value="-1" label="所有" />
                <form:options items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" />
            </form:select>
        </li>
        --%>
        <li class="btns">
            <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
            <shiro:hasPermission name="md:servicepointautoplan:sync">
            <input id="btnSyncAutoPlan" class="btn btn-success" type="button" value="同步自动派单" />
            </shiro:hasPermission>
        </li>
        <li class="clearfix"></li>
    </ul>
</form:form>
<sys:message content="${message}" />
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover table-hover">
    <thead>
    <tr>
        <th width="55">序号</th>
        <th width="150">网点编号</th>
        <th>网点名称</th>
        <th>负责人</th>
        <th>自动派单</th>
        <th width="55">等级</th>
        <th width="260">市/区(县)</th>
        <th width="310">街道</th>
        <%--<th width="160">服务半径(米)</th>--%>
        <th width="160">操作</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${page.list}" var="servicePoint">
        <c:set var="index" value="${index+1}" />
        <tr id="${servicePoint.id}" pId="0">
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${servicePoint.servicePointNo}</td>
            <td>${servicePoint.name}</td>
            <td>${servicePoint.primary.name}</td>
            <td>
                <c:choose>
                    <c:when test="${servicePoint.autoPlanFlag eq 0}">
                        否
                    </c:when>
                    <c:otherwise>
                        是
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                ${servicePoint.level.label}
            </td>
            <td></td>
            <td></td>
            <%--<td></td>--%>
            <td>
                <shiro:hasPermission name="md:servicepointautoplan:view">
                    <%--<a href="${ctx}/md/servicepointstation/batchForm?id=${servicePoint.id}">服务区域</a>
                    <br>--%>
                    <a href="javascript:void(0);" onclick="editServicePointAutoPlan('${servicePoint.id}')">服务区域</a>
                </shiro:hasPermission>
            </td>
        </tr>
        <%--
        <c:forEach  var="i" begin="0" end="${servicePoint.areas.size()-1}">
            <tr id="${servicePoint.areas.get(i).id}" pId="${servicePoint.id}">
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <c:if test="${i eq 0}">
                <td rowspan="${servicePoint.areas.size()}">
                        ${servicePoint.areas.get(i).parent.name}
                </td>
            </c:if>
            <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${servicePoint.areas.get(i).fullName}">${fns:abbr(servicePoint.areas.get(i).name,25)}</a></td>
            <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${servicePoint.areas.get(i).remarks}">${fns:abbr(servicePoint.areas.get(i).remarks,50)}</a></td>
            <td></td>
            <td></td>
            </tr>
        </c:forEach>
        --%>
        <c:forEach items="${servicePoint.areas}" var="item">
            <c:set var="itemindex" value="${itemindex+1}" />
            <tr id="${item.id}" pId="${servicePoint.id}">
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.fullName}">${item.parent.name}${fns:abbr(item.name,25)}</td>
                <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.remarks}">${fns:abbr(item.remarks,50)}</a></td>
                <%--<td></td>--%>
                <td></td>
            </tr>
        </c:forEach>
    </c:forEach>

    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>

