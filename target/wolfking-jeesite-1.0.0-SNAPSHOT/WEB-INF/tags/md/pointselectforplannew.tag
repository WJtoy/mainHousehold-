<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<%--<%@ include file="/WEB-INF/views/include/dialog.jsp"%>--%>
<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
<%@ attribute name="id" type="java.lang.String" required="true" description="编号"%>
<%@ attribute name="name" type="java.lang.String" required="true" description="隐藏域名称（ID）"%>
<%@ attribute name="value" type="java.lang.String" required="true" description="隐藏域值（ID）"%>
<%@ attribute name="labelName" type="java.lang.String" required="true" description="输入框名称（Name）"%>
<%@ attribute name="labelValue" type="java.lang.String" required="true" description="输入框值（Name）"%>
<%@ attribute name="title" type="java.lang.String" required="true" description="选择框标题"%>
<%@ attribute name="areaId" type="java.lang.String" required="true" description="區域ID"%>
<%@ attribute name="subAreaId" type="java.lang.String" required="true" description="四级區域ID"%>
<%@ attribute name="productCategoryId" type="java.lang.String" required="true" description="服务品类"%>
<%@ attribute name="cssClass" type="java.lang.String" required="false" description="css样式"%>
<%@ attribute name="width" type="java.lang.String" required="false" description="字段列表"%>
<%@ attribute name="height" type="java.lang.String" required="false" description="字段表头"%>
<%@ attribute name="formid" type="java.lang.String" required="false" description="form的id,已#开头,页面有多个form且有相同属性时使用"%>
<%@ attribute name="callbackmethod" type="java.lang.String" required="true" description="回调方法"%>
<%@ attribute name="cssStyle" type="java.lang.String" required="false" description="style样式"%>
<%@ attribute name="showArea" type="java.lang.Boolean" required="false" description="是否显示区域"%>
<%@ attribute name="noSubEnginner" type="java.lang.Boolean" required="false" description="是否显示子账号"%>
<%@ attribute name="noblackList" type="java.lang.Boolean" required="false" description="是否不显示黑名单"%>
<%@ attribute name="allowClear" type="java.lang.Boolean" required="false" description="是否允许清除"%>
<%@ attribute name="noModal" type="java.lang.Boolean" required="false" description="是否为非模态模式"%>
<%@ attribute name="serviceAddress" type="java.lang.String" required="true" description="上门地址"%>
<div class="input-append">
    <input id="${id}Id" name="${name}" class="${cssClass}" type="hidden" value="${value}"${disabled eq 'true' ? ' disabled=\'disabled\'' : ''}/>
    <input id="${id}Name" name="${labelName}" readonly="readonly" type="text" value="${labelValue}" maxlength="50"${disabled eq 'true' ? ' disabled=\'disabled\'' : ''}
           class="${cssClass}" style="${cssStyle}"/>
    <a id="${id}Button" href="javascript:" data-toggle="tooltip" data-tooltip="按区域派单" style="padding: 4px;" class="btn btn-primary ${disabled eq 'true' ? ' disabled' : ''}"${disabled eq 'true' ? ' disabled=\'true\'' : ' '}>区县</a>
    <a id="${id}ButtonNew" href="javascript:" data-toggle="tooltip" data-tooltip="按乡镇派单" style="padding: 4px;" class="btn btn-success ${disabled eq 'true' ? ' disabled' : ''}"${disabled eq 'true' ? ' disabled=\'true\'' : ' '}>乡镇</a>
</div>
<script type="text/javascript">
    var parentLayerIndex = parent.layer.getFrameIndex(window.name);
    $("#${id}Button").click(function(){
        // 是否限制选择，如果限制，设置为disabled
        if ($("#${id}Id").attr("disabled")){
            return true;
        }

        var addr = encodeURIComponent('${serviceAddress}');
        var btnlist = ['确定', '关闭'${allowClear?",'清除'":""}];
        var replyIdex = top.layer.open({
            type: 2,
            id:'layer_pointlist',
            zIndex:19891015,
            title:'${title}',
            content: "${ctx}/md/servicepoint/selectForPlan?dialogType=layer&area.id=${areaId}&showArea=${showArea}&productCategoryId=${productCategoryId}&noSubEnginner=${noSubEnginner}&noblackList=${noblackList}&address="+addr+"&parentLayerIndex=" + parentLayerIndex,
            shade: ${noModal?0:0.3},
            area: ['${width}px', '${height}px'],
            maxmin: true,
            yes: function(index, layero){
                var iframeWin = parent.window['layui-layer-iframe' + index];
                var data = iframeWin.data;
                <%--var data = h.find("iframe")[0].contentWindow.data;--%>
                $("${formid} #${id}Id").val(data.id);
                $("${formid} #${id}Name").val(data.name);
                if("${callbackmethod}" != ""){
                    ${callbackmethod}(data);
                }
                top.layer.close(index);
            },
            btn2: function(index, layero){
                top.layer.close(index);
            },
            btn3: function(index, layero){
                $("${formid} #${id}Id").val("").trigger('change');
                $("${formid} #${id}Name").val("");
            },
            success: function(layero,index){
//				top.layer.full(index);
//                top.layer.style(index, {
//                    'margin-top': '10px'
//                });
            }
        });
    });

    $("#${id}ButtonNew").click(function(){
        // 是否限制选择，如果限制，设置为disabled
        if ($("#${id}Id").attr("disabled")){
            return true;
        }
        var addr = encodeURIComponent('${serviceAddress}');
        var btnlist = ['确定', '关闭'${allowClear?",'清除'":""}];
        var replyIdex = top.layer.open({
            type: 2,
            id:'layer_pointlist',
            zIndex:19891015,
            title:'${title}',
            content: "${ctx}/md/servicepoint/selectForPlan?dialogType=layer&area.id=${areaId}&subArea.id=${subAreaId}&showArea=${showArea}&productCategoryId=${productCategoryId}&noSubEnginner=${noSubEnginner}&noblackList=${noblackList}&address="+addr + "&parentLayerIndex=" + parentLayerIndex,
            shade: ${noModal?0:0.3},
            area: ['${width}px', '${height}px'],
            maxmin: true,
            yes: function(index, layero){
                var iframeWin = parent.window['layui-layer-iframe' + index];
                var data = iframeWin.data;
                <%--var data = h.find("iframe")[0].contentWindow.data;--%>
                $("${formid} #${id}Id").val(data.id);
                $("${formid} #${id}Name").val(data.name);
                if("${callbackmethod}" != ""){
                    ${callbackmethod}(data);
                }
                top.layer.close(index);
            },
            btn2: function(index, layero){
                top.layer.close(index);
            },
            btn3: function(index, layero){
                $("${formid} #${id}Id").val("").trigger('change');
                $("${formid} #${id}Name").val("");
            },
            success: function(layero,index){
//				top.layer.full(index);
//                top.layer.style(index, {
//                    'margin-top': '10px'
//                });
            }
        });
    });

    $(document).ready(function () {
        $('a[data-toggle=tooltip]').darkTooltip();
    });

    function setServicePoint(data){
        <%--var data = h.find("iframe")[0].contentWindow.data;--%>
        $("${formid} #${id}Id").val(data.id);
        $("${formid} #${id}Name").val(data.name);
        if("${callbackmethod}" != ""){
            ${callbackmethod}(data);
        }
    }
</script>
