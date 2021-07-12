<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>品牌-产品分类配置</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/treeview.jsp" %>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
					//保存品牌信息
					var mids = [], brandnodes = brandTree.getCheckedNodes(true);
					for(var i=0; i<brandnodes.length; i++) {
						
						mids.push(brandnodes[i].id);
						
					}
					if (mids.length==0){
                        layerMsg('请至少勾选一个品牌');
					    return false;
					}
					$("#brandIds").val(mids);
					//保存品牌信息
					
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
			//品牌信息
			var brandNodes=[<c:forEach items="${brandList}" var="brand">{id:'${brand.id}', pId:'0', name:"${not empty brand.id?brand.name:'品牌列表'}"},
				            </c:forEach>];
			var brandTree= $.fn.zTree.init($("#brandTree"),setting,brandNodes);
			// 默认选择节点
			var brandids="${brandsCaterotyModel.brandIds}".split(",");
			for ( var i = 0; i < brandids.length; i++)
			{
				var node = brandTree.getNodeByParam("id", brandids[i]);
				try{
                    brandTree.checkNode(node, true, false);
				}catch(e){}
			}
			// 默认展开全部节点
            brandTree.expandAll(true);
			//品牌信息
		});
	</script>
  </head>
  
  <body>
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/md/brandCategory">列表</a></li>
		<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:brandcategory:edit">添加</shiro:hasPermission>
			<shiro:lacksPermission name="md:brandcategory:edit">查看</shiro:lacksPermission>
		</a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="brandsCaterotyModel" action="${ctx}/md/brandCategory/save" method="post" class="form-horizontal">
		<input id="oldProductCategoryId" name="oldProductCategoryId" type="hidden" value="${brandsCaterotyModel.category.id}"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">产品分类:</label>
			<div class="controls">
                <sys:treeselect id="category" name="category.id" value="${brandsCaterotyModel.category.id}" labelName="category.name" labelValue="${brandsCaterotyModel.category.name}"
					title="产品分类" url="/md/productcategory/treeData" cssClass="required"/>
			</div>
		</div>
		<div class="control-group" id="divMaterial">
			<label class="control-label">品牌信息:</label>
			<div class="controls">
			<div id="brandTree" class="ztree" style="margin-top:3px;float:left;height:150px;width:500px;overflow:auto;"></div>
			<form:hidden path="brandIds"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:brandcategory:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>
