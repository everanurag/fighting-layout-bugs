function foldupdown(obj){
		//alert(objLink.parentNode.id);
		
		var objLink = obj;
		var objBox = $(objLink.parentNode.id + "_down");
		//alert(objLink.parentNode.id);
		
		if (objBox.style.display == "none"){
			Effect.BlindDown(objBox, {duration:0.5});
		}else{
			Effect.BlindUp(objBox, {duration:0.5});
		}
		
	}
