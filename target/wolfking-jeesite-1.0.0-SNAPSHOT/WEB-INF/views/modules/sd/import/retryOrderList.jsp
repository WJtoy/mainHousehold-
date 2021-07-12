<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
<title>导入异常管理</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default" />
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		top.layer.closeAll();
		Order.rootUrl = "${ctx}";
		var clickTag = 0;
		function openjBox(url, title, width, height) {
			top.$.jBox.open("iframe:" + url, title, width, height, {
				top : '5%',
				buttons : {},
				loaded : function(h) {
					$("#jbox-iframe", h).prop("height", "98%");
				}
			});
		}
        $(document).ready(function () {
            $("#selectAll").change(function () {
                var $check = $(this);
                $("input[type=checkbox]:enabled").each(function () {
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

            $("#btnTransfer").click(function () {
                if (clickTag == 1) {
                    return false;
                }
                clickTag = 1;
                var ids = [];
                $("#contentTable input[name='checkedRecords']:checked").each(function () {
                    ids.push($(this).val());
                });

                if (ids.length == 0) {
                    clickTag = 0;
                    layerAlert('请选择需要转单的订单', '系统提示');
                    return;
                }
                var $btnSubmit = $("#btnTransfer");
                $btnSubmit.attr('disabled', 'disabled');
                var loadingIndex;
                var ajaxSuccess = 0;
                var confirmClickTag = 0;
                top.layer.confirm('确定自动转单吗？'
                    ,{
                        icon: 3
                        ,title:'系统确认'
                        ,cancel: function(index, layero){
                            clickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                        }
                        ,success: function(layro, index) {
                            $(document).on('keydown', layro, function(e) {
                                if (e.keyCode == 13) {
                                    layro.find('a.layui-layer-btn0').trigger('click')
                                }else if(e.keyCode == 27){
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                    top.layer.close(index);//关闭本身
                                }
                            })
                        }
                    }, function(index,layero) {
                        if(confirmClickTag == 1){
                            return false;
                        }
                        var btn0 = $(".layui-layer-btn0",layero);
                        if(btn0.hasClass("layui-btn-disabled")){
                            return false;
                        }
                        confirmClickTag = 1;
                        btn0.addClass("layui-btn-disabled").attr("disabled","disabled");
                        top.layer.close(index);//关闭本身
                        var loaindIndex;
                        var data = {ids: ids};
                        $.ajax({
                            cache: false,
                            type: "POST",
                            url: "${ctx}/sd/order/import/new/retryTransferOrders",
                            data: data,
                            beforeSend: function () {
                                loadingIndex = layer.msg('订单自动转单申请中，请稍等...', {
                                    icon: 16,
                                    time: 0,
                                    shade: 0.3
                                });
                            },
                            complete: function () {
                                if (loadingIndex) {
                                    layer.close(loadingIndex);
                                }
                                if (ajaxSuccess == 0) {
                                    setTimeout(function () {
                                        clickTag = 0;
                                        $btnSubmit.removeAttr('disabled');
                                    }, 2000);
                                }
                            },
                            success: function (data) {
                                if (ajaxLogout(data)) {
                                    return false;
                                }
                                if (data.success) {
                                    ajaxSuccess = 1;
                                    layerMsg('订单自动转单申请成功，为确保无误，请稍后重新刷新列表确认！');
                                    reloadThisPage();
                                }
                                else {
                                    layer.alert(data.message, {zIndex: 29891014, area: ['500px', '400px'], title: "错误提示"});
                                }
                            },
                            error: function (e) {
                                ajaxLogout(e.responseText, null, "订单自动转单错误，请重试!");
                            }
                        });
                        return false;
                    },function(index) {
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
                    });

            });

        });

		function reloadThisPage(){
            var iframe = getActiveTabIframe();//定义在jeesite.min.js中
            if (iframe != undefined) {
                iframe.repage();
            }else{
                location.href = "${ctx}/sd/order/import/new/manage";
            }
        }
	</script>
	<style type="text/css">
		#contentTable td {word-break: break-word;}
	</style>
</head>

<body>
	<ul id="navtabs" class="nav nav-tabs">
		<li class="active"><a href="javascript:;">导入单异常管理</a></li>
	</ul>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<form:form id="searchForm" modelAttribute="order" action="${ctx}/sd/order/import/new/manage" method="post" class="breadcrumb form-search">
		<input id="searchType" name="searchType" type="hidden"
			value="processing" />
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
		<c:if test="${canSearch == true}">
		<div>
			<c:choose>
				<c:when test="${currentuser.isCustomer()==false}">
					<label>客户：</label>
					<sys:treeselect id="customer" name="customer.id" value="${order.customer.id}" labelName="customer.name"
									labelValue="${order.customer.name}" title="客户" url="/md/customer/treeData" cssClass="input-small" allowClear="true" cssStyle="width:220px;"/>
				</c:when>
				<c:otherwise>
					<input type="hidden" id="customerId" name="customer.id" value="${currentuser.customerAccountProfile.customer.id}"/>
				</c:otherwise>
			</c:choose>

			<label>用 户：</label>
			<input type=text class="input-mini" id="userName" name="userName" value="${order.userName }" maxlength="20" />
			<label>电 话：</label><input type=text class="input-mini" id="phone" name="phone" value="${order.phone }" maxlength="20" />
			<label>下单日期：</label>
			<input id="createDate" name="createDate" type="text" readonly="readonly" style="width:95px;margin-left:4px" maxlength="20"
				class="input-small Wdate" value="${fns:formatDate(order.createDate,'yyyy-MM-dd')}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});" />
			<label>~</label>&nbsp;&nbsp;&nbsp;
			<input id="updateDate" name="updateDate" type="text" readonly="readonly" style="width:95px" maxlength="20"
				class="input-small Wdate" value="${fns:formatDate(order.updateDate,'yyyy-MM-dd')}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});" />
			&nbsp;&nbsp;
			<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
			&nbsp;&nbsp;
			<a id="btnTransfer" href="javascript:" class="btn btn-primary">自动转单</a>
		</div>
		</c:if>
	</form:form>

	<sys:message content="${message}" />
	<c:set var="rowNumber" value="0" />
	<table id="contentTable" class="table table-bordered table-condensed" style="table-layout:fixed;">
		<thead>
			<tr>
				<th width="20px"><input type="checkbox" id="selectAll" name="selectAll"/></th>
				<th width="40px">序号</th>
				<th width="100px">客户</th>
				<th width="100px">购买店铺</th>
				<th width="60px">联系人</th>
				<th width="90px">手机</th>
				<th width="80px">电话</th>
				<th width="150px">详细地址</th>
				<th width="80px">产品名称</th>
				<th width="60px">产品品牌</th>
				<th width="80px">产品型号</th>
				<th width="90px">服务项目</th>
				<th width="40px">数量</th>
				<th width="80px">快递公司</th>
				<th width="100px">快递单号</th>
				<th width="260">订单描述</th>
				<th width="120px">第三方单号</th>
				<th width="100px">创建时间</th>
				<th width="60px">错误次数</th>
				<th width="100px">最后转换时间</th>
				<th width="300px">错误信息</th>
				<th width="80px">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${page.list}" var="order">
				<c:set var="rowNumber" value="${rowNumber+1}" />
				<tr>
					<td>
						<input type="checkbox" id="cbox${rowNumber}" value="${order.id}" name="checkedRecords"/>
					</td>
					<td>${rowNumber}</td>
					<td>${order.customer.name}</td>
					<td>${order.b2bShop.shopName}</td>
					<td>${order.userName}</td>
					<td>${order.phone}</td>
					<td>${order.tel}</td>
					<td>${order.address}</td>
					<td>${order.product.name}</td>
					<td>${order.brand}</td>
					<td>${order.productSpec}</td>
					<td>${order.serviceType.name}</td>
					<td>${order.qty}</td>
					<td>${order.expressCompany.label}</td>
					<td>${order.expressNo}</td>
					<td>${order.description}</td>
					<td>${order.thdNo}</td>
					<td><fmt:formatDate value="${order.createDate}" pattern="yyyy-MM-dd" /></td>
					<td>${order.retryTimes}</td>
                    <td><fmt:formatDate value="${order.updateDate}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
					<td>${order.errorMsg}</td>
					<td>
						<div class="btn-group">
							<a class="btn btn-primary dropdown-toggle" data-toggle="dropdown"
								href="#">操作 <span class="caret"></span> </a>
							<ul class="dropdown-menu">
								<shiro:hasPermission name="sd:temporder:edit">
									<li><a href="javascript:;" onclick="edit('${order.id}');"><i class="icon-pencil"></i>修改</a></li>
								</shiro:hasPermission>
								<shiro:hasPermission name="sd:temporder:cancel">
									<li><a href="javascript:void(0);"
										   onclick="Order.cancelImportOrder('${order.id}');"><i
											class="icon-ban-circle"></i>取消</a></li>
								</shiro:hasPermission>
							</ul>
						</div>
					</td>
			</c:forEach>

		</tbody>
	</table>
	<div class="pagination">${page}</div>
	<style type="text/css">
	.dropdown-menu {  min-width: 80px;  }
	.dropdown-menu>li>a {  text-align: left;  padding: 3px 10px;  }
	.pagination {  margin: 10px 0;  }
	</style>
	<script type="text/javascript">
		function edit(id){
            var cid = $("#customerId").val();
            var cname = $("#customerName").val();
            var createDate = $("#createDate").val();
            var updateDate = $("#updateDate").val();
            var pageNo = $("#pageNo").val();
            var href = "${ctx}/sd/order/import/new/form?id=" + id + "&customer.id=" + cid + "&customer.name=" + encodeURIComponent(cname)
					+ "&createDate=" + createDate + "&updateDate=" + updateDate + "&pageNo=" + pageNo;
            location.href = href;
            return false;
		}

		$(document).ready(function() {

			$("td,th").css({
				"text-align" : "center",
				"vertical-align" : "middle"
			});
		});
	</script>
</body>
</html>
