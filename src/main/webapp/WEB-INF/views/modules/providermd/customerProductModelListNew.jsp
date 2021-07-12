<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>客户产品型号</title>
	<meta about="客户产品型号(微服务md)" />
	<meta name="decorator" content="default" />
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<style>
		.admin_button {
			margin-top: -6px;
			margin-bottom: 15px;
			border-radius: 4px;
			border: 1px solid;
			border-color: #C0C0C0;
			background-color: rgb(238, 238, 238);
			width: 100px;
			height: 30px
		}
	</style>
	<script type="text/javascript">
		$(document).ready(function() {
			// 产品发生变更,获取客户产品型号
			$(document).on("change","#customerId",function (e) {
				var customerId = $("#customerId").val();
				if (customerId !='' ) {

					$.ajax({
						url: "${ctx}/provider/md/customerBrandCategory/ajax/getListByCustomer",
						data: {customerId : customerId},
						success:function (e) {
							if(e.success){
								$("#brandId").empty();
								var programme_sel=[];
								programme_sel.push('<option value="" selected="selected">请选择</option>')
								var customerBrands = e.data.customerBrands;
								for(var i=0, len = customerBrands.length; i<len; i++){
									var programme = customerBrands[i];
									programme_sel.push('<option value="'+programme.id+'" data-id="'+programme.id+'">'+programme.brandName+'</option>')
								}
								$("#brandId").append(programme_sel.join(' '));
								$("#brandId").val("");
								$("#brandId").change();
							}else {
								$("#brandId").html('<option value="" selected>请选择</option>');
								// layerMsg('该客户还没有配置产品！');
							}
						},
						error:function (e) {
							layerError("请求客户产品型号失败","错误提示");
						}
					});
				}else {
					layerMsg('请先选择客户！');
				}
			});
		});
        function editProductModel(id, customerName, productName) {
            var text = "添加型号";
            var url = "${ctx}/provider/md/customerProductModel/form";
            if(id != null){
                text = "修改型号";
                url = "${ctx}/provider/md/customerProductModel/form?id="+id+"&customerName="+customerName+"&productName="+productName;
            }
            var area = ['640px', '577px'];
            top.layer.open({
                type: 2,
                id:"productModel",
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


        function removeProductModel(id, customerId, productId){
            layer.confirm(
                '确认要删除吗？',
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
                        url: "${ctx}/provider/md/customerProductModel/delete?id="+id+"&customerId="+customerId+"&productId="+productId,
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
</head>

<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">产品型号</a></li>
		<shiro:hasPermission name="md:customerproductmodel:edit">
			<li><a href="${ctx}/provider/md/customerProductModel/importForm">批量添加</a></li>
		</shiro:hasPermission>
	</ul>
	<c:set var="currentuser" value="${fns:getUser()}"/>
	<sys:message content="${message}" />
	<form:form id="searchForm" modelAttribute="customerProductModel" action="${ctx}/provider/md/customerProductModel/getList" method="post" class="form-search"
			   cssStyle="border-bottom: 1px solid #EEEEEE;padding-bottom: 15px;margin-top: 15px;">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden"
			value="${page.pageSize}" />
		<c:choose>
			<c:when test="${currentuser.isCustomer()==true}">
			</c:when>
			<c:when test="${currentuser.isSaleman()==true}">
				&nbsp;
				<label>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
				<select id="customerId" name="customerId" class="input-small" style="width:240px;">
					<c:forEach items="${fns:getMyCustomerList()}" var="dict">
						<option value="${dict.id}"
								<c:out value="${(customerProductModel.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
					</c:forEach>
				</select>
			</c:when>
			<c:otherwise>
				&nbsp;
				<label>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
				<select id="customerId" name="customerId" class="input-small" style="width:240px;">
					<option value=""
							<c:out value="${(empty customerProductModel.customerId)?'selected=selected':''}" />>请选择</option>
					<c:forEach items="${fns:getMyCustomerList()}" var="dict">
						<option value="${dict.id}"
								<c:out value="${(customerProductModel.customerId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
					</c:forEach>
				</select>
			</c:otherwise>
		</c:choose>
		&nbsp;
		<label>品&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;牌：</label>
		<form:select path="brandId" cssClass="input-small" cssStyle="width:200px;">
			<form:option value="" label="请选择"/>
			<form:options items="${customerBrandList}" itemLabel="brandName" itemValue="id" htmlEscape="false"/>
		</form:select>
		&nbsp;
		<label>产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
		<form:select path="productId" cssClass="input-small" cssStyle="width:200px;">
			<form:option value="" label="请选择"/>
			<form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
		</form:select>
		&nbsp;
		<label>型&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;号:</label>
		<form:input path="customerModel" htmlEscape="false" maxlength="50" class="input-small" cssStyle="width: 200px"/>
		<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="top.$.jBox.tip('正在查询,请稍候...', 'loading');return setPage();" value="查询" style="margin-left: 16px;"/>
	</form:form>

	<shiro:hasPermission name="md:customerproductmodel:edit">
		<button class="admin_button" onclick="editProductModel(null)">
			<i class="icon-plus-sign"></i>&nbsp;添加型号
		</button>
	</shiro:hasPermission>

	<table id="contentTable"
		class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<c:if test="${currentuser.isCustomer()==false}">
					<th width="227">客户</th>
				</c:if>
				<th width="200">品牌</th>
				<th width="227">产品</th>
				<th width="200">型号</th>
				<th width="240">名称</th>
				<th width="352">描述</th>
				<shiro:hasPermission name="md:customerproductmodel:edit">
					<th width="160">操作</th>
				</shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
			<c:set var="index" value="0"></c:set>
			<c:forEach items="${page.list}" var="entity">
				<tr>
					<c:set var="index" value="${index+1}"></c:set>
					<td>${index+(page.pageNo-1)*page.pageSize}</td>
					<c:if test="${currentuser.isCustomer()==false}">
						<td>${entity.customerName}</td>
					</c:if>
					<td>${entity.brandName}</td>
					<td>${entity.productName}</td>
					<td>${entity.customerModel}</td>
					<td>${entity.customerProductName}</td>
					<td>
					<c:choose>
						<c:when test="${fn:length(entity.remarks)>40}">
							<a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.remarks}">${fns:abbr(entity.remarks,80)}</a>
						</c:when>
						<c:otherwise>
							${entity.remarks}
						</c:otherwise>
					</c:choose>
					</td>
					<shiro:hasPermission name="md:customerproductmodel:edit">
						<td><a onclick="editProductModel('${entity.id}', '${fns:urlEncode(entity.customerName)}', '${fns:urlEncode(entity.productName)}')" href="#">修改</a>
							<a style="margin-left: 6px;" href="#" onclick="removeProductModel('${entity.id}', '${entity.customerId}', '${entity.productId}')">删除</a>
						</td>
					</shiro:hasPermission>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>

<script type="text/javascript">
    $(document).ready(function () {
        $("th").css({"text-align": "center", "vertical-align": "middle"});
        $("td").css({"text-align": "center", "vertical-align": "middle"});
    });
</script>
</html>
