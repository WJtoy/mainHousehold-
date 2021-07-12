<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>客户服务价格管理</title>
	<meta name="decorator" content="default"/>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<script src="${ctxStatic}/js/fixtable.js" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<style>
		.icon-object{
			height: 24px;
			width: 24px;
			vertical-align: middle;
		}
	</style>
	<script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {

            var w = $(window).width();
            FixTable("treeTable", 2, w, "100%");
			var pagestyle = function() {
				var width = $(window).width() -0;
                FixTable("treeTable", 2, width, "100%");
                $("#treeTable_tableLayout").css("width",width);
				//$("#divGrid").css("width",h);
			}
			//注册加载事件
			//$("#iframe",window).load(pagestyle);
			//注册窗体改变大小事件
			$(window).resize(pagestyle);

            $("#btnSubmit").on("click", function(){
           /*     $("#pageNo").val(1);
                var val = $("#customerId").val();
                if (val == undefined || val.length == 0) {
                    layerInfo("请选择客户!", "信息提示");
                    return false;
                }*/

                var url = "${ctx}/md/customer/price/list";
                $("#searchForm").attr("action",url);
                $("#searchForm").submit();
                return false;
            });
		});

	    function go2Edit(priceId){
	    	window.location="${ctx}/md/customer/price/form?id="+priceId+"&"+$("#urlParams").val() +"&qFirstSearch=" + $("#firstSearch").val();
	    }
	    function go2Delete(priceId){
            layer.confirm(
                '确认要停用该价格吗？',
				{
				    btn: ['确定','取消'], //按钮
					title:'提示'
            }, function(index){
                layer.close(index);//关闭本身
                var loadingIndex = top.layer.msg('正在停用价格，请稍等...', {
                    icon: 16,
                    time: 0,//不定时关闭
                    shade: 0.3
                });
                $.ajax({
                    url: "${ctx}/md/customer/price/deleteNew?id="+priceId+"&"+$("#urlParams").val() + "&qFirstSearch=" + $("#firstSearch").val(),
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
                            layerError("服务价格停用失败:" + data.message, "错误提示");
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
        function go2Active(priceId){
        }

        function enablePrice(priceId){
            layer.confirm(
                '确认要启用该价格吗？',
                {
                    btn: ['确定','取消'], //按钮
                    title:'提示'
                }, function(index){
                    layer.close(index);//关闭本身
                    var loadingIndex = top.layer.msg('正在启用价格，请稍等...', {
                        icon: 16,
                        time: 0,//不定时关闭
                        shade: 0.3
                    });
                    $.ajax({
                        url: "${ctx}/md/customer/price/activeNew?id="+priceId+"&"+$("#urlParams").val()+"&qFirstSearch=" + $("#firstSearch").val(),
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
                                layerError("服务价格启用失败:" + data.message, "错误提示");
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

        function openPriceInfo(customerId, productId, params, qFirstSearch){
            var text = "服务价格";
            var url = "${ctx}/md/customer/price/productform?customer.id="+customerId+"&product.id="+productId+"&"+params+"&qFirstSearch="+qFirstSearch+"";
            top.layer.open({
                type: 2,
                id:"customerPrice",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['1240px', '800px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
		}

        function editPriceInfo(priceId, warrantyStatus){
            var text = "修改";
            var url = "${ctx}/md/customer/price/form?id="+priceId+"&"+$("#urlParams").val() +"&qFirstSearch=" + $("#firstSearch").val() + "&serviceType.warrantyStatus="+warrantyStatus;
            top.layer.open({
                type: 2,
                id:"customerPrice",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['800px', '640px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

        function addPriceInfo(customerId, customerName, productId, productName,serviceTypeId,serviceTypeName, warrantyStatus){
            var text = "添加";
            var url = "${ctx}/md/customer/price/insertForm?customer.id="+customerId+"&customer.name="+customerName+"&product.id="+productId+"&product.name="+productName+"&serviceType.id="+serviceTypeId+"&serviceType.name="+serviceTypeName+"&serviceType.warrantyStatus="+warrantyStatus;
            top.layer.open({
                type: 2,
                id:"customerPrice",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['800px', '600px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }
	</script>
	<style type="text/css">
		.col_product {width: 250px;}
		.col_command {width: 78px;}
		.table tbody td.error {background-color: #f2dede!important;}
	</style>
</head>
<body>
	<ul class="nav nav-tabs">
	<li class="active"><a href="javascript:;">服务价格</a></li>
	<%--<shiro:hasPermission name="md:customerprice:edit"><li><a href="${ctx}/md/customer/price/forms">客户价格维护</a></li></shiro:hasPermission>--%>
	</ul>
	<c:set var="currentuser" value="${fns:getUser()}"/>
	<input type="hidden" value="${currentuser}">
	<sys:message content="${message}"/>
	<form:form id="searchForm" modelAttribute="customerPrice" action="${ctx}/md/customer/price/list" method="post" class="breadcrumb form-search">
		<c:set var="params" value="qCustomerId=${customerPrice.customer.id}&qCustomerName=${fns:urlEncode(customerPrice.customer.name)}&qProductCategoryId=${customerPrice.productCategory.id}&qProductCategoryName=${fns:urlEncode(customerPrice.productCategory.name)}&qProductId=${customerPrice.product.id}&qProductName=${fns:urlEncode(customerPrice.product.name)}"></c:set>
		<c:set var="qFirstSearch" value="${customerPrice.firstSearch}"/>
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<input id="urlParams" name="urlParams" type="hidden" value="${params}"/>
        <form:hidden path="firstSearch" />
		<c:if test="${currentuser.isCustomer() == false}">
			<label>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
			<%--<sys:treeselect id="customer" name="customer.id" value="${customerPrice.customer.id}"
                            labelName="customer.name"
                            labelValue="${customerPrice.customer.name}" title="客户" url="/md/customer/treeData"
                            cssClass="input-small" allowClear="false" cssStyle="width:250px;"/>--%>

			<form:select path="customer.id" id="customerId" class="input-large">
				<form:option value="0" label="请选择"/>
				<form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false" />
			</form:select>
			&nbsp;
		</c:if>
		<label class="control-label">产品分类:</label>
		<%--<sys:treeselect id="productCategory" name="productCategory.id" value="${customerPrice.productCategory.id}" labelName="productCategory.name" labelValue="${customerPrice.productCategory.name}"--%>
						<%--title="产品分类" url="/md/productcategory/treeData" cssStyle="width:100px;" allowClear="true"/>--%>

		<form:select path="productCategory.id" id="productCategory" class="input-large">
			<form:option value="" label="请选择"/>
			<form:options items="${fns:getProductCategories()}" itemLabel="name" itemValue="id" htmlEscape="false" />
		</form:select>
		<label>产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
		<%--<sys:treeselect id="product" name="product.id" value="${customerPrice.product.id}" labelName="product.name" labelValue="${customerPrice.product.name}"
				title="产品" url="/md/product/treeData" cssClass="input-small" allowClear="true" cssStyle="width:250px;"/>--%>
		<form:select path="product.id" cssClass="input-small" cssStyle="width:250px;">
			<form:option value="" label="请选择"/>
			<form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
		</form:select>
		&nbsp;
		<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="top.$.jBox.tip('正在查询,请稍候...', 'loading');return setPage();" value="查询"/>
	</form:form>



	<table id="treeTable" class="table table-striped table-responsive table-bordered table-condensed table-hover">
		<thead>
		<tr>
			<th rowspan="2" colspan="2"><label class="col_product">产品名称</label></th>
			<c:forEach items="${serviceTypes}" var="serviceType">
				<th colspan="4">${serviceType.name}</th>
			</c:forEach>
		</tr>
		<tr>
			<c:set var="serviceTypeCount" value="1" />
			<c:forEach items="${serviceTypes}" var="serviceType">
				<th>价格</th>
				<th>优惠价</th>
				<th>冻结价</th>
				<th><label class="col_command">操作</label></th>
				<c:set var="serviceTypeCount" value="${serviceTypeCount+1}" />
			</c:forEach>
		</tr>
		</thead>
		<tbody>
		<c:set var="productId" value="" />

		<c:if test="${customerPrice.customer.id != null && customerPrice.customer.id > 0}">


		<c:forEach items="${page.map.list}" var="customer">
			<tr id="${customer.customerId}" pId="0">
				<%--<td colspan="${serviceTypeCount*4}">${customer.customerCode} [${customer.customerName}]</td>--%>
				<td colspan="${serviceTypeCount*4}">${customer.customerName}</td>
			</tr>
			<c:forEach items="${customer.customerPriceMapList}" var="customerPriceMap">
				<c:set var="index" value="${index+1}" />
				<tr id="${customerPriceMap.productId}" pId="${customer.customerId}">
					<td style="text-align: center;vertical-align: middle">${index+(page.pageNo-1)*page.pageSize}</td>
					<%--<td><a href="${ctx}/md/customer/price/productform?customer.id=${customer.customerId}&product.id=${customerPriceMap.productId}&${params}&qFirstSearch=${qFirstSearch}">${customerPriceMap.productName}</a></td>--%>
					<td style="text-align: center;vertical-align: middle"><a href="#" onclick="openPriceInfo('${customer.customerId}','${customerPriceMap.productId}','${params}','${qFirstSearch}')">${customerPriceMap.productName}</a></td>

					<!-- list(price) -->
					<c:forEach items="${customerPriceMap.customerPriceList}" var="customerPrice">
						<c:choose>
							<c:when test="${customerPrice.flag == 2}">
								<td class="error">&nbsp;</td>
								<td class="error">&nbsp;</td>
								<td class="error">&nbsp;</td>
								<td class="error">&nbsp;</td>
							</c:when>
							<c:when test="${customerPrice.flag == 1}">
								<td style="text-align: center;vertical-align: middle">-</td>
								<td style="text-align: center;vertical-align: middle">-</td>
								<td style="text-align: center;vertical-align: middle">-</td>
								<td style="text-align: center;vertical-align: middle"><a href="javascript:void(0);" onclick="addPriceInfo('${customer.customerId}','${customer.customerName}','${customerPriceMap.productId}','${customerPriceMap.productName}','${customerPrice.serviceType.id}','${customerPrice.serviceType.name}','${customerPrice.serviceType.warrantyStatus.value}')">添加</a></td>
							</c:when>
							<c:otherwise>

								<c:choose>
									<c:when test="${customerPrice.delFlag == 2}">
										<td style="color: #BBBBBB;text-align: center;vertical-align: middle">${customerPrice.price}</td>
										<td style="color: #BBBBBB;text-align: center;vertical-align: middle">${customerPrice.discountPrice}</td>
										<td style="color: #BBBBBB;text-align: center;vertical-align: middle">${customerPrice.blockedPrice}</td>
									</c:when>
									<c:otherwise>
										<td style="text-align: center;vertical-align: middle">${customerPrice.price}</td>
										<td style="text-align: center;vertical-align: middle">${customerPrice.discountPrice}</td>
										<td style="text-align: center;vertical-align: middle">${customerPrice.blockedPrice}</td>
									</c:otherwise>
								</c:choose>

							<c:choose>
								<c:when test="${customerPrice.delFlag==1}">
									<td style="text-align: center;vertical-align: middle">
										<%--<span class="label">已停用</span>--%>
										<%--<a href="javascript:void(0);" onclick="go2Active('${customerPrice.id}');">启用</a>--%>
										<input id="open" class="btn btn-primary btn-small" type="submit" value="启用" onclick="enablePrice('${customerPrice.id}')" style=""/>
									</td>
								</c:when>
								<c:otherwise>
									<td style="text-align: center;vertical-align: middle">
										<c:if test="${customerPrice.id ne null && customerPrice.id ne 0}">
											<c:choose>
												<c:when test="${customerPrice.delFlag == 2}">
													<span><img src="${ctxStatic}/images/icon/bitbug_favicon.ico" class="icon-object"></span>
													<span style="margin-right: 8px;color: #BBBBBB">待审核</span>
												</c:when>
												<c:otherwise>
													<a href="javascript:void(0);" onclick="go2Delete('${customerPrice.id}');">停用</a>
													<a href="javascript:void(0);" onclick="editPriceInfo('${customerPrice.id}','${customerPrice.serviceType.warrantyStatus.value}');">修改</a>
												</c:otherwise>
											</c:choose>
										</c:if>
									</td>
								</c:otherwise>
							</c:choose>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</tr>
			</c:forEach>
		</c:forEach>
		</c:if>
		</tbody>
	</table>
	<div class="pagination">${page}</div>

	<script class="removedscript" type="text/javascript">
        $(document).ready(function() {
            $("th").css({"text-align":"center","vertical-align":"middle"});
            // $("td").css({"text-align":"center","vertical-align":"middle"});
        });
	</script>
</body>
</html>
