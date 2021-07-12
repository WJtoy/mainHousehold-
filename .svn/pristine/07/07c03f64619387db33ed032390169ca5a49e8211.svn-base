<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>B2B客户料号</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#inputForm").validate({
				submitHandler: function(form){
                    var value = $("#b2bCategoryName").val();
                    if(value.trim().length == 0){
                        layerMsg('b2b辅材分类不能为空');
                        return false
                    }
					loading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);
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
            //获取辅材分类
            getAllCategoryList();
		});


        //获取辅材分类
        function getAllCategoryList() {
            $.ajax({
                    url:"${ctx}/provider/md/auxiliaryMaterialCategory/ajax/findAllList",
                    success:function (e) {
                        if(e && e.success == true){
                            $("#auxiliaryMaterialCategoryId").empty();
                            var programme_sel=[];
                            var hiddenCategoryId = $("#hiddenCategoryId").val();
                            programme_sel.push('<option value="" selected="selected">请选择</option>')
                            for(var i=0,len=e.data.length;i<len;i++){
                                var programme = e.data[i];
                                if(programme.id == hiddenCategoryId){
                                    programme_sel.push('<option value="'+programme.id+'" selected="selected">'+programme.name+'</option>')
                                }else{
                                    programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                                }
                            }
                            $("#auxiliaryMaterialCategoryId").append(programme_sel.join(' '));
                            $("#auxiliaryMaterialCategoryId").change();
                            $("#hiddenCategoryId").val("");
                        }else if(e.success==false){
                            $("#auxiliaryMaterialCategoryId").html('<option value="" selected>请选择</option>');
                            $("#auxiliaryMaterialCategoryId").change();
                            layerError(e.message,"错误提示");
                        }
                    },
                    error:function (e) {
                        ajaxLogout(e.responseText,null,"请求服务费项目分类失败","错误提示！");
                    }
                }
            );
        }
	</script>
	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
	  </style>
  </head>
  
  <body>
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/b2bcenter/md/surchargeCategoryMapping/getList">列表</a></li>
		<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:b2bsurchargecategory:edit">${not empty surchargeCategoryMapping.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:b2bsurchargecategory:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
    <input type="hidden" id="hiddenCategoryId" value="${surchargeCategoryMapping.auxiliaryMaterialCategoryId}">
	<form:form id="inputForm" modelAttribute="surchargeCategoryMapping" action="${ctx}/b2bcenter/md/surchargeCategoryMapping/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">数据源:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${surchargeCategoryMapping.dataSource >0}">
						<form:hidden path="dataSource"></form:hidden>
						<input class="fromInput" readonly="true" value="${fns:getDictLabelFromMS(surchargeCategoryMapping.dataSource, 'order_data_source','Unknow' )}"></input>
					</c:when>
					<c:otherwise>
						<form:select disabled="${surchargeCategoryMapping.dataSource > 0?'true':'false'}" path="dataSource" cssClass="required input-medium" cssStyle="width: 220px;">
							<form:options items="${fns:getDictExceptListFromMS('order_data_source',1)}"
										  itemLabel="label" itemValue="value" htmlEscape="false" />
						</form:select>
					</c:otherwise>
				</c:choose>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">工单系统辅材分类:</label>
			<div class="controls">
                <select id="auxiliaryMaterialCategoryId" name="auxiliaryMaterialCategoryId" style="width:225px;" class="required">
                    <option value="">请选择</option>
                </select>
                <span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">B2B辅材分类:</label>
			<div class="controls">
				<form:input path="b2bCategoryName" htmlEscape="false" maxlength="30" class="required"/>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">描述:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="5" maxlength="230" class="input-xlarge" cssStyle="min-width: 260px;max-width: 560px;min-height: 70px;max-height: 210px;"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="md:b2bsurchargecategory:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>
