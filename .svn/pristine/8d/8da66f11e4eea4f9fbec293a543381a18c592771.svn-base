<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/treeview.jsp" %>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#value").focus();
			$("#inputForm").validate({
                rules: {
                    name: {remote: "${ctx}/md/product/checkProductName?id=" + '${product.id}'}
                },
                messages: {
                    name: {remote: "产品名称已存在"}
                },
				submitHandler: function(form){
					var ids = [], nodes = tree.getCheckedNodes(true);
					for(var i=0; i<nodes.length; i++) {
						ids.push(nodes[i].id);
					}
					$("#productIds").val(ids);
					
					//保存配件信息
					var mids = [], materialnodes = materialTree.getCheckedNodes(true);
					for(var i=0; i<materialnodes.length; i++) {
                        if(!materialnodes[i].isParent){
                            mids.push(materialnodes[i].id);
                        }
					}
					$("#materialIds").val(mids);
					//保存配件信息
					
					loading('正在提交，请稍等...');
					form.submit();
				},
				errorContainer: "#messageBox",
				errorPlacement: function(error, element) {
					$("#messageBox").text("输入有误，请先更正。");
					if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
						error.appendTo(element.parent().parent());
					} else {
						error.insertAfter(element);
					}
				}
			});
			var setting = {check:{enable:true,nocheckInherit:true},view:{selectedMulti:false},
					data:{simpleData:{enable:true}},callback:{beforeClick:function(id, node){
						tree.checkNode(node, !node.checked, true, true);
						return false;
					}}};
			// 用户-区域
			var zNodes=[
					<c:forEach items="${productList}" var="product">{id:'${product.id}', pId:'0', name:"${not empty product.id?product.name:'产品列表'}"},
		            </c:forEach>];
			// 初始化树结构
			var tree = $.fn.zTree.init($("#productTree"), setting, zNodes);
			// 默认选择节点
			var ids = "${product.productIds}".split(",");
			
			for(var i=0; i<ids.length; i++) {
				var node = tree.getNodeByParam("id", ids[i]);
				try{
					tree.checkNode(node, true, false);
					tree.expandNode(node,true,false,false);
				}catch(e){}
			}
			// 默认展开全部节点
			//tree.expandAll(true);
			// 默认展开一级节点
			var nodes = tree.getNodesByParam("level", 0);
			for(var i=0; i<nodes.length; i++) {
				tree.expandNode(nodes[i], true, false, false);
			}
			
			// 刷新（显示/隐藏）机构
			refreshProductTree();
			$("#setFlag").change(function(){
				refreshProductTree();
			});
			
			//配件信息
            var materNodes = [<c:forEach items="${materialCategoryList}" var="materialCategory">{id:'category_${materialCategory.id}',name:'${not empty materialCategory.id?materialCategory.name:'配件类别列表'}',isParent:'true'},
					</c:forEach><c:forEach items="${materialList}" var="material">{id:'${material.id}', pId:'category_${material.materialCategory.id}', name:"${not empty material.id?material.name:'配件列表'}"},
                </c:forEach>];
			var materialTree= $.fn.zTree.init($("#materialTree"),setting,materNodes);
			// 默认选择节点
			var materialids="${product.materialIds}".split(",");
			for ( var i = 0; i < materialids.length; i++)
			{
				var node = materialTree.getNodeByParam("id", materialids[i]);
				try{
					materialTree.checkNode(node, true, true);
				}catch(e){}
			}
			// 默认展开全部节点
			materialTree.expandAll(true);
			//配件信息
		});
		
		function refreshProductTree(){
			if($("#setFlag").val()==1){
				$("#divProduct").show();
				$("#productTree").show();
                $("#divMaterial").hide();
			}else{
				$("#divProduct").hide();
				$("#productTree").hide();
                $("#divMaterial").show();
			}
		}
	</script>
  </head>
  
  <body>
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/md/product">产品列表</a></li>
		<li class="active"><a href="javascript:void(0);">产品<shiro:hasPermission name="md:product:edit">${not empty product.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:product:edit">查看</shiro:lacksPermission></a></li>
		<li><a href="${ctx}/md/product/sort">产品排序</a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="product" action="${ctx}/md/product/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<input id="oldProductCategoryId" name="oldProductCategoryId" type="hidden" value="${product.category.id}"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">产品分类:</label>
			<div class="controls">
                <sys:treeselect id="category" name="category.id" value="${product.category.id}" labelName="category.name" labelValue="${product.category.name}"
					title="产品分类" url="/md/productcategory/treeData" cssClass="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">名称:</label>
			<div class="controls">
				<form:input path="name" htmlEscape="false" minLength = "2" maxlength="50" class="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">拼音简称:</label>
			<div class="controls">
				<form:input path="pinYin" htmlEscape="false" minLength = "2" maxlength="50" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">品牌:</label>
			<div class="controls">
				<form:input path="brand" htmlEscape="false" minLength = "2" maxlength="20"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">规格:</label>
			<div class="controls">
				<form:input path="model" htmlEscape="false" maxlength="30"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">排序:</label>
			<div class="controls">
				<form:input path="sort" htmlEscape="false" maxlength="100" class="required digits"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" class="input-xlarge"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">套组:</label>
			<div class="controls">
				<form:select path="setFlag">
						<form:option value="0" label="否"/>
						<form:option value="1" label="是"/>
				</form:select>
			</div>
		</div>
		<div class="control-group" id="divProduct">
			<label class="control-label">套组产品:</label>
			<div class="controls">
			<div id="productTree" class="ztree" style="margin-top:3px;float:left;height:150px;width:500px;overflow:auto;"></div>
			<form:hidden path="productIds"/>
			</div>
		</div>

		<div class="control-group" id="divMaterial">
			<label class="control-label">配件信息:</label>
			<div class="controls">
			<div id="materialTree" class="ztree" style="margin-top:3px;float:left;height:150px;width:500px;overflow:auto;"></div>
			<form:hidden path="materialIds"/>
			</div>
		</div>

		<div class="form-actions">
			<shiro:hasPermission name="md:product:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>
