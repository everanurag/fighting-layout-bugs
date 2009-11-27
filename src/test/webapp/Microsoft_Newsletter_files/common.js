var ie = (document.styleSheets && document.all)?true:false;
var ie5 = (ie && !document.compatMode)?true:false;
var ie6 = (ie && document.compatMode && !window.XMLHttpRequest) ? true:false;
var ie7 = (ie && document.compatMode && window.XMLHttpRequest && !window.getComputedStyle) ? true:false;

var DD = new Object();
DD.config = new Object();
	DD.config.isNS4 = (document.layers)?true:false;
	DD.config.isIE = (document.styleSheets && document.all && navigator.userAgent.toLowerCase().indexOf("opera") == -1)?true:false;
	DD.config.isNS6 = (document.getElementById && !document.all)?true:false;
	DD.config.isOpera = (document.all && document.styleSheets && navigator.userAgent.toLowerCase().indexOf("opera") > -1)?true:false;
	DD.config.isOS_WIN = (navigator.appVersion.indexOf('Win') != -1)?true:false;


DD.loadInit = function(){
	DD.bodyHeight();
}

DD.resizeInit = function(){
	DD.bodyHeight();
}

DD.bodyHeight = function(){
	var SYS_winHeight=((!DD.config.isOpera) ? document.documentElement.clientHeight : window.innerHeight);
	if($("dvBodyContainer_middle").offsetHeight<$("dvBodyContainer_minHeight").offsetHeight || $("dvBodyContainer_middle").offsetHeight<SYS_winHeight){
		$("dvBodyContainer_bottom").style.height=SYS_winHeight + "px";
	}
	if($("dvCntVal_left").offsetHeight<445){$("dvCntVal_left").style.height="445px";}
	if($("dvCntVal_right").offsetHeight<445){$("dvCntVal_right").style.height="445px";}
}

/*
Class DD.navi
*/
DD.navi = {
	img: [],

	register: function(anchorElementId, srcActive, srcInactive) {
		var newEntry = {
			on : new Image(),
			off : new Image()
		};
		newEntry.on.src = srcActive;
		newEntry.off.src = srcInactive;
		this.img[anchorElementId] = newEntry;
	},

	setState: function(anchorElement,active) {
		if(anchorElement.firstChild
			&& anchorElement.firstChild.src
			&& this.img[anchorElement.id])
		{
			var entry = this.img[anchorElement.id];
			anchorElement.firstChild.src= ((active) ? entry.on : entry.off).src;
		}
	},

	highlightSubNavi: function(callObj,callAct){
		callObj.className=((callAct) ? "dvSubNav_item_hover" : "dvSubNav_item");
	}

};



// register navi items
//DD.navi.register("nav_01", "images/navi/nav_01_on.jpg", "images/navi/nav_01_off.jpg");
//DD.navi.register("nav_02", "images/navi/nav_02_on.jpg", "images/navi/nav_02_off.jpg");
//DD.navi.register("nav_03", "images/navi/nav_03_on.jpg", "images/navi/nav_03_off.jpg");
//DD.navi.register("nav_04", "images/navi/nav_04_on.jpg", "images/navi/nav_04_off.jpg");


Object.extend = function(destination, source) {
  for (var property in source) {
    destination[property] = source[property];
  }
  return destination;
}

Object.extend(Object, {
  inspect: function(object) {
    try {
      if (object == undefined) return 'undefined';
      if (object == null) return 'null';
      return object.inspect ? object.inspect() : object.toString();
    } catch (e) {
      if (e instanceof RangeError) return '...';
      throw e;
    }
  },

  keys: function(object) {
    var keys = [];
    for (var property in object)
      keys.push(property);
    return keys;
  },

  values: function(object) {
    var values = [];
    for (var property in object)
      values.push(object[property]);
    return values;
  },

  clone: function(object) {
    return Object.extend({}, object);
  }
});


DD.qlContIsOpened=false;
DD.qlInterval=3;
DD.qlWidthSpeed=10;
DD.qlHeightSpeed=5;
DD.qlHideTimer=false;
DD.isOverQl=false;
DD.viewQuickLinks = function(callObj){
	if(DD.qlSlideTimer){return false;}
	DD.qlDvObj=document.getElementById("dvQuickLinks");
	if(!DD.qlContIsOpened){
		DD.qlDvObj.style.display="block";
		DD.qlDvObj.style.top=(DD.findPosY(callObj)+callObj.offsetHeight) + "px";
		DD.qlDvObj.style.right=( (document.documentElement.clientWidth - (DD.findPosX(callObj)+callObj.offsetWidth))) + "px";

		DD.qlWidth=545;
		DD.qlHeight=299;
		DD.qlDvObj.style.width="5px";
		DD.qlDvObj.style.height="29px";
	}else{
		DD.qlWidth=5;
	}

	DD.qlSlideTimer=setInterval("DD.slideQuickLinks(" + ((DD.qlContIsOpened) ? true : false) + ")",DD.qlInterval);
}

