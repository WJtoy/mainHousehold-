<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<html>
<head>
    <title>O</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <link rel="stylesheet" type="text/css" href="http://sandbox.runjs.cn/uploads/rs/55/sjckzedf/lanrenzhijia.css">
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <style type="text/css">
    .ellipsis {
    overflow: hidden; /*自动隐藏文字*/
    text-overflow: ellipsis;/*文字隐藏后添加省略号*/
    white-space: nowrap;/*强制不换行*/
    width: 8em;/*不允许出现半汉字截断*/
    }
    </style>
    <script type="text/javascript" language="javascript">
        $(document).ready(function() {
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});
            $("#btnSubmit").click(function(){
                top.$.jBox.tip('请稍候...', 'loading');
                $("#pageNo").val(1);
                $("#searchForm").attr("action","${ctx}/tmall/rpt/tmallorder/tmallOrderSUMReport");
                $("#searchForm").submit();
            });
            $("#btnExport").click(function(){
                top.$.jBox.confirm("确认要天猫状态数据吗？","系统提示",function(v,h,f){
                    if(v=="ok"){
                        $("#pageNo").val(1);
                        $("#searchForm").attr("action","${ctx}/tmall/rpt/tmallorder/export");
                        $("#searchForm").submit();
                        $("#searchForm").attr("action","${ctx}/tmall/rpt/tmallorder/tmallOrderSUMReport");
                    }
                },{buttonsFocus:1});
                top.$('.jbox-body .jbox-icon').css('top','55px');
            });
        });
    </script>

    <script>
        var data = [];
        var i = 0;
        <c:forEach items="${page.list}" var="item">
            data[i++] = '${item.infoJson}';
        </c:forEach>

        function showJson(index) {
            var json = data[index];
            top.layer.open({
                title:'内容',
                content: json
            });
        }
        function parseJson(index){
            var json = data[index];
            var obj = JSON.parse(json);
            var workcardId = obj.interfaceData.workcardId;
            var quarter = obj.interfaceData.quarter;

            top.layer.open({
                type: 2,
                title:'订单详情',
                content: "/tmall/rpt/tmallorder/orderDetailInfo?workcardId="+ workcardId+ "&quarter=" + (quarter || ''),
                shade: 0.3,
                shadeClose: true,
                area:['1200px','800px'],
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
        <li class="active"><a href="javascript:void(0);">天猫</a></li>
        <li>
            <a href="${ctx}/b2b/rpt/processlog/canboorder">康宝</a>
        </li>
        <li>
            <a href="${ctx}/b2b/rpt/processlog/jdorder">京东</a>
        </li>
        <li>
            <a href="${ctx}/b2b/rpt/processlog/konkaorder">康佳</a>
        </li>
        <li>
            <a href="${ctx}/b2b/rpt/processlog/joyoungorder">九阳</a>
        </li>
        <li><a href="${ctx}/b2b/rpt/processlog/inseorder">樱雪</a></li>
    </ul>

    <form:form id="searchForm" modelAttribute="processlogSearchModel" action="${ctx}/tmall/rpt/tmallorder/tmallOrderSUMReport" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <div>
        <label>接口：</label>
        <select id="sigualName" name="sigualName" class="input-small" style="width:225px;">

            <option value="2" <c:out value="${(2 == processlogSearchModel.sigualName)?'selected=selected':''}" />>所有</option>
            <option value="0" <c:out value="${(0 == processlogSearchModel.sigualName)?'selected=selected':''}" /> >查询工单信息</option>
            <option value="1" <c:out value="${(1 == processlogSearchModel.sigualName)?'selected=selected':''}" /> >查询反馈服务的执行情况</option>
            <%--<c:forEach var="type" items="${actionTypes}">--%>
                <%--<option value="${type.value}" <c:out value="${(type.value == processlogSearchModel.actionType)?'selected=selected':''}" /> >${type.label}</option>--%>
            <%--</c:forEach>--%>

        </select>
        &nbsp;&nbsp;
        <label>状 态：</label>
        <select id="processFlag" name="processFlag" class="input-small" style="width:225px;">
            <c:set var="processFlags" value="<%= com.wolfking.jeesite.ms.tmall.rpt.feign.B2BProcessFlag.values() %>"/>
            <option value="" <c:out value="${(empty processlogSearchModel.processFlag)?'selected=selected':''}" />>所有</option>
            <c:forEach var="type" items="${processFlags}">
                <option value="${type.value}" <c:out value="${(type.value == processlogSearchModel.processFlag)?'selected=selected':''}" /> >${type.label}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;

        <label>传送时间：</label>
        <input id="createDateStart" name="createDateStart" type="text" readonly="readonly" style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${processlogSearchModel.createDateStart}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>

        <label>~</label>
        &nbsp;&nbsp;&nbsp;

        <input id="createDateEnd" name="createDateEnd" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${processlogSearchModel.createDateEnd}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;&nbsp;
        <%--onclick="return setPage();"--%>

        <input id="btnSubmit" class="btn btn-primary" type="button"  value="查询" />
        &nbsp;&nbsp;
        <input id="btnExport" class="btn btn-primary" type="button" value="导出" />
    </div>

</form:form>

    <sys:message content="${message}"/>

    <div id="divGrid" style="overflow-x:hidden;">
        <table id="contentTable"
               class="table table-striped table-bordered table-condensed table-hover">
            <thead>
            <tr>
                <th>序号</th>
                <th>接口</th>
                <th>内容</th>
                <th>创建时间</th>
                <th>状态</th>
                <th>备注</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <c:set var="index" value="0"/>

    <div class="theme-popover-mask"></div>
            <c:forEach items="${page.list}" var="item">
                <c:set var="index" value="${index+1}"/>
                <tr>
                    <td>${index+(page.pageNo-1)*page.pageSize}</td>
                    <td>${item.interfaceName}</td>
                    <td>
                        <%--<a href="javascript:" data-toggle="tooltip"  data-tooltip='${item.infoJson}' >${fns:abbr(item.infoJson,40)}</a>--%>
                        <a href="javascript:showJson(${index-1})" >${fns:abbr(item.infoJson,40)}</a>
                    </td>
                    <td><fmt:formatDate value="${item.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                    <td>${item.processFlag==0?'受理':item.processFlag==1?'执行':item.processFlag==2?'拒绝':item.processFlag==3?'失败':'成功'}</td>
                    <td><a href="javascript:" data-toggle="tooltip"  data-tooltip='${item.processComment}' >${fns:abbr(item.processComment,40)}</a></td>
                    <td>
                    <c:if test="${item.interfaceName eq 'tmall.servicecenter.workcard.status.update'}">
                        <a href="javascript:void(0);" class="btn btn-mini btn-warning" onclick="parseJson(${index-1})">查看</a>
                        <%--<a href="javascript:void(0);" class="btn btn-mini btn-warning" onclick="Order.complain_view('${model.id}','${model.quarter}');">查看</a>--%>
                    </c:if>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
    <div class="pagination">${page}</div>
</body>


</html>
