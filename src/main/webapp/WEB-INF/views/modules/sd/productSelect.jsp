<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <title>上门服务时选择产品</title>
	  <%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		function page(n,s){
			$("#pageNo").val(n);
			$("#pageSize").val(s);
			$("#searchForm").submit();
	    	return false;
	    }
		var data = null;
        function setData(id,name,brand,model,servicetypeid,servicetypename,orderservietype){
            data = {
				id: id,
				name: name,
				brand: brand,
				model: model,
				servicetypeid: servicetypeid,
				servicetypename: servicetypename,
				orderservietype: orderservietype
			};
            var myjbox;
            if(window.parent.$.jBox){
                myjbox = window.parent.$.jBox;
			}
            else if(top.$.jBox){
                myjbox = top.$.jBox;
            }
            else if($.jBox){
                myjbox = $.jBox;
            }
            if(myjbox) {
                myjbox.getBox().find("button[value='ok']").trigger("click");
            }
        }
		$(function(){
			$('#divtable').slimscroll();
		});
	</script>
  </head>
  
  <body>
  <sys:message content="${message}"/>
  <table id="treeTable" class="table table-striped table-bordered table-condensed">
	  <thead><tr><th>名称</th><th>服务项目</th><th>产品分类</th><th>品牌</th><th>型号/规格</th><th>描述</th></tr></thead>
	  <tbody>
	  <c:forEach items="${page.list}" var="product">
		  <tr >
			  <td><a href="javascript:void(0);" onclick="setData('${product.id}','${product.name}','${product.brand }','${product.model}','${ product.serviceType.id}','${product.serviceType.name }','${product.serviceType.orderServiceType}');" >${product.name}</a></td>
			  <td>${product.serviceType.name}</td>
			  <td>${product.category.name}</td>
			  <td>${product.brand}</td>
			  <td>${product.model}</td>
			  <td>${product.remarks}</td>
		  </tr>
	  </c:forEach>
	  </tbody>
  </table>
	<div class="pagination">${page}</div>
  </body>
</html>
