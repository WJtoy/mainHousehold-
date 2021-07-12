<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点产品</title>
	<meta name="decorator" content="default"/>

	  <script src="${ctxStatic}/layui/layui.js"></script>
	  <link rel="stylesheet" type="text/css" href="${ctxStatic}/layui/css/layui.css">

	<script type="text/javascript">
	</script>
	<style type="text/css">
		.right {
			float: right;
			padding: 10px;
			width: calc(100% - 208px);
			height: 450px;
			overflow: auto;
		}

		.list-group-c.active{
			z-index: 2;
			color: #fff;
			background-color: #0096DA;
			border-color: #337ab7;
			/*height: 45px;*/
			/*line-height: 25px;*/
		}
		.list-group-c {
			position: relative;
			display: block;
			padding: 10px 15px;
			margin-bottom: 8px;
			background-color: #fff;
			/*border: 1px solid #ddd;*/
			/*text-decoration: none;*/
		}
		#tabs label:hover {
			text-decoration: none;
			/*background: #999999;*/
			/*color: #fff;*/
			/*height: 45px;*/
			/*line-height: 60px;*/
		}
		#tabs{
			text-align: center;
			margin: 0 auto;
			width: 116px;
		}
		#cateDiv{
			height: 460px;
			width: 116px;
			float: left;
			border-right: 1px solid #EEEEEE;
		}
		#editBtn{
			/*height: 57px;*/
			float: right;
			border-top: 1px solid #EEEEEE;
			margin-right: 17px;
			margin-top: 2px;
			width: 95%;
		}
		.tab-item{
			/*float: left;*/
			width: 93%;
			margin: 8px;
			margin-left: 16px;
		}
		#productDiv{
			float: left;
			width: 77%;
			height: 443px;
		}
		.label-e{
			color: #999999;
		}
		.label-item{
			width: 50%;
			margin-bottom: 26px;
			float: left;
			height: 22px;
		}

	</style>
  </head>

  <body>

<form:form id="inputForm" modelAttribute="servicePoint" method="post" class="" style="margin: 0px;">
	<form:hidden path="id"/>
	<input id="productIds" name="productIds" type="hidden" value=""/>


	<div class="layui-form">
		<div id="cateDiv">
			<div id="tabs" class="list-group col-md-4 col-xs-4">
				<!--tab里面的值应与下面标签页的id一致-->
				<c:choose>
					<c:when test="${productCategories.size() >0}">
						<c:forEach items="${productCategories}" var="entity" varStatus="i">
							<c:choose>
								<c:when test="${i.index == 0}">
									<label tab="tab-item${entity.id}" class="tab list-group-c label-e active" >${entity.name}</label>
								</c:when>
								<c:otherwise>
									<label tab="tab-item${entity.id}" class="tab list-group-c label-e" >${entity.name}</label>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</c:when>
					<c:otherwise>
                        <label></label>
					</c:otherwise>
				</c:choose>
			</div>
		</div>


		<div id="productDiv" class="col-md-8 col-xs-4 right">
			<c:forEach items="${products}" var="p" >
				<div id="tab-item${p.key}" class="tab-item">
					<c:forEach items="${p.value}" var="i">
						<%--<label class="label-item">--%>
						<div class="label-item">
							<input type="checkbox" name="products" lay-skin="primary" value="${i.id}">${i.name}
						</div>
						<%--</label>--%>
					</c:forEach>
				</div>
			</c:forEach>
		</div>


		<div id="editBtn">
				<input id="btnSubmit" class="btn btn-primary layui-btn layui-btn-sm" type="button" lay-submit lay-filter="formSave" value="保 存" style="margin-left: 410px;margin-top: 10px;width: 65px;background: #0096DA;border-radius: 4px;font-size: 14px;"/>
				<input id="btnCancel" class="btn layui-btn layui-btn-sm layui-btn-primary" type="button" value="关 闭" style="margin-top:10px;width: 65px;border-radius: 4px;font-size: 14px;"onclick="cancel()"/>
		</div>
	</div>
</form:form>
<form:form id="submitForm" style="margin: 0px;"></form:form>
<script>
    $(document).ready(function () {
        layui.use('form', function(){
            var form = layui.form,
                $ = layui.$;

            // 监听提交
            form.on('submit(formSave)', function(data){
                var arr_box = [];
                arr_box = $('input[type=checkbox]:checked').map(function() {
                    return $(this).val();
                });

                if(clickTag == 1){
                    return false;
                }
                if(!$("#inputForm").valid()){
                    return false;
                }

                clickTag = 1;
                $("#productIds").val(arr_box.get().join(","));
                var loadingIndex;
                var productIds = $("#productIds").val();
                var servicePointId = $("#id").val();
                var options = {
                    url: "${ctx}/md/servicePointProduct/saveServicePointProduct",
                    type: 'post',
                    dataType: 'json',
                    data: {productIds:productIds,servicePointId:servicePointId},
                    beforeSubmit: function(formData, jqForm, options){
                        loadingIndex = layer.msg('正在提交，请稍等...', {
                            icon: 16,
                            time: 0,
                            shade: 0.3
                        });
                        return true;
                    },// 提交前的回调函数
                    success:function (data) {
                        //提交后的回调函数
                        if(loadingIndex) {
                            layer.close(loadingIndex);
                        }
                        if(ajaxLogout(data)){
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            return false;
                        }
                        if (data.success) {
                            layerMsg("保存成功");
                            setTimeout(function () {
                                top.layer.close(this_index);
                            }, 2000);
                        } else {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            layerError("数据保存错误:" + data.message, "错误提示");
                        }
                        return false;
                    },
                    error: function (data) {
                        setTimeout(function () {
                            clickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                        }, 2000);
                        ajaxLogout(data,null,"数据保存错误，请重试!");
                    },
                };
                $("#submitForm").ajaxSubmit(options);
            });
        });
    });

    // 事件委托给父级div来处理
    $(document).ready(function () {
        $(".tab-item").hide();
        $("#tab-item1").show();

    });

    function initCheckBox(){
        var productIdList = {};
        <c:if test="${not empty productIds && !(productIds eq null)}">
        productIdList = ${productIds}
        </c:if>
        if (productIdList.length >0) {
            var n = 0;
            for(var i in productIdList){
                var productId = productIdList[i];
                var query = ":checkbox[name='products'][value="+productId+"]";
                $(query).attr("checked","checked");
            }
        }
    }
    initCheckBox();

    $("#tabs").on("click", "label", function (event) {
        // 触发事件的元素
        let target = $(event.target);
        $(".tab-item").hide();
        $(".tab").removeClass("active");
        // 添加css样式
        target.addClass("active");
        // 取自定义字段里面的值(即a标签里面的tab字段)
        // tab字段里面存的是各个标签页的id, 以此来控制显示和隐藏
        $("#" + target.attr("tab")).show();
    });

    var clickTag = 0;
    var this_index = top.layer.index;
    var $btnSubmit = $("#btnSubmit");


    function cancel() {
        top.layer.close(this_index);// 关闭本身
    };

</script>
</body>


</html>
