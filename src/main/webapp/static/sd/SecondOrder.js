/**
 * 灯饰下单功能
 * 依赖layer弹窗
 */
var SecondOrder={
    init:function(){
    	this.rownum=0;
        this.data={};
        this.rootUrl="";
        this.reload = false;
        this.customerId = "";
        this.hideChargeColumn = false;
        this.version = "1.0";
        this.clickTag = 0;
    },
    //重置数据
    resetData:function(){
        if(SecondOrder.data) {
            SecondOrder.data['shops'] = [];//店铺
            SecondOrder.data['productMap'] = {};//产品服务
            SecondOrder.data['specMap'] = {};//规格
            SecondOrder.data['specProductMap'] = {};//规格与产品关联
            SecondOrder.data['brandMap'] = {}; //品牌
            SecondOrder.data['productTypeMap'] = {};
            SecondOrder.data["serviceMap"] = {};
        }
    },
    //[下单] Step1: - 装载客户账户余额,信用额度，店铺及一级分类，二级分类列表和已添加的订单产品
    loadCustomerInfoWhenCreate:function (customerId,isChangeCustomer) {
        var self = this;
        if(isChangeCustomer && isChangeCustomer === true){
            SecondOrder.resetSelect2("shopId", "", "请选择", true);
        }
        SecondOrder.resetData();
        SecondOrder.resetProductSelectCard(true);
        if(!customerId || customerId === "0"){
            return;
        }
        var loadingIndex;
        //get data from web
        $.ajax(
            {
                url : self.rootUrl+"/sd/order/createOrEdit/v2/getCustomerInfoForCreateOrder",
                type : "GET",
                data : {customerId:customerId},
                contentType : "application/json",
                beforeSend: function () {
                    loadingIndex = layer.msg('正在读取数据，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
                },
                complete: function () {
                    if (loadingIndex) {
                        layer.close(loadingIndex);
                    }
                },
                success : function(data) {
                    if (ajaxLogout(data)) {
                        return false;
                    }
                    if (data.success == false) {
                        layerError(data.message, "错误提示");
                        return;
                    }

                    if (data.productTypes.length == 0) {
                        $("#btnSubmit").prop("disabled", true);
                        $("#btnItemSubmit").prop("disabled", true);
                        $("#productType").prop("disabled", true);
                        $("#productTypeItem").prop("disabled", true);
                        layerAlert("系统后台未维护产品分类，请通知业务或管理员。", "系统提示");
                        return;
                    }else{
                        $("#productType").prop("disabled", false);
                        $("#productTypeItem").prop("disabled", false);
                    }
                    /*缓存中已添加的服务项*/
                    if (data && data.hasOwnProperty("items") && data.items && data.items.length > 0) {
                        var tmpl = document.getElementById('orderForm-item-table-row').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var trnew = doTtmpl(data);
                        $(trnew).insertBefore($("#tr_summry"));
                        $("#customerId").prop("disabled", true);
                        SecondOrder.refreshItemGallery();
                    }
                    self.hideChargeColumns();
                    var dbalance = data.customer.balance - data.customer.blockAmount;
                    $("#balance").text(dbalance.toFixed(2));
                    $("#credit").text(data.customer.credit.toFixed(2));
                    var amount = data.customer.balance + data.customer.credit - data.customer.blockAmount;
                    if (amount <= 0) {
                        $("#btnSubmit").prop("disabled", true);
                        $("#btnItemSubmit").removeClass("btn-primary").addClass("btn-danger").prop("disabled",true);
                        $("#productType").prop("disabled", true);
                        $("#productTypeItem").prop("disabled", true);
                        var msg = "您的账户余额不足,请尽快充值。<br>可下单金额：" + amount.toFixed(2);
                        layerAlert(msg, "系统提示");
                        return;
                    }else {
                        $("#btnItemSubmit").removeClass("btn-danger").removeClass("disabled").addClass("btn-primary").prop("disabled", false);
                        $("#productType").prop("disabled", false);
                        $("#productTypeItem").prop("disabled", false);
                    }
                    SecondOrder.customerId = customerId;
                    var options = [];
                    $("#shopId").empty();
                    // SecondOrder.data['shops'] = [];
                    if (data && data.customer && data.customer.shops && data.customer.shops.length > 0) {
                        var shops = data.customer.shops;
                        SecondOrder.data['shops'] = shops;
                        options = [];
                        options.push('<option value="" data-channel="1" selected="selected">请选择</option>');
                        if (shops && shops.length > 0) {
                            $.each(shops, function (i, item) {
                                options.push('<option value="' + item.value + '" data-channel="' + item.sort + '">' + item.label + '</option>');
                            });
                        }
                        $("#shopId").append(options.join(' '));
                    }
                    //产品分类
                    // SecondOrder.data['productTypeMap'] = {};
                    if(data && data.productTypes){
                        options = [];
                        options.push('<option value="0" selected="selected">请选择</option>');
                        //按一级分类分组
                        var productTypeMap = arrayToMap2(data.productTypes, function (item) {
                            options.push('<option value="' + item.id + '" data-categoryid="' + item.referId + '" data-categoryname="' + item.referName + '">' + item.name + '</option>');
                            return item.id
                        },function (item){
                            return item.items;
                        });
                        // data['productTypeMap'] = productTypeMap;
                        SecondOrder.data['productTypeMap'] = productTypeMap;
                        // $("#productType").empty();
                        $("#productType").empty().append(options.join(' '));
                    }
                    /*物流 改用延迟装载
                    if(data && data.expresses && data.expresses.length > 0){
                        options = [];
                        options.push('<option value="0" selected="selected">请选择</option>');
                        $.each(data.expresses, function (i, item) {
                            options.push('<option value="' + item.value + '">' + item.label + '</option>');
                        });
                        $("#expressCompany").empty().append(options.join(' '));
                    }*/
                    var rowsQty = $("#productTable").find("tbody>tr").not("#tr_summry").length;
                    if(rowsQty > 0){
                        $("#btnSubmit").removeClass("btn-danger").addClass("btn-primary").prop("disabled",false);
                    }else{
                        $("#btnSubmit").removeClass("btn-primary").addClass("btn-danger").prop("disabled",true);
                    }
                },
                error : function(e)
                {
                    ajaxLogout(e.responseText,null,"装载产品分类错误，请重试!");
                }
            });
        return false;
    },
    //[下单] Step2: 根据二级分类读取产品及规格,品牌及服务列表等信息
    loadProductSpecAndInfoForCreateOrder:function(){
        var self = this;
        SecondOrder.resetSelect2("productSpec","0","请选择",true);
        SecondOrder.resetSelect2("product","0","请选择",true);
        SecondOrder.resetSelect2("serviceType","0","请选择",true);
        SecondOrder.resetSelect2("brand","","请选择",true);
        $("#divUploadFile").find(".upload_warp_left").not("#btnSelectFile").remove();
        SecondOrder.refreshGalleryQty();

        var customerId = $("[id='customer.id']").val();
        var productTypeItemId = $("#productTypeItem").val();
        if(productTypeItemId === "" || productTypeItemId === "0"){
            return false;
        }
        var specs = SecondOrder.data['specMap'] || {};
        var spec = specs[productTypeItemId];
        if(!spec){
            var productTypeItemName = $("productTypeItem option:selected").text();
            if(customerId && productTypeItemId){
                var loadingIndex;
                $.ajax(
                    {
                        url : self.rootUrl+"/sd/order/createOrEdit/v2/getProductSpecAndInfoForCreateOrder",
                        type : "GET",
                        data : {customerId: customerId ,productTypeItemId: productTypeItemId},
                        contentType : "application/json",
                        beforeSend: function () {
                            loadingIndex = layer.msg('正在读取数据，请稍等...', {
                                icon: 16,
                                time: 0,
                                shade: 0.3
                            });
                        },
                        complete: function () {
                            if (loadingIndex) {
                                layer.close(loadingIndex);
                            }
                        },
                        success : function(data) {

                            if (ajaxLogout(data)) {
                                return false;
                            }
                            if (data.success == false) {
                                layerError(data.message, "错误提示");
                                return;
                            }
                            if(!data.data || data.data.length == 0) {
                                return;
                            }
                            var rtnData = JSON.parse(data.data);
                            if (rtnData) {
                                /*
                                if (rtnData.hasOwnProperty("specs") && rtnData.specs.length == 0) {
                                    $("#btnSubmit").prop("disabled", true);
                                    $("#btnItemSubmit").prop("disabled", true);
                                    layerAlert("系统后台未维护产品分类:" + productTypeItemName + " 的规格，请通知业务或管理员。", "系统提示");
                                    return;
                                }else{
                                    $("#btnSubmit").prop("disabled", false);
                                    $("#btnItemSubmit").prop("disabled", false);
                                }*/
                                //products
                                if(rtnData.hasOwnProperty("products") && rtnData.products.length >0){
                                    var products = SecondOrder.data["productMap"] || {};
                                    rtnData.products.forEach(function (o) {
                                        var p = products[o.id];
                                        if(!p){
                                            products[o.id] = o;
                                        }
                                    });
                                    SecondOrder.data["productMap"] = products;
                                    //specProductMap存储:[二级分类id] = [{"id": 178,"name": "简易吊灯(1头)","serviceTypes": [2,2]}]
                                    var specProductMap = SecondOrder.data['specProductMap'] || {};
                                    specProductMap[productTypeItemId] = rtnData.products;
                                    SecondOrder.data["specProductMap"] = specProductMap;
                                }
                                //specMap存储: [二级分类id] = [{id,name,products},{}]
                                specs[productTypeItemId] = rtnData.specs || [];
                                SecondOrder.data['specMap'] = specs;
                                spec = rtnData.specs;
                                SecondOrder.fillSpecs(productTypeItemId);
                                if(rtnData.hasOwnProperty("brands") && rtnData.brands.length >0){
                                    var brands = SecondOrder.data["brandMap"] || {};
                                    rtnData.brands.forEach(function (o) {
                                        var p = brands[o.id];
                                        if(!p){
                                            brands[o.id] = o;
                                        }
                                    });
                                    SecondOrder.data["brandMap"] = brands;
                                }
                                if(rtnData.hasOwnProperty("services") && rtnData.services.length >0){
                                    var services = SecondOrder.data["serviceMap"] || {};
                                    rtnData.services.forEach(function (o) {
                                        var p = services[o.id];
                                        if(!p){
                                            services[o.id] = o;
                                        }
                                    });
                                    SecondOrder.data["serviceMap"] = services;
                                }
                            }
                        },
                        error : function(e)
                        {
                            ajaxLogout(e.responseText,null,"装载产品信息失败误，请重试!");
                        }
                    });
            }
        }else{
            SecondOrder.fillSpecs(productTypeItemId);
        }
        return false;
    },
    // [下单] Step3: 读取快递信息(.data.expresses)，已转载无需装载
    loadExpressInfo:function() {
        if (SecondOrder.data && SecondOrder.data.hasOwnProperty("expresses") && SecondOrder.data.expresses.length > 0) {
            return;
        }
        var self = this;
        //get data from web
        $.ajax(
            {
                url: self.rootUrl + "/sd/order/createOrEdit/v2/getExpressInfo",
                type: "GET",
                data: {},
                contentType: "application/json",
                success: function (data) {
                    if (ajaxLogout(data)) {
                        return false;
                    }
                    if (data.success == false) {
                        layerError(data.message, "错误提示");
                        return;
                    }

                    if (!data.data || data.data.length == 0) {
                        return;
                    }
                    SecondOrder.data["expresses"] = data.data;/*缓存*/
                    var options = [];
                    options.push('<option value="" selected="selected">请选择</option>');
                    $.each(data.data, function (i, item) {
                        options.push('<option value="' + item.value + '">' + item.label + '</option>');
                    });
                    $("#expressCompany").empty().append(options.join(' '));
                },
                error: function (e) {
                    //ajaxLogout(e.responseText, null, "装载产品分类错误，请重试!");
                }
            });
        return false;
    },
    //选择分类后刷新二级分类列表
    fillProductTypeItems:function(){
        SecondOrder.resetSelect2("productTypeItem","0","请选择",true);
        SecondOrder.resetSelect2("productSpec","0","请选择",true);
        SecondOrder.resetSelect2("product","0","请选择",true);
        SecondOrder.resetSelect2("serviceType","0","请选择",true);
        SecondOrder.resetSelect2("brand","","请选择",true);
        $("#divUploadFile").find(".upload_warp_left").not("#btnSelectFile").remove();
        SecondOrder.refreshGalleryQty();

        var productTypeId = $("#productType").val();
        var productTypeMap = SecondOrder.data['productTypeMap'] || {};
        var produtType = productTypeMap[productTypeId];
        if(produtType && produtType.length >0){
            var options = [];
            // options.push('<option value="0" selected="selected">请选择</option>');
            $.each(produtType, function (i, item) {
                options.push('<option value="' + item.id + '">' + item.name + '</option>');
            });
            $("#productTypeItem").append(options.join(' '));
        }
    },
    //刷新规格列表
    fillSpecs:function(productTypeItemId){
        SecondOrder.resetSelect2("product","0","请选择",true);
        SecondOrder.resetSelect2("serviceType","0","请选择",true);
        SecondOrder.resetSelect2("brand","","请选择",true);
        $("#brand").empty();
        if(!productTypeItemId){
            productTypeItemId = $("#productTypeItem").val();
        }
        var specs = SecondOrder.data['specMap'] || {};
        var spec = specs[productTypeItemId];
        if(spec){
            var options = [];
            options.push('<option value="0" selected="selected">请选择</option>');
            //按二级分类分组
            spec.forEach(function(o){
                options.push('<option value="' + o.id + '">' + o.name + '</option>');//spec
            });
            $("#productSpec").empty().append(options.join(' '));
            $("#productSpec").trigger("change");
        }else{
            SecondOrder.resetSelect2("productSpec","0","请选择",true);
        }
    },
    //选择规格后刷新产品列表
    fillProducts:function(){
        SecondOrder.resetSelect2("product","0","请选择",true);
        SecondOrder.resetSelect2("serviceType","0","请选择",true);
        SecondOrder.resetSelect2("brand","","请选择",true);
        $("#serviceType").change();
        //手动输入
        $("#divBrand").html('<input type="text" class="model input-block-level" id="brand" name="brand" value="" maxlength="100" placeholder="品牌" />');

        var productTypeItemId = $("#productTypeItem").val();
        var specId = parseInt($("#productSpec").val());
        if(productTypeItemId === 0){
            return false;
        }
        if(specId > 0) {
            var specs = SecondOrder.data['specMap'] || {};
            var specArr = specs[productTypeItemId];//array
            var spec;
            if (specArr && specArr.length > 0) {
                var filterRtn = specArr.filter(item => item.id === specId);
                if (filterRtn.length > 0) {
                    spec = filterRtn[0];
                }
            }
            if (spec && spec.products && spec.products.length > 0) {
                var options = [];
                // options.push('<option value="0" selected="selected">请选择</option>');
                var productMap = SecondOrder.data['productMap'] || {};
                spec.products.forEach(function (id) {
                    var p = productMap[id];
                    if (p) {
                        options.push('<option value="' + p.id + '">' + p.name + '</option>');//spec
                    }
                });
                $("#product").append(options.join(' '));
            }
        }else{
            //load all products
            var specProductMap = SecondOrder.data["specProductMap"] || {};
            var options = [];
            //按二级分类分组
            var specProduct = specProductMap[productTypeItemId] || [];
            specProduct.forEach(function(op){
                options.push('<option value="' + op.id + '">' + op.name + '</option>')
            });
            //products
            if(options && options.length>0){
                $("#product").append(options.join(' '));
            }
        }
    },
    //选择产品后刷新品牌及服务项目列表
    fillBrandAndServices:function(){
        $("#serviceType").empty();
        //手动输入
        $("#divBrand").html('<input type="text" class="model input-block-level" id="brand" name="brand" value="" maxlength="100" placeholder="品牌" />');
        var pId = $("#product").val();
        var productMap = SecondOrder.data['productMap'] || {};
        var product = productMap[pId];
        if(product){
            //品牌
            if(product.brands && product.brands.length>0){
                var selBrand = '<select id="brand" name="brand" class="model input-block-level">';
                var brandMap = SecondOrder.data['brandMap'] || {};
                var brand;
                $.each(product.brands, function(i, brandId) {
                    brand = brandMap[brandId];
                    if(brand){
                        selBrand = selBrand + "<option value='"+brand.name+"' data-id='" + brand.id + "' ";
                        if(i === 0){
                            selBrand = selBrand + " selected ";
                        }
                        selBrand = selBrand + ">" + brand.name + "</option>";
                    }
                });
                selBrand = selBrand + "</select>";
                $("#divBrand").html(selBrand);
                $("#brand").select2();
            }
            //服务项目
            var options = [];
            //options.push('<option value="0" selected="selected">请选择</option>');
            var serviceMap = SecondOrder.data['serviceMap'] || {};
            var serviceTypeId;
            var serviceArr = [];
            product.serviceTypes.forEach(function (id) {
                var s = serviceMap[id];
                if(s){
                    serviceArr.push({id:s.id,name:s.name,sort:s.sort});
                    /*
                    if(!serviceTypeId){
                        serviceTypeId = id;
                    }
                    options.push('<option value="' + s.id + '">' + s.name + '</option>');//spec
                     */
                }
            });
            serviceArr = serviceArr.sort(function(a,b){return a.sort-b.sort});
            serviceArr.forEach(function (s) {
                if(!serviceTypeId){
                    serviceTypeId = id;
                }
                options.push('<option value="' + s.id + '">' + s.name + '</option>');//spec
            });
            $("#serviceType").empty().append(options.join(' '));
            if(serviceTypeId) {
                $("#serviceType").val(serviceTypeId).change();
            }
        }
    },
    //下单时根据选择读取加急费，并刷新页面
    getUrgentCharge:function(areaId){
        //console.log('getUrgentCharge:start');
        var self = this;
        var customerId = $("[id='customer.id']").val();
        if(Utils.isNull(areaId)){
            areaId = $("#areaId").val();
        }
        var urgentLevelId = $("input:radio[name='urgentLevel.id']:checked").val();
        if (urgentLevelId == '0' || urgentLevelId == ''){
            $("#btnSubmit").prop("disabled", false);
        }
        //init
        if( Utils.isNull(customerId) || customerId == ''
            || Utils.isNull(areaId) || areaId == ''
            || Utils.isNull(urgentLevelId) || urgentLevelId == '' || urgentLevelId == '0'){
            $("#customerUrgentCharge").val("0.00");
            $("#engineerUrgentCharge").val("0.00");
            $("#lblUrgentCharge").text("0.0");
            $("#lblUrgent").text("0.0");
            var expectCharge = parseFloat($("#lblassignedCharge").text());
            var blockCharge = parseFloat($("#lblblockedCharge").text());
            var total = expectCharge+blockCharge;
            $("#lbltotalCharge").text(total.toFixed(2));
            //console.log('getUrgentCharge:customer isnull');
            return false;
        }
        //console.log(data);
        $.ajax({
            url : self.rootUrl+"/sd/order/getCustomerUrgentCharge?customerId=" + (customerId || '') + "&areaId=" + (areaId || '') + "&urgentLevelId=" + (urgentLevelId || '') + "&checkCustomer=false",
            type : "POST",
            success : function(data)
            {
                if(ajaxLogout(data)){
                    return false;
                }
                if(data.success == false){
                    $("#btnSubmit").prop("disabled", true);
                    layerError(data.message,"加急费");
                    return;
                }
                $("#customerUrgentCharge").val(data.data.chargeIn);
                $("#engineerUrgentCharge").val(data.data.chargeOut);
                $("#lblUrgentCharge").text(data.data.chargeIn.toFixed(1));
                $("#lblUrgent").text(data.data.chargeIn.toFixed(1));
                var expectCharge = parseFloat($("#lblassignedCharge").text());
                var blockCharge = parseFloat($("#lblblockedCharge").text());
                var total = expectCharge + blockCharge + data.data.chargeIn;
                $("#lbltotalCharge").text(total.toFixed(2));
                $("#btnSubmit").prop("disabled", false);
                //console.log('getUrgentCharge:success');
                return false;
            },
            error : function(e)
            {
                $("#btnSubmit").prop("disabled", true);
                $("#customerUrgentCharge").val("0.00");
                $("#engineerUrgentCharge").val("0.00");
                $("#lblUrgentCharge").text("0.0");
                $("#lblUrgent").text("0.0");
                var expectCharge = parseFloat($("#lblassignedCharge").text());
                var blockCharge = parseFloat($("#lblblockedCharge").text());
                var total = expectCharge+blockCharge;
                $("#lbltotalCharge").text(total.toFixed(2));
                ajaxLogout(e.responseText,null,"读取加急费用错误，请重试!");
                //console.log('getUrgentCharge:error');
                e.preventDefault();
            }
        });
        //console.log('getUrgentCharge:end');
        return false;
    },
    //重置下单界面
    resetOrderForm:function(isChangeCustomer){
        if(isChangeCustomer && isChangeCustomer === true){
            $("#balance").text("0.00");
            $("#credit").text("0.00");
            $("#lblUrgentCharge").text("0.00");
            $("[id='urgentLevel.id']").val("0");//不加急
            $("#btnSubmit").addClass("btn-danger").prop("disabled",true);
            $("#shopId").empty();
        }
        SecondOrder.resetProductSelectCard(true);
    },
    // 重置产品选择卡内容
    resetProductSelectCard:function(clearAll){
        //切换客户时，产品，品牌，服务类型都充值
        $("#qty").val("1");
        $("#expressNo").val("");
        $("#divUploadFile").find(".upload_warp_left").not("#btnSelectFile").remove();
        SecondOrder.refreshGalleryQty();
        if(clearAll && clearAll === true){
            //清空列表清空列表
            SecondOrder.resetSelect2("productType","0","请选择",true);
            SecondOrder.resetSelect2("expressCompany","","请选择");
        }else {
            //不清空列表，重置默认值
            SecondOrder.resetSelect2("productType", "0", "请选择");
            SecondOrder.resetSelect2("expressCompany", "", "请选择");
        }
        //以下控件根据传入参数处理
        SecondOrder.resetSelect2("productTypeItem","0","请选择",true);
        SecondOrder.resetSelect2("productSpec", "0", "请选择", true);
        SecondOrder.resetSelect2("product", "0", "请选择", true);
        SecondOrder.resetSelect2("brand", "", "", true);
        SecondOrder.resetSelect2("serviceType", "0", "请选择", true);
    },
    //重置选择控件
    resetSelect2:function(id,val,label,resetOptions){
        if(!id){
            return false;
        }
        var _ctl = $("#" + id);
        _ctl.val(val || "0");
        if(_ctl.is("select") ){
            if(resetOptions && resetOptions === true){
                var _sel = $("#" + id);
                _sel.empty();
                _sel.append('<option value="' + (val || "0") + '" selected="selected">'+ (label || '请选择') + '</option>');
            }
            $("#s2id_" + id).find("span.select2-chosen").html(label || '请选择');
        }
    },
    //打开图库选择会上传界面
    selectGallery:function(){
        var self = this;
        var cid = $("[id='customer.id']").val();
        if(!cid || cid === "0" || cid === ""){
            layerAlert("请选择客户","系统提示");
            return false;
        }
        var productTypeId = $("#productType").val();
        if(!productTypeId || productTypeId === "0" || productTypeId === ""){
            layerAlert("请选择分类","系统提示");
            return false;
        }
        var selOption = $("#productType option:selected");
        var categoryId = selOption.data("categoryid");
        if(!categoryId || categoryId === "0" || categoryId === ""){
            layerAlert("请选择的分类无品类信息","系统提示");
            return false;
        }
        var productTypeItemId = $("#productTypeItem").val();
        if(!productTypeItemId || productTypeItemId === "0" || productTypeItemId === ""){
            layerAlert("请选择二级分类","系统提示");
            return false;
        }
        var maxSelectGalleryQty = parseInt($("#maxSelectGalleryQty").val());
        /*
        var selectedQty = parseInt($("#selectedQty").val());
        if(selectedQty >= maxSelectGalleryQty){
            layerAlert("每个产品最多选择" + maxSelectGalleryQty + "张产品图片！","系统提示");
            return false;
        }*/
        var selIds = [];
        $("#divUploadFile").find(".upload_warp_left").not("#btnSelectFile").find("img").each(function(i,value){
            selIds.push($(value).data("id"));
        });
        var path = self.rootUrl + "/sd/order/createOrEdit/v2/getProductGallery?customerId=" + cid + '&productCategoryId=' + categoryId
         + "&productTypeId=" + productTypeId + "&productTypeItemId=" + productTypeItemId;
        // path = path + "&limitQty=" + (maxSelectGalleryQty-selectedQty);
        path = path + "&limitQty=" + maxSelectGalleryQty;
        path = path + "&selIds=" + selIds.join(",");
        top.galleryLayerid = top.layer.open({
            type: 2,
            id: 'layer_select_gallery',
            title: '下单-产品图片',
            maxmin: true,
            area : ['760px', '550px'],
            content: path,
            resize:true,
            btn: ['确定', '关闭'],
            yes: function(index, layero){
                var self = this;
                var iframeWin = top.window['layui-layer-iframe' + index];
                var pics = [];
                // var pics = $("#selGallery",iframeWin.document).val();
                var options = $("#selGallery option:selected",iframeWin.document);
                if(!options){
                    layerMsg("请先选择产品图片");
                    return false;
                }
                $.each(options, function (idx, option) {
                    var _option = $(option);
                    pics.push({id: _option.val() ,src: _option.data("img-src")});
                });
                if(!pics || pics.length === 0){
                    layerMsg("请先选择产品图片");
                    return false;
                }
                var tmpl = document.getElementById('tpl-gallery').innerHTML;
                var doTtmpl = doT.template(tmpl);
                var html = doTtmpl(pics);
                $("#divUploadFile").find(".upload_warp_left").not("#btnSelectFile").remove();
                $("#btnSelectFile").before(html.replace(/'/g,"\'"));//btnUploadFile前
                //更新已选择数量
                SecondOrder.refreshGalleryQty();
                //重新绑定删除事件
                $("a.upload_warp_img_div_del").off().on("click",SecondOrder.removeSelectGallery);
                $("#btnUploadFile").removeClass("imgOnDarg");
                $("#divUploadFile").viewer('destroy').viewer({ url: "data-original"});
                top.layer.close(index);
            },
            btn2: function(layero,index){
            },
            success: function(layero,index){
                // var picUrls = $(top.document.body).find('#layui-layer-iframe' + top.galleryLayerid).contents().find("#picUrls").val();
                // top.layer.close(top.SearchTeacherLayerId);
            },
            end:function(index){

            }
        });
    },
    //移除选择的图片
    removeSelectGallery:function(){
        var self = this;
        var galleryEl = $(this).closest(".upload_warp_left");
        if(galleryEl){
            galleryEl.remove();
            SecondOrder.refreshGalleryQty();
        }
    },
    //删除图库
    deleteGallery:function() {
        var self = SecondOrder;
        var divParents = $(this).parents("div.thumbnail");
        if (!divParents) {
            return false;
        }
        var galleryId = divParents.data("option-value");
        if (Utils.isNull(galleryId) || galleryId == '' || galleryId == '0') {
            return false;
        }
        top.layer.confirm(
            '确定删除此产品图片吗？'
            , {
                id: 'layer_del_galerry', icon: 3, closeBtn: 0, title: '系统确认', success: function (layro, index) {
                    $(document).on('keydown', layro, function (e) {
                        if (e.keyCode == 13) {
                            layro.find('a.layui-layer-btn0').trigger('click')
                        } else if (e.keyCode == 27) {
                            top.layer.close(index);//关闭本身
                        }
                    });
                }
            }, function (index) {
                var loadingIndex;
                $.ajax({
                    async: false,
                    cache: false,
                    type: "POST",
                    url: self.rootUrl + "/sd/order/createOrEdit/v2/deleteProductGallery?galleryId=" + (galleryId || '0'),
                    beforeSend: function () {
                        loadingIndex = top.layer.msg('正在提交，请稍等...', {
                            icon: 16,
                            time: 0,
                            shade: 0.3
                        });
                    },
                    complete: function () {
                        if (loadingIndex) {
                            top.layer.close(loadingIndex);
                        }
                    },
                    success: function (data) {
                        if (ajaxLogout(data)) {
                            return false;
                        }
                        if (data.success == false) {
                            layerError(data.message, "错误提示");
                            return false;
                        }
                        $('#selGallery option[value="' + galleryId + '"]').remove();
                        refreshGallery();//galleryUpload.html
                        SecondOrder.refreshGalleryQty();
                        return false;
                    },
                    error: function (e) {
                        if (loadingIndex) {
                            top.layer.close(loadingIndex);
                        }
                        ajaxLogout(e.responseText, null, "删除图库失败，请重试!");
                    }
                });
            }, function (index) {
                //cancel
            });
            return false;
    },
    //刷新上传数量信息
    refreshGalleryQty:function(){
        $("#divUploadFile").viewer('destroy').viewer({ url: "data-original"});
        var maxQty = parseInt($("#maxSelectGalleryQty").val());
        var selectedQty = $("#divUploadFile").find(".upload_warp_img_div").length;
        $("#selectedQty").val(selectedQty);
        $("a.retain-qty").text(maxQty - selectedQty);
    },
    //ajax提交添加订单项
    addOrderItem:function(){
        var self = this;
        var $productType = $("#productType");
        var ptId = $productType.val();
        if(!ptId || ptId === "0" || ptId ===""){
            layerAlert("请选择分类","信息提示");
            lock = false;
            $("#s2id_productType").addClass("select2-container-active");
            return true;
        }
        var ptiId = $("#productTypeItem").val();
        if(!ptiId || ptiId === "0" || ptiId ===""){
            layerAlert("请选择二级分类","信息提示");
            lock = false;
            $("#s2id_productTypeItem").addClass("select2-container-active");
            return true;
        }
        var $product = $("#product");
        var pid = $product.val();
        if(!pid || pid === "0" || pid ===""){
            layerAlert("请选择产品","信息提示");
            lock = false;
            $("#s2id_product").addClass("select2-container-active");
            return true;
        }
        var stid = $("#serviceType").val();
        if(!stid || stid === "0" || stid ===""){
            layerAlert("请选择服务类型","信息提示");
            lock = false;
            $("#s2id_serviceType").addClass("select2-container-active");
            return true;
        }

        var qty = $("#qty").val();
        if(!Utils.isPositiveInteger(qty)){
            layerAlert("数量应是整数类型，并且大于0","信息提示");
            lock = false;
            $("#qty").focus();
            return true;
        }
        var brand = $("#brand").val();
        if(brand && brand.indexOf(",") != -1){
            layerAlert("产品只能设定一个品牌","系统提示",true);
            lock = false;
            return false;
        }
        var expressCompany=$("#expressCompany").val();
        var expressCompanyName = "";
        if(expressCompany === "" || expressCompany === "0"){
            expressCompanyName = "";
        }else{
            expressCompanyName = $("#expressCompany").find("option:selected").text();
        }
        var expressNo=$("#expressNo").val();
        if(!Utils.isEmpty(expressCompanyName) && Utils.isEmpty(expressNo))
        {
            layerAlert("请输入快递单号","系统提示");
            lock = false;
            $("#expressno").focus();
            return true;
        }
        var pics = [];
        $.each($("#divUploadFile").find(".upload_warp_img_div").find("img"),function (i, item) {
            pics.push($(item).data("original"));
        });
        /*
        if(pics.length === 0){
            layerAlert("请选择或上传产品图片","系统提示");
            lock = false;
            return true;
        }*/
        var balance = parseFloat($("#balance").text());
        var credit = parseFloat($("#credit").text());
        var pName = $product.find("option:selected").text();
        var $selOption = $productType.find("option:selected");
        var ptName = $selOption.text();
        var categoryId = $selOption.data("categoryid");
        var categoryName = $selOption.data("categoryname");
        var ptiName = $("#productTypeItem").find("option:selected").text();
        var sName = $("#serviceType").find("option:selected").text();
        var pspecVal = $("#productSpec").val();
        var pspec = $("#productSpec option:selected").text();//规格
        if(pspecVal === "0"){
            pspec = "";
        }

        var cid = $("[id='customer.id']").val();
        var urgentLevelId = "0";
        var urgentLevelName = "";
        if($("#urgentLevelId").length>0){
            var optionSelected = $("#urgentLevelId").find("option:selected");
            urgentLevelId = optionSelected.val();
            urgentLevelName = optionSelected.text();
        }
        var customerUrgentCharge = 0;
        if($("#customerUrgentCharge").length>0) {
            customerUrgentCharge = $("#customerUrgentCharge").val();
        }
        var engineerUrgentCharge = 0;
        if($("#engineerUrgentCharge").length>0) {
            engineerUrgentCharge = $("#engineerUrgentCharge").val();
        }
        var action = $("#action").val() || 'newv2';
        var item = {
            "customerId":cid,
            "balance":balance,
            "credit":credit,
            "product.category.id":categoryId,
            "product.category.name":categoryName,
            "product.id":pid,
            "product.name":pName,
            "serviceType.id":stid,
            "serviceType.name":sName,
            "qty":qty,
            "brand":brand,
            "productSpec":pspec,
            "expressCompany.value":expressCompany,
            "expressCompany.label":expressCompanyName,
            "expressNo":expressNo,
            "remarks":"",
            "urgentLevel.id":urgentLevelId,
            "urgentLevel.remarks":urgentLevelName,
            "customerUrgentCharge":customerUrgentCharge,
            "engineerUrgentCharge":engineerUrgentCharge,
            "productType.name":ptId,
            "productType.value":ptName,
            "productTypeItem.name":ptiId,
            "productTypeItem.value":ptiName,
            "action": action
        };
        $.each(pics,function(i,pic){
            item['pics[' + i + ']'] = pic;
        });

        //post
        $.ajax({
            type: "POST",
            url: self.rootUrl+"/sd/order/saveitem?t="+ (new Date()).getTime(),
            data: item,
            complete: function () {
                lock = false;
            },
            success: function (data) {
                if(ajaxLogout(data)){
                    return false;
                }
                if(data && data.success == true){
                    SecondOrder.hideChargeColumn = self.hideChargeColumn;
                    SecondOrder.refreshOrderItemRows(data.data);
                }
                else if( data && data.message){
                    layerError(data.message,"错误提示");
                }
                else{
                    layerError("添加产品到订单详细清单时错误","错误提示");
                }
                return false;

            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"添加产品错误，请重试!");
            }
        });
    },
    //删除选中产品项
    delProductRows:function(){
        var self = this;
        if($("#productTable input[type='checkbox'][name='checkedRecords']:checkbox:checked").length == 0){
            clickTag = 0;
            layerInfo("请选择要删除的明细.","系统提示");
            return;
        }
        var itemids = [];

        $("#productTable input[type='checkbox'][name='checkedRecords']:checkbox:checked").each(function(){
            var id = $(this).val();
            var itemid = $("[id='items["+id+"].id']").val();
            if(!Utils.isEmpty(itemid) && itemid !='0' && itemid != 'undefined' && itemid != undefined){
                itemids.push(itemid);
            }
            else{
                itemid = $("[id='items["+id+"].tmpId']").val();
                if(!Utils.isEmpty(itemid)){
                    itemids.push(itemid);
                }
            }
        });

        if(itemids.length>0){
            var balance = parseFloat($("#balance").text());
            var credit = parseFloat($("#credit").text());
            var action = $("#action").val() || 'newv2';
            var data = {ids: itemids.join(","),balance: balance ,credit: credit ,"action": action};
            $.ajax({
                url: self.rootUrl+"/sd/order/delnewitem?"+ (new Date()).getTime(),
                type: "POST",
                data: data,
                cache:false,
                complete: function () {
                    setTimeout(function(){
                        clickTag = 0;
                    },1000);
                },
                success: function(data, status) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if(data && data.success == true){
                        SecondOrder.refreshOrderItemRows(data.data);
                        $("#selectAll").attr("checked", false);
                    }
                    else if( data && data.message){
                        layerError(data.message,"错误提示");
                    }
                    else{
                        layerError("删除订单详细清单时错误","错误提示");
                    }
                },
                error: function(request, status, error) {
                    ajaxLogout(request.responseText,null,"删除订单详细清单时错误，请重试!");
                }
            });
        }else{
            clickTag = 0;
        }
        return false;
    },
    //下单-在前端显示新增的订单产品
    refreshOrderItemRows:function(data) {
        var self = this;
        if (!data) {
            return;
        }
        $("#productTable").find("tbody>tr").not("#tr_summry").remove();

        $('#lblassignedCharge').text(data.expectCharge);
        $('#expectCharge').val(data.expectCharge);
        $("#lblblockedCharge").text(data.blockedCharge);
        $('#blockedCharge').val(data.blockedCharge);
        $("#lbltotalQty").text(data.totalQty);
        $("#totalQty").val(data.totalQty);

        var customerUrgentCharge = 0;
        if($("#customerUrgentCharge").length>0){
            customerUrgentCharge = parseFloat($("#customerUrgentCharge").val());
        }
        $("#lbltotalCharge").text(data.blockedCharge + data.expectCharge + customerUrgentCharge);

        var balance = parseFloat($("#balance").text());
        var credit= parseFloat($("#credit").text());
        if (balance + credit < data.blockedCharge + data.expectCharge) {
            //$("#lbltotalCharge").removeClass("alert-success").addClass("alert-error");
            $("#btnSubmit").removeClass("btn-primary").addClass("btn-danger").addClass("disabled").prop("disabled",true);
            $("#btnItemSubmit").removeClass("btn-primary").addClass("btn-danger").addClass("disabled").prop("disabled",true);
        } else {
            // $("#lbltotalCharge").removeClass("alert-error").addClass("alert-success");
            $("#btnSubmit").removeClass("btn-danger").removeClass("disabled").addClass("btn-primary").prop("disabled",false);
            $("#btnItemSubmit").removeClass("btn-danger").removeClass("disabled").addClass("btn-primary").prop("disabled",false);
        }
        var hasItems = true;
        if(!data.hasOwnProperty("items") || data.items.length === 0){
            hasItems = false;
        }
        //客服添加订单，有订单项后不允许更改客户
        if ($("#customerId").length>0) {
            if (hasItems) {
                $("#customerId").prop("disabled", true);
            } else {
                $("#customerId").removeAttr("disabled");
            }
        }
        if(hasItems) {
            var tmpl = document.getElementById('orderForm-item-table-row').innerHTML;
            var doTtmpl = doT.template(tmpl);
            var trnew = doTtmpl(data);
            $(trnew).insertBefore($("#tr_summry"));
            SecondOrder.refreshItemGallery();
        }
        self.hideChargeColumns();
    },
    //刷新已添加产品项图片浏览
    refreshItemGallery:function(){
        $("div.viewer-container").remove();
        SecondOrder.refreshGalleryQty();//产品选择卡
        $.each($("#productTable").find("div.gallery-thumb"),function(i,thumb){
            $(thumb).viewer('destroy').viewer({ url: "data-original"});
        });
    },
    //下单时根据选择读取加急费，并刷新页面
    getUrgentCharge:function(areaId){
        //console.log('getUrgentCharge:start');
        var self = this;
        var customerId = $("[id='customer.id']").val();
        if(Utils.isNull(areaId)){
            areaId = $("#areaId").val();
        }
        var urgentLevelId = $("input:radio[name='urgentLevel.id']:checked").val();
        if (urgentLevelId == '0' || urgentLevelId == ''){
            $("#btnSubmit").prop("disabled", false);
        }
        //init
        //$("#btnSubmit").prop("disabled") == true ||
        if( Utils.isNull(customerId) || customerId == ''
            || Utils.isNull(areaId) || areaId == ''
            || Utils.isNull(urgentLevelId) || urgentLevelId == '' || urgentLevelId == '0'){
            $("#customerUrgentCharge").val("0.00");
            $("#engineerUrgentCharge").val("0.00");
            $("#lblUrgentCharge").text("0.0");
            $("#lblUrgent").text("0.0");
            var expectCharge = parseFloat($("#lblassignedCharge").text());
            var blockCharge = parseFloat($("#lblblockedCharge").text());
            var total = expectCharge+blockCharge;
            $("#lbltotalCharge").text(total.toFixed(2));
            //console.log('getUrgentCharge:customer isnull');
            return false;
        }
        //console.log(data);
        $.ajax({
            url : self.rootUrl+"/sd/order/getCustomerUrgentCharge?customerId=" + (customerId || '') + "&areaId=" + (areaId || '') + "&urgentLevelId=" + (urgentLevelId || '') + "&checkCustomer=false",
            type : "POST",
            success : function(data)
            {
                if(ajaxLogout(data)){
                    return false;
                }
                if(data.success == false){
                    $("#btnSubmit").prop("disabled", true);
                    layerError(data.message,"加急费");
                    return;
                }
                $("#customerUrgentCharge").val(data.data.chargeIn);
                $("#engineerUrgentCharge").val(data.data.chargeOut);
                $("#lblUrgentCharge").text(data.data.chargeIn.toFixed(1));
                $("#lblUrgent").text(data.data.chargeIn.toFixed(1));
                var expectCharge = parseFloat($("#lblassignedCharge").text());
                var blockCharge = parseFloat($("#lblblockedCharge").text());
                var total = expectCharge + blockCharge + data.data.chargeIn;
                $("#lbltotalCharge").text(total.toFixed(2));
                $("#btnSubmit").prop("disabled", false);
                //console.log('getUrgentCharge:success');
                return false;
            },
            error : function(e)
            {
                $("#btnSubmit").prop("disabled", true);
                $("#customerUrgentCharge").val("0.00");
                $("#engineerUrgentCharge").val("0.00");
                $("#lblUrgentCharge").text("0.0");
                $("#lblUrgent").text("0.0");
                var expectCharge = parseFloat($("#lblassignedCharge").text());
                var blockCharge = parseFloat($("#lblblockedCharge").text());
                var total = expectCharge+blockCharge;
                $("#lbltotalCharge").text(total.toFixed(2));
                ajaxLogout(e.responseText,null,"读取加急费用错误，请重试!");
                //console.log('getUrgentCharge:error');
                e.preventDefault();
            }
        });
        //console.log('getUrgentCharge:end');
        return false;
    },
    //修改订单-显示订单产品
    editFormShowItemRows:function(data) {
        var self = this;
        if (!data) {
            return;
        }
        var hasItems = true;
        if(!data.hasOwnProperty("items") || data.items.length === 0){
            hasItems = false;
        }

        $('#lblassignedCharge').text(data.expectCharge);
        $('#expectCharge').val(data.expectCharge);
        $("#lblblockedCharge").text(data.blockedCharge);
        $('#blockedCharge').val(data.blockedCharge);
        $("#lbltotalQty").text(data.totalQty);
        $("#totalQty").val(data.totalQty);
        var customerUrgentCharge = 0;
        if($("#customerUrgentCharge").length>0){
            customerUrgentCharge = parseFloat($("#customerUrgentCharge").val());
        }
        var totalCharge = data.blockedCharge + data.expectCharge + customerUrgentCharge;
        $("#lbltotalCharge").text(totalCharge.toFixed(2));

        var balance = parseFloat($("#balance").text());
        var credit= parseFloat($("#credit").text());
        if (balance + credit < data.blockedCharge + data.expectCharge) {
            $("#btnSubmit").removeClass("btn-primary").addClass("btn-danger").addClass("disabled");
        } else {
            $("#btnSubmit").removeClass("btn-danger").removeClass("disabled").addClass("btn-primary");
        }
        if(hasItems) {
            var tmpl = document.getElementById('orderForm-item-table-row').innerHTML;
            var doTtmpl = doT.template(tmpl);
            var trnew = doTtmpl(data);
            $(trnew).insertBefore($("#tr_summry"));
            SecondOrder.refreshItemGallery();
        }
        self.hideChargeColumns();
    },
    //[修改订单] 装载客户店铺，物流及一级分类，二级分类列表
    loadCustomerInfoWhenEdit:function (customerId,shopId) {
        var self = this;
        if(!customerId || customerId === "" || customerId === "0"){
            return;
        }
        //get data from web
        $.ajax(
            {
                url : self.rootUrl+"/sd/order/createOrEdit/v2/getProductTypesForEditOrder",
                type : "GET",
                data : {customerId:customerId},
                contentType : "application/json",
                beforeSend: function () {
                },
                complete: function () {
                },
                success : function(data) {
                    if (ajaxLogout(data)) {
                        return false;
                    }
                    if (data.success == false) {
                        layerError(data.message, "错误提示");
                        return;
                    }

                    if (data.productTypes.length == 0) {
                        // $("#btnSubmit").prop("disabled", true);
                        layerAlert("系统后台未维护产品分类，请通知业务或管理员。", "系统提示");
                        return;
                    }
                    var options = [];
                    $("#shopId").empty();
                    SecondOrder.data['shops'] = [];
                    if (data && data && data.shops && data.shops.length > 0) {
                        var shops = data.shops;
                        SecondOrder.data['shops'] = shops;
                        options = [];
                        options.push('<option value="" data-channel="1" selected="selected">请选择</option>');
                        if (shops && shops.length > 0) {
                            var selectFlag = '';
                            $.each(shops, function (i, item) {
                                options.push('<option value="' + item.value + '" data-channel="' + item.sort + '">' + item.label + '</option>');
                            });
                        }
                        $("#shopId").append(options.join(' '));
                        //选中
                        if(shopId){
                            $("#shopId").val(shopId);
                            $("#shopId").trigger("change");
                        }
                    }
                    //产品分类
                    SecondOrder.data['productTypeMap'] ={};
                    if(data && data.productTypes){
                        options = [];
                        options.push('<option value="0" selected="selected">请选择</option>');
                        //按一级分类分组
                        var productTypeMap = arrayToMap2(data.productTypes, function (item) {
                            options.push('<option value="' + item.id + '" data-categoryid="' + item.referId + '" data-categoryname="' + item.referName + '">' + item.name + '</option>');
                            return item.id
                        },function (item){
                            return item.items;
                        });
                        SecondOrder.data['productTypeMap'] = productTypeMap;
                        $("#productType").empty().append(options.join(' '));
                    }
                    /*物流*/
                    if(data && data.expresses && data.expresses.length > 0){
                        options = [];
                        options.push('<option value="0" selected="selected">请选择</option>');
                        $.each(data.expresses, function (i, item) {
                            options.push('<option value="' + item.value + '">' + item.label + '</option>');
                        });
                        $("#expressCompany").empty().append(options.join(' '));
                    }
                },
                error : function(e)
                {
                    ajaxLogout(e.responseText,null,"装载产品分类错误，请重试!");
                }
            });
        return false;
    },
    hideChargeColumns:function (){
        var self = this;
        if(self.hideChargeColumn){
            //head
            $("#productTable thead tr th:eq(10)").hide();
            $("#productTable thead tr th:eq(11)").hide();
            //row
            $("#productTable tr td:nth-child(11)").hide();
            $("#productTable tr td:nth-child(12)").hide();
        }
    },
};

$(function(){
    SecondOrder.init();
});

