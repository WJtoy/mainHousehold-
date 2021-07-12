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
                    var value = $("#b2bItemName").val();
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

            $(document).on('change','#auxiliaryMaterialCategoryId',function(e){
                getItemList();
			});
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
		//获取工单系统的辅材项目
		function getItemList() {
			var auxiliaryMaterialCategoryId =$("#auxiliaryMaterialCategoryId").val();
			if(auxiliaryMaterialCategoryId!=null && auxiliaryMaterialCategoryId>0){
                $.ajax({
                        url:"${ctx}/b2bcenter/md/surchargeItemMapping/ajax/getAuxiliaryItemListByAuxiliaryCategoryId?auxiliaryMaterialCategoryId=" + auxiliaryMaterialCategoryId,
                        success:function (e) {
                            if(e && e.success == true){
                                $("#auxiliaryMaterialItemId").empty();
                                var programme_sel=[];
                                var hiddenItemId = $("#hiddenItemId").val();
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
                                for(var i=0,len=e.data.length;i<len;i++){
                                    var programme = e.data[i];
                                    if(programme.id == hiddenItemId){
                                        programme_sel.push('<option value="'+programme.id+'" selected="selected">'+programme.name+'</option>')
                                    }else{
                                        programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                                    }
                                }
                                $("#auxiliaryMaterialItemId").append(programme_sel.join(' '));
                                $("#auxiliaryMaterialItemId").change();
                                $("#hiddenItemId").val("");
                            }else if(e.success==false){
                                $("#auxiliaryMaterialItemId").html('<option value="" selected>请选择</option>');
                                $("#auxiliaryMaterialItemId").change();
                                layerError(e.message,"错误提示");
                            }
                        },
                        error:function (e) {
                            ajaxLogout(e.responseText,null,"请求服务费项目分类失败","错误提示！");
                        }
                    }
                );
			}
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
		<li><a href="${ctx}/b2bcenter/md/surchargeItemMapping/getList">列表</a></li>
		<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:b2bsurchargeitem:edit">${not empty surchargeItemMapping.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:b2bsurchargeitem:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<input type="hidden" id="hiddenItemId" value="${surchargeItemMapping.auxiliaryMaterialItemId}">
	<input type="hidden" id="hiddenCategoryId" value="${auxiliaryMaterialCategoryId}">
	<form:form id="inputForm" modelAttribute="surchargeItemMapping" action="${ctx}/b2bcenter/md/surchargeItemMapping/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">数据源:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${surchargeItemMapping.dataSource >0}">
						<form:hidden path="dataSource"></form:hidden>
						<input class="fromInput" readonly="true" value="${fns:getDictLabelFromMS(surchargeItemMapping.dataSource, 'order_data_source','Unknow' )}"></input>
					</c:when>
					<c:otherwise>
						<form:select disabled="${surchargeItemMapping.dataSource > 0?'true':'false'}" path="dataSource" cssClass="required input-medium" cssStyle="width: 220px;">
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
                <select id="auxiliaryMaterialCategoryId" style="width:225px;" class="required">
                    <option value="">请选择</option>
                </select>
                <span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">工单系统辅材项目:</label>
			<div class="controls">
				<select id="auxiliaryMaterialItemId" name="auxiliaryMaterialItemId" style="width:225px;" class="required">
					<option value="">请选择</option>
				</select>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">B2B辅材项目:</label>
			<div class="controls">
				<form:input path="b2bItemName" htmlEscape="false" maxlength="30" class="required"/>
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
			<shiro:hasPermission name="md:b2bsurchargeitem:edit"><input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  </body>
</html>
