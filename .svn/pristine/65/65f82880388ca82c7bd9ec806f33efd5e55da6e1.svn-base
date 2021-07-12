<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<%@ attribute name="value" type="java.lang.String" required="true" description="值"%>
<%@ attribute name="width" type="java.lang.Integer" required="true" description="控件宽度(px)"%>
<%@ attribute name="shopList" type="java.util.List" required="true" description="店铺列表"%>
<label>店铺：</label>
<form:select path="shopId" class="input-small" itemValue="${value}" style="width:${width}px;">
	<form:option value="" label="所有"/>
	<c:if test="${shopList != null && shopList.size() >0}">
		<form:options items="${shopList}" itemLabel="shopName" itemValue="shopId" htmlEscape="false"/>
	</c:if>
</form:select>
<script type="text/javascript">
	function resetShop(){
		$("#shopId").val("");
		$("#s2id_shopId").find("span.select2-chosen").html('所有');
	}

	$(document).on("change", "#dataSource",function () {
		var dataSource = $(this).val() || '0';
		var customerId = $("#customerId").val() || '0';
		getShopList(customerId, dataSource);
		return false;
	});

	$(document).on("change", "#customerId",function (event) {
		var customerId = $(this).val();
		var dataSource = $("#dataSource").val() || '0';
		getShopList(customerId, dataSource);
		event.preventDefault();
		return false;
	});

	function getShopList(customerId, dataSource) {
		var ctl_shopId = $("#shopId");
		var sid = "shopId";
		ctl_shopId.empty();
		// if (dataSource == '0' || dataSource == '1' || customerId == '0') {
		if (dataSource == '0' || customerId == '0') {
			var s2text = "所有";
			var option = document.createElement("option");
			option.text = s2text;
			option.value = "";
			ctl_shopId[0].options.add(option);
			$("#s2id_" + sid).find("span.select2-chosen").html('所有');
			return false;
		}
		$("#s2id_" + sid).find("span.select2-chosen").html('');
		$.ajax({
			url: "${ctx}/b2bcenter/md/customer/getShopList?dataSource=" + dataSource + "&customerId=" + customerId,
			type: "GET",
			success: function (data) {
				if (ajaxLogout(data)) {
					return false;
				}
				if (data.success == false) {
					layerError(data.message, "读取店铺列表错误");
					return;
				}
				var s2text = "所有";
				var option = document.createElement("option");
				option.text = s2text;
				option.value = "";
				ctl_shopId[0].options.add(option);
				$.each(data.data, function (i, item) {
					option = document.createElement("option");
					option.text = item.shopName;
					option.value = item.shopId;
					ctl_shopId[0].options.add(option);
				});
				$("#" + sid + " option:nth-child(1)").attr("selected", "selected");
				$("#s2id_" + sid).find("span.select2-chosen").html(s2text);
				return false;
			},
			error: function (e) {
				ajaxLogout(e.responseText, null, "读取店铺列表错误，请重试!");
				e.preventDefault();
			}
		});
		return false;
	}
</script>
