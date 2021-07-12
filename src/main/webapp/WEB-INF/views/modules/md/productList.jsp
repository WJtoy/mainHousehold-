<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>产品</title>
	<meta name="decorator" content="default" />
    <link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
    <script src="${ctxStatic}/jquery-upload-file/js/ajaxfileupload.js"></script>
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">

        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }

        .upload_warp_left {cursor: pointer;
        }
    </style>
	<script type="text/javascript">
        $(document).ready(function() {
            $("#treeTable").viewer();
        });

        function editProduct(type,id) {
            var text = "产品管理-添加产品";
            var url = "${ctx}/md/product/formNew";
            if(type == 2){
                text = "产品管理-修改"
                url = "${ctx}/md/product/formNew?id=" + id;
            }
            top.layer.open({
                type: 2,
                id:"product",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['1260px', '700px'],
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
		<li class="active"><a href="javascript:void(0);">产品列表</a>
		</li>
		<%--<shiro:hasPermission name="md:product:edit">
			<li><a href="${ctx}/md/product/form">产品添加</a>
			</li>
		</shiro:hasPermission>--%>
		<shiro:hasPermission name="md:product:edit">
			<li><a href="${ctx}/md/product/sort">产品排序</a>
			</li>
		</shiro:hasPermission>
	</ul>
	<form:form id="searchForm" action="${ctx}/md/product" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden"
			value="${page.pageSize}" />
		<label>客户：</label>
		</label>
	<%--	<sys:treeselect id="customer" name="customerId" value="${customerId}"
			labelName="customerName" labelValue="${customerName}" title="客户"
			url="/md/customer/treeData" cssClass="input-small"
			cssStyle="width:150px;" allowClear="true" />--%>
		<select name="customerId" class="input-large">
			<option value="">所有</option>
			<c:forEach items="${fns:getMyCustomerListFromMS()}" var="customer">
				<option value="${customer.id}"  <c:out value="${customer.id==customerId ?'selected':''}"/>>${customer.name}</option>
			</c:forEach>
		</select>
		&nbsp;
		<label>产品品类：</label>
		<sys:treeselect id="category" name="categoryId" value="${categoryId}" labelName="categoryName" labelValue="${categoryName}"
					title="产品分类" url="/md/productcategory/treeData" cssClass="required" allowClear="true"/>
		<label>产品名称：</label>
		<input type="text" id="name" name="name" maxlength="50"
			value="${name }" class="input-small" style="width:80px;" />
		<label>品牌 ：</label>
		<input type="text" id="brand" name="brand" maxlength="20"
			value="${brand }" class="input-small" style="width:80px;" />
		&nbsp;
		<label>型号/规格 ：</label>
		<input type="text" id="model" name="model" maxlength="30"
			value="${model }" class="input-small" style="width:80px;" />
		&nbsp;<input id="btnSubmit" class="btn btn-primary"
			value="查询"  type="submit" onclick="return setPage();" />
	</form:form>
	<shiro:hasPermission name="md:product:edit">
		<%--<input class="btn btn-primary" style="margin-top: 10px;margin-bottom: 20px;width: 60px" value="添加产品" onclick="editProduct(1,null)">--%>
		<button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 100px;height: 30px" onclick="editProduct(1,null)">
			<i class="icon-plus-sign"></i>&nbsp;添加产品
		</button>
	</shiro:hasPermission>
	<sys:message content="${message}" />
        <table id="treeTable"
		class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50px">序号</th>
				<th>产品品类</th>
				<th>产品</th>
				<th>拼音简称</th>
                <th width="150px">产品分类</th>
                <th width="250px">规格</th>
				<%--<th>品牌</th>
				<th>型号/规格</th>--%>
				<th>套组</th>
				<th>排序</th>
				<th>产品图片</th>
<%--				<th>配件</th>--%>
				<th width="45px">状态</th>
				<shiro:hasPermission name="md:product:edit">
					<th width="65px">操作</th>
				</shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
			<c:set var="index" value="0"></c:set>
			<c:forEach items="${page.list}" var="product">
				<tr>
					<c:set var="index" value="${index+1}"></c:set>
					<td>${index+(page.pageNo-1)*page.pageSize}</td>
					<td>${product.category.name}</td>
					<td>${product.name}</td>
					<td>${product.pinYin}</td>
                    <td>${product.productTypeInfo}</td>
                    <td>
                        <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${product.productSpecInfo}">${fns:abbr(product.productSpecInfo,50)}</a>
                    </td>
				<%--	<td>${product.brand}</td>
					<td>${product.model}</td>--%>
					<td>
					<c:choose>
						<c:when test="${product.setFlag==0}">
							否
						</c:when>
						<c:otherwise>
							<span style="color:red;">是</span>
						</c:otherwise>
					</c:choose>
					</td>
					<td>${product.sort}</td>
<%--					<td>--%>
<%--						<a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${product.materialNames}">${fns:abbr(product.materialNames,50)}</a>--%>
<%--					</td>--%>
					<td>
                        <c:if test="${product.attachment1 != null && product.attachment1 ne ''}">
                            <div class="upload_warp" id="divUploadWarp">
                                <div class="upload_warp_left" data-code="" data-index="">
                                    <div class="upload_warp_img_div drag" id="divImg_one">
                                        <img style="height: 50px;" itle="点击放大图片" id="viewImg_one"  data-original="${ctxUpload}/${product.attachment1}" src="${ctxUpload}/${product.attachment1}"/>
                                    </div>
                                </div>
                        </c:if>
					</td>
					<td>
						<c:choose>
							<c:when test="${product.approveFlag==0}">
								<span style="color:red;">未审核</span>
							</c:when>
							<c:otherwise>
								已审核
							</c:otherwise>
						</c:choose>
					</td>
					<shiro:hasPermission name="md:product:edit">
						<td><a href="javascript:editProduct(2,'${product.id}')">修改</a>
							<a href="${ctx}/md/product/delete?id=${product.id}&sf=${product.setFlag}"
							onclick="return confirmx('确认要删除该产品吗？', this.href)">删除</a></td>
					</shiro:hasPermission>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
    <script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
        });
    </script>
</body>
</html>
