<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>服务网点管理</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/dialog.jsp" %>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <%@include file="/WEB-INF/views/include/treeview.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <style type="text/css">
        .sort {color: #0663A2;cursor: pointer;}
        .form-horizontal .control-label {width: 70px;}
        .form-horizontal .controls {margin-left: 80px;}
        .form-search .ul-form li label {width: auto;}
        .flex-container {display: -webkit-flex;display: flex;width: 100%;margin-bottom: 15px;}
        .flex-item {}
        .flex-item-line {margin-right: 15px;}
        tr{height: 88px;}
        #td_left{text-align: left!important;}
        .admin_button {
            margin-top: -6px;
            margin-bottom: 15px;
            border-radius: 4px;
            border: 1px solid;
            border-color: #C0C0C0;
            background-color: rgb(238, 238, 238);
            width: 128px;
            height: 30px
        }
        .fullPaid{
            margin-left: 15px;
            width: 48px;
            height: 20px;
            line-height: 20px;
            border-radius: 2px;
            background-color: rgba(0, 150, 218, 100);
            color: rgba(255, 255, 255, 100);
            font-size: 12px;
            text-align: center;
            border: 0px solid rgba(255, 255, 255, 100);
        }
        .notFullPaid{
            margin-left: 15px;
            width: 48px;
            height: 20px;
            line-height: 20px;
            border-radius: 2px;
            background-color: rgba(255, 149, 2, 100);
            color: rgba(255, 255, 255, 100);
            font-size: 12px;
            text-align: center;
            border: 0px solid rgba(255, 255, 255, 100);
        }
        .notPayCost{
            margin-left: 15px;
            width: 48px;
            height: 20px;
            line-height: 20px;
            border-radius: 2px;
            background-color: rgba(245, 65, 66, 100);
            color: rgba(255, 255, 255, 100);
            font-size: 12px;
            text-align: center;
            border: 0px solid rgba(255, 255, 255, 100);
        }
    </style>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript">
        top.layer.closeAll();
        $(document).ready(function () {

            $("#contentTable").treeTable({expandLevel: 2});
            // 表格排序
            var orderBy = $("#orderBy").val().split(" ");
            $("#contentTable th.sort").each(function () {
                if ($(this).hasClass(orderBy[0])) {
                    orderBy[1] = orderBy[1] && orderBy[1].toUpperCase() == "DESC" ? "down" : "up";
                    $(this).html($(this).html() + " <i class=\"icon icon-arrow-" + orderBy[1] + "\"></i>");
                }
            });
            $("#contentTable th.sort").click(function () {
                var order = $(this).attr("class").split(" ");
                var sort = $("#orderBy").val().split(" ");
                for (var i = 0; i < order.length; i++) {
                    if (order[i] == "sort") {
                        order = order[i + 1];
                        break;
                    }
                }
                if (order == sort[0]) {
                    sort = (sort[1] && sort[1].toUpperCase() == "DESC" ? "ASC" : "DESC");
                    $("#orderBy").val(order + " DESC" != order + " " + sort ? "" : order + " " + sort);
                } else {
                    $("#orderBy").val(order + " ASC");
                }
                page();
            });

            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity: 'east'});
        });

        //查看网店备注历史列表
        function viewRemarkList(servicePointId, servicePointNo, servicePointName) {
            var planIndex = top.layer.open({
                type: 2,
                id: 'layer_planRemarkList_view',
                zIndex: 19891016,
                title: '历史备注',
                content: "${ctx}/provider/md/servicePointNew/viewRemarkList?servicePointId=" + (servicePointId || '') + "&servicePointNo=" + (servicePointNo || '') + "&servicePointName=" + (servicePointName || ''),
                // area: ['980px', '640px'],
                area: ['936px', (screen.height / 2) + 'px'],
                shade: 0.3,
                shadeClose: true,
                maxmin: false,
                success: function (layero, index) {
                },
                end: function () {
                }
            });
        }

        function editServicePoint(id){
            var text = "添加服务网点";
            var url = "${ctx}/provider/md/servicePointNew/form";
            if(id != null){
                text = "修改";
                url = "${ctx}/provider/md/servicePointNew/newForm?id=" + id;
            }
            var area = ['1000px', '825px'];
            top.layer.open({
                type: 2,
                id:"brand",
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

        function removeServicePoint(id,name){
            layer.confirm(
                '确认要删除网点' +'<label style="color:#63B9E6">'+ name +'</label>吗？',
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
                        url: "${ctx}/md/servicepoint/ajax/delete?id="+id+"&type=listDelete",
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

        function synchronizationServicePoint(id){
            layer.confirm(
                '确认要同步网点数据到服务器吗？',
                {
                    btn: ['确定','取消'], //按钮
                    title:'提示'
                }, function(index){
                    layer.close(index);//关闭本身
                    var loadingIndex = top.layer.msg('正在同步，请稍等...', {
                        icon: 16,
                        time: 0,//不定时关闭
                        shade: 0.3
                    });
                    $.ajax({
                        url: "${ctx}/md/servicepoint/ajax/syncDataToEs?id=" + id,
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
                                layerError("同步失败:" + data.message, "错误提示");
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
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:;">服务网点</a></li>
    <%--<shiro:hasPermission name="md:servicepoint:edit">--%>
    <%--<li><a href="${ctx}/md/servicepoint/form">服务网点添加</a></li>--%>
    <%--</shiro:hasPermission>--%>
    <li><a href="${ctx}/md/servicepoint/disableList">停用网点</a></li>
</ul>
<sys:message content="${message}" type="loading"/>
<shiro:hasPermission name="md:servicepoint:view">
    <form:form id="searchForm" modelAttribute="servicePoint" action="${ctx}/provider/md/servicePointNew" method="POST"
               class="breadcrumb form-search" cssStyle="border-bottom: 1px solid #EEEEEE;padding-bottom: 5px;margin-top: 15px;">
        <form:hidden path="firstSearch"/>
        <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
        <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
        <input id="orderBy" name="orderBy" type="hidden" value="${servicePoint.orderBy}"/>

        <div class="flex-container" style="margin-left: -10px;margin-top: 7px;margin-bottom: 7px;">

            <div class="flex-item-line">

                <label>网点编号：</label>
                <form:input path="servicePointNo" htmlEscape="false" maxlength="20" class="input-small"
                            cssStyle="width: 166px"/>
            </div>

            <div class="flex-item-line">
                <label>网点名称：</label>
                <form:input path="name" htmlEscape="false" maxlength="30" class="input-small" cssStyle="width: 266px"/>
            </div>



            <div class="flex-item-line">
                <label>网点电话：</label>
                <form:input path="contactInfo1" htmlEscape="false" maxlength="20" class="input-small"
                            cssStyle="width: 166px"/>
            </div>

            <div class="flex-item-line">
                <label>帐&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;号：</label>
                <form:input path="finance.bankNo" htmlEscape="false" maxlength="20" class="input-small" cssStyle="width: 166px"/>
            </div>

<%--            <div class="flex-item">--%>
<%--                <label>网点开发：</label>--%>
<%--                <form:input path="developer" htmlEscape="false" maxlength="20" class="input-small"--%>
<%--                            cssStyle="width: 200px"/>--%>
<%--            </div>--%>

            <div class="flex-item-line">
                <label>支付异常：</label>
                <form:select path="finance.bankIssue.value" class="input-small" cssStyle="width: 250px;height: 30px">
                    <form:option value="" label="所有"/>
                    <form:options items="${fns:getDictListFromMS('BankIssueType')}" itemLabel="label" itemValue="value"
                                  htmlEscape="false"/><%-- 切换为微服务 --%>
                </form:select>
            </div>

            <div class="flex-item">
                <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="top.$.jBox.tip('正在查询,请稍候...', 'loading');return setPage();" value="查询" />
            </div>
        </div>

<%--        <div class="flex-container">--%>
<%--            <div class="flex-item-line">--%>
<%--                <label class="control-label">开户银行：</label>--%>
<%--                <form:select path="finance.bank.value" class="input-small" cssStyle="width: 295px">--%>
<%--                    <form:option value="" label="所有" />--%>
<%--                    <form:options items="${fns:getDictListFromMS('banktype')}" itemLabel="label" itemValue="value" htmlEscape="false" />&lt;%&ndash;切换为微服务&ndash;%&gt;--%>
<%--                </form:select>--%>
<%--            </div>--%>

<%--            <div class="flex-item">--%>
<%--                <label>账&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;号：</label>--%>
<%--                <form:input path="finance.bankNo" htmlEscape="false" maxlength="20" class="input-small" cssStyle="width: 200px"/>--%>
<%--            </div>--%>

<%--            <div class="flex-item">--%>
<%--                <label class="control-label">分&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;类：</label>--%>
<%--                <form:select path="degree" class="input-small" cssStyle="width: 213px">--%>
<%--                    <form:option value="0" label="所有" />--%>
<%--                    <form:options items="${fns:getDictListFromMS('degreeType')}" itemLabel="label" itemValue="value" htmlEscape="false" />--%>
<%--                </form:select>--%>
<%--            </div>--%>

<%--            <div class="flex-item">--%>
<%--                <label>产品品类：</label>--%>
<%--                <select id="productCategory" name="productCategory" class="input-small" style="width:213px;">--%>
<%--                    <option value="0" <c:out value="${(empty servicePoint.productCategory)?'selected=selected':''}" />>所有</option>--%>
<%--                    <c:forEach items="${fns:getProductCategories()}" var="dict">--%>
<%--                        <option value="${dict.id}" <c:out value="${(servicePoint.productCategory eq dict.id)?'selected=selected':''}" />>${dict.name}</option>--%>
<%--                    </c:forEach>--%>
<%--                </select>--%>
<%--            </div>--%>

<%--            <shiro:hasPermission name="md:servicepoint:autocomplete">--%>
<%--                <div class="flex-item">--%>
<%--                    <label>自动完工：</label>--%>
<%--                    <form:select path="autoCompleteOrder" class="input-small" cssStyle="width: 200px">--%>
<%--                        <form:option value="-1">所有</form:option>--%>
<%--                        <form:option value="1">是</form:option>--%>
<%--                    </form:select>--%>
<%--                </div>--%>
<%--            </shiro:hasPermission>--%>
<%--        </div>--%>

<%--        <div class="flex-container">--%>
<%--            <div class="flex-item-line">--%>
<%--                <label>区&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;域：</label>--%>
<%--                <sys:areaselect name="area.id" id="area" value="${servicePoint.area.id}"--%>
<%--                                labelValue="${servicePoint.area.name}" labelName="area.name" title="区域"--%>
<%--                                mustSelectCounty="true" cssClass="required" cssStyle="width:240px;">--%>
<%--                </sys:areaselect>--%>
<%--            </div>--%>

<%--            <div class="flex-item">--%>
<%--                <label>手机接单：</label>--%>
<%--                <form:select path="appFlag" class="input-small" cssStyle="width: 213px">--%>
<%--                    <form:option value="-1">所有</form:option>--%>
<%--                    <form:option value="1">是</form:option>--%>
<%--                    <form:option value="0">否</form:option>--%>
<%--                </form:select>--%>
<%--            </div>--%>

<%--            <shiro:hasPermission name="md:servicepoint:bank">--%>
<%--                <div class="flex-item">--%>
<%--                    <label>开&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;票：</label>--%>
<%--                    <form:select path="finance.invoiceFlag" class="input-small" cssStyle="width: 213px">--%>
<%--                        <form:option value="-1">所有</form:option>--%>
<%--                        <form:option value="1">是</form:option>--%>
<%--                        <form:option value="0">否</form:option>--%>
<%--                    </form:select>--%>
<%--                </div>--%>

<%--                <div class="flex-item">--%>
<%--                    <label>扣&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;点：</label>--%>
<%--                    <form:select path="finance.discountFlag" class="input-small" cssStyle="width: 213px">--%>
<%--                        <form:option value="-1">所有</form:option>--%>
<%--                        <form:option value="1">是</form:option>--%>
<%--                        <form:option value="0">否</form:option>--%>
<%--                    </form:select>--%>
<%--                </div>--%>
<%--            </shiro:hasPermission>--%>

<%--            <div class="flex-item">--%>
<%--                <label>签&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;约：</label>--%>
<%--                <form:select path="signFlag" class="input-small" cssStyle="width: 200px">--%>
<%--                    <form:option value="-1" label="所有" />--%>
<%--                    <form:options items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" />&lt;%&ndash;切换为微服务&ndash;%&gt;--%>
<%--                </form:select>--%>
<%--            </div>--%>
<%--        </div>--%>

<%--        <div class="flex-container">--%>
<%--            <div class="flex-item-line">--%>
<%--                <label class="control-label">状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态：</label>--%>
<%--                <form:select path="status.value" class="input-small" cssStyle="width: 295px">--%>
<%--                    <form:option value="0" label="所有" />--%>
<%--                    <form:options items="${fns:getDictListFromMS('service_point_status')}" itemLabel="label" itemValue="value" htmlEscape="false" />--%>
<%--                </form:select>--%>
<%--            </div>--%>

<%--            <div class="flex-item">--%>
<%--                <label>完成单数：</label>--%>
<%--                <input id="orderCount" name="orderCount" type="number" value="${servicePoint.orderCount}" class="required number" style="width: 200px;" onkeyup="this.value=this.value.replace(/\D/g,'')" onafterpaste="this.value=this.value.replace(/\D/g,'')"/>--%>
<%--            </div>--%>

<%--            <div class="flex-item">--%>
<%--                <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="top.$.jBox.tip('正在查询,请稍候...', 'loading');return setPage();" value="查询" />--%>
<%--            </div>--%>
<%--        </div>--%>
    </form:form>

    <shiro:hasPermission name="md:servicepoint:edit">
        <button class="admin_button" style="margin-top: 15px" onclick="editServicePoint(null)">
            <i class="icon-plus-sign"></i>&nbsp;添加服务网点
        </button>
    </shiro:hasPermission>

    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
        <thead>
        <tr style="height: 40px">
            <th width="56">序号</th>
            <th width="88">网点编号</th>
            <th width="120">网点名称</th>
            <th width="160">网点地址</th>
            <th width="67">接单量</th>
            <th width="82">网点状态</th>
            <th width="56">网点等级</th>
            <th width="72">手机接单</th>
            <shiro:hasPermission name="md:servicepoint:autocomplete">
                <th width="72">自动完工</th>
            </shiro:hasPermission>
            <shiro:hasPermission name="md:servicepoint:bank">
                <th width="56">扣点</th>
                <th width="56">开票</th>
                <th width="84">质保等级<br>已缴金额(元)</th>
                <th width="160">账户信息</th>
                <th width="80">支付异常</th>
                <th width="72">网点开发</th>
            </shiro:hasPermission>
            <th width="104">覆盖区域</th>
            <th width="96">产品品类</th>
            <th width="72">网点分类</th>
            <th width="108">备注</th>
            <%--<th width="50" style="border-left-style: none">&nbsp;&nbsp;&nbsp;&nbsp;</th>--%>
            <shiro:hasPermission name="md:servicepoint:edit">
                <th width="80">操作</th>
            </shiro:hasPermission>
        </tr>
        </thead>

        <tbody>
        <c:forEach items="${page.list}" var="point">
            <c:set var="index" value="${index+1}"/>
            <tr id="${point.id}">
                <td>${index+(page.pageNo-1)*page.pageSize}</td>
                <td>${point.servicePointNo}</td>
<%--                <td>${point.name}</td>--%>
                <c:choose>
                    <c:when test="${point.finance.bankIssue==null || point.finance.bankIssue.value == '0'}">
                        <td><a href="javascript:void(0);" title="${point.name}">${fns:abbr(point.name,35)}</a></td>
                    </c:when>
                    <c:otherwise>
                        <td><a href="javascript:" data-toggle="tooltip"
                               data-tooltip="${point.finance.bankIssue.label}"><label id="lbltotalCharge" class="alert alert-error" style="width: 80px;padding-right: 15px;">${point.name}</label></a>
                        </td>
                    </c:otherwise>
                </c:choose>
                <td><a href="javascript:" data-toggle="tooltip"
                       data-tooltip="${point.address}">${point.address}</a></td>
                <td>${point.orderCount}</td>
                <td>${fns:getDictLabelFromMS(point.status.value, 'service_point_status', '')}</td>
                <td>${point.level.label}</td>
                <td style="color: <c:out value="${point.appFlag == 1 ?'':'#F54142'}"/>">${point.appFlag == 1 ? '是' : '否'}</td>
                <shiro:hasPermission name="md:servicepoint:autocomplete">
                    <td style="color: <c:out value="${point.autoCompleteOrder == 1 ?'':'#F54142'}"/>">${point.autoCompleteOrder == 1 ? '是' : '否'}</td>
                </shiro:hasPermission>
                <shiro:hasPermission name="md:servicepoint:bank">
                    <td style="color: <c:out value="${point.finance.discountFlag == 1 ?'':'#F54142'}"/>">
                        ${point.finance.discountFlag == 1 ? '是' : '否'}
                    </td>
                    <td style="color: <c:out value="${point.finance.invoiceFlag == 1 ?'':'#F54142'}"/>">
                        ${point.finance.invoiceFlag == 1 ? '是' : '否'}
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${point.mdDepositLevel.name == null || point.mdDepositLevel.name == '' && point.mdDepositLevel.maxAmount == null || point.mdDepositLevel.maxAmount == 0.0}">
                                <span style="color:red;">无</span>
                            </c:when>
                            <c:otherwise>
                                ${point.mdDepositLevel.name}<br>
                                ${point.finance.deposit}<br>
                                <c:choose>
                                    <c:when test="${point.deposit > point.finance.deposit && point.finance.deposit != 0}">
                                        <p class="notFullPaid">未缴满</p>
                                    </c:when>
                                    <c:when test="${point.deposit == point.finance.deposit || point.deposit < point.finance.deposit}">
                                        <p class="fullPaid">已缴满</p>
                                    </c:when>
                                    <c:when test="${point.finance.deposit == 0.0}">
                                        <p class="notPayCost">未缴费</p>
                                    </c:when>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        ${point.finance.bankOwner}<br>
                        ${point.finance.bank.label}<br>
                        ${point.finance.bankNo}
                    </td>
                    <td>
                        ${fns:getDictLabelFromMS(point.bankIssue.value,'BankIssueType','无')}
                    </td>

                    <td>${point.developer}</td>
                </shiro:hasPermission>
                <td><a href="javascript:" data-toggle="tooltip"
                       data-tooltip="${point.serviceAreas}">${fns:abbr(point.serviceAreas,30)}</a></td>
                <td><a href="javascript:" data-toggle="tooltip"
                       data-tooltip="${point.productCategoryNames}">${fns:abbr(point.productCategoryNames,30)}</a></td>
                <td>${fns:getDictLabelFromMS(point.degree,'degreeType','')}</td>
                <td>
                    <a href="javascript:" data-toggle="tooltip" style="cursor: pointer;display: block;width: 100px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;"
                       data-tooltip="${point.remarks}">${fns:abbr(point.remarks,30)}</a>
                    <c:if test="${point.remarks ne ''}">
                        <button id="btnShowPlanRemarkList" class="btn btn-small" type="button"
                                onclick="viewRemarkList('${point.id}','${point.servicePointNo}','${point.name}');">
                            历史备注
                        </button>
                    </c:if>
                </td>
                <shiro:hasAnyPermissions name="md:servicepoint:edit,md:servicepoint:delete">
                    <td id="td_left">
                        <a href="#" onclick="editServicePoint('${point.id}')">修改</a><br>
                        <shiro:hasPermission name="md:servicepoint:delete">
                            <a href="#" onclick="removeServicePoint('${point.id}','${point.name}')">删除</a><br>
                        </shiro:hasPermission>
                        <shiro:hasAnyPermissions name="md:engineer:edit">
                            <a href="#" onclick="synchronizationServicePoint('${point.id}')">同步</a><br>
                        </shiro:hasAnyPermissions>
                        <shiro:hasAnyPermissions name="md:engineer:edit,md:engineer:view">
                            <a href="${ctx}/md/engineer/list?servicePoint.id=${point.id}">师傅管理</a>
                        </shiro:hasAnyPermissions>
                    </td>
                </shiro:hasAnyPermissions>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <div class="pagination">${page}</div>
</shiro:hasPermission>
</body>

<script type="text/javascript">
    $(document).ready(function () {
        $("th").css({"text-align": "center", "vertical-align": "middle"});
        $("td").css({"text-align": "center", "vertical-align": "middle"});
    });
</script>
</html>
