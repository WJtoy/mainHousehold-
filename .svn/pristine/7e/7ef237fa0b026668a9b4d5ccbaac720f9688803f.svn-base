<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品品类</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	  <style type="text/css">
		  .table thead th,.table tbody td {
			  text-align: center;
			  vertical-align: middle;
			  BackColor: Transparent;
		  }
	  </style>

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
		  function editProductCategory(type,id) {
			  var text = "添加产品品类";
			  var url = "${ctx}/md/productcategory/form";
			  var area = ['727px', '500px'];
			  if(type == 20){
				  text = "修改产品品类";
				  url = "${ctx}/md/productcategory/form?id=" + id;
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

		  function removeProductcategory(id,name){
              $.ajax({
                  url: "${ctx}/md/productcategory/check?id="+id,
                  success: function (data) {
                      if(ajaxLogout(data)){
                          return false;
                      }
                      if(data && data.success == true){
			              layer.confirm(
					         '确认要删除产品品类' +'<label style="color:#63B9E6">'+ name +'</label>吗？',
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
							      url: "${ctx}/md/productcategory/delete?id="+id,
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
                      }else {
                          var mylayer = top.layer;
                          mylayer.alert(data.message, {zIndex:29891014, title: "提示"});
                          return false;
                      }
                  },
               });
              return false;
		     }
	  </script>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">产品品类</a></li>
	</ul>
	<form:form id="searchForm" modelAttribute="productCategory" action="${ctx}/md/productcategory" method="post" cssStyle="margin: 0px 0 0px;" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden"
			   value="${page.pageSize}" />

		<label>品类编码：</label>
		<input type="text" id="code" name="code" maxlength="50"
			   value="${productCategory.code}" class="input-small" style="width:200px;" />
		<label>品类名称：</label>
		<input type="text" id="name" name="name" maxlength="50"
			   value="${productCategory.name}" class="input-small" style="width:200px;" />
		&nbsp;<input id="btnSubmit" class="btn btn-primary"
		 value="查询"  type="submit" onclick="return setPage();" />
	</form:form>
	<shiro:hasPermission name="md:productcategory:edit">
	<button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 128px;height: 30px" onclick="editProductCategory()">
		<i class="icon-plus-sign"></i>&nbsp;添加产品品类
	</button></shiro:hasPermission>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50px">序号</th>
				<th width="240px">品类编码</th>
				<th width="240px">品类名称</th>
				<th width="240px">品类分组</th>
				<th width="160px">排序</th>
				<th width="120px">自动客评</th>
				<th width="120px">APP完工</th>
<%--				<th width="100px">VIP标识</th>--%>
				<th width="500px">描述</th>
				<shiro:hasPermission name="md:productcategory:edit"><th width="240px">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="productcategory">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index}</td>
				<td>${productcategory.code}</td>
				<td>${productcategory.name}</td>
				<td>${productcategory.groupCategoryName}</td>
				<td>${productcategory.sort}</td>
				<td>
					<c:choose>
						<c:when test="${productcategory.autoGradeFlag == 1}">
							<span  style="color:red;">是</span>
						</c:when>
						<c:otherwise>
							<span>否</span>
						</c:otherwise>
					</c:choose>
				</td>
				<td>
					<c:choose>
						<c:when test="${productcategory.appCompleteFlag == 1}">
							<span  style="color:red;">是</span>
						</c:when>
						<c:otherwise>
							<span>否</span>
						</c:otherwise>
					</c:choose>
				</td>
<%--				<td><c:choose>--%>
<%--					<c:when test="${productcategory.vipFlag == 1}">--%>
<%--						是--%>
<%--					</c:when>--%>
<%--					<c:otherwise>--%>
<%--						否--%>
<%--					</c:otherwise>--%>
<%--				</c:choose>--%>
<%--				</td>--%>
				<td>${productcategory.remarks}</td>
				<shiro:hasPermission name="md:productcategory:edit"><td>
					<a href="javascript:editProductCategory(20,'${productcategory.id}')">修改</a>
					&nbsp;&nbsp;&nbsp;&nbsp;
					<a href="#" onclick="removeProductcategory('${productcategory.id}','${productcategory.name}')">删除</a>
				</td></shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>
