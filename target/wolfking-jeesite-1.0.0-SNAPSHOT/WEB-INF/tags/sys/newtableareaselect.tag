<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<%@ attribute name="id" type="java.lang.String" required="true" description="编号"%>
<%@ attribute name="name" type="java.lang.String" required="true" description="隐藏域名称（ID）"%>
<%@ attribute name="value" type="java.lang.String" required="true" description="隐藏域值（ID）"%>
<%@ attribute name="labelName" type="java.lang.String" required="true" description="输入框名称（Name）"%>
<%@ attribute name="labelValue" type="java.lang.String" required="true" description="输入框值（Name）"%>
<%@ attribute name="title" type="java.lang.String" required="true" description="选择框标题"%>
<%@ attribute name="cssClass" type="java.lang.String" required="false" description="css样式"%>
<%@ attribute name="cssStyle" type="java.lang.String" required="false" description="css样式"%>
<%@ attribute name="disabled" type="java.lang.String" required="false" description="是否限制选择，如果限制，设置为disabled"%>
<%@ attribute name="mustSelectCounty" type="java.lang.Boolean" required="true" description="只能选择到区县级"%>
<%@ attribute name="tooltipPlacement" type="java.lang.String" required="false" description="tooltip方式显示错误信息时的位置"%>
<%@ attribute name="callback" type="java.lang.String" required="false" description="回调方法"%>
<div class="input-append">
    <input id="${id}Id" name="${name}" class="${cssClass}" type="hidden"  value="${value}"  />
    <input id="${id}Name" name="${labelName}" type="text" readonly="readonly"  value="${labelValue}" maxlength="200"
        data-placement="${tooltipPlacement}" data-value="${id}Id" data-url="${ctx}" data-checkselect="${mustSelectCounty}"  class="proCitySelAll ${cssClass}" style="${cssStyle}" /><a id="${id}Button" href="javascript:" class="btnCity btn${disabled eq 'true'? ' disabled' : ''}"${disabled eq 'true' ? ' disabled=\'disabled\'' : ''} ><i class="icon-city"></i></a>&nbsp;&nbsp;
</div>
<link href="${ctxStatic}/area/area.css" rel="stylesheet" />
<%--<script src="${ctxStatic}/area/area.js" type="text/javascript"></script>--%>
<script type="text/javascript">
    var area;
    $(function(){
        area = top.window.CountryAreaUtils;
        area.initComponent(document);
        $(document).on("click",".btnCity",function(event){
            <c:choose>
            <c:when test="${empty callback}">
            area.open(this,event);
            </c:when>
            <c:otherwise>
            area.opencallback(this,event,${callback});
            </c:otherwise>
            </c:choose>
        });
    });
</script>