DD.slideQuickLinks=function(direction){
	DD.qlDvObj.style.width=((!DD.qlContIsOpened) ? (DD.qlDvObj.offsetWidth + DD.qlWidthSpeed) : (DD.qlDvObj.offsetWidth - DD.qlWidthSpeed)) + "px";
	DD.qlDvObj.style.height=((!DD.qlContIsOpened) ? (DD.qlDvObj.offsetHeight + DD.qlHeightSpeed) : (DD.qlDvObj.offsetHeight - DD.qlHeightSpeed)) + "px";
	window.status=DD.qlDvObj.offsetWidth;
	if( (direction && DD.qlDvObj.offsetWidth<=DD.qlWidth) || (!direction && DD.qlDvObj.offsetWidth>=DD.qlWidth)){
		clearInterval(DD.qlSlideTimer);
		DD.qlSlideTimer=false;
		DD.qlContIsOpened=((direction) ? false : true);
		DD.qlDvObj.style.display=((direction) ? "none" : "block");
		if(!direction && !DD.isOverQl){
			DD.qlHideTimer=setTimeout('DD.viewQuickLinks(document.getElementById(\'urlQL_opener\'))',1000);
		}else if(direction){
			clearTimeout(DD.qlHideTimer);
		}
	}
}

DD.findPosX = function(obj){
	var curleft = 0;
	if (obj.offsetParent){while (obj.offsetParent){curleft += obj.offsetLeft;obj = obj.offsetParent;};}else if (obj.x){curleft += obj.x;}
	return curleft;
}
DD.findPosY = function(obj){
	var curtop = 0;
	if (obj.offsetParent){while (obj.offsetParent){curtop += obj.offsetTop;obj = obj.offsetParent;};}else if (obj.y){curtop += obj.y;}
	return curtop;
}

function $() {
	var elements = new Array();
	for (var i = 0; i < arguments.length; i++) {
		var element = arguments[i];
		if (typeof element == 'string')
			element = document.getElementById(element);
		if (arguments.length == 1)
			return element;
		elements.push(element);
	}
	return elements;
}
window.onload=function(){
	DD.loadInit();
}

window.onresize=function(){
	DD.resizeInit()
}








var guid;
var old_guid;
function dd_navi_show(guid){
    document.getElementById("ebene_1_"+guid).style.display="block"; // aktuelles navielement auf true setzen
    try{ //versuche
      if(guid != old_guid){ //wenn die alte guid nicht die selbe ist wie die aktuelle (wichtig- sonst würde die navigation wenn sie mal aktiv war, wieder verschwinden)
          document.getElementById("ebene_1_"+old_guid).style.display="none"; // setze das alte element auf display:none 
      }
    }catch(ex){}
   
    if(guid != old_guid){
      old_guid = guid; //wichtig erst hier herunten wird die alte guid gesetzt
    }
}


var f_guid;
var fh_guid;
var f_old_guid;

function dd_flyout_hide(fh_guid){
  document.getElementById("fly_"+fh_guid).style.display="none"; // setze das alte element auf display:none 
}


var e_guid;
function ebene2(e_guid){
  document.getElementById("ebene_2_"+e_guid).className="dvSubNav_item_hover";
}


function CSS() {}

CSS.getOffsetAbsoluteLeft = function(obj) {
	var x = 0;
	var dx = 0;
	while(obj) {
		if (obj) {
			dx = parseInt(obj.offsetLeft);
			if (!isNaN(dx)) x += dx;
			if (obj.style) {
				if ("absolute"==obj.style.position) return x;
			}
		}
		obj = obj.offsetParent;
	}
	return x;
}


function findPosX(obj){
	var curleft = 0;
	if (obj.offsetParent){while (obj.offsetParent){curleft += obj.offsetLeft;obj = obj.offsetParent;};}else if (obj.x){curleft += obj.x;}
	return curleft;
}



var arVersion = navigator.appVersion.split("MSIE")
var version = parseFloat(arVersion[1])
var f_guid='';
var f_old_guid='';
var offset='';

function dd_flyout(f_guid,offset){


offset=(offset);

if (version > 6.5){
		offset = (offset-20);
	} 
    



	document.getElementById("ebene_2_"+f_guid).className="dvSubNav_item_hover"; 

	document.getElementById("fly_"+f_guid).style.display="block"; // aktuelles navielement auf true setzen
  document.getElementById("fly_"+f_guid).style.left=offset+"px";
  
  try{ //versuche
    if(f_guid != f_old_guid){ //wenn die alte guid nicht die selbe ist wie die aktuelle (wichtig- sonst würde die navigation wenn sie mal aktiv war, wieder verschwinden)
      document.getElementById("fly_"+f_old_guid).style.display="none"; // setze das alte element auf display:none 
    	document.getElementById("ebene_2_"+f_old_guid).className="dvSubNav_item"; //alte id auf false setzen
		}
  }catch(ex){}
   
  if(f_guid != f_old_guid){
    f_old_guid = f_guid; //wichtig erst hier herunten wird die alte guid gesetzt
  }


}

function changenavi(id,stat, offIconPath, onIconPath){
	hideallhover();
	if(stat=='0'){
		document.getElementById("img_0"+id).src=offIconPath;
	}
	if(stat=='1'){
		document.getElementById("img_0"+id).src=onIconPath;
	}

}
