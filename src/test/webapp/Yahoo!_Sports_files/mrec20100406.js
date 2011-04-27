function mrec_window(x){if(mrec_target=="_top"){top.location=mrec_URL[x];}else{window.open(mrec_URL[x]);}}
var plugin=(navigator.mimeTypes&&navigator.mimeTypes["application/x-shockwave-flash"])?navigator.mimeTypes["application/x-shockwave-flash"].enabledPlugin:0;
if(plugin){plugin=parseInt(plugin.description.substring(plugin.description.indexOf(".")-2))>=9;}
else if(navigator.userAgent&&navigator.userAgent.indexOf("MSIE")>=0&&navigator.userAgent.indexOf("Windows")>=0){
document.write('<SCR'+'IPT LANGUAGE=VBScr'+'ipt\>\n'
+'on error resume next\n'
+'plugin=(IsObject(CreateObject("ShockwaveFlash.ShockwaveFlash.'+mrec_fver+'")))\n'
+'</SCR'+'IPT\>\n');}
if(mrec_target&&mrec_URL&&mrec_fv&&mrec_swf&&mrec_altURL&&mrec_altimg&&mrec_w&&mrec_h){
if(plugin){document.write('<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"'
+' width="'+mrec_w+'" height="'+mrec_h+'">'
+'<param name="movie" value="'+mrec_swf+'" /><param name="wmode" value="opaque" /><param name="loop" value="false" /><param name="quality" value="high" /><param name="allowScriptAccess" value="never" />'
+'<param name="flashvars" value="'+mrec_fv+'" />'
+'<embed src="'+mrec_swf+'" loop="false" wmode="opaque" quality="high"'
+' width="'+mrec_w+'" height="'+mrec_h+'" flashvars="'+mrec_fv+'"'
+' type="application/x-shockwave-flash" allowScriptAccess="never"></embed></object>');
}else{document.write('<a href="'+mrec_altURL+'" target="'+mrec_target+'"><img src="'+mrec_altimg+'" width="'+mrec_w+'" height="'+mrec_h+'" border="0" /></a>');}}